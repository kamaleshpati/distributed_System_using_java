package org.distributedsystem.documentsearchusingtfidf.search;

import org.distributedsystem.documentsearchusingtfidf.model.DocData;

import java.util.*;

public class TFIDF {
    public static double calculateTermFreq(List<String> wordFromDoc, String term){
        long count = wordFromDoc.stream().filter(s -> s.equalsIgnoreCase(term)).count();
        return (double) count/wordFromDoc.size();
    }

    public static DocData createDocData(List<String> wordFromDoc, List<String> terms){
        DocData docData = new DocData();
        for (String s: terms){
            double termFreq = calculateTermFreq(wordFromDoc,s);
            docData.putTermFreq(s,termFreq);
        }

        return docData;
    }

    private static double getInverseDocFreq(String term, Map<String, DocData> docDataMap){
        double nt = 0;
        for (String s: docDataMap.keySet()){
            DocData docData = docDataMap.get(s);
            try {
                double freq = docData.getFreq(s);
                if(freq > 0.0){
                    nt++;
                }
            }
            catch (NullPointerException nullPointerException){
                nt = 0;
            }
        }
        return nt == 0?0:Math.log10(docDataMap.size()/nt);
    }

    private static Map<String,Double> getTermToIDF(List<String> terms, Map<String,DocData> docDataMap){
        Map<String,Double> termToIDF = new HashMap<>();
        terms.forEach(st -> {
            double idf = getInverseDocFreq(st, docDataMap);
            termToIDF.put(st, idf);
        });
        return termToIDF;
    }

    private static double calculateDocScore(List<String> terms, DocData docData, Map<String,Double> termToIDF){
        double score = 0 ;
        for (String st: terms){
            double termFreq = docData.getFreq(st);
            double inverseTermfreq = termToIDF.get(st);
            score += termFreq+inverseTermfreq;
        }
        return score;
    }

    public static Map<Double,List<String>> getDocumentSortedByScore(List<String> terms, Map<String,DocData> docDataMap){
        TreeMap<Double,List<String>> scoreToDoc = new TreeMap<>();
        Map<String, Double> termToIDF = getTermToIDF(terms,docDataMap);
        for (String doc: docDataMap.keySet()){
            DocData docData = docDataMap.get(doc);
            double score = calculateDocScore(terms,docData,termToIDF);
            addDocScoreToTreeMap(scoreToDoc,score,doc);
        }
        return scoreToDoc.descendingMap();
    }

    private static void addDocScoreToTreeMap(TreeMap<Double, List<String>> scoreToDoc, double score, String doc) {
        List<String> docWithCurrScore = scoreToDoc.get(score);
        if(docWithCurrScore == null){
            docWithCurrScore = new ArrayList<>();
        }
        docWithCurrScore.add(doc);
        scoreToDoc.put(score,docWithCurrScore);
    }

    public static List<String> getWordsFromLine(String line) {
        return Arrays.asList(line.split("(\\.)+|(,)+|( )+|(-)+|(\\?)+|(!)+|(;)+|(:)+|(/d)+|(/n)+"));
    }

    public static List<String> getWordsFromDocument(List<String> lines) {
        List<String> words = new ArrayList<>();
        lines.stream().map(TFIDF::getWordsFromLine).forEach(words::addAll);
        return words;
    }
}
