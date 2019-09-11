package ch.epfl.javass.jass;

import java.util.Map;

public final class PrintingPlayer implements Player {
    private final Player underlyingPlayer;

    public PrintingPlayer(Player underlyingPlayer) {
      this.underlyingPlayer = underlyingPlayer;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
      System.out.print("It's my turn to play: ");
      Card c = underlyingPlayer.cardToPlay(state, hand);
      System.out.println(c);
      return c;
    }

    /**
     * A method called at the beginning of each game to set the id of the player
     * and tell the player about the names assigned to everyone (playerNames)
     * @param ownId
     * @param playerNames
     */
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        System.out.println("These are the players:");
        for(PlayerId id: PlayerId.ALL) {
            if(id.equals(ownId)) {
                System.out.println("  " + playerNames.get(id).toString() + " (me)");
            } else {
                System.out.println("  " + playerNames.get(id).toString());
            }
        }
    }
    
    /**
     * Called each time the hand of a player changes.
     * It can be called at the beginning of the turn (where new cards are given)
     * Or each time the player plays a cards.
     * @param newHand
     */
    public void updateHand(CardSet newHand) {
        System.out.println("New hand: " + newHand.toString());
    }
    
    /**
     * Called each time a new  trump is set.
     * @param trump
     */
    public void setTrump(Card.Color trump) {
        System.out.println("new trump: " + trump.toString());
    }
    
    /**
     * Called each time the trick has changed
     * i.e. each time a card is played
     * or each time a trick is collected
     * @param newTrick
     */
    public void updateTrick(Trick newTrick) {
        System.out.println(newTrick.toString());
    }
    
    /**
     * Called each time the score is updated
     * i.e. each time a trick is collected
     * @param score
     */
    public void updateScore(Score score) {
        System.out.println("New score: " + score.toString());
    }
    
    /**
     * Called as soon a team has more than 1000 points
     * @param winningTeam
     */
    public void setWinningTeam(TeamId winningTeam) {
        System.out.println(winningTeam.toString());
    }
  }