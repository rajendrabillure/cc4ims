# A Portable Call Center for IMS #


## Description ##
cc4ims is an IMS compatible Call Center that can be installed on Android OS and provide a portable Call Center


## Instructions ##

Before compiling you need to set 5 variables in the SignUpActivity.java file.
Those variables are:
  * PUBLIC\_IDENTITY
  * PRIVATE\_IDENTITY
  * PASSWORD
  * REALM
  * PROXY\_HOST
  * PROXY\_PORT


Also, you need an IMS server to connect. We are using [OpenIMSCore](http://www.openimscore.org/).

!!!For subscribe into presence server, you need a presence server with compatible presence information!!!



## Information ##

Developed by [Media Net Laboratory](http://www.medianetlab.gr/) at [Institute of Informatics and Telecommunications, NCSR Demokritos](http://www.iit.demokritos.gr/), in the frame of [GERYON FP7 SEC EU project](http://www.sec-geryon.eu/)

## Supported Functions ##

  * Call Management
  * Text Messaging
  * Presence information with map display.


## Using: ##

  * [Doubango Framework](http://www.doubango.org/)

  * [android-ngn-stack from imsdroid](https://code.google.com/p/imsdroid/)