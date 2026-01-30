// Copyright (c) 2026 dndv555 

package dndv.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {
   private String json;
   private int index;
   private char currentchar;
   
   public JsonParser(String json) {
       this.json = json;
       index = 0;
       if(json.length() < 0) {
           currentchar = json.charAt(0);
       }
   }
   
   // Next char
   private void nextchar() {
       index++;
       if(index < json.length()) {
           currentchar = json.charAt(index);
       } else {
           currentchar = '\0';
       }   
   }
   // Skip whitespace
   private void skipWhitespace() {
       while (currentchar == ' ' || currentchar == '\t' || 
           currentchar == '\n' || currentchar == '\r') {
           nextchar();
       }
   }
   // 
   private char peek() {
       if(index + 1 < json.length()) {
           return json.charAt(index + 1);
       }
       return '\0';
   }
   // Parsing String
   private String parseString() {
    StringBuilder sb = new StringBuilder();
    nextchar();
    
    while (currentchar != '"') {
        if (currentchar == '\\') {
            nextchar(); 
            switch (currentchar) {
                case '"': sb.append('"'); break;
                case '\\': sb.append('\\'); break;
                case '/': sb.append('/'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'n': sb.append('\n'); break;
                case 'r': sb.append('\r'); break;
                case 't': sb.append('\t'); break;
                default: sb.append(currentchar);
            }
        } else {
            sb.append(currentchar);
        }
        nextchar();
    }
    
    nextchar();
    return sb.toString();
    }
   
   // Parsing Number
   private Object parseNumber() throws JsonParseException {
    StringBuilder sb = new StringBuilder();
    
    while (Character.isDigit(currentchar) || currentchar == '.' || 
           currentchar == '-' || currentchar == '+' || 
           currentchar == 'e' || currentchar == 'E') {
        sb.append(currentchar);
        nextchar();
    }
    
    String numStr = sb.toString();
    try {
        if (numStr.contains(".") || numStr.contains("e") || numStr.contains("E")) {
            return Double.valueOf(numStr);
        } else {
            return Integer.valueOf(numStr);
        }
    } catch (NumberFormatException e) {
        throw new JsonParseException("Invalid number: " + numStr);
    }
}
   
   // Parsing keyword
   private Object parseKeyword() throws JsonParseException {
    StringBuilder sb = new StringBuilder();
    
    while (Character.isLetter(currentchar)) {
        sb.append(currentchar);
        nextchar();
    }
    
    String keyword = sb.toString();
    switch (keyword) {
        case "true": return true;
        case "false": return false;
        case "null": return null;
        default: throw new JsonParseException("Unknown keyword: " + keyword);
    }
  }
    
    private List<Object> parseArray() throws JsonParseException {
    List<Object> array = new ArrayList<>();
    nextchar(); 
    skipWhitespace();
    
    if (currentchar == ']') {
        nextchar(); 
        return array;
    }
    
       OUTER:
       while (true) {
           Object value = parseValue();
           array.add(value);
           skipWhitespace();
           switch (currentchar) {
               case ']':
                   nextchar();
                   break OUTER;
               case ',':
                   nextchar();
                   skipWhitespace();
                   break;
               default:
                   throw new RuntimeException("Expected ',' or ']' in array");
           }
       }
    
    return array;
  }
    
    // Parsing Object
    private Map<String, Object> parseObject() throws JsonParseException {
    Map<String, Object> obj = new HashMap<>();
    nextchar();
    skipWhitespace();
    
    if (currentchar == '}') {
        nextchar();
        return obj;
    }
    
       OUTER:
       while (true) {
           if (currentchar != '"') {
               throw new JsonParseException("Expected string key");
           }  String key = parseString();
           skipWhitespace();
           if (currentchar != ':') {
               throw new JsonParseException("Expected ':' after key");
           }  nextchar();
           skipWhitespace();
           Object value = parseValue();
           obj.put(key, value);
           skipWhitespace();
           switch (currentchar) {
               case '}':
                   nextchar();
                   break OUTER;
               case ',':
                   nextchar();
                   skipWhitespace();
                   break;
               default:
                   throw new JsonParseException("Expected ',' or '}' in object");
           }
       }
    
    return obj;
    }
    
    public Object parse() throws JsonParseException {
    skipWhitespace();
    return parseValue();
}

    private Object parseValue() throws JsonParseException {
    skipWhitespace();
    
    switch (currentchar) {
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
        default: throw new JsonParseException("Unexpected character: " + currentchar);
    }
  }
}  

