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
package nl.verheulconsultants.monitorisp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage extends BasePage {

    static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    private List<Host> selected = new ArrayList<>();
    private String hostsFile = "MonitorISPhosts";
    private String selectedFile = "MonitorISPselected";

    public HomePage() {
        // Load the saved host table.
        try {
            HostList.read(hostsFile);
            logger.info("The hosts file is read with values {}", HostList.hosts);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("The hosts file {} can not be read. The exception is {}", hostsFile, ex);
        }
        // Load the saved selected items.
        try {
            selected = HostList.readSelected(selectedFile);
            logger.info("The selection file is read with values {}", selected);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("The selection file {} can not be read. The exception is {}", selected, ex);
        }
        // Show a message.
        add(new Label("message", "The user dir is " + System.getProperty("user.dir")));

        /**
         * Add a form with a palette with Save button to select hosts to use for the service 
         * or (optional) to be removed from the host list.
         */
        Form<?> form1 = new Form<Void>("form1") {
            @Override
            protected void onSubmit() {
                HostList.save(HostList.hosts, hostsFile);
                logger.info("The hosts file is saved with values {}", HostList.hosts);
                HostList.save(selected, selectedFile);
                logger.info("The selection file is saved with values {}", selected);
            }
        };
        add(form1);

        IChoiceRenderer<Host> renderer = new ChoiceRenderer<>("name", "id");

        final Palette<Host> palette = new Palette<>("palette",
                new ListModel<>(selected),
                new CollectionModel<>(HostList.hosts),
                renderer, 10, true, false);

        form1.add(palette);

        /**
         * Add a form with a button with onSubmit implementation to remove selected hosts.
         */     
        Form<?> form2 = new Form<>("form2");

        Button button1 = new Button("button1") {
            @Override
            public void onSubmit() {
                if (selected != null) {
                    HostList.hosts.removeAll(selected);
                }
                logger.info("These hosts should be removed {}", selected);
            }
        };
        add(form2);
        form2.add(button1);

        /**
         * Add a form where we can enter a new host URL if needed.
         */
        final TextField<String> url = new TextField<>("new-host", Model.of(""));
        url.setRequired(false);
        url.add(new MyUrlValidator());

        Form<?> form3 = new Form<Void>("form3") {
            @Override
            protected void onSubmit() {
                final String urlValue = url.getModelObject();
                if (urlValue != null) {
                    HostList.hosts.add(new Host(Integer.toString(HostList.hosts.size()), urlValue));
                }
                logger.info("The hosts file has now the values {}", HostList.hosts);
            }
        };
        add(form3);
        form3.add(url);
        
        /**
         * Add a form with a buttons to start and stop the service.
         */     
        Form<?> form4 = new Form<>("form4");

        Button button2 = new Button("button2") {
            @Override
            public void onSubmit() {
                // add the action
                logger.info("The is started with hosts {}", selected);
            }
        };
        
        Button button3 = new Button("button3") {
            @Override
            public void onSubmit() {
                // add the action
                logger.info("The sevice is stopped");
            }
        };
        
        add(form4);
        form4.add(button2);
        form4.add(button3);
    }
}

class MyUrlValidator implements IValidator<String> {

    @Override
    public void validate(IValidatable<String> validatable) {
        String url = validatable.getValue();
        if (!isValid(url)) {
            validatable.error(decorate(new ValidationError(this), validatable));
        }
    }

    /**
     * Check for a valid url but omit checking the protocol header.
     *
     * @param urlString
     * @return
     */
    boolean isValid(String urlString) {
        //Assigning the url format regular expression
        String urlPattern = "^[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
        return urlString.matches(urlPattern);
    }

    /**
     * Allows subclasses to decorate reported errors
     *
     * @param error
     * @return decorated error
     */
    protected IValidationError decorate(IValidationError error, IValidatable<String> validatable) {
        return error;
    }

}
