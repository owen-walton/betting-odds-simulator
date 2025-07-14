/**
 * @author Owen Walton
 * Class still requires support for edge case json, compact json, and implementation of parseJsonArr()
 */
package com.betwise.oddscalc.ingestdata.ingestutils;

import java.util.HashMap;
import java.util.Map;

public final class ParseJSON {

    public Map<String, Object> parseJsonToMap(String jsonStr) {
        Map<String, Object> jsonMap = new HashMap<>();
        boolean endOfString = false;
        jsonStr = jsonStr.trim();

        while(!endOfString) {
            switch (jsonStr.charAt(0)) {
                case '{' -> jsonStr = stripOuterBrackets(jsonStr);
                case '"' -> {
                    // parse object will only return a map with size 1, hence it has parsed 1 object
                    // substring is exclusive so add 1 to end index
                    int objectEndIndex = getObjectEndIndex(jsonStr, 0);
                    jsonMap.putAll(parseObject(jsonStr.substring(0, objectEndIndex + 1)));

                    // parsed data must be removed from string
                    // object end index points to the value before the comma
                    // (+2 to point to first value of next object)
                    // (substring is exclusive so +3)
                    // firstly check if string is finished using end index + 1 (if end is reached no comma for +2)
                    if (jsonStr.length() == objectEndIndex + 1) {
                        endOfString = true;
                    } else {
                        jsonStr = jsonStr.substring(objectEndIndex + 3);
                    }

                }
                default -> {
                }
            }
        }
        return jsonMap;
    }

    public int getObjectEndIndex(String json, int startIndex) {

        if (startIndex < json.length()) {
            // checks string begins with quote as expected
            if (json.charAt(startIndex) == '"') {

                int colonIndex = findNextIndex(json, startIndex, ':');
                int startOfObjectValue = colonIndex + 1;
                switch (json.charAt(startOfObjectValue)) {
                    case '"' -> {
                        int nextCommaIndex = findNextIndex(json, startOfObjectValue, ',');
                        if (nextCommaIndex == - 1) {
                            return json.length()-1;
                        } else {
                            return nextCommaIndex;
                        }
                    }
                    case '{' -> {
                        return findCloseBracket(json, startOfObjectValue, '}');
                    }
                    case '[' -> {
                        return findCloseBracket(json, startOfObjectValue, ']');
                    }
                }

            }
        }
        return -1;
    }

    public Map<String, Object> parseObject(String jsonObjectStr) {

        // Method returns single map entry of 1 object and all inner objects are nested in that entry
        // so map size should always be 1
        // map is only initialised as it is added to, in order to protect this
        Map<String, Object> objectMap;
        String key;
        Object value;

        // start at index 1 to avoid function finding the first quote mark
        int outerQuoteMarkIndex = findNextNonEscapedIndex(jsonObjectStr, 1, '"');
        // find name of object without including quote marks
        key = jsonObjectStr.substring(1, outerQuoteMarkIndex);

        // now parse the value(s) of object
        // shorten string to not include the name or colon at beginning
        jsonObjectStr = jsonObjectStr.substring(findNextIndex(jsonObjectStr, 0, ':') + 1);

        switch (jsonObjectStr.charAt(0)) {
            case '"' -> value = jsonObjectStr.substring(1, findNextNonEscapedIndex(jsonObjectStr, 1, '"'));
            case '[' -> value = parseJsonArr(findBracketEnclosedString(jsonObjectStr, 0, ']'));
            case '{' -> value = parseJsonToMap(findBracketEnclosedString(jsonObjectStr, 0, '}'));
            default -> value = null;
        }

        objectMap = new HashMap<>();
        objectMap.put(key, value);
        return objectMap;
    }

    public Object parseJsonArr(String jsonArrStr) {

        return null;
    }

    public int findNextIndex(String str, int startIndex, char value) {

        int index = startIndex;

        if (str.substring(startIndex).contains("" + value)) {
            while (str.charAt(index) != value) {
                index ++;
            }

            return index;
        } else {
            return -1;
        }
    }

    public int findNextNonEscapedIndex(String str, int startIndex, char value) {

        for (int i = startIndex; i < str.length(); i++) {

            int numOfEscapes = 0;
            if (str.charAt(i) == value) {
                for (int j = i; j > 0; j--) {
                    if (str.charAt(j) != '\\') {
                        break;
                    }
                    numOfEscapes++;
                }
                if (numOfEscapes % 2 == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    public String findBracketEnclosedString(String str, int startIndex, char close) {
        return str.substring(startIndex + 1, findCloseBracket(str, startIndex, close));
    }

    public int findCloseBracket(String str, int startIndex, char close) {
        char open = str.charAt(startIndex);
        int openStatements = 0;
        int index = startIndex;

        do {
            char currentChar = str.charAt(index);
            if (currentChar == open) {
                openStatements ++;
            } else if (currentChar == close) {
                openStatements --;
            }
            index ++;
        } while (openStatements > 0);
        index --; // index is incorrectly incremented on last iteration so must be cancelled out

        return index;
    }

    public String stripOuterBrackets(String str) {
        return str.substring(1, str.length() - 1);
    }
}
