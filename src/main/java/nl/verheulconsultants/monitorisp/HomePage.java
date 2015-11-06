/*
 * HomePage.java
 *
 * Created on November 4, 2015, 10:06 AM
 */

package nl.verheulconsultants.monitorisp;           

import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;

public class HomePage extends BasePage {
    
    private static final List<Hosts> listHosting;

    static {
        listHosting = new ArrayList<>();
        listHosting.add(new Hosts("1", "google-public-dns-a.google.com"));
        listHosting.add(new Hosts("2", "xs4all.nl"));
        listHosting.add(new Hosts("3", "uva.nl"));
    }

    private List<Hosts> selected = new ArrayList<>();


    public HomePage() {
        add(new Label("message", "Hello, World!"));
        
        add(new FeedbackPanel("feedback"));

        Form<?> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                info("Selected language : " + selected);
            }
        };
        add(form);

	//make VPS selected by default
        //selected.add(new Hosting("2", "VPS"));
        IChoiceRenderer<Hosts> renderer = new ChoiceRenderer<>("name", "id");

        final Palette<Hosts> palette = new Palette<>("palette",
                new ListModel<>(selected),
                new CollectionModel<>(listHosting),
                renderer, 10, true);

        form.add(palette);
        
    }

}
