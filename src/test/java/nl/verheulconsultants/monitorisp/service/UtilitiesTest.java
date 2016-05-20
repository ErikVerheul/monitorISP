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
import static nl.verheulconsultants.monitorisp.service.Utilities.getTestHomeDir;
import static nl.verheulconsultants.monitorisp.service.Utilities.setSessionsDataFileNameForTest;
import static nl.verheulconsultants.monitorisp.ui.WicketApplication.getController;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author erik
 */
public class UtilitiesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ISPControllerTest.class);
    private static final ISPController controller  = getController();

    public UtilitiesTest() {
    }

    @Before
    public void setUp() {
        System.out.println("setUp");
        LOGGER.info("New Controller instance instantiated.");
        setSessionsDataFileNameForTest();
        // copy a test file to the test directory (will be overwritten)
        File resourcesDirectory = new File("src/test/resources");
        File source = new File(resourcesDirectory, "MonitorISPData.bin");
        Path sourcePath = source.toPath();
        //copy the test file to the test directory with the same name as the source
        try {
            Files.copy(sourcePath, getTestHomeDir().resolve(source.getName()), REPLACE_EXISTING);
            LOGGER.info("Fresh last session data copied");
        } catch (IOException ex) {
            LOGGER.error("File copy failed with exception {}", ex);
        }
        // Must load the session data explicit as Homepage is not doing it.
        if (controller.initWithPreviousSessionData()) {
            LOGGER.info("Preset previous session test data are used for initialization.");
        } else {
            LOGGER.info("Preset previous session test data could not be read, defaults are set");
        }
    }

    /**
     * Test of millisToTime method, of class Utilities.
     */
    @Test
    public void testMillisToTime() {
        System.out.println("millisToTime");
        long millis = 100000L;
        String expResult = "00:00:01:40 [d:h:m:s]";
        String result = Utilities.millisToTime(millis);
        assertEquals(expResult, result);
        long dayPlusmillis = millis + 24*60*60*1000;
        String expResult2 = "01:00:01:40 [d:h:m:s]";
        result = Utilities.millisToTime(dayPlusmillis);
        assertEquals(expResult2, result);
    }

    /**
     * Test of getLogFileName method, of class Utilities.
     */
    @Test
    public void testGetLogFileName() {
        System.out.println("getLogFileName");
        String expResult = "C:\\MonitorISP\\logs\\monitorisp.log";
        String result = Utilities.getLogFileName();
        assertEquals(expResult, result);
    }

}
