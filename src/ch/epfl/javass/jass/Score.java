package ch.epfl.javass.jass;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * A score in Jass can be represented that way:
 *  - Each team has a score
 *  - Each team's score hold the following attributes:
 *      - points so far gained in the game, since last turn has been collected
 *      - points so far gained in the turn, since last trick was collected
 *      - trick index, which is stored because we need to know its value 
 *          for certain calculations
 *  
 * The class has an attribute packedScore which packs all the above attributes,
 * in a way that is described in PackedScore.
 * 
 * the attribute is final i.e. immutable after instantiation
 * @author xavier
 *
 */
public final class Score {
    final long packedScore;
    
    public static Score INITIAL = new Score(PackedScore.INITIAL);
    
    private Score(long packed) {
        packedScore = packed;
    }
    
    /**
     * Return an instance of this class where packedScore = packed
     * Checks if packed is valid.
     * If not, throws InvalidArgumentException
     * @param packed
     * @return
     */
    public static Score ofPacked(long packed) {
        checkArgument(PackedScore.isValid(packed));
        return new Score(packed);
    }
    
    /**
     * @return packedScore representation of the score
     */
    public long packed() {
        return packedScore;
    }
    
    /**
     * turnTricks of team t
     * @param t
     * @return
     */
    public int turnTricks(TeamId t) {
        return PackedScore.turnTricks(packedScore, t);
    }
    
    /**
     * turnPoints of team t
     * @param t
     * @return
     */
    public int turnPoints(TeamId t) {
        return PackedScore.turnPoints(packedScore, t);
    }
    
    /**
     * gamePoints of team t
     * @param t
     * @return
     */
    public int gamePoints(TeamId t) {
        return PackedScore.gamePoints(packedScore, t);
    }
    /**
     * totalPoints of team t
     * @param t
     * @return
     */
    public int totalPoints(TeamId t) {
        return PackedScore.totalPoints(packedScore, t);
    }
    
    /**
     * Return the score after a trick was just won by 
     * winningTeam. The turnPoints only is updated. 
     * Throws IllegalArgumentException if trickPoints is negative
     * 
     * See PackedScore.withAdditionalTrick for more infos
     * @param winningTeam
     * @param trickPoints
     * @return
     */
    public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) {
        checkArgument(trickPoints >= 0);
        
        long updatedPacked = PackedScore.withAdditionalTrick(packedScore, winningTeam, trickPoints);
        return new Score(updatedPacked);
    }
    /**
     * Return the score after the current turn is ended
     * The turnTricks and turnPoints are set to 0
     * The gamePoints is updated
     * @return
     */
    public Score nextTurn() {
        long updatedPacked = PackedScore.nextTurn(packedScore);
        return new Score(updatedPacked);
    }
    
    /**
     * Returns true iff that is a Score 
     * and has the same packedScore as this
     */
    public boolean equals(Object that) {
        if(that instanceof Score)
            return ((Score) that).packedScore == this.packedScore;
        return false;
    }
    
    /**
     * Gives string representation of the score
     * of form (team1score / team2Score)
     * with teamScore of form (turnTricks | turnPoints | gamePoints)
     */
    public String toString() {
        return PackedScore.toString(packedScore);
    }
    
    /**
     * Returns hashCode for that object
     * It has to be equal to the hashCode of 
     * its packedScore due to the way we compare
     * (with equals)
     */
    public int hashCode() {
        return Long.hashCode(packedScore);
    }
}
