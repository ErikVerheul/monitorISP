# monitorISP
Monitor the availability of your Internet Service Provider

A WAR to be deployed on an always-on PC or server. Connect to the app with a browser.
This app is developed in Java and uses the Wicket framework.

To enable a smooth shutdown and restart of the service in Tomcat 7 do the following:
In the configuration file context.xml change to:
<!-- Uncomment this to disable session persistence across Tomcat restarts -->
    
    <Manager pathname="" />
