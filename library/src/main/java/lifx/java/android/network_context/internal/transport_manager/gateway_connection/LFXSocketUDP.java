//
//  LFXSocketUDP.java
//  LIFX
//
//  Created by Jarrod Boyes on 24/03/14.
//  Copyright (c) 2014 LIFX Labs. All rights reserved.
//

package lifx.java.android.network_context.internal.transport_manager.gateway_connection;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import android.os.Handler;
import android.os.Message;

import lifx.java.android.util.LFXLog;

public class LFXSocketUDP extends LFXSocketGeneric {
    private final static String TAG = LFXSocketUDP.class.getSimpleName();
    // Variables for receiving
    private Thread asyncReceiveThread;

    // Variables for sending
    private Thread asyncSendThread;
    public byte[] sendBuffer;

    private boolean isBroadcast = false;
    private byte[] broadcastIpAddress;
    private int broadcastPort;

    private DatagramSocket updSocket = null;

    public LFXSocketUDP() {
        super();
        if (LFXLog.isDebugEnabled()) LFXLog.d(TAG,"LFXSocketUDP() - Constructor");
    }

    public class MessageSendTask implements Runnable {
        private SocketMessage[] messages;

        public MessageSendTask(SocketMessage[] messages) {
            this.messages = messages;
        }

        @Override
        public void run() {
            for (SocketMessage message : messages) {
                DatagramSocket dataGramSocket = null;

                try {
                    byte[] messageData = message.getMessageData();

                    dataGramSocket = new DatagramSocket();
                    dataGramSocket.setReuseAddress(true);

                    DatagramPacket udpPacket;

                    InetAddress ipAddress;
                    int port;

                    ipAddress = InetAddress.getByAddress(message.getIpAddress());
                    port = message.getPort();

                    udpPacket = new DatagramPacket(messageData, messageData.length, ipAddress, port);

                    if (!dataGramSocket.isClosed()) {
                        dataGramSocket.send(udpPacket);
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                    close();
                }
                finally {
                    dataGramSocket.close();
                }

            }
        }
    }

    public void sendMessages(SocketMessage[] messages) {
        asyncSendThread = new Thread(new MessageSendTask(messages), "asyncSendThread");
        asyncSendThread.start();
    }

    private class UDPReceiveTask implements Runnable {
        private Handler handler;

        public UDPReceiveTask(Handler handler) {
            super();
            this.handler = handler;
        }

        public void publishProgress(SocketMessage[] messages) {
            Message msg = handler.obtainMessage();
            msg.what = SOCKET_RECEIVED_MESSAGE;
            msg.obj = messages[0];
            handler.sendMessage(msg);
        }

        public void publishConnected() {
            Message msg = handler.obtainMessage();
            msg.what = SOCKET_IS_CONNECTED;
            handler.sendMessage(msg);
        }

        public void publishConnecting() {
            Message msg = handler.obtainMessage();
            msg.what = SOCKET_IS_CONNECTING;
            handler.sendMessage(msg);
        }

        public void publishDisconnected() {
            Message msg = handler.obtainMessage();
            msg.what = SOCKET_IS_DISCONNECTED;
            handler.sendMessage(msg);
        }

        @Override
        public void run() {
            byte[] messageData = new byte[1024];
            DatagramPacket udpPacket = new DatagramPacket(messageData, messageData.length);

            try {
                boolean serverStarted = false;

                publishConnecting();

                InetAddress ipAddress = null;
                int port = 0;

                try {
                    if (updSocket == null || updSocket.isClosed()) {
                        updSocket = new DatagramSocket(null);

                        if (isBroadcast) {
                            ipAddress = InetAddress.getByAddress(broadcastIpAddress);
                            port = broadcastPort;
                            updSocket.setBroadcast(true);
                        } else {
                            ipAddress = InetAddress.getByAddress(getIpAddress());
                            port = getPort();
                        }

                        updSocket.setReuseAddress(true);
                        updSocket.bind(new InetSocketAddress(port));

                        serverStarted = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (LFXLog.isDebugEnabled()) LFXLog.d(TAG, "UDPReceiveTask() - Error Starting server. IP: " + ipAddress.toString() + ", Port: " + port);
                    updSocket = null;
                }

                if (LFXLog.isDebugEnabled()) LFXLog.d(TAG, "UDPReceiveTask() - UDP SOCKET MONITOR IS ONLINE - ip: " + ipAddress.toString() + ", port: " + port);

                if (serverStarted) {
                    setServerRunning(true);
                    publishConnected();
                } else {
                    setServerRunning(false);
                }

                while (getServerRunning()) {
                    updSocket.receive(udpPacket);

                    byte[] messageAddress = udpPacket.getAddress().getAddress();
                    int messagePort = udpPacket.getPort();
                    SocketMessage message = new SocketMessage(messageData, messageAddress, messagePort);

                    SocketMessage[] messages = new SocketMessage[]{message};
                    publishProgress((SocketMessage[]) messages);
                }
            } catch (Exception e) {
                if (LFXLog.isDebugEnabled()) LFXLog.d(TAG, "UDPReceiveTask() - UDP Socket has been closed.");
            }

            close();

            publishDisconnected();

            if (LFXLog.isDebugEnabled()) LFXLog.d(TAG, "UDPReceiveTask() - UDP Socket Monitor Ended Execution.");
        }
    }

    @Override
    public void close() {
        if (LFXLog.isDebugEnabled()) LFXLog.d(TAG, "close()");
        super.close();

        if (updSocket != null) {
            updSocket.close();
        }
    }

    public void connect(byte[] ipAddress, int port) {
        bind(ipAddress, port);

        if (asyncReceiveThread == null || !asyncReceiveThread.isAlive()) {
            state = SocketState.CONNECTING;
            asyncReceiveThread = new Thread(new UDPReceiveTask(handler), "asyncReceiveThread");
            asyncReceiveThread.start();
        }
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.UDP;
    }
}