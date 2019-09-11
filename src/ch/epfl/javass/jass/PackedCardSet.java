package ch.epfl.javass.jass;

import java.util.StringJoiner;

import ch.epfl.javass.bits.Bits64;

/**
 * The class is used to manipulate set of cards.
 * As there are only 36 cards, it is possible to represent a card set
 * using a (long) because it has 64 bits. The structure of the type goes as
 *  - bits 0 to 15: SPADE
 *  - bits 16 to 31: HEART
 *  - bits 32 to 47: DIAMOND
 *  - bits 48 to 63: CLUB
 *
 * The first of each tranche is reserved for the rank SIX,
 * the second for rank SEVEN and so on... (see Card.Rank for more)
 * 
 * If the i-th bit of the j-th tranche is 1, it means the card of 
 * rank ordinal i and of color ordinal j is in the set.
 * @author xavier
 *
 */
public final class PackedCardSet {
    private PackedCardSet() {}
    
    /// empty packed card set
    public static final long EMPTY = 0;
    /**
     * The value for ALL_CARDS was precomputed for better performance
     * The value is not -1L because this would be an invalid card set
     */
    public static final long ALL_CARDS = 143835907860922879L;
    
    /**
     * the shape of the tensor is [colorOrdinal][rankOrdinal]
     * Each value was computed separately and tested.
     * This was done for the purpose of computing the possible next steps
     * in a game of Jass for a reduced amount of computing time.
     * Which is particularly useful when using an AI simulating 
     * many games
     */
    private static final long[][] precomputedTrumpsAbove = {
            {510,508,504,32,488,0,424,296,40},
            {33423360,33292288,33030144,2097152,31981568,0,27787264,19398656,2621440},
            {2190433320960L,2181843386368L,2164663517184L,137438953472L,2095944040448L,0L,1821066133504L,1271310319616L,171798691840L},
            {143552238122434560L,142989288169013248L,141863388262170624L,9007199254740992L,137359788634800128L,0L,119345390125318144L,83316593106354176L,11258999068426240L}
    };
    
    // of from [colorOrdinal]
    private static final long[] precomputedColorSubsetMasks = {
            511, 33488896, 2194728288256L, 143833713099145216L
    };
    
    // size of each component of the pkCardSet
    private static int SUBSET_SIZE = 16;
    // start index of unused bits in each component
    private static int UNUSED_START_INDEX = 9;
    // size of unused bits in each component
    private static int UNUSED_SIZE = 7;
    
    /**
     * Used is isValid(long) to check if every Color
     * in the pkCardSet is using only the reserved bits 0 to 8.
     * See the javadoc for isValid(long) for more information
     * @param pkCardSet
     * @param index
     * @param size
     * @return
     */
    private static boolean subsetIsValid(long pkCardSet, int index, int size) {
        long colorPart = Bits64.extract(pkCardSet, index, size);
        long unusedPart = Bits64.extract(colorPart, UNUSED_START_INDEX, UNUSED_SIZE);
        return unusedPart==0;
    }
    
    /**
     * A pkCardSet is of form 
     * (CLUB (48->63) | DIAMOND (32->47) | HEART (16->31) | SPADE (0->15) )
     * and each color should use bits 0->8 an leave 9->15 unused:
     * there are only 9 cards in Jass thus we only need 9 bits to describe the
     * power set of all the cards (i.e. all the possible sets of cards).
     * @param pkCardSet
     * @return
     */
    public static boolean isValid(long pkCardSet) {
        boolean isValid = subsetIsValid(pkCardSet, 0, SUBSET_SIZE);
        isValid &= subsetIsValid(pkCardSet, SUBSET_SIZE, SUBSET_SIZE);
        isValid &= subsetIsValid(pkCardSet, 2*SUBSET_SIZE, SUBSET_SIZE);
        isValid &= subsetIsValid(pkCardSet, 3*SUBSET_SIZE, SUBSET_SIZE);
        return isValid;
    }
    
    /**
     * Returns a pkCardSet that represents the set of cards
     * that are strictly above pkCard given that the color of the pkCard
     * is the trump color.
     * We use precomputed values for performance
     * @param pkCard
     * @return
     */
    public static long trumpAbove(int pkCard) {
        int color = PackedCard.color(pkCard).ordinal();
        int rank = PackedCard.rank(pkCard).ordinal();
        return precomputedTrumpsAbove[color][rank];
    }
    
    /**
     * Returns a pkCardSet that represents only one element:
     * the one represented by pkCard.
     * @param pkCard
     * @return
     */
    public static long singleton(int pkCard) {
//        int colorIndex = 16*PackedCard.color(pkCard).ordinal();
//        int rankIndex = PackedCard.rank(pkCard).ordinal();
//        return Bits64.mask(colorIndex+rankIndex, 1);
        return 1L << pkCard;
    }
    
    /**
     * A pkCardSet is empty iff all its bits are set to 0
     * @param pkCardSet
     * @return
     */
    public static boolean isEmpty(long pkCardSet) {
        return pkCardSet == EMPTY;
    }
    
    /**
     * To get the size of the set represented by the pkCardSet
     * If a card is in the set, the bit at its index is going to be 1. (and conversely)
     * Thus we can just count the number of bits set to 1 inside the pkCardSet.
     * @param pkCardSet
     * @return
     */
    public static int size(long pkCardSet) {
        return Long.bitCount(pkCardSet);
    }
    
    private static int getCardForBitPositioning(long pkCardSet, int bitIndex) {
        if((Bits64.mask(bitIndex, 1) & pkCardSet) == 0)
            return PackedCard.INVALID;
        int rank = bitIndex % 16;
        int color = (bitIndex-rank)/16;
        return PackedCard.pack(Card.Color.values()[color], Card.Rank.values()[rank]);
    }
    
    /**
     * Returns the pkCard that corresponds to the specified index
     * If the pkCardSet does not contain any card,
     * it returns PackedCard.Invalid
     * @param pkCardSet
     * @param index
     * @return
     */
    public static int get(long pkCardSet, int index) {
        long lowestOneBit = Long.lowestOneBit(pkCardSet);
        while(lowestOneBit != 0 & index-->0) {
            pkCardSet -= lowestOneBit;
            lowestOneBit = Long.lowestOneBit(pkCardSet);
        }
//        return getCardForBitPositioning(pkCardSet, Long.numberOfTrailingZeros(lowestOneBit));
        return Long.numberOfTrailingZeros(lowestOneBit);
    }
    
    /**
     * Given a pkCardSet, it adds the pkCard into the set.
     * If the card was already there, the pkCardSet
     * is just left unchanged
     * @param pkCardSet
     * @param pkCard
     * @return
     */
    public static long add(long pkCardSet, int pkCard) {
        return pkCardSet | singleton(pkCard);
    }
    
    /**
     * Return the pkCardSet without the specified pkCard. 
     * If the pkCardSet does not contain the pkCard, the pkCardSet
     * is left unchanged
     * @param pkCardSet
     * @param pkCard
     * @return
     */
    public static long remove(long pkCardSet, int pkCard) {
        long cardSingleton = singleton(pkCard);
        return intersection(complement(cardSingleton), pkCardSet);
//        long allOne = Bits64.mask(0, Long.SIZE);
//        long allOneButpkCard = allOne - cardSingleton;
//        return allOneButpkCard&pkCardSet;
    }
    
    /**
     * Returns true iff the pkCard is in the pkCardSet
     * @param pkCardSet
     * @param pkCard
     * @return
     */
    public static boolean contains(long pkCardSet, int pkCard) {
        return (singleton(pkCard) & pkCardSet) != 0;
    }
    
    /**
     * Returns the set of cards that are not in the pkCardSet
     * @param pkCardSet
     * @return
     */
    public static long complement(long pkCardSet) {
        return ALL_CARDS&(~pkCardSet);
    }
    
    /**
     * Returns the set of cards which are in pkCardSet1 or in pkCardSet2
     * @param pkCardSet1
     * @param pkCardSet2
     * @return
     */
    public static long union(long pkCardSet1, long pkCardSet2) {
        return pkCardSet1 | pkCardSet2;
    }
    
    /**
     * Returns the set of cards which are in pkCardSet1 and in pkCardSet2
     * @param pkCardSet1
     * @param pkCardSet2
     * @return
     */
    public static long intersection(long pkCardSet1, long pkCardSet2) {
        return pkCardSet1 & pkCardSet2;
    }
    
    
    /**
     * Returns the set of cards that are in pkCardSet1 and not in pkCardSet2
     * @param pkCardSet1
     * @param pkCardSet2
     * @return
     */
    public static long difference(long pkCardSet1, long pkCardSet2) {
        return (pkCardSet1)&complement(pkCardSet2);
    }
    
    /**
     * Get a pkCardSet that represents all the cards of the 
     * given color. We use precomputedValues for performance
     * @param pkCardSet
     * @param color
     * @return
     */
    public static long subsetOfColor(long pkCardSet, Card.Color color) {
        int colorIndex = color.ordinal();
        return pkCardSet & precomputedColorSubsetMasks[colorIndex];
    }
    
    /**
     * Returns a string representation of the pkCardSet
     * @param pkCardSet
     * @return
     */
    public static String toString(long pkCardSet) {
        StringJoiner result = new StringJoiner(",", "{", "}"); 
        for(Card.Color color: Card.Color.values()) {
            for(Card.Rank rank: Card.Rank.values()) {
                int packed = PackedCard.pack(color, rank);
                if(contains(pkCardSet, packed)) {
                    result.add(PackedCard.toString(packed));
                }
            }
        }
        return result.toString();
    }
}
