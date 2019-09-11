package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.Jass;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;
import javafx.collections.FXCollections;

/**
 * A Bean is a class which attributes are all observable.
 * 
 * Specifically, the attributes of the TrickBean are the trump,
 * the trick and the winning player, presented in a form 
 * which is easily interpretable by the graphics class.
 * 
 * @author xavier
 *
 */
public final class TrickBean {
    private ObjectProperty<Color> trumpProperty;
    private ObservableMap<PlayerId, Card> trickProperty;
    private ObjectProperty<PlayerId> winningPlayerProperty;
    
    /**
     * Initiates the values of the bean.
     * 
     * The trump and winning player are null by default.
     * The trick property is a complete map which values are null.
     */
    public TrickBean() {
        trumpProperty = new SimpleObjectProperty<Color>(null);
        
        trickProperty = FXCollections.observableHashMap();
        for(PlayerId id: PlayerId.ALL)
            trickProperty.put(id, null);
        
        winningPlayerProperty = new SimpleObjectProperty<PlayerId>(null);
    }
    
    /**
     * Gives the trick, in an unmodifiable form. 
     * It is the map [[PLAYER_1: null], ..., [PLAYER_4: null]]
     * by default.
     * @return
     */
    public ObservableMap<PlayerId, Card> trickProperty() { 
        return FXCollections.unmodifiableObservableMap(trickProperty);
    } 
    
    /**
     * To set the trick property correctly given the newTrick
     * @param newTrick
     */
    public void setTrick(Trick newTrick) { 
        trumpProperty.set(newTrick.trump());
        if(!newTrick.isEmpty())
            winningPlayerProperty.set(newTrick.winningPlayer());
        else
            winningPlayerProperty.set(null);
        
        int size = newTrick.size();
        for(int i=0; i<size; i++)
            trickProperty.replace(newTrick.player(i), newTrick.card(i));
        for(int i=size; i<Jass.CARDS_PER_TRICK; i++)
            trickProperty.replace(newTrick.player(i), null);
    }
    
    /**
     * Gives the trump property, in an unmodifiable form. 
     * By default it is null.
     * @return
     */
    public ReadOnlyObjectProperty<Color> trumpProperty() {
        return trumpProperty;
    }
    
    /**
     * To set the trump property.
     * @param newTrump
     */
    public void setTrump(Color newTrump) {
        trumpProperty.set(newTrump);
    }
    
    /**
     * Gives the winning player, in an unmodifiable form.
     * It is null by default.
     * @return
     */
    public ReadOnlyObjectProperty<PlayerId> winningPlayerProperty() {
        return winningPlayerProperty;
    }
    
    /**
     * To set the winning player.
     * @param winningPlayer
     */
    public void setWinningPlayer(PlayerId winningPlayer) {
        winningPlayerProperty.set(winningPlayer);
    }
}
