/*
 * Set of globally used variables and methods.
 */
package nl.verheulconsultants.monitorisp.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author erik
 */
class Globals {
    static final Logger logger = LoggerFactory.getLogger(EMailClient.class);
    final static int primaryISP = 0;
    final static int backupISP = 1;
    List<String> hosts;
    long lastContactWithAnyHost;
    String propsFileName;
    Properties props;
    boolean propertiesSetTemporarely;
    long successfulChecks;
    long failedChecks;
    int switchoverCount;
    // this values which must be set to real values using JConsole
    int currentISP;
    long triggerDuration;
    long retryInterval;
    int maxRetries;
    String emailAddress;
    String logFileName;
    String primarySMTPserver;
    String backupSMTPserver;
    Level currentLogLevel;

    Globals() {
        hosts = new ArrayList<>();
        lastContactWithAnyHost = System.currentTimeMillis();
        successfulChecks = 0L;
        failedChecks = 0L;

        // initialize with temporary values which must be set to real values using JConsole
        currentISP = primaryISP;
        triggerDuration = 30L; //seconds
        emailAddress = "me@mydomain.dom";
        logFileName = "C:\\tmp\\SwitchISPservice.log";
        primarySMTPserver = "smtp.primarySMTPserver.dom";
        backupSMTPserver = "smtp.backupSMTPserver.dom";
        // end of temporary initialization which can be changed by the user

    }
    
    String getCurrentISPString() {
        if (currentISP == primaryISP) {
            return "primary";
        } else {
            return "backup";
        }
    }
    
    String getCurrentSMTPserver() {
        if (currentISP == primaryISP) {
            return primarySMTPserver;
        } else {
            return backupSMTPserver;
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
        } catch (java.util.concurrent.CancellationException ignore1) {
            // is OK, interrupt by thread cancellation
        } catch (java.lang.InterruptedException ignore2) {
            // is OK, interrupt caused by thread cancellation
        }
    }

    /**
     * Try to connect to any host in the list
     *
     * @return true if a host can be contacted and false if not one host from the list can be reached.
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
            logger.info("De host {0} is onbekend. Oorzaak = {1}", new Object[]{host, e});
            return false;
        }

        try {
            socketAddress = new InetSocketAddress(inetAddress, port);
        } catch (IllegalArgumentException e) {
            logger.info("De poort {0} kan niet valide zijn. Oorzaak = {1}", new Object[]{port, e});
            return false;
        }

        // Open the channel, set it to blocking, initiate connect
        try {
            sc = SocketChannel.open();
            sc.configureBlocking(true);
            sc.socket().connect(socketAddress, timeout);
            logger.debug("{0}/{1} is bereikbaar.", new Object[]{host, inetAddress.getHostAddress()});
            return true;
        } catch (IOException e) {
            logger.info("{0}/{1} is niet bereikbaar. De oorzaak is {2}", new Object[]{host, inetAddress.getHostAddress(), e});
            return false;
        } finally {
            if (sc != null) {
                try {
                    sc.close();
                } catch (IOException e) {
                    logger.warn("Het socket kanaal met host {0} kon niet worden gesloten. De oorzaak is {1}", new Object[]{host, e});
                }
            }
        }
    }
}
