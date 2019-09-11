package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * The class is used to represent a card, which has two attributes:
 *  - rank
 *  - color
 * We represent the card using an (int), formatted in a way that 
 * is explained in PackedCard.java 
 * 
 * The class is immutable and has no public constructor.
 * However, it has a static method of(_;_) to make an instance
 * of the class
 * @author xavier
 *
 */
public final class Card {
    private final int packedCard;
    
    private Card(int packed) {
        packedCard = packed;
    }
    
    /**
     * To get an instance of a card with specified color and rank
     */
    public static Card of(Color c, Rank r) {
        return new Card(PackedCard.pack(c, r));
    }
    
    /**
     * To get an instance of a card with specified packed representation
     */
    public static Card ofPacked(int packed) {
        checkArgument(PackedCard.isValid(packed));
        return new Card(packed);
    }
    
    /**
     * Get the packed representation
     */
    public int packed() {
        return packedCard;
    }
    
    /**
     * Get the color of the card
     */
    public Color color() {
        return PackedCard.color(packedCard);
    }
    
    /**
     * Get the rank of the card
     */
    public Rank rank() {
        return PackedCard.rank(packedCard);
    }
    
    /**
     * get if this card is better than that card given trump value
     */
    public boolean isBetter(Color trump, Card that) {
        return PackedCard.isBetter(trump, this.packed(), that.packed());
    }
    
    /**
     * get points for the card given trump
     */
    public int points(Color trump) {
        return PackedCard.points(trump, this.packed());
    }
    
    /**
     * True only if thatO is a card and has the same packed representation as this
     */
    public boolean equals(Object thatO) {
        if(thatO instanceof Card) {
            return ((Card) thatO).packed() == this.packed();
        }
        return false;
    }
    
    /**
     * Hashcode of two equal value should be equal.
     */
    public int hashCode() {
        return this.packed();
    }
    
    /**
     * Visual representation of the card
     */
    public String toString() {
        return PackedCard.toString(this.packed());
    }
    
    /**
     * To get all the cards possible in Jass
     * The method is used in JassGame for instantiating 
     * the intial deck of cards
     * @return
     */
    public static List<Card> getAllCards() {
        List<Card> allCards = new ArrayList<Card>();
        for (Card.Color c: Card.Color.ALL) {
            for (Card.Rank r: Card.Rank.ALL) {
                allCards.add(Card.of(c, r));
            }
        }
        return allCards;
    }
    
    public enum Color {
        SPADE,
        HEART,
        DIAMOND,
        CLUB;
        
        // this returns an non modifiable version of values()
        public static final List<Color> ALL = Collections.unmodifiableList(Arrays.asList(values()));
        // how many values there are
        public static final int COUNT = ALL.size();
        
        @Override
        /**
         * Using special unicode characters 
         * Their representation is full
         */
        public String toString() {
            switch(this) {
            case SPADE:
                return "\u2660";
            case HEART:
                return "\u2665";
            case DIAMOND:
                return "\u2666";
            case CLUB:
                return "\u2663";
            }
            // never reached since there is other value for (this) than 
            // what we compared it with in the switch statement
            return null;
        }
    }
    
    public enum Rank {
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JACK,
        QUEEN,
        KING,
        ACE;
        
        // Get the unmodifiable version of value()
        public static final List<Rank> ALL = Collections.unmodifiableList(Arrays.asList(values()));
        // How many cards there are
        public static final int COUNT = 9;
        
        /*
         * Get the ordinal of the card in the situtation where its color is the trump
         */
        public int trumpOrdinal() {
            switch(ordinal()) {
            case 0: case 1: case 2: // SIX, SEVEN, EIGHT
                return ordinal();
            case 3: // NINE
                return 7;
            case 4: // TEN
                return 3;
            case 5: // JACK
                return 8;
            case 6: case 7: case 8: // QUEEN, KING, ACE
                return ordinal()-2;
            }
            return -1; // never reached since we checked for every case
        }
        
        /**
         * visual representation of the rank of the card
         */
        public String toString() {
            if(this.ordinal() <= 4)
                return Integer.toString(this.ordinal()+6);
            
            switch(this.ordinal()) {
            case 5: return "J";
            case 6: return "Q";
            case 7: return "K";
            case 8: return "A";
            }
            return ""; // never reached since we checked for every case
        }
    }
}
