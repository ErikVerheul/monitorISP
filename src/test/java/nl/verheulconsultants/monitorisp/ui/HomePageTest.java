package nl.verheulconsultants.monitorisp.ui;

import nl.verheulconsultants.monitorisp.service.Host;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.Collection;
import java.util.List;
import nl.verheulconsultants.monitorisp.service.ISPController;
import static nl.verheulconsultants.monitorisp.service.ISPController.getPaletteModel;
import static nl.verheulconsultants.monitorisp.service.ISPController.getSelected;
import static nl.verheulconsultants.monitorisp.service.ISPController.getSelectedModel;
import static nl.verheulconsultants.monitorisp.service.ISPController.initWithDefaults;
import static nl.verheulconsultants.monitorisp.service.ISPController.initWithPreviousSessionData;
import static nl.verheulconsultants.monitorisp.service.Utilities.getTestHomeDir;
import static nl.verheulconsultants.monitorisp.service.Utilities.saveSession;
import static nl.verheulconsultants.monitorisp.service.Utilities.setSessionsDataFileNameForTest;
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
        System.out.println("setUp");
        tester = new WicketTester(new WicketApplication());
        LOGGER.info("New WicketTester instance instantiated");
        setSessionsDataFileNameForTest();
        // copy a test file to the test directory (will be overwritten)
        File resourcesDirectory = new File("src/test/resources");
        File source = new File(resourcesDirectory, "MonitorISPData.bin");
        Path sourcePath = source.toPath();
        //copy the test file to the test directory with the same name as the source
        try {
            Files.copy(sourcePath, getTestHomeDir().resolve(source.getName()), REPLACE_EXISTING);
            LOGGER.info("Fresh last session data copied");
        } catch (IOException ex) {
            LOGGER.error("File copy failed with exception {}", ex);
        }
        // Must load the session data explicit as WicketTester is not doing it.
        if (initWithPreviousSessionData()) {
            LOGGER.info("Preset previous session test data are used for initialization.");
        } else {
            LOGGER.info("Preset previous session test data could not be read, defaults are set");
        }
    }

    @Test
    public void homepageRendersSuccessfully() {
        System.out.println("homepageRendersSuccessfully");
        //start and render the test page
        tester.startPage(HomePage.class);

        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }

    @Test
    public void submitPallette() {
        System.out.println("submitPallette");
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
        Collection<Host> hosts = getPaletteModel().getObject();
        hosts.add(newHost);
        getSelected().clear();
        getSelected().add(newHost);
        System.out.println("These URL's will be removed " + getSelected());
        if (hosts.removeAll(getSelected())) {
            System.out.println("The model is changed to " + getSelectedModel());
        } else {
            System.out.println("The model is not changed.");
        }
        //submit form using inner component 'button' as alternate button
        formTester.submit();
        assertTrue("The actual number of selected items found in selectedModel is " + getSelectedModel().getObject().size()
                + " The items are " + getSelectedModel().getObject(),
                getSelectedModel().getObject().size() == 3);
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
        Collection<Host> hosts = getPaletteModel().getObject();
        hosts.add(new Host("4", "google.com"));
        //submit form using inner component 'button' as alternate button
        formTester.submit();
        assertTrue("The actual number of choice items found is " + getPaletteModel().getObject().size()
                + " The items are " + getPaletteModel().getObject(),
                getPaletteModel().getObject().size() == 5);
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
        assertTrue("The actual number of selected items found in choicesModel is " + getPaletteModel().getObject().size()
                + " The items are " + getPaletteModel().getObject(),
                getPaletteModel().getObject().size() == 5);

        assertTrue("The actual number of selected items found in selected is " + getSelected().size()
                + " The items are " + getSelected(),
                getSelected().size() == 3);
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
        System.out.println("submitStart");
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
        System.out.println("submitStop");
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
        assertTrue("The actual number of choice items found is " + getPaletteModel().getObject().size(), getPaletteModel().getObject().size() == 4);
        assertTrue("The actual number of selected items found is " + getSelectedModel().getObject().size(), getSelectedModel().getObject().size() == 3);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testInitWithDefaults() {
        System.out.println("initWithDefaults");
        initWithDefaults();
        assertTrue("The actual number of choice items found is " + getPaletteModel().getObject().size(), getPaletteModel().getObject().size() == 4);
        assertTrue("The actual number of selected items found is " + getSelectedModel().getObject().size(), getSelectedModel().getObject().size() == 3);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testAddingToSelection() {
        System.out.println("testAddingToSelection");
        initWithDefaults();
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = getPaletteModel().getObject();
        hosts.add(newHost);
        List<Host> selHosts = getSelectedModel().getObject();
        selHosts.add(newHost);
        assertTrue("The actual number of choice items found is " + getPaletteModel().getObject().size(), getPaletteModel().getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + getSelectedModel().getObject().size(), getSelectedModel().getObject().size() == 4);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testAddingToSelectionAndSaveSession() {
        System.out.println("testAddingToSelectionAndSaveSession");
        initWithDefaults();
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = getPaletteModel().getObject();
        hosts.add(newHost);
        List<Host> selHosts = getSelectedModel().getObject();
        selHosts.add(newHost);
        saveSession();
        assertTrue("The actual number of choice items found is " + getPaletteModel().getObject().size(), getPaletteModel().getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + getSelectedModel().getObject().size(), getSelectedModel().getObject().size() == 4);
        initWithPreviousSessionData();
        assertTrue("The actual number of choice items found is " + getPaletteModel().getObject().size(), getPaletteModel().getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + getSelectedModel().getObject().size(), getSelectedModel().getObject().size() == 4);
    }

}
