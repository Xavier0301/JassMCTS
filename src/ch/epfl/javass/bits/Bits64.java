package ch.epfl.javass.bits;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * The class is used to perform bitwise manipulations on (long)
 * instances, which have a binary representation of 64 bits.
 * 
 * @author xavier
 *
 */
public final class Bits64 {
    private Bits64() {}
    
    /** Check if [start; start+size-1] is an integer intarval and if it is in [0;63]
     * 
     */
    private static void checkIndexInterval(int start, int size) {
        checkArgument(start >= 0);
        checkArgument(start+size <= Long.SIZE);
        checkArgument(size >= 0);
    }
    
    /**
     * Used to get the the mask from 0 up to the index:
     * example: for index = 3 it looks like 111
     * Special case if index is 64: (1<<index) overflows and gives 1.
     * @param index
     * @return
     */
    private static long getPartialMask(int index) {
        return (index == Long.SIZE) ? -1L : (1L << index)-1L;
    }
    
    /** It's a function to get a (long)
     * which bits from start to start+size-1 are set to 1 and the rest to 0.
     * 
     * Throws IllegalArgumentException if [start; start+size-1] is not in [0;63]
     * 
     * @param start index of the first bit 
     * @param size size of the mask
     * @return An (int) such that the bits from start to start+size-1 are set to 1, the rest to 0.
     */
    public static long mask(int start, int size) {
        checkIndexInterval(start, size);
        
        // maskSup has all bits from 0 to start+size-1 set to 1.
        long maskSup = getPartialMask(start+size);
        // maskInf has all bits from 0 to start-1 set to 1
        long maskInf = getPartialMask(start);
                
        // this thus gives all bits from start to start+size-1 to 1
        return maskSup - maskInf;
    }
    
    /** Extracts from the argument bits its 
     * bits from start to start+size-1. The rest is 0.
     * 
     * @param bits (int) from which we want to extract bits from start to start+size-1
     * @param start index of the first bit
     * @param size size of the mask
     */
    public static long extract(long bits, int start, int size) {
        checkIndexInterval(start, size);
        
        long extracted = mask(start, size) & bits;
        long extractedShifted = extracted >> start;
        return extractedShifted;
    }
    
    /**
     * To get the values v1 and v2 in the same int 
     * looks like (v2 | v1) 
     * 
     * throws exception IllegalArgumentException 
     *  - if one of the sizes are not between 1 (included) and 31 (included), 
     *  - if one the values takes more bits than its size
     *  - if the sum of the sizes is more that 32
     */
    public static long pack(long v1, int s1, long v2, int s2) {
        validatePackArgument(v1, s1);
        validatePackArgument(v2, s2);
        checkArgument((s1+s2) <= Long.SIZE);
        
        long decal1 = v1;
        long decal2 = v2 << s1;
        
        return decal1|decal2; // was decal1 + decal2
    }
    
    /** verifies that
     * - the size is between 1 and 63 (included)
     * - the value is not bigger than the size it should take.
     * 
     */
    private static void validatePackArgument(long value, int size) {
        checkArgument(size >= 1);
        checkArgument(size < Long.SIZE);
        
        // check is 
        long sizeMask = mask(size, Long.SIZE-size);
        checkArgument((sizeMask & value) == 0);
    }
}
