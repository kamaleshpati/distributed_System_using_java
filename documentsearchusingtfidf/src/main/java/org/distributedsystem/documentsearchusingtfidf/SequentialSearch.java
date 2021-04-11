package org.distributedsystem.documentsearchusingtfidf;

import org.distributedsystem.documentsearchusingtfidf.models.DocData;
import org.distributedsystem.documentsearchusingtfidf.search.TFIDF;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class SequentialSearch {
    public static final String  BOOKS_DIR = "books";
    public static String searchQuery1 = "The best Detective that catches many criminals";

    public static void main(String[] args) throws IOException {
        List<String> resources = getResourceFiles(BOOKS_DIR);
        List<String> terms = TFIDF.getWordsFromLine(searchQuery1);
        findMostReleventDocs(resources,terms);
    }

    private static void findMostReleventDocs(List<String> resources, List<String> terms) throws IOException {
        Map<String, DocData> docDataMap = new HashMap<>();

        for (String resource : resources) {
            InputStream resourceStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource);

            if (resourceStream != null) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceStream));
                List<String> lines = bufferedReader.lines().collect(Collectors.toList());
                List<String> words = new ArrayList<>();
                lines.stream().map(TFIDF::getWordsFromLine).forEach(words::addAll);
                DocData docData = TFIDF.createDocData(words,terms);
                docDataMap.put(resource,docData);
            }

        }
        Map<Double, List<String>> documentDataByScore = TFIDF.getDocumentSortedByScore(terms,docDataMap);
        printResult(documentDataByScore);
    }

    private static void printResult(Map<Double, List<String>> documentDataByScore) {
        for(Map.Entry<Double, List<String>> map: documentDataByScore.entrySet()){
            double key = map.getKey();
            for(String s:map.getValue()){
                System.out.println("books: "+s+" score:"+key);
            }
        }
    }

    private static List<String> getResourceFiles(String path) throws IOException {
        List<String> filenames = new ArrayList<>();

        try (
                InputStream in = getResourceAsStream(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                    String resource;
                    while ((resource = br.readLine()) != null) {
                        filenames.add(BOOKS_DIR+"/"+resource);
                    }
                }

        return filenames;
    }

    private static InputStream getResourceAsStream(String resource) {
        final InputStream in
                = getContextClassLoader().getResourceAsStream(resource);

        return in == null ? SequentialSearch.class.getResourceAsStream(resource) : in;
    }

    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
}
