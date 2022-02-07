package com.clevertec.collection.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Thread-safe variant of custom doubly-linked list implementation of {@link List} interface.
 * Permits {@code null}.
 *
 * @param <E> type of contained list elements
 * @see LinkedList
 */
public class ConcurrentCustomLinkedList<E> implements List<E>, Serializable {

    /**
     * Monitor protecting all mutators.
     */
    private final transient Object lock = new Object();

    /**
     * Number of elements list contains.
     */
    private volatile int size;

    /**
     * Pointer to first node.
     */
    private Node<E> head;

    /**
     * Pointer to last node.
     */
    transient Node<E> tail;

    /**
     * Constructs an empty list.
     */
    public ConcurrentCustomLinkedList() {
        super();
    }

    /**
     * Internal node containing current element and links to previous node and next one for navigation.
     *
     * @param <E> type of contained item
     */
    private static class Node<E> {

        /**
         * Containing element in node.
         */
        E element;

        /**
         * Next node.
         */
        Node<E> next;

        /**
         * Previous node.
         */
        Node<E> prev;

        /**
         * Constructs new node with specified parameter values.
         */
        Node(E element, Node<E> prev, Node<E> next) {
            this.element = element;
            this.next = next;
            this.prev = prev;
        }
    }

    /**
     * Appends specified element to the end of list.
     *
     * @param element element to be appended to list
     * @return {@code true}
     */
    @Override
    public boolean add(E element) {
        synchronized (lock) {
            final Node<E> last = tail;
            final Node<E> newNode = new Node<>(element, last, null);
            tail = newNode;

            if (last == null) {
                head = newNode;
            } else {
                last.next = newNode;
            }

            size++;
        }

        return true;
    }

    /**
     * Returns element at specified position in list.
     *
     * @param index index of element to return
     * @return element at specified position in list
     * @throws IndexOutOfBoundsException if index is negative or out of range (greater than size of size)
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public E get(int index) {
        synchronized (lock) {
            return getNodeByIndex(index).element;
        }
    }


    /**
     * Returns iterator over elements in list.
     * Method will throw {@link UnsupportedOperationException} in response to its
     * {@code remove} method unless {@code remove(int)} method is overridden.
     *
     * @return iterator over elements in list
     */
    public Iterator<E> iterator() {
        return new ConcurrentCustomLinkedList<E>.Itr();
    }

    /**
     * Iterator over list implementing {@link Iterator} interface.
     */
    private class Itr implements Iterator<E> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursor = 0;

        /**
         * Index of element returned by most recent call to next or previous.
         * Reset to -1 if this element is deleted by call to remove.
         */
        int lastReturned = -1;

        /**
         * Returns {@code true} if iteration has more elements.
         *
         * @return {@code true} if iteration has more elements
         */
        public boolean hasNext() {
            return cursor != size();
        }

        /**
         * Returns the next element in iteration.
         *
         * @return the next element in iteration
         * @throws NoSuchElementException if iteration has no more elements
         */
        public E next() {
            try {
                int i = cursor;
                E next = get(i);
                lastReturned = i;
                cursor = i + 1;
                return next;
            } catch (IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        /**
         * Removes from list the last element returned by iterator.
         *
         * @throws IllegalStateException if {@code next} method has not
         *                               yet been called or {@code remove} method has already
         *                               been called after the last call to {@code next} method
         */
        public void remove() {
            if (lastReturned < 0) {
                throw new IllegalStateException();
            }

            try {
                ConcurrentCustomLinkedList.this.remove(lastReturned);
                if (lastReturned < cursor) {
                    cursor--;
                }

                lastReturned = -1;
            } catch (IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Removes element from list and returns removed element on specific index.
     * Reduces size of internal array after removal of element.
     *
     * @param index index of element to remove
     * @throws IndexOutOfBoundsException if index is negative or out of range (greater than size of size)
     *                                   ({@code index < 0 || index >= size()})
     */
    @Override
    public E remove(int index) {
        synchronized (lock) {
            return unlink(getNodeByIndex(index));
        }
    }

    /**
     * Removes the first occurrence of the specified element with the lowest index from list if it is present.
     *
     * @param element element to be removed from list
     * @return {@code true} if list contained specified element
     */
    @Override
    public boolean remove(Object element) {
        synchronized (lock) {
            if (element == null) {
                for (Node<E> node = head; node != null; node = node.next) {
                    if (node.element == null) {
                        unlink(node);
                        return true;
                    }
                }
            } else {
                for (Node<E> node = head; node != null; node = node.next) {
                    if (element.equals(node.element)) {
                        unlink(node);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns number of elements in list.
     *
     * @return number of elements in list
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if list contains no elements.
     *
     * @return {@code true} if list contains no elements
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns string representation of list.
     *
     * @return string representation of list
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');

        boolean hasFirstElementPassed = false;

        for (Node<E> node = head; node != null; node = node.next) {
            if (hasFirstElementPassed) {
                builder.append(", ");
            } else {
                hasFirstElementPassed = true;
            }

            builder.append(node.element);
        }

        builder.append(']');

        return builder.toString();
    }

    /**
     * Returns (non-null) node at specified element index.
     */
    private Node<E> getNodeByIndex(int index) {
        checkElementIndex(index);

        Node<E> node;
        if (isElementInFirstHalf(index, size)) {
            node = head;
            for (int i = 0; i < index; i++) {
                node = node.next;
            }
        } else {
            node = tail;
            for (int i = size - 1; i > index; i--) {
                node = node.prev;
            }
        }

        return node;
    }

    /**
     * Checks whether specified index is in range of list size.
     */
    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Element index = " + index + ", list size = " + size);
        }
    }

    /**
     * Determines whether element at specified index is closer to head or to tail of list.
     */
    private boolean isElementInFirstHalf(int index, int size) {
        return index < (size / 2);
    }

    /**
     * Unlinks non-null node.
     */
    private E unlink(Node<E> node) {
        if (node == null) {
            throw new IllegalArgumentException("Cannot unlink null node");
        }

        final E element = node.element;
        final Node<E> next = node.next;
        final Node<E> prev = node.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            node.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            node.next = null;
        }

        node.element = null;

        size--;

        return element;
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
//        return new Object[0];
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

}
