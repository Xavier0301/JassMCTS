package ch.epfl.javass.net;

import static ch.epfl.test.TestRandomizer.RANDOM_ITERATIONS;
import static ch.epfl.test.TestRandomizer.newRandom;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PackedScore;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;
import ch.epfl.javass.jass.TurnState;

public class StringSerializerTest {
    @Test
    void intSerializationWorks() {
        SplittableRandom rng = newRandom();
        for(int i=0; i<RANDOM_ITERATIONS; i++) {
            int toSerialize = rng.nextInt();
            String serialized = StringSerializer.serializeInt(toSerialize);
            int deserialized = StringSerializer.deserializeInt(serialized);
            
            assertEquals(deserialized, toSerialize);
            assertEquals(serialized, Integer.toUnsignedString(toSerialize, 16));
        }
    }
    
    @Test
    void longSerializationWorks() {
        SplittableRandom rng = newRandom();
        for(int i=0; i<RANDOM_ITERATIONS; i++) {
            long toSerialize = rng.nextLong();
            String serialized = StringSerializer.serializeLong(toSerialize);
            long deserialized = StringSerializer.deserializeLong(serialized);
            
            assertEquals(deserialized, toSerialize);
            assertEquals(serialized, Long.toUnsignedString(toSerialize, 16));
        }
    }
    
    String getRandomString(SplittableRandom rng, int lowerBound) {
        String possibilities = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghifjklmnopqrstuvwxyz1234567890";
        int length = rng.nextInt(lowerBound, 20);
        String randomString = "";
        for(int i=0; i<length; i++) {
            int randomIndex = rng.nextInt(possibilities.length());
            randomString += possibilities.charAt(randomIndex);
        }
        return randomString;
    }
    
    @Test
    void stringSerializationWorks() {
        SplittableRandom rng = newRandom();
        for(int i=0; i<RANDOM_ITERATIONS; i++) {
            String toSerialize = getRandomString(rng, 0);
            String serialized = StringSerializer.serializeString(toSerialize);
            String deserialized = StringSerializer.deserializeString(serialized);
            
            String expectedSerialized = Base64.getEncoder().encodeToString(toSerialize.getBytes());
            
            assertEquals(deserialized, toSerialize);
            assertEquals(serialized, expectedSerialized);
        }
    }
    
    private static TeamId nextTeamId(SplittableRandom rng) {
        return TeamId.ALL.get(rng.nextInt(TeamId.COUNT));
    }
    
    private static Score nextScore(SplittableRandom rng) {
        Score s = Score.INITIAL;
        for (int i = 0; i < rng.nextInt(5); ++i) {
            TeamId t = nextTeamId(rng);
            s = s.withAdditionalTrick(t, rng.nextInt(20));
        }
        return s;
    }
    
    TurnState getRandomInitial(SplittableRandom rng) {
        int trumpOrdinal = rng.nextInt(Card.Color.COUNT);
        Card.Color trump = Card.Color.ALL.get(trumpOrdinal);
        
        Score score = nextScore(rng);
        
        int playerOrdinal = rng.nextInt(PlayerId.COUNT);
        PlayerId firstPlayer = PlayerId.ALL.get(playerOrdinal);
        
        return TurnState.initial(trump, score, firstPlayer);
    }
    
    @Test
    void turnStateSerializationWorks() {
        SplittableRandom rng = newRandom();
        
        TurnState turn = getRandomInitial(rng);
        CardSet remaining = CardSet.ALL_CARDS;
        
        for(int i=0; i<RANDOM_ITERATIONS; i++) {
            String serialized = StringSerializer.serializeTurnState(turn);
            TurnState deserialized = StringSerializer.deserializeTurnState(serialized);
            
            long score = turn.score().packed();
            long unplayed = turn.unplayedCards().packed();
            int trick = turn.trick().packed();
            String scoreSerialized = StringSerializer.serializeLong(score);
            String unplayedSerialized = StringSerializer.serializeLong(unplayed);
            String trickSerialized = StringSerializer.serializeInt(trick);
            String expectedSerialized = scoreSerialized + "," + unplayedSerialized + "," + trickSerialized;
              
            // can't directly compare TurnState as the .equals(Obj) as not been overriden
            // it thus compares the adresse rendering the test useless.
            // another option was to override .equals(Obj) is TurnState.
            assertEquals(deserialized.score(), turn.score());
            assertEquals(deserialized.unplayedCards(), turn.unplayedCards());
            assertEquals(deserialized.trick(), turn.trick());
            assertEquals(serialized, expectedSerialized);
            
            int flush = rng.nextInt(20);
            if(flush == 0) {
                turn = getRandomInitial(rng);
                remaining = CardSet.ALL_CARDS;
            } else {
                int toPlayIndex = rng.nextInt(remaining.size());
                Card toPlay = remaining.get(toPlayIndex);
                
                remaining.remove(toPlay);
                turn.withNewCardPlayedAndTrickCollected(toPlay);
            }
        }
    }
    
    @Test
    void nameMapSerializationWorks() {
        SplittableRandom rng = newRandom();
        for(int i=0; i<RANDOM_ITERATIONS; i++) {
            Map<PlayerId, String> names = new HashMap<PlayerId, String>();
            
            String[] expectedSerialized = new String[4];
            for(int j=0; j<PlayerId.COUNT; j++) {
                String randomName = getRandomString(rng, 1);
                names.put(PlayerId.ALL.get(j), randomName);
                
                String serializedName = StringSerializer.serializeString(randomName);
                expectedSerialized[j] = serializedName;
            }
            
            String serialized = StringSerializer.serializeNameMap(names);
            Map<PlayerId, String> deserialized = StringSerializer.deserializeNameMap(serialized);
            
            
            assertEquals(serialized, String.join(",", expectedSerialized));
            assertEquals(deserialized, names);
            
        }
    }
}
