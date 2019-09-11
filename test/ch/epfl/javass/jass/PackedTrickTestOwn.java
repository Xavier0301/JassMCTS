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

public class PackedTrickTestOwn {
    @Test
    void isValidWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
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
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            assertTrue(PackedTrick.isValid(trick));
        }
    }
    
    @Test
    void isValidFalseForInvalidIndex() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
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
            int index = rng.nextInt(9, 16);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            assertFalse(PackedTrick.isValid(trick));
        }
    }
    
    int cardForValidity(SplittableRandom rng, boolean isValid, int index) {
        index = 3-index;
//        System.out.print(isValid);
        if(isValid) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            return (((c<<4) | r) << index*6 );
        } 
        return (PackedCard.INVALID << index*6);
    }
    
    int invalidSequenceOfCards(SplittableRandom rng) {
        int sequence = 0;
        
        boolean firstIsValid = rng.nextBoolean();
        sequence |= cardForValidity(rng, firstIsValid, 0);
        
        boolean secondIsValid = rng.nextBoolean();
        sequence |= cardForValidity(rng, secondIsValid, 1);
        
        if(!firstIsValid & !secondIsValid) {
            sequence |= cardForValidity(rng, true, 2);
            sequence |= cardForValidity(rng, false, 3);
            return sequence;
        }
        
        boolean thirdIsValid = rng.nextBoolean();
        sequence |= cardForValidity(rng, thirdIsValid, 2);
        
        if(firstIsValid & secondIsValid & thirdIsValid) {
            sequence |= cardForValidity(rng, false, 3);
            return sequence;
        }
        
        if(!firstIsValid & secondIsValid & thirdIsValid) {
            sequence |= cardForValidity(rng, false, 3);
            return sequence;
        }
        
        boolean fourthIsValid = rng.nextBoolean();
        sequence |= cardForValidity(rng, fourthIsValid, 3);
        
        return sequence;
    }
    
    @Test
    void isValidFalseForInvalidSequenceOfCards() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
            int invalidSeq = invalidSequenceOfCards(rng);
            
            trick |= invalidSeq;            
            
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            assertFalse(PackedTrick.isValid(trick));
        }
    }
    
    @Test
    void firstEmptyWorks() {
        for(int p=0; p<=3; p++) {
            for(int t=0; t<=3; t++) {
                int trick = (t << 30) | (p<<28);
                
                for(int j=0; j<=3; j++) {
                    trick |= (PackedCard.INVALID << 6*j);
                }
                
                assertEquals(trick, PackedTrick.firstEmpty(Card.Color.ALL.get(t), PlayerId.ALL.get(p)));
            }
        }
    }
    
    @Test
    void isEmptyWorks() {
        for(int p=0; p<=3; p++) {
            for(int t=0; t<=3; t++) {
                for(int i=0; i<=8; i++) {
                    int trick = (t << 30) | (p<<28) | (i<<24);
                    
                    for(int j=0; j<=3; j++) {
                        trick |= (PackedCard.INVALID << 6*j);
                    }
                    
                    assertTrue(PackedTrick.isEmpty(trick));
                }
            }
        }
    }
    
    @Test
    void isFullWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
            for(int j=0; j<=3; j++) {
                int c = rng.nextInt(4);
                int r = rng.nextInt(9);
                int pkCard = ((c<<4) | r);
                
                trick |= (pkCard << 6*j);
            }
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            assertTrue(PackedTrick.isFull(trick));
        }
    }
    
    @Test
    void sizeWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
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
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            assertEquals(PackedTrick.size(trick), 4-nbrInvalidCards);
        }
    }
    
    
    @Test
    void gettersWork() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
            int nbrInvalidCards = rng.nextInt(5);
            
            int[] cards = new int[4];
            
            for(int j=0; j<=3; j++) {
                if(j < 4-nbrInvalidCards) {
                    int c = rng.nextInt(4);
                    int r = rng.nextInt(9);
                    int pkCard = ((c<<4) | r);
                    
                    cards[j] = pkCard;
                    
                    trick |= (pkCard << 6*j);
                } else {
                    cards[j] = PackedCard.INVALID;
                    
                    trick |= (PackedCard.INVALID << 6*j);
                }
            }
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            assertEquals(PackedTrick.trump(trick).ordinal(), trump);
            assertEquals(PackedTrick.index(trick), index);
            
            for(int j=0; j<=3; j++) {
                assertEquals(PackedTrick.player(trick,j).ordinal(), (fPlayer+j)%4);
            }
            
            for(int j=0; j<=3; j++) {
                assertEquals(PackedTrick.card(trick,j), cards[j]);
            }
            
        }
    }
    
    @Test
    void withAddedCardWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
//            System.out.println("New iteration");
            int trick = 0;
           
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            int copyTrick = trick;
            
            int[] cards = new int[4];
            
            int nbrInvalidCards = rng.nextInt(5);
            for(int j=0; j<=3; j++) {
                if(j < 4-nbrInvalidCards) {
                    int c = rng.nextInt(4);
                    int r = rng.nextInt(9);
                    int pkCard = ((c<<4) | r);
                    
                    trick |= (pkCard << 6*j);
                    cards[j] = pkCard;
                } else {
                    trick |= (PackedCard.INVALID << 6*j);
                    cards[j] = PackedCard.INVALID;
                }
                copyTrick |= (PackedCard.INVALID << 6*j);
            }
            
//            for(int j=0; j<=3; j++) {
//                if(cards[j] == PackedCard.INVALID)
//                    System.out.println("INVALID");
//                else
//                    System.out.println(PackedCard.toString(cards[j]));
//            }
            
            for(int j=0; j<=3; j++) {
                if(cards[j] != PackedCard.INVALID)
                    copyTrick = PackedTrick.withAddedCard(copyTrick, cards[j]);
            }
            
            assertEquals(copyTrick, trick);
        }
    }
    
    @Test
    void baseColorWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
            int nbrInvalidCards = rng.nextInt(4);
            
            int baseColor = 0;
            
            for(int j=0; j<=3; j++) {
                if(j < 4-nbrInvalidCards) {
          
                    int c = rng.nextInt(4);
                    int r = rng.nextInt(9);
                    int pkCard = ((c<<4) | r);
                    
                    if(j == 0)
                        baseColor = c;
                    
                    trick |= (pkCard << 6*j);
                } else {
                    trick |= (PackedCard.INVALID << 6*j);
                }
            }
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            assertEquals(PackedTrick.baseColor(trick).ordinal(), baseColor);
        }
    }
    
    @Test
    void pointsWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            int points=0;
            
            int nbrInvalidCards = rng.nextInt(5);
            for(int j=0; j<=3; j++) {
                if(j < 4-nbrInvalidCards) {
          
                    int c = rng.nextInt(4);
                    int r = rng.nextInt(9);
                    int pkCard = ((c<<4) | r);
                    
                    points += PackedCard.points(Card.Color.ALL.get(trump), pkCard);
                                        
                    trick |= (pkCard << 6*j);
                } else {
                    trick |= (PackedCard.INVALID << 6*j);
                }
            }
            
            if(index == 8)
                points +=5;
            
            assertEquals(PackedTrick.points(trick), points);
        }
    }
    
    @Test
    void winningPlayerWorks() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {  
            int trick = 0;
            
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            int nbrInvalidCards = rng.nextInt(4);
            int currentBestPlayer = fPlayer;
            int currentBestCard = 0;
            for(int j=0; j<=3; j++) {
                if(j < 4-nbrInvalidCards) {
                    int c = rng.nextInt(4);
                    int r = rng.nextInt(9);
                    int pkCard = ((c<<4) | r);
                    
                    if(j==0)
                        currentBestCard = pkCard;
                    else if(PackedCard.isBetter(Card.Color.ALL.get(trump), pkCard, currentBestCard)) {
                        currentBestCard = pkCard;
                        currentBestPlayer = (fPlayer+j) % 4;
                    }
                    
                    trick |= (pkCard << 6*j);
                } else {
                    trick |= (PackedCard.INVALID << 6*j);
                }
            }
            
            assertEquals(PackedTrick.winningPlayer(trick).ordinal(), currentBestPlayer);
        }
    }
    
    @Test
    void playableCardsWork() {
        SplittableRandom rng = newRandom();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) { 
            
            
            
            int trick = 0;
            
            int index = rng.nextInt(9);
            trick |= (index << 24);
            
            int fPlayer = rng.nextInt(4);
            trick |= (fPlayer << 28);
            
            int trump = rng.nextInt(4);
            trick |= (trump << 30);
            
            int baseColor = 0;
            
            int bestTrumpCardYet = -1;
            
            int nbrInvalidCards = rng.nextInt(1,5);
            for(int j=0; j<=3; j++) {
                if(j < 4-nbrInvalidCards) {
                    int c = rng.nextInt(4);
                    int r = rng.nextInt(9);
                    int pkCard = ((c<<4) | r);
                    
                    if(j==0) 
                        baseColor = c;
                    
                    if(c == trump) {
                        if(bestTrumpCardYet == -1) 
                            bestTrumpCardYet = pkCard;
                        else if(PackedCard.isBetter(Card.Color.ALL.get(trump), pkCard, bestTrumpCardYet))
                            bestTrumpCardYet = pkCard;
                    }
                    
                    trick |= (pkCard << 6*j);
                } else {
                    trick |= (PackedCard.INVALID << 6*j);
                }
            }
            
            int bestTrumpCardInHand = -1;
            
            long hand=PackedCardSet.EMPTY;
            
            int numberOfCardsInHand = rng.nextInt(1,36);
            for(int j=0; j<numberOfCardsInHand; j++) {
                int c = rng.nextInt(4);
                int r = rng.nextInt(9);
                int pkCard = ((c<<4) | r);
                PackedCardSet.add(hand, pkCard);
                
                if(c==trump) {
                    if(bestTrumpCardInHand == -1)
                        bestTrumpCardInHand = pkCard;
                    else if(PackedCard.isBetter(Card.Color.ALL.get(trump), pkCard, bestTrumpCardInHand))
                        bestTrumpCardInHand = pkCard;
                }
            }
            
            if(PackedCardSet.isEmpty(PackedCardSet.subsetOfColor(hand, Card.Color.ALL.get(baseColor)))) {
                assertEquals(hand, PackedTrick.playableCards(trick, hand));
            } else if(trump == baseColor) {
                long trumpCardsInHand = PackedCardSet.subsetOfColor(hand, Card.Color.ALL.get(baseColor));
                int jackTrump = (trump<<4) | Card.Rank.JACK.ordinal();
                long jackTrumpSet = PackedCardSet.singleton(jackTrump);
                if(jackTrumpSet == trumpCardsInHand) {
                    assertEquals(hand, PackedTrick.playableCards(trick, hand));
                }
            } else if(bestTrumpCardInHand != -1 & PackedCard.isBetter(Card.Color.ALL.get(trump), bestTrumpCardYet, bestTrumpCardInHand)) {
                long baseColorSet = PackedCardSet.subsetOfColor(hand, Card.Color.ALL.get(baseColor));
                long trumpColorSet = PackedCardSet.subsetOfColor(hand, Card.Color.ALL.get(trump));
                long playable = PackedCardSet.union(baseColorSet, trumpColorSet);
                assertEquals(playable, PackedTrick.playableCards(trick, hand));
            } else {
                long baseColorSet = PackedCardSet.subsetOfColor(hand, Card.Color.ALL.get(baseColor));
                long trumpColorSet = PackedCardSet.subsetOfColor(hand, Card.Color.ALL.get(trump));
                long trumpAboveMask = 0;
                if(bestTrumpCardYet == -1) 
                    trumpAboveMask = trumpColorSet;
                else 
                    trumpAboveMask = PackedCardSet.trumpAbove(bestTrumpCardYet);
                long trumpAboveSet = PackedCardSet.intersection(trumpAboveMask, trumpColorSet);
                long playable = 0;
                if(trump == baseColor)
                    playable = trumpAboveSet;
                else
                    playable = PackedCardSet.union(baseColorSet, trumpAboveSet);
                assertEquals(playable, PackedTrick.playableCards(trick, hand));
            }
                
            
            assertTrue(PackedTrick.isValid(trick));
        }
    }
}
