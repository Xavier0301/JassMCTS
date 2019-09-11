package ch.epfl.javass.net;

import static java.nio.charset.StandardCharsets.US_ASCII;

import static ch.epfl.javass.Preconditions.checkArgument;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;

/**
 * This class describes a server, which is responsible for insuring communications
 * between the underlying player and a client, which is connected remotely to the 
 * server.
 * 
 * More specifically, the server and the client communicate via the Javass
 * communication protocol, which is built on top of TCP/IP.
 * 
 * The communication is initiated by the client, which communicates the states of the game
 * like the players, the current trump, the current trump etc. All the commands are listed
 * in JassCommand.java
 * 
 * The only instance where the server communicates back to the client is when
 * the client asks about the card to play. 
 * @author xavier
 *
 */
public final class RemotePlayerServer {
    private final Player underlyingPlayer;
    
    private ServerSocket serverSocket=null;
    private Socket socket=null;
    private BufferedReader reader=null;
    private BufferedWriter writer=null;
    
    // default port of the server socket.
    public static final int DEFAULT_PORT = 5108;
    
    /**
     * The remote player server is responsible for keeping the given
     * player up to date. 
     * 
     * The server is updating the player with informations communicated 
     * by the client, like the players in the game, the trump of the trick,
     * the state of the trick etc. 
     * 
     * @param playerToUpdate
     */
    public RemotePlayerServer(Player playerToUpdate) {
        this.underlyingPlayer = playerToUpdate;
    }
    
    /**
     * This method makes the server start listening to informations
     * that the client could have communicated it.
     * 
     * It parses the raw input to actual commands specified by
     * the Javass communication protocol.
     * 
     * The method should be run from a separate thread as it stops
     * till it receives a message.
     * 
     */
    public void run() {
        try {
            this.serverSocket = new ServerSocket(DEFAULT_PORT);
            this.socket = serverSocket.accept();
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), US_ASCII));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), US_ASCII));
            
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
        while(true) {
            try {                
                String optionalWrite = updatePlayer(getMessage());
                if(optionalWrite != null) {
                    writeMessage(optionalWrite);
                }
            
//            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), US_ASCII));
            } catch(IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
    
    /**
     * The thread stops here till the client sends a message
     * Specifically, reader.readLine() is not executed until
     * a message is received.
     * @return
     * @throws IOException
     */
    private String getMessage() throws IOException {
        return reader.readLine();
    }
    
    private void writeMessage(String message) throws IOException {
        writer.write(message);
        writer.write('\n');
        writer.flush();
    }
    
    private JassCommand getCommand(String commandLitteral) {
        checkArgument(commandLitteral.length() == 4);
        return JassCommand.valueOf(commandLitteral);
    }
    
    /**
     * Method reponsible for parsing the raw message, and interpreting it
     * for updating the player, or for asking the player to communicate 
     * the card it wants to play.
     * @param message
     * @return
     */
    private String updatePlayer(String message) {
        String[] components = message.split(" ");
        //the server does not expect to receive a non command message
        if(components.length <= 1)
            return null;
        
        JassCommand command = getCommand(components[0]);
        
        switch(command) {
        case PLRS:
            int ownIdOrdinal = Integer.parseInt(components[1]);
            PlayerId ownId = PlayerId.ALL.get(ownIdOrdinal);
            
            Map<PlayerId, String> names = StringSerializer.deserializeNameMap(components[2]);
            
            underlyingPlayer.setPlayers(ownId, names);
            
            break;
        case TRMP:
            int trumpOrdinal = Integer.parseInt(components[1]);
            Card.Color trump = Card.Color.ALL.get(trumpOrdinal);
            
            underlyingPlayer.setTrump(trump);
            
            break;
        case HAND:
            long packedHand1 = StringSerializer.deserializeLong(components[1]);
            CardSet hand1 = CardSet.ofPacked(packedHand1);
            
            underlyingPlayer.updateHand(hand1);
            
            break;
        case TRCK:
            int packedTrick = StringSerializer.deserializeInt(components[1]);
            Trick trick = Trick.ofPacked(packedTrick);
            
            underlyingPlayer.updateTrick(trick);
            
            break;
        case CARD:
            TurnState state = StringSerializer.deserializeTurnState(components[1]);
            
            long packedHand2 = StringSerializer.deserializeLong(components[2]);
            CardSet hand2 = CardSet.ofPacked(packedHand2);
            
            Card toPlay = underlyingPlayer.cardToPlay(state, hand2);
            
            return StringSerializer.serializeInt(toPlay.packed());
            
        case SCOR:
            long packedScore = StringSerializer.deserializeLong(components[1]);
            Score score = Score.ofPacked(packedScore);
            
            underlyingPlayer.updateScore(score);
            
            break;
        case WINR:
            int teamOrdinal = Integer.parseInt(components[1]);
            TeamId winningTeam = TeamId.ALL.get(teamOrdinal);
            
            underlyingPlayer.setWinningTeam(winningTeam);
            break;
        }
        
        return null;
    }
}
