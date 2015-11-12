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

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISPControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISPController.class);
    ISPController instance;

    public ISPControllerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        instance = new ISPController();
    }

    @After
    public void tearDown() {
         instance.exit();
    }

    /**
     * Test of isRunning method, of class ISPController.
     */
    @Test
    public void testIsRunning() {
        System.out.println("isRunning");
        boolean expResult = false;
        boolean result = instance.isRunning();
        assertEquals(expResult, result);
    }

    /**
     * Test of stopTemporarily method, of class ISPController.
     */
    @Test
    public void testStopTemporarily() {
        System.out.println("stopTemporarily");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        waitMilis(20);
        assertTrue(instance.isBusyCheckingConnections());
        instance.stopTemporarily();
        waitMilis(70);
        assertTrue(!instance.isBusyCheckingConnections());   
    }

    /**
     * Test of restart method, of class ISPController.
     */
    @Test
    public void testRestart() {
        System.out.println("restart");
        
        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        
        instance.doInBackground(hosts);
        waitMilis(10);
        instance.stopTemporarily();
        waitMilis(70);
        assertTrue(!instance.isBusyCheckingConnections());
        instance.restart(hosts);
        waitMilis(200);
        assertTrue(instance.isBusyCheckingConnections());                 
    }

    /**
     * Test of exit method, of class ISPController.
     */
    @Test
    public void testExit() {
        System.out.println("exit");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");
        instance.doInBackground(hosts);
        instance.exit();
        assertTrue(!instance.isRunning());
    }

    /**
     * Test of run method, of class ISPController.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        instance.start();
        assert(instance.isAlive());
        instance.exit();
        waitMilis(70);
        assert(!instance.isAlive());
    }

    /**
     * Test of doInBackground method, of class ISPController.
     */
    @Test
    public void testDoInBackground() {
        System.out.println("doInBackground");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        waitMilis(10);
        assertTrue(instance.isRunning());
        instance.exit();
        waitMilis(70);
        assertTrue(!instance.isRunning());
    }

    /**
     * Test of checkISP method, of class ISPController.
     */
    @Test
    public void testCheckISP() {
        System.out.println("checkISP");
        List<String> hURLs = new ArrayList();
        hURLs.add("uva.nl");
        boolean expResult = true;
        boolean result = instance.checkISP(hURLs);
        assertEquals(expResult, result);
    }

    /**
     * Put this thread to sleep for ms miliseconds.
     *
     * @param ms the sleep time
     */
    void waitMilis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ex) {
            LOGGER.info("A thread sleep was interrupted because of {}", ex);
        }
    }
}
