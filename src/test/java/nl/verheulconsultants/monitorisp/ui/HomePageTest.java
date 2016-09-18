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
import static nl.verheulconsultants.monitorisp.service.Utilities.getTestHomeDir;
import static nl.verheulconsultants.monitorisp.service.Utilities.setSessionsDataFileNameForTest;
import static nl.verheulconsultants.monitorisp.ui.WicketApplication.getController;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple test using the WicketTester.
 * 
 * @author Erik Verheul <erik@verheulconsultants.nl>
 */
public class HomePageTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HomePageTest.class);
    private final static ISPController CONTROLLER  = getController();
    private final WicketApplication application = new WicketApplication();
    private final WicketTester tester = new WicketTester(application);
    private final HomePage homePage = new HomePage();
    
    /**
     * Setup each test with a new instance of the application and set of data consisting of 4 choices all together from which 3 are selected.
     */
    @Before
    public void setUp() {
        System.out.println("setUp");
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
        if (CONTROLLER.initWithPreviousSessionData()) {
            LOGGER.info("Preset previous session test data are used for initialization.");
        } else {
            LOGGER.info("Preset previous session test data could not be read, defaults are set");
        }
    }

    /**
     *
     */
    @Test
    public void homepageRendersSuccessfully() {
        System.out.println("homepageRendersSuccessfully");
        //start and render the test page
        tester.startPage(homePage);

        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }

    /**
     *
     */
    @Test
    public void submitPallette() {
        System.out.println("submitPallette");
        //start and render the test page
        tester.startPage(homePage);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //submit form with default submitter
        formTester.submit();
    }

    /**
     *
     */
    @Test
    public void removeHost() {
        System.out.println("removeHost");
        //start and render the test page
        tester.startPage(homePage);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //add a host and then remove it               
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = CONTROLLER.getPaletteModel().getObject();
        hosts.add(newHost);
        CONTROLLER.getSelected().clear();
        CONTROLLER.getSelected().add(newHost);
        System.out.println("These URL's will be removed " + CONTROLLER.getSelected());
        if (!hosts.removeAll(CONTROLLER.getSelected())) {          
            System.out.println("The model could not be changed.");
        }
        //submit form using inner component 'button' as alternate button
        formTester.submit();
        assertTrue("The actual number of choise items found in paletteModel is " + CONTROLLER.getPaletteModel().getObject().size()
                + " The items are " + CONTROLLER.getPaletteModel().getObject(),
                CONTROLLER.getPaletteModel().getObject().size() == 4);
        assertTrue("The actual number of selected items found in selectedModel is " + CONTROLLER.getSelectedModel().getObject().size()
                + " The items are " + CONTROLLER.getSelectedModel().getObject(),
                CONTROLLER.getSelectedModel().getObject().size() == 1);
    }

    /**
     *
     */
    @Test
    public void addHostToChoices() {
        System.out.println("addHostToChoices");
        //start and render the test page
        tester.startPage(homePage);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //add a host to the choices
        Collection<Host> hosts = CONTROLLER.getPaletteModel().getObject();
        hosts.add(new Host("4", "google.com"));
        //submit form using inner component 'button' as alternate button
        formTester.submit();
        assertTrue("The actual number of choice items found is " + CONTROLLER.getPaletteModel().getObject().size()
                + " The items are " + CONTROLLER.getPaletteModel().getObject(),
                CONTROLLER.getPaletteModel().getObject().size() == 5);
    }

    /**
     *
     */
    @Test
    public void addHostToSelection() {
        System.out.println("addHostToSelection");
        //start and render the test page
        tester.startPage(homePage);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("addHostForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.setValue("newHost", "google.com");
        formTester.submit();
        assertTrue("The actual number of selected items found in choicesModel is " + CONTROLLER.getPaletteModel().getObject().size()
                + " The items are " + CONTROLLER.getPaletteModel().getObject(),
                CONTROLLER.getPaletteModel().getObject().size() == 5);

        assertTrue("The actual number of selected items found in selected is " + CONTROLLER.getSelected().size()
                + " The items are " + CONTROLLER.getSelected(),
                CONTROLLER.getSelected().size() == 3);
    }

    /**
     *
     */
    @Test
    public void addRouterAddress() {
        System.out.println("addRouterAddress");
        //start and render the test page
        tester.startPage(homePage);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("routerForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.setValue("routerAddress", "192.168.0.6");
        formTester.submit();
        assertEquals("The actual router address found is " + CONTROLLER.getRouterAddress(), "192.168.0.6", CONTROLLER.getRouterAddress());
    }

    /**
     *
     */
    @Test
    public void submitStart() {
        System.out.println("submitStart");
        //start and render the test page
        tester.startPage(homePage);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("paletteForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.submit("startButton");
    }

    /**
     *
     */
    @Test
    public void submitStop() {
        System.out.println("submitStop");
        //start and render the test page
        tester.startPage(homePage);
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
        CONTROLLER.initWithPreviousSessionData();
        assertTrue("The actual number of choice items found is " + CONTROLLER.getPaletteModel().getObject().size(), CONTROLLER.getPaletteModel().getObject().size() == 4);
        assertTrue("The actual number of selected items found is " + CONTROLLER.getSelectedModel().getObject().size(), CONTROLLER.getSelectedModel().getObject().size() == 3);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testInitWithDefaults() {
        System.out.println("initWithDefaults");
        CONTROLLER.initWithDefaults();
        assertTrue("The actual number of choice items found is " + CONTROLLER.getPaletteModel().getObject().size(), CONTROLLER.getPaletteModel().getObject().size() == 4);
        assertTrue("The actual number of selected items found is " + CONTROLLER.getSelectedModel().getObject().size(), CONTROLLER.getSelectedModel().getObject().size() == 3);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testAddingToSelection() {
        System.out.println("testAddingToSelection");
        CONTROLLER.initWithDefaults();
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = CONTROLLER.getPaletteModel().getObject();
        hosts.add(newHost);
        List<Host> selHosts = CONTROLLER.getSelectedModel().getObject();
        selHosts.add(newHost);
        assertTrue("The actual number of choice items found is " + CONTROLLER.getPaletteModel().getObject().size(), CONTROLLER.getPaletteModel().getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + CONTROLLER.getSelectedModel().getObject().size(), CONTROLLER.getSelectedModel().getObject().size() == 4);
    }

    /**
     * Test of initWithDefaults method, of class HomePage.
     */
    @Test
    public void testAddingToSelectionAndSaveSession() {
        System.out.println("testAddingToSelectionAndSaveSession");
        CONTROLLER.initWithDefaults();
        Host newHost = new Host("4", "google.com");
        Collection<Host> hosts = CONTROLLER.getPaletteModel().getObject();
        hosts.add(newHost);
        List<Host> selHosts = CONTROLLER.getSelectedModel().getObject();
        selHosts.add(newHost);
        CONTROLLER.getSessionData().saveData();
        assertTrue("The actual number of choice items found is " + CONTROLLER.getPaletteModel().getObject().size(), CONTROLLER.getPaletteModel().getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + CONTROLLER.getSelectedModel().getObject().size(), CONTROLLER.getSelectedModel().getObject().size() == 4);
        CONTROLLER.initWithPreviousSessionData();
        assertTrue("The actual number of choice items found is " + CONTROLLER.getPaletteModel().getObject().size(), CONTROLLER.getPaletteModel().getObject().size() == 5);
        assertTrue("The actual number of selected items found is " + CONTROLLER.getSelectedModel().getObject().size(), CONTROLLER.getSelectedModel().getObject().size() == 4);
    }

}
