package ch.epfl.javass.bits;
import static ch.epfl.javass.Preconditions.checkArgument;
import static ch.epfl.javass.Preconditions.checkIndex;

/**
 * The class is used to do bitwise manipulation on (int) instances.
 * The (int) type has a binary representation of 32 bits.
 * 
 * @author xavier
 *
 */
public final class Bits32 {
    // Non-instantiable
    private Bits32() {}
    
    /** Check if [start; start+size-1] is an integer intarval and if it is in [0;31]
     * 
     */
    private static void checkIndexInterval(int start, int size) {
        checkArgument(start >= 0);
        checkArgument(start+size <= Integer.SIZE);
        checkArgument(size >= 0);
    }
    
    /**
     * Used to get the the mask from 0 up to the index:
     * example: for index = 3 it looks like 111
     * Special case if index is 32: (1<<index) overflows and gives 1.
     * @param index
     * @return
     */
    private static int getPartialMask(int index) {
        return (index == Integer.SIZE) ? -1 : (1 << index)-1;
    }
    
    /** It's a function to get a (int)
     * which bits from start to start+size-1 are set to 1 and the rest to 0.
     * 
     * Throws IllegalArgumentException if [start; start+size-1] is not in [0;31]
     * 
     * @param start index of the first bit 
     * @param size size of the mask
     * @return An (int) such that the bits from start to start+size-1 are set to 1, the rest to 0.
     */
    public static int mask(int start, int size) {
        checkIndexInterval(start, size);
        
        // maskSup has all bits from 0 to start+size-1 set to 1.
        int maskSup = getPartialMask(start+size);
        // maskInf has all bits from 0 to start-1 set to 1
        int maskInf = getPartialMask(start);
                
        // this thus gives all bits from start to start+size-1 to 1
        return maskSup - maskInf;
    }
    
    /** Extracts from the argument bits its... 
     * ...bits from start to start+size-1. The rest is 0.
     * 
     * @param bits (int) from which we want to extract bits from start to start+size-1
     * @param start index of the first bit
     * @param size size of the mask
     */
    public static int extract(int bits, int start, int size) {
        checkIndexInterval(start, size);
        
        int extracted = mask(start, size) & bits;
        // using >>> because we need a logical shift right not an arithmetic one
        // in case of an int (1100000....0000) with an arithmetic shift number >> 30
        // we would end up with something like (11111.....111110) (it extends the sign)
        // which is not what we want here.
        int extractedShifted = extracted >>> start;
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
    public static int pack(int v1, int s1, int v2, int s2) {
        validatePackArgument(v1, s1);
        validatePackArgument(v2, s2);
        checkArgument((s1+s2) <= Integer.SIZE);
        
        int decal1 = v1;
        int decal2 = v2 << s1;
        
        return decal1+decal2;
    }
    
    /*
     * Same as pack for 2 values.
     * We thus obtain (v3 | v2 | v1)
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) {
        validatePackArgument(v1, s1);
        validatePackArgument(v2, s2);
        validatePackArgument(v3, s3);
        checkArgument((s1+s2+s3) <= Integer.SIZE);
        
        int decal1 = v1;
        int decal2 = v2 << s1;
        int decal3 = v3 << (s1+s2);
        
        return decal1+decal2+decal3;
    }
    
    /*
     * Same as pack for 2 values:
     * We thus obtain (v7 | v6 | v5 | v4 | v3 | v2 | v1)
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3, int v4, int s4, int v5, int s5, int v6, int s6, int v7, int s7) {
        validatePackArgument(v1, s1);
        validatePackArgument(v2, s2);
        validatePackArgument(v3, s3);
        validatePackArgument(v4, s4);
        validatePackArgument(v5, s5);
        validatePackArgument(v6, s6);
        validatePackArgument(v7, s7);
        checkArgument((s1+s2+s3+s4+s5+s6+s7) <= Integer.SIZE);
        
        int decal1 = v1;
        int decal2 = v2 << s1;
        int decal3 = v3 << (s1+s2);
        int decal4 = v4 << (s1+s2+s3);
        int decal5 = v5 << (s1+s2+s3+s4);
        int decal6 = v6 << (s1+s2+s3+s4+s5);
        int decal7 = v7 << (s1+s2+s3+s4+s5+s6);
        
        return decal1+decal2+decal3+decal4+decal5+decal6+decal7;
    }
    
    /** verifies that
     * - the size is between 1 and 31 (included)
     * - the value is not bigger than the size it should take.
     * 
     */
    private static void validatePackArgument(int value, int size) {
        checkArgument(size >= 1);
        checkArgument(size < Integer.SIZE);
        
        // check is 
        int sizeMask = mask(size, Integer.SIZE-size);
        checkArgument((sizeMask & value) == 0);
    }
}
