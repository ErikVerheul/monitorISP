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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class HostList {

    static final Logger logger = LoggerFactory.getLogger(HostList.class);
    static List<Host> hosts;

    /**
     * Saves a list of hosts in a file with name FileName.
     * @param hosts
     * @param fileName
     * @return true if successful
     */
    static boolean save(List<Host> hosts, String fileName) {
        ObjectOutputStream oos;
        try (FileOutputStream fout = new FileOutputStream(fileName)) {
            oos = new ObjectOutputStream(fout);
            oos.writeObject(hosts);
            return true;
        } catch (IOException ex) {
            logger.error(fileName + " can not be saved. The exception is {}", ex);
            return false;
        }
    }

    static boolean readHosts(String fileName) throws IOException, ClassNotFoundException {
        try (FileInputStream fin = new FileInputStream(fileName)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            hosts = (List<Host>) ois.readObject();
            if (hosts != null && hosts.isEmpty()) {
                init();
                return true;
            } else {
                return false;
            }          
        } catch (FileNotFoundException ex) {
            init();
            return true;
        }
    }
    
    static List readSelected(String fileName) throws IOException, ClassNotFoundException {
        try (FileInputStream fin = new FileInputStream(fileName)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            return (List<Host>) ois.readObject();
        } catch (FileNotFoundException ex) {
            return new ArrayList<>();
        }
    }

    static void init() {
        hosts = new ArrayList<>();
        hosts.add(new Host("0", "google-public-dns-a.google.com"));       
        hosts.add(new Host("1", "uva.nl"));
        hosts.add(new Host("2", "xs4all.nl"));       
    }
}
