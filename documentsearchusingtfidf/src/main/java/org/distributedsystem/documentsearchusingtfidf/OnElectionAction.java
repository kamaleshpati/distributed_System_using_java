package org.distributedsystem.documentsearchusingtfidf;

import org.apache.zookeeper.KeeperException;
import org.distributedsystem.documentsearchusingtfidf.cluster.OnElectionCallback;
import org.distributedsystem.documentsearchusingtfidf.cluster.ServiceRegistry;
import org.distributedsystem.documentsearchusingtfidf.networksevices.WebClient;
import org.distributedsystem.documentsearchusingtfidf.networksevices.WebServer;
import org.distributedsystem.documentsearchusingtfidf.search.SearchCoordinator;
import org.distributedsystem.documentsearchusingtfidf.search.SearchWorker;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry workerServiceRegistry;
    private final ServiceRegistry coordinatorsServiceRegistry;
    private final int port;
    private WebServer webServer;

    public OnElectionAction(ServiceRegistry serviceRegistry, ServiceRegistry coordinatorsServiceRegistry, int port) {
        this.workerServiceRegistry = serviceRegistry;
        this.coordinatorsServiceRegistry = coordinatorsServiceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        workerServiceRegistry.unregisterFromCluster();
        workerServiceRegistry.registerForUpdates();
        if(webServer != null){
            webServer.stop();
        }
        SearchCoordinator searchCoordinator = new SearchCoordinator(workerServiceRegistry, new WebClient());
        webServer = new WebServer(port, searchCoordinator);
        webServer.startServer();


        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, searchCoordinator.getEndpoint());
            coordinatorsServiceRegistry.registerToCluster(currentServerAddress);
        } catch (InterruptedException | UnknownHostException | KeeperException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWorker() {
        SearchWorker searchWorker = new SearchWorker();
        if (webServer == null) {
            webServer = new WebServer(port, searchWorker);
            webServer.startServer();
        }

        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port,searchWorker.getEndpoint());

            workerServiceRegistry.registerToCluster(currentServerAddress);
        } catch (InterruptedException | UnknownHostException | KeeperException e) {
            e.printStackTrace();
        }

    }
}
