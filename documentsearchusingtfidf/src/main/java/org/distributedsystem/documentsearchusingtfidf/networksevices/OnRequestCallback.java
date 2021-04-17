package org.distributedsystem.documentsearchusingtfidf.networksevices;

import java.io.IOException;

public interface OnRequestCallback {
    byte[] handleRequest(byte[] requestPayload) throws IOException;

    String getEndpoint();
}
