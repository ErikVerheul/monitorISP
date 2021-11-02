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

import java.util.ArrayList;
import java.util.List;
import nl.verheulconsultants.monitorisp.service.Host;
import nl.verheulconsultants.monitorisp.service.ISPController;
import static nl.verheulconsultants.monitorisp.service.Utilities.sleepMillis;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 *
 * @see nl.verheulconsultants.monitorisp.Start#main(String[])
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public class WicketApplication extends WebApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(WicketApplication.class);

    /**
     *
     */
    public static final ISPController CONTROLLER = new ISPController();

    /**
     * Make the CONTROLLER available.
     *
     * @return the ISPController
     */
    public static ISPController getController() {
        return CONTROLLER;
    }

    /**
     * Set the home page.
     *
     * @return HomePage
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage() {
        return HomePage.class;
    }

    private List<String> getAddresses(List<Host> hosts) {
        List<String> addresses = new ArrayList<>();
        hosts.stream().forEach((h) -> {
            addresses.add(h.getHostAddress());
        });
        return addresses;
    }

    /**
     * Read the data of the previous session. Start the controller.
     *
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();
        if (!CONTROLLER.isRunning()) {
            CONTROLLER.initWithPreviousSessionData();
            CONTROLLER.doInBackground(getAddresses(CONTROLLER.getSelected()));
            LOGGER.info("Application init(): The service is started for checking connections with hosts {}", CONTROLLER.getSelected());
        }
    }

    /**
     * Kill the running thread and save current session data before service exitService.
     */
    @Override
    public void onDestroy() {
        CONTROLLER.exitService();
        sleepMillis(140);
        if (CONTROLLER.getSessionData().saveData()) {
            LOGGER.info("Session data is saved at exiting the application.");
        }
    }

}
