package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * In jass, there are four players.
 * @author xavier
 *
 */
public enum PlayerId {
    PLAYER_1,
    PLAYER_2,
    PLAYER_3,
    PLAYER_4;
    
    // this returns an non modifiable version of values()
    public static List<PlayerId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    // how many values there are
    public static final int COUNT = values().length;
    
    /**
     * returns which team the player belongs to.
     * it is TeamId.TEAM_1 for players 1 and 3
     * and TeamId.TEAM_2 for player 2 and 4
     */
    public TeamId team() {
        if(this == PLAYER_1 || this == PLAYER_3)
            return TeamId.TEAM_1;
        return TeamId.TEAM_2;
    }
}
