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
package nl.verheulconsultants.monitorisp.ui;

import java.util.ArrayList;
import static nl.verheulconsultants.monitorisp.service.Status.*;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class HostListTest {

    public HostListTest() {
    }

    /**
     * Test of save method, of class HostList.
     */
    @Test
    public void testSave() {
        System.out.println("save");

        List<Host> hosts = new ArrayList();
        hosts.add(new Host(Integer.toString(1), "uva.nl"));
        String fileName = hostsFile;
        boolean expResult = true;
        boolean result = HostList.save(hosts, fileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of readHosts method, of class HostList.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testReadHosts() throws Exception {
        System.out.println("readHosts");

        String fileName = hostsFile;
        boolean expResult = true;
        boolean result = HostList.readHosts(fileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of readSelected method, of class HostList.
     * @throws java.lang.Exception
     */
    @Test
    public void testReadSelected() throws Exception {
        System.out.println("readSelected");

        String fileName = selectedFile;
        List result = HostList.readSelected(fileName);
        assertTrue(!result.isEmpty());
    }

    /**
     * Test of init method, of class HostList.
     */
    @Test
    public void testInit() {
        System.out.println("init");

        HostList.init();
        assertTrue(!HostList.hosts.isEmpty());
    }
}
