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
package nl.verheulconsultants.monitorisp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class HostList {

    static List<Hosts> hosts;

    static void save(List<Hosts> selected, String fileName) {
        ObjectOutputStream oos;
        try (FileOutputStream fout = new FileOutputStream(fileName)) {
            oos = new ObjectOutputStream(fout);
            oos.writeObject(selected);
        } catch (IOException ex) {
            Logger.getLogger(HostList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void read(String fileName) throws IOException, ClassNotFoundException {
        try (FileInputStream fin = new FileInputStream(fileName)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            hosts = (List<Hosts>) ois.readObject();
        } catch (FileNotFoundException ex) {
            init();
        }
    }
    
    static void init() {
        hosts = new ArrayList<>();
        hosts.add(new Hosts("1", "google-public-dns-a.google.com"));
        hosts.add(new Hosts("2", "xs4all.nl"));
        hosts.add(new Hosts("3", "uva.nl"));
    }
}
