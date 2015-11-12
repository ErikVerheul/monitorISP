/*
 * Monitor the availability of the ISP by checking if at least one of a list of selectedHostsURLs on the Internet can be reached. 
 */
package nl.verheulconsultants.monitorisp.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISPController extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISPController.class);
    private boolean running = false;
    private boolean stop = false;
    private boolean exit = false;
    private List<String> selectedHostsURLs = new ArrayList<>();

    /**
     * The service has started and is running.
     *
     * @return true if running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * The service is busy checking connections.
     *
     * @return true if running.
     */
    public boolean isBusyCheckingConnections() {
        return Status.busyCheckingConnections;
    }

    /**
     * Stop checking connections temporarily.
     */
    public void stopTemporarily() {
        stop = true;
    }

    /**
     * Restart after temporarily stop.
     *
     * @param hosts
     */
    public void restart(List<String> hosts) {
        this.selectedHostsURLs = hosts;
        stop = false;
    }

    /**
     * Stop the service thread completely.
     */
    public void exit() {
        stop = true;
        exit = true;
    }

    @Override
    @SuppressWarnings("static-access")
    public void run() {
        running = true;
        stop = false;
        LOGGER.info("The controller has started.");

        /**
         * Outer loop is always loping unless exit = true. When loping started =
         * true
         */
        do {
            if (!selectedHostsURLs.isEmpty()) {
                innerLoop();
            } else {
                LOGGER.warn("Cannot run the service with an empty host list");
                exit = true;
                break;
            }
            // wait for instructions to restart or to exit completely
            waitMilis(1000);
        } while (!exit);

        running = false;
    }

    /**
     * Inner loop checking if connections to the hosts are possible When loping
     * busyCheckingConnections = true Registers the periods when no connections
     * could be made.
     */
    void innerLoop() {
        long loopStart;
        long loopEnd;
        while (!exit && !stop) {
            long outageStart = 0L;
            long outageEnd = 0L;
            Status.busyCheckingConnections = true;
            loopStart = System.currentTimeMillis();
            if (checkISP(selectedHostsURLs)) {
                Status.canReachISP = true;
                Status.lastContactWithAnyHost = System.currentTimeMillis();
                if (outageStart > 0) {
                    outageEnd = Status.lastContactWithAnyHost;
                    Status.outages.add(new Outage(outageStart, outageEnd));
                    outageStart = 0L;
                }
            } else {
                if (Status.canReachISP) {
                    Status.numberOfInterruptions++;
                    outageStart = System.currentTimeMillis();
                }
                Status.canReachISP = false;
                LOGGER.warn("The ISP cannot be reached.");
                Status.lastFail = System.currentTimeMillis();
            }
            // wait 5 seconds to check the ISP connection again
            waitMilis(5000);
            loopEnd = System.currentTimeMillis();
            if (!Status.canReachISP) {
                Status.totalISPunavailability = Status.totalISPunavailability + loopEnd - loopStart;
            }
        }
        if (Status.busyCheckingConnections) {
            LOGGER.info("The controller has stopped.\n");
            LOGGER.info("{} Connection checks are executed, {} were successful.",
                    Status.successfulChecks + Status.failedChecks, Status.successfulChecks);
        }
        Status.busyCheckingConnections = false;
    }

    public void doInBackground(List<String> hosts) {
        this.selectedHostsURLs = hosts;
        start();
    }

    /**
     * Try to connect to any host in the list
     *
     * @return true if a host can be contacted and false if not one host from
     * the list can be reached.
     */
    boolean checkISP(List<String> hURLs) {
        boolean hostFound = false;
        for (String host : hURLs) {
            // test a TCP connection on port 80 with the destination host and a time-out of 2000 ms.
            if (testConnection(host, 80, 2000)) {
                hostFound = true;
                Status.successfulChecks++;
                // when successfull there is no need to try the other selectedHostsURLs
                break;
            } else {
                Status.failedChecks++;
                // wait 1 second before contacting the next host in the list
                waitMilis(1000);
            }
        }
        return hostFound;
    }

    /**
     * Connect using layer4 (sockets)
     *
     * @see http://www.mindchasers.com/topics/ping.htm
     * @param
     * @return true is a connection could be made within the time-out interval
     */
    private boolean testConnection(String host, Integer port, int timeout) {
        InetAddress inetAddress;
        InetSocketAddress socketAddress;
        SocketChannel sc = null;

        try {
            inetAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            LOGGER.info("The host {} is unknown. Cause = {}", new Object[]{host, e});
            return false;
        }

        try {
            socketAddress = new InetSocketAddress(inetAddress, port);
        } catch (IllegalArgumentException e) {
            LOGGER.info("The port {} can not be valid. Cause = {}", new Object[]{port, e});
            return false;
        }

        // Open the channel, set it to blocking, initiate connect
        try {
            sc = SocketChannel.open();
            sc.configureBlocking(true);
            sc.socket().connect(socketAddress, timeout);
            LOGGER.debug("{}/{} cannot be reached.", new Object[]{host, inetAddress.getHostAddress()});
            return true;
        } catch (IOException e) {
            LOGGER.info("{}/{} cannot be reached. The cause is {}", new Object[]{host, inetAddress.getHostAddress(), e});
            return false;
        } finally {
            if (sc != null) {
                try {
                    sc.close();
                } catch (IOException e) {
                    LOGGER.warn("The socket channel with host {} could not be closed. The cause is {}", new Object[]{host, e});
                }
            }
        }
    }

    /**
     * Put this thread to sleep for ms miliseconds. Slice the sleep to exit fast
     * in case of a stop or exit.
     *
     * @param ms the sleep time
     */
    void waitMilis(long ms) {
        int sliceNr = 100;
        long slice = ms / sliceNr;
        for (int i = 0; i < sliceNr && !exit && !stop; i++) {
            try {
                Thread.sleep(slice);
            } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ex) {
                LOGGER.info("A thread sleep was interrupted because of {}", ex);
            }
        }
    }
    
}
