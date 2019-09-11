package ch.epfl.javass.net;

import java.util.HashMap;
import java.util.Map;

import ch.epfl.javass.ai.MctsPlayer;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;

public class RemotePlayerTest {
    public static void main(String[] args) {                        
        Map<PlayerId, Player> players = new HashMap<PlayerId, Player>();
        Map<PlayerId, String> names = new HashMap<PlayerId, String>();
        for(int i=0; i<=3; i++) {
            PlayerId id = PlayerId.ALL.get(i);
            Player player = new RandomPlayer(i);
            players.put(id, player);
            names.put(id, "player "+Integer.toString(i));
        }
        
        players.put(PlayerId.PLAYER_4, new RemotePlayerClient("localhost"));
        names.put(PlayerId.PLAYER_4, "player 4 (remote)");
                
        JassGame game = new JassGame(0, players, names);
                
        while (! game.isGameOver()) {
            System.out.println("beginning of trick");
            game.advanceToEndOfNextTrick();
            System.out.println("----");
        }
    }
}
