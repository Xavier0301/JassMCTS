package ch.epfl.javass.ai;

import java.util.Map;

import ch.epfl.javass.ai.benchmark.PlayerBenchmarkable;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.TurnState;
import ch.epfl.javass.jass.Card.Color;

public final class SimplePlayer implements Player, PlayerBenchmarkable {
    
    private int wins = 0;
    private PlayerId ownId = null;
    
    private long totalExecutionTime = 0;
    private long totalExecutions = 0;

    public int getNumberOfWins() {
        return wins;
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public long getTotalNumberOfExecutions() {
        return totalExecutions;
    }
    
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        long beginTime = System.nanoTime();
        
        CardSet playable = state.trick().playableCards(hand);
        Card toPlay = getCard(state, playable);
                
        totalExecutionTime += (System.nanoTime() - beginTime);
        totalExecutions++;
        
        return toPlay;
    }
    
    private Card getCard(TurnState state, CardSet playable) {
        int trickSize = state.trick().size();
        Color trump = state.trick().trump();
        
        // GET THE CARDS OF THE OTHER PLAYERS
        
        Card teammateCard = null;
        Card opponentCard1 = null;
        Card opponentCard2 = null;
        
        switch(trickSize) {
        case 1:
            opponentCard1 = state.trick().card(0);
            break;
        case 2:
            teammateCard = state.trick().card(0);
            opponentCard1 = state.trick().card(1);
            break;
        case 3: 
            opponentCard1 = state.trick().card(0);
            teammateCard = state.trick().card(1);
            opponentCard2 = state.trick().card(2);
            break;
        default: break;
        }
        
        // GET THE CARD TO PLAY
        
        if(teammateHasBestCard(teammateCard, opponentCard1, opponentCard2, trump)) {
            // does this player have a stronger card than his teammate?
            Card bestCard = getStrongerOfBestValue(playable, teammateCard, trump);
            
            if(bestCard == null) 
                return getWeakest(playable, trump);
            else 
                return bestCard;
        } else {
            Card bestCard = getStrongerOfBestValue(playable, opponentCard1, opponentCard2, trump);
            
            if(bestCard == null)
                return getWeakest(playable, trump);
            else
                return bestCard;
        }
    }
    
    private boolean teammateHasBestCard(Card teammateCard, Card opponentCard1, Card opponentCard2, Color trump) {
        if(teammateCard == null) 
            return false;
        
        // if its teammate card is not null, opponentCard1 cannot be null either
        if(opponentCard2 == null)
            return teammateCard.isBetter(trump, opponentCard1);
        return teammateCard.isBetter(trump, opponentCard1) & teammateCard.isBetter(trump, opponentCard2);
    }
    
    private boolean cardIsBetter(Card toPlay, Card opponentCard1, Card opponentCard2, Color trump) {
        if(opponentCard1 == null)
            return false;
                    
        if(opponentCard2 == null)
            return toPlay.isBetter(trump, opponentCard1);
        return toPlay.isBetter(trump, opponentCard1) & toPlay.isBetter(trump, opponentCard2);
    }
    
    private Card getStrongerOfBestValue(CardSet playable, Card teammateCard, Color trump) {
        Card bestCard = null;
        int value = -1;
        for(int i=0; i<playable.size(); i++) {
            Card contender = playable.get(i);
            int contenderValue = contender.points(trump);
            if(contender.isBetter(trump, teammateCard) & contenderValue > value) {
                bestCard = contender;
                value = contenderValue;
            }
        }
        
        return bestCard;
    }
    
    private Card getStrongerOfBestValue(CardSet playable, Card opponentCard1, Card opponentCard2, Color trump) {
        Card bestCard = null;
        int value = -1;
        for(int i=0; i<playable.size(); i++) {
            Card contender = playable.get(i);
            int contenderValue = contender.points(trump);
            if(cardIsBetter(contender, opponentCard1, opponentCard2, trump) & contenderValue > value) {
                bestCard = contender;
                value = contenderValue;
            }
        }
        
        return bestCard;
    }
    
    private Card getWeakest(CardSet cards, Color trump) {
        Card weakest = cards.get(0);
        for(int i=1; i<cards.size(); i++) {
            Card contender = cards.get(i);
            if(weakest.isBetter(trump, contender))
                weakest = contender;
        }
        return weakest;
    }
    
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.ownId = ownId;
    }
    
    public void setWinningTeam(TeamId winningTeam) {
        if(ownId.team().equals(winningTeam))
            wins++;
    }
}