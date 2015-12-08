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
import java.util.concurrent.CopyOnWriteArrayList;
import static nl.verheulconsultants.monitorisp.service.ISPController.NOROUTERADDRESS;
import static nl.verheulconsultants.monitorisp.service.Utilities.getSessionDataFileName;
import org.apache.wicket.model.util.CollectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A DAO with all session data that need to be saved and loaded when the service is brought down.
 */
public class MonitorISPData implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorISPData.class);
    private MonitorISPData dataRead;
    private static final long serialVersionUID = 1L;

    CollectionModel<Host> paletteModel;
    List<Host> selected;
    String routerAddress;
    List<OutageListItem> outages;
    long startOfService;
    long lastContactWithAnyHost;
    long lastFail;
    long numberOfInterruptions;
    long failedChecks;
    long successfulChecks;
    long timeStamp;

    /**
     * A DAO for saving and loading all session data in one go.
     *
     */
    MonitorISPData() {
        paletteModel = new CollectionModel<>();
        selected = new ArrayList<>();
        routerAddress = NOROUTERADDRESS;
        outages = new CopyOnWriteArrayList<>();
        startOfService = System.currentTimeMillis();
        lastContactWithAnyHost = 0L;
        lastFail = 0L;
        numberOfInterruptions = 0L;
        failedChecks = 0L;
        successfulChecks = 0L;
        timeStamp = 0L;
        LOGGER.info("MonitorISPData is initialized");
    }

    // Check if the fields are set for writing. Some values are not checked as they can be zero. 
    private boolean allSet() {
        if (!paletteModel.getObject().isEmpty()
                && null != selected
                && null != routerAddress
                && null != outages
                && startOfService > 0L
                && timeStamp > 0L) {
            return true;
        } else {
            LOGGER.error("WRITE Check falied: \npaletteModel = {}, \n#choices = {}, \nselected = {}, \nrouterAddress = {}, \noutages = {}, \nstartOfService = {}, \ntimeStamp = {}",
                    paletteModel,
                    null == paletteModel ? 0 : paletteModel.getObject().size(),
                    selected,
                    routerAddress,
                    outages,
                    startOfService,
                    timeStamp);
            return false;
        }
    }

    // Check if the fields are read. Some values are not checked as they can be zero or not yet initialized.
    private boolean allRead() {
        if (!dataRead.paletteModel.getObject().isEmpty()
                && null != dataRead.selected
                && null != dataRead.routerAddress
                && null != dataRead.outages
                && dataRead.startOfService > 0L
                && dataRead.timeStamp > 0L) {
            return true;
        } else {
            LOGGER.error("READ check failed: \npaletteModel = {}, \n#choices = {}, \nselected = {}, \nrouterAddress = {}, \noutages = {}, \nstartOfService = {}, \ntimeStamp = {}",
                    dataRead.paletteModel,
                    null == dataRead.paletteModel ? 0 : dataRead.paletteModel.getObject().size(),
                    dataRead.selected,
                    dataRead.routerAddress,
                    dataRead.outages,
                    dataRead.startOfService,
                    dataRead.timeStamp);
            return false;
        }
    }

    /**
     * Save all data of the current session.
     *
     * @return true is successful
     */
    public boolean saveData() {
        LOGGER.info("Save all data of the current session.");
        timeStamp = System.currentTimeMillis();
        if (allSet()) {
            try (FileOutputStream fout = new FileOutputStream(getSessionDataFileName());
                    ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                oos.writeObject(this);
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
     *
     * @return true if data is read successful
     */
    public boolean loadData() {
        LOGGER.info("Read all data of the previous session.");
        try (FileInputStream fin = new FileInputStream(getSessionDataFileName());
                ObjectInputStream ois = new ObjectInputStream(fin)) {
            dataRead = (MonitorISPData) ois.readObject();
            if (allRead()) {
                this.paletteModel = dataRead.paletteModel;
                this.selected = dataRead.selected;
                this.routerAddress = dataRead.routerAddress;
                this.outages = dataRead.outages;
                this.startOfService = dataRead.startOfService;
                this.lastContactWithAnyHost = dataRead.lastContactWithAnyHost;
                this.lastFail = dataRead.lastFail;
                this.numberOfInterruptions = dataRead.numberOfInterruptions;
                this.failedChecks = dataRead.failedChecks;
                this.successfulChecks = dataRead.successfulChecks;
                this.timeStamp = dataRead.timeStamp;
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

}
