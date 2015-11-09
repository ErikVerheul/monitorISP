/*
 * Switch to the other ISP is the current ISP is not reachable. When on the backup ISP try to revert to the primary ISP.
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

/**
 *
 * @author erik
 */
public class ISPController extends Thread {
    static final Logger logger = LoggerFactory.getLogger(ISPController.class);
    private boolean done = false;
    private boolean stop = false;
    private boolean exit = false;
    List<String> hosts = new ArrayList<>();
    long lastContactWithAnyHost = System.currentTimeMillis();
    long successfulChecks = 0L;
    long failedChecks = 0L;

    public boolean isRunning() {
        return !done;
    }

    public void stopTemporarely() {
        stop = true;
    }

    public void restart() {
        done = false;
        stop = false;
    }

    public void exit() {
        stop = true;
        exit = true;
    }

    public boolean isStoppedTemporarely() {
        return stop;
    }

    @Override
    @SuppressWarnings("static-access")
    public void run() {
        done = false;
        stop = false;
        logger.info("De controller is gestart.");

        do {
            if (hosts.size() > 0) {
                while (!stop) {
                    if (!checkISP()) {
                        logger.warn("De ISP is niet bereikbaar.");
                    }
                    waitMilis(5000);  // wait 5 seconds to check the ISP connection again
                }
            }
            if (!done) {
                logger.info("De controller is gestopt.\n");
                logger.info("Er zijn {} connectie checks uitgevoerd, waarvan {} succesvol.", (successfulChecks+failedChecks), successfulChecks);
            }
            done = true;
            // wait for instructions to restart or to exit completely
            waitMilis(1000);
        } while (!exit);
    }

    public void doInBackground(List hosts) {
        this.hosts = hosts;
        start();
    }

    /**
     * Try to connect to any host in the list
     *
     * @return true if a host can be contacted and false if not one host from
     * the list can be reached.
     */
    boolean checkISP() {
        boolean hostFound = false;
        for (String host : hosts) {
            // test a TCP connection on port 80 with the destination host and a time-out of 2000 ms.
            if (testConnection(host, 80, 2000)) {
                lastContactWithAnyHost = System.currentTimeMillis();
                hostFound = true;
                successfulChecks++;
                break; // when successfull there is no need to try the other hosts
            } else {
                failedChecks++;
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
        } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ignore1) {
            // is OK, interrupt by thread cancellation
        }
        
    }
}
