/*
 * Monitor the availability of the ISP by checking if at leat one of a list of selectedHostsURLs on the Internet can be reached. 
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

    static final Logger logger = LoggerFactory.getLogger(ISPController.class);
    private boolean running = false;
    private boolean stop = false;
    private boolean exit = false;
    List<String> selectedHostsURLs = new ArrayList<>();

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
        logger.info("De controller is gestart.");
        long loopStart;
        long loopEnd;

        /**
         * Outer loop is always loping unless exit = true. When loping started =
         * true
         */
        do {
            if (!selectedHostsURLs.isEmpty()) {
                /**
                 * Inner loop checking if connections to the hosts are possible
                 * When loping busyCheckingConnections = true
                 */
                while (!stop) {
                    Status.busyCheckingConnections = true;
                    loopStart = System.currentTimeMillis();
                    if (checkISP(selectedHostsURLs)) {
                        Status.canReachISP = true;
                        Status.lastContactWithAnyHost = System.currentTimeMillis();
                    } else {
                        if (Status.canReachISP) {
                            Status.numberOfInterruptions++;
                        }
                        Status.canReachISP = false;
                        logger.warn("The ISP cannot be reached.");
                        Status.lastFail = System.currentTimeMillis();
                    }
                    waitMilis(5000);  // wait 5 seconds to check the ISP connection again
                    loopEnd = System.currentTimeMillis();
                    if (!Status.canReachISP) {
                        Status.totalISPunavailability = Status.totalISPunavailability + loopEnd - loopStart;
                    }
                }
                if (Status.busyCheckingConnections) {
                    logger.info("The controller has stopped.\n");
                    logger.info("{} Connection checks are executed, {} were successful.",
                            Status.successfulChecks + Status.failedChecks, Status.successfulChecks);
                }
                Status.busyCheckingConnections = false;
            } else {
                logger.warn("Cannot run the service with an empty host list");
                exit = true;
                break;
            }
            // wait for instructions to restart or to exit completely
            waitMilis(1000);
        } while (!exit);

        running = false;
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
                break; // when successfull there is no need to try the other selectedHostsURLs
            } else {
                Status.failedChecks++;
                waitMilis(1000);  // wait 1 second before contacting the next host in the list
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
            logger.info("De host {} is onbekend. Oorzaak = {}", new Object[]{host, e});
            return false;
        }

        try {
            socketAddress = new InetSocketAddress(inetAddress, port);
        } catch (IllegalArgumentException e) {
            logger.info("De poort {} kan niet valide zijn. Oorzaak = {}", new Object[]{port, e});
            return false;
        }

        // Open the channel, set it to blocking, initiate connect
        try {
            sc = SocketChannel.open();
            sc.configureBlocking(true);
            sc.socket().connect(socketAddress, timeout);
            logger.debug("{}/{} is bereikbaar.", new Object[]{host, inetAddress.getHostAddress()});
            return true;
        } catch (IOException e) {
            logger.info("{}/{} is niet bereikbaar. De oorzaak is {}", new Object[]{host, inetAddress.getHostAddress(), e});
            return false;
        } finally {
            if (sc != null) {
                try {
                    sc.close();
                } catch (IOException e) {
                    logger.warn("Het socket kanaal met host {} kon niet worden gesloten. De oorzaak is {}", new Object[]{host, e});
                }
            }
        }
    }

    /**
     * Put this thread to sleep for ms miliseconds
     *
     * @param ms the sleep time
     */
//    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "I know what I'm doing")
    void waitMilis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ex) {
            logger.info("A thread sleep was interrupted because of {}", ex);
        }

    }
}
