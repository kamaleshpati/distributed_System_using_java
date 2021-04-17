package org.distributedsystem.documentsearchusingtfidf.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DocData implements Serializable {
    private Map<String, Double> termToFreq = new HashMap<>();
    public void putTermFreq(String term, double freq){
        termToFreq.put(term,freq);
    }
    public double getFreq(String term){
        return termToFreq.get(term);
    }
}
