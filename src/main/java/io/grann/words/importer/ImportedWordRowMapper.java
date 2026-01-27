    package io.grann.words.importer;

import java.util.Map;

public final class ImportedWordRowMapper {

    private ImportedWordRowMapper() {}

    public static ImportedWord fromRowMap(Map<String, String> rowMap) {
        ImportedWord wordImportRow = new ImportedWord();

        wordImportRow.setWord(rowMap.get("word"));
        wordImportRow.setAlternate(rowMap.get("alternate"));
        wordImportRow.setTranslation(rowMap.get("translation"));
        wordImportRow.setNote(rowMap.get("note"));
        wordImportRow.setWordClass(rowMap.get("word class"));
        wordImportRow.setRank(parseInteger(rowMap.get("rank")));

        return wordImportRow;
    }

    private static Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Float.valueOf(value.trim()).intValue();
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
