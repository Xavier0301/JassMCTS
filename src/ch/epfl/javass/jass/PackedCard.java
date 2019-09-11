package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * The class is used to manipulate (int) that represent a card.
 * In the binary representation of a card represented this way,
 * we have the following structure
 *  - bits 0 to 3: rank ordinal of max value 8, where the actual enum is in Card.Rank
 *  - bits 4 to 5: card color of max value 4, where the enum is in Card.Color
 *  - bits 6 to 31: unused bits. We only need the color and the rank to represent a card
 *  
 * @author xavier
 *
 */
public final class PackedCard {
    private PackedCard() {}
    
    /**
     * Represents an invalid PackedCard
     */
    public static final int INVALID = 0b111111;
    
    private static final int UNUSED_START = 6;
    private static final int UNUSED_SIZE = 26;
    
    private static final int RANK_SIZE = 4;
    private static final int COLOR_SIZE = 2;
    
    private static final int[] POSSIBLE_POINTS_IF_TRUMP = {0,0,0,14,10,20,3,4,11};
    private static final int[] POSSIBLE_POINTS_OTHERWISE = {0,0,0,0,10,2,3,4,11};
    
    
    /** A card is of form: (unused (6->31) | color (4->5) | rank (0->3))
     * it is valid iff the rank is between 0 and 8 (included)
     * and if it does not use the bits 6 to 31.
     * @param pkCard
     * @return
     */
    public static boolean isValid(int pkCard) {        
        int cardRank = Bits32.extract(pkCard, 0, RANK_SIZE);
        boolean isValid = (cardRank < Card.Rank.COUNT) && (cardRank >= 0);
        
        // card color is always between 0 and 3 as it takes 2 bits
        
        int unusedBitsMask = Bits32.extract(pkCard, UNUSED_START, UNUSED_SIZE);
        isValid &= (unusedBitsMask == 0);
        
        return isValid;
    }
    
    /**
     * Returns the representation of the card as (int) given color and rank
     * @param c
     * @param r
     * @return
     */
    public static int pack(Card.Color c, Card.Rank r) {
        return Bits32.pack(r.ordinal(), RANK_SIZE, c.ordinal(), COLOR_SIZE);
    }
    
    /**
     * Gets the color of the packedCard
     * @param pkCard
     * @return
     */
    public static Card.Color color(int pkCard) {
        assert isValid(pkCard);
        
        int colorOrdinal = Bits32.extract(pkCard, RANK_SIZE, COLOR_SIZE);
        return Card.Color.ALL.get(colorOrdinal);
    }
    
    /**
     * Gets the rank of the packedCard
     * @param pkCard
     * @return
     */
    public static Card.Rank rank(int pkCard) {
        assert isValid(pkCard);
        
        int rankOrdinal = Bits32.extract(pkCard, 0, RANK_SIZE);
        return Card.Rank.ALL.get(rankOrdinal);
    }
    
    /**
     * Algorithm to determine if the ls card is better to the rs card
     * They are not comparable if not of same color nor their color is trump.
     * Otherwise, if they are of same color we compare ordinal or trumpOrdinal
     * Otherwise if one is trump it is greater
     * @param trump
     * @param pkCardL
     * @param pkCardR
     * @return
     */
    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        assert isValid(pkCardL);
        assert isValid(pkCardR);
        
        Card.Color lColor = color(pkCardL);
        Card.Color rColor = color(pkCardR);
        
        Card.Rank lRank = rank(pkCardL);
        Card.Rank rRank = rank(pkCardR);
        
        if(lColor == rColor) {
            if(lColor == trump) 
                return (lRank.trumpOrdinal() > rRank.trumpOrdinal());
            else 
                return (lRank.ordinal() > rRank.ordinal());
        } else if(lColor == trump) {
            return true;
        }
        return false;
    }
    
    /**
     * The points that a card represents given the trump
     * @param trump
     * @param pkCard
     * @return
     */
    public static int points(Card.Color trump, int pkCard) {
        assert isValid(pkCard);
        
        Card.Rank cardRank = rank(pkCard);
        int[] currentPossibleValues = color(pkCard) == trump ? POSSIBLE_POINTS_IF_TRUMP : POSSIBLE_POINTS_OTHERWISE;
        
        return currentPossibleValues[cardRank.ordinal()];
    
    }
    
    /**
     * Visual representation of the card
     * @param pkCard
     * @return
     */
    public static String toString(int pkCard) {
        assert isValid(pkCard);
        
        return color(pkCard).toString() + rank(pkCard).toString();
    }
}
