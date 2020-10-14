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

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

public class IntervalTree<K extends Comparable<K>, V> implements Iterable<IntervalTree.Node<K, V>> {
    /**
     * Return the number of intervals in the tree.
     *
     * @return The number of intervals.
     */
    public int size() {
        return mRoot == null ? 0 : mRoot.getSize();
    }

    /**
     * Remove all entries.
     */
    public void clear() {
        mRoot = null;
    }

    /**
     * Put a new interval into the tree (or update the value associated with an existing interval).
     *
     * @param interval The interval.
     * @param value The associated value.
     * @return The old value associated with that interval, or null.
     */
    public V put(final Range<K> interval, final V value) {

        V result = null;

        if (mRoot == null) {
            mRoot = new Node<>(interval, value);
        } else {
            Node<K, V> parent = null;
            Node<K, V> node = mRoot;
            int cmpVal = 0;

            while (node != null) {
                parent = node; // last non-null node
                cmpVal = node.compare(interval);
                if (cmpVal == 0) {
                    break;
                }

                node = cmpVal < 0 ? node.getLeft() : node.getRight();
            }

            if (cmpVal == 0) {
                result = parent.setValue(value);
            } else {
                if (cmpVal < 0) {
                    mRoot = parent.insertLeft(interval, value, mRoot);
                } else {
                    mRoot = parent.insertRight(interval, value, mRoot);
                }
            }
        }

        return result;
    }

    /**
     * If the specified start and end positions are not already associated with a value,
     * associates it with the given value.
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is null. This
     * method may be of use when combining multiple values that have the same start and end position.
     *
     * @param interval          interval
     * @param value             value to merge into the tree, must not be equal null
     * @param remappingFunction a function that merges the new value with the existing value for the same start and end position,
     *                          if the function returns the null then the mapping will be unset
     * @return the updated value that is stored in the tree after the completion of this merge operation, or null
     */
    public V merge(Range<K> interval, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final V alreadyPresent = put(interval, value);
        if (alreadyPresent != null) {
            final V newComputedValue = remappingFunction.apply(value, alreadyPresent);
            if (newComputedValue == null) {
                remove(interval);
            } else {
                put(interval, newComputedValue);
            }
            return newComputedValue;
        }
        return value;
    }

    /**
     * Remove an interval from the tree.
     *
     * @param interval The interval.
     * @return The value associated with that interval, or null.
     */
    public V remove(final Range<K> interval) {
        V result = null;
        Node<K, V> node = mRoot;

        while (node != null) {
            final int cmpVal = node.compare(interval);
            if (cmpVal == 0) {
                result = node.getValue();
                mRoot = node.remove(mRoot);
                break;
            }

            node = cmpVal < 0 ? node.getLeft() : node.getRight();
        }

        return result;
    }

    /**
     * Find an interval.
     *
     * @param interval The interval.
     * @return The Node that represents that interval, or null.
     */
    public Node<K, V> find(final Range<K> interval) {
        Node<K, V> node = mRoot;

        while (node != null) {
            final int cmpVal = node.compare(interval);
            if (cmpVal == 0) {
                break;
            }

            node = cmpVal < 0 ? node.getLeft() : node.getRight();
        }

        return node;
    }

    /**
     * Find the nth interval in the tree.
     *
     * @param idx The rank of the interval sought (from 0 to size()-1).
     * @return The Node that represents the nth interval.
     */
    public Node<K, V> findByIndex(final int idx) {
        return Node.findByRank(mRoot, idx + 1);
    }

    /**
     * Find the rank of the specified interval.  If the specified interval is not in the
     * tree, then -1 is returned.
     *
     * @param interval The interval.
     * @return The rank of that interval, or -1.
     */
    public int getIndex(final Range<K> interval) {
        return Node.getRank(mRoot, interval) - 1;
    }

    /**
     * Find the least interval in the tree.
     *
     * @return The earliest interval, or null if the tree is empty.
     */
    public Node<K, V> min() {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;

        while (node != null) {
            result = node;
            node = node.getLeft();
        }

        return result;
    }

    /**
     * Find the earliest interval in the tree greater than or equal to the specified interval.
     *
     * @param interval The interval.
     * @return The earliest >= interval, or null if there is none.
     */
    @SuppressWarnings("null")
    public Node<K, V> min(final Range<K> interval) {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;
        int cmpVal = 0;

        while (node != null) {
            result = node;
            cmpVal = node.compare(interval);
            if (cmpVal == 0) {
                break;
            }

            node = cmpVal < 0 ? node.getLeft() : node.getRight();
        }

        if (cmpVal > 0) {
            result = result.getNext();
        }

        return result;
    }

    /**
     * Find the earliest interval in the tree that overlaps the specified interval.
     *
     * @param interval The interval.
     * @return The earliest overlapping interval, or null if there is none.
     */
    public Node<K, V> minOverlapper(final Range<K> interval) {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;

        if (node != null && compareLowerUpper(interval, node.getMaxEnd()) <= 0) {
            while (true) {
                if (node.getInterval().isConnected(interval)) { // this node overlaps.  there might be a lesser overlapper down the left sub-tree.
                    // no need to consider the right sub-tree:  even if there's an overlapper, if won't be minimal
                    result = node;
                    node = node.getLeft();
                    if (node == null || compareLowerUpper(interval, node.getMaxEnd()) > 0)
                        break; // no left sub-tree or all nodes end too early
                } else { // no overlap.  if there might be a left sub-tree overlapper, consider the left sub-tree.
                    final Node<K, V> left = node.getLeft();
                    if (left != null && compareLowerUpper(interval, left.getMaxEnd()) <= 0) {
                        node = left;
                    } else { // left sub-tree cannot contain an overlapper.  consider the right sub-tree.
                        if (compareLowerUpper(node.getInterval(), interval) > 0)
                            break; // everything in the right sub-tree is past the end of the query interval

                        node = node.getRight();
                        if (node == null || compareLowerUpper(interval, node.getMaxEnd()) > 0)
                            break; // no right sub-tree or all nodes end too early
                    }
                }
            }
        }

        return result;
    }

    /**
     * Find the greatest interval in the tree.
     *
     * @return The latest interval, or null if the tree is empty.
     */
    public Node<K, V> max() {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;

        while (node != null) {
            result = node;
            node = node.getRight();
        }

        return result;
    }

    /**
     * Find the latest interval in the tree less than or equal to the specified interval.
     *
     * @param interval The interval.
     * @return The latest >= interval, or null if there is none.
     */
    @SuppressWarnings("null")
    public Node<K, V> max(final Range<K> interval) {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;
        int cmpVal = 0;

        while (node != null) {
            result = node;
            cmpVal = node.compare(interval);
            if (cmpVal == 0) {
                break;
            }

            node = cmpVal < 0 ? node.getLeft() : node.getRight();
        }

        if (cmpVal < 0) {
            result = result.getPrev();
        }

        return result;
    }

    /**
     * Return an iterator over the entire tree.
     *
     * @return An iterator.
     */
    @Override
    public Iterator<Node<K, V>> iterator() {
        return new FwdIterator(min());
    }

    /**
     * Return an iterator over all intervals greater than or equal to the specified interval.
     *
     * @param interval The interval.
     * @return An iterator.
     */
    public Iterator<Node<K, V>> iterator(final Range<K> interval) {
        return new FwdIterator(min(interval));
    }

    /**
     * Return an iterator over all intervals overlapping the specified range.
     *
     * @param interval The interval.
     * @return An iterator.
     */
    public Iterator<Node<K, V>> overlappers(final Range<K> interval) {
        return new OverlapIterator(interval);
    }

    /**
     * Return an iterator over the entire tree that returns intervals in reverse order.
     *
     * @return An iterator.
     */
    public Iterator<Node<K, V>> reverseIterator() {
        return new RevIterator(max());
    }

    /**
     * Return an iterator over all intervals less than or equal to the specified interval, in reverse order.
     *
     * @param interval The interval.
     * @return An iterator.
     */
    public Iterator<Node<K, V>> reverseIterator(final Range<K> interval) {
        return new RevIterator(max(interval));
    }

    /**
     * This method is only for debugging.
     * It verifies whether the tree is internally consistent with respect to the mMaxEnd cached on each node.
     *
     * @throws IllegalStateException If an inconsistency is detected.
     */
    public void checkMaxEnds() {
        if (mRoot != null) mRoot.checkMaxEnd();
    }

    /**
     * This method draws a nested picture of the tree on System.out.
     * Useful for debugging.
     */
    public void printTree() {
        if (mRoot != null) mRoot.printNode();
    }

    void removeNode(final Node<K, V> node) {
        mRoot = node.remove(mRoot);
    }

    private Node<K, V> mRoot;

    public static class Node<K1 extends Comparable<K1>, V1> {

        Node(final Range<K1> interval, final V1 value) {
            mIvl = interval;
            mValue = value;
            mSize = 1;
            mMaxEnd = Range.upTo(interval.upperEndpoint(), interval.upperBoundType());
            mIsBlack = true;
        }

        Node(final Node<K1, V1> parent, final Range<K1> interval, final V1 value) {
            mParent = parent;
            mIvl = interval;
            mValue = value;
            mMaxEnd = Range.upTo(interval.upperEndpoint(), interval.upperBoundType());
            mSize = 1;
        }

        public Range<K1> getInterval() {
            return mIvl;
        }

        public V1 getValue() {
            return mValue;
        }

        public V1 setValue(final V1 value) {
            final V1 result = mValue;
            mValue = value;
            return result;
        }

        int getSize() {
            return mSize;
        }

        Range<K1> getMaxEnd() {
            return mMaxEnd;
        }

        Node<K1, V1> getLeft() {
            return mLeft;
        }

        Node<K1, V1> insertLeft(final Range<K1> interval, final V1 value, final Node<K1, V1> root) {
            mLeft = new Node<>(this, interval, value);
            return insertFixup(mLeft, root);
        }

        Node<K1, V1> getRight() {
            return mRight;
        }

        Node<K1, V1> insertRight(final Range<K1> interval, final V1 value, final Node<K1, V1> root) {
            mRight = new Node<>(this, interval, value);
            return insertFixup(mRight, root);
        }

        Node<K1, V1> getNext() {
            Node<K1, V1> result;

            if (mRight != null) {
                result = mRight;
                while (result.mLeft != null) {
                    result = result.mLeft;
                }
            } else {
                Node<K1, V1> node = this;
                result = mParent;
                while (result != null && node == result.mRight) {
                    node = result;
                    result = result.mParent;
                }
            }

            return result;
        }

        Node<K1, V1> getPrev() {
            Node<K1, V1> result;

            if (mLeft != null) {
                result = mLeft;
                while (result.mRight != null) {
                    result = result.mRight;
                }
            } else {
                Node<K1, V1> node = this;
                result = mParent;
                while (result != null && node == result.mLeft) {
                    node = result;
                    result = result.mParent;
                }
            }

            return result;
        }

        boolean wasRemoved() {
            return mSize == 0;
        }

        Node<K1, V1> remove(Node<K1, V1> root) {
            if (mSize == 0) {
                throw new IllegalStateException("Entry was already removed.");
            }

            if (mLeft == null) {
                if (mRight == null) { // no children
                    if (mParent == null) {
                        root = null;
                    } else if (mParent.mLeft == this) {
                        mParent.mLeft = null;
                        fixup(mParent);

                        if (mIsBlack)
                            root = removeFixup(mParent, null, root);
                    } else {
                        mParent.mRight = null;
                        fixup(mParent);

                        if (mIsBlack)
                            root = removeFixup(mParent, null, root);
                    }
                } else { // single child on right
                    root = spliceOut(mRight, root);
                }
            } else if (mRight == null) { // single child on left
                root = spliceOut(mLeft, root);
            } else { // two children
                final Node<K1, V1> next = getNext();
                root = next.remove(root);

                // put next into tree in same position as this, effectively removing this
                if ((next.mParent = mParent) == null)
                    root = next;
                else if (mParent.mLeft == this)
                    mParent.mLeft = next;
                else
                    mParent.mRight = next;

                if ((next.mLeft = mLeft) != null) {
                    mLeft.mParent = next;
                }

                if ((next.mRight = mRight) != null) {
                    mRight.mParent = next;
                }

                next.mIsBlack = mIsBlack;
                next.mSize = mSize;

                // PIC-123 fix
                fixup(next);
            }

            mSize = 0;
            return root;
        }

        // backwards comparison!  compares start+end to this.
        int compare(final Range<K1> interval) {
            int lower = compareLower(interval, mIvl);
            int upper = compareUpper(interval, mIvl);
            return lower != 0 ? lower : upper;
        }

        @SuppressWarnings("null")
        static <K1 extends Comparable<K1>, V1> Node<K1, V1> getNextOverlapper(Node<K1, V1> node, final Range<K1> interval) {
            do {
                Node<K1, V1> nextNode = node.mRight;
                if (nextNode != null && compareLowerUpper(interval, nextNode.mMaxEnd) <= 0) {
                    node = nextNode;
                    while ((nextNode = node.mLeft) != null && compareLowerUpper(interval, nextNode.mMaxEnd) <= 0)
                        node = nextNode;
                } else {
                    nextNode = node;
                    while ((node = nextNode.mParent) != null && node.mRight == nextNode)
                        nextNode = node;
                }
                if (node != null && compareLowerUpper(node.mIvl, interval) > 0)
                    node = null;
            }
            while (node != null && !node.mIvl.isConnected(interval));

            return node;
        }

        static <K1 extends Comparable<K1>, V1> Node<K1, V1> findByRank(Node<K1, V1> node, int rank) {
            while (node != null) {
                final int nodeRank = node.getRank();
                if (rank == nodeRank)
                    break;

                if (rank < nodeRank) {
                    node = node.mLeft;
                } else {
                    node = node.mRight;
                    rank -= nodeRank;
                }
            }

            return node;
        }

        static <K1 extends Comparable<K1>, V1> int getRank(Node<K1, V1> node, final Range<K1> interval) {
            int rank = 0;

            while (node != null) {
                final int cmpVal = node.compare(interval);
                if (cmpVal < 0) {
                    node = node.mLeft;
                } else {
                    rank += node.getRank();
                    if (cmpVal == 0)
                        return rank; // EARLY RETURN!!!

                    node = node.mRight;
                }
            }

            return 0;
        }

        private int getRank() {
            int result = 1;
            if (mLeft != null)
                result = mLeft.mSize + 1;
            return result;
        }

        private Node<K1, V1> spliceOut(final Node<K1, V1> child, Node<K1, V1> root) {
            if ((child.mParent = mParent) == null) {
                root = child;
                child.mIsBlack = true;
            } else {
                if (mParent.mLeft == this)
                    mParent.mLeft = child;
                else
                    mParent.mRight = child;
                fixup(mParent);

                if (mIsBlack)
                    root = removeFixup(mParent, child, root);
            }

            return root;
        }

        private Node<K1, V1> rotateLeft(Node<K1, V1> root) {
            final Node<K1, V1> child = mRight;

            final int childSize = child.mSize;
            child.mSize = mSize;
            mSize -= childSize;

            if ((mRight = child.mLeft) != null) {
                mRight.mParent = this;
                mSize += mRight.mSize;
            }

            if ((child.mParent = mParent) == null)
                root = child;
            else if (this == mParent.mLeft)
                mParent.mLeft = child;
            else
                mParent.mRight = child;

            child.mLeft = this;
            mParent = child;

            setMaxEnd();
            child.setMaxEnd();

            return root;
        }

        private Node<K1, V1> rotateRight(Node<K1, V1> root) {
            final Node<K1, V1> child = mLeft;

            final int childSize = child.mSize;
            child.mSize = mSize;
            mSize -= childSize;

            if ((mLeft = child.mRight) != null) {
                mLeft.mParent = this;
                mSize += mLeft.mSize;
            }

            if ((child.mParent = mParent) == null)
                root = child;
            else if (this == mParent.mLeft)
                mParent.mLeft = child;
            else
                mParent.mRight = child;

            child.mRight = this;
            mParent = child;

            setMaxEnd();
            child.setMaxEnd();

            return root;
        }

        private void setMaxEnd() {
            mMaxEnd = Range.upTo(mIvl.upperEndpoint(), mIvl.upperBoundType());
            if (mLeft != null)
                mMaxEnd = max(mMaxEnd, mLeft.mMaxEnd);
            if (mRight != null)
                mMaxEnd = max(mMaxEnd, mRight.mMaxEnd);
        }

        private static <K1 extends Comparable<K1>, V1> void fixup(Node<K1, V1> node) {
            do {
                node.mSize = 1;
                node.mMaxEnd = Range.upTo(node.mIvl.upperEndpoint(), node.mIvl.upperBoundType());
                if (node.mLeft != null) {
                    node.mSize += node.mLeft.mSize;
                    node.mMaxEnd = max(node.mMaxEnd, node.mLeft.mMaxEnd);
                }
                if (node.mRight != null) {
                    node.mSize += node.mRight.mSize;
                    node.mMaxEnd = max(node.mMaxEnd, node.mRight.mMaxEnd);
                }
            }
            while ((node = node.mParent) != null);
        }

        private static <K1 extends Comparable<K1>, V1> Node<K1, V1> insertFixup(Node<K1, V1> daughter, Node<K1, V1> root) {
            Node<K1, V1> mom = daughter.mParent;
            fixup(mom);

            while (mom != null && !mom.mIsBlack) {
                final Node<K1, V1> gramma = mom.mParent;
                Node<K1, V1> auntie = gramma.mLeft;
                if (auntie == mom) {
                    auntie = gramma.mRight;
                    if (auntie != null && !auntie.mIsBlack) {
                        mom.mIsBlack = true;
                        auntie.mIsBlack = true;
                        gramma.mIsBlack = false;
                        daughter = gramma;
                    } else {
                        if (daughter == mom.mRight) {
                            root = mom.rotateLeft(root);
                            mom = daughter;
                        }
                        mom.mIsBlack = true;
                        gramma.mIsBlack = false;
                        root = gramma.rotateRight(root);
                        break;
                    }
                } else {
                    if (auntie != null && !auntie.mIsBlack) {
                        mom.mIsBlack = true;
                        auntie.mIsBlack = true;
                        gramma.mIsBlack = false;
                        daughter = gramma;
                    } else {
                        if (daughter == mom.mLeft) {
                            root = mom.rotateRight(root);
                            mom = daughter;
                        }
                        mom.mIsBlack = true;
                        gramma.mIsBlack = false;
                        root = gramma.rotateLeft(root);
                        break;
                    }
                }
                mom = daughter.mParent;
            }
            root.mIsBlack = true;
            return root;
        }

        private static <K1 extends Comparable<K1>, V1> Node<K1, V1> removeFixup(Node<K1, V1> parent, Node<K1, V1> node, Node<K1, V1> root) {
            do {
                if (node == parent.mLeft) {
                    Node<K1, V1> sister = parent.mRight;
                    if (!sister.mIsBlack) {
                        sister.mIsBlack = true;
                        parent.mIsBlack = false;
                        root = parent.rotateLeft(root);
                        sister = parent.mRight;
                    }
                    if ((sister.mLeft == null || sister.mLeft.mIsBlack) && (sister.mRight == null || sister.mRight.mIsBlack)) {
                        sister.mIsBlack = false;
                        node = parent;
                    } else {
                        if (sister.mRight == null || sister.mRight.mIsBlack) {
                            sister.mLeft.mIsBlack = true;
                            sister.mIsBlack = false;
                            root = sister.rotateRight(root);
                            sister = parent.mRight;
                        }
                        sister.mIsBlack = parent.mIsBlack;
                        parent.mIsBlack = true;
                        sister.mRight.mIsBlack = true;
                        root = parent.rotateLeft(root);
                        node = root;
                    }
                } else {
                    Node<K1, V1> sister = parent.mLeft;
                    if (!sister.mIsBlack) {
                        sister.mIsBlack = true;
                        parent.mIsBlack = false;
                        root = parent.rotateRight(root);
                        sister = parent.mLeft;
                    }
                    if ((sister.mLeft == null || sister.mLeft.mIsBlack) && (sister.mRight == null || sister.mRight.mIsBlack)) {
                        sister.mIsBlack = false;
                        node = parent;
                    } else {
                        if (sister.mLeft == null || sister.mLeft.mIsBlack) {
                            sister.mRight.mIsBlack = true;
                            sister.mIsBlack = false;
                            root = sister.rotateLeft(root);
                            sister = parent.mLeft;
                        }
                        sister.mIsBlack = parent.mIsBlack;
                        parent.mIsBlack = true;
                        sister.mLeft.mIsBlack = true;
                        root = parent.rotateRight(root);
                        node = root;
                    }
                }
                parent = node.mParent;
            }
            while (parent != null && node.mIsBlack);

            node.mIsBlack = true;
            return root;
        }

        public void checkMaxEnd() {
            if (compareUpper(mMaxEnd, calcMaxEnd()) != 0) {
                throw new IllegalStateException("Max end mismatch " + mMaxEnd + " vs " + calcMaxEnd() + ": " + this);
            }
            if (mLeft != null) mLeft.checkMaxEnd();
            if (mRight != null) mRight.checkMaxEnd();
        }

        private Range<K1> calcMaxEnd() {
            Range<K1> end = Range.upTo(mIvl.upperEndpoint(), mIvl.upperBoundType());
            if (mLeft != null) {
                end = max(end, mLeft.mMaxEnd);
            }

            if (mRight != null) {
                end = max(end, mRight.mMaxEnd);
            }
            return end;
        }

        private static <K1 extends Comparable<K1>> Range<K1> max(Range<K1> k1, Range<K1> k2) {
            return k1.span(k2);
        }

        public void printNode() {
            this.printNodeInternal("", "root: ");
        }

        private void printNodeInternal(final String padding, final String tag) {
            System.out.println(padding + tag + " " + this);
            if (mLeft != null) mLeft.printNodeInternal(padding + "  ", "left: ");
            if (mRight != null) mRight.printNodeInternal(padding + "  ", "right:");
        }

        public String toString() {
            return "Node(" + mIvl + "," + mValue + "," + mSize + "," + mMaxEnd + "," + mIsBlack + ")";
        }

        private Node<K1, V1> mParent;
        private Node<K1, V1> mLeft;
        private Node<K1, V1> mRight;
        private final Range<K1> mIvl;
        private V1 mValue;
        private int mSize;
        private Range<K1> mMaxEnd;
        private boolean mIsBlack;
    }

    public class FwdIterator
        implements Iterator<Node<K, V>> {
        public FwdIterator(final Node<K, V> node) {
            mNext = node;
        }

        @Override
        public boolean hasNext() {
            return mNext != null;
        }

        @Override
        public Node<K, V> next() {
            if (mNext == null) {
                throw new NoSuchElementException("No next element.");
            }

            if (mNext.wasRemoved()) {
                mNext = min(mNext.getInterval());
                if (mNext == null)
                    throw new ConcurrentModificationException("Current element was removed, and there are no more elements.");
            }
            mLast = mNext;
            mNext = mNext.getNext();
            return mLast;
        }

        @Override
        public void remove() {
            if (mLast == null) {
                throw new IllegalStateException("No entry to remove.");
            }

            removeNode(mLast);
            mLast = null;
        }

        private Node<K, V> mNext;
        private Node<K, V> mLast;
    }

    public class RevIterator
        implements Iterator<Node<K, V>> {
        public RevIterator(final Node<K, V> node) {
            mNext = node;
        }

        @Override
        public boolean hasNext() {
            return mNext != null;
        }

        @Override
        public Node<K, V> next() {
            if (mNext == null)
                throw new NoSuchElementException("No next element.");
            if (mNext.wasRemoved()) {
                mNext = max(mNext.getInterval());
                if (mNext == null)
                    throw new ConcurrentModificationException("Current element was removed, and there are no more elements.");
            }
            mLast = mNext;
            mNext = mNext.getPrev();
            return mLast;
        }

        @Override
        public void remove() {
            if (mLast == null) {
                throw new IllegalStateException("No entry to remove.");
            }

            removeNode(mLast);
            mLast = null;
        }

        private Node<K, V> mNext;
        private Node<K, V> mLast;
    }

    public class OverlapIterator
        implements Iterator<Node<K, V>> {
        public OverlapIterator(final Range<K> interval) {
            mNext = minOverlapper(interval);
            mIvl = interval;
        }

        @Override
        public boolean hasNext() {
            return mNext != null;
        }

        @Override
        public Node<K, V> next() {
            if (mNext == null) {
                throw new NoSuchElementException("No next element.");
            }

            if (mNext.wasRemoved()) {
                throw new ConcurrentModificationException("Current element was removed.");
            }

            mLast = mNext;
            mNext = Node.getNextOverlapper(mNext, mIvl);
            return mLast;
        }

        @Override
        public void remove() {
            if (mLast == null) {
                throw new IllegalStateException("No entry to remove.");
            }

            removeNode(mLast);
            mLast = null;
        }

        private Node<K, V> mNext;
        private Node<K, V> mLast;
        private final Range<K> mIvl;
    }

    private static <K1 extends Comparable<K1>> int compareLower(Range<K1> r1, Range<K1> r2) {
        K1 start1 = r1.lowerEndpoint();
        K1 start2 = r2.lowerEndpoint();
        BoundType bound1 = r1.lowerBoundType();
        BoundType bound2 = r2.lowerBoundType();
        int cmp = start1.compareTo(start2);
        if (cmp != 0) {
            return cmp;
        } else {
            if (bound1 == BoundType.CLOSED) {
                return bound2 == BoundType.CLOSED ? 0 : -1;
            } else {
                return bound2 == BoundType.CLOSED ? 1 : 0;
            }
        }
    }

    private static <K1 extends Comparable<K1>> int compareUpper(Range<K1> r1, Range<K1> r2) {
        K1 end1 = r1.upperEndpoint();
        K1 end2 = r2.upperEndpoint();
        BoundType bound1 = r1.upperBoundType();
        BoundType bound2 = r2.upperBoundType();
        int cmp = end1.compareTo(end2);
        if (cmp != 0) {
            return cmp;
        } else {
            if (bound1 == BoundType.CLOSED) {
                return bound2 == BoundType.CLOSED ? 0 : 1;
            } else {
                return bound2 == BoundType.CLOSED ? -1 : 0;
            }
        }
    }

    private static <K1 extends Comparable<K1>> int compareLowerUpper(Range<K1> r1, Range<K1> r2) {
        K1 start = r1.lowerEndpoint();
        K1 end = r2.upperEndpoint();
        BoundType startBound = r1.lowerBoundType();
        BoundType endBound = r2.upperBoundType();
        int cmp = start.compareTo(end);
        if (cmp != 0) {
            return cmp;
        } else {
            if (startBound == BoundType.CLOSED) {
                return endBound == BoundType.CLOSED ? 0 : 1;
            } else {
                return endBound == BoundType.CLOSED ? 1 : 0;
            }
        }
    }
}