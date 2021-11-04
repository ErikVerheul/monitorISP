/*
 * The MIT License
 *
 * Copyright (c) 2015-2021, Verheul Consultants
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
package nl.verheulconsultants.monitorisp.service;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Enumeration;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of static utilities.
 *
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public class Utilities {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utilities.class);

    /**
     *
     */
    public static final String APPHOMEDIR = "C:\\MonitorISP\\";
    private static final String TESTHOMEDIR = "C:\\MonitorISP\\test\\";
    private static String sessionDataFileName = APPHOMEDIR + "MonitorISPData.bin";

    /**
     *
     */
    public static final int ISP = 0;

    /**
     *
     */
    public static final int INTERNAL = 1;

    /**
     *
     */
    public static final int SERVICEDOWN = 2;

    /**
     *
     */
    public static final int CONTROLLERDOWN = 3;

    /**
     *
     * @return
     */
    public static String getSessionDataFileName() {
        return sessionDataFileName;
    }

    /**
     * Set a test directory for storing the session data.
     */
    public static void setSessionsDataFileNameForTest() {
        sessionDataFileName = TESTHOMEDIR + "MonitorISPData.bin";
    }

    /**
     * Get the test directory for storing the session data.
     *
     * @return the path
     */
    public static Path getTestHomeDir() {
        return FileSystems.getDefault().getPath(TESTHOMEDIR);
    }

    /**
     * Check for a valid url (but omit checking the protocol header) or Ip4 or Ip6 address.
     *
     * @param urlString
     * @return
     */
    public static boolean isValidHostAddress(String urlString) {
        //Assigning the url format regular expression
        String urlPattern = "^[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
        return urlString.matches(urlPattern) || isValidIp(urlString);
    }

    /**
     * Use the org.apache.httpcomponents class library to validate Ip4 and Ip6 addresses.
     *
     * @param ip the ip
     * @return check if the ip is valid ipv4 or ipv6
     */
    private static boolean isValidIp(final String ip) {
        return InetAddressUtils.isIPv4Address(ip) || InetAddressUtils.isIPv6Address(ip);
    }

    public static final int MILIS_IN_SECOND = 1_000;
    public static final int SEC_IN_MIN = 60;
    public static final int MIN_IN_HR = 60;
    public static final int HR_IN_DAY = 24;

    /**
     * Convert a duration in milliseconds to string.
     *
     * @param millis
     * @return a string with format dd:hh:mm:ss
     */
    public static String millisToTime(long millis) {
        long second = 0;
        long minute = 0;
        long hour = 0;
        long day = 0;
        if (millis > 0) {
            second = (millis / MILIS_IN_SECOND) % SEC_IN_MIN;
            minute = (millis / (MILIS_IN_SECOND * SEC_IN_MIN)) % MIN_IN_HR;
            hour = (millis / (MILIS_IN_SECOND * SEC_IN_MIN * MIN_IN_HR)) % HR_IN_DAY;
            day = millis / (MILIS_IN_SECOND * SEC_IN_MIN * MIN_IN_HR * HR_IN_DAY);
        }
        return String.format("%02d:%02d:%02d:%02d", day, hour, minute, second) + " [d:h:m:s]";
    }

    /**
     * Put this thread to sleep for ms milliseconds.
     *
     * @param ms the sleep time
     */
    public static void sleepMillis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ex) {
            LOGGER.info("A thread sleep was interrupted because of {}", ex);
        }
    }

    /**
     * This utility is not yet used.
     *
     * @return the location of the log files
     */
    public static String getLogFileName() {
        FileAppender fileAppender = null;

        Enumeration appenders = LogManager.getRootLogger().getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender currAppender = (Appender) appenders.nextElement();
            if (currAppender instanceof FileAppender) {
                fileAppender = (FileAppender) currAppender;
            }
        }

        if (fileAppender != null) {
            return fileAppender.getFile();
        } else {
            return "Log file location not found.";
        }
    }

    //Prevent this utility class to be instantiated.
    private Utilities() {

    }

}
