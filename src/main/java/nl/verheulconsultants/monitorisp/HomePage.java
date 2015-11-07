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
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.util.CollectionModel;
import org.apache.wicket.model.util.ListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage extends BasePage {

    static final Logger logger = LoggerFactory.getLogger(BasePage.class);

    private List<Hosts> selected = new ArrayList<>();
    private String hostsFile = "MonitorISPhosts";
    private String selectedFile = "MonitorISPselected";

    public HomePage() {
        try {
            HostList.read(hostsFile);
            logger.info("The hosts file is read with values {}", HostList.hosts);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("The hosts file {} can not be read. The exception is {}", hostsFile, ex);
        }
        
        try {
            selected = HostList.readSelected(selectedFile);            
            logger.info("The selection file is read with values {}", selected);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("The selection file {} can not be read. The exception is {}", selected, ex);
        }

        add(new Label("message", "The user dir is " + System.getProperty("user.dir")));

        /**
         * Save the selected hosts on file with the 'save' button. Mark the
         * current selected items.
         */
        Form<?> form = new Form<Void>("form") {
            @Override
            protected void onSubmit() {
                HostList.save(HostList.hosts, hostsFile);
                logger.info("The hosts file is saved with values {}", HostList.hosts);
                HostList.save(selected, selectedFile);
                logger.info("The selection file is saved with values {}", selected);
            }
        };
        add(form);

        IChoiceRenderer<Hosts> renderer = new ChoiceRenderer<>("name", "id");

        final Palette<Hosts> palette = new Palette<>("palette",
                new ListModel<>(selected),
                new CollectionModel<>(HostList.hosts),
                renderer, 10, true, false);

        form.add(palette);

    }

}
