package org.leader.leaderfindalgo;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Main implements Watcher {
    private static final String ZOOKEEPER_ADDR = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NODE = "/parent";
    private ZooKeeper zooKeeper = null;
    private static int port = 8000;

    public static void main(String[] args) {
        int curPort = args.length == 1? Integer.parseInt(args[0]):port;
        Main main_class = new Main();
        ZooKeeper zooKeeper = main_class.connectZookeeper();

        ServiceRegistry serviceRegistry = new ServiceRegistry(zooKeeper);
        OnElectionAction onElectionAction = new OnElectionAction(serviceRegistry, curPort);
        LeaderElectionAlgo leaderElectionAlgo = new LeaderElectionAlgo(zooKeeper, onElectionAction);
        leaderElectionAlgo.volunteerForElection();
        leaderElectionAlgo.selectLeader();

        main_class.run();
        main_class.close();
        System.out.println("Disconnected from zookeeper");
    }

    public ZooKeeper connectZookeeper(){
        try {
            this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDR,SESSION_TIMEOUT, this);
            return zooKeeper;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void run(){
        synchronized (zooKeeper){
            try {
                zooKeeper.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(){
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("connected to zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("disconnection event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }
}
