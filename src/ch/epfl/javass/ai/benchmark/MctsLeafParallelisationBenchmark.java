package ch.epfl.javass.ai.benchmark;

import java.util.concurrent.TimeUnit;

import ch.epfl.javass.ai.MctsLeafParallelisationPlayer;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TurnState;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Card.Rank;

public class MctsLeafParallelisationBenchmark {
    
    private static final int SAMPLE_SIZE = 100;
    private static final Integer THREADS = null;
    private static final int RNG_SEED = 0;
    private static final int ITERATIONS = 10_000;

    public static void main(String[] args) {        
        long averageExecutionTime = 0;
                
        for(int i=1; i<=(SAMPLE_SIZE+1); i++) {
            /**
             * We use System.nanoTime() instead of System.currentTimeMillis() because the latter is based of a clock
             * that is often corrected for inaccuracies, which means that an measure elapsed time might be different
             * than the actual elapsed time. System.nanoTime() is made for measuring elapsed time thus it does not
             * suffer this weakness.
             */
            long beginTime = System.nanoTime();
            MctsLeafParallelisationPlayer p = new MctsLeafParallelisationPlayer(PlayerId.PLAYER_2, RNG_SEED, ITERATIONS, THREADS);
            TurnState state = TurnState.initial(Color.SPADE, Score.INITIAL, PlayerId.PLAYER_1)
                    .withNewCardPlayed(Card.of(Color.SPADE, Rank.JACK));
            CardSet hand = CardSet.EMPTY
                    .add(Card.of(Color.SPADE, Rank.EIGHT))
                    .add(Card.of(Color.SPADE, Rank.NINE))
                    .add(Card.of(Color.SPADE, Rank.TEN))
                    .add(Card.of(Color.HEART, Rank.SIX))
                    .add(Card.of(Color.HEART, Rank.SEVEN))
                    .add(Card.of(Color.HEART, Rank.EIGHT))
                    .add(Card.of(Color.HEART, Rank.NINE))
                    .add(Card.of(Color.HEART, Rank.TEN))
                    .add(Card.of(Color.HEART, Rank.JACK));
            
            Card c = p.cardToPlay(state, hand);
            
            long executionTime = System.nanoTime() - beginTime;
            
            /**
             * For some reason, the first time we measure the execution time, the given time
             * is much higher than the subsequent ones. The same happens for a regular MctsPlayer,
             * which means that it's nothing that has to do with ExecutorService an a potential
             * reduction of the computational cost overhead once we has already instantiated an
             * ExecutorService.
             * To remedy this, we ignore the first measure.
             */
            if(i == 2)
                averageExecutionTime = executionTime;
            else if(i != 1)
                averageExecutionTime = (averageExecutionTime*(i-1)+executionTime)/i;
                        
            System.out.println(executionTime);
        }

        System.out.println(TimeUnit.NANOSECONDS.toMillis(averageExecutionTime));
    }

}
