// Copyright (c) 2026 dndv555 

package dndv.json;

public class JsonParseException extends Exception {
    public JsonParseException(String message) {
        super(message);
    }
    
    public JsonParseException(String message, int position, char currentChar) {
        super(String.format("%s at position %d: '%c'", message, position, currentChar));
    }
}
