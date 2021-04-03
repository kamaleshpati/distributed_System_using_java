package org.leader.leaderfindalgo;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ServiceRegistry implements Watcher {
    private static final String REGISTRY_NODE = "/service_registry";
    private final ZooKeeper zooKeeper;
    private String currNode = null;
    private List<String> allServicesAddrs;
    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createServiceRegistry();
    }

    public void registerToCluster(String metadata){
        try {
            this.currNode = zooKeeper.create(REGISTRY_NODE+"/c_", metadata.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("registered to cluster");
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createServiceRegistry(){
        try {
            if(zooKeeper.exists(REGISTRY_NODE, false) == null){
                zooKeeper.create(REGISTRY_NODE,new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private synchronized void updateAddr() throws KeeperException, InterruptedException {
        List<String> workers = zooKeeper.getChildren(REGISTRY_NODE,this);
        List<String> ADDRS = new ArrayList<>(workers.size());
        for(String s: workers ){
            String str = REGISTRY_NODE+"/"+s;
            Stat stat = zooKeeper.exists(str,false);
            if(stat == null){
                continue;
            }
            byte[] addressData = zooKeeper.getData(str, false, stat);
            String data = new String(addressData);
            ADDRS.add(data);
        }
        this.allServicesAddrs = Collections.unmodifiableList(ADDRS);
        System.out.println("cluster "+this.allServicesAddrs);
    }

    public synchronized List<String> getAllServicesAddrsFromCluster(){
        if(allServicesAddrs == null){
            try {
                updateAddr();
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return allServicesAddrs;
    }

    public void regForUpdate(){
        try {
            updateAddr();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddr();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void unregister() throws KeeperException, InterruptedException {
        if(currNode != null && zooKeeper.exists(currNode,false) != null){
            zooKeeper.delete(currNode, -1);
        }
    }
}
