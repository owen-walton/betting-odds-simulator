package com.betwise.oddscalc.ingestdata.ingestutils;

import com.google.protobuf.MapEntry;

import java.util.HashMap;
import java.util.Map;

public final class ParseJSON {

    public Map<String, Object> parseJsonToMap(String jsonStr) {
        Map<String, Object> jsonMap = new HashMap<>();
        boolean endOfString = false;
        jsonStr = jsonStr.trim();

        int i = 0;
        while(i < jsonStr.length()) {
            switch (jsonStr.charAt(i)) {
                case '{' -> jsonStr = stripOuterBrackets(jsonStr);
                case '"' -> {
                    // parse object will only return a map with size 1, hence it has parsed 1 object
                    // substring is exclusive so add 1 to end index
                    int objectEndIndex = getObjectEndIndex(jsonStr, i);
                    jsonMap.putAll(parseObject(jsonStr.substring(i, objectEndIndex + 1)));
                    // object end index points to the value before the comma
                    // (+2 to point to first value of next object)
                    // (substring is exclusive so +3)
                    jsonStr = jsonStr.substring(objectEndIndex + 3);
                }
                default -> {
                    break;
                }
            }
        }
        return jsonMap;
    }

    public int getObjectEndIndex(String json, int startIndex) {

        if (startIndex < json.length()) {
            if (json.charAt(startIndex) == '"') {

                int commaIndex = findNextIndex(json, startIndex, ',');

                return commaIndex - 1;

            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public Map<String, Object> parseObject(String jsonObjectStr) {

        // Method returns single map entry of 1 object and all inner objects are nested in that entry
        // so map size should always be 1
        // map is only initialised as it is added to, in order to protect this
        Map<String, Object> objectMap;
        String key;
        Object value;

        // start at index 1 to avoid function finding the first quote mark
        int outerQuoteMarkIndex = findNextIndex(jsonObjectStr, 1, '"');
        // find name of object without including quote marks
        key = jsonObjectStr.substring(1, outerQuoteMarkIndex);

        // now parse the value(s) of object
        // shorten string to not include the name or colon at beginning
        jsonObjectStr = jsonObjectStr.substring(findNextIndex(jsonObjectStr, 0, ':') + 1);

        switch (jsonObjectStr.charAt(0)) {
            case '"' -> value = jsonObjectStr.substring(1, findNextIndex(jsonObjectStr, 1, '"'));
            case '[' -> value = parseJsonArr(findBracketEnclosedString(jsonObjectStr, 0, ']'));
            case '{' -> value = parseJsonToMap(findBracketEnclosedString(jsonObjectStr, 0, '}'));
            default -> value = null;
        }

        objectMap = new HashMap<>();
        objectMap.put(key, value);
        return objectMap;
    }

    public Object parseJsonArr(String jsonArrStr) {


    }

    public int findNextIndex(String str, int startIndex, char value) {

        int index = startIndex;

        if (str.contains("" + value)) {
            while (str.charAt(index) != value) {
                index ++;
            }

            return index;
        } else {
            return -1;
        }
    }

    public String findBracketEnclosedString(String str, int startIndex, char close) {
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

        return str.substring(startIndex, index + 1);
    }

    public String stripOuterBrackets(String str) {
        return str.substring(1, str.length() - 1);
    }
}
