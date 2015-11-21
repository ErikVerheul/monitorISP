/*
 * Monitor the availability of the ISP by checking if at least one of a list of selectedHostsURLs on the Internet can be reached. 
 */
package nl.verheulconsultants.monitorisp.service;

import static nl.verheulconsultants.monitorisp.service.Utilities.isValidHostAddress;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISPController extends Thread {
    
    private static final long STARTOFSERVICE = System.currentTimeMillis();
    private static long lastContactWithAnyHost = System.currentTimeMillis();
    private static long lastFail = 0L;
    static long successfulChecks = 0L;
    static long failedChecks = 0L;
    private static long numberOfInterruptions = 0L;
    private static long totalISPunavailability = 0L;
    private static boolean canReachISP = true;
    private static boolean busyCheckingConnections = false;
    private static final List<OutageListItem> OUTAGES = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ISPController.class);
    private boolean running = false;
    private boolean stop = false;
    private boolean exit = false;
    private List<String> selectedHostsURLs = new ArrayList<>();
    private int outageIndex = 0;
    private long outageStart = 0L;
    private long outageEnd;
    static String routerAddress = "unknown";
    static boolean outageTypeIsp = false;
    

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
        return busyCheckingConnections;
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
         * Outer loop is always loping unless exit = true. When loping started = true
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
            sleepMilis(1000);
        } while (!exit);

        running = false;
    }
    
    public static String getRouterAddress() {
        return routerAddress;
    }
    
    public static void setRouterAddress (String address) {
        routerAddress = address;
    }

    /**
     * Inner loop checking if connections to the hosts are possible When loping busyCheckingConnections = true Registers the periods when no connections could
     * be made.
     */
    void innerLoop() {
        long loopStart;
        long loopEnd;

        while (!exit && !stop) {
            busyCheckingConnections = true;
            loopStart = System.currentTimeMillis();
            if (checkISP(selectedHostsURLs)) {
                canReachISP = true;
                lastContactWithAnyHost = System.currentTimeMillis();
                if (outageStart > 0L) {
                    outageEnd = lastContactWithAnyHost;
                    OUTAGES.add(new OutageListItem(outageIndex, millisToTime(outageStart), millisToTime(outageEnd)));
                    outageIndex++;
                    outageStart = 0L;
                }
            } else {
                if (canReachISP) {
                    numberOfInterruptions++;
                    outageStart = System.currentTimeMillis();
                    outageTypeIsp = canConnectRouter(routerAddress);
                }
                canReachISP = false;
                LOGGER.warn("The ISP cannot be reached.");
                lastFail = System.currentTimeMillis();
            }
            // wait 5 seconds to check the ISP connection again
            sleepMilis(5000);
            loopEnd = System.currentTimeMillis();
            if (!canReachISP) {
                totalISPunavailability = totalISPunavailability + loopEnd - loopStart;
            }
        }
        if (busyCheckingConnections) {
            LOGGER.info("The controller has stopped.\n");
            LOGGER.info("{} Connection checks are executed, {} were successful.",
                    successfulChecks + failedChecks, successfulChecks);
        }
        busyCheckingConnections = false;
    }
    
    /**
     * Perform the connection checks in a separate thread.
     * 
     * @param hosts to check
     */
    public void doInBackground(List<String> hosts) {
        this.selectedHostsURLs = hosts;
        start();
    }
    
    private boolean canConnectRouter(String routerIP) {
        // if the router address is not set we can not exclude internal network failure
        if (routerIP.equalsIgnoreCase("unknown")) return true;
        // if the router address is not avalid address we can not exclude internal network failure
        if (!isValidHostAddress(routerIP)) {
            LOGGER.warn("The router address {} is not valid. The internal network error detection is omitted", routerIP);
            return true;
        }      
        return checkISP(new ArrayList<>(Arrays.asList(routerIP)));  
    }

    /**
     * Try to connect to any host in the list
     *
     * @return true if a host can be contacted and false if not one host from the list can be reached.
     */
    boolean checkISP(List<String> hURLs) {
        boolean hostFound = false;
        for (String host : hURLs) {
            // test a TCP connection on port 80 with the destination host and a time-out of 2000 ms.
            if (testConnection(host, 80, 2000)) {
                hostFound = true;
                successfulChecks++;
                // when successfull there is no need to try the other selectedHostsURLs
                break;
            } else {
                failedChecks++;
                // wait 1 second before contacting the next host in the list
                sleepMilis(1000);
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
     * Put this thread to sleep for ms miliseconds. Slice the sleep to exit fast in case of a stop or exit.
     *
     * @param ms the sleep time
     */
    void sleepMilis(long ms) {
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

    public List getStatusData() {
        List ret = new ArrayList();

        StatusListItem x0 = new StatusListItem();
        x0.name = "startOfService";
        x0.value = new Date(STARTOFSERVICE).toString();
        x0.index = 1;
        ret.add(x0);

        StatusListItem x1 = new StatusListItem();
        x1.name = "lastContactWithAnyHost";
        x1.value = new Date(lastContactWithAnyHost).toString();
        x1.index = 2;
        ret.add(x1);

        StatusListItem x2 = new StatusListItem();
        x2.name = "lastFail";
        if (lastFail > 0) {
            x2.value = new Date(lastFail).toString();
        } else {
            x2.value = "No failure yet";
        }
        x2.index = 3;
        ret.add(x2);

        StatusListItem x3 = new StatusListItem();
        x3.name = "numberOfInterruptions";
        x3.value = Long.toString(numberOfInterruptions);
        x3.index = 4;
        ret.add(x3);

        StatusListItem x4 = new StatusListItem();
        x4.name = "failedChecks";
        x4.value = Long.toString(failedChecks);
        x4.index = 5;
        ret.add(x4);

        StatusListItem x5 = new StatusListItem();
        x5.name = "successfulChecks";
        x5.value = Long.toString(successfulChecks);
        x5.index = 6;
        ret.add(x5);

        StatusListItem x6 = new StatusListItem();
        x6.name = "totalISPunavailability";
        x6.value = millisToTime(totalISPunavailability);
        x6.index = 7;
        ret.add(x6);

        StatusListItem x7 = new StatusListItem();
        x7.name = "INTERNET UP?";
        if (busyCheckingConnections) {
            x7.value = Boolean.toString(canReachISP);
        } else {
            x7.value = "UNKNOWN, conroller is not running";
        }
        x7.index = 8;
        ret.add(x7);

        return ret;
    }

    public List getOutageData() {
        return OUTAGES;
    }

    private String millisToTime(long millis) {
        long second = 0;
        long minute = 0;
        long hour = 0;
        if (millis > 0) {
            second = (millis / 1000) % 60;
            minute = (millis / (1000 * 60)) % 60;
            hour = (millis / (1000 * 60 * 60)) % 24;
        }
        return String.format("%02d:%02d:%02d", hour, minute, second) + " [h:m:s]";
    }

}
