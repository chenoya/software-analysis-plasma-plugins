# Software analysis Plasma plugins
This repository contains three plugins allowing performing properties verification on an executable software.
The plugins integrate with [Plasma](https://project.inria.fr/plasma-lab/) (version 1.4.4), a statistical model checking platform.
The tool is based on the use of debuggers like [GDB](https://www.gnu.org/software/gdb/) (version 7+) to perform the simulation of the model 
and retrieve data which is then used to check the desired properties.

## Build and installation
First, you will need to clone this repository and download [Plasma 1.4.4](https://project.inria.fr/plasma-lab/download/).
The expected directory structure is the following :
```
.
├─ software-analysis-plasma-plugins/
├─ plasmalab-1.4.4/
```
If the structure is different, you can edit the path's configuration in the `properties` fields in the `pom.xml` files. 

To build the plugins and install them inside Plasma, simply run the following [Maven](https://maven.apache.org/install.html) command in 
the `software-analysis-plasma-plugins/` directory :
```
mvn package
```
This will compile everything, build a JAR file and install it inside Plasma.

You can also download the JAR file [directly](https://github.com/chenoya/software-analysis-plasma-plugins/releases/) and save it to `plasmalab-1.4.4/plugins/`.

To start using Plasma and the plugins, run this command in the `plasmalab-1.4.4/` folder :
```
java -jar libs/fr.inria.plasmalab.uiterminal-1.4.4.jar launch
```

You can now import an example or create you own configuration.

## Plugin 1 : Software Simulator
This plugin uses GDB to simulate an executable and retrieve values. 
It allows to create a new model with some parameters.
To create one in Plasma GUI, go to `File > New > Content` and select `Software simulator`.
The content created respect the TOML syntax and defines several fields.
```
executable
```
is the path to the executable we want to simulate
```
function
```
is the function in this executable we want to monitor
```
simulator.name
```
is the debugger used, only `gdb` is available at this time
```
simulator.options
```
are options specific to the debugger used. 
These options can be enabled. In the case of GDB, the options are `CF` to enable the monitoring of the Carry Flag, 
`OF` to enable the monitoring of the Overflow Flag,
`STACK_M` to enable the monitoring of the stack,
(it evaluates to true if the stack above the one of this function has been edited since the last operation; useful for detecting buffer-overflow),
and `gdb_path` to set the path of the GDB debugger.
```
variables
```
allows defining named expressions that will be evaluated during the simulation and accessible by their name in the properties.
These expressions need to fit in a Java double value to be usable from the Plasma interface.  
A complete example : 
```toml
executable = "../software-analysis-plasma-plugins/examples/buffer_overflow/buffer_overflow"
function = "main"

[simulator]
name = "gdb"

[simulator.options]
CF = true
OF = true
STACK_M = true
gdb_path = "/usr/bin/gdb"

[variables]
x = "x"
r = "r"
b0 = "(int) buf[0]"
b12 = "(int) buf[12]"
```
## Plugin 2 : Software BLTL Checker

This second plugin extends the existing BLTL plugin allowing verifying Bounded Linear Time Logic properties. 
It adds two  features : 
1. Print or export the execution traces which caused a property violation,
2. Insert expression evaluated by the debugger directly into the BLTL syntax.

A file defining a requirement is also mainly using TOML and looks like
```toml
[traces]
type="one per file"
folder = "abc/"
prefix = "req_01"

[BLTL]
G <= #100 $x!=100$
``` 
The `traces.type` can be 
* `'none'` : do not nothing with the trace when a property fails,
* `'print'` : print them to the standard output,
* `'one per file'` : output one faulty trace per file called `prefix + a number` in the `folder`,
* `'all in one file'` : output all faulty traces in a file called `prefix` in `folder`.

The BLTL property comes after the BLTL tag. It follows the [syntax from the existing plugin](http://plasma-lab.gforge.inria.fr/plasma_lab_doc/1.4.4/html/languages/bltl.html#b-ltl-grammar)
and adds the possiblilty to insert boolean expression inside the property between two `$` (A `$` in the expression can be inserted by writing `$$`).
```
Formula ::= '$' Debugger_expression '$'
```
In the example above, GDB will evaluate the boolean expression `x!=100` and the plugin will validate the property depending on the result.

## Plugin 3 : SWIFI Generator
The last plugin is a new algorithm which performs verification on a model where the executable has been compromised by some fault injections. 
The faults are inserted by the [SWIFI tool](https://github.com/chenoya/swifi-tool). 
The user can set the maximum number of simulations and some other parameter forwarded to the tool.
The number of each type of fault models has to be defined. For example a 2 in the NOP field means the algorithm will NOP two places in the executable, testing every combination (which can be numberous).