package org.leader.leaderfindalgo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class LeaderElectionAlgo implements Watcher {
    private static final String ZOOKEEPER_ADDR = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NODE = "/parent";
    private ZooKeeper zooKeeper;
    private String currZnode;

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
            case NodeDeleted:
                selectLeader();
        }

    }

    public void volunteerForElection(){
        String znodePrefix = ELECTION_NODE + "/c_";
        try {
            String znodeFullpath = zooKeeper.create(znodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("znode name" + znodeFullpath);
            this.currZnode = znodeFullpath.replace(ELECTION_NODE+"/", "");

        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void selectLeader(){
        try {
            String predecessorNode = "";
            Stat predecessorNodeStat = null;
            while (predecessorNodeStat == null) {
                List<String> children = zooKeeper.getChildren(ELECTION_NODE, false);
                Collections.sort(children);
                String smallest = children.get(0);

                if (smallest.equals(currZnode)) {
                    System.out.println("I am the leader"+currZnode);
                    return;
                } else {
                    System.out.println("I am not the leader " + smallest + " is leader");
                    int predecessorIndex = Collections.binarySearch(children, currZnode) - 1;
                    predecessorNode = children.get(predecessorIndex);
                    predecessorNodeStat = zooKeeper.exists(ELECTION_NODE + "/" + predecessorNode, this);


                }
                System.out.println("watching " + predecessorNodeStat);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        LeaderElectionAlgo leaderElectionAlgo = new LeaderElectionAlgo();
        leaderElectionAlgo.connectZookeeper();
        leaderElectionAlgo.volunteerForElection();
        leaderElectionAlgo.selectLeader();
        leaderElectionAlgo.run();
        leaderElectionAlgo.close();
        System.out.println("Disconnected from zookeeper");
    }
}
