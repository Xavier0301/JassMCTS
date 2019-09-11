package ch.epfl.javass.net;

/**
 * The enum is used in the context of communicating states of a jass game.
 * More specifically, we communicate the informations of the game using different
 * commands, which are listed in this enum.
 * @author xavier
 *
 */
public enum JassCommand {
    PLRS, // set players
    TRMP, // set trump
    HAND, // update hand
    TRCK, // update trick
    CARD, // card to play
    SCOR, // update score
    WINR; // set winning team
}
