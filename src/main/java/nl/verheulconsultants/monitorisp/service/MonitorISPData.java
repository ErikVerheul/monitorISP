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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import static nl.verheulconsultants.monitorisp.service.Utilities.getSessionDataFileName;
import org.apache.wicket.model.util.CollectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorISPData implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorISPData.class);
    private static final long serialVersionUID = 1L;
    private CollectionModel<Host> paletteModel;
    private List<Host> selected;
    private String routerAddress;
    private List<OutageListItem> outages;
    private MonitorISPData dataRead;
    private long startOfService;
    private long lastContactWithAnyHost;
    private long lastFail;
    private long numberOfInterruptions;
    private long failedChecks;
    private long successfulChecks;
    private long timeStamp;

    /**
     * Setter
     * @param paletteModel 
     */
    public void setPaletteModel(CollectionModel<Host> paletteModel) {
        this.paletteModel = paletteModel;
    }

    /**
     * Setter
     * @param selected 
     */
    public void setSelected(List<Host> selected) {
        this.selected = selected;
    }

    /**
     * Setter
     * @param routerAddress 
     */
    public void setRouterAddress(String routerAddress) {
        this.routerAddress = routerAddress;
    }
    
    /**
     * Setter
     * @param outages 
     */
    public void setOutages(List<OutageListItem> outages) {
        this.outages = outages;
    }
    
    /**
     * Setter
     * @param startOfService 
     */
    public void setStartOfService(long startOfService) {
        this.startOfService = startOfService;
    }
    
    /**
     * Setter
     * @param lastContactWithAnyHost 
     */
    public void setLastContactWithAnyHost(long lastContactWithAnyHost) {
        this.lastContactWithAnyHost = lastContactWithAnyHost;
    }
    
    /**
     * Setter
     * @param lastFail 
     */
    public void setLastFail(long lastFail) {
        this.lastFail = lastFail;
    }
    
    /**
     * Setter
     * @param numberOfInterruptions 
     */
    public void setNumberOfInterruptions(long numberOfInterruptions) {
        this.numberOfInterruptions = numberOfInterruptions;
    }
    
    /**
     * Setter
     * @param failedChecks 
     */
    public void setFailedChecks(long failedChecks) {
        this.failedChecks = failedChecks;
    }
    
    /**
     * Setter
     * @param successfulChecks 
     */
    public void setSuccessfulChecks(long successfulChecks) {
        this.successfulChecks = successfulChecks;
    }
    
    /**
     * Setter
     * @param timeStamp 
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    
    // Check if all fields are set for writing. Some values are not checked as they can be zero. 
    private boolean allSet() {
        return paletteModel != null && selected != null && routerAddress != null && outages != null 
                && startOfService > 0L && timeStamp > 0L;
    }
    
    // Check if all fields are read. Some values are not checked as they can be zero.
    private boolean allRead() {
        return dataRead.paletteModel != null && dataRead.selected != null && dataRead.routerAddress != null && dataRead.outages != null 
                && dataRead.startOfService > 0L && dataRead.timeStamp > 0L;
    }

    /**
     * Save all data of the current session.
     * @param allData
     * @return true is successful
     */
    public boolean saveData(MonitorISPData allData) {
        LOGGER.info("Save all data of the current session.");
        if (allSet()) {
            ObjectOutputStream oos;
            try (FileOutputStream fout = new FileOutputStream(getSessionDataFileName())) {
                oos = new ObjectOutputStream(fout);
                oos.writeObject(allData);
                return true;
            } catch (IOException ex) {
                LOGGER.error("The application data can not be saved in file {}. The exception is {}", getSessionDataFileName(), ex);
                return false;
            }
        } else {
            LOGGER.error("Cannot save. Not all data elements are set");
            return false;
        }
    }
    
    /**
     * Read all data of the previous session.
     * @return true is successful
     */
    public boolean readData() {
        LOGGER.info("Read all data of the previous session.");
        try (FileInputStream fin = new FileInputStream(getSessionDataFileName())) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            dataRead = (MonitorISPData) ois.readObject();
            if (allRead()) {
                return true;
            } else {
                LOGGER.error("Not all expected data was read.");
                return false;
            }
        } catch (IOException ex) {
            LOGGER.error("An IO error occurred reading file {}. The exception is {}", getSessionDataFileName(), ex);
            return false;
        } catch (ClassNotFoundException ex2) {
            LOGGER.error("Unexpected internal error with exception {}", ex2);
            return false;
        }
    }
    
    /**
     * Getter
     * @return paletteModel
     */
    public CollectionModel<Host> getPaletteModel() {
        return dataRead.paletteModel;
    }
    
    /**
     * Getter
     * @return selected
     */
    public List<Host> getSelected() {
        return dataRead.selected;
    }
    
    /**
     * Getter
     * @return routerAddress
     */
    public String getRouterAddress() {
        return dataRead.routerAddress;
    }
    
    /**
     * Getter
     * @return outages
     */
    public List<OutageListItem> getOutages() {
        return dataRead.outages;
    }
    
    /**
     * Getter
     * @return startOfService
     */
    public long getStartOfService() {
        return dataRead.startOfService;
    }
    
    /**
     * Getter
     * @return lastContactWithAnyHost
     */
    public long getLastContactWithAnyHost() {
        return dataRead.lastContactWithAnyHost;
    }
    
    /**
     * Getter
     * @return lastFail
     */
    public long getLastFail() {
        return dataRead.lastFail;
    }
    
    /**
     * Getter
     * @return numberOfInterruptions
     */
    public long getNumberOfInterruptions() {
        return dataRead.numberOfInterruptions;
    }
    
    /**
     * Getter
     * @return failedChecks
     */
    public long getFailedChecks() {
        return dataRead.failedChecks;
    }
    
    /**
     * Getter
     * @return successfulChecks
     */
    public long getSuccessfulChecks() {
        return dataRead.successfulChecks;
    }
    
    /**
     * Getter
     * @return timeStamp
     */
    public long getTimeStamp() {
        return dataRead.timeStamp;
    }
    

}
