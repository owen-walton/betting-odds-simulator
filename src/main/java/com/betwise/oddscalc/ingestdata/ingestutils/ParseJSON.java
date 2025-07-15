/**
 * @author Owen Walton
 * Class relies on trusted input, incorrect json syntax will break and this is not currently handled safely
 * this is because all jsons required in program come from sources that are entrusted to follow syntax
 * ensureSyntax() will confirm the json syntax is correct before attempting to parse
 */
package com.betwise.oddscalc.ingestdata.ingestutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ParseJSON {

    // ensureSyntax is incomplete
    public boolean ensureSyntax(String jsonString) {
        return false;
    }

    public Map<String, Object> parseJsonToMap(String jsonStr) {
        Map<String, Object> jsonMap = new HashMap<>();
        boolean endOfString = false;
        jsonStr = removeWhiteSpace(jsonStr);

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
                    // firstly check if string is finished using end index + 1
                    if (jsonStr.length() == objectEndIndex + 1) {
                        endOfString = true;
                    } else {
                        jsonStr = jsonStr.substring(objectEndIndex + 2);
                    }
                }
                default -> { // should never be called
                }
            }
        }
        return jsonMap;
    }

    // wrapper of getValueEndIndex to allow end index to be found when a key is present
    private int getObjectEndIndex(String json, int startIndex) {

        if (startIndex < json.length()) {
            // checks string begins with quote as expected
            if (json.charAt(startIndex) == '"') {

                int colonIndex = findNextNonEscapedIndex(json, startIndex, ':');
                int startOfObjectValue = colonIndex + 1;

                return getValueEndIndex(json, startOfObjectValue);
            }
        }
        return -1;
    }

    private int getValueEndIndex(String jsonStr, int startIndex) {
        switch (jsonStr.charAt(startIndex)) {
            case '"' -> {
                int nextCommaIndex = findNextNonEscapedIndex(jsonStr, startIndex, ',');
                // if no more commas in string -1 is returned
                if (nextCommaIndex == -1) {
                    return jsonStr.length()-1;
                } else {
                    return nextCommaIndex - 1;
                }
            }
            case '{' -> {
                return findCloseBracket(jsonStr, startIndex, '}');
            }
            case '[' -> {
                return findCloseBracket(jsonStr, startIndex, ']');
            }
            // if primitive
            default -> {
                int nextCommaIndex = findNextNonEscapedIndex(jsonStr, startIndex, ',');
                // if no more commas in string -1 is returned
                if (nextCommaIndex == -1) {
                    return jsonStr.length()-1;
                } else {
                    return nextCommaIndex - 1;
                }
            }
        }
    }

    private Map<String, Object> parseObject(String jsonObjectStr) {

        // Method returns single map entry of 1 object and all inner objects are nested in that entry
        // so map size should always be 1
        // map is only initialised as it is added to, in order to protect this
        Map<String, Object> objectMap;
        String key;
        Object value;

        // parse key
        key = parseKey(jsonObjectStr);

        // shorten string to not include the key or colon at beginning
        jsonObjectStr = jsonObjectStr.substring(findNextNonEscapedIndex(jsonObjectStr, 0, ':') + 1);

        // now parse the value(s) of object
        value = parseValue(jsonObjectStr);

        objectMap = new HashMap<>();
        objectMap.put(key, value);
        return objectMap;
    }

    private String parseKey(String jsonObjectStr) {
        // start at index 1 to avoid function finding the first quote mark
        int outerQuoteMarkIndex = findNextNonEscapedIndex(jsonObjectStr, 1, '"');
        // find name of object without including quote marks
        return jsonObjectStr.substring(1, outerQuoteMarkIndex);
    }

    // requires a string beginning with the value not key (remove key and colon from start)
    // however can have more data on end
    private Object parseValue(String jsonValueStr) {
        Object value;
        switch (jsonValueStr.charAt(0)) {
            case '"' -> value = jsonValueStr.substring(1, findNextNonEscapedIndex(jsonValueStr, 1, '"'));
            case '[' -> value = parseJsonArr(findBracketEnclosedString(jsonValueStr, 0, ']'));
            case '{' -> value = parseJsonToMap(findBracketEnclosedString(jsonValueStr, 0, '}'));
            // for primitive values
            default -> {
                if (jsonValueStr.startsWith("true")) {
                    value = true;
                } else if (jsonValueStr.startsWith("false")) {
                    value = false;
                } else if (jsonValueStr.startsWith("null")) {
                    value = null;
                } else { // must be number
                    value = parseNumber(jsonValueStr, 0);
                }
            }
        }
        return value;
    }

    private Object parseNumber(String json, int startIndex) {
        int decimalCount = 0;
        int eCount = 0;
        int dashCount = 0;
        int charCount = 0;
        boolean endOfNumber = false;

        // loop gets length of number and puts in 'charCount'
        // doesn't require json syntax check (e.g. minus sign only at start) because all json inputs are trusted
        while(decimalCount <= 1 && eCount <= 1 && dashCount <= 1 && !endOfNumber && charCount < json.length()) {
            char currentChar = json.charAt(charCount);
            if (currentChar == '.') {
                decimalCount++;
            } else if (currentChar == 'e' || currentChar == 'E') {
                eCount++;
            } else if (currentChar == '-') {
                if (charCount != 0) {
                    dashCount++; // dash count doesn't store a negative sign as it is there for dashes next to an 'E'
                }
            } else if (!Character.isDigit(currentChar)) {
                // if character is not part of a number (all possible chars for number are checked above)
                // then set end of number to true and continue so char count is not incremented
                endOfNumber = true;
                continue;
            }
            charCount++;
        }

        String number = json.substring(startIndex, startIndex + charCount);
        // if integer
        if (decimalCount == 0 && eCount == 0) {
            try {
                return Integer.parseInt(number);
            } catch (NumberFormatException e1) {
                // if too large to store an integer use long
                return Long.parseLong(number);
            }
        } else {
            // must be decimal
            return Double.parseDouble(number);
        }
    }

    private Object parseJsonArr(String jsonArrStr) {

        List<Object> arrayList = new ArrayList<>();

        while (!jsonArrStr.isEmpty()) {
            // parse first value in string
            int valueEndIndex = getValueEndIndex(jsonArrStr, 0);
            arrayList.add(parseValue(jsonArrStr.substring(0, valueEndIndex + 1)));
            // if last value has just been parsed (array is finished)
            if (valueEndIndex == jsonArrStr.length() - 1) {
                break;
            }
            // remove parsed data from string
            // valueEndIndex + 2 points to end of last object, +1 points to comma, +2 points to start of next value
            jsonArrStr = jsonArrStr.substring(valueEndIndex + 2);
        }
        return arrayList;
    }

    private int findNextNonEscapedIndex(String str, int startIndex, char value) {
        boolean inQuotes = false; // function assumes not already in quotes (start index must not be in quotes)

        for (int i = startIndex; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            // must go before in quotes check so that quotes can also be looked for with this function
            if (!inQuotes && currentChar == value && !isEscaped(str, i)) {
                return i;
            }
            if (currentChar == '"' && !isEscaped(str, i)) {
                inQuotes = !inQuotes;
            }
        }
        // -1 means not found
        return -1;
    }

    private boolean isEscaped(String str, int index) {

        int numOfEscapes = 0;

        for (int i = index; i > 0; i--) {
            if (str.charAt(i) != '\\') {
                break;
            }
            numOfEscapes++;
        }
        return numOfEscapes % 2 == 1;
    }

    private String findBracketEnclosedString(String str, int startIndex, char close) {
        return str.substring(startIndex + 1, findCloseBracket(str, startIndex, close));
    }

    private int findCloseBracket(String str, int startIndex, char close) {
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

    private String stripOuterBrackets(String str) {
        return str.substring(1, str.length() - 1);
    }

    // will not work on half of a string if a quote is missing from first half (only use on full strings)
    private String removeWhiteSpace(String str) {
        boolean inQuotes = false;
        StringBuilder newStr = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);
            if (currentChar == '"' && !isEscaped(str, i)) {
                inQuotes = !inQuotes;
                newStr.append(currentChar);
            } else if (inQuotes || !(currentChar == ' ' || currentChar == '\n')) {
                newStr.append(currentChar);
            }
        }
        return newStr.toString();
    }
}
