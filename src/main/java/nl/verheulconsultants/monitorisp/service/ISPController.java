/*
 * Switch to the other ISP is the current ISP is not reachable. When on the backup ISP try to revert to the primary ISP.
 */
package nl.verheulconsultants.monitorisp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author erik
 */
class ISPController {
    static final Logger logger = LoggerFactory.getLogger(ISPController.class);
    private Globals g;
    private boolean done = false;
    private boolean stop = false;
    private boolean exit = false;

    ISPController(Globals g) {
        this.g = g;
    }

    boolean isRunning() {
        return !done;
    }

    void stop() {
        stop = true;
    }

    void restart() {
        done = false;
        stop = false;
    }

    void exit() {
        stop = true;
        exit = true;
    }

    boolean isDone() {
        return done;
    }

    @SuppressWarnings("static-access")
    protected void doInBackground() {
        done = false;
        stop = false;
        logger.info("De controller is gestart.");

        do {
            // wait until the JMX client defines at least one host
            if (g.hosts.size() > 0) {
                while (!stop) {             
                    if (!g.checkISP()) {
                        long timeLeftForSwitchOver = g.triggerDuration * 1000L - (System.currentTimeMillis() - g.lastContactWithAnyHost);
                        logger.info("De {} ISP is niet bereikbaar. Tijd over tot omschakeling is {} sec.", 
                                new Object[]{g.getCurrentISPString(), timeLeftForSwitchOver / 1000});
                    }
                    g.waitMilis(5000);  // wait 5 seconds to check the ISP connection again
                }
            }
            if (!done) {
                logger.info("De controller is gestopt.\n");
                logger.info("Er zijn {}{} connectie checks uitgevoerd.", g.successfulChecks, g.failedChecks);
            }
            done = true;
            // wait for instructions to restart or to exit completely
            g.waitMilis(1000);
        } while (!exit);
    }
}
