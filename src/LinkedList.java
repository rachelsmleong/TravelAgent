/**
 * Execution: Implements List
 * 
 */

import java.awt.*;

public class LinkedList<T> implements List<T> {

    private Node<T> head;
    private int size;

    // constructor
    public LinkedList() {
        head = new Node<T>(null);
        size = 0;
    }

    /**
     * Adds the object x to the end of the list.
     *
     * @param x the element to be added to this list
     * @return true
     */
    public boolean add(T x) {
        if (isEmpty()) {
            head.next = new Node<T>(x);
            size++;

            return true;

        } else {
            Node<T> current = head.next;
            while (current.next != null) {
                current = current.next;
            }
            current.next = new Node<T>(x);
            size++;

        }
        return true;
    }

    /**
     * Adds the object x at the specified position
     *
     * @param index the position to add the element
     * @param x     the element to be added to the list
     * @return true if the operation succeeded, false otherwise
     * @throws IllegalArgumentException - if index is longer
     *                                  than the currentlength of the list
     */
    public boolean add(int index, T x) {
        Node<T> current = head;
        if (index > size || index < 0) {
            throw new IllegalArgumentException("Index longer than list length");
        }
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        current.next = new Node<T>(x, current.next);
        size++;
        return true;
    }

    /**
     * Returns the number of elements in this list
     *
     * @return the number of elements in this list
     */
    public int size() {
        return size;
    }

    /**
     * Returns the element with the specified position in this list
     *
     * @param index the position of the element
     * @return the element at the specified position in this list
     * @throws IllegalArugmentException if index is longer than the
     *                                  number of elements in the list
     */
    public T get(int index) {
        Node<T> current = head;
        if (index >= size || index < 0) {
            throw new IllegalArgumentException("Index longer than list length");
        } else {
            for (int i = 0; i < index + 1; i++) {
                current = current.next;
            }
            return current.element;
        }
    }

    /**
     * Replaces the object at the specified position
     *
     * @param index the position to replace
     * @param x     the element to be stored
     * @return the previous value of the element at index
     * @throws IllegalArugmentException if index is longer than the
     *                                  number of elements in the list
     */
    public T set(int index, T x) {
        if (index >= size || index < 0) {
            throw new IllegalArgumentException("Index longer than list length");
        } else {
            Node<T> current = head.next;
            for (int i = 0; i < index; i++) {
                current = current.next;
            }
            T prevValue = current.element;
            current.element = x;
            return prevValue;
        }
    }

    /**
     * Removes the object at the specified position
     *
     * @param index the position to remove
     * @return the object that was removed
     * @throws IllegalArugmentException if index is more than
     *                                  the number of elements in the list
     */
    public T remove(int index) {
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        T removedValue = current.next.element;
        current.next = current.next.next;
        size--;
        return removedValue;
    }

    /**
     * Tests if this list has no elements.
     *
     * @return <tt>true</tt> if this list has no elements;
     * <tt>false</tt> otherwise.
     */
    public boolean isEmpty() {
        return head.next == null;
    }

    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param element element whose presence in this List is to be tested.
     * @return <code>true</code> if the specified element is present;
     * <code>false</code> otherwise.
     */
    public boolean contains(T element) {
        Node<T> current = head.next;
        for (int i = 0; i < size; i++) {
            if (current.element == element) return true;
            current = current.next;
        }
        return false;
    }

    /**
     * Returns the index of the specified element
     *
     * @param element the element we're looking for
     * @return the index of the element in the list, or
     * -1 if it is not contained within the list
     */
    public int indexOf(T element) {
        int index = 0;
        if (contains(element)){
            Node<T> current = head.next;
            while (current != null) {
                if (element == current.element) {
                    break;
                } else {
                    current = current.next;
                    index++;
                }
            }

        } else return -1; return index;
    }

    public String toString() {
        String elements = "";
        Node current = head;
        while (current.next != null) {
            current = current.next;
            elements = elements + current.element + "\n";

        }
        return elements;
    }

    class Node<T> {
        Node<T> next;
        T element;

        Node(T x) {
            element = x;
            next = null;
        }

        Node(T x, Node<T> n) {
            element = x;
            next = n;
        }
    }

}
