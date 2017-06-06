# Developers guide

This is a quick and durty guide on how to start coding on RODA.

## Get the source code

You can easily get the source code by cloning the project into your machine (just need git installed):

```bash
$ git clone https://github.com/keeps/roda.git
```

If you plan to contribute to RODA, you will need to first fork the repository into your own GitHub account and then clone it into your machine. To learn how to do it, please check this [GitHub article](https://help.github.com/articles/fork-a-repo).


<!-- WARNING: changing this title will break links -->
## How to build and run

RODA uses [Apache Maven](http://maven.apache.org/) build system. Being a multi-module Maven project, in the root **pom.xml** is declared all the important information to all modules in RODA, such as:

* Modules to be included in the default build cycle
* Maven repositories to be used
* Dependency management (version numbers are declared here and inherited by the sub-modules)
* Plugin management (version numbers are declared here and inherited by the sub-modules)
* Profiles available (There are a lot of usable profiles. One that only includes the core projects (**core**), other that includes user interface projects (**wui**), other that build RODA wui docker image (**wui,roda-wui-docker**), and some other ones that, for example, can include external plugins projects that can be integrated in RODA (**all**)).

### Dependencies

The pre-requisites to build RODA are:

* Git client
* Apache Maven
* Oracle Java 8

To install all dependencies in Debian based systems execute:

```bash
$ sudo add-apt-repository ppa:webupd8team/java
$ sudo apt-get update
$ sudo apt-get install oracle-java8-installer oracle-java8-set-default git maven ant
```

### Compilation

To compile, go to the RODA sources folder and execute the command:

```bash
$ mvn clean package
```

Use the following command to skip the Unit Tests (faster).

```bash
$ mvn clean package -Dmaven.test.skip=true
```


After a successful compile, RODA web application will be available at `roda-ui/roda-wui/target/roda-wui-VERSION.war`. To deploy it, just put it inside your favourite servlet container (e.g. Apache Tomcat) and that is it.

## How to set up the development environment

### Required software

Besides the software needed to build RODA, you need:

* Eclipse for Java ([Download page](http://www.eclipse.org/downloads/))
* Eclipse Maven Plugin ([Download & install instructions](http://www.eclipse.org/m2e/download/))

Optionally you may install the following tools:

* Google Plugin for Eclipse ([Download & install instructions](https://developers.google.com/eclipse/docs/getting_started)) is usefull to develop and test graphical user interface developments.

**NOTE:** This is not a restrictive list of software to be used for developing RODA (as other software, like IDEs, can be used instead of the one suggested.) 

### How to import the code in Eclipse

1. Start Eclipse
2. Select "File > Import". Then, select "Maven > Existing Maven Projects" and click "Next"
3. In the "Root Directory", browse to RODA source code directory on your filesystem and select "Open"
4. Optionally, you can add it to a "Working set"
5. Click "Finish"


## Code structure

RODA is structured as follows:

### /

* **pom.xml** - root Maven Project Object Model
* **code-style** - checkstyle & Eclipse code formatter files
* **roda-common/** - this module contains common components used by other modules/projects
  * **roda-common-data** - this module contains all RODA related model objects used in all other modules/projects
  * **roda-common-utils** - this module contains base utilities to be used by other modules/projects

### /roda-core/

  * **roda-core** - this module contains model, index and storage services, with special attention to the following packages:
    * **common** - this package contains roda-core related utilities
    * **storage** - this package contains both a storage abstraction (inspired on OpenStack Swift) and some implementations (ATM a filesystem & Fedora 4 based implementation)
    * **model** - this package contains all logic around RODA objects (e.g. CRUD operations, etc.), built on top of RODA storage abstraction
    * **index** - this package contains all indexing logic for RODA model objects, working together with RODA model through Observable pattern
    * **migration** - this package contains all migration logic (e.g. every time a change in a model object occurs a migration might be needed)
  * **roda-core-tests** - this module contains tests and tests helpers for roda-core module. Besides that, this module can be added as dependency for other project that have, for example, plugins and ones wants to test them more easily
  * **roda-plugins** - this module contains core plugins sub-divided in sub-modules (characterization, reindex, ingest & others)
    * **base** - this module contains roda core miscellaneous plugins 
    * **antivirus** - this module contains roda core plugins related to antivirus 
    * **characterization** - this module contains roda core description objects manipulation plugins 
    * **ingest** - this module contains roda core ingest task plugins 
    * **reindex** - this module contains index maintenance roda core plugins

### /roda-ui/

* **roda-wui**- this module contains the Web User Interface (WUI) web application and the web-services REST. Basically the components to allow programmatic interaction with RODA.

### /roda-common/

* **roda-common-data** - this module contains all RODA related model objects used in all other modules/projects
* **roda-common-utils** - this module contains base utilities to be used by other modules/projects


## Contribute

### Source code

1. [Fork the RODA GitHub project](https://help.github.com/articles/fork-a-repo)
2. Change the code and push into the forked project
3. [Submit a pull request](https://help.github.com/articles/using-pull-requests)

To increase the changes of you code being accepted and merged into RODA source here's a checklist of things to go over before submitting a contribution. For example:

* Has unit tests (that covers at least 80% of the code)
* Has documentation (at least 80% of public API)
* Agrees to contributor license agreement, certifying that any contributed code is original work and that the copyright is turned over to the project

### Translations

If you would like to translate RODA to a new language please read the [Translation Guide](Translation_Guide.md).

### External plugins

To create new plugins and use them to RODA it is necessary to:

1. Create a new Maven project that depends on roda-core and declare the plugin class qualified name in _pom.xml_ like, for example: [pom.xml](https://github.com/keeps/roda/blob/master/roda-core/roda-plugins/characterization/roda-plugin-characterization-droid/pom.xml)
1. The plugin class must extend **AbstractPlugin** class and implement the necessary methods like, for example: [Droid plugin](https://github.com/keeps/roda/blob/master/roda-core/roda-plugins/characterization/roda-plugin-characterization-droid/src/main/java/org/roda/core/plugins/plugins/characterization/DroidPlugin.java)
1. After creating the plugin, it is necessary to generate a jar file
1. That jar file should then be included under RODA base installation folder specifically in **config/plugins/PLUGIN_NAME/**

## REST API

RODA is completely controlled via a REST API. This is great to develop external services or integrate other applications  with the repository. The documentation of the API is available at [https://demo.roda-community.org/api-docs/](https://demo.roda-community.org/api-docs/).

### Developing 3rd party integrations

If you are interested in developing an integration with RODA via the REST API, please contact the product team for further information by leaving a question or a comment on https://github.com/keeps/roda/issues.

