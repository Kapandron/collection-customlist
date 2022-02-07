package com.clevertec.run;

import com.clevertec.collection.concurrent.ConcurrentCustomArrayList;
import com.clevertec.collection.concurrent.ConcurrentCustomLinkedList;

import java.util.Iterator;
import java.util.List;

public class ConcurrentChecker {

    private static final int FIRST_ELEMENT_NUMBER_TO_ADD = 0;

    private static final int MILLIS_TO_SLEEP_FOR_WRITE_THREAD = 1000; // im millis

    private static final int MILLIS_TO_SLEEP_FOR_READ_THREAD = 10; // im millis

    public static void main(String... args) {

        List<Integer> list = Checker.init(new ConcurrentCustomArrayList<>());
//        List<Integer> list = Checker.init(new ConcurrentCustomLinkedList<>());

        int operationNumber = 0;

        Checker.print(list, ++operationNumber);

        Checker.printByIndex(list, ++operationNumber);

        Checker.removeElement(list, 1, ++operationNumber);

        Checker.printByIndex(list, ++operationNumber);

        Checker.removeElement(list, 0, ++operationNumber);
        Checker.removeElement(list, 4, ++operationNumber);
        Checker.removeElement(list, 2, ++operationNumber);

        Checker.printByIndex(list, ++operationNumber);

        Checker.removeByIndexOutOfRange(list, 11, ++operationNumber);

        Checker.getByIndexOutOfRange(list, 11, ++operationNumber);

        Checker.useInStream(list, ++operationNumber);

        checkThreadSafe(list, operationNumber);

    }

    /**
     * Adds element to list for every second.
     */
    static class WriteThread extends Thread {

        private final List<Integer> list;

        public WriteThread(String name, List<Integer> list) {
            super.setName(name);
            this.list = list;
        }

        public void run() {
            int elementNumber = FIRST_ELEMENT_NUMBER_TO_ADD;

            while (true) {

                try {

                    Thread.sleep(MILLIS_TO_SLEEP_FOR_WRITE_THREAD);

                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }

                list.add(elementNumber);

                System.out.println("\n" + super.getName() + " has successfully added a new element " + elementNumber);

                elementNumber++;
            }
        }
    }

    /**
     * Iterates list repeatedly with a small delay (10 milliseconds) for every iteration.
     */
    static class ReadThread extends Thread {
        private final List<Integer> list;

        public ReadThread(String name, List<Integer> list) {
            this.list = list;
            super.setName(name);
        }

        public void run() {

            while (true) {

                StringBuilder output = new StringBuilder("\n" + super.getName() + ":");

                Iterator<Integer> iterator = list.iterator();

                while (iterator.hasNext()) {
                    Integer next = iterator.next();
                    output.append(" ").append(next);

                    // fake processing time
                    try {

                        Thread.sleep(MILLIS_TO_SLEEP_FOR_READ_THREAD);

                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }

                System.out.println(output);
            }
        }
    }

    private static void checkThreadSafe(List<Integer> list, int operationNumber) {
        System.out.print(operationNumber + ") ");
        System.out.println("Checking custom list in multithreaded execution: ");

        new WriteThread("Writer", list).start();

        new ReadThread("Reader", list).start();

        System.out.println("----------------------------------------");
    }

}
