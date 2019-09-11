package ch.epfl.javass.jass;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public final class TeamIdTest {
    private static TeamId[] getAllTeamIds() {
        return new TeamId[] {
                TeamId.TEAM_1,
                TeamId.TEAM_2
        };
    }
    
    @Test
    void teamIdsAreInTheRightOrder() {
        assertArrayEquals(getAllTeamIds(), TeamId.values());
    }
    
    @Test
    void teamIdCountIsCorrect() {
        assertEquals(TeamId.COUNT, 2);
    }
    
    @Test
    void teamIdOtherIsCorrect() {
        assertEquals(TeamId.TEAM_2.other(), TeamId.TEAM_1);
        assertEquals(TeamId.TEAM_1.other(), TeamId.TEAM_2);
    }
}
