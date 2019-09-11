package ch.epfl.javass.jass;

public interface Jass {
    // le nombre de cartes dans une main au début d'un tour
    int HAND_SIZE = 9;
    // le nombre de plis dans un tour de jeu
    int TRICKS_PER_TURN = 9;
    // le nombre de points nécessaire à une victoire
    int WINNING_POINTS = 1000;
    // le nombre de points additionnels obtenus par une équipe remportant la totalité des plis d'un tour
    int MATCH_ADDITIONAL_POINTS = 100;
    // le nombre de points additionnels obtenu par l'équipe remportant le dernier pli
    int LAST_TRICK_ADDITIONAL_POINTS = 5;
    // the number of card that are played in a single trick.
    int CARDS_PER_TRICK = 4;
}
