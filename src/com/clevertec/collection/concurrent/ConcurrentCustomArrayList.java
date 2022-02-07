package com.clevertec.collection.concurrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Thread-safe variant of custom resizable-array implementation of {@link List} interface.
 * Permits {@code null}.
 *
 * @param <E> type of contained list elements
 * @see ArrayList
 */
public class ConcurrentCustomArrayList<E> implements List<E>, RandomAccess, Serializable {

    /**
     * Default initial capacity.
     */
    private static final int INITIAL_CAPACITY = 8;

    /**
     * Multiplayer to increase capacity of list.
     */
    private static final int INCREASING_ARRAY_SIZE_MULTIPLAYER = 2;

    /**
     * Monitor protecting all mutators.
     */
    private final transient Object lock = new Object();

    /**
     * Array buffer into which elements of list are stored.
     * Capacity of list is length of array buffer.
     */
    private volatile E[] elementData;

    /**
     * Number of elements list contains.
     */
    private volatile int size;

    /**
     * Constructs empty list with initial capacity.
     */
    public ConcurrentCustomArrayList() {
        size = 0;
        elementData = initArray();
    }

    /**
     * Add element to list.
     * Increases current capacity of list, make it double if size equals length of elements array.
     *
     * @param element element to be appended to list
     * @return {@code true}
     */
    @Override
    public boolean add(E element) {
        synchronized (lock) {
            if (size == elementData.length) {
                grow();
            }
            elementData[size++] = element;
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
            checkElementIndex(index);

            return elementData[index];
        }
    }

//    /**
//     * Returns iterator over elements in list.
//     *
//     * @return iterator over elements in list
//     */
//    public Iterator<E> iterator() {
//        return new CopyOnWriteIterator<>(getArray(), 0);
//    }
//
//    /**
//     * Iterator over list implementing {@link Iterator} interface.
//     * Returned iterator provides snapshot of  state of list when the iterator was constructed.
//     * No synchronization is needed while traversing iterator.
//     * Does not support {@code remove} method.
//     */
//    private static class CopyOnWriteIterator<E> implements Iterator<E> {
//        /**
//         * Snapshot of the array.
//         */
//        private final E[] snapshot;
//
//        /**
//         * Index of element to be returned by subsequent call to next.
//         */
//        private int cursor;
//
//        /**
//         * Constructs iterator by specified values of aray of elements and cursor.
//         */
//        CopyOnWriteIterator(E[] elementsSnapshot, int initialCursor) {
//            cursor = initialCursor;
//            snapshot = elementsSnapshot;
//        }
//
//        /**
//         * Returns {@code true} if iteration has more elements.
//         *
//         * @return {@code true} if iteration has more elements
//         */
//        public boolean hasNext() {
//            return cursor < snapshot.length;
//        }
//
//        public E next() {
//            if (!hasNext()) {
//                throw new NoSuchElementException();
//            }
//            return snapshot[cursor++];
//        }
//
//        /**
//         * Not supported. Always throws {@code UnsupportedOperationException}.
//         *
//         * @throws UnsupportedOperationException always
//         */
//        public void remove() {
//            throw new UnsupportedOperationException();
//        }
//    }

    /**
     * Returns iterator over elements in list.
     * Method will throw {@link UnsupportedOperationException} in response to its
     * {@code remove} method unless {@code remove(int)} method is overridden.
     *
     * @return iterator over elements in list
     */
    public Iterator<E> iterator() {
        return new ConcurrentCustomArrayList<E>.Itr();
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
                ConcurrentCustomArrayList.this.remove(lastReturned);
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
            checkElementIndex(index);

            E removedElement = elementData[index];

            size--;

            if (size > 0) {
                System.arraycopy(elementData, index + 1, elementData, index, size - index);
//            for (int i = index; i < size; i++) {
//                elementData[i] = elementData[i + 1];
//            }
            } else {
                elementData = initArray();
            }

            return removedElement;
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
            Integer elementIndex = null;

            if (element == null) {
                for (int i = 0; i < size; i++) {
                    if (element == elementData[i]) {
                        elementIndex = i;
                        break;
                    }
                }
            } else {
                for (int i = 0; i < size; i++) {
                    if (element.equals(elementData[i])) {
                        elementIndex = i;
                        break;
                    }
                }
            }

            if (elementIndex != null) {
                size--;
                if (size > 0) {
                    System.arraycopy(elementData, elementIndex + 1, elementData, elementIndex, size - elementIndex);
                } else {
                    elementData = initArray();
                }

                return true;
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
        return IntStream.range(0, size)
                .mapToObj(i -> String.valueOf(elementData[i]))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    /**
     * Increases capacity of list instance by making it double.
     */
    private void grow() {
        int newIncreasedCapacity = elementData.length * INCREASING_ARRAY_SIZE_MULTIPLAYER;
        elementData = Arrays.copyOf(elementData, newIncreasedCapacity);
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
     * Returns empty array with specified initial capacity.
     */
    @SuppressWarnings("unchecked")
    private E[] initArray() {
        return (E[]) new Object[INITIAL_CAPACITY];
    }

    /**
     * Gets array of elements.
     */
    private E[] getArray() {
        return elementData;
    }

    /**
     * Returns {@code true} if list contains specified element.
     *
     * @param o element whose presence in list is to be tested
     * @return {@code true} if list contains specified element
     * @throws ClassCastException if type of specified element
     *                            is incompatible with list
     */
    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns index of the first occurrence of specified element with the lowest index
     * in list or -1 if this list does not contain the element.
     *
     * @param o element to search for
     * @return index of the first occurrence of specified element in
     * list or -1 if list does not contain element
     * @throws ClassCastException if type of specified element
     *                            is incompatible with list
     */
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
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
    public void replaceAll(UnaryOperator<E> operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sort(Comparator<? super E> c) {
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
    public Object[] toArray() {
//        return new Object[0];
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public Spliterator<E> spliterator() {
////        return List.super.spliterator();
//        throw new UnsupportedOperationException();
//    }

}
