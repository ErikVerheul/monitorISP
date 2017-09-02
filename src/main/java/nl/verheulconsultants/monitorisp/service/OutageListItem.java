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
import java.util.Date;
import static nl.verheulconsultants.monitorisp.service.Utilities.CONTROLLERDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.SERVICEDOWN;
import static nl.verheulconsultants.monitorisp.service.Utilities.INTERNAL;
import static nl.verheulconsultants.monitorisp.service.Utilities.ISP;
import static nl.verheulconsultants.monitorisp.service.Utilities.millisToTime;

/**
 * Class for storing the different outage occurrences.
 *
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public class OutageListItem implements Serializable {

    private static final long serialVersionUID = 1L;
    int index;
    long outageStart;
    long outageEnd;
    long duration;
    int cause;

    OutageListItem(int index, long start, long end, long duration, int cause) {
        this.index = index;
        this.outageStart = start;
        this.outageEnd = end;
        this.duration = duration;
        this.cause = cause;
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
     * Returns the outage outageStart date and time.
     *
     * @return outageStart date
     */
    public String getStart() {
        return new Date(outageStart).toString();
    }

    /**
     * Returns the outage outageEnd date and time.
     *
     * @return outageEnd date
     */
    public String getEnd() {
        return new Date(outageEnd).toString();
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
     * Return the cause type of an outage.
     *
     * @return the cause
     */
    public int getOutageCause() {
        return cause;
    }

    /**
     * Return the cause type of an outage as string.
     *
     * @return the cause as String
     */
    public String getOutageCauseAsString() {
        switch (cause) {
            case ISP:
                return "ISP";
            case INTERNAL:
                return "internal network problem";
            case SERVICEDOWN:
                return "service was down";
            case CONTROLLERDOWN:
                return "controller was down";
            default:
                return "unknown outage type";
        }
    }

    @Override
    public String toString() {
        return "Outage [" + index + ", from:" + new Date(outageStart).toString() + ", to:" + new Date(outageEnd).toString()
                + ", duration:" + millisToTime(duration) + ", cause = " + getOutageCauseAsString() + "]";
    }
}
