package com.clevertec.collection;

import java.util.AbstractList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Custom abstract list implementation of {@link List} interface
 * providing default implementation of iterator over elements in list.
 * Must additionally override {@link #remove(int)} method to implement iterable list.
 *
 * @param <E> type of contained list elements
 * @see AbstractList
 */
public abstract class CustomAbstractList<E> implements List<E> {

    /**
     * Number of elements list contains.
     */
    protected int size;

    /**
     * Initializes size for empty list.
     */
    public CustomAbstractList() {
        size = 0;
    }

    /**
     * Returns iterator over elements in list.
     * Method will throw {@link UnsupportedOperationException} in response to its
     * {@code remove} method unless {@code remove(int)} method is overridden.
     *
     * @return iterator over elements in list
     */
    public Iterator<E> iterator() {
        return new Itr();
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
                CustomAbstractList.this.remove(lastReturned);
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
     * Always throws an {@code UnsupportedOperationException}.
     * Need be overridden by subclasses implementation.
     *
     * @throws UnsupportedOperationException always
     */
    public E remove(int index) {
        throw new UnsupportedOperationException();
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
     * Checks whether specified index is in range of list size.
     */
    protected void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Element index = " + index + ", list size = " + size);
        }
    }

}
