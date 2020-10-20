/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kcache.keta.server.leader;

import org.apache.kafka.clients.GroupRebalanceConfig;
import org.apache.kafka.clients.consumer.internals.AbstractCoordinator;
import org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient;
import org.apache.kafka.common.message.JoinGroupRequestData;
import org.apache.kafka.common.message.JoinGroupResponseData;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.utils.LogContext;
import org.apache.kafka.common.utils.Time;
import org.apache.kafka.common.utils.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This class manages the coordination process with the Kafka group coordinator on the broker for
 * coordinating group members.
 */
final class KetaCoordinator extends AbstractCoordinator implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(KetaCoordinator.class);

    public static final String KDB_SUBPROTOCOL_V0 = "v0";

    private final KetaIdentity identity;
    private KetaProtocol.Assignment assignmentSnapshot;
    private final KetaRebalanceListener listener;

    /**
     * Initialize the coordination manager.
     */
    public KetaCoordinator(
        LogContext logContext,
        ConsumerNetworkClient client,
        String groupId,
        int rebalanceTimeoutMs,
        int sessionTimeoutMs,
        int heartbeatIntervalMs,
        Metrics metrics,
        String metricGrpPrefix,
        Time time,
        long retryBackoffMs,
        KetaIdentity identity,
        KetaRebalanceListener listener) {
        super(
            new GroupRebalanceConfig(
                sessionTimeoutMs,
                rebalanceTimeoutMs,
                heartbeatIntervalMs,
                groupId,
                Optional.empty(),
                retryBackoffMs,
                true
            ),
            logContext,
            client,
            metrics,
            metricGrpPrefix,
            time
        );
        this.identity = identity;
        this.assignmentSnapshot = null;
        this.listener = listener;
    }

    @Override
    public String protocolType() {
        return "kdb";
    }

    public void poll(long timeout) {
        // poll for io until the timeout expires
        final long start = time.milliseconds();
        long now = start;
        long remaining;

        do {
            if (coordinatorUnknown()) {
                ensureCoordinatorReady(time.timer(Long.MAX_VALUE));
                now = time.milliseconds();
            }

            if (rejoinNeededOrPending()) {
                ensureActiveGroup();
                now = time.milliseconds();
            }

            pollHeartbeat(now);

            long elapsed = now - start;
            remaining = timeout - elapsed;

            // Note that because the network client is shared with the background heartbeat thread,
            // we do not want to block in poll longer than the time to the next heartbeat.
            client.poll(time.timer(Math.min(Math.max(0, remaining), timeToNextHeartbeat(now))));

            now = time.milliseconds();
            elapsed = now - start;
            remaining = timeout - elapsed;
        } while (remaining > 0);
    }

    @Override
    public JoinGroupRequestData.JoinGroupRequestProtocolCollection metadata() {
        ByteBuffer metadata = KetaProtocol.serializeMetadata(identity);
        return new JoinGroupRequestData.JoinGroupRequestProtocolCollection(
            Collections.singletonList(new JoinGroupRequestData.JoinGroupRequestProtocol()
                .setName(KDB_SUBPROTOCOL_V0)
                .setMetadata(metadata.array())).iterator());
    }

    @Override
    protected void onJoinComplete(
        int generation,
        String memberId,
        String protocol,
        ByteBuffer memberAssignment
    ) {
        assignmentSnapshot = KetaProtocol.deserializeAssignment(memberAssignment);
        listener.onAssigned(assignmentSnapshot, generation);
    }

    @Override
    protected Map<String, ByteBuffer> performAssignment(
        String kafkaLeaderId, // Kafka group "leader" who does assignment, *not* the cluster leader
        String protocol,
        List<JoinGroupResponseData.JoinGroupResponseMember> allMemberMetadata
    ) {
        LOG.debug("Performing assignment");

        List<KetaIdentity> members = new ArrayList<>();
        Map<String, KetaIdentity> memberConfigs = new HashMap<>();
        for (JoinGroupResponseData.JoinGroupResponseMember entry : allMemberMetadata) {
            KetaIdentity identity
                = KetaProtocol.deserializeMetadata(ByteBuffer.wrap(entry.metadata()));
            memberConfigs.put(entry.memberId(), identity);
            members.add(identity);
        }

        LOG.debug("Member information: {}", memberConfigs);

        // Compute the leader as the leader-eligible member with the "smallest" (lexicographically) ID.
        // This doesn't guarantee a member will stay leader until it leaves the group, but should
        // usually keep the leader assigned to the same member across rebalances.
        KetaIdentity leaderIdentity = null;
        String leaderKafkaId = null;
        Set<String> urls = new HashSet<>();
        for (Map.Entry<String, KetaIdentity> entry : memberConfigs.entrySet()) {
            String kafkaMemberId = entry.getKey();
            KetaIdentity memberIdentity = entry.getValue();
            urls.add(memberIdentity.getUrl());
            boolean eligible = memberIdentity.getLeaderEligibility();
            boolean smallerIdentity = leaderIdentity == null
                || memberIdentity.getUrl().compareTo(leaderIdentity.getUrl()) < 0;
            if (eligible && smallerIdentity) {
                leaderKafkaId = kafkaMemberId;
                leaderIdentity = memberIdentity;
            }
        }
        short error = KetaProtocol.Assignment.NO_ERROR;

        // Validate that group members aren't trying to use the same URL
        if (urls.size() != memberConfigs.size()) {
            LOG.error("Found duplicate URLs for group members. This indicates a "
                + "misconfiguration and is common when executing in containers. Use the host.name "
                + "configuration to set each instance's advertised host name to a value that is "
                + "routable from all other group members.");
            error = KetaProtocol.Assignment.DUPLICATE_URLS;
        }

        // All members currently receive the same assignment information since it is just the leader ID
        Map<String, ByteBuffer> groupAssignment = new HashMap<>();
        KetaProtocol.Assignment assignment = new KetaProtocol.Assignment(error, leaderKafkaId, leaderIdentity, members);
        LOG.debug("Assignment: {}", assignment);
        for (String member : memberConfigs.keySet()) {
            groupAssignment.put(member, KetaProtocol.serializeAssignment(assignment));
        }
        return groupAssignment;
    }

    @Override
    protected void onJoinPrepare(int generation, String memberId) {
        LOG.debug("Revoking previous assignment {}", assignmentSnapshot);
        if (assignmentSnapshot != null) {
            listener.onRevoked();
        }
    }

    @Override
    protected synchronized boolean ensureCoordinatorReady(Timer timer) {
        return super.ensureCoordinatorReady(timer);
    }

    @Override
    protected boolean rejoinNeededOrPending() {
        return super.rejoinNeededOrPending() || assignmentSnapshot == null;
    }
}
