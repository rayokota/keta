# Keta - A Transactional Metadata Store Backed by Apache Kafka

[![Build Status][travis-shield]][travis-link]
[![Maven][maven-shield]][maven-link]
[![Javadoc][javadoc-shield]][javadoc-link]

[travis-shield]: https://travis-ci.org/rayokota/keta.svg?branch=master
[travis-link]: https://travis-ci.org/rayokota/keta
[maven-shield]: https://img.shields.io/maven-central/v/io.kcache/keta-core.svg
[maven-link]: https://search.maven.org/#search%7Cga%7C1%7Cketa-core
[javadoc-shield]: https://javadoc.io/badge/io.kcache/keta-core.svg?color=blue
[javadoc-link]: https://javadoc.io/doc/io.kcache/keta-core

Keta is a transactional metadata store backed by Apache Kafka.

## Maven

Releases of Keta are deployed to Maven Central.

```xml
<dependency>
    <groupId>io.kcache</groupId>
    <artifactId>keta-core</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Getting Started

To run Keta, download a [release](https://github.com/rayokota/keta/releases), unpack it, and then modify `config/keta.properties` to point to an existing Kafka broker.  Then run the following:

```bash
$ bin/keta-start config/keta.properties
```

Keta can be used with any client that supports the etcd v3 APIs.  To use Keta with the `etcdctl` command line client, first download etcd [here](https://github.com/etcd-io/etcd).  Then at a separate terminal, start `etcdctl`.

```
$ etcdctl put mykey "this is awesome"
$ etcdctl get mykey
```

The etcd APIs have a concise way for expressioning transactions.

```
$ etcdctl put user1 bad
$ etcdctl txn --interactive

compares:
value("user1") = "bad"      

success requests (get, put, delete):
del user1  

failure requests (get, put, delete):
put user1 good
```

To expire key-values, use a lease.

```
$ etcdctl lease grant 300
# lease 2be7547fbc6a5afa granted with TTL(300s)

$ etcdctl put sample value --lease=2be7547fbc6a5afa
$ etcdctl get sample

$ etcdctl lease keep-alive 2be7547fbc6a5afa
$ etcdctl lease revoke 2be7547fbc6a5afa
# or after 300 seconds
$ etcdctl get sample
```

To receive change notifications, use a watch.

```
$ etcdctl watch stock --prefix
```

Then at a separate terminal, enter the following:

```
$ etcdctl put stock1 10
$ etcdctl put stock2 20
```

If multiple Keta servers are configured with the same cluster group ID (see [Basic Configuration](#basic-configuration)), then they will form a cluster and one of them will be elected as leader, while the others will become followers (replicas).  If a follower receives a request, it will be forwarded to the leader.  If the leader fails, one of the followers will be elected as the new leader.



## Basic Configuration

Keta has a number of configuration properties that can be specified.  

- `listeners` - List of listener URLs that include the scheme, host, and port.  Defaults to `http://0.0.0.0:2379`.  
- `cluster.group.id` - The group ID to be used for leader election.  Defaults to `keta`.
- `leader.eligibility` - Whether this node can participate in leader election.  Defaults to true.
- `kafkacache.backing.cache` - The backing cache for KCache, one of `memory` (default), `bdbje`, `lmdb`, `mapdb`, or `rocksdb`.
- `kafkacache.data.dir` - The root directory for backing cache storage.  Defaults to `/tmp`.
- `kafkacache.bootstrap.servers` - A list of host and port pairs to use for establishing the initial connection to Kafka.
- `kafkacache.group.id` - The group ID to use for the internal consumers, which needs to be unique for each node.  Defaults to `keta-1`.
- `kafkacache.topic.replication.factor` - The replication factor for the internal topics created by Keta.  Defaults to 3.
- `kafkacache.init.timeout.ms` - The timeout for initialization of the Kafka cache, including creation of internal topics.  Defaults to 300 seconds.
- `kafkacache.timeout.ms` - The timeout for an operation on the Kafka cache.  Defaults to 60 seconds.

## Security

### HTTPS

To use HTTPS, first configure the `listeners` with an `https` prefix, then specify the following properties with the appropriate values.

```
ssl.keystore.location=/var/private/ssl/custom.keystore
ssl.keystore.password=changeme
ssl.key.password=changeme
ssl.truststore.location=/var/private/ssl/custom.truststore
ssl.truststore.password=changeme
```


### Authentication and Role-Based Access Control

Keta supports the same authentication and role-based access control (RBAC) APIs as etcd.  For more info, see the etcd documentation [here](https://etcd.io/docs/v3.4.0/op-guide/authentication/).


### Kafka Authentication

Authentication to a secure Kafka cluster is described [here](https://github.com/rayokota/kcache#security).
 
## Implementation Notes


Keta uses seven topics to hold metadata:

- `_keta_kv` - A topic that holds the key-values.
- `_keta_leases` - A topic that holds leases.
- `_keta_auth` - A topic that holds auth configuration.
- `_keta_auth_users` - A topic that holds auth users.
- `_keta_auth_roles` - A topic that holds auth roles.
- `_keta_commits` - A topic that holds the list of committed transactions.
- `_keta_timestamps` - A topic that stores the maximum timestamp that the transaction manager is allowed to return to clients.

For more info on Keta, see this [blog post](https://yokota.blog/2020/11/09/keta-a-metadata-store-backed-by-apache-kafka/).
