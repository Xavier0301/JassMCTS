package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import javafx.scene.image.Image;

/**
 * The names of the image for a given cards is formatted in 
 * a non-trivial way. 
 * 
 * ImageHelper is used to facilitate the process of getting the image
 * for a given card. It only has static methods.
 * 
 * It is non-instantiable.
 * 
 * @author xavier
 *
 */
public final class ImageHelper {
    /**
     * The CardImageRes describes the resolutions a card image can have
     * In particular, we have two sizes available as artworks.
     * 
     * Each enum comes with a specified with. The reason as to why 
     * is that the names of the images contain the width, so having
     * this information helps to get the image.
     * 
     * @author xavier
     *
     */
    static public enum CardImageRes {
        SMALL(160),
        MEDIUM(240);
        
        private int width;
        
        private CardImageRes(int width) {
            this.width = width;
        }
        
        /**
         * To get the width corresponding to the given image resolution
         * @return
         */
        public int getWidth() {
            return width;
        }
    }
    
    private ImageHelper() {};
    
    // The images are stored at the given path.
    // The path is relative to the project
    static public String PATH = "/images/";
    
    /**
     * Gives the artwork corresponding to the given card and resolution.
     * If the resolution is SMALL, it gives a 160*240 image
     * If the resolution is MEDIUM, it gives a 240*360 images
     * @param card
     * @param res
     * @return
     */
    static public Image getCardImage(Card card, CardImageRes res) {
        return new Image(PATH + getImageName(card, res));
    }
    
    /**
     * Gives the images name corresponding to the card and the res
     * @param card
     * @param res
     * @return
     */
    static private String getImageName(Card card, CardImageRes res) {
        String c = Integer.toString(card.color().ordinal());
        String r = Integer.toString(card.rank().ordinal());
        String w = Integer.toString(res.getWidth());
        return "/card_"+c+"_"+r+"_"+w+".png" ;
    }
    
    /**
     * Gives the trump image of a given color. 
     * It comes in a unique size: 202*202
     * @param color
     * @return
     */
    static public Image getTrumpImage(Card.Color color) {
        String c = Integer.toString(color.ordinal());
        return new Image(PATH + "trump_"+c+".png");
    }
    
} 
