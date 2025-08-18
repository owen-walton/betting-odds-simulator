package com.betwise.oddscalc.ingestdata.ingestutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HTTPClient {

    public String get(String urlString) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            // Explicitly GET
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15_000); // 15 seconds
            connection.setReadTimeout(15_000);

            // Optionally set headers
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "Betwise-OddsCalc-Client/1.0");

            int status = connection.getResponseCode();

            InputStream inputStream;
            if (status >= 200 && status < 300) {
                inputStream = connection.getInputStream();
            } else {
                // For errors, read the error stream if present
                inputStream = connection.getErrorStream();
                if (inputStream == null) {
                    throw new IOException("HTTP " + status + " with no response body");
                }
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                StringBuilder responseBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line).append("\n");
                }
                return responseBuilder.toString().trim();
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}