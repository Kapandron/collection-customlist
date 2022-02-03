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
     * Returns iterator over elements in list.
     * <p>
     * Method will throw {@link UnsupportedOperationException} in response to its
     * {@code remove} method unless {@code remove(int)} method is overridden.
     *
     * @return iterator over elements in list
     */
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * Always throws an {@code UnsupportedOperationException}.
     * Need be overridden by subclasses implementation.
     *
     * @throws UnsupportedOperationException
     */
    public E remove(int index) {
        throw new UnsupportedOperationException();
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

}
