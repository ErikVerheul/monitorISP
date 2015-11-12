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

import org.junit.Test;
import static org.junit.Assert.*;

public class MyUrlValidatorTest {
    
    public MyUrlValidatorTest() {
    }

    /**
     * Test of isValid method, of class MyUrlValidator.
     */
    @Test
    public void testIsValid() {
        System.out.println("isValid");
        
        MyUrlValidator instance = new MyUrlValidator();
        String urlString;
        boolean expResult;
        boolean result;
        
        urlString = "a.b.c.com";
        expResult = true;
        result = instance.isValid(urlString);
        assertEquals(expResult, result);
        
        urlString = "a1-2.b.c.com";
        expResult = true;
        result = instance.isValid(urlString);
        assertEquals(expResult, result);
        
        urlString = "a1-2.b.c@com";
        expResult = false;
        result = instance.isValid(urlString);
        assertEquals(expResult, result);
        
        /**
         * Protocol addition is not allowed in this check.
         * We are just creating a connection.
         */
        urlString = "http://b.c.com";
        expResult = false;
        result = instance.isValid(urlString);
        assertEquals(expResult, result);
        
    }

}
