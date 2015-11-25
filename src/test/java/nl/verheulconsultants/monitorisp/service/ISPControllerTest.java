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
import static nl.verheulconsultants.monitorisp.service.Utilities.getTestHomeDir;
import static nl.verheulconsultants.monitorisp.service.Utilities.setSessionsDataFileNameForTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISPControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISPController.class);
    ISPController instance;

    public ISPControllerTest() {
    }

    @Before
    public void setUp() {
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
    }

    @After
    public void tearDown() {
         instance.exit();
         sleepMilis(200);
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
        sleepMilis(120);
        assertTrue(instance.isBusyCheckingConnections());
        instance.stopTemporarily();
        sleepMilis(1400);
        assertFalse(instance.isBusyCheckingConnections());   
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
        sleepMilis(120);
        instance.stopTemporarily();
        sleepMilis(1400);
        assertFalse(instance.isBusyCheckingConnections());
        instance.restart(hosts);
        sleepMilis(1400);
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
        sleepMilis(2000);
        assertFalse(instance.isRunning());
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
        sleepMilis(120);
        assert(!instance.isAlive());
    }

    /**
     * Test of doInBackground method, of class ISPController with valid URL.
     */
    @Test
    public void testDoInBackground1() {
        System.out.println("doInBackground");

        List<String> hosts = new ArrayList();
        hosts.add("uva.nl");

        instance.doInBackground(hosts);
        sleepMilis(1400);
        assertTrue(ISPController.successfulChecks > 0);
    }
    
    /**
     * Test of doInBackground method, of class ISPController with non-valid URL.
     */
    @Test
    public void testDoInBackground2() {
        System.out.println("doInBackground");

        List<String> hosts = new ArrayList();
        hosts.add("willnotconnect.com");

        instance.doInBackground(hosts);
        sleepMilis(1400);
        assertTrue(ISPController.failedChecks > 0);
    }

    /**
     * Test of checkISP method, of class ISPController with valid URL.
     */
    @Test
    public void testCheckISP1() {
        System.out.println("checkISP");
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
        System.out.println("checkISP");
        List<String> hURLs = new ArrayList();
        hURLs.add("willnotconnect.com");
        boolean expResult = false;
        boolean result = instance.checkISP(hURLs);
        assertEquals(expResult, result);
    }

    /**
     * Put this thread to sleep for ms milliseconds.
     *
     * @param ms the sleep time
     */
    void sleepMilis(long ms) {
        try {
            Thread.sleep(ms);
        } catch (java.util.concurrent.CancellationException | java.lang.InterruptedException ex) {
            LOGGER.info("A thread sleep was interrupted because of {}", ex);
        }
    }
}
