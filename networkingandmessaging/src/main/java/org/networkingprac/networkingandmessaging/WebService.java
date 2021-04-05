package org.networkingprac.networkingandmessaging;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.Executors;

public class WebService {
    private static final String taskEndpoint = "/task";
    private static final String statusEndpoint = "/status";

    private final int port;
    private HttpServer httpServer;

    public WebService(int port) {
        this.port = port;
    }

    public void startServer(){
        try {
            this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        HttpContext statusContext = httpServer.createContext(statusEndpoint);
        HttpContext taskContext = httpServer.createContext(taskEndpoint);

        statusContext.setHandler(this::handleStatusCheckRequest);
        taskContext.setHandler(this::handleTaskCheckRequest);

        this.httpServer.setExecutor(Executors.newFixedThreadPool(4));
        this.httpServer.start();

    }

    private void handleTaskCheckRequest(HttpExchange httpExchange) throws IOException {
        if(!httpExchange.getRequestMethod().equalsIgnoreCase("post")){
            httpExchange.close();
            return;
        }
        Headers headers = httpExchange.getRequestHeaders();
        if(headers.containsKey("X-Test") && headers.get("X-test").get(0).equalsIgnoreCase("true")){
            String dummyResp = "123\n";
            sendResponse(dummyResp.getBytes(), httpExchange);
            return;
        }
        boolean isDebugMode = false;
        if(headers.containsKey("X-Debug") && headers.get("X-Debug").get(0).equalsIgnoreCase("true")){
            isDebugMode = true;

        }
        long startTime = System.nanoTime();
        byte[] reqBytes = IOUtils.readAllBytes(httpExchange.getRequestBody());
        byte[] respByte = calculateResp(reqBytes);
        long finishTime = System.nanoTime();

        if(isDebugMode){
            String debugMessage = String.format("op took %s",(finishTime-startTime));
            httpExchange.getResponseHeaders().put("X-Debug-info", Collections.singletonList(debugMessage));
        }

        sendResponse(respByte, httpExchange);
    }

    private byte[] calculateResp(byte[] reqBytes) {
        String bodyString = new String(reqBytes);
        String[] liNum = bodyString.split(",");
        BigInteger res = BigInteger.ONE;
        for (String st: liNum){
            BigInteger bigInteger = new BigInteger(st);
            res = res.multiply(bigInteger);
        }

        return String.format("Result of multiplication %s \n", res).getBytes();
    }

    private void handleStatusCheckRequest(HttpExchange httpExchange){
        if(!httpExchange.getRequestMethod().equalsIgnoreCase("get")){
            httpExchange.close();
            return;
        }
        String responseMessage = "Server is alive";
        sendResponse(responseMessage.getBytes(), httpExchange);
    }

    private void sendResponse(byte[] bytes, HttpExchange httpExchange) {
        try {
            httpExchange.sendResponseHeaders(200,bytes.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int serverPort = 8080;
        if(args.length == 1){
            serverPort = Integer.parseInt(args[0]);
        }
        WebService webService = new WebService(serverPort);
        webService.startServer();
        System.out.println("setup completed");
        System.out.println("server is listening to "+ serverPort);
    }
}
