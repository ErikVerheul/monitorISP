/*
 * The MIT License
 *
 * Copyright (c) 2015-2021, Verheul Consultants
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

import nl.verheulconsultants.monitorisp.service.Host;
import java.util.ArrayList;
import static nl.verheulconsultants.monitorisp.service.Utilities.*;
import java.util.List;
import java.time.Duration;
import nl.verheulconsultants.monitorisp.service.ISPController;
import nl.verheulconsultants.monitorisp.service.OutageListItem;
import nl.verheulconsultants.monitorisp.service.StatusListItem;
import static nl.verheulconsultants.monitorisp.ui.WicketApplication.getController;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
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
import org.apache.wicket.util.io.IClusterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Standard Wicket setup.
 *
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public final class HomePage extends BasePage {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(HomePage.class);
    private static final ISPController CONTROLLER = getController();
    private final Palette<Host> palette;
    private final Form<?> formSelectHosts;
    private final Button removeButton;
    private final AjaxButton startStopButton;
    private final TextField<String> newHostName;
    private final Form<?> formStartStop;
    private final Form<?> formNewHost;
    private final InputRouterAddress address;
    private TextField<String> routerAddress;
    private final Form<?> formRouter;
    private static final int AJAX_UPDATE_INTERVAL = 5;
    private String startStopLabelText = "Stop";

    /**
     * Wicket initializes this page multiple times. Be aware not to execute code multiple times if not allowed.
     */
    public HomePage() {
        ///////////////////////// Show the router address /////////////////////
        address = new InputRouterAddress(CONTROLLER.getRouterAddress());
        routerAddress = new TextField<>("routerAddress", new PropertyModel(address, "address"));

        ///////////////////////// Select hosts to test against ////////////////
        IChoiceRenderer<Host> renderer = new ChoiceRenderer<>("hostAddress", "id");
        palette = new Palette<>("palette1", CONTROLLER.getSelectedModel(), CONTROLLER.getPaletteModel(), renderer, 10, true) {
            @Override
            protected Recorder newRecorderComponent() {
              Recorder recorder = super.newRecorderComponent();
              recorder.add(new AjaxFormComponentUpdatingBehavior("change") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                  processInput(); // let Palette process input too
                  LOGGER.info("The selection is changed by the user to {}", getValue());
                  CONTROLLER.setSelected(getSelectedChoices());
                }
              });
              return recorder;
            }
        };
        palette.add(new DefaultTheme());
        LOGGER.info("The palette is initiated with choices {}.", CONTROLLER.getPaletteModel());
        LOGGER.info("The palette is initiated with selection {}.", CONTROLLER.getSelected());

        formSelectHosts = new Form<Void>("paletteForm") {
        };
        add(formSelectHosts);
        formSelectHosts.add(palette);

        ////////////////////// Remove available hosts /////////////////////////
        // note: palette.modelChanged() in AjaxButton does not work!
        removeButton = new Button("removeButton") {
            @Override
            public void onSubmit() {
                CONTROLLER.removeAvailableHosts();
                palette.modelChanged();
            }
        };
        formSelectHosts.add(removeButton);

        //////////////////////// Start-Stop button ////////////////////////////
        formStartStop = new Form<Void>("startStopForm") {
        };
        startStopButton = new AjaxButton("btnId") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                if (CONTROLLER != null) {
                    if (CONTROLLER.isBusyCheckingConnections()) {
                        CONTROLLER.stopTemporarily();
                        LOGGER.info("The service is stopped temporarily.");
                        startStopLabelText = "Start";
                    } else {
                        startRunning();
                        LOGGER.info("The service is started from a temporary stop.");
                        startStopLabelText = "Stop";
                    }
                } else {
                    LOGGER.info("Can not start or stop temporary, the controller is not running.");
                    startStopLabelText = "The controller is not running";
                }
                target.add(this);
            }
        };

        Label startStopLabel = new Label("labelId", new Model<String>() {
            @Override
            public String getObject() {
                return startStopLabelText;
            }
        });
        startStopButton.add(startStopLabel);
        formStartStop.add(startStopButton);
        add(formStartStop);

        ////////////////////////// Add new host ///////////////////////////////
        newHostName = new TextField<>("newHost", Model.of(""));
        formNewHost = new Form<Void>("addHostForm") {
            @Override
            protected void onSubmit() {
                final String hostName = newHostName.getModelObject();
                if (isValidHostAddress(hostName)) {
                    CONTROLLER.addChoice(hostName);
                    palette.modelChanged();
                    LOGGER.info("The user added the hostname {} to the available choices.", hostName);
                } else {
                    error("Wrong host name. Please try again.");
                }
            }
        };
        newHostName.setRequired(false);
        formNewHost.add(newHostName);
        add(formNewHost);

        //////////////////////// Set new router address ///////////////////////
        formRouter = new Form<Void>("routerForm") {
            @Override
            protected void onSubmit() {
                final String addressValue = routerAddress.getModelObject();
                if ("unknown".equals(addressValue) || isValidHostAddress(addressValue)) {
                    CONTROLLER.setRouterAddress(addressValue);
                    LOGGER.info("The router address is set to {}", addressValue);
                } else {
                    error("Wrong router address. Please try again or type unknown");
                }
            }
        };
        routerAddress.setRequired(false);
        formRouter.add(routerAddress);
        add(formRouter);

        //////////////// feedback panel for showing errors etc. ////////////////
        add(new FeedbackPanel("feedback"));

        //////////////////////// Display Status view //////////////////////////
        //get the list of items to display from provider (database, etc) in the form of a LoadableDetachableModel
        IModel listStatusViewModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return CONTROLLER.getStatusData();
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
        statusListContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(AJAX_UPDATE_INTERVAL)));
        // add the list view to the container
        statusListContainer.add(statusListView);
        // finally add the container to the page
        add(statusListContainer);

        //get the list of items to display from provider (database, etc)
        //in the form of a LoadableDetachableModel
        IModel listOutageViewModel = new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return CONTROLLER.getOutageDataReversedOrder();
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
        outageListContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.ofSeconds(AJAX_UPDATE_INTERVAL)));
        // add the list view to the container
        outageListContainer.add(outageListView);
        // finally add the container to the page
        add(outageListContainer);
    }

    private void startRunning() {
        if (CONTROLLER.isRunning()) {
            if (!CONTROLLER.isBusyCheckingConnections()) {
                CONTROLLER.restart(getAddresses(CONTROLLER.getSelected()));
                LOGGER.info("The service is restarted for checking connections with hosts {}", CONTROLLER.getSelected());
            } else {
                LOGGER.info("CANNOT start twice, the service is allready checking connections with {}", CONTROLLER.getSelected());
            }
        }
    }

    private List<String> getAddresses(List<Host> hosts) {
        List<String> addresses = new ArrayList<>();
        hosts.stream().forEach((h) -> {
            addresses.add(h.getHostAddress());
        });
        return addresses;
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
