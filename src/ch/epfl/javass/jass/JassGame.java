package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The class is used to run a game of jass. 
 * in jass, a game can be represented by the following attributed
 *  - set of players
 *  - set of player hands
 *  - set of player names
 *  - current turn state
 * 
 * We also make use of Random types for shuffling the cards:
 *  - shuffleRng, trumpRng
 *  
 * @author xavier
 *
 */
public final class JassGame {
    private final Map<PlayerId, Player> players;
    private final Map<PlayerId, String> playerNames;
    
    private final Map<PlayerId, CardSet> playerHands;
    
    private final Random shuffleRng;
    private final Random trumpRng;
    
    private TurnState currentTurn;   
    
    private TeamId winningTeam = null;
    
    private PlayerId firstPlayer = null;
        
    /**
     * We do the following things when instantiating a game:
     *  - setup all the variables
     *  - initialize the first turn
     * @param rngSeed
     * @param players
     * @param playerNames
     */
    public JassGame(long rngSeed, Map<PlayerId, Player> players, Map<PlayerId, String> playerNames) {
        Random rng = new Random(rngSeed);
        this.shuffleRng = new Random(rng.nextLong());
        this.trumpRng = new Random(rng.nextLong());
        
        this.players = Collections.unmodifiableMap(new EnumMap<>(players));
        this.playerNames = Collections.unmodifiableMap(new EnumMap<>(playerNames));
        
        // creating the hands of the players.
        this.playerHands = new EnumMap<>(PlayerId.class);
        for(PlayerId id: PlayerId.ALL) {
            this.playerHands.put(id, CardSet.EMPTY);
        }

        // method used to inform each player of the 
        // player list and their names
        setPlayersNameList();
        
        initializeFirstTurn();
    }
    
    private Card.Color getRandomTrump() {
        int randomTrumpOrdinal = trumpRng.nextInt(Card.Color.COUNT);
        return Card.Color.ALL.get(randomTrumpOrdinal);
    }
    
    /**
     * The following method tells you if a game is over
     * When the method is called, the winningTeam attribute is set.
     * @return
     */
    public boolean isGameOver() {
        if(winningTeam != null) 
            return true;
        int teamOneScore = currentTurn.score().totalPoints(TeamId.TEAM_1);
        int teamTwoScore = currentTurn.score().totalPoints(TeamId.TEAM_2);
        boolean teamOneWon = teamOneScore >= Jass.WINNING_POINTS;
        boolean teamTwoWon = teamTwoScore >= Jass.WINNING_POINTS;
        if(teamOneWon) {
            winningTeam = TeamId.TEAM_1;
        } else if(teamTwoWon) {
            winningTeam = TeamId.TEAM_2;
        }
        return teamOneWon | teamTwoWon;
    }
    
    private List<Card> getShuffledDeck() {
        List<Card> deck = Card.getAllCards();
        Collections.shuffle(deck, shuffleRng);
        return deck;
    }
    
    private void distributeCards() {
        List<Card> deck = getShuffledDeck();
        
        for(PlayerId playerId: PlayerId.ALL) {
            List<Card> playerCards = deck.subList(playerId.ordinal()*Jass.HAND_SIZE, (playerId.ordinal()+1)*Jass.HAND_SIZE);
            playerHands.put(playerId, CardSet.of(playerCards));
        }
        updatePlayersHand();
    }
    
    private PlayerId setNextFirstPlayerOfTurn() {
        if(this.firstPlayer == null) {
            PlayerId firstPlayer = PlayerId.PLAYER_1;
            for(PlayerId playerId: PlayerId.ALL) {
                if(setIncludesSevenOfDiamond(playerHands.get(playerId))) {
                    firstPlayer = playerId;
                }
            }
            this.firstPlayer = firstPlayer;
            return firstPlayer;
        } else {
            int currentFirstOrdinal = this.firstPlayer.ordinal();
            int nextFirstOrdinal = (currentFirstOrdinal+1)%4;
            return PlayerId.ALL.get(nextFirstOrdinal);
        }
    }
     
    /**
     * The first player of a turn is the one who owns the seven of diamond
     * This method helps to figure out which player is the first one.
     * @param cards
     * @return
     */
    private boolean setIncludesSevenOfDiamond(CardSet cards) {
        return cards.contains(Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN));
//        Card sevenOfDiamond = Card.of(Card.Color.DIAMOND, Card.Rank.SEVEN);
//        CardSet singleton = CardSet.EMPTY;
//        singleton = singleton.add(sevenOfDiamond);
//        return !cards.intersection(singleton).isEmpty();
    }
    
    private void initializeFirstTurn() {
        Card.Color trump = getRandomTrump();
        Score initialScore = Score.INITIAL;
        distributeCards();
        
        setNextFirstPlayerOfTurn();
        
        this.currentTurn = TurnState.initial(trump, initialScore, firstPlayer);
        
        setPlayersTrump(trump);
    }
    
    private void initializeNewTurn() {
        Card.Color trump = getRandomTrump();
        Score initialScore = this.currentTurn.score().nextTurn();
        distributeCards();        
        
        setNextFirstPlayerOfTurn();
        
        this.currentTurn = TurnState.initial(trump, initialScore, firstPlayer);
        
        setPlayersTrump(trump);
    }
    
    /**
     * In this method, we do the following things:
     *  - we check if the game is over
     *  - we initialize a new turn if the last turn state is a terminal one
     *  - we check if the current trick is full. If so, we collect it.
     *  - we update the score and trick state of each player
     *  - we ask for each player the card he wants to play till
     *      the trick is full.
     *      - at each card played, we notify the player of its 
     *      new hand and each player of the new trick state
     *  - we check if the game is over at the end also
     */
    public void advanceToEndOfNextTrick() {
        if(isGameOver()) 
            return;
        
        if(currentTurn.trick().isFull())
            currentTurn = currentTurn.withTrickCollected();
        if(currentTurn.isTerminal()) 
            initializeNewTurn();
        
        updatePlayersScore(currentTurn.score());
        updatePlayersTrick(currentTurn.trick());
        
        if(isGameOver()) {
            updatePlayersWin();
            return;
        }
        
        while(!currentTurn.trick().isFull()) {
            PlayerId playerId = currentTurn.nextPlayer();
            
            Card toPlay = getPlayer(playerId).cardToPlay(currentTurn, playerHands.get(playerId));
            currentTurn = currentTurn.withNewCardPlayed(toPlay);
            
            updatePlayerHand(toPlay, playerId);
            
            updatePlayersTrick(currentTurn.trick());
        }
        
        if(isGameOver()) 
            updatePlayersWin();
        
    }
    
    private Player getPlayer(PlayerId playerId) {
        return players.get(playerId);
    }
    
    private void setPlayersNameList() {
        for(PlayerId playerId: PlayerId.ALL) {
            getPlayer(playerId).setPlayers(playerId, playerNames);
        }
    }
    
    private void updatePlayersTrick(Trick trick) {
        for(PlayerId playerId: PlayerId.ALL) {
            getPlayer(playerId).updateTrick(trick);
        }
    }
    
    private void updatePlayersHand() {
        for(PlayerId playerId: PlayerId.ALL) {
            CardSet hand = playerHands.get(playerId);
            getPlayer(playerId).updateHand(hand);
        }
    }
    
    private void updatePlayerHand(Card cardPlayed, PlayerId playerId) {
        CardSet hand = playerHands.get(playerId);
        CardSet newHand = hand.remove(cardPlayed);
        getPlayer(playerId).updateHand(newHand);
        playerHands.replace(playerId, newHand);
    }
    
    private void setPlayersTrump(Card.Color trump) {
        for(PlayerId playerId: PlayerId.ALL) {
            getPlayer(playerId).setTrump(trump);
        }
    }
    
    private void updatePlayersScore(Score score) {
        for(PlayerId playerId: PlayerId.ALL) {
            getPlayer(playerId).updateScore(score);
        }
    }
    
    private void updatePlayersWin() {
        for(PlayerId playerId: PlayerId.ALL) {
            getPlayer(playerId).setWinningTeam(winningTeam);
        }
    }
}
