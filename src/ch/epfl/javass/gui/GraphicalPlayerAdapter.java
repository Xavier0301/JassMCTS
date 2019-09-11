package ch.epfl.javass.gui;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.TurnState;
import javafx.collections.ListChangeListener;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;

import static javafx.application.Platform.runLater;

/**
 * This class is a Player, which as a graphical representation,
 * i.e. a call to any method of the Player interface is translated
 * into an update of the graphics. 
 * 
 * This is done partly by the GraphicalPlayer class, which handles directly
 * the graphics update. 
 * 
 * This class creates a window and is responsible for insuring that the methods
 * of the Player interface are overriden in such a way that the graphics are correctly
 * updated.
 * 
 * @author xavier
 *
 */
public final class GraphicalPlayerAdapter implements Player {
    
    private static final int QUEUE_CAPACITY = 1;
    
    private HandBean handBean;
    private ScoreBean scoreBean;
    private TrickBean trickBean;
    
    private GraphicalPlayer graphicalPlayer;
    
    private BlockingQueue<Card> blockingQueue;
    
    /**
     * Creates the GraphicalPlayerAdapter, but no window is creates here,
     * because it can only be created when the players are set.
     */
    public GraphicalPlayerAdapter() {
        handBean = new HandBean();
        scoreBean = new ScoreBean();
        trickBean = new TrickBean();
                
        blockingQueue = new ArrayBlockingQueue<Card>(QUEUE_CAPACITY);
    }
    
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        graphicalPlayer = new GraphicalPlayer(ownId, playerNames, scoreBean, trickBean, handBean, blockingQueue);
        runLater(() -> { 
            graphicalPlayer.createStage().show(); 
        });
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        runLater(() -> {
            handBean.setPlayableCards(state.trick().playableCards(hand));
        });
        try {
            return blockingQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Called each time the hand of a player changes.
     * It can be called at the beginning of the turn (where new cards are given)
     * Or each time the player plays a cards.
     * @param newHand
     */
    public void updateHand(CardSet newHand) {
        runLater(() -> {
            handBean.setHand(newHand);
        });
    }
    
    /**
     * Called each time a new  trump is set.
     * @param trump
     */
    public void setTrump(Card.Color trump) {
        runLater(() -> {
            trickBean.setTrump(trump);
        });
    }
    
    /**
     * Called each time the trick has changed
     * i.e. each time a card is played
     * or each time a trick is collected
     * @param newTrick
     */
    public void updateTrick(Trick newTrick) {
        runLater(() -> {
            trickBean.setTrick(newTrick);
        });
    }
    
    /**
     * Called each time the score is updated
     * i.e. each time a trick is collected
     * @param score
     */
    public void updateScore(Score score) {
        runLater(() -> {
            scoreBean.setScore(score);
        });
    }
    
    /**
     * Called as soon a team has more than 1000 points
     * @param winningTeam
     */
    public void setWinningTeam(TeamId winningTeam) {
        runLater(() -> {
            scoreBean.setWinningTeam(winningTeam);
        });
    }

}
