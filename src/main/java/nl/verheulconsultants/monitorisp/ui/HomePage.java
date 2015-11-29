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
import static nl.verheulconsultants.monitorisp.service.Utilities.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import nl.verheulconsultants.monitorisp.service.ISPController;
import nl.verheulconsultants.monitorisp.service.OutageListItem;
import nl.verheulconsultants.monitorisp.service.StatusListItem;
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
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HomePage extends BasePage {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(HomePage.class);
    
    static List<Host> selected = new ArrayList<>();
    static ListModel<Host> selectedModel;
    private static final List<Host> hosts = new ArrayList<>();
    static CollectionModel<Host> choicesModel = new CollectionModel<>(hosts);
    
    private static Palette<Host> palette;
    private final Form<?> formSelectHosts;
    private final Button removeButton;
    private final Button startButton;
    private final Button stopButton;
    private final TextField<String> newUrl;
    private final Form<?> formNewHost;
    private final InputRouterAddress address;
    private TextField<String> routerAddress;
    private final Form<?> formRouter;
    private static long lastTimeDataSaved;

    public HomePage() {
//        selectedModel = new ListModel<>(selected);
        controller.initWithPreviousSessionData();
        address = new InputRouterAddress(ISPController.getRouterAddress());
        routerAddress = new TextField<>("routerAddress", new PropertyModel(address, "address"));
        newUrl = new TextField<>("newHost", Model.of(""));

        formSelectHosts = new Form<Void>("paletteForm") {
            @Override
            protected void onSubmit() {
                if (saveSession()) {
                    LOGGER.info("All data are saved.");
                }
            }
        };

        formRouter = new Form<Void>("routerForm") {
            @Override
            protected void onSubmit() {
                final String addressValue = routerAddress.getModelObject();
                if ("unknown".equals(addressValue) || isValidHostAddress(addressValue)) {
                    ISPController.setRouterAddress(addressValue);
                    LOGGER.info("The router address is set to {}", addressValue);
                    saveSession();
                } else {
                    error("Wrong router address. Please try again or type unknown");
                }
            }
        };

        stopButton = new Button("stopButton") {
            @Override
            public void onSubmit() {
                if (controller != null
                        && controller.isBusyCheckingConnections()) {
                    controller.stopTemporarily();
                    LOGGER.info("The service is stopped temporarely.");
                } else {
                    LOGGER.info("Can not stop, the controller is not running.");
                }
            }
        };

        formNewHost = new Form<Void>("addHostForm") {
            @Override
            protected void onSubmit() {
                final String urlValue = newUrl.getModelObject();
                if (isValidHostAddress(urlValue)) {
                    Collection<Host> hosts = choicesModel.getObject();
                    hosts.add(new Host(Integer.toString(hosts.size()), urlValue));
                    LOGGER.info("The URL {} is added", urlValue);
                    LOGGER.info("The host list is changed to {}", choicesModel);
                    if (saveSession()) {
                        LOGGER.info("All data are saved.");
                    }
                } else {
                    error("Wrong host address. Please try again.");
                }
            }
        };

        startButton = new Button("startButton") {
            @Override
            public void onSubmit() {
                if (controller.isRunning()) {
                    if (!controller.isBusyCheckingConnections()) {
                        controller.restart(getNames(selected));
                        LOGGER.info("The service is restarted for checking connections with hosts {}", selected);
                    } else {
                        LOGGER.info("CANNOT start twice, the service is allready checking connections with {}", selected);
                    }
                } else {
                    controller.doInBackground(getNames(selected));
                    LOGGER.info("The service is started for checking connections with hosts {}", selected);
                }
            }
        };

        removeButton = new Button("removeButton") {
            @Override
            public void onSubmit() {
                Collection<Host> hosts = choicesModel.getObject();
                LOGGER.info("These URL's will be removed {}", selected);
                hosts.removeAll(selected);
                LOGGER.info("The model is changed to {}", choicesModel);
            }
        };

        constructorContinued();
    }

    private void constructorContinued() {
        // Show a message.
        add(new Label("message1", "The application home dir is " + APPHOMEDIR));
        add(new Label("message2", "The log file is located here " + getLogFileName()));
        add(new FeedbackPanel("feedback"));

        IChoiceRenderer<Host> renderer = new ChoiceRenderer<>("name", "id");
        palette = new Palette<>("palette1",
                selectedModel,
                choicesModel,
                renderer, 10, true, false);
        LOGGER.info("The palette is initiated with choices {}.", choicesModel);
        LOGGER.info("The palette is initiated with selection {}.", selected);

        // version 7.x.x
        palette.add(new DefaultTheme());

        add(formSelectHosts);
        formSelectHosts.add(palette);
        formSelectHosts.add(removeButton);
        formSelectHosts.add(startButton);
        formSelectHosts.add(stopButton);

        newUrl.setRequired(false);

        add(formNewHost);
        formNewHost.add(newUrl);

        add(formRouter);

        routerAddress.setRequired(false);
        formRouter.add(routerAddress);

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
                StatusListItem sli = (StatusListItem) item.getModelObject();
                item.add(new Label("Name", sli.getName()));
                item.add(new Label("Value", sli.getValue()));
                item.add(new Label("Index", sli.getIndex()));
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

        //get the list of items to display from provider (database, etc)
        //in the form of a LoadableDetachableModel
        IModel listOutageViewModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return ISPController.getOutageData();
            }
        };

        ListView outageListView = new ListView("outageListView", listOutageViewModel) {
            @Override
            protected void populateItem(final ListItem item) {
                OutageListItem olu = (OutageListItem) item.getModelObject();
                item.add(new Label("Index", olu.getIndex()));
                item.add(new Label("Start", olu.getStart()));
                item.add(new Label("End", olu.getEnd()));
                item.add(new Label("Duration", millisToTime(olu.getDuration())));
                item.add(new Label("OutageCausedInternal", olu.getOutageCauseAsString()));
            }
        };

        //encapsulate the ListView in a WebMarkupContainer in order for it to update
        WebMarkupContainer outageListContainer = new WebMarkupContainer("outageContainer");
        //generate a markup-id so the contents can be updated through an AJAX call
        outageListContainer.setOutputMarkupId(true);
        outageListContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        // add the list view to the container
        outageListContainer.add(outageListView);
        // finally add the container to the page
        add(outageListContainer);
    }
    
    /**
     * @return the palette model with all choices.
     */
    public static CollectionModel<Host> getPaletteModel() {
        return choicesModel;
    }
    
    /**
     * Set the palette choices
     * @param choicesModel 
     */
    public static void setPaletteModel(CollectionModel<Host> choicesModel) {
        HomePage.choicesModel = choicesModel;
        LOGGER.info("setPaletteModel set the choices to {}.", choicesModel);
    }

    /**
     * @return the selected hosts.
     */
    public static List<Host> getSelected() {
        return selected;
    }
    
    /**
     * Set the palette selection
     * @param selected 
     */
    public static void setSelected(List<Host> selected) {
        HomePage.selected = selected;
        selectedModel = new ListModel<>(selected);
        LOGGER.info("setSelected set the selection to {}.", selected);
    }
    
    
    /**
     * Return the date of the data save of the previous session.
     * @return the date as long
     */
    public static long getLastTimeDataSaved() {
        return lastTimeDataSaved;
    }
    
    /**
     * Set the date of the data save of the previous session.
     * @param lastTimeDataSaved
     */
    public static void setLastTimeDataSaved(long lastTimeDataSaved) {
        HomePage.lastTimeDataSaved = lastTimeDataSaved;
    }

    /**
     * Default initialization. Three known hosts to check connections. One dummy host is added to the choices to test failed connections.
     */
    public static void initWithDefaults() {
        Collection<Host> hostsLocal = choicesModel.getObject();
        hostsLocal.clear();
        hostsLocal.add(new Host("0", "willfailconnection.com"));
        Host uva = new Host("1", "uva.nl");
        hostsLocal.add(uva);
        Host xs4all = new Host("2", "xs4all.nl");
        hostsLocal.add(xs4all);
        Host vu = new Host("3", "vu.nl");
        hostsLocal.add(vu);

        selected.clear();
        selected.add(uva);
        selected.add(xs4all);
        selected.add(vu);
        selectedModel = new ListModel<>(selected);
    }

    private List<String> getNames(List<Host> hosts) {
        List<String> names = new ArrayList<>();
        for (Host h : hosts) {
            names.add(h.getName());
        }
        return names;
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

    /**
     * Simple data class that acts as a holder for the data for the router address field.
     */
    private static class InputRouterAddress implements IClusterable {

        private static final long serialVersionUID = 1L;
        String address;

        InputRouterAddress(String address) {
            this.address = address;
        }

        void setAddress(String address) {
            this.address = address;
        }

        String getAddress() {
            return address;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Router address = '" + address + "'";
        }
    }
}
