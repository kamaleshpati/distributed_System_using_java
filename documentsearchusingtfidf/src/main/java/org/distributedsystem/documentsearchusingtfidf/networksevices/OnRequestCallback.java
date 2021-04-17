package org.distributedsystem.documentsearchusingtfidf.networksevices;

public interface OnRequestCallback {
    byte[] handleRequest(byte[] requestPayload);

    String getEndpoint();
}
