package ch.epfl.javass.net;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.TurnState;

/**
 * The class is used to encode and decode information. More specifically,
 * using this class you can encode (serialize) informations that are of type
 * int, long, String, TurnState, Map<PlayerId, String>. 
 * 
 * This is useful in the context of communicating informations, where you 
 * want to encode information of the said types in a precise manner.
 * 
 * It is specifically tailored for the use in the context of Jass as it includes
 * encoding of types such as TurnState, Map<PlayerId, String>
 * 
 * @author xavier
 *
 */
public final class StringSerializer {
    private StringSerializer() {}
    
    static private final int BASE16_RADIX = 16;
    static private final Charset utf8 = StandardCharsets.UTF_8;
    
    /**
     * Returns a representation of the given int in base 16.
     * @param number
     * @return
     */
    static public String serializeInt(int number) {
        return Integer.toUnsignedString(number, BASE16_RADIX);
    }
    
    /**
     * Given a base 16 representation of an int, it returns that number
     * @param base16Encoded
     * @return
     */
    static public int deserializeInt(String base16Encoded) {
        return Integer.parseUnsignedInt(base16Encoded, BASE16_RADIX);
    }
    
    /**
     * Returns a representation of the given long in base 16.
     * @param number
     * @return
     */
    static public String serializeLong(long number) {
        return Long.toUnsignedString(number, BASE16_RADIX);
    }
    
    /**
     * Given a base 16 representation of a long, it returns that number
     * @param base16Encoded
     * @return
     */
    static public long deserializeLong(String base16Encoded) {
        return Long.parseUnsignedLong(base16Encoded, BASE16_RADIX);
    }
    
    /**
     * The function serialized a String.
     * We first get the bytes describing the utf8 encoding of the string.
     * Then we return the Base64 encoding of this stream of bytes.
     * @param string
     * @return
     */
    static public String serializeString(String string) {
        return Base64.getEncoder().encodeToString(string.getBytes(utf8));
    }
    
    /**
     * Given the a serialized string, it gives the string.
     * More specifically, the argument "base64Encoded" must have been encoded in the following way:
     * - Take the bytes describing the string in utf8 encoding
     * - Take the Base64 encoding of these bytes.
     * @param base64Encoded
     * @return
     */
    static public String deserializeString(String base64Encoded) {
        byte[] byteDecoded = Base64.getDecoder().decode(base64Encoded);
        return new String(byteDecoded, utf8);
    }
    
    /**
     * The function encodes the state by encoding the components of the state. 
     * More specifically, it encodes each component using serializeLong(_) and serializeInt(_):
     *  - packed score is encoded using serializeLong
     *  - packed unplayed cards is encoded using serializeLong
     *  - packed trick is encoded using serializeInt
     *  
     * Then the three resulting strings are joined by a comma.
     * 
     * For ex: akjsdhf,saldfhaksl,sdkljfa 
     * could be a serialized turn state.
     *  
     * @param state
     * @return
     */
    static public String serializeTurnState(TurnState state) {
        String serializedScore = serializeLong(state.packedScore());
        String serializedUnplayedCards = serializeLong(state.packedUnplayedCards());
        String serializedTrick = serializeInt(state.packedTrick());
        return String.join(",", serializedScore, serializedUnplayedCards, serializedTrick);
    }
    
    /**
     * Gives the turn state given the serialized version. More specifically, the given serialized
     * should be of form
     *  <serialized packed score>,<serialized packed unplayed cards>,<serialized packed trick>
     * @param serialized
     * @return
     */
    static public TurnState deserializeTurnState(String serialized) {
        String[] elements = serialized.split(",");
        long packedScore = deserializeLong(elements[0]);
        long unplayedPackedCards = deserializeLong(elements[1]);
        int packedTrick = deserializeInt(elements[2]);
        return TurnState.ofPackedComponents(packedScore, unplayedPackedCards, packedTrick);
    }
    
    /**
     * Serializes the given names by first serializing each name and then joining
     * them by a comma
     * @param names a Map with four components, i.e. there is no PlayerId not represented
     * @return
     */
    static public String serializeNameMap(Map<PlayerId, String> names) {
        String[] serializedNames = new String[4];
        names.forEach((key, name) -> {
            serializedNames[key.ordinal()] = serializeString(name);
        });
        return String.join(",", serializedNames);
    }
    
    /**
     * Gives the map corresponding to the serialized version, of form
     *  <serialized name of p1>,<serialized name of p2>,<serialized name of p3>,<serialized name of p4>
     * @param serialized
     * @return
     */
    static public Map<PlayerId, String> deserializeNameMap(String serialized) {
        String[] deserializedNames = serialized.split(",");
        Map<PlayerId, String> names = new HashMap<PlayerId, String>();
        for(int i=0; i<deserializedNames.length; i++) 
            names.put(PlayerId.ALL.get(i), deserializeString(deserializedNames[i]));
        return names;
    }
}
