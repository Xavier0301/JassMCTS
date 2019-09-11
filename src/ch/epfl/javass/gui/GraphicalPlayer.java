package ch.epfl.javass.gui;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.BlockingQueue;

import ch.epfl.javass.gui.ImageHelper.CardImageRes;
import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TeamId;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * This class handles the graphics of a game, i.e. drawing the game inside a window.
 * It does not handle the creation of such window.
 * 
 * The graphics are updated as the games goes forward, via the use of Beans
 * (HandBean, ScoreBean, TrickBean).
 * 
 * @author xavier
 *
 */
public final class GraphicalPlayer {
    private StackPane stackPane;
    private BorderPane borderPane;
    
    /**
     * The methods is responsible for initiating the graphics of the game, i.e. 
     * laying out informations about the game like the score, the trick and the player's hand.
     * 
     * The graphics is updated thanks to the given scoreBean, trickBean and handBean.
     * 
     * @param playerId id of the player to which the graphics are presented
     * @param players 
     * @param scoreBean
     * @param trickBean
     * @param handBean
     * @param secondaryThread BlockingQueue used to transmit the card a player clicked on.
     */
    public GraphicalPlayer(PlayerId playerId, Map<PlayerId, String> players, ScoreBean scoreBean, TrickBean trickBean, HandBean handBean, BlockingQueue<Card> secondaryThread) {
        stackPane = new StackPane();
        borderPane = new BorderPane();
        
        borderPane.setTop(new ScorePane(players, scoreBean));
        borderPane.setCenter(new TrickPane(trickBean, players, playerId));
        borderPane.setBottom(new HandPane(handBean, secondaryThread));
        
        stackPane.getChildren().add(borderPane);
        stackPane.getChildren().add(new VictoryPane(players, scoreBean));
        
    }
    
    /**
     * Returns the stage that can be shown to the player. 
     * @return
     */
    public Stage createStage() {
        Scene scene = new Scene(stackPane);
        Stage stage = new Stage();
        stage.setTitle("Javass");
        stage.setScene(scene);
        stage.sizeToScene();
        return stage;
    }
    
    private class ScorePane extends GridPane {   
        private static final int MARGIN = 8;
        
        private static final int NAME_COLUMN = 0;
        private static final int TURN_POINTS_COLUMN = 1;
        private static final int ADD_TURN_POINTS_COLUMNS = 2;
        private static final int TOTAL_SEPARATOR_COLUMN = 3;
        private static final int TOTAL_POINTS_COLUMN = 4;
        
        private static final int FIRST_ROW = 0;
        private static final int SECOND_ROW = 1;
        
        /**
         * creates the score pane, which is updated with the correct information
         * thanks to the scoreBean
         * @param players
         * @param scoreBean
         */
        public ScorePane(Map<PlayerId, String> players, ScoreBean scoreBean) {
            // TEAM NAME
            
            Label teamOneLabel = new Label(getTeamName(players, TeamId.TEAM_1));
            Label teamTwoLabel = new Label(getTeamName(players, TeamId.TEAM_2));
            
            this.add(teamOneLabel, NAME_COLUMN, FIRST_ROW);
            this.add(teamTwoLabel, NAME_COLUMN, SECOND_ROW);
            
            // TURN POINTS
            
            Label turnPoints1 = new Label("0");
            Label turnPoints2 = new Label("0");
            
            this.add(turnPoints1, TURN_POINTS_COLUMN, FIRST_ROW);
            this.add(turnPoints2, TURN_POINTS_COLUMN, SECOND_ROW);
            
            Label previousTurnPoints1 = new Label("(+0)");
            Label previousTurnPoints2 = new Label("(+0)");
            
            this.add(previousTurnPoints1, ADD_TURN_POINTS_COLUMNS, FIRST_ROW);
            this.add(previousTurnPoints2, ADD_TURN_POINTS_COLUMNS, SECOND_ROW);
            
            scoreBean.turnPointsProperty(TeamId.TEAM_1).addListener((o, ov, nv) -> {
                turnPoints1.setText(Integer.toString((int) nv));
                previousTurnPoints1.setText(formatPreviousTurnPoints((int) ov, (int) nv));
                
            });
            scoreBean.turnPointsProperty(TeamId.TEAM_2).addListener((o, ov, nv) -> {
                turnPoints2.setText(Integer.toString((int) nv));
                previousTurnPoints2.setText(formatPreviousTurnPoints((int) ov, (int) nv));
            });
            
            // SEPARATOR 
            
            Label totalSeparator1 = new Label("/Total:");
            Label totalSeparator2 = new Label("/Total:");
            
            this.add(totalSeparator1, TOTAL_SEPARATOR_COLUMN, FIRST_ROW);
            this.add(totalSeparator2, TOTAL_SEPARATOR_COLUMN, SECOND_ROW);
            
            // TOTAL POINTS
            
            Label totalPoints1 = new Label("0");
            Label totalPoints2 = new Label("0");
            
            this.add(totalPoints1, TOTAL_POINTS_COLUMN, FIRST_ROW);
            this.add(totalPoints2, TOTAL_POINTS_COLUMN, SECOND_ROW);
            
            scoreBean.totalPointsProperty(TeamId.TEAM_1).addListener((o, ov, nv) -> {
                totalPoints1.setText(Integer.toString((int) nv));
            });
            scoreBean.totalPointsProperty(TeamId.TEAM_2).addListener((o, ov, nv) -> {
                totalPoints2.setText(Integer.toString((int) nv));
            });
            
            this.setStyle("-fx-font: 16 Optima;\n" + 
                    "-fx-background-color: lightgray;\n" + 
                    "-fx-padding: 5px;\n" + 
                    "-fx-alignment: center;");
            this.setVgap(MARGIN);
            this.setHgap(MARGIN);
        }
        
        private String formatPreviousTurnPoints(int oldValue, int newValue) {
            if(newValue-oldValue < 0)
                return "(+0)";
            return "(+" + Integer.toString(newValue-oldValue) +")";
        }
        
        private String getTeamName(Map<PlayerId, String> players, TeamId teamId) {
            String[] names = new String[2];
            int i=0;
            for(PlayerId playerId: PlayerId.ALL) {
                if(playerId.team() == teamId) {
                    names[i] = players.get(playerId);
                    i++;
                }
            }
            
            return (String.join(" et ", names) + ":");
        }
    }
    
    /**
     * Each child of this trick pane (except the center child, which is the trump image view),
     * has a very specific layout:
     * VBox |- StackPane |- ImageView
     *      |            |- Rectangle (overlay)
     *      |- Text
     * 
     * Also, the text and the stack pane order can be inverted in the list of children 
     * of the vbox, the reason as to why being that the order depends on the position.
     * 
     * @author xavier
     *
     */
    private class TrickPane extends GridPane {
        private static final int CARD_HEIGHT = 180;
        private static final int CARD_WIDTH = 120;
        
        private static final int TRUMP_HEIGHT = 101;
        private static final int TRUMP_WIDTH = 101;
        
        private static final int OVERLAY_BLUR_RADIUS = 4;
        private static final int VBOX_SPACING = 5;
        
        private Map<PlayerId, int[]> positions;
        private Map<PlayerId, VBox> cardVBoxes;
        
        private ObservableMap<PlayerId, Card> trickProperty;
        
        /**
         * Creates the trick pane, which is updated correctly with the trickBean.
         * The layout is such that the card of the ownId player is represented at the bottom.
         * @param trickBean
         * @param players
         * @param ownId
         */
        public TrickPane(TrickBean trickBean, Map<PlayerId, String> players, PlayerId ownId) {
            // CARDS: CARD VIEWS
            positions = new EnumMap<PlayerId, int[]>(PlayerId.class);
            computePositions(ownId);
            
            // see the class javadoc to understand how we layout each element in the grid pane.
            cardVBoxes = new EnumMap<PlayerId, VBox>(PlayerId.class);
            for(PlayerId p: PlayerId.ALL) {
                ImageView imageView = new ImageView();
                imageView.setFitHeight(CARD_HEIGHT);
                imageView.setFitWidth(CARD_WIDTH);
                
                Text playerName = new Text(players.get(p));
                playerName.setStyle("-fx-font: 14 Optima;");
                
                StackPane cardStackPane = new StackPane();
                cardStackPane.getChildren().addAll(imageView, generateCardOverlay());
                
                VBox cardView = new VBox(VBOX_SPACING);
                cardView.setAlignment(Pos.CENTER);
                if(p == ownId)
                    cardView.getChildren().addAll(cardStackPane, playerName);
                else 
                    cardView.getChildren().addAll(playerName, cardStackPane);
                
                
                cardVBoxes.put(p, cardView);
            }
              
            trickProperty = trickBean.trickProperty();
            trickProperty.addListener(new MapChangeListener<PlayerId, Card>() {
                @Override
                public void onChanged(Change<? extends PlayerId, ? extends Card> change) {
                    Card changeCard = change.getValueAdded(); // it would never be a value remove in that scenario
                    ImageView cardView = getCardImageView(change.getKey(), ownId);
                    if(changeCard != null) {
                        Image image = ImageHelper.getCardImage(changeCard, CardImageRes.MEDIUM);
                        cardView.setImage(image);
                    } else {
                        cardView.setImage(null);
                    }
                }
            });
            
            // CARDS: BEST CARD SO FAR INDICATOR
            
            trickBean.winningPlayerProperty().addListener(new ChangeListener<PlayerId>() {
                @Override
                public void changed(ObservableValue<? extends PlayerId> observable, PlayerId oldValue, PlayerId newValue) {
                    if(oldValue != null | newValue == null) 
                        setOverlayVisibilityAt(oldValue, ownId, false);
                    if(newValue != null)
                        setOverlayVisibilityAt(newValue, ownId, true);
                }
            });
            
            cardVBoxes.forEach((playerId, vBox) -> {
                int[] pos = positions.get(playerId);
                this.add(vBox, pos[0], pos[1], pos[2], pos[3]);
//                this.setHalignment(vBox, HPos.CENTER);
            });
            
            // TRUMP
            ImageView trumpView = new ImageView();
            trumpView.setFitHeight(TRUMP_HEIGHT);
            trumpView.setFitWidth(TRUMP_WIDTH);
                        
            trickBean.trumpProperty().addListener(new ChangeListener<Card.Color>() {
                @Override
                public void changed(ObservableValue<? extends Color> observable, Color oldValue, Color newValue) {
                    Image newImage = ImageHelper.getTrumpImage(newValue);
                    trumpView.setImage(newImage);
                }
                
            });
            
            this.add(trumpView, 1, 1);
            this.setHalignment(trumpView, HPos.CENTER);
            
            this.setStyle("-fx-background-color: whitesmoke;\n" + 
                    "-fx-padding: 5px;\n" + 
                    "-fx-border-width: 3px 0px;\n" + 
                    "-fx-border-style: solid;\n" + 
                    "-fx-border-color: gray;\n" + 
                    "-fx-alignment: center;");
            
            this.setAlignment(Pos.CENTER);
        }
        
        /**
         * The ownId card should be shown at the bottom
         * This method computes positions is such a way that
         * the card of ownId is shown at the bottom
         * @param ownId
         */
        private void computePositions(PlayerId ownId) {
            // starting for the bottom, i.e. the card that the ownId
            // player should be at.
            List<int[]> orderedPositions = new ArrayList<int[]>();
            orderedPositions.add(new int[] {1,2,1,1}); // bottom card
            orderedPositions.add(new int[] {2,0,1,3}); // right card
            orderedPositions.add(new int[] {1,0,1,1}); // top card
            orderedPositions.add(new int[] {0,0,1,3}); // left card
            
            for(int i = 0; i<PlayerId.COUNT; i++) {
                int adjustedIndex = (i+ownId.ordinal())%4;
                PlayerId player = PlayerId.ALL.get(adjustedIndex);
                positions.put(player, orderedPositions.get(i));
            }
        }
        
        private ImageView getCardImageView(PlayerId player, PlayerId ownId) {
            VBox vbox = cardVBoxes.get(player);
            StackPane stack;
            if(player == ownId)
                stack = (StackPane) vbox.getChildren().get(0);
            else 
                stack = (StackPane) vbox.getChildren().get(1);
            return (ImageView) stack.getChildren().get(0);
        }
        
        private Rectangle getCardOverlay(PlayerId player, PlayerId ownId) {
            VBox vbox = cardVBoxes.get(player);
            StackPane stack;
            if(player == ownId)
                stack = (StackPane) vbox.getChildren().get(0);
            else 
                stack = (StackPane) vbox.getChildren().get(1);
            return (Rectangle) stack.getChildren().get(1);
        }
        
        private Rectangle generateCardOverlay() {
            Rectangle overlay = new Rectangle();
            overlay.setHeight(CARD_HEIGHT);
            overlay.setWidth(CARD_WIDTH);
            
            overlay.setStyle("-fx-arc-width: 20;\n" + 
                    "-fx-arc-height: 20;\n" + 
                    "-fx-fill: transparent;\n" + 
                    "-fx-stroke: lightpink;\n" + 
                    "-fx-stroke-width: 5;\n" + 
                    "-fx-opacity: 0.5;");
            
            overlay.setEffect(new GaussianBlur(OVERLAY_BLUR_RADIUS));
            
            overlay.setVisible(false);
            
            return overlay;
        }
        
        private void setOverlayVisibilityAt(PlayerId playerId, PlayerId ownId, boolean isVisible) {
            Rectangle overlay = getCardOverlay(playerId, ownId);
            overlay.setVisible(isVisible);
        }
        
    }
    
    private class VictoryPane extends BorderPane {
        private Text text;
        private Map<TeamId, String> teamNames;
        
        private int team1Score=0;
        private int team2Score=0;
        
        /**
         * Creates the victory pane, which shows once there is a winner (scoreBean.winningTeamProperty)
         * @param players
         * @param scoreBean
         */
        public VictoryPane(Map<PlayerId, String> players, ScoreBean scoreBean) {
            teamNames = new EnumMap<TeamId, String>(TeamId.class);
            
            computeTeamNames(players);
            
            text = new Text();
            this.setCenter(text);
            this.setVisible(false);
            
            scoreBean.totalPointsProperty(TeamId.TEAM_1).addListener((o, oV, nV) -> team1Score = (int) nV);
            scoreBean.totalPointsProperty(TeamId.TEAM_2).addListener((o, oV, nV) -> team2Score = (int) nV);
            
            scoreBean.winningTeamProperty().addListener(new ChangeListener<TeamId>() {
                @Override
                public void changed(ObservableValue<? extends TeamId> observable, TeamId oldValue, TeamId newValue) {
                    text.setText(getCongratulationText(newValue));
                    setVisible(true);
                }
            });
            
            this.setStyle("-fx-font: 16 Optima;\n" + 
                    "-fx-background-color: white;");
        }
        
        private String getCongratulationText(TeamId winningTeam) {
            int winnerScore = team1Score;
            int loserScore = team1Score;
            if(team1Score >= team2Score) 
                loserScore = team2Score;
            else 
                winnerScore = team2Score;
            
            String teamName = teamNames.get(winningTeam);
            
            return teamName + " ont gagne avec avec " + Integer.toString(winnerScore) + " points contre " + Integer.toString(loserScore) + ".";
        }
        
        private void computeTeamNames(Map<PlayerId, String> players) {
            for(TeamId id: TeamId.ALL) 
                teamNames.put(id, getTeamName(id, players));
        }
        
        private String getTeamName(TeamId teamId, Map<PlayerId, String> players) {
            List<String> componentsList = new ArrayList<String>();
            players.forEach((playerId, name) -> {
                if(playerId.team() == teamId) 
                    componentsList.add(name);
            });
            return String.join(" et ", componentsList);
        }
        
    }
    
    /**
     * The layout of HandPane is very basic: it has 9 ImageView children.
     * When a card is removed from the hand, the concerned ImageView 
     * gets a null .image
     * @author xavier
     *
     */
    private class HandPane extends HBox {
        private static final int CARD_HEIGHT = 120;
        private static final int CARD_WIDTH = 80;
        
        private static final double UNPLAYABLE_OPACITY = 0.2;
        private static final double PLAYABLE_OPACITY = 1;
        
        private ImageView[] handCards;
        private Card[] correspondingCards;
        
        private ObservableList<Card> handProperty;
        private ObservableSet<Card> playableCardsProperty;
        
        /**
         * Creates the hand pane, which is updated correctly thanks to the handBean.
         * When the user clicks on a card, this card is added to the given blockingQueue.
         * @param handBean
         * @param blockingQueue
         */
        public HandPane(HandBean handBean, BlockingQueue<Card> blockingQueue) {
            handCards = new ImageView[Jass.HAND_SIZE];
            correspondingCards = new Card[Jass.HAND_SIZE];
            for(int i=0; i<Jass.HAND_SIZE; i++) {
                correspondingCards[i] = null;
                
                ImageView cardView = new ImageView();
                cardView.setFitHeight(CARD_HEIGHT);
                cardView.setFitWidth(CARD_WIDTH);
                cardView.setOpacity(UNPLAYABLE_OPACITY);
                
                cardView.setOnMouseClicked((e) -> {
                    blockingQueue.add(getCardForImageView(cardView));
                });
                
                handCards[i] = cardView;
            }
            
            // we have to store the hand property, as it is a FXCollections.unmodifiable...
            // the object that is return by FXCollections makes listeners weak listeners,
            // which means they are cleared by the GC once no reference is counted.
            // It is the case when this method ends.
            handProperty = handBean.handProperty();
            handProperty.addListener(new ListChangeListener<Card>() {
                @Override
                public void onChanged(Change<? extends Card> c) {
                    while(c.next()) {
                        for(int i=c.getFrom(); i<c.getTo(); i++) {
                            Card cardChanged = c.getList().get(i);
                            if(cardChanged == null) {
                                handCards[i].setImage(null);
                                correspondingCards[i] = null;
                            } else {
                                Image image = ImageHelper.getCardImage(cardChanged, CardImageRes.SMALL);
                                handCards[i].setImage(image);
                                correspondingCards[i] = cardChanged;
                            }
                        }
                    }
                }
            });
            
            playableCardsProperty = handBean.playableCardsProperty();
            playableCardsProperty.addListener(new SetChangeListener<Card>() {
                @Override
                public void onChanged(Change<? extends Card> change) {
                    Card element;
                    ImageView correspondingView;
                    if(change.wasRemoved()) {
                        element = change.getElementRemoved();
                        correspondingView = getImageViewForCard(element);
                        // the correspondingView can be null is the card was just played
                        if(correspondingView != null) {
                            correspondingView.setOpacity(UNPLAYABLE_OPACITY);
                            correspondingView.setDisable(true);
                        }
                    } else {
                        element = change.getElementAdded();
                        correspondingView = getImageViewForCard(element);
                        // the correspondingView can be null is the card was just played
                        if(correspondingView != null) {
                            correspondingView.setOpacity(PLAYABLE_OPACITY);
                            correspondingView.setDisable(false);
                        }
                    }
                }
            });
            
            this.getChildren().addAll(handCards);
            
            this.setStyle("-fx-background-color: lightgray;\n" + 
                    "-fx-spacing: 5px;\n" + 
                    "-fx-padding: 5px;");
        }
        
        private ImageView getImageViewForCard(Card c) {
            for(int i=0; i<correspondingCards.length; i++) {
                if(c.equals(correspondingCards[i]))
                    return handCards[i];
            }
            return null;
        }
        
        private Card getCardForImageView(ImageView view) {
            for(int i=0; i<handCards.length; i++) {
                if(view == handCards[i])
                    return correspondingCards[i];
            }
            return null;
        }
    }
}
