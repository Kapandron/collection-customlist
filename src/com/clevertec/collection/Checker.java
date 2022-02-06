package com.clevertec.collection;

import java.util.List;

public class Checker {

    public static void main(String... args) {

        List<Integer> list = init(new CustomArrayList<>());
//        List<Integer> list = init(new CustomLinkedList<>());

        int operationNumber = 0;

        print(list, ++operationNumber);

        printByIndex(list, ++operationNumber);

        removeElement(list, 1, ++operationNumber);

        printByIndex(list, ++operationNumber);

        removeElement(list, 0, ++operationNumber);
        removeElement(list, 4, ++operationNumber);
        removeElement(list, 2, ++operationNumber);

        printByIndex(list, ++operationNumber);

        removeByIndexOutOfRange(list, 11, ++operationNumber);

        getByIndexOutOfRange(list, 11, ++operationNumber);

        useInStream(list, ++operationNumber);

    }

    private static List<Integer> init(List<Integer> list) {
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        list.add(1);
        list.add(2);

        return list;
    }

    private static void print(List<Integer> list, int operationNumber) {
        System.out.print(operationNumber + ") ");
        System.out.println("Elements of custom list from toString():");
        System.out.println(list);
        System.out.println("----------------------------------------");
    }

    private static void printByIndex(List<Integer> list, int operationNumber) {
        System.out.print(operationNumber + ") ");
        System.out.println("Elements of custom list by index: ");
        for (int i = 0; i < list.size(); i++) {
            System.out.println("Element at index [" + i + "] = " + list.get(i));
        }
        System.out.println("----------------------------------------");
    }

    private static void removeElement(List<Integer> list, int elementIndexToRemove, int operationNumber) {
        System.out.print(operationNumber + ") ");
        System.out.println("Removing element from custom list by index: ");
        System.out.println("Element at index [" + elementIndexToRemove + "] = " + list.remove(elementIndexToRemove));
        if (list.size() > elementIndexToRemove) {
            System.out.println("After removing element at index [" + elementIndexToRemove + "] = " + list.get(elementIndexToRemove));
        } else {
            System.out.println("Element at index [" + elementIndexToRemove + "] was last one in list");
        }
        System.out.println("----------------------------------------");
    }

    private static void removeByIndexOutOfRange(List<Integer> list, int elementIndexToRemove, int operationNumber) {
        System.out.print(operationNumber + ") ");
        System.out.println("Reproducing exception by remove element from custom list with index out of range: ");
        try {
            list.remove(elementIndexToRemove);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("IndexOutOfBoundsException has been thrown because there is no element to remove "
                    + "on index [" + elementIndexToRemove + "], list size = " + list.size());
        }
        System.out.println("----------------------------------------");
    }

    private static void getByIndexOutOfRange(List<Integer> list, int elementIndexToGet, int operationNumber) {
        System.out.print(operationNumber + ") ");
        System.out.println("Reproducing exception by get element from custom list with index out of range: ");
        try {
            list.get(elementIndexToGet);
        } catch (IndexOutOfBoundsException ex) {
            System.out.println("IndexOutOfBoundsException has been thrown because there is no element to get "
                    + "on index [" + elementIndexToGet + "], list size = " + list.size());
        }
        System.out.println("----------------------------------------");
    }

    private static void useInStream(List<Integer> list, int operationNumber) {
        System.out.print(operationNumber + ") ");
        System.out.println("Use custom list with stream: ");
        System.out.print("First element of list: ");
        System.out.println(list.stream().findFirst().orElse(null));
        System.out.println("----------------------------------------");
    }

}
