/**
 * @author Owen Walton
 * Helper class for handling and parsing json so the contents can be interpreted/ingested
 * ,
 * Class relies on trusted input, incorrect json syntax will break and this is not currently handled safely
 * this is because all jsons required in program come from sources that are entrusted to follow syntax
 * ,
 * ensureSyntax() will confirm the json syntax is correct before attempting to parse, not currently implemented
 */
package com.betwise.oddscalc.ingestdata.ingestutils;

import java.util.*;

public final class ParseJSON {

    private ParseJSON() {
    }

    // ensureSyntax is incomplete
    private static boolean ensureSyntax(String jsonString) {
        return false;
    }

    // returns null for an invalid key path
    // unrelated to json parsing but a helper for accessing the desired field from a map returned by parseJsonToMap()
    // takes a path in format "xxx/yyy/zzz", zzz is the key of the required value and xxx and yyy are its parent keys
    public static Object getValueFromMap(String keyPath, Map<String, Object> map) {

        // gets the value where the key follows the path provided
        Object current = map;

        for(String key : keyPath.split("/")) {
            if (!(current instanceof Map)) {
                // key path is invalid
                return null;
            }
            current = ((Map<String, Object>) current).get(key);
        }
        return current;
    }

    /**
     * parseJsonToMap():
     * This method has three signatures:
     * - two public wrappers for the private method, one for when there are ignored keys and one that parses all paths
     * - the private method that handles all the logic, the private method needs to call itself in a recursive fashion,
     *   the currentPath parameter is empty in the wrappers so that the method knows it is at the start of the parse
     */
    public static Map<String, Object> parseJsonToMap(String jsonStr) {
        return parseJsonToMap(jsonStr, new HashSet<>(), "");
    }

    public static Map<String, Object> parseJsonToMap(String jsonStr, Set<String> ignoredKeyPaths) {
        return parseJsonToMap(jsonStr, ignoredKeyPaths, "");
    }

    // recursively parse the JSON into a nested HashMap<String, Object>, (the object is likely to be an inner map)
    // remove braces then for each "key -> object" at top level, calls parseObject()
    // (object can be any data type incl primitive)
    // skipping any paths in ignoredKeyPaths
    private static Map<String, Object> parseJsonToMap(String jsonStr, Set<String> ignoredKeyPaths, String currentPath) {
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
                    Map<String, Object> parsedObj = parseObject(jsonStr.substring(0, objectEndIndex + 1), ignoredKeyPaths, currentPath);
                    if (parsedObj != null) {
                        jsonMap.putAll(parsedObj);
                    }

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

    // wrapper of getValueEndIndex used when a key is present in order to remove key
    // i.e. "obj":{"x":1, "y":3}, ... will give getValueEndIndex() {"x":1, "y":3}, ...
    private static int getObjectEndIndex(String json, int startIndex) {

        if (startIndex < json.length()) {
            // checks string begins with quote as expected
            if (json.charAt(startIndex) == '"') {

                int colonIndex = findNextNonEscapedIndex(json, startIndex, ':');
                int startOfObjectValue = colonIndex + 1;

                // startOfObjectValue in the json refers to the { that opens an object, then getValueEndIndex finds }
                return getValueEndIndex(json, startOfObjectValue);
            }
        }
        return -1;
    }

    // a value can either be:
    // an object "key":{...}, an array "key":[...], a string "key":"...", or primitive value "key":123
    // this method takes in a string and the index of the start of the value
    // (not including "key:", there is a wrapper for this)
    // it will return the index of the end of the object i.e. index of corresponding '}' to '{'
    private static int getValueEndIndex(String jsonStr, int startIndex) {
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

    private static Map<String, Object> parseObject(String jsonObjectStr, Set<String> ignoredKeyPaths, String currentPath) {

        // Method returns single map entry of 1 object and all inner objects are nested in that entry
        // so map size should always be 1
        // map is only initialised as it is added to, in order to protect this
        Map<String, Object> objectMap;
        String key;
        Object value;

        // parse key
        key = parseKey(jsonObjectStr);
        String fullPath = currentPath.isEmpty() ? key : currentPath + "/" + key;

        // ignore this object entirely if matched with ignoredKeyPaths
        if (ignoredKeyPaths.contains(fullPath)) {
            return null;
        }

        // shorten string to not include the key or colon at beginning
        jsonObjectStr = jsonObjectStr.substring(findNextNonEscapedIndex(jsonObjectStr, 0, ':') + 1);

        // now parse the value(s) of object
        value = parseValue(jsonObjectStr, ignoredKeyPaths, fullPath);

        objectMap = new HashMap<>();
        objectMap.put(key, value);
        return objectMap;
    }

    // return the key (without quotes) of the string given in form "key":...
    private static String parseKey(String jsonObjectStr) {
        // start at index 1 to avoid function finding the first quote mark
        int outerQuoteMarkIndex = findNextNonEscapedIndex(jsonObjectStr, 1, '"');
        // find name of object without including quote marks
        return jsonObjectStr.substring(1, outerQuoteMarkIndex);
    }

    // requires a string beginning with the value not key (remove key and colon from start)
    // however can have more data on end
    // converts a json value to the corresponding java value by identifying json type then calling correct helper
    // if an inner object is found, parseJsonToMap is recursively called despite currently being inside it
    private static Object parseValue(String jsonValueStr, Set<String> ignoredKeyPaths, String currentPath) {
        Object value;
        switch (jsonValueStr.charAt(0)) {
            case '"' -> value = jsonValueStr.substring(1, findNextNonEscapedIndex(jsonValueStr, 1, '"'));
            case '[' -> value = parseJsonArr(findBracketEnclosedString(jsonValueStr, 0, ']'));
            case '{' -> value = parseJsonToMap(findBracketEnclosedString(jsonValueStr, 0, '}'), ignoredKeyPaths, currentPath);
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

    // array parsing method may call this wrapper because it doesn't require key path ignoring
    private static Object parseValue(String jsonValueStr) {
        return parseValue(jsonValueStr, new HashSet<>(), "");
    }

    // counts decimal points, exponent markers and dashes so that number ends correctly
    // stops reading when a non-number character is hit
    // returns integer, long, or double depending on what the string contains
    private static Object parseNumber(String json, int startIndex) {
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

    // takes in a shortened array string in form [xx,yy,zz]
    // iterates through values, recursively calling parseValue() on each one e.g xx,
    // this supports any data type inside each array field
    private static Object parseJsonArr(String jsonArrStr) {

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

    // takes in char value, then looks for the next index of that value including and after the start index
    // -1 mean value doesn't appear again
    // if the char is inside a quote it isn't counted,
    // so in order to know if inside quotes, the function must assume it is not inside an open quote before start index
    private static int findNextNonEscapedIndex(String str, int startIndex, char value) {
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

    // helper method to see if a char is escaped by \,
    // however \\, doesn't escape the comma, only a \ so there must be an odd number of \
    private static boolean isEscaped(String str, int index) {

        int numOfEscapes = 0;

        for (int i = index; i > 0; i--) {
            if (str.charAt(i) != '\\') {
                break;
            }
            numOfEscapes++;
        }
        return numOfEscapes % 2 == 1;
    }

    // wrapper to get the enclosed string instead of just the index the bracket closes
    private static String findBracketEnclosedString(String str, int startIndex, char close) {
        return str.substring(startIndex + 1, findCloseBracket(str, startIndex, close));
    }

    // finds the value at start index, setting that to char open, then find the corresponding close bracket
    // if another open is found then 2 closes are needed, etc
    private static int findCloseBracket(String str, int startIndex, char close) {
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

    private static String stripOuterBrackets(String str) {
        return str.substring(1, str.length() - 1);
    }

    // doesn't remove white space from inside quotes
    // will not work on half of a string if a quote is missing from first half (only use on full strings)
    private static String removeWhiteSpace(String str) {
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
