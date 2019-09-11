package ch.epfl.javass.ai.benchmark;

import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;

import ch.epfl.javass.ai.ImprovedMctsPlayer;
import ch.epfl.javass.ai.MctsTunedPlayer;
import ch.epfl.javass.ai.ShallowMinimaxPlayer;
import ch.epfl.javass.ai.SimplePlayer;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;

/**
 * Used to evaluate the performance of players that implement PlayerBenchmarkable.
 * @author xavier
 *
 */
public class PlayerBenchmark {
    private static final int SAMPLE_SIZE = 100;
    
    public static void main(String[] args) {
        // WE HAVE TO MAKE SURE THAT EACH PLAYER ADDED TO THE GAME
        // IMPLEMENT DefaultPolicyBenchmarkable
        Map<PlayerId, Player> players = new EnumMap<PlayerId, Player>(PlayerId.class);
        Map<PlayerId, String> playerNames = new EnumMap<PlayerId, String>(PlayerId.class);
        
        SplittableRandom rng = new SplittableRandom();
        
        /*
         * SimplePlayer
         * ShallowMinimaxPlayer
         * RandomPlayer
         * MctsPlayer(id, 0, 10_000)
         * MctsTunedPlayer(id, 0, 10_000)
         * ImprovedMctsPlayer(id, 10_000, null)
         */
        for(PlayerId id: PlayerId.ALL) {
            if(id.ordinal() % 2 == 0) 
                players.put(id, new ImprovedMctsPlayer(id, 10_000, null));
            else 
                players.put(id, new RandomPlayer());
        }
        
        for(PlayerId id: PlayerId.ALL) 
            playerNames.put(id, id.name());
        
        for(int i=0; i<SAMPLE_SIZE; i++) {
            JassGame game = new JassGame(rng.nextLong(), players, playerNames);
            while(!game.isGameOver()) {
                game.advanceToEndOfNextTrick();
            }
        } 
        
        PlayerBenchmarkable player1 = (PlayerBenchmarkable) players.get(PlayerId.PLAYER_1);
        PlayerBenchmarkable player3 = (PlayerBenchmarkable) players.get(PlayerId.PLAYER_3);
        
        PlayerBenchmarkable player2 = (PlayerBenchmarkable) players.get(PlayerId.PLAYER_2);
        PlayerBenchmarkable player4 = (PlayerBenchmarkable) players.get(PlayerId.PLAYER_4);

        int teamOneWins = player1.getNumberOfWins();
        int teamTwoWins = ((RandomPlayer) players.get(PlayerId.PLAYER_2)).getNumberOfWins();
        
        System.out.println(teamOneWins);
        System.out.println(teamTwoWins);
        
        System.out.println();
        
        long totalExecTime1 = player1.getTotalExecutionTime() + player3.getTotalExecutionTime();
        long totalExecs1 = player1.getTotalNumberOfExecutions() + player3.getTotalNumberOfExecutions();
        
        System.out.println(totalExecTime1/totalExecs1);
        
        long totalExecTime2 = player2.getTotalExecutionTime() + player4.getTotalExecutionTime();
        long totalExecs2 = player2.getTotalNumberOfExecutions() + player4.getTotalNumberOfExecutions();
        
        System.out.println();
        System.out.println(totalExecTime2/totalExecs2);
        
    }
}
