package ch.epfl.javass.net;

import ch.epfl.javass.jass.Player;

public class ServerForRemotePlayerTest {
    public static void main(String[] args) {
        Player player4Actual = new RandomPlayer(4);
        RemotePlayerServer player4Server = new RemotePlayerServer(player4Actual);
        player4Server.run();
        
        System.out.println("finished running");
    }
}
