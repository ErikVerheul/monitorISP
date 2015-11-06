/*
 * HeaderPanel.java
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

public class HeaderPanel extends Panel {

    /**
     * Construct.
     * @param componentName name of the component
     * @param exampleTitle title of the example
     */

    public HeaderPanel(String componentName, String exampleTitle)
    {
        super(componentName);
        add(new Label("exampleTitle", exampleTitle));
    }

}
