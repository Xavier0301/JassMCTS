package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public final class ScoreTest {
    @Test
    void ofWorks() {
        for(int numberOfTricks1=0; numberOfTricks1 <= 9; numberOfTricks1++) {
            for(int turnPoints1=0; turnPoints1<=257; turnPoints1++) {
                for(int gamePoints1=0; gamePoints1<=2000; gamePoints1++) {
                    long packed = PackedScore.pack(numberOfTricks1, turnPoints1, gamePoints1, numberOfTricks1, turnPoints1, gamePoints1);
                    
                    Score score = Score.ofPacked(packed);
                    assertEquals(packed, score.packed());
                }
            }
        }
    }
    
    static boolean componentsMatch(Score s, int trick, int turn, int game, TeamId team) {
        int trickP = s.turnTricks(team);
        int turnP = s.turnPoints(team);
        int gameP = s.gamePoints(team);
        int totalP = s.totalPoints(team);
        
        return (trickP == trick) & (turnP == turn) & (gameP == game) & ((turnP+gameP) == totalP);
    }
    
    @Test
    void getterWorks() {
        for(int numberOfTricks1=0; numberOfTricks1 <= 9; numberOfTricks1++) {
            for(int turnPoints1=0; turnPoints1<=257; turnPoints1++) {
                for(int gamePoints1=0; gamePoints1<=2000; gamePoints1++) {
                    long packed = PackedScore.pack(numberOfTricks1, turnPoints1, gamePoints1, numberOfTricks1, turnPoints1, gamePoints1);
                    Score score = Score.ofPacked(packed);
                    
                    assertTrue(componentsMatch(score, numberOfTricks1, turnPoints1, gamePoints1, TeamId.TEAM_1));
                    assertTrue(componentsMatch(score, numberOfTricks1, turnPoints1, gamePoints1, TeamId.TEAM_2));
                }
            }
        }
    }
    
    void equalsIsCorrectForSomeCases() {
        for(int numberOfTricks1=3; numberOfTricks1 <= 5; numberOfTricks1++) {
            for(int turnPoints1=120; turnPoints1<=140; turnPoints1++) {
                for(int gamePoints1=1; gamePoints1<=10; gamePoints1++) {
                    long packed1 = PackedScore.pack(numberOfTricks1, turnPoints1, gamePoints1, numberOfTricks1, turnPoints1, gamePoints1);
                    Score score1 = Score.ofPacked(packed1);
                    for(int numberOfTricks2=4; numberOfTricks2 <= 6; numberOfTricks2++) {
                        for(int turnPoints2=130; turnPoints2<=150; turnPoints2++) {
                            for(int gamePoints2=5; gamePoints2<=20; gamePoints2++) {
                                long packed2 = PackedScore.pack(numberOfTricks2, turnPoints2, gamePoints2, numberOfTricks2, turnPoints2, gamePoints2);
                                Score score2 = Score.ofPacked(packed2);
                                assertTrue((packed1 == packed2) == score1.equals(score2));
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Test
    void withAdditionalTrickDoesNotChangeReceiver() {
        Score s0 = Score.INITIAL;
        Score s1 = s0.withAdditionalTrick(TeamId.TEAM_1, 5);
        assertEquals(PackedScore.INITIAL, s0.packed());

        long pkS1 = s1.packed();
        assertNotEquals(PackedScore.INITIAL, pkS1);

        Score s2 = s1.withAdditionalTrick(TeamId.TEAM_2, 5);
        assertEquals(pkS1, s1.packed());

        long pkS2 = s2.packed();
        assertNotEquals(pkS1, pkS2);
    }

    @Test
    void nextTurnDoesNotChangeReceiver() {
        Score s0 = Score.INITIAL;
        for (int i = 0; i < 9; ++i)
            s0 = s0.withAdditionalTrick(TeamId.TEAM_1, i == 0 ? 21 : 17);

        Score s1 = s0.nextTurn();

        assertNotEquals(s0.packed(), s1.packed());
    }
    
    //testing that all hashes are different will take too much time
    //at least we can assume that all Long hashes are different
}
