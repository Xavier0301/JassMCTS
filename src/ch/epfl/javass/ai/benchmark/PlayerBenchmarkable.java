package ch.epfl.javass.ai.benchmark;

/**
 * Everything that is needed to measure the performances of a player.
 * @author xavier
 *
 */
public interface PlayerBenchmarkable {
    /**
     * @return the number of wins in total.
     */
    public int getNumberOfWins();
    
    /**
     * @return the total elapsed time spent on cardToPlay
     */
    public long getTotalExecutionTime();
    
    /**
     * @return the number of times cardToPlay has been executed on a player
     */
    public long getTotalNumberOfExecutions();
}
