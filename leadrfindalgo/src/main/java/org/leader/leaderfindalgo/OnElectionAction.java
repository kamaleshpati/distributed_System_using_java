package org.leader.leaderfindalgo;

import org.apache.zookeeper.KeeperException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback{
    private final ServiceRegistry serviceRegistry;
    private final int port;

    public OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBe() {

        try {
            serviceRegistry.unregister();
            serviceRegistry.regForUpdate();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void worker() {
        try {
            String cuurentServerAddr = String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(),port);
            serviceRegistry.registerToCluster(cuurentServerAddr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
