package ch.epfl.javass;

import ch.epfl.javass.gui.GraphicalPlayerAdapter;
import ch.epfl.javass.net.RemotePlayerServer;
import javafx.application.Application;
import javafx.stage.Stage;

public final class RemoteMain extends Application {
    
    private static final String STARTING_MESSAGE = "La partie commencera à la connexion du client...";
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GraphicalPlayerAdapter player = new GraphicalPlayerAdapter();
        RemotePlayerServer server = new RemotePlayerServer(player);
        
        Thread serverThread = new Thread(() -> {
            server.run();
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        System.out.println(STARTING_MESSAGE);
    }

}
