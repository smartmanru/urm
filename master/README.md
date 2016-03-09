# URM - Unified Release Management

## Overview

  URM is a tool set enabling to set up continuous delivery framework, aligned with complex release policies:
  - Linux-based command center, powerful command-line interface
  - Linux/Windows builds and deployments, maven/gradle/dotnet
  - full/incremental releases for generic multi-datacenter application sets
  - build and deployments without writing scripts, compact declarative tobe-specifications
  - align with specific technologies, not replacing with its own
  - multi-repository codebase management, svn/git repositories
  - multi-datacenter heterogenious environment management, flexible but controlled configurations
  - enforce engineering product management
  - enforce storing codebase in repositories for buildable, configuration files and database modification files
  - enforce storing all artefacts required for automatic deployment in well-defined distributive packages
  - multi-team environment
  - release planning control
  - messaging integrations
  - urm is 1.7 java application launching parallel local and remote ssh-sessions for execution of specific command streams
  
## Installation

  - create home directory
  - export urm/master to home/master
  - create home/etc
  - create in home/etc configuration files product.conf, distr.xml, source.xml using sample files in urm/master/samples
  - make sure all .sh files are executable and have Linix linefeeds:
  
  ```
  chmod 744 `find . -name "*.sh`
  sed -i "s/\r//" `find . -name "*.sh`
  ```

## Contacts

  URM is in extensive use for specific national proejcts and is continuously developing.
  URM is regarded as open tool to be able to evolve properly.
  
  - If you need help or find missing feature required for your process, please let us know.
  
  ```
  email: vsavchik@gmail.com
  Skype: vladimir.savchik
  ```
