// Copyright (c) 2026 dndv555

package dndv.json;

import java.util.*;

public class JsonParser {
    private String json;
    private int index;
    private char currentChar;
    
    public JsonParser(String json) throws JsonParseException {
        if (json == null) {
            throw new JsonParseException("JSON string cannot be null");
        }
        this.json = json;
        this.index = 0;
        if (json.length() > 0) {
            this.currentChar = json.charAt(0);
        } else {
            throw new JsonParseException("JSON string is empty");
        }
    }
    
    private void nextChar() {
        index++;
        if (index < json.length()) {
            currentChar = json.charAt(index);
        } else {
            currentChar = '\0';
        }
    }
    
    // Skip WhiteSpace
    private void skipWhitespace() {
        while (index < json.length() && 
               (currentChar == ' ' || currentChar == '\t' || 
                currentChar == '\n' || currentChar == '\r')) {
            nextChar();
        }
    }
    
    // Parsing String
    private String parseString() throws JsonParseException {
        StringBuilder sb = new StringBuilder();
        nextChar(); 
        
        while (currentChar != '"') {
            if (index >= json.length()) {
                throw new JsonParseException("Unterminated string", index, currentChar);
            }
            
            if (currentChar == '\\') {
                nextChar();
                if (index >= json.length()) {
                    throw new JsonParseException("Unterminated escape sequence", index, currentChar);
                }
                switch (currentChar) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    default: sb.append(currentChar);
                }
            } else {
                sb.append(currentChar);
            }
            nextChar();
        }
        nextChar();
        return sb.toString();
    }
    
    // Parsing Number
    private Object parseNumber() throws JsonParseException {
        int start = index;
        
        while (index < json.length() && 
               (Character.isDigit(currentChar) || 
                currentChar == '.' || currentChar == '-' || 
                currentChar == '+' || currentChar == 'e' || 
                currentChar == 'E')) {
            nextChar();
        }
        
        String numStr = json.substring(start, index);
        
        try {
            if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
                return Double.valueOf(numStr);
            } else {
                long longValue = Long.parseLong(numStr);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                }
                return longValue;
            }
        } catch (NumberFormatException e) {
            throw new JsonParseException("Invalid number format: " + numStr, start, json.charAt(start));
        }
    }
    
    // Parsing keyword
    private Object parseKeyword() throws JsonParseException {
        int start = index;
        StringBuilder sb = new StringBuilder();
        
        while (index < json.length() && Character.isLetter(currentChar)) {
            sb.append(currentChar);
            nextChar();
        }
        
        String keyword = sb.toString();
        switch (keyword) {
            case "true": return true;
            case "false": return false;
            case "null": return null;
            default: 
                throw new JsonParseException("Unknown keyword: " + keyword, start, json.charAt(start));
        }
    }
    
    // Parsing Array
    private List<Object> parseArray() throws JsonParseException {
        List<Object> array = new ArrayList<>();
        nextChar(); // Skipping ']'
        skipWhitespace();
        
        if (currentChar == ']') {
            nextChar();
            return array;
        }
        
        OUTER:
        while (true) {
            Object value = parseValue();
            array.add(value);
            skipWhitespace();
            switch (currentChar) {
                case ']':
                    nextChar();
                    break OUTER;
                case ',':
                    nextChar();
                    skipWhitespace();
                    break;
                default:
                    throw new JsonParseException("Expected ',' or ']' in array", index, currentChar);
            }
        }
        
        return array;
    }
    
    private Map<String, Object> parseObject() throws JsonParseException {
        Map<String, Object> obj = new LinkedHashMap<>();
        nextChar(); // Skipping '{'
        skipWhitespace();
        
        if (currentChar == '}') {
            nextChar();
            return obj;
        }
        
        OUTER:
        while (true) {
            if (currentChar != '"') {
                throw new JsonParseException("Expected string key in object", index, currentChar);
            }
            String key = parseString();
            skipWhitespace();
            if (currentChar != ':') {
                throw new JsonParseException("Expected ':' after key", index, currentChar);
            }
            nextChar();
            skipWhitespace();
            Object value = parseValue();
            obj.put(key, value);
            skipWhitespace();
            switch (currentChar) {
                case '}':
                    nextChar();
                    break OUTER;
                case ',':
                    nextChar();
                    skipWhitespace();
                    break;
                default:
                    throw new JsonParseException("Expected ',' or '}' in object", index, currentChar);
            }
        }
        
        return obj;
    }
    
    private Object parseValue() throws JsonParseException {
        skipWhitespace();
        
        if (index >= json.length()) {
            throw new JsonParseException("Unexpected end of JSON", index, currentChar);
        }
        
        switch (currentChar) {
            case '"': return parseString();
            case '{': return parseObject();
            case '[': return parseArray();
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9': return parseNumber();
            case 't':
            case 'f':
            case 'n': return parseKeyword();
            default: 
                throw new JsonParseException(
                    "Unexpected character. Expected: string, object, array, number, or boolean/null", 
                    index, 
                    currentChar
                );
        }
    }
    
    public Object parse() throws JsonParseException {
        skipWhitespace();
        Object result = parseValue();
        skipWhitespace();
        
        if (index < json.length()) {
            throw new JsonParseException("Extra content after JSON", index, currentChar);
        }
        
        return result;
    }
}