package nl.verheulconsultants.monitorisp.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Collection;
import java.util.List;
import nl.verheulconsultants.monitorisp.service.ISPController;
import static nl.verheulconsultants.monitorisp.service.ISPController.initWithPreviousSessionData;
import static nl.verheulconsultants.monitorisp.service.Utilities.getTestHomeDir;
import static nl.verheulconsultants.monitorisp.service.Utilities.saveSession;
import static nl.verheulconsultants.monitorisp.service.Utilities.setSessionsDataFileNameForTest;
import static nl.verheulconsultants.monitorisp.ui.HomePage.choicesModel;
import static nl.verheulconsultants.monitorisp.ui.HomePage.selected;
import static nl.verheulconsultants.monitorisp.ui.HomePage.selectedModel;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple test using the WicketTester
 */
public class HomePageTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomePageTest.class);
    private WicketTester tester;

    /**
     * Setup each test with a new instance of the application and set of data consisting of 4 choices all together from which 3 are selected.
     */
    @Before
    public void setUp() {
        tester = new WicketTester(new WicketApplication());

        setSessionsDataFileNameForTest();
        // copy a test file to the test directory (the copy will be overwritten)
        File resourcesDirectory = new File("src/test/resources");
        File source = new File(resourcesDirectory, "MonitorISPData.bin");
        Path sourcePath = source.toPath();
        //copy the test file to the test directory with the same name as the source
        try {
            Files.copy(sourcePath, getTestHomeDir().resolve(source.getName()), REPLACE_EXISTING);
        } catch (IOException ex) {
            LOGGER.error("File copy failed with exception {}", ex);
        }
    }

    @Test
    public void homepageRendersSuccessfully() {
        //start and render the test page
        tester.startPage(HomePage.class);

        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    public void submitPallette() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //submit form with default submitter
        formTester.submit();
    }

    @Test
    public void removeHost() {
        System.out.println("removeHost");
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //add a host and then remove it               
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = choicesModel.getObject();
        hosts.add(newHost);
        selected.clear();
        selected.add(newHost);
        System.out.println("These URL's will be removed " + selected);
        if (hosts.removeAll(selected)) {
            System.out.println("The model is changed to " + selectedModel);
        } else {
            System.out.println("The model is not changed.");
        }
        //submit form using inner component 'button' as alternate button
        formTester.submit();
        assertTrue("The actual number of selected items found in selectedModel is " + selectedModel.getObject().size()
                + " The items are " + selectedModel.getObject(),
                selectedModel.getObject().size() == 3);
    }

    @Test
    public void addHostToChoices() {
        System.out.println("addHostToChoices");
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //add a host to the choices
        Collection<Host> hosts = choicesModel.getObject();
        hosts.add(new Host("4", "google.com"));
        //submit form using inner component 'button' as alternate button
        formTester.submit();
        assertTrue("The actual number of choice items found is " + choicesModel.getObject().size()
                + " The items are " + choicesModel.getObject(),
                choicesModel.getObject().size() == 5);
    }

    @Test
    public void addHostToSelection() {
        System.out.println("addHostToSelection");
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("addHostForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.setValue("newHost", "google.com");
        formTester.submit();
        assertTrue("The actual number of selected items found in choicesModel is " + choicesModel.getObject().size()
                + " The items are " + choicesModel.getObject(),
                choicesModel.getObject().size() == 5);

        assertTrue("The actual number of selected items found in selected is " + selected.size()
                + " The items are " + selected,
                selected.size() == 3);
    }

    @Test
    public void addRouterAddress() {
        System.out.println("addRouterAddress");
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("routerForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.setValue("routerAddress", "192.168.0.6");
        formTester.submit();
        assertEquals("The actual router address found is " + ISPController.getRouterAddress(), "192.168.0.6", ISPController.getRouterAddress());
    }

    @Test
    public void submitStart() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.submit("startButton");
    }

    @Test
    public void submitStop() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.submit("stopButton");
    }

    /**
     * Test of initWithPreviousSessionData method, of class HomePage.
     */
    @Test
    public void testInitWithPreviousSessionData() {
        System.out.println("initWithPreviousSessionData");
        initWithPreviousSessionData();
        assertTrue("The actual number of choice items found is " + choicesModel.getObject().size(), choicesModel.getObject().size() == 4);
        assertTrue("The actual number of selected items found is " + selectedModel.getObject().size(), selectedModel.getObject().size() == 3);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testInitWithDefaults() {
        System.out.println("initWithDefaults");
        HomePage.initWithDefaults();
        assertTrue("The actual number of choice items found is " + choicesModel.getObject().size(), choicesModel.getObject().size() == 4);
        assertTrue("The actual number of selected items found is " + selectedModel.getObject().size(), selectedModel.getObject().size() == 3);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testAddingToSelection() {
        System.out.println("testAddingToSelection");
        HomePage.initWithDefaults();
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = choicesModel.getObject();
        hosts.add(newHost);
        List<Host> selHosts = selectedModel.getObject();
        selHosts.add(newHost);
        assertTrue("The actual number of choice items found is " + choicesModel.getObject().size(), choicesModel.getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + selectedModel.getObject().size(), selectedModel.getObject().size() == 4);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testAddingToSelectionAndSaveSession() {
        System.out.println("testAddingToSelectionAndSaveSession");
        HomePage.initWithDefaults();
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = choicesModel.getObject();
        hosts.add(newHost);
        List<Host> selHosts = selectedModel.getObject();
        selHosts.add(newHost);
        saveSession();
        assertTrue("The actual number of choice items found is " + choicesModel.getObject().size(), choicesModel.getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + selectedModel.getObject().size(), selectedModel.getObject().size() == 4);
    }

}
