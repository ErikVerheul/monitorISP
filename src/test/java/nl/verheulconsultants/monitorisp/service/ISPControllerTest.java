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
import static nl.verheulconsultants.monitorisp.service.ISPController.getLastOutage;
import static nl.verheulconsultants.monitorisp.service.ISPController.initWithPreviousSessionData;
import static nl.verheulconsultants.monitorisp.service.ISPController.setIntest;
import static nl.verheulconsultants.monitorisp.service.ISPController.setRouterAddress;
import static nl.verheulconsultants.monitorisp.service.ISPController.simulateCannotReachRouter;
import static nl.verheulconsultants.monitorisp.service.ISPController.simulateFailure;
import static nl.verheulconsultants.monitorisp.service.Utilities.CONTROLLERDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.INTERNAL;
import static nl.verheulconsultants.monitorisp.service.Utilities.ISP;
import static nl.verheulconsultants.monitorisp.service.Utilities.SERVICEDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.getTestHomeDir;
import static nl.verheulconsultants.monitorisp.service.Utilities.setSessionsDataFileNameForTest;
import static nl.verheulconsultants.monitorisp.service.Utilities.sleepMillis;
import static nl.verheulconsultants.monitorisp.ui.WicketApplication.controller;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISPControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISPControllerTest.class);
    ISPController instance;

    /**
     * Prevent an automatic start with the saved previous session data.
     */
    @BeforeClass
    public static void setUpClass() {
        setIntest();
    }
    
    @Before
    public void setUp() {
        System.out.println("setUp");
        instance = new ISPController();

        setSessionsDataFileNameForTest();
        // copy a test file to the test directory (will be overwritten)
        File resourcesDirectory = new File("src/test/resources");
        File source = new File(resourcesDirectory, "MonitorISPData.bin");
        Path sourcePath = source.toPath();
        //copy the test file to the test directory with the same name as the source
        try {
            Files.copy(sourcePath, getTestHomeDir().resolve(source.getName()), REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.error("File copy failed with exception {}", ex);
        }

        if (initWithPreviousSessionData()) {
            LOGGER.info("Preset previous session test data are used for initialization.");
        } else {
            LOGGER.info("Preset previous session test data could not be read, defaults are set");
        }
    }

    @After
    public void tearDown() {
        System.out.println("tearDown");
        instance.exit();
        sleepMillis(140);
        if (controller.isRunning()) {
            LOGGER.warn("The controller is still running!");
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
        assertTrue(instance.isBusyCheckingConnections());
        instance.stopTemporarily();
        sleepMillis(1400);
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
        sleepMillis(1400);
        assertFalse(instance.isBusyCheckingConnections());
        instance.restart(hosts);
        sleepMillis(2000);
        assertTrue(instance.isBusyCheckingConnections());
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
        sleepMillis(140);
        assertFalse(instance.isRunning());
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
        sleepMillis(140);
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
        sleepMillis(1400);
        assertTrue(ISPController.successfulChecks > 0);
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
        sleepMillis(1400);
        assertTrue(ISPController.failedChecks > 0);
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
        setRouterAddress("192.168.0.6");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue(instance.isBusyCheckingConnections());
        simulateFailure(true);
        sleepMillis(6000);
        simulateFailure(false);
        sleepMillis(6000);
        OutageListItem lastOutage = getLastOutage();
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == ISP);
    }

    /**
     * Test if a record is registered when the ISP can not be reached and a false router address is entered.
     */
    @Test
    public void testISPInterruptedRegistrationWithFalseRouterAddress() {
        System.out.println("testISPInterruptedRegistrationWithFalseRouterAddress");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        setRouterAddress("false router address");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue(instance.isBusyCheckingConnections());
        simulateFailure(true);
        sleepMillis(6000);
        simulateFailure(false);
        sleepMillis(6000);
        OutageListItem lastOutage = getLastOutage();
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == ISP);
    }

    /**
     * Test if a record is registered when the ISP can not be reached due to an internal network failure.
     */
    @Test
    public void testInternalInterruptedRegistration() {
        System.out.println("testInternalInterruptedRegistration");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        setRouterAddress("192.168.0.6");
        simulateCannotReachRouter(true);

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue(instance.isBusyCheckingConnections());
        simulateFailure(true);
        sleepMillis(6000);
        simulateFailure(false);
        sleepMillis(6000);
        simulateCannotReachRouter(false);
        OutageListItem lastOutage = getLastOutage();
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == INTERNAL);
    }

    /**
     * Test if a record is registered when the ISP can not be reached due to an internal network failure and a false router address is entered.
     */
    @Test
    public void testInternalInterruptedRegistrationWithFalseRouterAddress() {
        System.out.println("testInternalInterruptedRegistrationWithFalseRouterAddress");
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        setRouterAddress("false router address");

        instance.doInBackground(hosts);
        sleepMillis(120);
        assertTrue(instance.isBusyCheckingConnections());
        simulateFailure(true);
        sleepMillis(6000);
        simulateFailure(false);
        sleepMillis(6000);
        OutageListItem lastOutage = getLastOutage();
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
        assertTrue(instance.isBusyCheckingConnections());
        OutageListItem lastOutage = getLastOutage();
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
        sleepMillis(1400);
        assertFalse(instance.isBusyCheckingConnections());
        instance.restart(hosts);
        sleepMillis(2000);
        assertTrue(instance.isBusyCheckingConnections());
        OutageListItem lastOutage = getLastOutage();
        assertTrue("No outages were registered", null != lastOutage);
        assertTrue("The actual last outage is " + lastOutage, lastOutage.getOutageCause() == CONTROLLERDOWN);
    }

}
