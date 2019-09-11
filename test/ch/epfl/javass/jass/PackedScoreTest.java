package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PackedScoreTest {
    @Test
    void isValidWorksForSomeValidScores() {
        /*all valid scores takes too long to check
        we check for all (0|teamScore) or (teamScore|0)
        types of score. */
        for(int numberOfTricks1=0; numberOfTricks1 <= 9; numberOfTricks1++) {
            for(int turnPoints1=0; turnPoints1<=257; turnPoints1++) {
                for(int gamePoints1=0; gamePoints1<=2000; gamePoints1++) {
                    long score1 = ((gamePoints1<<13) | (turnPoints1<<4) | (numberOfTricks1));
                    long score2 = (score1 << 32);
                    assertTrue(PackedScore.isValid(score1));
                    assertTrue(PackedScore.isValid(score2));
                }
            }
        }
    }
    
    @Test
    void isValidFailsForSomeUnvalidScores() {
        // only gamepoint invalid
        for(int numberOfTricks1=0; numberOfTricks1 <= 9; numberOfTricks1++) {
            for(int turnPoints1=0; turnPoints1<=257; turnPoints1++) {
                for(int gamePoints1=2001; gamePoints1<=3000; gamePoints1++) {
                    long score1 = ((gamePoints1<<13) | (turnPoints1<<4) | (numberOfTricks1));
                    long score2 = (score1 << 32);
                    assertFalse(PackedScore.isValid(score1));
                    assertFalse(PackedScore.isValid(score2));
                }
            }
        }
        // only turnpoint invalid
        for(int numberOfTricks1=0; numberOfTricks1 <= 9; numberOfTricks1++) {
            for(int turnPoints1=258; turnPoints1<=511; turnPoints1++) {
                for(int gamePoints1=0; gamePoints1<=2000; gamePoints1++) {
                    long score1 = ((gamePoints1<<13) | (turnPoints1<<4) | (numberOfTricks1));
                    long score2 = (score1 << 32);
                    assertFalse(PackedScore.isValid(score1));
                    assertFalse(PackedScore.isValid(score2));
                }
            }
        }
        //only tricks invalid
        for(int numberOfTricks1=10; numberOfTricks1 <= 15; numberOfTricks1++) {
            for(int turnPoints1=0; turnPoints1<=257; turnPoints1++) {
                for(int gamePoints1=0; gamePoints1<=2000; gamePoints1++) {
                    long score1 = ((gamePoints1<<13) | (turnPoints1<<4) | (numberOfTricks1));
                    long score2 = (score1 << 32);
                    assertFalse(PackedScore.isValid(score1));
                    assertFalse(PackedScore.isValid(score2));
                }
            }
        }
        //all 3
        for(int numberOfTricks1=11; numberOfTricks1 <= 15; numberOfTricks1++) {
            for(int turnPoints1=255; turnPoints1<=400; turnPoints1++) {
                for(int gamePoints1=2000; gamePoints1<=2300; gamePoints1++) {
                    long score1 = ((gamePoints1<<13) | (turnPoints1<<4) | (numberOfTricks1));
                    long score2 = (score1 << 32);
                    assertFalse(PackedScore.isValid(score1));
                    assertFalse(PackedScore.isValid(score2));
                }
            }
        }
        // only unused
        for(int numberOfTricks1=0; numberOfTricks1 <= 9; numberOfTricks1++) {
            for(int turnPoints1=0; turnPoints1<=257; turnPoints1++) {
                for(int gamePoints1=0; gamePoints1<=2000; gamePoints1++) {
                    for(int invalidBits=24; invalidBits<=32; invalidBits++) {
                        long invalid = (1L << invalidBits)-1L;
                        
                        long score1 = ((gamePoints1<<13) | (turnPoints1<<4) | (numberOfTricks1));
                        long score2 = (score1 << 32);
                        assertFalse(PackedScore.isValid(score1 | (invalid)));
                        assertFalse(PackedScore.isValid(score2 | (invalid << 32)));
                    }
                }
            }
        }
    }
    
    static boolean componentsMatch(long packed, int trick, int turn, int game, TeamId team) {
        int trickP = PackedScore.turnTricks(packed, team);
        int turnP = PackedScore.turnPoints(packed, team);
        int gameP = PackedScore.gamePoints(packed, team);
        int totalP = PackedScore.totalPoints(packed, team);
        
        return (trickP == trick) & (turnP == turn) & (gameP == game) & ((turnP+gameP) == totalP);
    }
    
    @Test
    void packAndGetterWork() {
        for(int numberOfTricks1=0; numberOfTricks1 <= 9; numberOfTricks1++) {
            for(int turnPoints1=0; turnPoints1<=257; turnPoints1++) {
                for(int gamePoints1=0; gamePoints1<=2000; gamePoints1++) {
                    long packed = PackedScore.pack(numberOfTricks1, turnPoints1, gamePoints1, numberOfTricks1, turnPoints1, gamePoints1);
                    
                    assertTrue(componentsMatch(packed, numberOfTricks1, turnPoints1, gamePoints1, TeamId.TEAM_1));
                    assertTrue(componentsMatch(packed, numberOfTricks1, turnPoints1, gamePoints1, TeamId.TEAM_2));
                }
            }
        }
    }
    
    @Test
    void withAdditionalTrickAndNextTurnWorkOnSome() {
        int[] trick1 = new int[] {
                0,1,1,2,2,3,3,4,4,5
        };
        int[] trick2 = new int[] {
                0,0,1,1,2,2,3,3,4,4
        };
        int[] turn1 = new int[] {
                0,13,13,31,31,49,49,67,67,85
        };
        int[] turn2 = new int[] {
                0,0,18,18,36,36,54,54,72,72
        };
        
        
        long s = PackedScore.INITIAL;
        for (int i = 0; i < Jass.TRICKS_PER_TURN; ++i) {
            assertEquals(PackedScore.turnTricks(s, TeamId.TEAM_1), trick1[i]);
            assertEquals(PackedScore.turnTricks(s, TeamId.TEAM_2), trick2[i]);
            assertEquals(PackedScore.turnPoints(s, TeamId.TEAM_1), turn1[i]);
            assertEquals(PackedScore.turnPoints(s, TeamId.TEAM_2), turn2[i]);
            
          int p = (i == 0 ? 13 : 18);
          TeamId w = (i % 2 == 0 ? TeamId.TEAM_1 : TeamId.TEAM_2);
          s = PackedScore.withAdditionalTrick(s, w, p);
        }
        s = PackedScore.nextTurn(s);
        assertEquals(PackedScore.gamePoints(s, TeamId.TEAM_1), turn1[turn1.length-1]);
        assertEquals(PackedScore.gamePoints(s, TeamId.TEAM_2), turn2[turn2.length-1]);
    }
    
    /* not testing toString as it used for debugging and is
     does not have a predefined behavior */
}
