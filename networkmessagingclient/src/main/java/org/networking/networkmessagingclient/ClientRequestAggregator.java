package org.networking.networkmessagingclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ClientRequestAggregator {

    private static List<String> sendTask(String url, List<String> tasks) throws IOException {
        CompletableFuture[] futures = new CompletableFuture[tasks.size()];
        for (int i = 0; i < tasks.size(); i++) {
            futures[i] = new ClientRequest(url,tasks.get(i)).getResponse();
        }

        List<String> results = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            results.add(String.valueOf(futures[i].join()));
        }
        return results;

    }

    public static void main(String[] args) throws IOException {
        List<String> bodies = new ArrayList<>();
        bodies.add("50,100");
        bodies.add("5,8,9");
        bodies.add("9,826,8");
        List<String> results = ClientRequestAggregator.sendTask("http://localhost:8080/task",bodies);
        System.out.println(results);;


    }

}
