/**
 * @author Owen Walton
 * Helper class for reading files, currently only needed for zip files
 */

package com.betwise.oddscalc.ingestdata.ingestutils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.*;

public final class FileReadHelper {

    // reads a singular file from inside a zip, given the location of the zip file and the file's name
    // returns a List<String>, where each String is a line in the file
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

    // like readZipFromResources, but instead used when multiple files from the same zip are to be read instead of one
    // it returns a map "FileName -> Contents" where contents is a List<String> of lines from the file
    //
    // this is used instead of iterating through the single version of this method
    // because opening and closing a zip file is an expensive process
    // so this method stays in the zip until required files are read
    public static Map<String, List<String>> readZipFilesFromResources(String zipPathInResources, Set<String> filesInZip, String fileExtension) {
        Map<String, List<String>> fileContents = new HashMap<>();

        try {
            File zipFileOnDisk = new File(Objects.requireNonNull(
                    FileReadHelper.class.getClassLoader().getResource(zipPathInResources)).toURI());
            try (ZipFile zipFile = new ZipFile(zipFileOnDisk, StandardCharsets.UTF_8)) {
                for (String file : filesInZip) {
                    String fullName = file + fileExtension;
                    ZipEntry entry = zipFile.getEntry(fullName);
                    if (entry != null) {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                            List<String> lines = new ArrayList<>();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                lines.add(line);
                            }
                            fileContents.put(file, lines);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileContents;
    }
}