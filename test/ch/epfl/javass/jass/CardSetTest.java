package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertThrows;


import java.util.ArrayList;
import java.util.SplittableRandom;

import org.junit.jupiter.api.Test;

public class CardSetTest {
    @Test
    void ofPackedAndPackedWork() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            
            CardSet cardSet = CardSet.ofPacked(packedSet);
            
            assertTrue(cardSet.packed() == packedSet);
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
            }
            
        }
    }
    
    @Test
    void ofListAndPackedWork() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            cards.add(Card.ofPacked((c<<4)|r));
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            
            CardSet cardSet = CardSet.of(cards);
            
            assertTrue(cardSet.packed() == packedSet);
                        
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                cards.clear();
            }
        }
    }
    
    @Test
    void throwsIllegalArgumentForInvalidPacked() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9,15);
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long pkSetOfInvalidCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfInvalidCard;
            
            final long mf = packedSet;
            
            assertThrows(IllegalArgumentException.class, () -> {
                CardSet.ofPacked(mf);
            });
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
            }
        }
    }

}
