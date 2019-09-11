package ch.epfl.javass.net;


import java.util.Random;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.TurnState;

public class RandomPlayer implements Player {
    
    Random rng;
    
    public RandomPlayer(int rngSeed) {
        this.rng = new Random(rngSeed);
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        CardSet playableInHand = state.trick().playableCards(hand);
        int toPlayIndex = rng.nextInt(playableInHand.size());
        return playableInHand.get(toPlayIndex);
    }
    
}
