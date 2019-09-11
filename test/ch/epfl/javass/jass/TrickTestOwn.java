package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertThrows;


import java.util.ArrayList;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

public class TrickTestOwn {
    boolean equalState(Trick trick, int pkTrick) {
        boolean haveEqualsAttributes = (trick.size() == PackedTrick.size(pkTrick));
        haveEqualsAttributes &= (trick.isFull() == PackedTrick.isFull(pkTrick));
        haveEqualsAttributes &= (trick.isEmpty() == PackedTrick.isEmpty(pkTrick));
        haveEqualsAttributes &= (trick.isLast() == PackedTrick.isLast(pkTrick));
        return haveEqualsAttributes;
    }
    
    @Test
    void simulation() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {            
            int trick = 0;
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
                        
            int nbrInvalidCards = rng.nextInt(5);
            for(int j=0; j<=3; j++) {
                if(j < 4-nbrInvalidCards) {
                    int c = rng.nextInt(4);
                    int r = rng.nextInt(9);
                    int pkCard = ((c<<4) | r);
                    
                    trick |= (pkCard << 6*j);
                } else {
                    trick |= (PackedCard.INVALID << 6*j);
                }
            }
            
            Trick t = Trick.ofPacked(trick);
            
            assertTrue(equalState(t, trick));
            assertEquals(t.packed(), trick);
            
            for(int j=0; j<nbrInvalidCards; j++) {
                int c = rng.nextInt(4);
                int r = rng.nextInt(9);
                int pkCard = ((c<<4) | r);
                
                trick = PackedTrick.withAddedCard(trick, pkCard);
                
                t = t.withAddedCard(Card.ofPacked(pkCard));
            }
            
            assertTrue(equalState(t, trick));
            assertEquals(t.packed(), trick);
            
            if(index != 8) {
                trick = PackedTrick.nextEmpty(trick);
                t = t.nextEmpty();
            }
            
            assertTrue(equalState(t, trick));
            assertEquals(t.packed(), trick);
            
        }
    }
}
