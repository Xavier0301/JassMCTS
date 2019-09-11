package ch.epfl.javass.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.epfl.javass.ai.benchmark.PlayerBenchmarkable;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.TurnState;

/**
 * This player implement the Minimax algorithm to compute its cardToPlay.
 * In particular, the Minimax tree does not go deeper than the end of the 
 * current trick, which is why it is called Shallow.
 * @author xavier
 *
 */
public final class ShallowMinimaxPlayer implements Player, PlayerBenchmarkable {
    
    /**
     * Wether we want the values of the node to be the fraction
     * (own team score)/(opponent team score) or if we want the value
     * to simply be (own team score):
     */
    private static final boolean SHOULD_MAXIMIZE_FRACTION = true;
    
    private int wins = 0;
    private PlayerId ownId = null;
    
    private long totalExecutionTime = 0;
    private long totalExecutions = 0;

    @Override
    public int getNumberOfWins() {
        return wins;
    }
    
    @Override
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    @Override
    public long getTotalNumberOfExecutions() {
        return totalExecutions;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        totalExecutions++;
        long beginTime = System.nanoTime();
        
        int depth = 4-state.trick().size();
                    
        List<List<MinimaxNode>> tree = new ArrayList<List<MinimaxNode>>();
        
        // POPULATING THE TREE
        
        List<MinimaxNode> root = new ArrayList<MinimaxNode>();
        root.add(new MinimaxNode(state, ownId));
        tree.add(root);
        
        for(int d=1; d<=depth; d++) {
            List<MinimaxNode> parentNodes = tree.get(d-1);
            List<MinimaxNode> leveledNodes = new ArrayList<MinimaxNode>();
            
            int count = 0;
            for(int i=0; i<parentNodes.size(); i++) {
                int lo = count;
                
                MinimaxNode parent = parentNodes.get(i);
                CardSet unplayed = parent.getUnplayed(hand);
                
                while(!unplayed.isEmpty()) {
                    Card nextCard = unplayed.get(0);
                    TurnState nextState = parent.state.withNewCardPlayed(nextCard);
                    MinimaxNode nextChild = new MinimaxNode(nextState, ownId);
                    
                    leveledNodes.add(nextChild);
                    
                    unplayed = unplayed.remove(nextCard);
                    count++;
                }
                int hi = count;
                
                parent.setChildrenIndexRange(lo, hi);
            }
            
            tree.add(leveledNodes);
        }
        
        // COMPUTING THE VALUES
        
        // first: compute the values of the last row:
        
        List<MinimaxNode> lastRow = tree.get(depth);
        for(int i=0; i<lastRow.size(); i++) {
            MinimaxNode lastRowNode = lastRow.get(i);
            // correcting for initial value has no impact on the 
            // performances but is better for readability
            Score nextScore = lastRowNode.state.withTrickCollected().score();
            if(SHOULD_MAXIMIZE_FRACTION) 
                lastRowNode.value = getValueFraction(nextScore, ownId.team(), state.score());
            else
                lastRowNode.value = getValue(nextScore, ownId.team(), state.score());
                
        }
        
        // second: backtrack to the top
        
        for(int d=depth-1; d>=0; d--) {
            List<MinimaxNode> parentRow = tree.get(d);
            List<MinimaxNode> childrenRow = tree.get(d+1);
            for(int i=0; i<parentRow.size(); i++) {
                MinimaxNode parentNode = parentRow.get(i);
                List<MinimaxNode> correspondingChildren = childrenRow.subList(parentNode.childrenStartIndex, parentNode.childrenEndIndex);
                parentNode.setValue(correspondingChildren, parentNode.state.nextPlayer().team());
            }
        }
        
        
        // GETTING WHICH DIRECT CHILD HAS BEST VALUE
        
        List<MinimaxNode> possibilities = tree.get(1);
        double maxValue = possibilities.get(0).value;
        int maxIndex = 0;
        for(int i=1; i<possibilities.size(); i++) {
            double contenderValue = possibilities.get(i).value;
            if(contenderValue > maxValue) {
                maxValue = contenderValue;
                maxIndex = i;
            }
        }
        
        Card bestCard = state.trick().playableCards(hand).get(maxIndex);
        
        totalExecutionTime += (System.nanoTime()-beginTime);
                    
        return bestCard;
    }
    
    private int getValue(Score score, TeamId teamId, Score initialScore) {
        return score.totalPoints(teamId) - initialScore.totalPoints(teamId);
    }
    
    private double getValueFraction(Score score, TeamId teamId, Score initialScore) {
        int initialValue = initialScore.totalPoints(teamId);
        int initialOpponentValue = initialScore.totalPoints(teamId.other());
        
        int finalValue = score.totalPoints(teamId);
        int finalOpponentValue = score.totalPoints(teamId.other());
        
        return ((double) (finalValue - initialValue + 1))/((double) (finalOpponentValue - initialOpponentValue + 1));
    }
     
    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.ownId = ownId;
    }
    
    @Override
    public void setWinningTeam(TeamId winningTeam) {
        if(ownId.team().equals(winningTeam))
            wins++;
    }
    
    private void displayBlank(List<List<MinimaxNode>> tree) {
        for(List<MinimaxNode> nodes: tree) {
            for(MinimaxNode node: nodes) {
                System.out.print("o ");
            }
            System.out.println();
        }
    }
    
    private final static class MinimaxNode {
        TurnState state;
        PlayerId ownId;
                    
        int childrenStartIndex = -1;
        int childrenEndIndex = -1;
                    
        double value = 0;
                    
        public MinimaxNode(TurnState state, PlayerId ownId) {
            this.state = state;
            this.ownId = ownId;
        }
        
        public CardSet getUnplayed(CardSet hand) {
            if(isTerminal())
                return CardSet.EMPTY;
            if(state.nextPlayer() == ownId) 
                return state.trick().playableCards(hand);
            CardSet notInHand = state.unplayedCards().difference(hand);
            CardSet notInHandPlayable = state.trick().playableCards(notInHand);
            return notInHandPlayable;
        }
        
        public void setChildrenIndexRange(int start, int end) {
            this.childrenStartIndex = start;
            this.childrenEndIndex = end;
        }
        
        private void setValue(List<MinimaxNode> children, TeamId teamId) {
            if(teamId.equals(ownId.team()))
                value = getMaxOfChildren(children);
            else
                value = getMinOfChildren(children);
        }
                    
        private double getMinOfChildren(List<MinimaxNode> children) {
            double minValue = 2000;
            for(int i=0; i<children.size(); i++) {
                if(children.get(i).value < minValue)
                    minValue = children.get(i).value;
            }
            return minValue;
        }
        
        private double getMaxOfChildren(List<MinimaxNode> children) {
            double maxValue = -1;
            for(int i=0; i<children.size(); i++) {
                if(children.get(i).value > maxValue)
                    maxValue = children.get(i).value;
            }
            return maxValue;
        }
        
        private boolean isTerminal() {
            return state.trick().isFull();
        }
    }
}