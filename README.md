# PStateModify

Force a specific performance state on an Nvidia GPU to save power.

## Requirements

* Java 21. If your distro doesn't ship it yet, you can download a build [here](https://jdk.java.net/21/).

## Basic Usage

Download the repo and navigate to the directory where you downloaded it. Open a terminal and execute:

`java --source 21 --enable-preview --enable-native-access=ALL-UNNAMED ./PStateModify.java -i <GPU> -s <STATE>`

 ## Options
 
 `-i` - the GPU index
 
 `-s` - the performance state. If not a valid performance state, it will snap to a valid one. **A value of 16 will re-enable dynamic performance states.** Start with a value of 8 and decrease if more performance is desired.
 
 `-l` - library name override for distros that don't name the library correctly or for Windows.