# GDB Plasma plugin
Plugin allowing to simulate executable binary code using GDB and retrieve values for verification inside Plasma.
## Build and installation
To build the plugin and install it inside Plasma, simply run  
`mvn compile package`  
This will compile the Antlr4 syntax, compile the Java code, build a JAR file and install it inside Plasma.  
Some configuration might be required and can be done in the `properties` fields in the `pom.xml` files. For example, you may need to edit the Plasma installation path and the related libraries.


