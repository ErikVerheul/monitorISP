/*
 * The MIT License
 *
 * Copyright (c) 2015, Verheul Consultants
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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
import java.util.Date;
import java.util.List;
import static nl.verheulconsultants.monitorisp.service.Utilities.CONTROLLERDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.SERVICEDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.INTERNAL;
import static nl.verheulconsultants.monitorisp.service.Utilities.ISP;
import static nl.verheulconsultants.monitorisp.service.Utilities.millisToTime;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The thread that checks if a given list of hosts on the Internet can be reached.
 *
 * If successful with one host it sleeps for 5 seconds to try again. If it cannot connect to any host in the list a disconnection is registered. If in this case it cannot connect
 * to the router either the disconnection is registered as a local network failure.
 *
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public class ISPController extends Thread {

    public final static int PORT = 80;
    public final static int TIMEOUT_5_SEC = 5_000;
    public final static int TIMEOUT_ONE_SEC = 1_000;
    public final static int TIMEOUT_900_MIL = 900;
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ISPController.class);
    private static final List<Host> HOSTS = new ArrayList<>();
    static final String NOROUTERADDRESS = "unknown";
    private static long currentISPunavailability = 0L;
    private static boolean canReachISP = true;
    private static boolean busyCheckingConnections = false;
    private final MonitorISPData sessionData;
    private ListModel<Host> selectedModel;
    private boolean running = false;
    // Note: do not try to make stop or exit static
    private boolean stop = false;
    private boolean exit = false;
    private List<String> selectedHostsURLs;
    private long outageStart = 0L;
    private boolean simulateISPFailure;
    private boolean simulateCannotReachRouter;
    private boolean canConnectWithRouter;
    private long controllerDownTimeStamp = 0L;

    /**
     * The controller running as a thread to check if a number of hosts can be reached.
     */
    public ISPController() {
        sessionData = new MonitorISPData();
        selectedHostsURLs = new ArrayList<>();
        simulateISPFailure = false;
        simulateCannotReachRouter = false;
    }

    /**
     *
     * @return all current session data
     */
    public MonitorISPData getSessionData() {
        return sessionData;
    }

    /**
     * @return the selected hosts.
     */
    public List<Host> getSelected() {
        return sessionData.selected;
    }

    /**
     * @return the model of the selected hosts.
     */
    public ListModel<Host> getSelectedModel() {
        return selectedModel;
    }

    /**
     * @return the palette model with all choices.
     */
    public CollectionModel<Host> getPaletteModel() {
        return sessionData.paletteModel;
    }

    /**
     * Initiate with the data of the previous session or, if not possible, with default values. ToDo: fix logging address = null in choices and selection while it's not.
     *
     * @return true if initiated with previous session data
     */
    public boolean initWithPreviousSessionData() {
        if (sessionData.loadData()) {
            selectedModel = new ListModel<>(sessionData.selected);
            LOGGER.info("Previous session data are loaded successfully.");
            LOGGER.info("The timestamp read is {}.", new Date(sessionData.timeStamp).toString());
            LOGGER.info("The choices contain now {} hosts: {}", sessionData.paletteModel.getObject().size(), sessionData.paletteModel.getObject());
            LOGGER.info("The selection contains now {} hosts: {}", sessionData.selected.size(), sessionData.selected);
            LOGGER.info("The history contains now {} records", getOutagesSize());
            return true;
        }
        // Initiate with default values.          
        LOGGER.warn("Previous session data could not be read. The choices are initiated with default values.");
        sessionData.timeStamp = 0L;
        initWithDefaults();
        return false;
    }

    /**
     * Default initialization. Three known hosts to check connections. One dummy host is added to the choices to test failed connections.
     */
    public void initWithDefaults() {
        HOSTS.clear();
        HOSTS.add(new Host("0", "willfailconnection.com"));
        Host uva = new Host("1", "uva.nl");
        HOSTS.add(uva);
        Host xs4all = new Host("2", "xs4all.nl");
        HOSTS.add(xs4all);
        Host vu = new Host("3", "vu.nl");
        HOSTS.add(vu);
        sessionData.paletteModel = new CollectionModel<>(HOSTS);

        sessionData.selected.clear();
        sessionData.selected.add(uva);
        sessionData.selected.add(xs4all);
        sessionData.selected.add(vu);
        selectedModel = new ListModel<>(sessionData.selected);
    }

    /**
     * The service has started and is running.
     *
     * @return true if running.
     */
    public boolean isRunning() {
        return running && this.isAlive();
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
        LOGGER.info("The controller thread is temporarely stopped.");
        stop = true;
    }

    /**
     * Restart after temporarily stop. Selected hosts may have changed.
     *
     * @param hosts
     */
    public void restart(List<String> hosts) {
        LOGGER.info("The controller thread is restarted.");
        this.selectedHostsURLs = hosts;
        handleControllerWasDown();
        stop = false;
    }

    /**
     * Stop the service thread completely.
     */
    public void exitService() {
        LOGGER.info("The controller thread exit was called.");
        stop = true;
        exit = true;
    }

    @Override
    public void run() {
        running = true;
        stop = false;
        LOGGER.info("The controller has started.");

        /**
         * Outer loop is always loping unless exit = true. Note that the event that the controller was not running is registered. There are two causes: 1. The controller was
         * stopped in the UI and restarted. 2. The service was down and restarted.
         */
        do {
            if (!selectedHostsURLs.isEmpty()) {
                innerLoop(selectedHostsURLs);
            } else {
                LOGGER.warn("Cannot run the service with an empty selection list");
                exit = true;
                break;
            }
            // wait for instructions to restart or to exitService completely
            sleepMillisFixed(TIMEOUT_ONE_SEC);
        } while (!exit);

        running = false;
    }

    private void handleServiceWasDown() {
        long start = sessionData.lastContactWithAnyHost;
        long now = System.currentTimeMillis();
        try {
            sessionData.outages.add(new OutageListItem(sessionData.outages.size(), start, now, now - start, SERVICEDOWN));
            LOGGER.info("Service was down is registered");
        } catch (java.lang.UnsupportedOperationException ex) {
            LOGGER.error("Could not register that Service was down, the exception is {}", ex);
        }
    }

    private void handleControllerWasDown() {
        long start = controllerDownTimeStamp;
        long now = System.currentTimeMillis();
        try {
            sessionData.outages.add(new OutageListItem(sessionData.outages.size(), start, now, now - start, CONTROLLERDOWN));
            LOGGER.info("Controller was down is registered");
        } catch (java.lang.UnsupportedOperationException ex) {
            LOGGER.error("Could not register that Controller was down, the exception is {}", ex);
        }
    }

    /**
     * Get the router address.
     *
     * @return
     */
    public String getRouterAddress() {
        return sessionData.routerAddress;
    }

    /**
     * Set the router address.
     *
     * @param address
     */
    public void setRouterAddress(String address) {
        sessionData.routerAddress = address;
    }

    /**
     * @return the number of registered outages.
     */
    public int getOutagesSize() {
        return sessionData.outages.size();
    }

    /**
     * @return the last registered outage or null if none are available.
     */
    public OutageListItem getLastOutage() {
        if (null == sessionData.outages || sessionData.outages.isEmpty()) {
            return null;
        } else {
            return sessionData.outages.get(sessionData.outages.size() - 1);
        }
    }

    /**
     * Inner loop checking if connections to the hosts are possible.
     *
     * When looping busyCheckingConnections == true. Registers the periods when no connections could be made. Will return at an exitService or stop.
     */
    private void innerLoop(List<String> selectedURLs) {
        long loopStart;
        while (!exit && !stop) {
            busyCheckingConnections = true;
            loopStart = System.currentTimeMillis();
            if (checkISP(selectedURLs)) {
                // Success, ISP can be connected               
                canReachISP = true;
                sessionData.lastContactWithAnyHost = System.currentTimeMillis();
                currentISPunavailability = 0L;
                if (outageStart > 0L) {
                    long outageEnd = sessionData.lastContactWithAnyHost;
                    sessionData.outages.add(new OutageListItem(sessionData.outages.size(), outageStart,
                            outageEnd, outageEnd - outageStart, canConnectWithRouter ? ISP : INTERNAL));
                    outageStart = 0L;
                }
                canConnectWithRouter = true;
            } else {
                if (canReachISP) {
                    // Connection failed first time after successful connections
                    sessionData.numberOfInterruptions++;
                    outageStart = loopStart;
                    canConnectWithRouter = canConnectRouter();
                    LOGGER.info("canConnectWithRouter is set to {} and will be set to true at the first successful connection.", canConnectWithRouter);
                }
                canReachISP = false;
                sessionData.lastFail = System.currentTimeMillis();
                // update the current unavailability
                currentISPunavailability = sessionData.lastFail - outageStart;
            }
            // wait to check the ISP connection again
            sleepMillisSliced(TIMEOUT_5_SEC);
        }

        if (busyCheckingConnections) {
            controllerDownTimeStamp = System.currentTimeMillis();
            LOGGER.info("The controller has stopped.\n");
            LOGGER.info("{} Connection checks are executed, {} were successful.",
                    sessionData.successfulChecks + sessionData.failedChecks, sessionData.successfulChecks);
        }
        busyCheckingConnections = false;
    }

    /**
     * Perform the connection checks in a separate thread.
     *
     * @param hosts
     */
    public void doInBackground(List<String> hosts) {
        LOGGER.info("The controller thread is created and started.");
        this.selectedHostsURLs = hosts;
        handleServiceWasDown();
        start();
    }

    private boolean canConnectRouter() {
        if (simulateCannotReachRouter) {
            return false;
        }
        // if the router address is not set we can not exclude internal network failure
        if (NOROUTERADDRESS.equalsIgnoreCase(sessionData.routerAddress)) {
            LOGGER.warn("The router address is not set. The internal network error detection is omitted");
            return true;
        }
        // if the router address is not a valid address we can not exclude internal network failure
        if (!isValidHostAddress(sessionData.routerAddress)) {
            LOGGER.warn("The router address {} is not valid. The internal network error detection is omitted", sessionData.routerAddress);
            return true;
        }
        return checkRouter();
    }

    /**
     * A method for test purposes only.
     *
     * @param yesNo if true all connections are simulated to fail. If false real connection test are performed.
     */
    public void simulateISPfailure(boolean yesNo) {
        if (yesNo) {
            LOGGER.info("The ISP is SIMULATED to not be reachable");
        } else {
            LOGGER.info("The ISP unreachable SIMULATION is RESET to be reachable");
        }
        simulateISPFailure = yesNo;
    }

    /**
     * A method for test purposes only.
     *
     * @param yesNo if true a connection to the router address will fail. If false real connection test are performed.
     */
    public void simulateCannotReachRouter(boolean yesNo) {
        if (yesNo) {
            LOGGER.info("The router is SIMULATED to not be reachable");
        } else {
            LOGGER.info("The router unreachable SIMULATION is RESET to be reachable");
        }
        simulateCannotReachRouter = yesNo;
    }

    /**
     * Try to connect to any host in the list. This method will return almost immediately when the first host can be reached or take max nrOfHosts x 1900 mS.
     *
     * @param hURLs the hosts to test
     * @return true if a host can be contacted and false if not one host from the list can be reached.
     */
    boolean checkISP(List<String> hURLs) {
        boolean hostFound = false;
        if (simulateISPFailure) {
            LOGGER.info("Failed ISP check SIMULATED");
        } else {
            for (String host : hURLs) {
                // test a TCP connection with the destination host and a time-out.
                if (testConnection(host, PORT, TIMEOUT_900_MIL)) {
                    hostFound = true;
                    sessionData.successfulChecks++;
                    // when successfull there is no need to try the other selectedHostsURLs
                    break;
                } else {
                    sessionData.failedChecks++;
                    // wait 1 second before contacting the next host in the list
                    sleepMillisFixed(TIMEOUT_ONE_SEC);
                }
            }
        }
        return hostFound;
    }

    /**
     * Check if the router address can be reached.
     *
     * @return true if the router can be reached
     */
    private boolean checkRouter() {
        return testConnection(sessionData.routerAddress, PORT, TIMEOUT_ONE_SEC);
    }

    /**
     * Connect using layer4 (sockets)
     *
     * @see http://www.mindchasers.com/topics/ping.htm
     * @param
     * @return true is a connection could be made within the time-out interval
     */
    private static boolean testConnection(String host, Integer port, int timeout) {
        InetAddress inetAddress;
        InetSocketAddress socketAddress;
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
        try (SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(true);
            sc.socket().connect(socketAddress, timeout);
            return true;
        } catch (IOException e) {
            LOGGER.info("{}/{} cannot be reached. The cause is {}", new Object[]{host, inetAddress.getHostAddress(), e});
            return false;
        }
    }

    public static final int NUMBER_OF_SLYCES = 10;

    /**
     * Put this thread to sleep for ms milliseconds. Slice the sleep to exitService fast in case of a stop or exitService. Sleep only one slice while an outage is ongoing.
     *
     * @param ms the maximum sleep time
     */
    private void sleepMillisSliced(long ms) {
        long slice = ms / NUMBER_OF_SLYCES;
        for (int i = 0; i < NUMBER_OF_SLYCES && !exit && !stop; i++) {
            try {
                Thread.sleep(slice);
                if (!canReachISP) {
                    break;
                }
            } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ex) {
                LOGGER.info("A thread sleep was interrupted because of {}", ex);
            }
        }
    }

    /**
     * Put this thread to sleep for a fixed time of ms milliseconds.
     *
     * @param ms the fixed sleep time
     */
    private static void sleepMillisFixed(long ms) {
        try {
            Thread.sleep(ms);
        } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ex) {
            LOGGER.info("A thread sleep was interrupted because of {}", ex);
        }
    }

    /**
     * These data are refreshed every 5 seconds by Wicket Javascript.
     *
     * @return a list of status date.
     */
    public List getStatusData() {
        List ret = new ArrayList();

        StatusListItem x0 = new StatusListItem();
        x0.name = "startOfService";
        x0.value = new Date(sessionData.startOfService).toString();
        x0.index = 1;
        ret.add(x0);

        StatusListItem x1 = new StatusListItem();
        x1.name = "lastContactWithAnyHost";
        x1.value = new Date(sessionData.lastContactWithAnyHost).toString();
        x1.index = x0.index + 1;
        ret.add(x1);

        StatusListItem x2 = new StatusListItem();
        x2.name = "lastFail";
        if (sessionData.lastFail > 0) {
            x2.value = new Date(sessionData.lastFail).toString();
        } else {
            x2.value = "No failure yet";
        }
        x2.index = x1.index + 1;
        ret.add(x2);

        StatusListItem x3 = new StatusListItem();
        x3.name = "numberOfInterruptions";
        x3.value = Long.toString(sessionData.numberOfInterruptions);
        x3.index = x2.index + 1;
        ret.add(x3);

        StatusListItem x4 = new StatusListItem();
        x4.name = "failedChecks";
        x4.value = Long.toString(sessionData.failedChecks);
        x4.index = x3.index + 1;
        ret.add(x4);

        StatusListItem x5 = new StatusListItem();
        x5.name = "successfulChecks";
        x5.value = Long.toString(sessionData.successfulChecks);
        x5.index = x4.index + 1;
        ret.add(x5);

        StatusListItem x6 = new StatusListItem();
        x6.name = "currentISPunavailability";
        x6.value = millisToTime(currentISPunavailability);
        x6.index = x5.index + 1;
        ret.add(x6);

        StatusListItem x7 = new StatusListItem();
        x7.name = "totalISPunavailability";
        x7.value = millisToTime(getTotalISPUnavailability());
        x7.index = x6.index + 1;
        ret.add(x7);

        StatusListItem x8 = new StatusListItem();
        x8.name = "outageCausedInternal";
        if (NOROUTERADDRESS.equals(sessionData.routerAddress)) {
            x8.value = "Cannot say, router address unknown";
        } else {
            if (busyCheckingConnections) {
                x8.value = Boolean.toString(!canConnectWithRouter);
            } else {
                x8.value = "Cannot say, conroller is not running";
            }
        }
        x8.index = x7.index + 1;
        ret.add(x8);

        StatusListItem x9 = new StatusListItem();
        x9.name = "INTERNET UP?";
        if (busyCheckingConnections) {
            x9.value = Boolean.toString(canReachISP);
        } else {
            x9.value = "UNKNOWN, conroller is not running";
        }
        x9.index = x8.index + 1;
        ret.add(x9);

        return ret;
    }

    /**
     * Get all outage data in reversed order. The most recent first.
     *
     * @return the full list
     */
    public List getOutageDataReversedOrder() {
        return ReversedView.of(sessionData.outages);
    }

    private long getTotalISPUnavailability() {
        long sumOutages = 0L;
        for (OutageListItem item : sessionData.outages) {
            if (item.cause == ISP) {
                sumOutages += item.getDuration();
            }
        }
        return sumOutages + currentISPunavailability;
    }

}
