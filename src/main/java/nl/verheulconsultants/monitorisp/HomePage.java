/*
 * HomePage.java
 *
 * Created on November 4, 2015, 10:06 AM
 */
package nl.verheulconsultants.monitorisp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;

public class HomePage extends BasePage {

    private List<Hosts> selected = new ArrayList<>();
    private String hostsFile = "MonitorISPhosts";

    public HomePage() {
        try {
            HostList.read(hostsFile);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(HomePage.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        add(new Label("message", "The user dir is " + System.getProperty("user.dir")));

        add(new FeedbackPanel("feedback"));

        Form<?> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                info("Selected hosts : " + selected);
                HostList.save(selected, hostsFile);
            }
        };
        add(form);

        //make VPS selected by default
        //selected.add(new Hosting("2", "VPS"));
        IChoiceRenderer<Hosts> renderer = new ChoiceRenderer<>("name", "id");

        final Palette<Hosts> palette = new Palette<>("palette",
                new ListModel<>(selected),
                new CollectionModel<>(HostList.hosts),
                renderer, 10, true);

        form.add(palette);

    }

}
