package eu.netide.core.connectivity;

import eu.netide.core.api.IBackendConnector;
import eu.netide.core.api.IConnectorListener;
import eu.netide.core.api.IShimConnector;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

/**
 * Created by timvi on 09.07.2015.
 */
public class ZeroMQBasedConnector implements IShimConnector, IBackendConnector, Runnable {

    private static final String STOP_COMMAND = "Control.STOP";
    private static final String CONTROL_ADDRESS = "inproc://ShimConnectorControl";

    private int port;
    private ZMQ.Context context;
    private Thread thread;

    private IConnectorListener shimListener;
    private IConnectorListener backendListener;

    public ZeroMQBasedConnector() {

    }

    public void Start() {
        context = ZMQ.context(1);
        thread = new Thread(this);
        thread.start();
    }

    public void Stop() {
        if (thread != null) {
            ZMQ.Socket stopSocket = context.socket(ZMQ.PUSH);
            stopSocket.connect(CONTROL_ADDRESS);
            stopSocket.send(STOP_COMMAND);
            stopSocket.close();
            try {
                thread.join();
                context.term();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("ZeroMQBasedConnector stopped.");
    }

    @Override
    public void RegisterShimListener(IConnectorListener listener) {
        this.shimListener = listener;
    }

    @Override
    public void RegisterBackendListener(IConnectorListener listener) {
        this.backendListener = listener;
    }

    @Override
    public boolean SendData(byte[] data) {
        return SendData(data, "shim");
    }

    @Override
    public boolean SendData(byte[] data, String destinationId) {
        ZMsg msg = new ZMsg();
        msg.add(destinationId);
        msg.add("");
        msg.add(data);
        // relayed via control socket to prevent threading issues
        ZMQ.Socket sendSocket = context.socket(ZMQ.PUSH);
        sendSocket.connect(CONTROL_ADDRESS);
        msg.send(sendSocket);
        sendSocket.close();
        return true;
    }

    @Override
    public void run() {
        System.out.println("ZeroMQBasedConnector started.");
        ZMQ.Socket socket = context.socket(ZMQ.ROUTER);
        socket.bind("tcp://*:" + port);
        System.out.println("Listening on port " + port);

        ZMQ.Socket controlSocket = context.socket(ZMQ.PULL);
        controlSocket.bind(CONTROL_ADDRESS);

        ZMQ.Poller poller = new ZMQ.Poller(2);
        poller.register(socket, ZMQ.Poller.POLLIN);
        poller.register(controlSocket, ZMQ.Poller.POLLIN);

        while (!Thread.currentThread().isInterrupted()) {
            poller.poll(10);
            if (poller.pollin(0)) {
                ZMsg message = ZMsg.recvMsg(socket);
                String senderId = message.getFirst().toString();
                byte[] data = message.getLast().getData();
                if (senderId.equals("shim") && shimListener != null) {
                    shimListener.OnDataReceived(data, senderId);
                } else if (backendListener != null) {
                    backendListener.OnDataReceived(data, senderId);
                }
            }
            if (poller.pollin(1)) {
                ZMsg message = ZMsg.recvMsg(controlSocket);

                if (message.getFirst().toString().equals(STOP_COMMAND)) {
                    break;
                } else {
                    message.send(socket);
                }
            }
        }
        socket.close();
        controlSocket.close();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}