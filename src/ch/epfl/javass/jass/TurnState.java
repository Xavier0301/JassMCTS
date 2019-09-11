package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;
import static ch.epfl.javass.Preconditions.checkState;

/**
 * The class is used to represent the state of a turn. 
 * In jass, the turn is described with three attributed:
 *  - a score
 *  - the set of unplayed cards so far
 *  - the state of the trick
 *  
 * It is a very high level way of describing the turn,
 * and if you want to get better understanding of
 *  - how a score is represented go to Score.java
 *  - how a set of cards is represented go to CardSet.java
 *  - how a trick is represented go to Trick.java
 * @author xavier
 *
 */
public final class TurnState {
    private final long packedScore;
    private final long packedUnplayedCards;
    private final int packedCurrentTrick;
    
    private TurnState(Card.Color trump, Score score, PlayerId firstPlayer) {
        this.packedScore = score.packed();
        this.packedUnplayedCards = PackedCardSet.ALL_CARDS;
        this.packedCurrentTrick = PackedTrick.firstEmpty(trump, firstPlayer);
    }
    
    private TurnState(long pkScore, long pkUnplayedCards, int pkTrick) {
        this.packedScore = pkScore;
        this.packedUnplayedCards = pkUnplayedCards;
        this.packedCurrentTrick = pkTrick;
    }
    
    /**
     * To get an TurnState which currentTrick is the first empty of trump and firstPlayer,
     * and which score is as given. The unplayedCards here is the set of all cards
     * since the trick is empty.
     * @param trump
     * @param score
     * @param firstPlayer
     * @return
     */
    public static  TurnState initial(Card.Color trump, Score score, PlayerId firstPlayer) {
        return new TurnState(trump, score, firstPlayer);
    }
    
    /**
     * To get a TurnState which packed components correspond to the given packed
     * Throws IllegalArgumentException if any of the given packed is invalid
     * @param pkScore
     * @param pkUnplayedCards
     * @param pkTrick
     * @return
     */
    public static TurnState ofPackedComponents(long pkScore, long pkUnplayedCards, int pkTrick) {
        checkArgument(PackedScore.isValid(pkScore));
        checkArgument(PackedCardSet.isValid(pkUnplayedCards));
        checkArgument(PackedTrick.isValid(pkTrick));
        
        return new TurnState(pkScore, pkUnplayedCards, pkTrick);
    }
    
    /**
     * A getter for the packed version of the score of the turn
     * @return
     */
    public long packedScore() {
        return packedScore;
    }
    
    /**
     * A getter for the packed version of the unplayed cards in the 
     * current trick of the turn
     * @return
     */
    public long packedUnplayedCards() {
        return packedUnplayedCards;
    }
    
    /**
     * A getter for the packed version current trick of the turn
     * @return
     */
    public int packedTrick() {
        return packedCurrentTrick;
    }
    
    /**
     * A getter for the score of the turn
     * @return
     */
    public Score score() {
        return Score.ofPacked(packedScore);
    }
    
    /**
     * A getter for the set of unplayed cards in the current trick
     * of the turn
     * @return
     */
    public CardSet unplayedCards() {
        return CardSet.ofPacked(packedUnplayedCards);
    }
    
    /**
     * A getter for the current trick of the turn
     * @return
     */
    public Trick trick() {
        return Trick.ofPacked(packedCurrentTrick);
    }
    
    /**
     * a turn is terminal if the current trick is the last possible trick
     * i.e. it's the 9th trick of the turn and if the current trick is full
     * @return
     */
    public boolean isTerminal() {
        if(!PackedTrick.isValid(packedCurrentTrick))
            return true;
        return ((trick().index() == Jass.TRICKS_PER_TURN-1) & trick().isFull());
    }
    
    /**
     * The next player is the player that comes after the last player 
     * who played in the current trick
     * Therefore, it throws IllegalStateException if the current trick is full
     * @return
     */
    public PlayerId nextPlayer() {
        checkState(!PackedTrick.isFull(packedCurrentTrick));
        return PackedTrick.player(packedCurrentTrick, PackedTrick.size(packedCurrentTrick));
    }
    
    /**
     * Returns the TurnState in which we added the given card to the current trick.
     * We also removed the given card from the set of unplayed cards
     * Therefore, it throws IllegalStateException if the current trick is full
     * @param card
     * @return
     */
    public TurnState withNewCardPlayed(Card card) {
        checkState(!PackedTrick.isFull(packedCurrentTrick));
        int packedNextTrick = PackedTrick.withAddedCard(packedCurrentTrick, card.packed());
        long packedNextUnplayedCards = PackedCardSet.remove(packedUnplayedCards, card.packed());
        return TurnState.ofPackedComponents(packedScore, packedNextUnplayedCards, packedNextTrick);
    }
    
    /**
     * Returns the TurnState for which the score has been updated to include the score
     * of the current trick (which is full), and for which the current trick is therefore empty
     * (since the trick has been collected) and the unplayed cards is reset to all cards
     * Throws IllegalStateArgument if the current trick is not full
     * @return
     */
    public TurnState withTrickCollected() {
        checkState(PackedTrick.isFull(packedCurrentTrick));
        
        int packedNextTrick = PackedTrick.nextEmpty(packedCurrentTrick);
        PlayerId winningPlayer = PackedTrick.winningPlayer(packedCurrentTrick);
        TeamId winningTeam = winningPlayer.team();
        long packedNextScore = PackedScore.withAdditionalTrick(packedScore, winningTeam, trick().points());
        
        return new TurnState(packedNextScore, packedUnplayedCards, packedNextTrick);
    }
    
    /**
     * Adds a new card to the current trick and then tries to collect the current 
     * trick IF POSSIBLE (i.e. if the current trick is full)
     * Throws IllegalStateArgument if the current trick is full, because then adding a card 
     * would be absurd
     * See withNewCardPlayed(Card) and withTrickCollected() for further informations
     * @param card
     * @return
     */
    public TurnState withNewCardPlayedAndTrickCollected(Card card) {
        checkState(!PackedTrick.isFull(packedCurrentTrick));
        
        TurnState nextTurn = this.withNewCardPlayed(card);
        if(PackedTrick.isFull(nextTurn.packedTrick())) {
            nextTurn = nextTurn.withTrickCollected();
        }
        return nextTurn;
    }
}
