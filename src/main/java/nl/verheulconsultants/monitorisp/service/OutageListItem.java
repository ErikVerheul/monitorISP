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

public class OutageListItem {

    int index;
    String start;
    String end;
    long duration;
    boolean outageCausedInternal;

    OutageListItem(int index, String start, String end, long duration, boolean outageCauseInternal) {
        this.index = index;
        this.start = start;
        this.end = end;
        this.duration = duration;
        this.outageCausedInternal = outageCauseInternal;
    }
    
    /**
     * Returns the index starting with 1 for the first outage.
     * 
     * @return index + 1
     */
     public int getIndex() {
        return index + 1;
    }
    
     /**
     * Returns the outage start date and time.
     *
     * @return start date
     */
    public String getStart() {
        return start;
    }

    /**
     * Returns the outage end date and time.
     *
     * @return end date
     */
    public String getEnd() {
        return end;
    }
    
    /**
     * Returns the outage duration in hh:mm:ss.
     *
     * @return duration
     */
    public long getDuration() {
        return duration;
    }
    
    /**
     * Check if the outage is caused by a network problem up to the router address.
     * Can be a internal cabling or switch problem of the router it self.
     * 
     * @return true if the problem is internal
     */
    public String getOutageCausedInternal() {
        return Boolean.toString(outageCausedInternal);
    }
}
