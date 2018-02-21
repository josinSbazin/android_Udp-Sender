package com.app.ganzara.udpsender.model;

import android.content.Context;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class UDPHelper extends Thread {

    public UDPHelper(){
    }

    public String sendAndWait(String msg, EndPoint endPoint, int timeout) throws IOException {
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddres = InetAddress.getByName(endPoint.ipAddress);
        byte[] sendData = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddres, endPoint.port);

        byte[] buf = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);

        int croppedTimeout = timeout - 100;
        if (croppedTimeout >= 0) {
            clientSocket.setSoTimeout(croppedTimeout);
        }

        clientSocket.send(sendPacket);

        while (true) {
            try {
                clientSocket.receive(receivePacket);
                return "received from " + receivePacket.getAddress() + ", " + receivePacket.getPort() + ": " + new String(receivePacket.getData(), 0, receivePacket.getLength());
            } catch (SocketTimeoutException e) {
                // timeout exception.
                clientSocket.close();
                break;
            }
        }
        return null;
    }

    @Override
    public void run() {

    }
}
