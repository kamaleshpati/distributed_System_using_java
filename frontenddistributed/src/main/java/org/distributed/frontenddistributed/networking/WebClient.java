package org.distributed.frontenddistributed.networking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class WebClient {
    private HttpURLConnection con;

    public WebClient() {}


    public CompletableFuture sendTask(String url, byte[] requestPayload) throws IOException {
        URL urlSite = new URL(url);
        this.con = (HttpURLConnection) urlSite.openConnection();
        this.con.setRequestMethod("POST");
        this.con.setRequestProperty("X-Debug", "true");

        this.con.setDoOutput(true);

        OutputStream outputStream = this.con.getOutputStream();
        outputStream.write(requestPayload);
        outputStream.flush();
        outputStream.close();

        CompletableFuture<? extends CharSequence> responseString = CompletableFuture.supplyAsync(() -> {
            try {
                this.con.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int responseCode = 0;
            try {
                responseCode = this.con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(new InputStreamReader(this.con.getInputStream()));
                    String inputLine = null;
                    StringBuilder response = new StringBuilder();

                    while (true) {
                        try {
                            if ((inputLine = in.readLine()) == null) break;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        response.append(inputLine);
                    }
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    return  "POST request not worked";
                }
            } catch (IOException e) {
                return (e.getMessage());
            }
        });

        return responseString;
    }
}
