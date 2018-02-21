package com.app.ganzara.udpsender.model;


public class EndPoint {
    public String ipAddress;
    public int port;

    public EndPoint(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }
}
