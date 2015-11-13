package nl.verheulconsultants.monitorisp.ui;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage {

    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester(new WicketApplication());
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
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("removeForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.submit("removeButton");
    }
    
    @Test
    public void addHost() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("addForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.submit("new-host");
    }

    @Test
    public void submitStart() {
        //start and render the test page
        tester.startPage(HomePage.class);
        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);
        //create a new form tester without filling its form components with a blank string
        FormTester formTester = tester.newFormTester("startStopForm", false);
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
        FormTester formTester = tester.newFormTester("startStopForm", false);
        //submit form using inner component 'button' as alternate button
        formTester.submit("stopButton");
    }
}
