package ch.epfl.javass.jass;

import java.util.Map;

/**
 * In jass, a player is the one to make decision about 
 * which card he wants to play. 
 * 
 * He also has methods to inform him of the players, hand, 
 * score, current trick state, winner.
 * 
 * These would be usually called in JassGame each time it should:
 * for example if another played a card, the player will be informed about
 * it via setTrick(_)
 * @author xavier
 *
 */
public interface Player {
    /**
     * indicates the card that the player wants to play
     * @param state
     * @param hand
     * @return
     */
    public Card cardToPlay(TurnState state, CardSet hand);
    
    /**
     * A method called at the beginning of each game to set the id of the player
     * and tell the player about the names assigned to everyone (playerNames)
     * @param ownId
     * @param playerNames
     */
    public default void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        
    }
    
    /**
     * Called each time the hand of a player changes.
     * It can be called at the beginning of the turn (where new cards are given)
     * Or each time the player plays a cards.
     * @param newHand
     */
    public default void updateHand(CardSet newHand) {
        
    }
    
    /**
     * Called each time a new  trump is set.
     * @param trump
     */
    public default void setTrump(Card.Color trump) {
        
    }
    
    /**
     * Called each time the trick has changed
     * i.e. each time a card is played
     * or each time a trick is collected
     * @param newTrick
     */
    public default void updateTrick(Trick newTrick) {
        
    }
    
    /**
     * Called each time the score is updated
     * i.e. each time a trick is collected
     * @param score
     */
    public default void updateScore(Score score) {
        
    }
    
    /**
     * Called as soon a team has more than 1000 points
     * @param winningTeam
     */
    public default void setWinningTeam(TeamId winningTeam) {
        
    }
}
