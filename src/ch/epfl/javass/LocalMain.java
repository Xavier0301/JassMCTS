package ch.epfl.javass;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ch.epfl.javass.ai.ImprovedMctsPlayer;
import ch.epfl.javass.ai.MctsPlayer;
import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.jass.JassGame;
import ch.epfl.javass.jass.PacedPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

public final class LocalMain extends Application  {
    static public final int DEFAULT_MCTS_ITERATIONS = 10000;
    static private final String DEFAULT_DISTANT_PLAYER_HOSTNAME = "localhost";
    
    static private final int DEFAULT_ARGUMENT_COUNT = 4;
    static private final int EXTENDED_ARGUMENT_COUNT = 5;
    
    static private final double DEFAULT_PACING_TIME = 2;
    
    static private final String[] DEFAULT_NAMES = {"Aline", "Bastien", "Colette", "David"};
    
    static private final int HUMAN_LOCAL_PLAYER_MAX_COMPONENTS = 2;
    static private final int SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS = 3;
    static private final int IMPROVED_SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS = 4;
    static private final int DISTANT_PLAYER_MAX_COMPONENTS = 3;
    static private final int PLAYER_NAME_COMPONENT_INDEX = 1;
    
    static private final int INTER_TRICK_SLEEP_TIME = 1000;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parameters params = getParameters();
        List<String> rawParams = new ArrayList<String>(params.getRaw());
        
        if(rawParams.size() != DEFAULT_ARGUMENT_COUNT & rawParams.size() != EXTENDED_ARGUMENT_COUNT) {
            // TOO FEW OR TOO MANY ARGUMENTS => EXPLAIN HOW IT WORKS
            handleError(ErrorDescriptions.DEFAULT_EXPLANATIONS);
        }
        
        Random rng=null;
        if(rawParams.size() == EXTENDED_ARGUMENT_COUNT) {
            try {
                long initialSeed = Long.parseLong(rawParams.get(EXTENDED_ARGUMENT_COUNT-1));
                if(initialSeed < 0)
                    handleError(ErrorDescriptions.RANDOM_SEED_ERROR, rawParams.get(EXTENDED_ARGUMENT_COUNT-1));
                rng = new Random(initialSeed);
            } catch(NumberFormatException e) {
                // RANDOM SEED PARAM INVALID, ERROR
                handleError(ErrorDescriptions.RANDOM_SEED_ERROR, rawParams.get(EXTENDED_ARGUMENT_COUNT-1));
            }
            // We don't need the last element anymore.
            rawParams.remove(EXTENDED_ARGUMENT_COUNT-1);
        } else {
            rng = new Random();
        }
        
        long[] randomSeeds = generateRandomSeeds(rng);
        
        Map<PlayerId, Player> players = new EnumMap<PlayerId, Player>(PlayerId.class);
        Map<PlayerId, String> playerNames = new EnumMap<PlayerId, String>(PlayerId.class);
                
        for(int i=0; i<rawParams.size(); i++) {
            PlayerId playerId = PlayerId.ALL.get(i);
            
            ParsingResult result = parseArgument(rawParams.get(i), randomSeeds[i+1], DEFAULT_NAMES[i], playerId);
            
            Player pacedPlayer = new PacedPlayer(result.player, DEFAULT_PACING_TIME);
            
            players.put(playerId, pacedPlayer);
            playerNames.put(playerId, result.name);
        }
        
        Thread gameThread = new Thread(() -> {
            JassGame game = new JassGame(randomSeeds[0], players, playerNames);
            while(!game.isGameOver()) {
                game.advanceToEndOfNextTrick();
                try { Thread.sleep(INTER_TRICK_SLEEP_TIME); } catch (Exception e) {}
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }
    
    long[] generateRandomSeeds(Random rng) {
        long[] seeds = new long[EXTENDED_ARGUMENT_COUNT];
        for(int i=0; i<EXTENDED_ARGUMENT_COUNT; i++) 
            seeds[i] = rng.nextLong();
        return seeds;
    }
    
    ParsingResult parseArgument(String arg, long randomSeed, String defaultName, PlayerId playerId) {
        Player player = null;
        
        String[] components = arg.split(":");
        switch(components[0]) {
            case PlayerTypeIdentifiers.HUMAN_LOCAL:
                if(components.length > HUMAN_LOCAL_PLAYER_MAX_COMPONENTS) {
                    // TOO MANY COMPONENTS, ERROR
                    handleError(ErrorDescriptions.PLAYER_SPECIFICATION_ERROR, arg);
                }
                player = new GraphicalPlayerAdapter();
                
                break;
            case PlayerTypeIdentifiers.SIMULATED_LOCAL:
                if(components.length > SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS) {
                    // TOO MANY COMPONENTS, ERROR
                    handleError(ErrorDescriptions.PLAYER_SPECIFICATION_ERROR, arg);
                }
                int iterations = DEFAULT_MCTS_ITERATIONS;
                if(components.length == SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS) {
                    try {
                        iterations = Integer.parseInt(components[SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS-1]);
                    } catch(NumberFormatException e) {
                        // THE SPECIFIED ITERATIONS IS INVALID, ERROR
                        handleError(ErrorDescriptions.ITERATIONS_SPECIFICATION_ERROR, arg);
                    }
                }
                
                player = new MctsPlayer(playerId, randomSeed, iterations);
                
                break;
            case PlayerTypeIdentifiers.IMPROVED_SIMULATED_LOCAL:
                if(components.length > IMPROVED_SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS) {
                 // TOO MANY COMPONENTS, ERROR
                    handleError(ErrorDescriptions.PLAYER_SPECIFICATION_ERROR, arg);
                }
                
                int iterationsNr = DEFAULT_MCTS_ITERATIONS;
                if(components.length >= IMPROVED_SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS-1) {
                    try {
                        iterationsNr = Integer.parseInt(components[IMPROVED_SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS-2]);
                    } catch(NumberFormatException e) {
                        // THE SPECIFIED ITERATIONS IS INVALID, ERROR
                        handleError(ErrorDescriptions.ITERATIONS_SPECIFICATION_ERROR, arg);
                    }
                }
                
                Integer threads = null;
                if(components.length == IMPROVED_SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS) {
                    try {
                        threads = Integer.parseInt(components[IMPROVED_SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS-1]);
                    } catch(NumberFormatException e) {
                        // THE SPECIFIED ITERATIONS IS INVALID, ERROR
                        handleError(ErrorDescriptions.ITERATIONS_SPECIFICATION_ERROR, arg);
                    }
                }
                
                player = new ImprovedMctsPlayer(playerId, iterationsNr, threads);
                
                break;
            case PlayerTypeIdentifiers.DISTANT:
                if(components.length > DISTANT_PLAYER_MAX_COMPONENTS) {
                    // TOO MANY COMPONENTS, ERROR
                    handleError(ErrorDescriptions.PLAYER_SPECIFICATION_ERROR, arg);
                }
                String hostname = DEFAULT_DISTANT_PLAYER_HOSTNAME;
                if(components.length == DISTANT_PLAYER_MAX_COMPONENTS) {
                    hostname = components[SIMULATED_LOCAL_PLAYER_MAX_COMPONENTS-1];
                }
                
                try {
                    player = new RemotePlayerClient(hostname);
                } catch(UncheckedIOException e) {
                    // PROBLEM CONNECTING TO SERVER, ERROR
                    handleError(ErrorDescriptions.SERVER_CONNECTION_ERROR, hostname);
                }
                
                break;      
            default:
                //THE IDENTIFIER IS NOT VALID, ERROR
                handleError(ErrorDescriptions.PLAYER_IDENTIFIER_ERROR, components[0]);
        }
        
        String name = defaultName;
        if(components.length > PLAYER_NAME_COMPONENT_INDEX) {
            if(!components[PLAYER_NAME_COMPONENT_INDEX].isEmpty())
                name = components[PLAYER_NAME_COMPONENT_INDEX];
        }
        
        return new ParsingResult(player, name);
    }
        
    private class PlayerTypeIdentifiers {
        private static final String HUMAN_LOCAL = "h";
        private static final String SIMULATED_LOCAL = "s";
        private static final String IMPROVED_SIMULATED_LOCAL = "s++";
        private static final String DISTANT = "r";
    }
    
    private class ErrorDescriptions {
        static private final int DEFAULT_ERROR_STATUS_EXIT = 1;
        
        static private final String RANDOM_SEED_ERROR = "The specified random seed (5th parameter) is invalid";
        
        static private final String PLAYER_SPECIFICATION_ERROR = "Player specification is malformed (invalid)";
        
        static private final String ITERATIONS_SPECIFICATION_ERROR = "The specified number ofiterations is invalid";
        
        static private final String SERVER_CONNECTION_ERROR = "Failed to connect to server";
        
        static private final String PLAYER_IDENTIFIER_ERROR = "The specified player identifier is invalid";
        
        static private final String DEFAULT_EXPLANATIONS = "Usage: <player 1> <player 2> <player 3> <player 4> [<random seed>]\n" + 
                "where:\n" + 
                "    - Each player can be specified like: <player type>:[<player name>]:[<additional argument 1>]:[<additional argument 2>]\n" + 
                "    where\n" + 
                "        - <player type> can be h (local human player), s (simulated local player), r (distant player), s++ (improved simulated local player)\n" + 
                "        - <player name> is optional\n" + 
                "        - <additional argument 1> can be the number of iterations for a simulated player (default is 10k), or the hostname of a distant player (default is localhost)\n" + 
                "        - <additional argument 2> can only be the number of threads for an improved simulated player (default depends on the machine). We encourage you to not specify anything.\n" + 
                "    - The random seed is an optional integer\n" + 
                "\n" + 
                "Example: s h:Marie r:Céline:128.178.243.14 s::20000 400\n" + 
                "where\n" + 
                "    - The first player is a simulated local player, with default name and default number of iterations\n" + 
                "    - The second player is a human local player, named Marie.\n" + 
                "    - The third player is a distant player, named Céline and with hostname 128.178.243.14\n" + 
                "    - The fourth player is a simulated local player, with default name and 20k iterations";
    }
    
    private class ParsingResult {
        Player player;
        String name;
        
        public ParsingResult(Player player, String name) {this.player = player; this.name = name; }
    }
    
    private void handleError(String errorMessage) {
        handleError(errorMessage, null);
    }
    
    private void handleError(String errorMessage, String errorSubject) {
        if(errorSubject != null)
            System.out.println(errorMessage + ": " + errorSubject);
        else 
            System.out.println(errorMessage);
        System.exit(ErrorDescriptions.DEFAULT_ERROR_STATUS_EXIT);
    }
}
