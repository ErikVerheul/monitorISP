/*
 * FooterPanel.java
 *
 * Created on November 4, 2015, 10:06 AM
 */
 
package nl.verheulconsultants.monitorisp;           

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/** 
 *
 * @author erik
 * @version 
 */

public final class FooterPanel extends Panel {

    public FooterPanel(String id, String text) {
        super(id);
        add(new Label("footerpanel_text", text));
    }

}
