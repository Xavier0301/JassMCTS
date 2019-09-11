package ch.epfl.javass.gui;

import javafx.beans.Observable;
import javafx.beans.property.*;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PackedScore;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;

/**
 * A Bean is a class which attributes are all observable.
 * 
 * Specifically, the score bean has 8 attributes,
 * 7 of which you can observe. The reason as to why is that 
 * scoreProperty is not meant to be observed, but rather to be updated,
 * in a way where the other properties of the bean are correctly updated.
 * 
 * In other words, only the other seven properties are used
 * by the graphics class, which the other is not easily interpretable,
 * but as it is used throughout the project (packed representation),
 * we can easily update the 7 properties given the packed score.
 * 
 * @author xavier
 *
 */
public final class ScoreBean {
    private LongProperty scoreProperty;
    
    private IntegerProperty turnPoints1;
    private IntegerProperty gamePoints1;
    private IntegerProperty totalPoints1;
    
    private IntegerProperty turnPoints2;
    private IntegerProperty gamePoints2;
    private IntegerProperty totalPoints2;
    
    private ObjectProperty<TeamId> winningTeam;
    
    /**
     * Initializing the properties of the score bean, for each team
     * Namely,
     * - turn points
     * - game points
     * - total points
     * - winning team
     */
    public ScoreBean() {
        scoreProperty = new SimpleLongProperty();
        scoreProperty.set(0);
        
        turnPoints1 = new SimpleIntegerProperty(0);
        gamePoints1 = new SimpleIntegerProperty(0);
        totalPoints1 = new SimpleIntegerProperty(0);
        
        turnPoints2 = new SimpleIntegerProperty(0);
        gamePoints2 = new SimpleIntegerProperty(0);
        totalPoints2 = new SimpleIntegerProperty(0);
        
        winningTeam = new SimpleObjectProperty<TeamId>(null);
        
        // the seven other properties are updated each time
        // the packed score is set.
        scoreProperty.addListener((s) -> update(s));
    }
    
    /**
     * By setting the score, the bean update the attributes
     * turnPoints, gamePoints, totalPoints, with the information
     * of the given score.
     * @param score
     */
    public void setScore(Score score) {
        scoreProperty.set(score.packed());
    }
    
    /**
     * The update is used to parse the score into its components,
     * so as to update the other properties.
     * @param s SimpleLongProperty : the score
     */
    private void update(Observable s) {
        if(s instanceof SimpleLongProperty) {
            long packed = ((SimpleLongProperty) s).get();
            
            int tp1 = PackedScore.totalPoints(packed, TeamId.TEAM_1);
            int tp2 = PackedScore.totalPoints(packed, TeamId.TEAM_2);
            
            turnPoints1.set(PackedScore.turnPoints(packed, TeamId.TEAM_1));
            gamePoints1.set(PackedScore.gamePoints(packed, TeamId.TEAM_1));
            totalPoints1.set(tp1);
            
            turnPoints2.set(PackedScore.turnPoints(packed, TeamId.TEAM_2));
            gamePoints2.set(PackedScore.gamePoints(packed, TeamId.TEAM_2));
            totalPoints2.set(tp2);
            
            // not sure if I should manually update winning team here
//            updateWinningTeam(tp1, tp2);
        }
    }
    
    private void updateWinningTeam(int tp1, int tp2) {
        if(tp1 >= Jass.WINNING_POINTS)
            setWinningTeam(TeamId.TEAM_1);
        if(tp2 >= Jass.WINNING_POINTS)
            setWinningTeam(TeamId.TEAM_2);
    }
    
    // TURN POINTS
    /**
     * Gives the turn points of the given team, in an unmodifiable form.
     * It is 0 by default
     * @param team
     * @return
     */
    public ReadOnlyIntegerProperty turnPointsProperty(TeamId team) {
        return (team == TeamId.TEAM_1) ? turnPoints1 : turnPoints2;
    }
    
    /**
     * To set the turn points of the given team to the new value.
     * @param team
     * @param points
     */
    public void setTurnPoints(TeamId team, int points) {
        if(team == TeamId.TEAM_1)
            turnPoints1.set(points);
        else
            turnPoints2.set(points);
    }
    
    // GAME POINTS
    /**
     * Gives the game points of the given team, in an unmodifiable form.
     * It is 0 by default
     * @param team
     * @return
     */
    public ReadOnlyIntegerProperty gamePointsProperty(TeamId team) {
        return (team == TeamId.TEAM_1) ? gamePoints1 : gamePoints2;
    }
    
    /**
     * To set the game points of the given team to the new value.
     * @param team
     * @param points
     */
    public void setGamePoints(TeamId team, int points) {
        if(team == TeamId.TEAM_1)
            gamePoints1.set(points);
        else
            gamePoints2.set(points);
    }
    
    // TOTAL POINTS
    /**
     * Gives the turn points of the given team, in an unmodifiable form.
     * It is 0 by default
     * @param team
     * @return
     */
    public ReadOnlyIntegerProperty totalPointsProperty(TeamId team) {
        return (team == TeamId.TEAM_1) ? totalPoints1 : totalPoints2;
    }
    
    /**
     * To set the total points of the given team to the new value
     * @param team
     * @param points
     */
    public void setTotalPoints(TeamId team, int points) {
        if(team == TeamId.TEAM_1) 
            totalPoints1.set(points);
        else
            totalPoints2.set(points);
    }
    
    // WINNING TEAM
    /**
     * Gives the winning team, in an unmodifiable form.
     * It is null by default.
     * @return
     */
    ReadOnlyObjectProperty<TeamId> winningTeamProperty() {
        return winningTeam;
    }
    
    /**
     * To set the winning team
     * @param winner
     */
    void setWinningTeam(TeamId winner) {
        winningTeam.set(winner);
    }
}
