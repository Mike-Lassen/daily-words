package io.grann.words.importer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class CsvMapImporter {

    public List<Map<String, String>> importToMaps(InputStream csvInputStream) throws IOException {
        Objects.requireNonNull(csvInputStream, "csvInputStream");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvInputStream, StandardCharsets.UTF_8));
             CSVParser parser = CSVFormat.DEFAULT
                     .builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {

            Map<String, Integer> headerMap = parser.getHeaderMap();
            if (headerMap == null || headerMap.isEmpty()) {
                throw new IllegalArgumentException("CSV has no header row.");
            }

            List<Map<String, String>> rows = new ArrayList<>();

            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();

                for (String header : headerMap.keySet()) {
                    row.put(normalizeHeader(header), record.get(header));
                }

                // skip fully empty rows
                if (row.values().stream().allMatch(value -> value == null || value.isBlank())) {
                    continue;
                }

                rows.add(row);
            }

            return rows;
        }
    }

    private static String normalizeHeader(String header) {
        return header.trim().toLowerCase(Locale.ROOT);
    }

}
