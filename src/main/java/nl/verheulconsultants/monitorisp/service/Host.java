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

import java.io.Serializable;

/**
 * Class for storing a host in a format that matches the Palette requirements.
 * 
 */
public class Host implements Serializable {

    private static final long serialVersionUID = 1L;
    String id;
    String hostAddress;

    /**
     * Palette needs an object with an id and a hostAddress.
     * Here the hostAddress is the domain address of the host.
     * 
     * @param id
     * @param hostAddress
     */
    public Host(String id, String hostAddress) {
        this.id = id;
        this.hostAddress = hostAddress;
    }
    
    /**
     * 
     * @return the host address
     */
    public String getHostAddress() {
        return hostAddress;
    }
    
    @Override
    public String toString() {
        return "Host [id = " + id + ", address = " + hostAddress + "]";
    }
}
