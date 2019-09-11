package ch.epfl.javass.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

/**
 * A Bean is a class which attributes are all observable.
 * 
 * Specifically, with HandBean you can observe the handProperty
 * and the playableCardsProperty.
 * 
 * The properties returned by the getters are unmodifiable.
 * 
 * The hand property is a list of fixed size (=Jass.HAND_SIZE=9).
 * What that means is that if you play a card in your hand it becomes
 * null, instead of being removed from the list. 
 * This behavior makes it easier to update the graphics of the game.
 * 
 * The playable cards is a set
 * 
 * @author xavier
 *
 */
public final class HandBean {
    private ObservableList<Card> handProperty;
    private ObservableSet<Card> playableCardsProperty;
    
    /**
     * Initializing the properties of the bean, namely
     * handProperty and playableCardsProperty.
     */
    public HandBean() {
        List<Card> copies = Collections.nCopies(Jass.HAND_SIZE, null);
        handProperty = FXCollections.observableArrayList();
        handProperty.addAll(copies);
        
        playableCardsProperty = FXCollections.observableSet();
    }
    
    private void insertInHandProperty(Card card) {
        for(int i=0; i<Jass.HAND_SIZE; i++) {
            if(handProperty.get(i) == null) {
                handProperty.set(i, card);
                return;
            }
        }
    }
    
    /**
     * Returns the hand property of the bean. The hand property can be listened
     * to, or you can get the underlying List of Cards, but you cannot modify the 
     * underlying List of Cards.
     * It is a list of 9 null cards by default.
     * @return
     */
    public ObservableList<Card> handProperty() { 
        return FXCollections.unmodifiableObservableList(handProperty);
    }
    
    /**
     * The method is responsible for updating correctly the handProperty 
     * of this bean, given the newHand.
     * @param newHand
     */
    public void setHand(CardSet newHand) {
        List<Integer> toRemove = new ArrayList<Integer>();
        for(int i=0; i<handProperty.size(); i++) {
            Card card = handProperty.get(i);
            if(card != null && !newHand.contains(card))
                toRemove.add(i);
        }
        
        for(Integer i: toRemove) {
            handProperty.set(i, null);
        }
        
        for(int i=0; i<newHand.size(); i++) {
            Card card = newHand.get(i);
            if(!handProperty.contains(card)) 
                insertInHandProperty(card);
        }
    }
    
    /**
     * Returns the playable cards property. You can listen to the property,
     * or get the underlying set of cards, but you cannot modify it.
     * It is an empty set by default.
     * @return
     */
    public ObservableSet<Card> playableCardsProperty() { 
        return FXCollections.unmodifiableObservableSet(playableCardsProperty);
    }
    
    /**
     * This method is responsible for updating the playableCardsProperty correctly,
     * given the newPlayableCards.
     * @param newPlayableCards
     */
    public void setPlayableCards(CardSet newPlayableCards) { 
        List<Card> toRemove = new ArrayList<Card>();
        for(Card c: playableCardsProperty) {
            if(!newPlayableCards.contains(c))
                toRemove.add(c);
        }
        
        for(Card c: toRemove)
            playableCardsProperty.remove(c);
        
        for(int i=0; i<newPlayableCards.size(); i++) {
            Card card = newPlayableCards.get(i);
            if(!playableCardsProperty.contains(card))
                playableCardsProperty.add(card);
        }
            
    }
}
