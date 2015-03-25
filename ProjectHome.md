## Description ##
Proof of concept multi-module project for Documentum. This is essentially an experiment in Continuous Integration for Documentum development.

## Project Goal ##
  * Dependency Management
  * Standard project structure and tasks
  * Multi-project inter-dependencies
  * One-click build
  * One-click deployment
  * Continuous Integration

## Overview of Approach ##
  * Ant used to build JAR and WAR artifacts
  * Maven Ant tasks used to resolve dependencies
  * Global targets & macros made available to sub-projects
  * JARS for JarDefs are resolved from Maven repository (no source code in Composer projects)
  * Jenkins CI server used to build DARs using Headless Composer
  * Jenkins CI server used to deploy DARs using Headless Composer
  * Ant used to deploy JAR, WAR and DAR artifacts to Maven repository

I would suggest reading up on [details of the build system](http://code.google.com/p/dctm-multi-module/wiki/HowItWorks) before digging into the [code and configuration files](http://code.google.com/p/dctm-multi-module/source/checkout).