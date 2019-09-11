package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;
import static ch.epfl.javass.Preconditions.checkIndex;
import static ch.epfl.javass.Preconditions.checkState;

/**
 * Class used to represent a Trick. In Jass a trick can be represented 
 * by the following attributes:
 *  - trump of the trick
 *  - first player of the trick
 *  - the set of cards layed down so far (can be 4 of them)
 *  - index of the trick
 *  
 * We can pack all these attributes in an int using the 
 * format described in PackedTrick.java
 * 
 * this attribute is final thus immutable after init
 * @author xavier
 *
 */
public final class Trick {
    private final int packedTrick;
    
    private Trick(int packed) {
        this.packedTrick = packed;
    }
    
    /**
     * Represents an invalid trick
     */
    public final static Trick INVALID = new Trick(PackedTrick.INVALID);
    
    /**
     * Get a Trick that represents the packed version given
     * Throws IllegalArgumentException if the packed is invalid
     * @param packed
     * @return
     */
    public static Trick ofPacked(int packed) {
        checkArgument(PackedTrick.isValid(packed));
        return new Trick(packed);
    }
    
    /**
     * Gives the packed representation of this Trick
     * @return
     */
    public int packed() {
        return packedTrick;
    }
    
    public static Trick firstEmpty(Card.Color trump, PlayerId firstPlayer) {
        int packed = PackedTrick.firstEmpty(trump, firstPlayer);
        return ofPacked(packed);
    }
    
    /**
     * Returns the trick that has index one up of this
     * and with 0 cards inside, as well as with the 
     * correct trump and winner
     * Throws IllegalStateException if this trick is full,
     * because in this case the nextEmpty makes no sense
     * @return
     */
    public Trick nextEmpty() {
        checkState(isFull());
        int packed = PackedTrick.nextEmpty(packed());
        return new Trick(packed);
    }
    
    /**
     * Returns true iff no card has been played
     * @return
     */
    public boolean isEmpty() {
        return PackedTrick.isEmpty(packed());
    }
    
    /**
     * Returns true iff 4 cards have been played
     * @return
     */
    public boolean isFull() {
        return PackedTrick.isFull(packed());
    }
    
    /**
     * Returns true iff this trick is the last 
     * of the turn
     * @return
     */
    public boolean isLast() {
        return PackedTrick.isLast(packed());
    }
    
    /**
     * Returns how many cards are currently played
     * @return
     */
    public int size() {
        return PackedTrick.size(packed());
    }
    
    /**
     * Returns the trump color of the trick
     * @return
     */
    public Card.Color trump() {
        return PackedTrick.trump(packed());
    }
    
    /**
     * Returns the index of the trick, 
     * i.e. 0 if it is the 1st trick of the turn,
     *  1 if its the 2nd and so on (8 is the max index,
     *  as there are only 9 tricks in a turn)
     * @return
     */
    public int index() {
        return PackedTrick.index(packed());
    }
    
    /**
     * Returns the player who played the 
     * card at given index
     * Throws IndexOfOfBoundsException if the given index 
     * is more that the max index of a trick (i.e. 3)
     * @param index
     * @return
     */
    public PlayerId player(int index) {
        checkIndex(index, 4);
        return PackedTrick.player(packed(), index);
    }
    
    /**
     * Returns the card at given index
     * Throws IndexOutOfBoundsException if the given index
     * is above the size of this trick
     * @param index
     * @return
     */
    public Card card(int index) {
        checkIndex(index, size());
        int packed = PackedTrick.card(packed(), index);
        return Card.ofPacked(packed);
    }
    
    /**
     * Returns the same trick but with a card played
     * added to the trick
     * It throws IllegalStateException if this trick is full
     * @param c
     * @return
     */
    public Trick withAddedCard(Card c) {
        checkState(!isFull());
        int addedPkTrick = PackedTrick.withAddedCard(packed(), c.packed());
        return Trick.ofPacked(addedPkTrick);
    }
    
    /**
     * To get the color of the first card played.
     * It throws IllegalStateException is the trick is empty,
     * because the first card does not exist in this case
     * @return
     */
    public Card.Color baseColor() {
        checkState(!isEmpty());
        return PackedTrick.baseColor(packed());
    }
    
    /**
     * To get the set of playable card given the hand.
     * There are specific rules that make certain cards playable,
     * which are stated in PackedTrick.java. 
     * It throws IllegalStateException if the trick is full
     * @param hand
     * @return
     */
    public CardSet playableCards(CardSet hand) {
        checkState(!isFull());
        long playablePkSet = PackedTrick.playableCards(packed(), hand.packed());
        return CardSet.ofPacked(playablePkSet);
    }
    
    /**
     * Returns the number of points this trick represents
     * @return
     */
    public int points() {
        return PackedTrick.points(packed());
    }
    
    /**
     * Reutrns the winning player, i.e. the person that has so far
     * played the best card.
     * Throws IllegalStateException if this trick is emtpy,
     * because there can be no winning player then.
     * @return
     */
    public PlayerId winningPlayer() {
        checkState(!isEmpty());
        return PackedTrick.winningPlayer(packed());
    }
    
    /**
     * Returns true iff that is a Trick and has same
     * packed as this
     */
    public boolean equals(Object that) {
        if(that instanceof Trick)
            return ((Trick) that).packed() == this.packed();
        return false;
    }
    
    /**
     * Gives string representation of the Trick
     */
    public String toString() {
        return PackedTrick.toString(packed());
    }
    
    /**
     * Returns hashCode for that object
     * It has to be equal to the hashCode of 
     * its packed due to the way we compare
     * (with equals)
     */
    public int hashCode() {
        return Integer.hashCode(packed());
    }
}
