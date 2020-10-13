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
        final IntervalTree<String> intervalTree = new IntervalTree<String>();
        Iterator<IntervalTree.Node<String>> results = intervalTree.overlappers(1, 500);
        assertEquals(countElements(results), 0, "Testing with no left-hand set failed.");

        // Test no matches at all
        intervalTree.put(1, 400, "foo");
        intervalTree.put(600, 800, "foo2");
        results = intervalTree.overlappers(450, 599);
        assertEquals(countElements(results), 0, "Testing with no overlaps at all.");

    }

    private int countElements(final Iterator<IntervalTree.Node<String>> it) {
        int ret = 0;
        while (it.hasNext()) {
            ++ret;
            it.next();
        }
        return ret;
    }

    private final IntervalTree<String> intervalTree = new IntervalTree<String>();

    @BeforeEach
    public void init() { //due to the destructive nature of removeMany test...
        intervalTree.clear();

        // each interval has a "name:length"
        intervalTree.put(1, 10, "foo1:10");
        intervalTree.put(2, 9, "foo2:8");
        intervalTree.put(3, 8, "foo3:6");
        intervalTree.put(4, 7, "foo4:4");
        intervalTree.put(5, 6, "foo5:2");
        intervalTree.put(1, 9, "foo6:9");
    }

    @Test
    public void testLength() {

        Iterator<IntervalTree.Node<String>> iterator = intervalTree.iterator();
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;

        for (IntervalTree.Node<String> node : iterable) {
            assertEquals(node.getLength(), Integer.parseInt(node.getValue().replaceAll(".*:", "")));
        }
    }

    public Object[][] adjacentIntervalsTestData() {
        return new Object[][]{
            {1, 4, 5, 10, true},
            {1, 3, 5, 10, false},
            {1, 4, 6, 10, false},
            {1, 2, 6, 10, false},
            {1, 10, 6, 10, false},
            {1, 10, 11, 20, true},
            {1, 10, 11, 20, true},
        };
    }

    @Test
    public void testAdjacent() {
        for (Object[] data : adjacentIntervalsTestData()) {
            testAdjacent((Integer) data[0], (Integer) data[1], (Integer) data[2], (Integer) data[3], (Boolean) data[4]);
        }
    }

    private void testAdjacent(int start1, int end1, int start2, int end2, boolean areAdjacent) {

        final IntervalTree.Node<String> node1 = new IntervalTree.Node<>(start1, end1, "one");
        final IntervalTree.Node<String> node2 = new IntervalTree.Node<>(start2, end2, "two");

        assertTrue(node1.isAdjacent(node2) == areAdjacent);
        assertTrue(node2.isAdjacent(node1) == areAdjacent);
    }

    @Test
    public void testRank() {
        for (IntervalTree.Node<String> node : intervalTree) {
            assertEquals(intervalTree.findByIndex(
                intervalTree.getIndex(node.getStart(), node.getEnd())), node);
        }
    }

    @Test
    public void testIterator() {

        final IntervalTree.Node<String> testNode = new IntervalTree.Node<>(3, 4, "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.iterator(testNode.getStart(), testNode.getEnd());
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;
        for (IntervalTree.Node<String> node : iterable) {
            assertTrue(node.compare(testNode.getStart(), testNode.getEnd()) <= 0);
            count++;
        }
        assertEquals(count, 3); // foobar3, foobar4, and foobar5 only.
    }

    @Test
    public void testRemoveMany() {
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.reverseIterator();
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;

        for (IntervalTree.Node<String> node : iterable) {
            intervalTree.removeNode(node);
        }
        assertEquals(intervalTree.size(), 0);
    }

    @Test
    public void testRevIterator() {

        final IntervalTree.Node<String> testNode = new IntervalTree.Node<>(3, 4, "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.reverseIterator(testNode.getStart(), testNode.getEnd());
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;
        for (IntervalTree.Node<String> node : iterable) {
            assertTrue(node.compare(testNode.getStart(), testNode.getEnd()) >= 0);
            count++;
        }
        assertEquals(count, 3); // foobar1, foobar2, and foobar6
    }


    @Test
    public void testOverlapIterator() {

        final IntervalTree.Node<String> testNode = new IntervalTree.Node<>(3, 4, "foobar1");
        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.overlappers(testNode.getStart(), testNode.getEnd());
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;
        for (IntervalTree.Node<String> node : iterable) {
            count++;
        }
        assertEquals(count, 5); // foobar1, foobar2, foobar3, foobar4, and foobar6
    }


    @Test
    public void testTotalRevIterator() {

        int count = 0;
        Iterator<IntervalTree.Node<String>> iterator = intervalTree.reverseIterator();
        Iterable<IntervalTree.Node<String>> iterable = () -> iterator;

        for (IntervalTree.Node<String> ignored : iterable) {
            count++;
        }
        assertEquals(count, intervalTree.size()); // foobar1, foobar2, and foobar6
    }

    @Test
    public void testMatches() {
        // Single match
        assertEquals(countElements(intervalTree.overlappers(10, 10)), 1, "Test single overlap");
        assertTrue(iteratorContains(intervalTree.overlappers(10, 10), "foo1:10"), "Test single overlap for correct overlapee");

        // Multiple matches
        assertEquals(countElements(intervalTree.overlappers(7, 8)), 5, "Test multiple overlap");
        assertTrue(iteratorContains(intervalTree.overlappers(7, 8), "foo1:10"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(7, 8), "foo2:8"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(7, 8), "foo3:6"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(7, 8), "foo4:4"), "Test multiple overlap for correct overlapees");
        assertTrue(iteratorContains(intervalTree.overlappers(7, 8), "foo6:9"), "Test multiple overlap for correct overlapees");
        assertTrue(!iteratorContains(intervalTree.overlappers(7, 8), "foo5:2"), "Test multiple overlap for correct overlapees");
    }

    private boolean iteratorContains(final Iterator<IntervalTree.Node<String>> nodeIterator, final String s) {
        while (nodeIterator.hasNext()) {
            if (nodeIterator.next().getValue().equals(s)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testNearEnds() {
        final IntervalTree<String> intervalTree = new IntervalTree<String>();
        intervalTree.put(10, 20, "foo");
        assertEquals(countElements(intervalTree.overlappers(10, 10)), 1, "Test overlap (no buffers) at near end exactly");
        assertEquals(countElements(intervalTree.overlappers(9, 10)), 1, "Test overlap (no buffers) at near end exactly");
        assertEquals(countElements(intervalTree.overlappers(9, 9)), 0, "Test just before overlap (no buffers)");
        assertEquals(countElements(intervalTree.overlappers(20, 20)), 1, "Test overlap (no buffers) at far end exactly");
        assertEquals(countElements(intervalTree.overlappers(20, 21)), 1, "Test overlap (no buffers) at far end exactly");
        assertEquals(countElements(intervalTree.overlappers(21, 21)), 0, "Test just beyond overlap (no buffers)");
    }

    @Test
    public void testHandlingOfDuplicateMappings() {
        final IntervalTree<String> intervalTree = new IntervalTree<String>();
        intervalTree.put(1, 10, "foo1");
        // This call replaces foo1 with foo2
        assertEquals(intervalTree.put(1, 10, "foo2"), "foo1");
        intervalTree.put(2, 8, "foo3");

        assertEquals(countElements(intervalTree.overlappers(3, 5)), 2);
        assertFalse(iteratorContains(intervalTree.overlappers(3, 5), "foo1"));
        assertTrue(iteratorContains(intervalTree.overlappers(3, 5), "foo2"));
        assertTrue(iteratorContains(intervalTree.overlappers(3, 5), "foo3"));
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
        IntervalTree<String> intervalTree = new IntervalTree<String>();
        for (int[] add : adds) {
            intervalTree.put(add[0], add[1], "frob");
        }
        assertEquals(intervalTree.remove(46402360, 46402594), "frob");
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
        final IntervalTree<Integer> tree = new IntervalTree<>();
        tree.setSentinel(sentinel);
        values.forEach(value -> {
            tree.merge(10, 20, value, function);
        });
        final Iterator<IntervalTree.Node<Integer>> iterator = tree.iterator();
        assertEquals(iterator.next().getValue(), expected);
        assertFalse(iterator.hasNext());
    }
}