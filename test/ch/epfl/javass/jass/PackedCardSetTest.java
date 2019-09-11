package ch.epfl.javass.jass;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.SplittableRandom;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

public class PackedCardSetTest {
    @Test
    void isValidWorksForSomeValid() {
        SplittableRandom rng = newRandom();
        
        assertTrue(PackedCardSet.isValid(PackedCardSet.EMPTY));
        assertTrue(PackedCardSet.isValid(PackedCardSet.ALL_CARDS));
        long packedSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            assertTrue(PackedCardSet.isValid(packedSet));
        }
    }
    
    @Test
    void isValidWorksForSomeInvalid() {
        SplittableRandom rng = newRandom();
        
        assertTrue(PackedCardSet.isValid(PackedCardSet.EMPTY));
        assertTrue(PackedCardSet.isValid(PackedCardSet.ALL_CARDS));
        long packedSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int unused = rng.nextInt(9, 16);
            
            int colorIndex = 16*c;
            int unusedIndex = unused;
            
            long pkSetOfCard = 1L << colorIndex+unusedIndex;            
            packedSet |= pkSetOfCard;
            assertFalse(PackedCardSet.isValid(packedSet));
        }
    }
    
    // code used to get the trumps above list
    @Test
    void trumpAboveWorksForAll() {
        ArrayList<ArrayList<Long>> trumpsAbove = new ArrayList<ArrayList<Long>>();
        for(int cColor=0; cColor<4;++cColor) {
            ArrayList<Long> trumpAboveColor = new ArrayList<Long>();
            for(int rRank=0; rRank<9; ++rRank) {
                int ref = (cColor << 4) | rRank;
                long packedSet = PackedCardSet.EMPTY;
                for (int c = 0; c < 4; ++c) {
                    for (int r = 0; r < 9; ++r) {
                        int packed = (c<<4) | r;
                        if(PackedCard.isBetter(PackedCard.color(ref), packed, ref)) {
                            packedSet = PackedCardSet.add(packedSet, packed);
//                            System.out.println(Long.toBinaryString(packed));
//                            System.out.println(packed);
                        } else {
                            
                        }
                    }
                }
                
                trumpAboveColor.add(packedSet);
//                System.out.println(Long.toBinaryString(packedSet));
//                System.out.print(packedSet);
//                System.out.print("L,");
            }
            trumpsAbove.add(trumpAboveColor);
        }
//        System.out.println(trumpsAbove.toString());
        
        for (int c = 0; c < 4; ++c) {
            for (int r = 0; r < 9; ++r) {
                int packed = (c<<4)|r;
                assertTrue(trumpsAbove.get(c).get(r) == PackedCardSet.trumpAbove(packed));        
            }
        }
    }
    
    @Test
    void singletonIsCorrectForSome() {
        for (int c = 0; c < 4; ++c) {
            for (int r = 0; r < 9; ++r) {
                int colorIndex = 16*c;
                int rankIndex = r;
                
                int packed = (c<<4) | r;
                
                long pkSetOfCard = 1L << colorIndex+rankIndex;   
                
                assertTrue(pkSetOfCard == PackedCardSet.singleton(packed));
            }
        }
    }
    
    @Test
    void isEmptyIsCorrectForOne() {
        assertTrue(PackedCardSet.EMPTY == 0);
        assertTrue(PackedCardSet.isEmpty(PackedCardSet.EMPTY));
        assertFalse(PackedCardSet.isEmpty(1));
    }
    
    @Test
    void sizeIsCorrectForSome() {
        SplittableRandom rng = newRandom();
        int size = 0;
        long packedSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long oldPackedSet = packedSet;
            
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            
            if(packedSet != oldPackedSet)
                size++;
            
            assertTrue(size == PackedCardSet.size(packedSet));
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                size=0;
            }
            
       }
    }
    
    @Test
    void getIsCorrect() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        TreeMap<Integer,Integer> sortedMap = new TreeMap<Integer,Integer>();
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
                                  
            int colorIndex = 16*c;
            int rankIndex1 = r;
                        
            long pkSetOfCard1 = 1L << colorIndex+rankIndex1;    
            int bitPosition = Long.numberOfTrailingZeros(pkSetOfCard1);
            packedSet |= pkSetOfCard1;
            
            if(!sortedMap.containsKey(bitPosition))
                sortedMap.put(bitPosition, (c<<4)|r);
//            System.out.println(PackedCardSet.toString(packedSet));
//            System.out.println(PackedCardSet.toString(otherSet));
//            System.out.println(PackedCardSet.toString(notInOtherSet));
            
//            System.out.println(sortedMap.toString());
                                                 
            for(int j=0; j<sortedMap.size(); j++) {
                Integer obj = sortedMap.get(sortedMap.keySet().toArray()[j]);
                int pkCard = PackedCardSet.get(packedSet, j);
//                System.out.println(pkCard);
                assertTrue(pkCard == obj);
            }
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                sortedMap.clear();
            }
        }
    }
    
    @Test
    void addIsCorrectForSome() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        long mirrorSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            mirrorSet = PackedCardSet.add(mirrorSet, (c<<4)|r);
                        
            assertTrue(packedSet == mirrorSet);
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                mirrorSet = PackedCardSet.EMPTY;
            }
            
        }
    }
    
    @Test
    void removeIsCorrectForSome() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        long mirrorSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            int packed = (c<<4)|r;
            
//            System.out.println(PackedCard.toString(packed));
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long oldSet = packedSet;
            
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            mirrorSet |= pkSetOfCard;
            
            if(oldSet != packedSet)
                mirrorSet = PackedCardSet.remove(mirrorSet, packed);
            
//            System.out.println(Long.toBinaryString(mirrorSet));
//            System.out.println(PackedCardSet.toString(packedSet));
//            System.out.println(PackedCardSet.toString(mirrorSet));
//            System.out.println(PackedCardSet.toString(oldSet));
                        
            assertTrue(oldSet == mirrorSet);
            
            mirrorSet |= pkSetOfCard;
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                mirrorSet = PackedCardSet.EMPTY;
            }
            
        }
    }
    
    @Test
    void containsIsCorrect() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            int packed = (c<<4)|r;
                        
            int colorIndex = 16*c;
            int rankIndex = r;
                        
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
                        
            assertTrue(PackedCardSet.contains(packedSet, packed));
            
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
            }
            
        }
    }
    
    @Test
    void complementIsCorrect() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        long complementSet = PackedCardSet.ALL_CARDS;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
                                    
            int colorIndex = 16*c;
            int rankIndex = r;
                        
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            
            long pkNotSetOfCard = -1L - pkSetOfCard;
            complementSet &= pkNotSetOfCard;
                        
            assertTrue(PackedCardSet.complement(packedSet) == complementSet);
            
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                complementSet = PackedCardSet.ALL_CARDS;
            }
        }
    }
    
    @Test
    void unionIsCorrect() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        long otherSet = PackedCardSet.EMPTY;
        long unionSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c1 = rng.nextInt(4);
            int r1 = rng.nextInt(9);
            
            int c2 = rng.nextInt(4);
            int r2 = rng.nextInt(9);
                                    
            int colorIndex1 = 16*c1;
            int rankIndex1 = r1;
            
            int colorIndex2 = 16*c2;
            int rankIndex2 = r2;
                        
            long pkSetOfCard1 = 1L << colorIndex1+rankIndex1;            
            packedSet |= pkSetOfCard1;
            
            long pkSetOfCard2 = 1L << colorIndex2+rankIndex2;            
            otherSet |= pkSetOfCard2;
            
            unionSet |= pkSetOfCard1 | pkSetOfCard2;
                                    
            assertTrue(PackedCardSet.union(packedSet,otherSet) == unionSet);
            
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                otherSet = PackedCardSet.EMPTY;
                unionSet = PackedCardSet.EMPTY;
            }
        }
    }
    
    @Test
    void intersectionIsCorrect() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        long otherSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c1 = rng.nextInt(4);
            int r1 = rng.nextInt(9);
            
            int c2 = rng.nextInt(4);
            int r2 = rng.nextInt(9);
                                    
            int colorIndex1 = 16*c1;
            int rankIndex1 = r1;
            
            int colorIndex2 = 16*c2;
            int rankIndex2 = r2;
                        
            long pkSetOfCard1 = 1L << colorIndex1+rankIndex1;            
            packedSet |= pkSetOfCard1;
            
            long pkSetOfCard2 = 1L << colorIndex2+rankIndex2;            
            otherSet |= pkSetOfCard2;
                                                
            assertTrue(PackedCardSet.intersection(packedSet,otherSet) == (packedSet&otherSet));
            
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                otherSet = PackedCardSet.EMPTY;
            }
        }
    }
    
    @Test
    void differenceIsCorrect() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        long otherSet = PackedCardSet.EMPTY;
        long notInOtherSet = PackedCardSet.ALL_CARDS;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c1 = rng.nextInt(4);
            int r1 = rng.nextInt(9);
            
            int c2 = rng.nextInt(4);
            int r2 = rng.nextInt(9);
                                    
            int colorIndex1 = 16*c1;
            int rankIndex1 = r1;
            
            int colorIndex2 = 16*c2;
            int rankIndex2 = r2;
                        
            long pkSetOfCard1 = 1L << colorIndex1+rankIndex1;            
            packedSet |= pkSetOfCard1;
            
            long pkSetOfCard2 = 1L << colorIndex2+rankIndex2;            
            otherSet |= pkSetOfCard2;
            
            long pkNotSetOfCard2 = -1L - pkSetOfCard2;
            notInOtherSet &= pkNotSetOfCard2;
            
//            System.out.println(PackedCardSet.toString(packedSet));
//            System.out.println(PackedCardSet.toString(otherSet));
//            System.out.println(PackedCardSet.toString(notInOtherSet));
                                                            
            assertTrue(PackedCardSet.difference(packedSet,otherSet) == (packedSet&notInOtherSet));
            
            
            int shouldEmptySet = rng.nextInt(20);
            if(shouldEmptySet == 0) {
                packedSet = PackedCardSet.EMPTY;
                otherSet = PackedCardSet.EMPTY;
                notInOtherSet = PackedCardSet.ALL_CARDS;
            }
        }
    }
    
    //used to get the color subset masks
    @Test
    void subsetOfCOlorWorksForSome() {
        SplittableRandom rng = newRandom();
        long packedSet = PackedCardSet.EMPTY;
        for (int i = 0; i < RANDOM_ITERATIONS; ++i) {
            int c = rng.nextInt(4);
            int r = rng.nextInt(9);
            
            int colorIndex = 16*c;
            int rankIndex = r;
            
            long pkSetOfCard = 1L << colorIndex+rankIndex;            
            packedSet |= pkSetOfCard;
            
            for(int cColor=0; cColor<4;++cColor) {
                long subset = PackedCardSet.EMPTY;
                for(int rRank=0; rRank<9; ++rRank) {
                    int ref = (cColor << 4) | rRank;
                    subset = PackedCardSet.add(subset, ref);
                }
                long expectedSet = PackedCardSet.subsetOfColor(packedSet, Card.Color.values()[cColor]);
                assertTrue((subset&packedSet) == expectedSet);
//                System.out.println(Long.toBinaryString(subset));
//                System.out.println(subset);
            }
            
        }
    }
    
    @Test
    void allCardsIsCorrect() {
        long subset = PackedCardSet.EMPTY;
        for(int cColor=0; cColor<4;++cColor) {
            for(int rRank=0; rRank<9; ++rRank) {
                int ref = (cColor << 4) | rRank;
                subset = PackedCardSet.add(subset, ref);
            }
        }
        assertTrue(subset == PackedCardSet.ALL_CARDS);
//        System.out.println(Long.toBinaryString(subset));
//        System.out.println(subset);
    }
}
