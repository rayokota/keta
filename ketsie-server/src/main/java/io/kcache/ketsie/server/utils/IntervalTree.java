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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
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
     * If the interval is novel, the special sentinel value is returned.
     *
     * @param start The interval's start.
     * @param end   The interval's end.
     * @param value The associated value.
     * @return The old value associated with that interval, or the sentinel.
     */
    public V put(final K start, final K end, final V value) {
        if (start.compareTo(end) > 0)
            throw new IllegalArgumentException("Start cannot exceed end.");

        V result = mSentinel;

        if (mRoot == null) {
            mRoot = new Node<>(start, end, value);
        } else {
            Node<K, V> parent = null;
            Node<K, V> node = mRoot;
            int cmpVal = 0;

            while (node != null) {
                parent = node; // last non-null node
                cmpVal = node.compare(start, end);
                if (cmpVal == 0) {
                    break;
                }

                node = cmpVal < 0 ? node.getLeft() : node.getRight();
            }

            if (cmpVal == 0) {
                result = parent.setValue(value);
            } else {
                if (cmpVal < 0) {
                    mRoot = parent.insertLeft(start, end, value, mRoot);
                } else {
                    mRoot = parent.insertRight(start, end, value, mRoot);
                }
            }
        }

        return result;
    }

    /**
     * If the specified start and end positions are not already associated with a value or are
     * associated with the sentinel ( see {@link #getSentinel()}, associates it with the given (non-sentinel) value.
     * Otherwise, replaces the associated value with the results of the given
     * remapping function, or removes if the result is equal to the sentinel value. This
     * method may be of use when combining multiple values that have the same start and end position.
     *
     * @param start             interval start position
     * @param end               interval end position
     * @param value             value to merge into the tree, must not be equal to the sentinel value
     * @param remappingFunction a function that merges the new value with the existing value for the same start and end position,
     *                          if the function returns the sentinel value then the mapping will be unset
     * @return the updated value that is stored in the tree after the completion of this merge operation, this will
     * be the sentinel value if nothing ended up being stored
     */
    public V merge(K start, K end, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        final V alreadyPresent = put(start, end, value);
        if (!Objects.equals(alreadyPresent, mSentinel)) {
            final V newComputedValue = remappingFunction.apply(value, alreadyPresent);
            if (Objects.equals(newComputedValue, mSentinel)) {
                remove(start, end);
            } else {
                put(start, end, newComputedValue);
            }
            return newComputedValue;
        }
        return value;
    }

    /**
     * Remove an interval from the tree.  If the interval does not exist in the tree the
     * special sentinel value is returned.
     *
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return The value associated with that interval, or the sentinel.
     */
    public V remove(final K start, final K end) {
        V result = mSentinel;
        Node<K, V> node = mRoot;

        while (node != null) {
            final int cmpVal = node.compare(start, end);
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
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return The Node that represents that interval, or null.
     */
    public Node<K, V> find(final K start, final K end) {
        Node<K, V> node = mRoot;

        while (node != null) {
            final int cmpVal = node.compare(start, end);
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
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return The rank of that interval, or -1.
     */
    public int getIndex(final K start, final K end) {
        return Node.getRank(mRoot, start, end) - 1;
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
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return The earliest >= interval, or null if there is none.
     */
    @SuppressWarnings("null")
    public Node<K, V> min(final K start, final K end) {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;
        int cmpVal = 0;

        while (node != null) {
            result = node;
            cmpVal = node.compare(start, end);
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
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return The earliest overlapping interval, or null if there is none.
     */
    public Node<K, V> minOverlapper(final K start, final K end) {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;

        if (node != null && node.getMaxEnd().compareTo(start) >= 0) {
            while (true) {
                if (node.getStart().compareTo(end) <= 0 && start.compareTo(node.getEnd()) <= 0) { // this node overlaps.  there might be a lesser overlapper down the left sub-tree.
                    // no need to consider the right sub-tree:  even if there's an overlapper, if won't be minimal
                    result = node;
                    node = node.getLeft();
                    if (node == null || node.getMaxEnd().compareTo(start) < 0)
                        break; // no left sub-tree or all nodes end too early
                } else { // no overlap.  if there might be a left sub-tree overlapper, consider the left sub-tree.
                    final Node<K, V> left = node.getLeft();
                    if (left != null && left.getMaxEnd().compareTo(start) >= 0) {
                        node = left;
                    } else { // left sub-tree cannot contain an overlapper.  consider the right sub-tree.
                        if (node.getStart().compareTo(end) > 0)
                            break; // everything in the right sub-tree is past the end of the query interval

                        node = node.getRight();
                        if (node == null || node.getMaxEnd().compareTo(start) < 0)
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
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return The latest >= interval, or null if there is none.
     */
    @SuppressWarnings("null")
    public Node<K, V> max(final K start, final K end) {
        Node<K, V> result = null;
        Node<K, V> node = mRoot;
        int cmpVal = 0;

        while (node != null) {
            result = node;
            cmpVal = node.compare(start, end);
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
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return An iterator.
     */
    public Iterator<Node<K, V>> iterator(final K start, final K end) {
        return new FwdIterator(min(start, end));
    }

    /**
     * Return an iterator over all intervals overlapping the specified range.
     *
     * @param start The range start.
     * @param end   The range end.
     * @return An iterator.
     */
    public Iterator<Node<K, V>> overlappers(final K start, final K end) {
        return new OverlapIterator(start, end);
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
     * @param start The interval's start.
     * @param end   The interval's end.
     * @return An iterator.
     */
    public Iterator<Node<K, V>> reverseIterator(final K start, final K end) {
        return new RevIterator(max(start, end));
    }

    /**
     * Get the special sentinel value that will be used to signal novelty when putting a new interval
     * into the tree, or to signal "not found" when removing an interval.  This is null by default.
     *
     * @return The sentinel value.
     */
    public V getSentinel() {
        return mSentinel;
    }

    /**
     * Set the special sentinel value that will be used to signal novelty when putting a new interval
     * into the tree, or to signal "not found" when removing an interval.
     *
     * @param sentinel The new sentinel value.
     * @return The old sentinel value.
     */
    public V setSentinel(final V sentinel) {
        final V result = mSentinel;
        mSentinel = sentinel;
        return result;
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
    private V mSentinel;

    public static class Node<K1 extends Comparable<K1>, V1> {

        Node(final K1 start, final K1 end, final V1 value) {
            mStart = start;
            mEnd = end;
            mValue = value;
            mSize = 1;
            mMaxEnd = mEnd;
            mIsBlack = true;
        }

        Node(final Node<K1, V1> parent, final K1 start, final K1 end, final V1 value) {
            mParent = parent;
            mStart = start;
            mEnd = end;
            mValue = value;
            mMaxEnd = mEnd;
            mSize = 1;
        }

        public K1 getStart() {
            return mStart;
        }

        public K1 getEnd() {
            return mEnd;
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

        K1 getMaxEnd() {
            return mMaxEnd;
        }

        Node<K1, V1> getLeft() {
            return mLeft;
        }

        Node<K1, V1> insertLeft(final K1 start, final K1 end, final V1 value, final Node<K1, V1> root) {
            mLeft = new Node<>(this, start, end, value);
            return insertFixup(mLeft, root);
        }

        Node<K1, V1> getRight() {
            return mRight;
        }

        Node<K1, V1> insertRight(final K1 start, final K1 end, final V1 value, final Node<K1, V1> root) {
            mRight = new Node<>(this, start, end, value);
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
        int compare(final K1 start, final K1 end) {
            int result = 0;

            if (start.compareTo(mStart) > 0)
                result = 1;
            else if (start.compareTo(mStart) < 0)
                result = -1;
            else if (end.compareTo(mEnd) > 0)
                result = 1;
            else if (end.compareTo(mEnd) < 0)
                result = -1;

            return result;
        }

        @SuppressWarnings("null")
        static <K1 extends Comparable<K1>, V1> Node<K1, V1> getNextOverlapper(Node<K1, V1> node, final K1 start, final K1 end) {
            do {
                Node<K1, V1> nextNode = node.mRight;
                if (nextNode != null && nextNode.mMaxEnd.compareTo(start) >= 0) {
                    node = nextNode;
                    while ((nextNode = node.mLeft) != null && nextNode.mMaxEnd.compareTo(start) >= 0)
                        node = nextNode;
                } else {
                    nextNode = node;
                    while ((node = nextNode.mParent) != null && node.mRight == nextNode)
                        nextNode = node;
                }

                if (node != null && node.mStart.compareTo(end) > 0)
                    node = null;
            }
            while (node != null && !(node.mStart.compareTo(end) <= 0 && start.compareTo(node.mEnd) <= 0));

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

        static <K1 extends Comparable<K1>, V1> int getRank(Node<K1, V1> node, final K1 start, final K1 end) {
            int rank = 0;

            while (node != null) {
                final int cmpVal = node.compare(start, end);
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
            mMaxEnd = mEnd;
            if (mLeft != null)
                mMaxEnd = max(mMaxEnd, mLeft.mMaxEnd);
            if (mRight != null)
                mMaxEnd = max(mMaxEnd, mRight.mMaxEnd);
        }

        private static <K1 extends Comparable<K1>, V1> void fixup(Node<K1, V1> node) {
            do {
                node.mSize = 1;
                node.mMaxEnd = node.mEnd;
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
            if (mMaxEnd.compareTo(calcMaxEnd()) != 0) {
                throw new IllegalStateException("Max end mismatch " + mMaxEnd + " vs " + calcMaxEnd() + ": " + this);
            }
            if (mLeft != null) mLeft.checkMaxEnd();
            if (mRight != null) mRight.checkMaxEnd();
        }

        private K1 calcMaxEnd() {
            K1 end = mEnd;
            if (mLeft != null) {
                end = max(end, mLeft.mMaxEnd);
            }

            if (mRight != null) {
                end = max(end, mRight.mMaxEnd);
            }
            return end;
        }

        private static <K1 extends Comparable<K1>> K1 max(K1 k1, K1 k2) {
            int cmp = k1.compareTo(k2);
            return cmp >= 0 ? k1 : k2;
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
            return "Node(" + mStart + "," + mEnd + "," + mValue + "," + mSize + "," + mMaxEnd + "," + mIsBlack + ")";
        }

        private Node<K1, V1> mParent;
        private Node<K1, V1> mLeft;
        private Node<K1, V1> mRight;
        private final K1 mStart;
        private final K1 mEnd;
        private V1 mValue;
        private int mSize;
        private K1 mMaxEnd;
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
                mNext = min(mNext.getStart(), mNext.getEnd());
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
                mNext = max(mNext.getStart(), mNext.getEnd());
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
        public OverlapIterator(final K start, final K end) {
            mNext = minOverlapper(start, end);
            mStart = start;
            mEnd = end;
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
            mNext = Node.getNextOverlapper(mNext, mStart, mEnd);
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
        private final K mStart;
        private final K mEnd;
    }

    public static class ValuesIterator<K1 extends Comparable<K1>, V1>
        implements Iterator<V1> {
        public ValuesIterator(final Iterator<Node<K1, V1>> itr) {
            mItr = itr;
        }

        @Override
        public boolean hasNext() {
            return mItr.hasNext();
        }

        @Override
        public V1 next() {
            return mItr.next().getValue();
        }

        @Override
        public void remove() {
            mItr.remove();
        }

        private final Iterator<Node<K1, V1>> mItr;
    }
}