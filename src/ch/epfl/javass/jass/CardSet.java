package ch.epfl.javass.jass;

import java.util.List;
import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * Class used to represent a set of cards.
 * In jass, there are 36 cards. So you can represent a set of jass cards
 * using only 36 bits: each one telling you if the card represented by the bit
 * is in the set or not. For example in a set of 2 objects:
 *  - an apple
 *  - an orange
 * You could represent the set {apple} by 10, {apple, orange} by 11,
 *  {orange} by 01, and {} by 00. The bit to the left tells you 
 *  if the apple is in the set (1=in, 0=not in) and the bit on the right
 *  tells you if the orange is in.
 * The same goes for a set of 36 cards.
 *  
 * the packedCardSet attribute is a (long) that represent a set of cards, 
 * following the format explained in PackedCardSet.java
 * 
 * A CardSet is immutable
 * @author xavier
 *
 */
public final class CardSet {
    private final long packedCardSet;
    
    // cardset that is empty (0 card)
    public static CardSet EMPTY = CardSet.ofPacked(PackedCardSet.EMPTY);
    // cardset that contains all cards possible (36 cards)
    public static CardSet ALL_CARDS = CardSet.ofPacked(PackedCardSet.ALL_CARDS);
    
    private CardSet(List<Card> cards) {
        long packed = PackedCardSet.EMPTY;
        for(Card card: cards) {
            packed = PackedCardSet.add(packed, card.packed());
        }
        packedCardSet = packed;
    }
    
    private CardSet(long packed) {
        packedCardSet = packed;
    }
    
    /**
     * To get an instance of this class which packed
     * represents the set of cards passed in args
     * We don't use constructors for clarity purpose
     * @param cards
     * @return
     */
    public static CardSet of(List<Card> cards) {
        return new CardSet(cards);
    }
    
    /**
     * To get an instance of this class
     * which packed is equal to the packed passed in args
     * We don't use constructors for clarity purpose
     * Throws IllegalArgumentException if packed is not valid
     * @param packed
     * @return
     */
    public static CardSet ofPacked(long packed) {
        checkArgument(PackedCardSet.isValid(packed));
        
        return new CardSet(packed);
    }
    
    /**
     * To get the packed version of the set
     * @return
     */
    public long packed() {
        return packedCardSet;
    }
    
    /**
     * True iff there are no cards
     * @return
     */
    public boolean isEmpty() {
        return PackedCardSet.isEmpty(packed());
    }
    
    /**
     * Return the number of elements in the CardSet
     * @return
     */
    public int size() {
        return PackedCardSet.size(packed());
    }
    
    /**
     * To get the card at specified index.
     * Getting Card.INVALID if the CardSet does
     * not have the card at spec index
     * @param index
     * @return
     */
    public Card get(int index) {
        int pkCard = PackedCardSet.get(packed(), index);
        return Card.ofPacked(pkCard);
    }
    
    /**
     * Returns the CardSet in which we added the specified
     * card.
     * @param card
     * @return
     */
    public CardSet add(Card card) {
        long pkSet = PackedCardSet.add(packed(), card.packed());
        return CardSet.ofPacked(pkSet);
    }
    
    /**
     * Returns the CardSet from which we removed the 
     * specified card.
     * @param card
     * @return
     */
    public CardSet remove(Card card) {
        long pkSet = PackedCardSet.remove(packed(), card.packed());
        return CardSet.ofPacked(pkSet);
    }
    
    /**
     * Returns true iff the CardSet contains the specified card.s
     * @param card
     * @return
     */
    public boolean contains(Card card) {
        return PackedCardSet.contains(packed(), card.packed());
    }
    
    /**
     * Returns the set of cards that are not in the CardSet
     * @return
     */
    public CardSet complement() {
        long pkSet = PackedCardSet.complement(packed());
        return CardSet.ofPacked(pkSet);
    }
    
    /**
     * Returns the set of elements that are in this cardset or 
     * that cardset.
     * @param that
     * @return
     */
    public CardSet union(CardSet that) {
        long pkSet = PackedCardSet.union(this.packed(), that.packed());
        return CardSet.ofPacked(pkSet);
    }
    
    /**
     * Returns the set of elements that are in this cardset
     * and in that cardset
     * @param that
     * @return
     */
    public CardSet intersection(CardSet that) {
        long pkSet = PackedCardSet.intersection(this.packed(), that.packed());
        return CardSet.ofPacked(pkSet);
    }
    
    /**
     * Returns the set of elements that are in this cardset
     * but not in that cardset
     * @param that
     * @return
     */
    public CardSet difference(CardSet that) {
        long pkSet = PackedCardSet.difference(this.packed(), that.packed());
        return CardSet.ofPacked(pkSet);
    }
    
    /**
     * Returns a cardset in which there are only the cards
     * of the specified color from this cardset.
     * @param color
     * @return
     */
    public CardSet subsetOfColor(Card.Color color) {
        long pkSet = PackedCardSet.subsetOfColor(packed(), color);
        return CardSet.ofPacked(pkSet);
    }
    
    /**
     * Returns true iff that is a CardSet and has same
     * packed as this
     */
    public boolean equals(Object that) {
        if(that instanceof CardSet)
            return ((CardSet) that).packed() == this.packed();
        return false;
    }
    
    /**
     * Gives string representation of the CardSet
     */
    public String toString() {
        return PackedCardSet.toString(packed());
    }
    
    /**
     * Returns hashCode for that object
     * It has to be equal to the hashCode of 
     * its packed due to the way we compare
     * (with equals)
     */
    public int hashCode() {
        return Long.hashCode(packed());
    }
}
