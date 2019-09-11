package ch.epfl.javass.ai.benchmark;

import java.util.Map;
import java.util.SplittableRandom;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.TurnState;

public final class RandomPlayer implements Player, PlayerBenchmarkable {
    SplittableRandom rng = new SplittableRandom();
    
    private int wins = 0;
    private PlayerId ownId = null;
    
    private long totalExecutionTime = 0;
    private long totalExecutions = 0;

    public int getNumberOfWins() {
        return wins;
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public long getTotalNumberOfExecutions() {
        return totalExecutions;
    }
    
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long beginTime = System.nanoTime();
        
        CardSet playable = state.trick().playableCards(hand);
        int index = rng.nextInt(playable.size());
        Card toPlay = playable.get(index);
        
        totalExecutionTime += (System.nanoTime()-beginTime);
        totalExecutions++;
        
        return toPlay;
    }
    
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.ownId = ownId;
    }
    
    public void setWinningTeam(TeamId winningTeam) {
        if(ownId.team().equals(winningTeam))
            wins++;
    }
}
