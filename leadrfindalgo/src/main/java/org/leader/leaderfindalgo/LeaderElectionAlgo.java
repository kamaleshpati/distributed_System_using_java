package org.leader.leaderfindalgo;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class LeaderElectionAlgo implements Watcher {
    private static final String ZOOKEEPER_ADDR = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;

    public void connectZookeeper(){
        try {
            this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDR,SESSION_TIMEOUT,this);

        } catch (IOException e) {
            e.printStackTrace();
        }
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
        switch (watchedEvent.getType()){
            case None:
                if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("connected to zookeeper");
                } else {
                    synchronized (zooKeeper){
                        System.out.println("disconnection event");
                        zooKeeper.notifyAll();
                    }
                }
        }

    }

    public static void main(String[] args) {
        LeaderElectionAlgo leaderElectionAlgo = new LeaderElectionAlgo();
        leaderElectionAlgo.connectZookeeper();
        leaderElectionAlgo.run();
        leaderElectionAlgo.close();
        System.out.println("Disconnected from zookeeper");
    }
}
