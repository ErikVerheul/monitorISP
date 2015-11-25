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
import java.util.ArrayList;
import java.util.List;
import static nl.verheulconsultants.monitorisp.service.Utilities.sessionDataFileName;
import nl.verheulconsultants.monitorisp.ui.Host;
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
    private static final String CANNOTGET = "Cannot get. The data is not read yet";

    public void setPaletteModel(CollectionModel<Host> paletteModel) {
        this.paletteModel = paletteModel;
    }

    public void setSelected(List<Host> selected) {
        this.selected = selected;
    }

    public void setRouterAddress(String routerAddress) {
        this.routerAddress = routerAddress;
    }

    public void setOutages(List<OutageListItem> outages) {
        this.outages = outages;
    }
    
    public void setStartOfService(long startOfService) {
        this.startOfService = startOfService;
    }
    
    public void setLastContactWithAnyHost(long lastContactWithAnyHost) {
        this.lastContactWithAnyHost = lastContactWithAnyHost;
    }
    
    public void setLastFail(long lastFail) {
        this.lastFail = lastFail;
    }
    
    public void setNumberOfInterruptions(long numberOfInterruptions) {
        this.numberOfInterruptions = numberOfInterruptions;
    }
    
    public void setFailedChecks(long failedChecks) {
        this.failedChecks = failedChecks;
    }
    
    public void setSuccessfulChecks(long successfulChecks) {
        this.successfulChecks = successfulChecks;
    }
    
    
    // Check if all fields are set for writing. Some values are not checked as they can be zero.
    private boolean allSet() {
        return paletteModel != null && selected != null && routerAddress != null && outages != null 
                && startOfService > 0 && lastContactWithAnyHost > 0;
    }
    
    // Check if all fields are read. Some values are not checked as they can be zero.
    private boolean allRead() {
        return dataRead.paletteModel != null && dataRead.selected != null && dataRead.routerAddress != null && dataRead.outages != null 
                && dataRead.startOfService > 0 && dataRead.lastContactWithAnyHost > 0;
    }

    public boolean saveData(MonitorISPData allData) {
        if (allSet()) {
            ObjectOutputStream oos;
            try (FileOutputStream fout = new FileOutputStream(sessionDataFileName)) {
                oos = new ObjectOutputStream(fout);
                oos.writeObject(allData);
                return true;
            } catch (IOException ex) {
                LOGGER.error("The application data can not be saved in file {}. The exception is {}", sessionDataFileName, ex);
                return false;
            }
        } else {
            LOGGER.error("Cannot save. Not all data elements are set");
            return false;
        }
    }
    
    public boolean readData() {
        try (FileInputStream fin = new FileInputStream(sessionDataFileName)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            dataRead = (MonitorISPData) ois.readObject();
            if (allRead()) {
                return true;
            } else {
                LOGGER.error("Not all expected data was read.");
                return false;
            }
        } catch (IOException ex) {
            LOGGER.error("An IO error occurred reading file {}. The exception is {}", sessionDataFileName, ex);
            return false;
        } catch (ClassNotFoundException ex2) {
            LOGGER.error("Unexpected internal error with exception {}", ex2);
            return false;
        }
    }
    
    private boolean isDataRead() {
        return dataRead != null;
    }
    
    
    public CollectionModel<Host> getPaletteModel() {
        if (isDataRead()) {
            return dataRead.paletteModel;
        } else {
            LOGGER.error(CANNOTGET);
            return null;
        }
    }
    
    public List<Host> getSelected() {
        if (isDataRead()) {
            return dataRead.selected;
        } else {
            LOGGER.error(CANNOTGET);
            return new ArrayList<>();
        }
    }
    
    public String getRouterAddress() {
        if (isDataRead()) {
            return dataRead.routerAddress;
        } else {
            LOGGER.error(CANNOTGET);
            return null;
        }
    }
    
    public List<OutageListItem> getOutages() {
        if (isDataRead()) {
            return dataRead.outages;
        } else {
            LOGGER.error(CANNOTGET);
            return new ArrayList<>();
        }
    }
    
    public long getStartOfService() {
        if (isDataRead()) {
            return dataRead.startOfService;
        } else {
            LOGGER.error(CANNOTGET);
            return 0L;
        } 
    }
    
    public long getLastContactWithAnyHost() {
        if (isDataRead()) {
            return dataRead.lastContactWithAnyHost;
        } else {
            LOGGER.error(CANNOTGET);
            return 0L;
        } 
    }
    
    public long getLastFail() {
        if (isDataRead()) {
            return dataRead.lastFail;
        } else {
            LOGGER.error(CANNOTGET);
            return 0L;
        } 
    }
    
    public long getNumberOfInterruptions() {
        if (isDataRead()) {
            return dataRead.numberOfInterruptions;
        } else {
            LOGGER.error(CANNOTGET);
            return 0L;
        } 
    }
    
    public long getFailedChecks() {
        if (isDataRead()) {
            return dataRead.failedChecks;
        } else {
            LOGGER.error(CANNOTGET);
            return 0L;
        } 
    }
    
    public long getSuccessfulChecks() {
        if (isDataRead()) {
            return dataRead.successfulChecks;
        } else {
            LOGGER.error(CANNOTGET);
            return 0L;
        } 
    }
    

}
