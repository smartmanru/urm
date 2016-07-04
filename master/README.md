# URM - Unified Release Management

## Overview

  URM is a system to perform following primary operations:
  - take control over codebase
  - build binaries from source code
  - create distributive from binaries and ready artefacts
  - deploy distributive to environment
  - maintain environment

  URM enables to set up continuous delivery framework, aligned with complex release policies:
  - build/deployment/administration command center
  - enterprise version with multiple products managed, JMX access, Web-access
  - standalone version with powerful command-line interface
  - operating model without need to maintain complex play books
  - Linux/Windows builds and deployments, maven/gradle/dotnet
  - distributive management, populating from builds, version control repositories and manual files
  - full/incremental releases for generic multi-datacenter application sets
  - build and deployment without writing scripts, compact declarative tobe-specifications
  - multi-repository codebase management, svn/git repositories
  - configurations as code
  - enforce engineering product management
  - enforce storing codebase in repositories for buildable, configuration files and database modification files
  - enforce storing all artefacts required for automatic deployment in well-defined distributive packages
  - multi-team environment
  - release planning control
  - messaging integrations
  - urm is 1.7 java application launching parallel local and remote sessions for execution of specific command streams
  
## Installation

  - download all files to temporary folder
  - choose local home directory for your instance
  - run one of
	install.cmd <home directory> [standalone] - standalone version (by default)
	install.sh <home directory> [standalone] - standalone version (by default)
	install.cmd <home directory> server - enterprise version
	install.sh <home directory> server - enterprise version
  - it will create initial strucutre where you can define your products
  - define your products
  - to run in console mode go to bin folder and execute configure (run configure help)
  	run congugure.cmd to create Windows helper scripts in all specified products and environments
  	run congugure.sh to create Linux helper scripts in all specified products and environments
  - see "Using svn..." below to bring URM under version control

## Upgrade

  - download all files to temporary folder on host where home directory for your URM instance resides
  - run one of
	upgrade.cmd <home directory>
	upgrade.sh <home directory>
  - see "Using svn..." below to bring URM under version control

## Using svn
  - when need to redistribute URM instance to other adminstration hosts
  - when need to setup version control of product specifications
  - after installation:
	import everything from URM home directory into svn using svn import
	delete home and checkout from svn
  - after upgrade or reconfiguration:
	it can result in adding or removing helper scripts
	go to bin folder and run svnsave to save changes in svn
  - on Linux to avoid Windows newlines and to set executable flag for .sh files use svnget:
	copy svnget to suitable permanent location, e.g. user home or to any folder in PATH
	after any URM version upgrade or reconfiguration or specification updates in svn run svnget <home path>

## Contacts

  URM is in extensive use for specific national proejcts and is continuously developing.
  URM is regarded as open tool to be able to evolve properly.
  
  - If you need help or find missing feature required for your process, please let us know.
  
  ```
  email: vsavchik@gmail.com
  Skype: vladimir.savchik
  ```
