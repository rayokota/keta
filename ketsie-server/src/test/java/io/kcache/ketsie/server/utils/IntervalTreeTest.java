/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kcache.ketsie.server.utils;

import com.google.common.collect.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntervalTreeTest {
    @Test
    public void testNoMatches() {
        // Test empty tree
        final IntervalTree<Integer, String> intervalTree = new IntervalTree<>();
        Iterator<IntervalTree.Node<Integer, String>> results = intervalTree.overlappers(Range.closed(1, 500));
        assertEquals(countElements(results), 0, "Testing with no left-hand set failed.");

        // Test no matches at all
        intervalTree.put(Range.closed(1, 400), "foo");
        intervalTree.put(Range.closed(600, 800), "foo2");
        results = intervalTree.overlappers(Range.closed(450, 599));
        assertEquals(countElements(results), 0, "Testing with no overlaps at all.");

    }

    private int countElements(final Iterator<IntervalTree.Node<Integer, String>> it) {
        int ret = 0;
        while (it.hasNext()) {
            ++ret;
            it.next();
        }
        return ret;
    }

    private final IntervalTree<Integer, String> intervalTree = new IntervalTree<>();

    @BeforeEach
    public void init() { //due to the destructive nature of removeMany test...
        intervalTree.clear();

        // each interval has a "name:length"
        intervalTree.put(Range.closed(1, 10), "foo1:10");
        intervalTree.put(Range.closed(2, 9), "foo2:8");
        intervalTree.put(Range.closed(3, 8), "foo3:6");
        intervalTree.put(Range.closed(4, 7), "foo4:4");
        intervalTree.put(Range.closed(5, 6), "foo5:2");
        intervalTree.put(Range.closed(1, 9), "foo6:9");
    }

    @Test
    public void testRank() {
        for (IntervalTree.Node<Integer, String> node : intervalTree) {
            assertEquals(intervalTree.findByIndex(
                intervalTree.getIndex(node.getInterval())), node);
        }
    }

    @Test
    public void testIterator() {

        final IntervalTree.Node<Integer, String> testNode = new IntervalTree.Node<>(Range.closed(3, 4), "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<Integer, String>> iterator = intervalTree.iterator(testNode.getInterval());
        Iterable<IntervalTree.Node<Integer, String>> iterable = () -> iterator;
        for (IntervalTree.Node<Integer, String> node : iterable) {
            assertTrue(node.compare(testNode.getInterval()) <= 0);
            count++;
        }
        assertEquals(count, 3); // foobar3, foobar4, and foobar5 only.
    }

    @Test
    public void testRemoveMany() {
        Iterator<IntervalTree.Node<Integer, String>> iterator = intervalTree.reverseIterator();
        Iterable<IntervalTree.Node<Integer, String>> iterable = () -> iterator;

        for (IntervalTree.Node<Integer, String> node : iterable) {
            intervalTree.removeNode(node);
        }
        assertEquals(intervalTree.size(), 0);
    }

    @Test
    public void testRevIterator() {

        final IntervalTree.Node<Integer, String> testNode = new IntervalTree.Node<>(Range.closed(3, 4), "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<Integer, String>> iterator = intervalTree.reverseIterator(testNode.getInterval());
        Iterable<IntervalTree.Node<Integer, String>> iterable = () -> iterator;
        for (IntervalTree.Node<Integer, String> node : iterable) {
            assertTrue(node.compare(testNode.getInterval()) >= 0);
            count++;
        }
        assertEquals(count, 3); // foobar1, foobar2, and foobar6
    }


    @Test
    public void testOverlapIterator() {

        final IntervalTree.Node<Integer, String> testNode = new IntervalTree.Node<>(Range.closed(3, 4), "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<Integer, String>> iterator = intervalTree.overlappers(testNode.getInterval());
        Iterable<IntervalTree.Node<Integer, String>> iterable = () -> iterator;
        for (IntervalTree.Node<Integer, String> node : iterable) {
            count++;
        }
        assertEquals(count, 5); // foobar1, foobar2, foobar3, foobar4, and foobar6
    }


    @Test
    public void testTotalRevIterator() {

        int count = 0;
        Iterator<IntervalTree.Node<Integer, String>> iterator = intervalTree.reverseIterator();
        Iterable<IntervalTree.Node<Integer, String>> iterable = () -> iterator;

        for (IntervalTree.Node<Integer, String> ignored : iterable) {
            count++;
        }
        assertEquals(count, intervalTree.size()); // foobar1, foobar2, and foobar6
    }

    @Test
    public void testMatches() {
        // Single match
        assertEquals(countElements(intervalTree.overlappers(Range.closed(10, 10))), 1, "Test single overlap");
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(10, 10)), "foo1:10"), "Test single overlap for correct overlapee");

        // Multiple matches
        assertEquals(countElements(intervalTree.overlappers(Range.closed(7, 8))), 5, "Test multiple overlap");
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(7, 8)), "foo1:10"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(7, 8)), "foo2:8"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(7, 8)), "foo3:6"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(7, 8)), "foo4:4"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(7, 8)), "foo6:9"), "Test multiple overlap for correct overlapees");
        assertTrue(!iteratorContains(intervalTree.overlappers(Range.closed(7, 8)), "foo5:2"), "Test multiple overlap for correct overlapees");
    }

    private boolean iteratorContains(final Iterator<IntervalTree.Node<Integer, String>> nodeIterator, final String s) {
        while (nodeIterator.hasNext()) {
            if (nodeIterator.next().getValue().equals(s)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testNearEnds() {
        final IntervalTree<Integer, String> intervalTree = new IntervalTree<>();
        intervalTree.put(Range.closed(10, 20), "foo");
        assertEquals(countElements(intervalTree.overlappers(Range.closed(10, 10))), 1, "Test overlap (no buffers) at near end exactly");
        assertEquals(countElements(intervalTree.overlappers(Range.closed(9, 10))), 1, "Test overlap (no buffers) at near end exactly");
        assertEquals(countElements(intervalTree.overlappers(Range.closed(9, 9))), 0, "Test just before overlap (no buffers)");
        assertEquals(countElements(intervalTree.overlappers(Range.closed(20, 20))), 1, "Test overlap (no buffers) at far end exactly");
        assertEquals(countElements(intervalTree.overlappers(Range.closed(20, 21))), 1, "Test overlap (no buffers) at far end exactly");
        assertEquals(countElements(intervalTree.overlappers(Range.closed(21, 21))), 0, "Test just beyond overlap (no buffers)");
    }

    @Test
    public void testHandlingOfDuplicateMappings() {
        final IntervalTree<Integer, String> intervalTree = new IntervalTree<>();
        intervalTree.put(Range.closed(1, 10), "foo1");
        // This call replaces foo1 with foo2
        assertEquals(intervalTree.put(Range.closed(1, 10), "foo2"), "foo1");
        intervalTree.put(Range.closed(2, 8), "foo3");

        assertEquals(countElements(intervalTree.overlappers(Range.closed(3, 5))), 2);
        assertFalse(iteratorContains(intervalTree.overlappers(Range.closed(3, 5)), "foo1"));
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(3, 5)), "foo2"));
        assertTrue(iteratorContains(intervalTree.overlappers(Range.closed(3, 5)), "foo3"));
    }

    /**
     * Test of PIC-123
     */
    @Test
    public void testRemove() {
        int[][] adds = {
            {46129744, 46129978},
            {46393843, 46394077},
            {46260491, 46260725},
            {46402360, 46402594},
            {46369255, 46369464},
            {46293772, 46293981},
            {46357687, 46357896},
            {46431752, 46431961},
            {46429997, 46430206},
            {46404026, 46404192},
            {46390511, 46390677},
            {46090593, 46090759},
            {46045352, 46045518},
            {46297633, 46297799},
            {46124297, 46124463},
            {46395291, 46395504},
            {46439072, 46439240},
            {46400792, 46400959},
            {46178616, 46178851},
            {46129747, 46129982},
            {46396546, 46396781},
            {46112353, 46112588},
            {46432996, 46433231},
            {46399109, 46399344},
            {46372058, 46372292},
            {46386826, 46387060},
            {46381795, 46382029},
            {46179789, 46180023},
            {46394409, 46394643},
            {46376176, 46376429},
            {46389943, 46390177},
            {46433654, 46433888},
            {46379440, 46379674},
            {46391117, 46391351},
        };
        IntervalTree<Integer, String> intervalTree = new IntervalTree<>();
        for (int[] add : adds) {
            intervalTree.put(Range.closed(add[0], add[1]), "frob");
        }
        assertEquals(intervalTree.remove(Range.closed(46402360, 46402594)), "frob");
        intervalTree.checkMaxEnds();
    }


    public Object[][] getMergeTestCases() {
        return new Object[][]{
            {add, null, Arrays.asList(0, 1, 2, 3), 6},
            {add, null, Arrays.asList(0, 1, 2), 3},
            {add, null, Arrays.asList(0, 1), 1},
            {add, null, Collections.singletonList(0), 0},
            {addButCountNullAs10, null, Arrays.asList(1, 2, 3), 6},
            {addButCountNullAs10, 0, Arrays.asList(1, 2, null), 13},
            {addButCountNullAs10, 0, Arrays.asList(null, null, null), 30}
        };
    }

    private static final BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
    private static final BiFunction<Integer, Integer, Integer> addButCountNullAs10 = (a, b) -> (a == null ? 10 : a) + (b == null ? 10 : b);

    @Test
    public void testMerge() {
        for (Object[] data : getMergeTestCases()) {
            testMerge((BiFunction<Integer, Integer, Integer>) data[0], (Integer) data[1], (List<Integer>) data[2], (Integer) data[3]);
        }
    }

    private void testMerge(BiFunction<Integer, Integer, Integer> function, Integer sentinel, List<Integer> values, Integer expected) {
        final IntervalTree<Integer, Integer> tree = new IntervalTree<>();
        tree.setSentinel(sentinel);
        values.forEach(value -> {
            tree.merge(Range.closed(10, 20), value, function);
        });
        final Iterator<IntervalTree.Node<Integer, Integer>> iterator = tree.iterator();
        assertEquals(iterator.next().getValue(), expected);
        assertFalse(iterator.hasNext());
    }
}