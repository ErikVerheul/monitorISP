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

import static nl.verheulconsultants.monitorisp.service.Globals.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import nl.verheulconsultants.monitorisp.service.StatusListItem;
import static nl.verheulconsultants.monitorisp.ui.PersistModel.loadModel;
import static nl.verheulconsultants.monitorisp.ui.PersistModel.loadSelected;
import static nl.verheulconsultants.monitorisp.ui.WicketApplication.controller;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.theme.DefaultTheme;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage extends BasePage {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomePage.class);
    private List<Host> selected = new ArrayList<>();
    private CollectionModel<Host> palletteModel;
    final Palette<Host> palette;
    
    /**
     * Add a form which saves all changes to disk after any button is clicked.
     */
    Form<?> form1 = new Form<Void>("paletteForm") {
        @Override
        protected void onSubmit() {
            if (!selected.isEmpty()) {
                if (PersistModel.saveChoices(palletteModel, CHOICESFILENAME)) {
                    LOGGER.info("The choise list is saved with values {}", palletteModel);
                } else {
                    LOGGER.error("The choise list could not be saved with values {}", palletteModel);
                }
                if (PersistModel.saveSelected(selected, SELECTIONFILENAME)) {
                    LOGGER.info("The selected items are saved with values {}", selected);
                } else {
                    LOGGER.error("The selected items could not be saved with values {}", selected);
                }
            } else {
                error("Please select one or more hosts.");
            }
        }
    };

    /**
     * A button to remove selected newUrl's from the list.
     */
    Button removeButton = new Button("removeButton") {
        @Override
        public void onSubmit() {
            Collection<Host> hosts = palletteModel.getObject();
            LOGGER.info("These URL's will be removed {}", selected);
            hosts.removeAll(selected);
            LOGGER.info("The model is changed to {}", palletteModel);
        }
    };

    /**
     * A button to start the service.
     */
    Button startButton = new Button("startButton") {
        @Override
        public void onSubmit() {
            List selectedHostsURLs = new <String>ArrayList();
            for (Host h : selected) {
                selectedHostsURLs.add(h.getName());
            }

            if (!selected.isEmpty()) {
                if (WicketApplication.controller.isRunning()) {
                    if (!WicketApplication.controller.isBusyCheckingConnections()) {
                        WicketApplication.controller.restart(selectedHostsURLs);
                        LOGGER.info("The service is restarted for checking connections with hosts {}", selected);
                    } else {
                        LOGGER.info("CANNOT start twice, the service is allready checking connections with {}", selected);
                    }
                } else {
                    WicketApplication.controller.doInBackground(selectedHostsURLs);
                    LOGGER.info("The service is started for checking connections with hosts {}", selected);
                }
            } else {
                LOGGER.warn("The service CANNOT be started with no hosts defined to check the connection.");
            }
        }
    };

    /**
     * A button to stop the service temporarily.
     */
    Button stopButton = new Button("stopButton") {
        @Override
        public void onSubmit() {
            if (WicketApplication.controller != null
                    && WicketApplication.controller.isBusyCheckingConnections()) {
                WicketApplication.controller.stopTemporarily();
                LOGGER.info("The service is stopped temporarely.");
            } else {
                LOGGER.info("Can not stop, the controller is not running.");
            }
        }
    };

    /**
     * A text field where we can enter a new host URL if needed.
     */
    final TextField<String> newUrl = new TextField<>("new-host", Model.of(""));
    Form<?> form2 = new Form<Void>("addForm") {
        @Override
        protected void onSubmit() {
            final String urlValue = newUrl.getModelObject();
            Collection<Host> hosts = palletteModel.getObject();
            hosts.add(new Host(Integer.toString(hosts.size()), urlValue));
            LOGGER.info("The URL {} is added", urlValue);
            LOGGER.info("The model is changed to {}", palletteModel);
        }
    };

    public HomePage() {
        // Load the saved host table or initiate with default values.
        palletteModel = loadModel(CHOICESFILENAME);
        selected = loadSelected(SELECTIONFILENAME);

        // Show a message.
        add(new Label("message1", "The application home dir is " + APPHOMEDIR));
        add(new Label("message2", "The log file is located here " + getLogFileName()));
        add(new FeedbackPanel("feedback"));

        IChoiceRenderer<Host> renderer = new ChoiceRenderer<>("name", "id");
        palette = new Palette<>("palette1",
                new ListModel<>(selected),
                palletteModel,
                renderer, 10, true, false);
        
        // version 7.x.x
        palette.add(new DefaultTheme()); 
        
        add(form1);
        form1.add(palette);
        form1.add(removeButton);
        form1.add(startButton);
        form1.add(stopButton);

        newUrl.setRequired(false);
        newUrl.add(new MyUrlValidator());

        add(form2);
        form2.add(newUrl);
       
        //get the list of items to display from provider (database, etc)
        //in the form of a LoadableDetachableModel
        IModel listStatusViewModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return controller.getStatusData();
            }
        };

        ListView statusListView = new ListView("statusListView", listStatusViewModel) {
            @Override
            protected void populateItem(final ListItem item) {
                StatusListItem mli = (StatusListItem) item.getModelObject();
                item.add(new Label("Name", mli.name));
                item.add(new Label("Value", mli.value));
                item.add(new Label("Index", mli.index));
            }
        };

        //encapsulate the ListView in a WebMarkupContainer in order for it to update
        WebMarkupContainer statusListContainer = new WebMarkupContainer("statusContainer");
        //generate a markup-id so the contents can be updated through an AJAX call
        statusListContainer.setOutputMarkupId(true);
        statusListContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        // add the list view to the container
        statusListContainer.add(statusListView);
        // finally add the container to the page
        add(statusListContainer);
    }

    private String getLogFileName() {
        FileAppender fileAppender = null;

        Enumeration appenders = LogManager.getRootLogger().getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender currAppender = (Appender) appenders.nextElement();
            if (currAppender instanceof FileAppender) {
                fileAppender = (FileAppender) currAppender;
            }
        }

        if (fileAppender != null) {
            return fileAppender.getFile();
        } else {
            return "Log file location not found.";
        }
    }
}
