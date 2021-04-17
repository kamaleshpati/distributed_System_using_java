package org.distributedsystem.documentsearchusingtfidf.search;

import org.distributedsystem.documentsearchusingtfidf.models.DocData;
import org.distributedsystem.documentsearchusingtfidf.models.Result;
import org.distributedsystem.documentsearchusingtfidf.models.SerializationUtils;
import org.distributedsystem.documentsearchusingtfidf.models.Task;
import org.distributedsystem.documentsearchusingtfidf.networksevices.OnRequestCallback;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SearchWorker implements OnRequestCallback {
    private static final String ENDPOINT = "/task";
    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        Task task = (Task) SerializationUtils.deserialize(requestPayload);
        Result result = createResults(task);
        return SerializationUtils.serialize(result);
    }

    private Result createResults(Task task) {
        List<String> documents = task.getDocuments();
        System.out.printf("Received %d documents to process%n", documents.size());
        Result result = new Result();

        for (String document : documents) {
            List<String> words = parseWordsFromDocument(document);
            DocData documentData = TFIDF.createDocData(words, task.getSearchTerms());
            result.addDocumentData(document, documentData);
        }
        return result;
    }

    private List<String> parseWordsFromDocument(String document) {
        InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(document);
        if (resourceStream!=null) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceStream));
            List<String> lines = bufferedReader.lines().collect(Collectors.toList());
            return TFIDF.getWordsFromDocument(lines);
        }else {
            return Collections.emptyList();
        }
    }
    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
}
