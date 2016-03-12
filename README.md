# monitorISP
Monitor the availability of your Internet Service Provider

A WAR to be deployed on an always-on PC or server. Connect to the app with a browser.
This app is developed in Java and uses the Wicket framework.

Version 0.4 has all basic functions and should be stable now. No effort was taken yet to make the app look better.
Version 0.5 is refactored so that it automatically restarts after a server shutdown.
To enable a smooth shutdown and restart of the service in Tomcat 7 do the following:
In the configuration file context.xml change to:
<!-- 2015-12-09 Done by Erik: Uncomment this to disable session persistence across Tomcat restarts -->
    
    <Manager pathname="" />
