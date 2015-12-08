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
 */
public class WicketApplication extends WebApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(WicketApplication.class);
    public static final ISPController controller = new ISPController();

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

    /**
     * Read the data of the previous session.
     *
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init() {
        super.init();
    }

    /**
     * Kill the running thread and save current session data before service exit.
     */
    @Override
    public void onDestroy() {
        controller.exit();
        sleepMillis(140);
        if (controller.getSessionData().saveData()) {
            LOGGER.info("Session data is saved at exiting the application.");
        }
    }

}
