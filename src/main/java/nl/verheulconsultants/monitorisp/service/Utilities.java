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

import org.apache.http.conn.util.InetAddressUtils;

public class Utilities {
    
    //Prevent this utility class to be instantiated.
    private Utilities() {
        
    }
    
    public static final String APPHOMEDIR = "C:\\MonitorISP\\";
    public static final String CHOICESFILENAME = APPHOMEDIR + "MonitorISPchoices";
    public static final String SELECTIONFILENAME = APPHOMEDIR + "MonitorISPselected";
    
    /**
     * Check for a valid url (but omit checking the protocol header) or Ip4 or Ip6 address.
     *
     * @param urlString
     * @return
     */
    public static boolean isValid(String urlString) {
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
    
}
