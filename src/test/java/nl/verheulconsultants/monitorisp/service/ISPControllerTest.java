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
package nl.verheulconsultants.monitorisp.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.List;
import static nl.verheulconsultants.monitorisp.service.Utilities.CONTROLLERDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.INTERNAL;
import static nl.verheulconsultants.monitorisp.service.Utilities.ISP;
import static nl.verheulconsultants.monitorisp.service.Utilities.SERVICEDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.getTestHomeDir;
import static nl.verheulconsultants.monitorisp.service.Utilities.setSessionsDataFileNameForTest;
import static nl.verheulconsultants.monitorisp.service.Utilities.sleepMillis;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ISPController tests. Each test starts the controller and uses a fresh copy of
 * a set of session data located in the test resources.
 */
public class ISPControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISPControllerTest.class);
    ISPController instance;

    @Before
    public void setUp() {
        System.out.println("setUp");
        instance = new ISPController();
        LOGGER.info("New Controller instance instantiated.");
        setSessionsDataFileNameForTest();
        // copy a test file to the test directory (will be overwritten)
        File resourcesDirectory = new File("src/test/resources");
        File source = new File(resourcesDirectory, "MonitorISPData.bin");
        Path sourcePath = source.toPath();
        //copy the test file to the test directory with the same name as the source
        try {
            Files.copy(sourcePath, getTestHomeDir().resolve(source.getName()), REPLACE_EXISTING);
            LOGGER.info("Fresh test session data copied");
        } catch (IOException ex) {
            LOGGER.error("File copy failed with exception {}", ex);
        }
        // Must load the session data explicit as Homepage is not doing it.
        if (instance.initWithPreviousSessionData()) {
            LOGGER.info("Preset previous session test data are used for initialization.");
        } else {
            LOGGER.info("Preset previous session test data could not be read, defaults are set");
        }
    }

    @After
    public void tearDown() {
        System.out.println("tearDown");
        instance.exit();
        sleepMillis(1500);
        if (instance.isAlive()) {
            LOGGER.warn("The controller thread is still running!");
        } else {
            LOGGER.info("The controller thread has exited.");
        }
    }

    /**
     * Test of isRunning method, of class ISPController.
     */
    @Test
    public void testIsRunning() {
        System.out.println("testIsRunning");
        boolean expResult = false;
        boolean result = instance.isRunning();
        assertEquals(expResult, result);
    }

    /**
     * Test of stopTemporarily method, of class ISPController.
     */
    @Test
    public void testStopTemporarily() {
        System.out.println("testStopTemporarily");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
        instance.stopTemporarily();
        sleepMillis(1100);
        assertFalse(instance.isBusyCheckingConnections());
    }

    /**
     * Test of restart method, of class ISPController.
     */
    @Test
    public void testRestart() {
        System.out.println("testRestart");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        sleepMillis(120);
        instance.stopTemporarily();
        sleepMillis(1100);
        assertFalse(instance.isBusyCheckingConnections());
        instance.restart(hosts);
        sleepMillis(1100);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
    }

    /**
     * Test of exit method, of class ISPController.
     */
    @Test
    public void testExit() {
        System.out.println("testExit");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        sleepMillis(120);
        instance.exit();
        sleepMillis(1500);
        assertFalse(instance.isRunning());
        assertFalse(instance.isAlive());
    }

    /**
     * Test of run method, of class ISPController.
     */
    @Test
    public void testRun() {
        System.out.println("testRun");
        instance.start();
        assert (instance.isAlive());
        instance.exit();
        sleepMillis(1500);
        assert (!instance.isAlive());
    }

    /**
     * Test of doInBackground method, of class ISPController with valid URL.
     */
    @Test
    public void testDoInBackground1() {
        System.out.println("testDoInBackground1");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        sleepMillis(1500);
        assertTrue(instance.getSessionData().successfulChecks > 0);
    }

    /**
     * Test of doInBackground method, of class ISPController with non-valid URL.
     */
    @Test
    public void testDoInBackground2() {
        System.out.println("testDoInBackground2");

        List<String> hosts = new ArrayList();
        hosts.add("willnotconnect.com");

        instance.doInBackground(hosts);
        sleepMillis(1500);
        assertTrue(instance.getSessionData().failedChecks > 0);
    }

    /**
     * Test of checkISP method, of class ISPController with valid URL.
     */
    @Test
    public void testCheckISP1() {
        System.out.println("testCheckISP1");
        List<String> hURLs = new ArrayList();
        hURLs.add("uva.nl");
        boolean expResult = true;
        boolean result = instance.checkISP(hURLs);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkISP method, of class ISPController with non-valid URL.
     */
    @Test
    public void testCheckISP2() {
        System.out.println("testCheckISP2");
        List<String> hURLs = new ArrayList();
        hURLs.add("willnotconnect.com");
        boolean expResult = false;
        boolean result = instance.checkISP(hURLs);
        assertEquals(expResult, result);
    }

    /**
     * Test if a record is registered when the ISP can not be reached.
     */
    @Test
    public void testISPInterruptedRegistration() {
        System.out.println("testISPInterruptedRegistration");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        //Set this value to your nearest router ip.
        instance.setRouterAddress("192.168.0.6");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
        instance.simulateFailure(true);
        sleepMillis(6000);
        instance.simulateFailure(false);
        sleepMillis(6000);
        OutageListItem lastOutage = instance.getLastOutage();
        LOGGER.info("Outage = {}", lastOutage);
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == ISP);
    }

    /**
     * Test if a record is registered when the ISP can not be reached and a
     * false router address is entered.
     */
    @Test
    public void testISPInterruptedRegistrationWithFalseRouterAddress() {
        System.out.println("testISPInterruptedRegistrationWithFalseRouterAddress");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        instance.setRouterAddress("false router address");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
        instance.simulateFailure(true);
        sleepMillis(6000);
        instance.simulateFailure(false);
        sleepMillis(6000);
        OutageListItem lastOutage = instance.getLastOutage();
        LOGGER.info("Outage = {}", lastOutage);
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == ISP);
    }

    /**
     * Test if a record is registered when the ISP can not be reached due to an
     * internal network failure.
     */
    @Test
    public void testInternalInterruptedRegistration() {
        System.out.println("testInternalInterruptedRegistration");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        instance.setRouterAddress("192.168.0.6");
        instance.simulateCannotReachRouter(true);

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
        instance.simulateFailure(true);
        sleepMillis(6000);
        instance.simulateFailure(false);
        sleepMillis(6000);
        OutageListItem lastOutage = instance.getLastOutage();
        LOGGER.info("Outage = {}", lastOutage);
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == INTERNAL);
    }

    /**
     * Test if a record is registered when the ISP can not be reached due to an
     * internal network failure and a false router address is entered.
     */
    @Test
    public void testInternalInterruptedRegistrationWithFalseRouterAddress() {
        System.out.println("testInternalInterruptedRegistrationWithFalseRouterAddress");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        instance.setRouterAddress("false router address");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
        instance.simulateFailure(true);
        sleepMillis(6000);
        instance.simulateFailure(false);
        sleepMillis(6000);
        OutageListItem lastOutage = instance.getLastOutage();
        LOGGER.info("Outage = {}", lastOutage);
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == ISP);
    }

    /**
     * Test if a record is registered when service is restarted.
     */
    @Test
    public void testServiceInterruptedRegistration() {
        System.out.println("testServiceInterruptedRegistration");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
        OutageListItem lastOutage = instance.getLastOutage();
        LOGGER.info("Outage = {}", lastOutage);
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == SERVICEDOWN);
    }

    /**
     * Test if a record is registered when the controller is temporarily down.
     */
    @Test
    public void testControllerDownRegistration() {
        System.out.println("testControllerDownRegistration");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        sleepMillis(120);
        instance.stopTemporarily();
        sleepMillis(1500);
        assertFalse(instance.isBusyCheckingConnections());
        instance.restart(hosts);
        sleepMillis(1500);
        assertTrue("The controller is NOT checking connections now", instance.isBusyCheckingConnections());
        OutageListItem lastOutage = instance.getLastOutage();
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == CONTROLLERDOWN);
        instance.exit();
        sleepMillis(1500);
        if (instance.getSessionData().saveData()) {
            LOGGER.info("Session data is saved at exiting the application.");
        } else {
            LOGGER.error("Session data is NOT saved at exiting the application.");
        }

        LOGGER.info("Load data");
        instance.initWithPreviousSessionData();
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == CONTROLLERDOWN);

    }

}
