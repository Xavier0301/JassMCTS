package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public final class PlayerIdTest {
    private static PlayerId[] getAllPlayerIds() {
        return new PlayerId[] {
                PlayerId.PLAYER_1,
                PlayerId.PLAYER_2,
                PlayerId.PLAYER_3,
                PlayerId.PLAYER_4,
        };
    }
    
    @Test
    void playerIdsAreInTheRightOrder() {
        assertArrayEquals(getAllPlayerIds(), PlayerId.values());
    }
    
    @Test
    void playerIdCountIsCorrect() {
        assertEquals(PlayerId.COUNT, 4);
    }
    
    @Test
    void playerIdTeamIsCorrect() {
        assertEquals(PlayerId.PLAYER_1.team(), TeamId.TEAM_1);
        assertEquals(PlayerId.PLAYER_2.team(), TeamId.TEAM_2);
        assertEquals(PlayerId.PLAYER_3.team(), TeamId.TEAM_1);
        assertEquals(PlayerId.PLAYER_4.team(), TeamId.TEAM_2);
    }
}
