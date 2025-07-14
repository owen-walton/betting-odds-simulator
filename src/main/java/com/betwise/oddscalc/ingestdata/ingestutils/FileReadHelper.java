package com.betwise.oddscalc.ingestdata.ingestutils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

public final class FileReadHelper {

    public static List<String> readZipFromResources(String zipPathInResources, String fileInZip) {
        List<String> lines = new ArrayList<>();

        try (
                // find zip
                InputStream inputStream = FileReadHelper.class.getClassLoader().getResourceAsStream(zipPathInResources);
                ZipInputStream zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8)
        ) {
            ZipEntry entry;

            // find file in zip
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equals(fileInZip)) {

                    // read from file
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipInputStream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }
                    }
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
}
