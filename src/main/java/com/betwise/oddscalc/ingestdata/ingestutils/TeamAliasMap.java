package com.betwise.oddscalc.ingestdata.ingestutils;

import java.util.HashMap;
import java.util.Map;

public class TeamAliasMap {

    private Map<String, String> teamAliasMap;

    public TeamAliasMap() {
        buildMap();
    }

    public void buildMap() {
        teamAliasMap = new HashMap<>();
        teamAliasMap.put("Ivory Coast", "Côte Du0027ivoire");
        teamAliasMap.put("Czech Republic", "Czechia");
        teamAliasMap.put("St Helena", "Saint Helena");
        teamAliasMap.put("Turks and Caicos Island", "Turks And Caicos Islands");
        teamAliasMap.put("Eswatini", "Swaziland");
        teamAliasMap.put("South Korea", "Korea");
        teamAliasMap.put("Tanzania", "Tanzania The United Republic Of");
        teamAliasMap.put("England", "United Kingdom Of Great Britain And Northern Ireland");
        teamAliasMap.put("United States of America", "United States of America");
        teamAliasMap.put("United States of America", "United States Minor Outlying Islands"); // if treated as USA
        teamAliasMap.put("United States of America", "Virgin Islands Us"); // normalize US territory to USA
        teamAliasMap.put("West Indies", "Trinidad And Tobago");
        teamAliasMap.put("West Indies", "Barbados");
        teamAliasMap.put("West Indies", "Saint Kitts And Nevis");
        teamAliasMap.put("West Indies", "Saint Vincent And The Grenadines");
        teamAliasMap.put("West Indies", "Grenada");
        teamAliasMap.put("West Indies", "Jamaica");
        teamAliasMap.put("Turks and Caicos Island", "Turks And Caicos Islands");
        teamAliasMap.put("St Helena", "Saint Helena");
        teamAliasMap.put("England", "United Kingdom Of Great Britain And Northern Ireland");
    }

    public String aliasCheck(String sz) {
        for (Map.Entry<String, String> entry : teamAliasMap.entrySet()) {
            if (entry.getValue().equals(sz)) {
                return entry.getKey();
            }
        }
        return sz;
    }
}
