package io.grann.words.importer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportedWord {
    private String word;
    private String alternate;
    private Integer rank;
    private String translation;
    private String note;
    private String wordClass;
    private String allWordClasses;
    private Integer level;
}
