What is LiveView Navigator?
---------------------------

LiveView Navigator shows direction and distance of a given position in relation to your current position on a Sony Ericsson LiveView device. This way you can keep your phone in your pocked while navigating to a location.

This project is part of the c:geo development and is intended to be used with c:geo, but is open for other apps at the moment, because it has a very generic interface. Later this might be changed and a fork will be continued with special geocaching functions like logging, show description/logs/... and more.

How to use it?
--------------

In your app send the position to LiveView Navigator. Then turn on your LiveView and open the LiveView Navigator plugin. This enables the GPS and shows the position and direction on your LiveView after a fix. Keep in mind, that the direction is used from the GPS, so it works only while in motion.

The GPS will be disabled after a certain time (configurable in the plugin preferences) to save battery in case the LiveView was disconnected from your phone, but the LiveView server app didn't recognize that (what happens quite often). To enable it again simply push the select button on your LiveView.

Development
-----------

Here are some helpful resources for developers:

- SDK: http://developer.sonymobile.com/wportal/devworld/downloads/download/liveviewmicrodisplaysdk
- Tutorial: http://developer.sonymobile.com/wportal/devworld/downloads/download/liveviewmicrodisplaydevelopertutorial
- Quick overview: http://www.stealthcopter.com/blog/2011/04/sony-ericsson-liveview-sdk-and-my-alarm-clock-plugin/