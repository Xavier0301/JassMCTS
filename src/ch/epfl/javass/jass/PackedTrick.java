package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;

/**
 * Class used to manipulate tricks encoded in a (int).
 * A trick can be caracterized by
 * - the card layed down so far
 * - the trump of the trick
 * - the first player of the trick
 * the index of the trick
 * 
 * So we can format a (int) to represent a trick:
 *  - bits 0 to 5: card0 (it can be PackedCard.INVALID,
 *      which would mean the card has not been played at that position
 *  - bits 6 to 11: card1 (can be PackedCard.INVALID)
 *  - bits 12 to 17: card2 (can be PackedCard.INVALID)
 *  - bits 18 to 23: card3 (can be PackedCard.INVALID)
 *  - bits 24 to 27: index (from 0 to 9)
 *  - bits 28 to 29: PlayerId ordinal
 *  - bits 30 to 31: trump Color Ordinal
 *  
 * @author xavier
 *
 */
public final class PackedTrick {
    private PackedTrick() {}
    
    public static final int INVALID = -1;
    
    private static int CARD_SIZE = 6;
    
    private static int PLAYER_SIZE = 2;
    private static int PLAYER_START = 28;
    
    private static int TRUMP_SIZE = 2;
    private static int TRUMP_START = 30;
            
    private static int INDEX_SIZE = 4;
    private static int INDEX_START = 24;
    private static int MAX_INDEX_VALUE = 8;
    
    /**
     * 
     * @param pkTrick
     * @param index
     * @param size
     * @return
     */
    private static boolean cardComponentIsValid(int pkTrick, int index, int size) {
        int component = Bits32.extract(pkTrick, index, size);
        return PackedCard.isValid(component);
    }
    
    private static boolean componentIsValid(int pkTrick, int index, int size, int maxValue) {
        int component = Bits32.extract(pkTrick, index, size);
        return component <= maxValue;
    }
    
    /**
     * A pkTrick is of form
     * (trump (30->31 | player1 (28->29) | index (24->27) | card3 (18->23) | ... | card0(0->5)))
     * where 
     *  - in the middle of the trick, it is understood that we haven't played all
     *      the cards so invalid cards can be added to the indexes where we haven't played
     *      Therefore, it is only possible to have ONLY invalid cards after a chain of valid cards 
     *      like so (invalid, invalid, valid, valid) (i.e. (valid, invalid, valid, valid) makes
     *      no sense since we have played the 4th card of the trick but not the third).
     *  - trump and player1 each take 2 bits and can take 4 values, thus we don't don't have to
     *      check the last 4 bits because any value would be valid
     *  - index is between 0 and 8, and thus has to take 4 bits, but we have to make sure that
     *      its value is not above 8 (MAX_INDEX_VALUE).
     * @param pkTrick
     * @return
     */
    public static boolean isValid(int pkTrick) {
        boolean cardValid0 = cardComponentIsValid(pkTrick, 0, CARD_SIZE);
        boolean cardValid1 = cardComponentIsValid(pkTrick, 1*CARD_SIZE, CARD_SIZE);
        boolean cardValid2 = cardComponentIsValid(pkTrick, 2*CARD_SIZE, CARD_SIZE);
        boolean cardValid3 = cardComponentIsValid(pkTrick, 3*CARD_SIZE, CARD_SIZE);
        
        boolean pkIsValid = cardValid0 & cardValid1 & cardValid2 & cardValid3;
        pkIsValid |= cardValid0 & cardValid1 & cardValid2 & !cardValid3;
        pkIsValid |= cardValid0 & cardValid1 & !cardValid2 & !cardValid3;
        pkIsValid |= cardValid0 & !cardValid1 & !cardValid2 & !cardValid3;
        pkIsValid |= !cardValid0 & !cardValid1 & !cardValid2 & !cardValid3;
        
        pkIsValid &= componentIsValid(pkTrick, 4*CARD_SIZE, INDEX_SIZE, MAX_INDEX_VALUE);
        return pkIsValid;
    }
    
    private static int createEmptyPacked(int trump, int player, int index) {
        return Bits32.pack(PackedCard.INVALID, CARD_SIZE, PackedCard.INVALID, CARD_SIZE, PackedCard.INVALID, CARD_SIZE, PackedCard.INVALID, CARD_SIZE, index, INDEX_SIZE, player, PLAYER_SIZE, trump, TRUMP_SIZE);
    }
    
    /**
     * Returns the trick corresponding to all 4 cards not played (thus invalid)
     * the index begin 0 (first index) and with correct trump and firstPlayer
     * @param trump
     * @param firstPlayer
     * @return
     */
    public static int firstEmpty(Card.Color trump, PlayerId firstPlayer) {
        return createEmptyPacked(trump.ordinal(), firstPlayer.ordinal(), 0);
    }
    
    /**
     * Returns the trick that corresponds to a trick with index one up the
     * one given in pkTrick, and with 0 cards inside, as well as with the 
     * correct trump and winner
     * Returns invalid if the index indicates that the trick is the last
     * of the turn
     * @param pkTrick
     * @return
     */
    public static int nextEmpty(int pkTrick) {
        if(isLast(pkTrick))
            return INVALID;
        
        int index = index(pkTrick)+1;
        int trump = trump(pkTrick).ordinal();
        int winner = winningPlayer(pkTrick).ordinal();
        return createEmptyPacked(trump, winner, index);
    }
    
    /**
     * 
     * @param pkTrick
     * @return
     */
    public static boolean isLast(int pkTrick) {
        return index(pkTrick) == MAX_INDEX_VALUE;
    }
    
    /**
     * A pkTrick is empty iff there are 0 card in it
     * @param pkTrick
     * @return
     */
    public static boolean isEmpty(int pkTrick) {
        return size(pkTrick) == 0;
    }
    
    /**
     * A pkTrick is full iff there are 4 cards in it
     * @param pkTrick
     * @return
     */
    public static boolean isFull(int pkTrick) {
        return size(pkTrick) == 4;
    }
    
    /**
     * Counts how many cards there are in the pkTrick
     * @param pkTrick
     * @return
     */
    public static int size(int pkTrick) {
        int index = 0;
        for(;PackedCard.isValid(card(pkTrick, index)) & index<=3; index++);
        return index;
    }
    
    /**
     * Gives the player AS A INT 
     * @param pkTrick
     * @return
     */
    public static PlayerId player(int pkTrick, int index) {
        int firstPlayerOrdinal = Bits32.extract(pkTrick, PLAYER_START, PLAYER_SIZE);
        int playerIndex = (firstPlayerOrdinal + index)%PlayerId.COUNT;
        return PlayerId.ALL.get(playerIndex);
    }
    
    /**
     * Returns the trump of the pkTrick
     * @param pkTrick
     * @return
     */
    public static Card.Color trump(int pkTrick) {
        int colorOrdinal = Bits32.extract(pkTrick, TRUMP_START, TRUMP_SIZE);
        return Card.Color.values()[colorOrdinal];
    }
    
    /**
     * Gives the index of the trick
     * @param pkTrick
     * @return
     */
    public static int index(int pkTrick) {
        return Bits32.extract(pkTrick, INDEX_START, INDEX_SIZE);
    }
    
    /**
     * Gives the packed version of the card at index
     * @param pkTrick
     * @param index
     * @return
     */
    public static int card(int pkTrick, int index) {
        return Bits32.extract(pkTrick, CARD_SIZE*index, CARD_SIZE);
    }
    
    /**
     * Get a pkTrick with pkCard instead of the card that was at index given.
     * @param pkTrick
     * @param pkCard
     * @param index
     * @return
     */
    private static int setCardAtIndex(int pkTrick, int pkCard, int index) {
        int existingCardMask = Bits32.mask(index*CARD_SIZE, CARD_SIZE);
        int notMask = ~(existingCardMask);
        int wipedPkTrick = pkTrick & notMask;
        return wipedPkTrick | (pkCard << index*CARD_SIZE);
    }
    
    /**
     * Returns the same pkTrick with a card added at the right sport
     * @param pkTrick
     * @param pkCard
     * @return
     */
    public static int withAddedCard(int pkTrick, int pkCard) {
        return setCardAtIndex(pkTrick, pkCard, size(pkTrick));
    }
    
    /**
     * Get the color of the first card played
     * @param pkTrick
     * @return
     */
    public static Card.Color baseColor(int pkTrick) {
        return PackedCard.color(card(pkTrick, 0));
    }
    
    /**
     * THE FOLLOWING FUNCTION CAN BE BROKEN INTO TWO PARTS FOR BETTER 
     * DESIGN: 
     *  - HAVE A FUNCTION THAT DETERMINES IF THE TRUMP HAS BEEN PLAYED
     *  - HAVE A FUNCTION THAT IS CALLED BY playabledCards(int,long) ONLY IF
     *      THE ABOVE HAS DETERMINED THERE WAS INDEED A TRUMP PLAYED
     * I have decided to keep this design for performance even if it relies on 
     * the fact that we know -1 means no trump played (which is a weird convention). 
     * playableCards(int,long) will be involved in the AI decion-making 
     * thus I want to keep the playableCards(int,long) function the leanest.
     * @param pkTrick
     * @param pkCard
     *  - Returns -1 if no trump card has been played
     *  - Returns the rank ordinal of the best trump card played.
     */
    private static int maxTrumpRankPlayed(int pkTrick) {
        Card.Color trump = trump(pkTrick);
        int maxRankOrdinal = -1;
        // a card that is alway gonna be lesser than a card with trump color
        int pkCardToCompare = PackedCard.pack(trump == Card.Color.CLUB ? Card.Color.DIAMOND : Card.Color.CLUB, Card.Rank.SIX);
        for(int i=0; i<=3; i++) {
            int card = card(pkTrick, i);
            if(PackedCard.isValid(card)) {
                if(PackedCard.color(card) == trump & PackedCard.isBetter(trump, card, pkCardToCompare)) {
                    maxRankOrdinal = PackedCard.rank(card).ordinal();
                }
            }
        }
        return maxRankOrdinal;
    }    
        
    /**
     * Get all the cards that can be played given the current trick
     * and the pkHand (which a set of cards)
     * @param pkTrick
     * @param pkHand
     * @return
     */
    public static long playableCards(int pkTrick, long pkHand) {
        if(size(pkTrick) == 0) 
            return pkHand;
                
        int baseCard = card(pkTrick,0);
        Card.Color baseColor = PackedCard.color(baseCard);
        Card.Color trumpColor = trump(pkTrick);
        long baseColorCards = PackedCardSet.subsetOfColor(pkHand, baseColor);
        long trumpColorCards = PackedCardSet.subsetOfColor(pkHand, trumpColor);
        
        if(PackedCardSet.isEmpty(baseColorCards)) {
            int maxRankOfTrump = maxTrumpRankPlayed(pkTrick);
            if(maxRankOfTrump == -1) {
                return pkHand;
            } else {
                int bestTrump = PackedCard.pack(trumpColor, Card.Rank.values()[maxRankOfTrump]);
                long trumpAboveBest = PackedCardSet.trumpAbove(bestTrump);
                
                long playableTrump = PackedCardSet.intersection(trumpAboveBest, pkHand);
                if(PackedCardSet.isEmpty(playableTrump))
                    playableTrump = trumpColorCards;
                long playableNotTrump = PackedCardSet.difference(pkHand, trumpColorCards);
                
                return PackedCardSet.union(playableTrump, playableNotTrump);
            }
        } else if(baseColor == trumpColor) {
            int jackTrump = PackedCard.pack(trumpColor, Card.Rank.JACK);
            if(PackedCardSet.singleton(jackTrump) == trumpColorCards)
                return pkHand;
        } 
        
        int maxRankOfTrump = maxTrumpRankPlayed(pkTrick);
        if(maxRankOfTrump == -1) {
            return PackedCardSet.union(baseColorCards, trumpColorCards);
        } else {
            int bestTrump = PackedCard.pack(trumpColor, Card.Rank.values()[maxRankOfTrump]);
            long trumpAboveBest = PackedCardSet.trumpAbove(bestTrump);
            
            if(trumpColor == baseColor) 
                baseColorCards = PackedCardSet.EMPTY;
            
            long playableTrumpCards = PackedCardSet.intersection(trumpColorCards, trumpAboveBest);
            if(PackedCardSet.isEmpty(playableTrumpCards))
                playableTrumpCards = trumpColorCards;
            
            return PackedCardSet.union(playableTrumpCards, baseColorCards);
            
        }
    }
    
    /**
     * To get the points of the trick. The points of the trick 
     * is the sum of the points of the cards.
     * There are 5 additional points if the trick was the last
     * of the turn.
     * @param pkTrick
     * @return
     */
    public static int points(int pkTrick) {
        Card.Color trump = trump(pkTrick);
        int points = 0;
        
        for(int i=0; i<=3; i++) {
            int card = card(pkTrick, i);
            if(PackedCard.isValid(card))
                points += PackedCard.points(trump, card);
        }
        
        if(index(pkTrick) == MAX_INDEX_VALUE)
            points += Jass.LAST_TRICK_ADDITIONAL_POINTS;
        
        return points;
    }
    
    /**
     * To get the winning player of the trick.
     * The winner is the one who played the best card of the trick SO FAR.
     * @param pkTrick
     * @return
     */
    public static PlayerId winningPlayer(int pkTrick) {
        Card.Color trump = trump(pkTrick);
        
        int firstPlayer = player(pkTrick, 0).ordinal();
        
        int indexOfBestCard = 0;
        int bestCard = card(pkTrick,indexOfBestCard);
        for(int i=1; i<size(pkTrick);i++) {
            int contenderCard = card(pkTrick, i);
            if(PackedCard.isBetter(trump, contenderCard, bestCard)) {
                indexOfBestCard = i;
                bestCard = contenderCard;
            }
        }
        int playerOrdinal = (firstPlayer+indexOfBestCard)%4;
        
        return PlayerId.ALL.get(playerOrdinal);
    }
    
    /**
     * Lists all the cards
     * @param pkTrick
     * @return
     */
    public static String toString(int pkTrick) {
        String representation = "";
        //adding the index to the rp:
        representation += "Trick " + Integer.toString(PackedTrick.index(pkTrick)) + ", ";
        
        //adding the 
        representation += "Started by " + PackedTrick.player(pkTrick, 0).toString() + ", ";
        //adding the cards played to the rp
        for(int i=0; PackedCard.isValid(card(pkTrick, i)) & i<=3 ; i++)
            representation += PackedCard.toString(card(pkTrick,i))+",";
        return representation.replaceFirst(".$", "");
    }
}
