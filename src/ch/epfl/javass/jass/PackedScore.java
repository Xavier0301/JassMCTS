package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;
import ch.epfl.javass.bits.Bits64;

/**
 * The class is used to manipulate score that are represented
 * using (long). The score represented is the score so far 
 * in a game. Each team has
 *  - game points (2000 max): points earned in total so far 
 *      since last turn has been collected
 *  - turn points (257 max): points earned so far in the turn 
 *      since last trick has been collected
 *  - trick index: (9 max): which trick the turn is at
 * There are two teams, so we can represent the score in the following way:
 *  - bits 0 to 31: score of TeamId.TEAM_1
 *  - bits 32 to 63: score of TeamId.TEAM_2
 *  
 * Each team score format goes as follows:
 *  - bits 0 to 3: number of tricks
 *  - bits 4 to 12: tricks points
 *  - bits 13 to 23: game points
 *  - bits 24 to 31: unused bits.
 *  
 * @author xavier
 *
 */
public final class PackedScore {
    private PackedScore() {}
    
    // used to initialize scores at the beginning of games
    public static final long INITIAL = 0L;
    
    //used to check if a packedScore has its elements
    // in proper bounds
    static private final int unusedLo = 24;
    static private final int unusedSize = 8;
    static private final int gamePointsLo = 13;
    static private final int gamePointsSize = 11;
    static private final int turnPointsLo = 4;
    static private final int turnPointsSize = 9;
    static private final int numberOfTricksLo = 0;
    static private final int numberOfTricksSize = 4;
    
    // max number of tricks
    static private final int MAX_TRICKS_NUMBER = 9;
    // max points per trick
    static private final int MAX_TURN_POINTS = 257;
    // max number of points in a game
    static private final int MAX_GAME_POINTS = 2000;
    
    private static boolean isInBounds(int score, int max, int startIndex, int size) {
        int extracted = Bits32.extract(score, startIndex, size);
        return extracted <= max;
    }
    
    /*
     * teamScore is supposed to be 32 bits. It is of form
     * (31-24: 0 | 23-13 game points | 12-4 trick points | 3-0 number of tricks)
     * their bounds is specified at the top of this file
     */
    private static boolean teamScoreIsValid(int teamScore) {
        boolean isValid = isInBounds(teamScore, MAX_TRICKS_NUMBER, numberOfTricksLo, numberOfTricksSize);
        isValid &= isInBounds(teamScore, MAX_TURN_POINTS, turnPointsLo, turnPointsSize);
        isValid &= isInBounds(teamScore, MAX_GAME_POINTS, gamePointsLo, gamePointsSize);
        isValid &= isInBounds(teamScore, 0, unusedLo, unusedSize);
        
        return isValid;
    }
    
    /**
     * Decompose the pkScore into its two composants 
     * pkScore is of the form (team_2 score | team_1 score) with 32 bits for each
     * the 32 bits decomposition si explained in the javadoc of teamScoreIsValid(long)
     * @param pkScore
     * @return
     */
    public static boolean isValid(long pkScore) {
        int firstTeam = (int) Bits64.extract(pkScore, 0, Integer.SIZE);
        int secondTeam = (int) Bits64.extract(pkScore, Integer.SIZE, Integer.SIZE);
        
        return teamScoreIsValid(firstTeam) & teamScoreIsValid(secondTeam);
    }
    
    /**
     * pack args into a (int) of form
     * (gamePoints | turnPoints | turnTricks)
     * @param turnTricks1
     * @param turnPoints1
     * @param gamePoints1
     * @return
     */
    private static int packTeam(int turnTricks, int turnPoints, int gamePoints) {
        return Bits32.pack(turnTricks, numberOfTricksSize, turnPoints, turnPointsSize, gamePoints, gamePointsSize);
    }
    
    /**
     * Pack each composant into a long of form
     * (team_2|team_1) where each team is of form
     * (gamePoints | turnPoints | turnTricks)
     * @param turnTricks1
     * @param turnPoints1
     * @param gamePoints1
     * @param turnTricks2
     * @param turnPoints2
     * @param gamePoints2
     * @return
     */
    public static long pack(int turnTricks1, int turnPoints1, int gamePoints1, int turnTricks2, int turnPoints2, int gamePoints2) {
        int team1 = packTeam(turnTricks1, turnPoints1, gamePoints1);
        int team2 = packTeam(turnTricks2, turnPoints2, gamePoints2);
        return Bits64.pack(team1, Integer.SIZE, team2, Integer.SIZE);
    }
    
    /**
     * Get the score of the team t
     * @param pkScore
     * @param t
     * @return
     */
    private static int getTeamPart(long pkScore, TeamId t) {
        assert isValid(pkScore);
        
        int lowerBound = ((t==TeamId.TEAM_1) ? 0:Integer.SIZE);
        return (int) (Bits64.extract(pkScore, lowerBound, Integer.SIZE));
    }
    
    /**
     * Get the number of tricks for team t
     * @param pkScore
     * @param t
     * @return
     */
    public static int turnTricks(long pkScore, TeamId t) {
        assert isValid(pkScore);
        
        int teamScore = getTeamPart(pkScore, t);
        return Bits32.extract(teamScore, numberOfTricksLo, numberOfTricksSize);
    }
    
    /**
     * Get the turn points for team t
     * @param pkScore
     * @param t
     * @return
     */
    public static int turnPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);
        
        int teamScore = getTeamPart(pkScore, t);
        return Bits32.extract(teamScore, turnPointsLo, turnPointsSize);
    }
    
    /**
     * get the game points for team t
     * @param pkScore
     * @param t
     * @return
     */
    public static int gamePoints(long pkScore, TeamId t) {
        assert isValid(pkScore);
        
        int teamScore = getTeamPart(pkScore, t);
        return Bits32.extract(teamScore, gamePointsLo, gamePointsSize);
    }
    
    /**
     * Get total points for team t
     * As the score stores the points of the game thus far 
     * and the the points of the current turn we have a simple formula
     * @param pkScore
     * @param t
     * @return
     */
    public static int totalPoints(long pkScore, TeamId t) {
        assert isValid(pkScore);
        
        return turnPoints(pkScore, t) + gamePoints(pkScore, t);
    }
    
    /**
     * Adds the specified trick points to pkScore of winningTeam 
     * Add MATCH_ADDITIONAL_POINTS if the winning team won 
     * all the tricks of the turn
     * (Only changes the turnPoints of the concerned team in the pkScore)
     * DOES NOT HANDLE LAST_TRICK_ADDITIONAL_POINTS
     * 
     * I chose to not use any private methods although it would probably have
     * shorten the code. 
     * @param pkScore
     * @param winningTeam
     * @param trickPoints
     * @return
     */
    public static long withAdditionalTrick(long pkScore, TeamId winningTeam, int trickPoints) {
        assert isValid(pkScore);
        
        int tricksOfTurn = turnTricks(pkScore, winningTeam)+1;
        int pointsOfTurn = turnPoints(pkScore, winningTeam);
        int pointsOfGame = gamePoints(pkScore, winningTeam);
        pointsOfTurn += trickPoints;
        if(tricksOfTurn == Jass.TRICKS_PER_TURN) 
            pointsOfTurn += Jass.MATCH_ADDITIONAL_POINTS;
        
        int otherTricks = turnTricks(pkScore, winningTeam.other());
        int otherTurnP = turnPoints(pkScore, winningTeam.other());
        int otherGameP = gamePoints(pkScore, winningTeam.other());
        
        
        if(winningTeam == TeamId.TEAM_1) 
            return pack(tricksOfTurn, pointsOfTurn, pointsOfGame, otherTricks, otherTurnP, otherGameP);
        else 
            return pack(otherTricks, otherTurnP, otherGameP, tricksOfTurn, pointsOfTurn, pointsOfGame);
    }
    
    /**
     * Get score when turn is completed
     * This means settings turnTricks et turnPoints to 0
     * This means updating gamePoints to totalPoints
     * @param pkScore
     * @return
     */
    public static long nextTurn(long pkScore) {
        assert isValid(pkScore);
        
        return pack(0, 0, totalPoints(pkScore, TeamId.TEAM_1), 0, 0, totalPoints(pkScore, TeamId.TEAM_2));
    }
    
    /**
     * Gives a string to represent score of teamId in pkScore 
     * of form (turnTricks, turnPoints, gamePoints)
     * @param pkScore
     * @param teamId
     * @return
     */
    private static String litteralRepresentation(long pkScore, TeamId teamId) {
        return "("+turnTricks(pkScore, teamId)+","+turnPoints(pkScore, teamId)+","+gamePoints(pkScore, teamId)+")";
    }
    
    /**
     * gives a string of form (team1Score \ team2Score)
     * with team score of form (turnTricks, turnPoints, gamePoints)
     * @param pkScore
     * @return
     */
    public static String toString(long pkScore) {
        assert isValid(pkScore);
        
        return litteralRepresentation(pkScore, TeamId.TEAM_1) + "/" + litteralRepresentation(pkScore, TeamId.TEAM_2);
    }
}
