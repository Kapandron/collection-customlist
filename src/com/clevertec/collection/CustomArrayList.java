package com.clevertec.collection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Custom resizable-array implementation of {@link List} interface.
 * Permits {@code null}.
 *
 * @param <E> type of contained list elements
 * @see ArrayList
 */
public class CustomArrayList<E> implements List<E>, RandomAccess, Serializable {

    /**
     * Default initial capacity.
     */
    private static final int INITIAL_CAPACITY = 10;

    /**
     * Multiplayer to increase capacity of list.
     */
    private static final int INCREASING_ARRAY_SIZE_MULTIPLAYER = 2;

    /**
     * Number of elements list contains.
     */
    private int size;

    /**
     * Array buffer into which elements of list are stored.
     * Capacity of list is length of array buffer.
     */
    private E[] elementData;

    /**
     * Constructs empty list with initial capacity.
     */
    public CustomArrayList() {
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
        if (size == elementData.length) {
            grow();
        }
        elementData[size++] = element;

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
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("getting by index = " + index + ", but list size = " + index);
        }

        return elementData[index];
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
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("getting by index = " + index + ", but list size = " + index);
        }

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

    /**
     * Removes the first occurrence of the specified element with the lowest index from list if it is present.
     *
     * @param element element to be removed from list
     * @return {@code true} if list contained specified element
     */
    @Override
    public boolean remove(Object element) {
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
     * Returns empty array with specified initial capacity.
     */
    private E[] initArray() {
        return (E[]) new Object[INITIAL_CAPACITY];
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
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
    public Spliterator<E> spliterator() {
//        return List.super.spliterator();
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
