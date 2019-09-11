package ch.epfl.javass.jass;

import java.util.Map;

/**
 * This class implements a player, where we make sure that the time 
 * he spends deciding the card to play is at least the attribute minTime.
 * 
 * This is done to prevent analysis of the state of the game by some players,
 * which a too long of a deciding time/too short of a deciding time might reveal.
 * 
 * For example, if an AI has only 1 card to play, it will be fast to decide, 
 * and it could reveal too much about its hand.
 * @author xavier
 *
 */
public final class PacedPlayer implements Player {
    final private Player underlyingPlayer;
    final private int minTime; // in millis
    /**
     * A PacedPlayer behaves like the underlyingPlayer given
     * but makes sure the method that plays the card is taking minTime
     * @param underlyingPlayer
     * @param minTime IN SECONDS
     */
    public PacedPlayer(Player underlyingPlayer, double minTime) {
        this.underlyingPlayer = underlyingPlayer;
        this.minTime = (int) (minTime*1000);
    }
    
    /**
     * indicates the card that the player wants to play
     * @param state
     * @param hand
     * @return
     */
    public Card cardToPlay(TurnState state, CardSet hand) {
        long beforeTime = System.currentTimeMillis();
        Card result = underlyingPlayer.cardToPlay(state, hand);
        long afterTime = System.currentTimeMillis();
        long ellapsedMillis = afterTime-beforeTime;
        if(ellapsedMillis < minTime) {
            try {
                Thread.sleep(minTime-ellapsedMillis);
              } catch (InterruptedException e) { /* ignore */ }
        }
        return result;
    }
    
    /**
     * A method called at the beginning of each game to set the id of the player
     * and tell the player about the names assigned to everyone (playerNames)
     * @param ownId
     * @param playerNames
     */
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        underlyingPlayer.setPlayers(ownId, playerNames);
    }
    
    /**
     * Called each time the hand of a player changes.
     * It can be called at the beginning of the turn (where new cards are given)
     * Or each time the player plays a cards.
     * @param newHand
     */
    public void updateHand(CardSet newHand) {
        underlyingPlayer.updateHand(newHand);
    }
    
    /**
     * Called each time a new  trump is set.
     * @param trump
     */
    public void setTrump(Card.Color trump) {
        underlyingPlayer.setTrump(trump);
    }
    
    /**
     * Called each time the trick has changed
     * i.e. each time a card is played
     * or each time a trick is collected
     * @param newTrick
     */
    public void updateTrick(Trick newTrick) {
        underlyingPlayer.updateTrick(newTrick);
    }
    
    /**
     * Called each time the score is updated
     * i.e. each time a trick is collected
     * @param score
     */
    public void updateScore(Score score) {
        underlyingPlayer.updateScore(score);
    }
    
    /**
     * Called as soon a team has more than 1000 points
     * @param winningTeam
     */
    public void setWinningTeam(TeamId winningTeam) {
        underlyingPlayer.setWinningTeam(winningTeam);
    }
}
