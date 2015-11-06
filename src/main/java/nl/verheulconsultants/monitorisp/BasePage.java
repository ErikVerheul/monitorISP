/*
 * WicketExamplePage.java
 *
 * Created on November 4, 2015, 10:06 AM
 */
package nl.verheulconsultants.monitorisp;

import org.apache.wicket.markup.html.WebPage;

public abstract class BasePage extends WebPage {

    public BasePage() {
        super();
        add(new HeaderPanel("headerpanel", "Welcome To Wicket")); 
        add(new FooterPanel("footerpanel", "Powered by Wicket and the NetBeans Wicket Plugin"));
    }

}
