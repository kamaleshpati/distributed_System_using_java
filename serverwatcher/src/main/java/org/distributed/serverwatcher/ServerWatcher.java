package org.distributed.serverwatcher;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ServerWatcher implements Watcher{
    private static final String ZOOKEEPER_ADDR = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String TARGET_NODE = "/target";

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
                break;
            case NodeDeleted:
                System.out.println(TARGET_NODE + " was deleted");
                break;
            case NodeCreated:
                System.out.println(TARGET_NODE + " created");
                break;
            case NodeDataChanged:
                System.out.println(TARGET_NODE +" data changed");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_NODE +" child changed");
                break;

        }

        watchTarget();

    }

    public void watchTarget(){
        try {
            Stat stat = zooKeeper.exists(TARGET_NODE, this);
            if(stat == null){
                return;
            }
            byte[] bytes = zooKeeper.getData(TARGET_NODE,this,stat);
            List<String> childern = zooKeeper.getChildren(TARGET_NODE,this);
            System.out.println("DATA "+ Arrays.toString(bytes) + " childern "+childern);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        ServerWatcher serverWatcher = new ServerWatcher();
        serverWatcher.connectZookeeper();
        serverWatcher.watchTarget();
        serverWatcher.run();
        serverWatcher.close();
        System.out.println("Disconnected from zookeeper");
    }


}
