package org.distributedsystem.documentsearchusingtfidf.models;

import java.util.HashMap;
import java.util.Map;

public class DocData {
    private Map<String, Double> termToFreq = new HashMap<>();
    public void putTermFreq(String term, double freq){
        termToFreq.put(term,freq);
    }
    public double getFreq(String term){
        return termToFreq.get(term);
    }
}
