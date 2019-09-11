package ch.epfl.javass.ai;

import static ch.epfl.javass.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;

import ch.epfl.javass.ai.benchmark.PlayerBenchmarkable;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.TurnState;

import static java.lang.Math.sqrt;
import static java.lang.Math.log;

/**
 * We implement a player who is going to make an informed decision on its own (i.e. an AI)
 * The algorithm used to determine the next card to play is Monte Carlo Tree Search (MCTS)
 * Here is a summary of what it does:
 *  - As soon as you are asked what card to play via cardToPlay(_;_),
 *  the algorithm creates a tree, which root represents the given state
 *  - The tree is given how many iterations it should perform at construction,
 *  and it is going to perform the following at each iteration:
 *      - You add a node to the tree, which is going to represent a turn state
 *      not yet explored.
 *      - Once the node is created, you play a random TURN from the state of the node
 *      - You store the score obtained at the end of the turn
 *      
 * After the iterations are done, we have enough data (hopefuly, with enough iterations),
 * to know which node collected the best score in average.
 * This is done by the following mechanism: not only when you play a random turn from the given node
 * you attribute its score to the node, but for each child node's (i.e. each turn state that passes
 * at one points by the state of the given node) random turn, we update the score of the parents node.
 * 
 * The intuition is that given a node with a turn state, the more subsequent turn possibilities you explore 
 * i.e. the more child nodes you create, the more you have data on what score the turn state leads to.
 * 
 * The way you choose the card to play is thus choosing which child node of the root leads to the best
 * outcome for this Player's team
 * 
 * @author xavier
 *
 */
public final class MctsPlayer implements Player, PlayerBenchmarkable {
    private PlayerId ownId;
    private SplittableRandom rng;
    private int iterations;
//    Node startingNode;
        
    private static double c = 40.0;
    
    /**
     * Mcts Player is an artifical intelligence for the game, which implements
     * the Monte Carlo Search Tree Algorithm.
     * @param ownId id of this player
     * @param rngSeed used to play random games 
     * @param iterations number of iterations in the algorithm
     */
    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        checkArgument(iterations >= Jass.HAND_SIZE);
        
        this.ownId = ownId;
        this.rng = new SplittableRandom(rngSeed);
        this.iterations = iterations;
    }
    
    private int wins = 0;
    
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
    public void setWinningTeam(TeamId winningTeam) {
        if(ownId.team().equals(winningTeam))
            wins++;
    }
    
    /**
     * This is the method that implements the Monte Carlo Search Tree algorithm
     * The steps are explained in the documentation of this class
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        totalExecutions++;
        long beginTime = System.nanoTime();
        
        int leftIterations = iterations;
        
        Node startingNode = new Node(state, hand, ownId);
        while(leftIterations-->0) {
            // SELECTION - EXPANSION
            List<Integer> trivialPath = new ArrayList<Integer>();
            List<Integer> path = startingNode.addNodeIfPossible(trivialPath);
            Node addedNode = getNodeForPath(path, startingNode);
            
            // SIMULATION
            Score score = randomlyPlayTurnFromNode(addedNode);
            
            // BACKPROPAGATION
            propagatePointsToNodesOf(path, startingNode, score);
        }
                
        int bestChildIndex = startingNode.getBestChildIndex(0);
        Card bestCard = state.trick().playableCards(hand).get(bestChildIndex);
        
        totalExecutionTime += (System.nanoTime()-beginTime);
        
        return bestCard;
    }
    
    /**
     * Get the playable cards that are not in the hand nor in the cards
     * already played in the turn.
     * @param state
     * @param hand
     * @return
     */
    private CardSet getPlayableCards(TurnState state, CardSet hand) {
        CardSet unplayedCards = state.unplayedCards();
        CardSet handSoFar = unplayedCards.intersection(hand);
        
        if(ownId == state.nextPlayer())
            return state.trick().playableCards(handSoFar);
                
        CardSet unplayedNotInHand = unplayedCards.difference(handSoFar);
        return state.trick().playableCards(unplayedNotInHand);
//        return unplayedNotInHand;
    }

    private Node getNodeForPath(List<Integer> path, Node startingNode) {
        Node child = startingNode;
        for(Integer index: path) {
            child = child.children[index];
        }
        return child;
    }
    
    private Score randomlyPlayTurnFromNode(Node node) {
        TurnState turn = node.correspondingTurnState;
        CardSet hand = node.handSoFar;
        
        while(!turn.isTerminal()) {
            CardSet playable = getPlayableCards(turn, hand);
            
            int indexOfNextCard = rng.nextInt(playable.size());
            Card nextCard = playable.get(indexOfNextCard);
                        
            turn = turn.withNewCardPlayedAndTrickCollected(nextCard);
            // we don't need to update the hand because in getPlayableCards(_,_),
            // we return unplayedCard.intersection(hand) and not just hand.
        }
        
        return turn.score();
    }
    
    private void propagatePointsToNodesOf(List<Integer> path, Node startingNode, Score score) {
        startingNode.addTurn(score.turnPoints(ownId.team().other()));
        PlayerId nextPlayer = startingNode.correspondingTurnState.nextPlayer();
        Node node = startingNode;
        for(Integer index: path) {
            node = node.children[index];
            node.addTurn(score.turnPoints(nextPlayer.team()));
            nextPlayer = node.correspondingTurnState.nextPlayer();
        }
    }
    
    private final static class Node {
        /**
         * @param state should be collected by convention
         * @param handSoFar
         * @param playerId
         */
        private Node(TurnState state, CardSet handSoFar, PlayerId playerId) {
            this.correspondingTurnState = state;
            this.handSoFar = handSoFar;
            this.playerId = playerId;
            this.unplayedCards = getUnplayedCards();
            this.children = new Node[this.unplayedCards.size()];
        }
        
        private CardSet getUnplayedCards() {
            if(correspondingTurnState.isTerminal())
                return CardSet.EMPTY;
            if(correspondingTurnState.nextPlayer() == playerId) 
                return correspondingTurnState.trick().playableCards(handSoFar);
            CardSet notInHand = correspondingTurnState.unplayedCards().difference(handSoFar);
            CardSet notInHandPlayable = correspondingTurnState.trick().playableCards(notInHand);
            return notInHandPlayable;
        }
        
        /**
         * This is the turn state of this node. The trick of the turn is always
         * collected by convention.
         * It is easier to know the unplayed cards at construction and the next player.
         */
        final TurnState correspondingTurnState;
        /**
         * When the Node is create, none of its children actually exist,
         * they are all initialized to null at the beginning.
         * They will eventually be correctly instantiated.
         * (Will they be instantiated as soon as a new turn state stemming 
         * from the turn state of this node is simulated randomly ?)
         * 
         * The number of children depends on how many possibilities there actually 
         * is for the turn state after the corresponding turn state
         */
        Node[] children;
        /**
         * As soon as a new child is added, the corresponding card is removed
         * from unplayedCards. This is because a new child added means a state that is different from any other 
         * child, thus using a card thus far unused, and we need to inform the node 
         * that the card has actually been used in a simulation
         */
        CardSet unplayedCards;
        
        /**
         * We keep track of the hand so far to get a correct tree of possible turn states.
         * We use this attribute to compute the next child node if the next player
         * was the playerId.
         */
        CardSet handSoFar;
        PlayerId playerId;
                
        /*
         * Noted S(n), it gives the points that were so far gained from 
         * randomly played turns, which contains the corresponding turn state.
         * 
         * What this means is that every time the algorithm is going to simulate a new 
         * turn, if the turn happens to randomly come at one point to the corresponding
         * turn state of the node, it would credit the node for the points 
         * gained in the turn.
         * 
         * Also, it is going to augment the counter (int totalRandomlyPlayedTurns)
         * every time a randomly played turn happens to have the turn state of this 
         * node at one point
         * 
         * Thus we can compute the average points of a game knowing that such game came at the 
         * corresponding turn state at one point. 
         * 
         * So for example, if the corresponding turn state is a trick of index 5, where the first
         * player is PLAYER_2, where the trump is Heart and the played cards are (six of heart, seven of spade),
         * if the algorithm is randomly playing a turn and it eventually reaches a trick
         * of index five, if the underlying variables are equal then the algorithm is going to tell the 
         * node the points that were gained after it's done simulating the whole turn, 
         * and it is going to add 1 to the counter of playedTurns of the node.
         */
        int totalRandomlyGainedPoints = 0;
        int totalRandomlyPlayedTurns = 0;
        
        private double computeChildV(Node child, double c) {
            assert child != null;
            
            double firstCoeff = ((double) child.totalRandomlyGainedPoints)/((double) child.totalRandomlyPlayedTurns);
            double insideSqrt = (2*log((double) this.totalRandomlyPlayedTurns))/((double) child.totalRandomlyPlayedTurns);
            double secondCoeff = c*sqrt(insideSqrt);
            
            return firstCoeff+secondCoeff;
        }
        
        private int getBestChildIndex(double c) {
            double maxV = -1;
            int indexOfBest = -1;
            for(int i=0; i<children.length; i++) {
                Node child = children[i];
                if(child != null) {
                    double V = this.computeChildV(child, c);
                    if(V > maxV) {
                        maxV = V;
                        indexOfBest = i;
                    }
                }
            }
            
            return indexOfBest;
        }
        
        private List<Integer> addNodeIfPossible(List<Integer> previousPath) {
            if(correspondingTurnState.isTerminal())
                return previousPath;
            if(!unplayedCards.isEmpty()) {
                previousPath.add(setNewChild());
                return previousPath;
            }
            
            int bestChildIndex = getBestChildIndex(MctsPlayer.c);
            previousPath.add(bestChildIndex);
            return children[bestChildIndex].addNodeIfPossible(previousPath);
        }
        
        private int setNewChild() {
            assert !unplayedCards.isEmpty();
                        
            Card nextRemaining = unplayedCards.get(0);
            unplayedCards = unplayedCards.remove(nextRemaining);
            
            TurnState newState;
            /* 
             * We collect the trick before passing it the the new child constructor by convention.
             */
            newState = correspondingTurnState.withNewCardPlayedAndTrickCollected(nextRemaining);
            
            Node newChild = new Node(newState, getNextHand(nextRemaining), playerId);
            for(int i=0; i<children.length; i++) {
                if(children[i] == null) {
                    children[i] = newChild;
                    return i;
                }
            }
            return -1;
        }
        
        private CardSet getNextHand(Card played) {
            if(correspondingTurnState.nextPlayer() == playerId)
                return handSoFar.remove(played);
            return handSoFar;
        }
       
        private void addTurn(int points) {
            totalRandomlyGainedPoints+=points;
            totalRandomlyPlayedTurns++;
        }
        
        // for debugging purpose
        private void display() {
            System.out.print(correspondingTurnState.trick().toString() + "|");
            System.out.print(this.computeChildV(this, 0));
            System.out.print("|");
            System.out.print(this.totalRandomlyGainedPoints);
            System.out.print("(");
            System.out.print(this.totalRandomlyPlayedTurns);
            System.out.println(")");
            for(Node child: children) {
                if(child == null) 
                    System.out.println(" - {null} ");
                else {
                    System.out.print(" - {" + child.correspondingTurnState.trick().toString() + "|");
                    System.out.print(this.computeChildV(child, 0));
                    System.out.print("|");
                    System.out.print(child.totalRandomlyGainedPoints);
                    System.out.print("(");
                    System.out.print(child.totalRandomlyPlayedTurns);
                    System.out.println(")}");
                }
            }
            System.out.println();
        }
    }
}
