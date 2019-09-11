package ch.epfl.javass.net;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.Trick;
import ch.epfl.javass.jass.TurnState;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Map;

/**
 * This class represents a player which is located in a remote location,
 * i.e. on a server. 
 * 
 * Each method of a Player are overriden to communicate with the server
 * corresponding to the specified host name.
 * @author xavier
 *
 */
public final class RemotePlayerClient implements AutoCloseable, Player {
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    
    /**
     * Initiates a player which actual player is located on a remote server.
     * This player is communicating with the server for keeping the remote player
     * up to date and to telling the game which card it wants to play.
     * 
     * @param hostName it can be "localhost" or an IP address like "192.168.0.1"
     */
    public RemotePlayerClient(String hostName) {
        try {
            this.socket = new Socket(hostName, RemotePlayerServer.DEFAULT_PORT);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), US_ASCII));
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), US_ASCII));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private void write(String message) {
        try {
            writer.write(message);
            writer.write('\n');
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    
    private String read() {
        try {
            return reader.readLine();
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * close() is specified in AutoCloseable. 
     */
    @Override
    public void close() throws Exception {
        reader.close();
        writer.close();
        socket.close();
    }
    
    /**
     * This is used to create well formatted messages to send.
     * @param command
     * @param components
     * @return
     */
    private String assemble(JassCommand command, String... components) {
        String data = String.join(" ", components);
        return String.join(" ", command.name(), data);
    }

    /**
     * cardToPlay(_,_) is specified in Player.java
     */
    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        String stateSerialized = StringSerializer.serializeTurnState(state);
        String handSerialized = StringSerializer.serializeLong(hand.packed());
        String message = assemble(JassCommand.CARD, stateSerialized, handSerialized);
        write(message);
        int packedCard = StringSerializer.deserializeInt(read());
        return Card.ofPacked(packedCard);
    }
    
    /**
     * A method called at the beginning of each game to set the id of the player
     * and tell the player about the names assigned to everyone (playerNames)
     * @param ownId
     * @param playerNames
     */
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        String idSerialized = Integer.toString(ownId.ordinal());
        String namesSerialized = StringSerializer.serializeNameMap(playerNames);
        
        String message = assemble(JassCommand.PLRS, idSerialized, namesSerialized);
        write(message);
    }
    
    /**
     * Called each time the hand of a player changes.
     * It can be called at the beginning of the turn (where new cards are given)
     * Or each time the player plays a cards.
     * @param newHand
     */
    public void updateHand(CardSet newHand) {
        String handSerialized = StringSerializer.serializeLong(newHand.packed());
        
        String message = assemble(JassCommand.HAND, handSerialized);
        write(message);
    }
    
    /**
     * Called each time a new  trump is set.
     * @param trump
     */
    public void setTrump(Card.Color trump) {
        String trumpSerialized = Integer.toString(trump.ordinal());
        
        String message = assemble(JassCommand.TRMP, trumpSerialized);
        write(message);
    }
    
    /**
     * Called each time the trick has changed
     * i.e. each time a card is played
     * or each time a trick is collected
     * @param newTrick
     */
    public void updateTrick(Trick newTrick) {
        String trickSerialized = StringSerializer.serializeInt(newTrick.packed());
        
        String message = assemble(JassCommand.TRCK, trickSerialized);
        write(message);
    }
    
    /**
     * Called each time the score is updated
     * i.e. each time a trick is collected
     * @param score
     */
    public void updateScore(Score score) {
        String scoreSerialized = StringSerializer.serializeLong(score.packed());
        
        String message = assemble(JassCommand.SCOR, scoreSerialized);
        write(message);
    }
    
    /**
     * Called as soon a team has more than 1000 points
     * @param winningTeam
     */
    public void setWinningTeam(TeamId winningTeam) {
        String teamSerialized = Integer.toString(winningTeam.ordinal());
        
        String message = assemble(JassCommand.WINR, teamSerialized);
        write(message);
    }
    
}