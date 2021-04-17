package org.distributedsystem.documentsearchusingtfidf.models;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {
    private Map<String, DocData> documentToDocumentData = new HashMap<>();

    public void addDocumentData(String document, DocData documentData) {
        this.documentToDocumentData.put(document, documentData);
    }

    public Map<String, DocData> getDocumentToDocumentData() {
        return Collections.unmodifiableMap(documentToDocumentData);
    }
}
