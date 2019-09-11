package ch.epfl.javass;

/**
 * The class is used to simplify all types of checks that 
 * throws an exception.
 * @author xavier
 *
 */
public final class Preconditions {
    private Preconditions() {}
    
    /**
     * Is used for checking arguments in methods:
     * example: checkArgument(number < 128);
     * throws the error IllegalArgumentException if the condition is not true
     */
    public static void checkArgument(boolean b) {
        if(!b) {
            throw new IllegalArgumentException();
        }
    }
    
    public static void checkState(boolean b) {
        if(!b) {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Throws IndexOutOfBoundsException if index is not in [0;size-1]
     * @return the index if it is safe
     */
    public static int checkIndex(int index, int size) {
        if(index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return index;
    }
}