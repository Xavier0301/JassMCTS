package ch.epfl.javass.jass;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Used to represent a Team. In Jass, there are two teams.
 * @author xavier
 *
 */
public enum TeamId {
    TEAM_1,
    TEAM_2;
    
 // this returns an non modifiable version of values()
    public static List<TeamId> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    // how many values there are
    public static final int COUNT = values().length;
    
    /**
     * The purpose of this method is to easily get the opposing team of a given
     * team id.
     * @return
     */
    public TeamId other() {
        if(this == TEAM_1) 
            return TEAM_2;
        return TEAM_1;
    }
}
