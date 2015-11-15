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

import static nl.verheulconsultants.monitorisp.service.Status.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import static nl.verheulconsultants.monitorisp.ui.PersistModel.loadModel;
import static nl.verheulconsultants.monitorisp.ui.PersistModel.loadSelected;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.LogManager;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
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
    private CollectionModel<Host> theModel;
    final Palette<Host> palette1;
    
    /**
     * Add a form which saves all changes to disk after any button is clicked.
     */
    Form<?> form1 = new Form<Void>("paletteForm") {
        @Override
        protected void onSubmit() {
            if (!selected.isEmpty()) {
                if (PersistModel.saveChoices(theModel, CHOICESFILENAME)) {
                    LOGGER.info("The choise list is saved with values {}", theModel);
                } else {
                    LOGGER.error("The choise list could not be saved with values {}", theModel);
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
     * A button to remove selected url's from the list.
     */
    Button button1 = new Button("removeButton") {
        @Override
        public void onSubmit() {
            Collection<Host> hosts = theModel.getObject();
            LOGGER.info("These URL's will be removed {}", selected);
            hosts.removeAll(selected);
            LOGGER.info("The model is changed to {}", theModel);
        }
    };

    /**
     * A button to start the service.
     */
    Button button2 = new Button("startButton") {
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
    Button button3 = new Button("stopButton") {
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
    final TextField<String> url = new TextField<>("new-host", Model.of(""));
    Form<?> form2 = new Form<Void>("addForm") {
        @Override
        protected void onSubmit() {
            final String urlValue = url.getModelObject();
            Collection<Host> hosts = theModel.getObject();
            hosts.add(new Host(Integer.toString(hosts.size()), urlValue));
            LOGGER.info("The URL {} is added", urlValue);
            LOGGER.info("The model is changed to {}", theModel);
        }
    };

    public HomePage() {
        // Load the saved host table or initiate with default values.
        theModel = loadModel(CHOICESFILENAME);
        selected = loadSelected(SELECTIONFILENAME);

        // Show a message.
        add(new Label("message1", "The application home dir is " + APPHOMEDIR));
        add(new Label("message2", "The log file is located here " + getLogFileName()));

        add(form1);
        add(new FeedbackPanel("feedback"));

        IChoiceRenderer<Host> renderer = new ChoiceRenderer<>("name", "id");

        palette1 = new Palette<>("palette1",
                new ListModel<>(selected),
                theModel,
                renderer, 10, true, false);
        form1.add(palette1);

        form1.add(button1);

        url.setRequired(false);
        url.add(new MyUrlValidator());

        add(form2);
        form2.add(url);

        form1.add(button2);
        form1.add(button3);

        //get the list of items to display from provider (database, etc)
        //in the form of a LoadableDetachableModel
        IModel listViewModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return getData();
            }
        };

        ListView listView = new ListView("listView", listViewModel) {
            @Override
            protected void populateItem(final ListItem item) {
                MyListItem mli = (MyListItem) item.getModelObject();
                item.add(new Label("Name", mli.name));
                item.add(new Label("Value", mli.value));
                item.add(new Label("Index", mli.index));
            }
        };

        //encapsulate the ListView in a WebMarkupContainer in order for it to update
        WebMarkupContainer listContainer = new WebMarkupContainer("theContainer");
        //generate a markup-id so the contents can be updated through an AJAX call
        listContainer.setOutputMarkupId(true);
        listContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        // add the list view to the container
        listContainer.add(listView);
        // finally add the container to the page
        add(listContainer);
    }

    private List getData() {
        List ret = new ArrayList();

        MyListItem x0 = new MyListItem();
        x0.name = "startOfService";
        x0.value = new Date(startOfService).toString();
        x0.index = 1;
        ret.add(x0);

        MyListItem x1 = new MyListItem();
        x1.name = "lastContactWithAnyHost";
        x1.value = new Date(lastContactWithAnyHost).toString();
        x1.index = 2;
        ret.add(x1);

        MyListItem x2 = new MyListItem();
        x2.name = "lastFail";
        if (lastFail > 0) {
            x2.value = new Date(lastFail).toString();
        } else {
            x2.value = "No failure yet";
        }
        x2.index = 3;
        ret.add(x2);

        MyListItem x3 = new MyListItem();
        x3.name = "numberOfInterruptions";
        x3.value = Long.toString(numberOfInterruptions);
        x3.index = 4;
        ret.add(x3);

        MyListItem x4 = new MyListItem();
        x4.name = "failedChecks";
        x4.value = Long.toString(failedChecks);
        x4.index = 5;
        ret.add(x4);

        MyListItem x5 = new MyListItem();
        x5.name = "successfulChecks";
        x5.value = Long.toString(successfulChecks);
        x5.index = 6;
        ret.add(x5);

        MyListItem x6 = new MyListItem();
        x6.name = "totalISPunavailability";
        x6.value = millisToTime(totalISPunavailability);
        x6.index = 7;
        ret.add(x6);

        MyListItem x7 = new MyListItem();
        x7.name = "INTERNET UP?";
        if (busyCheckingConnections) {
            x7.value = Boolean.toString(canReachISP);
        } else {
            x7.value = "UNKNOWN, conroller is not running";
        }
        x7.index = 8;
        ret.add(x7);

        return ret;
    }

    // a very simple model object just to have something concrete for an example
    private class MyListItem {

        String name;
        String value;
        int index;
    }

    String millisToTime(long millis) {
        long second = 0;
        long minute = 0;
        long hour = 0;
        if (millis > 0) {
            second = (millis / 1000) % 60;
            minute = (millis / (1000 * 60)) % 60;
            hour = (millis / (1000 * 60 * 60)) % 24;
        }
        return String.format("%02d:%02d:%02d", hour, minute, second) + " [h:m:s]";
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
