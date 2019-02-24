/**
 * Interface for generic lists.
 * @author maxtrom, brohan
 */
public interface List<T> {
    
    /**
     * Adds the object x to the end of the list.
     * @param x the element to be added to this list
     * @return true
     */
    public boolean add(T x);
    
    /**
     * Adds the object x at the specified position
     * @param index the position to add the element
     * @param x the element to be added to the list
     * @return true if the operation succeeded, false otherwise
     * @throws IllegalArgumentException - if index is longer
     * than the currentlength of the list
     */
    public boolean add(int index, T x);
    
    /**
     * Returns the number of elements in this list
     * @return the number of elements in this list
     */
    public int size();
    
    /**
     * Returns the element with the specified position in this list
     * @param index the position of the element
     * @return the element at the specified position in this list
     * @throws IllegalArugmentException if index is longer than the
     * number of elements in the list
     */
    public T get(int index);
    
    /**
     * Replaces the object at the specified position
     * @param index the position to replace
     * @param x the element to be stored
     * @return the previous value of the element at index
     * @throws IllegalArugmentException if index is longer than the
     * number of elements in the list
     */
    public T set(int index, T x);
    
    /**
     * Removes the object at the specified position
     * @param index the position to remove
     * @return the object that was removed
     * @throws IllegalArugmentException if index is more than
     * the number of elements in the list
     */
    public T remove(int index);
    
    /**
     * Tests if this list has no elements.
     * @return  <tt>true</tt> if this list has no elements;
     *          <tt>false</tt> otherwise.
     */
    public boolean isEmpty();
    
    /**
     * Returns <tt>true</tt> if this list contains the specified element.
     *
     * @param element element whose presence in this List is to be tested.
     * @return  <code>true</code> if the specified element is present;
     *  <code>false</code> otherwise.
     */
    public boolean contains(T element);
    
    
    /** 
     * Returns the index of the specified element
     *
     * @param element the element we're looking for
     * @return the index of the element in the list, or -1 if it is not contained within the list
     */
    public int indexOf(T element);
     
    
}