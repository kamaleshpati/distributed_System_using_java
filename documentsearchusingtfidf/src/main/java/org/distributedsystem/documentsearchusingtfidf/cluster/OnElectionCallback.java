package org.distributedsystem.documentsearchusingtfidf.cluster;

public interface OnElectionCallback {
    void onElectedToBeLeader();

    void onWorker();
}
