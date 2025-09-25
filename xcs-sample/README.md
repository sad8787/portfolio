# XCS Sample

This repository contains the code base of a sample project that will be used in
the XCS subject inside the DGSS itinerary.

## 1. Deployment Environment
The environment is based on Java 8, Maven 3.3+, Git 1.9+, MySQL 8+, WildFly 10.1.0 and Eclipse IDE for Enterprise Java and Web Developers.

### 1.1. Java JDK 8
Download and install Java JDK 8 (the commands `java` and `javac` must be available).

You can test your Java installation with the commands:

```bash
java -version
javac -version
```

### 1.2. Maven 3.3+
Install Maven 3 in your system, if it was not installed (the `mvn` command must be available). If you are in a Debian-based OS, install the `maven` package (**don't install `maven2` package!!**):

```
sudo apt-get install maven
```

You can test your Maven installation with the command:

```bash
mvn --version
```

### 1.3. Git 1.9+
Firstly, install Git in your system if it was not installed (the `git` command must be available). We will work with Git to get updates of these sample. Concretely, we will work with a Git repository inside [our Gitlab server](https://www.sing-group.org/dt/gitlab).

You can tests your Git installation with the command:

```bash
git --version
```

Once Git is installed in your system, clone the project:

```bash
git clone http://sing-group.org/dt/gitlab/dgss-2425/xcs-sample.git
```

### 1.4. MySQL 8+
In order to run the tests with the `wildfly-embedded-mysql` profile (more about this in the **Sample 2** section) and to run the application, we need a MySQL server.

The server can be installed as usual, but it must contain two databases:
  * The `xcs` database for running the application.
  * The `xcssampletest` database for testing the appliaction.

In both cases, the user `xcs` identified by `xcs` password should have all privileges on this database. You can do this by executing the following commands:

```sql
CREATE DATABASE xcs;
CREATE DATABASE xcssampletest;

CREATE USER xcs@'%' IDENTIFIED BY 'xcs';

GRANT ALL PRIVILEGES ON xcs.* TO xcs@'%';
GRANT ALL PRIVILEGES ON xcssampletest.* TO xcs@'%';
FLUSH PRIVILEGES;
```

If you want to add some data to the `xcs` database to run the application (data will be automatically inserted to the `xcssampletest` database during the tests) you can also execute:

```sql
DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
  `role` varchar(5) NOT NULL,
  `login` varchar(100) NOT NULL,
  `password` varchar(32) NOT NULL,
  PRIMARY KEY (`login`)
);

DROP TABLE IF EXISTS `Pet`;
CREATE TABLE `Pet` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `animal` varchar(4) NOT NULL,
  `birth` datetime NOT NULL,
  `name` varchar(100) NOT NULL,
  `owner` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_6mfctqh1tpytabbk1u4bk1pym` (`owner`),
  CONSTRAINT `FK_6mfctqh1tpytabbk1u4bk1pym` FOREIGN KEY (`owner`) REFERENCES `User` (`login`)
);

-- All the passwords are "<login>pass".
INSERT INTO `User`
VALUES ('ADMIN','jose','A3F6F4B40B24E2FD61F08923ED452F34'),
       ('OWNER','pepe','B43B4D046860B2BD945BCA2597BF9F07'),
       ('OWNER','juan','B4FBB95580592697DC71488A1F19277E'),
       ('OWNER','ana','22BEEAE33E9B2657F9610621502CD7A4'),
       ('OWNER','lorena','05009E420932C21E5A68F5EF1AADD530');

INSERT INTO `Pet` (animal, birth, name, owner)
VALUES ('CAT','2000-01-01 01:01:01','Pepecat','pepe'),
       ('CAT','2000-01-01 01:01:01','Max','juan'),
       ('DOG','2000-01-01 01:01:01','Juandog','juan'),
       ('CAT','2000-01-01 01:01:01','Anacat','ana'),
       ('DOG','2000-01-01 01:01:01','Max','ana'),
       ('BIRD','2000-01-01 01:01:01','Anabird','ana');
```

You can find the `xcs-sample-mysql.sql` and `xcs-sample-test-mysql.sql` scripts with these queries stored in the `additional-material/db` project folder.

### 1.5. Eclipse for Java EE
Open Eclipse for Java EE and import your Maven project with `File -> Import -> Maven -> Existing Maven Projects`. In the dialog opened you have to select as `Root directory` the directory of the project that you have just cloned (it should contain a `pom.xml` file).

Eclipse should then import a parent project (`xcs-sample`) and 6 child projects (`tests`, `domain`, `service`, `rest`, `jsf` and `ear`). If any project is disabled for import it is probably because you already have another project in your workspace with the same name. To avoid this problem you can open the advanced settings of the dialog and use a custom `Name template` (for example, `xcs-sample-[artifactId]`).

If you want, you can use any other IDE, such as IntelliJ IDEA or NetBeans, as long as they are compatible with Maven projects, but we recommend using Eclipse for Java EE.

### 1.6 WildFly 10.1.0
If you want to run the project you need a Java EE server. In this section you can find how to configure a local WildFly server to execute the project. Basically, we need to configure the WildFly server to include the datasource and the security configuration needed by the application.

In the following sections you can find an explanation of how you can configure the WildFly server by editing the `standalone.xml`. However, the `additional-material/wildfly` directory of the project already includes a `standalone.xml` ready to be used with the 10.1.0 version, that you can just copy directly into your WildFly server (replacing the original
  `standalone/configuration/standalone.xml` file).

#### 1.6.1. Datasource configuration
There are several ways to add a datasource to a WildFly server. We are going to add it by editing the `standalone/configuration/standalone.xml` configuration file of the server. To do so, you have to edit this file and add the following content to the `<datasources>` element:

```xml
<datasource jta="true" jndi-name="java:jboss/datasources/xcs" pool-name="xcs-mysql" enabled="true" use-ccm="true">
    <connection-url>jdbc:mysql://localhost:3306/xcs?useJDBCCompliantTimezoneShift=true&amp;useLegacyDatetimeCode=false&amp;serverTimezone=Europe%2FMadrid</connection-url>
    <driver>mysql-connector-j-8.1.0.jar</driver>
    <security>
        <user-name>xcs</user-name>
        <password>xcs</password>
    </security>
</datasource>
```

In addition, you also have to add the MySQL driver to the deployments folder (`standalone/deployments`). You can download it form [here] (https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.1.0/mysql-connector-j-8.1.0.jar) to the deployments (`standalone/deployments`) directory and WildFly will
automatically deploy it on startup.

#### 1.6.2. Security configuration
All the WildFly security configuration is done in the `standalone/configuration/standalone.xml` file of the server.

Inside the `<security-reamls>` element you have to add a new security realm:

```xml
<security-realm name="RemotingRealm">
  <authentication>
    <jaas name="AppRealmLoopThrough"/>
  </authentication>
</security-realm>
```

Then, inside the `<security-domains>` element you have to add the following security domains:

```xml
<security-domain name="AppRealmLoopThrough" cache-type="default">
  <authentication>
    <login-module code="Client" flag="required">
      <module-option name="multi-threaded" value="true"/>
    </login-module>
  </authentication>
</security-domain>
<security-domain name="xcs-sample-security-domain">
  <authentication>
    <login-module code="Database" flag="required">
      <module-option name="dsJndiName" value="java:jboss/datasources/xcs"/>
      <module-option name="principalsQuery" value="SELECT password FROM User WHERE login=?"/>
      <module-option name="rolesQuery" value="SELECT role, 'Roles' FROM User WHERE login=?"/>
      <module-option name="hashAlgorithm" value="MD5"/>
      <module-option name="hashEncoding" value="hex"/>
      <module-option name="ignorePasswordCase" value="true"/>
    </login-module>
  </authentication>
</security-domain>
```

#### 1.6.3. Deploying the application manually
When the `package` goal is run in the `xcs-sample` project, an EAR file is generated inside the `target` folder of the `ear` project.

The EAR file contains all the elements of the project (JARs and WARs) and, therefore, you only have to deploy this file in the WildFly container to deploy the entire application. To do so, you can copy this file to the `standalone/deployments` folder of WidlFly.

Once this is done, you can run the WildFly server executing the `bin/standalone.sh` script. The application should be running in http://localhost:8080/xcs-sample. If you want to access through the web interface, you can open the http://localhost:8080/xcs-sample/jsf URL.

#### 1.6.4 Deploying the application from Maven
Maven configuration is prepared to deploy the generated EAR file to a running WildFly. Doing so is as easy as launching the `wildfly:deploy` goal:

```bash
mvn wildfly:deploy
```

This will launch the construction of the project and, at the end, the EAR will be deployed. Remember that, if you want a fast deployment, you can avoid the test execution with the parameter `-DskipTests=true`.

#### 1.7. Running the application from Maven (Recommended)
This project includes the Maven WildFly plugin, which allows the execution of the application without needing an external WildFly server. To run the application with the running MySQL database (`xcs`) you just have execute the following command:

```bash
mvn install -P wildfly-mysql-run,-wildfly-embedded-h2
```
This will launch the complete construction cycle without running the tests, start a WildFly server and deploy the application. Once the application is running you can access it in the URL http://localhost:8080/xcs-sample/jsf.

**Important**: You shouldn't have a local WildFly instance running or Maven will not be able to start its own WildFly and will try to deploy the application in the running instance. This will cause changes to the WildFly configuration that may leave it in an unstable state.

To stop the WildFly launched you can execute the following command:

```bash
mvn wildfly:shutdown
```

## 2. Samples
## 2.1. Sample 1: Testing entities
Using JUnit and Hamcrest, we will see how to test JPA entities or any other Java class. This libraries are the base for every test done in the application.

## 2.2. Sample 2: Testing EJBs
Using Arquillian and Arquillian Persistence, the EJBs are tested. We wouldn't do unit testing in this layer, as we don't want to mock the `EntityManager`.

In this layer we will use some workarounds to set the desired role and principal in the tests.

### 2.2.1. How to run tests with Arquillian?
This project is configured to use two Maven profiles:
* `wildfly-embedded-h2`: this profile uses WildFly in embedded mode with a H2 database, whose driver is included by default in WilFly.
* `wildfly-embedded-mysql`: same as before, but it uses a MySQL database.

In both profiles, the WildFly server is automatically downloaded using the `maven-dependency-plugin`, that extracts it in the `target/wildfly-<version>` folder (`target/wildfly-10.1.0.Final` currently). In the MySQL profile, the MySQL driver is also downloaded using this plugin and added to the `target/wildfly-<version>/standalone/deployments` folder, to make it available in the WildFly server.

For each profile, Maven is configured to use the files stored in `src/test/resources-<profile name>` as resources when running tests, in addition to the stored in the `src/test/resources` folder, as usual. Inside this folder, the projects using Arquillian must include a `standalone.xml` file, that will be replace the default `standalone.xml` file of the WildFly server. This is specially useful to configure the security constraints and datasources.

In order to avoid port collising with other WildFly instances, the WildFly used by the test have a port offset of 10000. This means that the HTTP port is displaced from the default 8080 port to the 18080 port, and the management port is displaced from the default 9990 port to the 19990 port.

Therefore, when running Maven tests (e.g. `mvn test`), they will run without any external requirement.

#### 2.2.1.1 Arquillian tests in Eclipse
To run Arquillian tests in Eclipse (or in any non-Maven enviroment) a further step is needed. You must configure the following system properties:
* `arquillian.launch`: the launch configuration that arquillian should use.
* `wildfly.version`: the version of the WildFly server stored in `target`. The current version is `10.1.0.Final`.
* `wildfly.jbossHome`: the location of the WildFly server.
* `wildfly.modulePath`: the location of the module of the WildFly server.
* `java.util.logging.manager`: the logger to be used by the standard Java logger. Commonly, the value `org.jboss.logmanager.LogManager` is used.
* `jboss.socket.binding.port-offset`: this is an optional parameter that can be used to move the WildFly default ports.
* `wildfly.http.port`: HTTP of the WildFly server.
* `wildfly.management.port`: management port of the WildFly server.

In Eclipse, this system properties can be added to the run configuration in the `VM arguments` field of the `Arguments` tab. For example, the following configuration will work for the current configuration:

```
-Darquillian.launch=wildfly-embedded
-Dwildfly.version=10.1.0.Final
-Dwildfly.jbossHome=target/wildfly-10.1.0.Final
-Dwildfly.modulePath=target/wildfly-10.1.0.Final/modules
-Djava.util.logging.manager=org.jboss.logmanager.LogManager
-Djboss.socket.binding.port-offset=10000
-Dwildfly.http.port=18080
-Dwildfly.management.port=19990
```

This configuration will run with the **H2** database. If you wish to run the tests with the **MySQL** database, you have to add to additional system configuration:
* `mysql.version`: The version of the MySQL driver (currently, `8.1.0`)

Therefore, the `VM arguments` configuration for running the tests in Eclipse using the MySQL database is:

```
-Darquillian.launch=wildfly-embedded
-Dmysql.version=8.1.0
-Dwildfly.version=10.1.0.Final
-Dwildfly.jbossHome=target/wildfly-10.1.0.Final
-Dwildfly.modulePath=target/wildfly-10.1.0.Final/modules
-Djava.util.logging.manager=org.jboss.logmanager.LogManager
-Djboss.socket.binding.port-offset=10000
-Dwildfly.http.port=18080
-Dwildfly.management.port=19990
```

## 2.3. Sample 3: Testing with test doubles
Using EasyMock, we will mock the EJBs to test the REST classes isolated from the underlying layer.

## 2.4. Sample 4: Testing JAX-RS
Using Arquillian REST Client, we will test the REST API accessing it as real HTTP clients.

Tests can be run using the same configuration as explained in *Sample 2*.

When executed, the REST resources can be found in:
* Owners: http://localhost:8080/xcs-sample/rest/api/owner
* Pets: http://localhost:8080/xcs-sample/rest/api/pet

## 2.5. Sample 5: Testing JSF
Using Arquillian Drone, Arquillian Graphene and Selenium, we will test the JSF web interface accessing it as real Web clients.

Tests can be run using the same configuration as explained in *Sample 2*.

When executed, the REST resources can be found in http://localhost:8080/xcs-sample/jsf/faces/index.html.

<span style="color:red">Warning: </span>JSF test have been disabled due to a  [known connection issue](https://firefox-source-docs.mozilla.org/testing/geckodriver/Usage.html#Running-Firefox-in-an-container-based-package) between the Firefox driver and the Firefox installed by default in Ubuntu
22.04.

## 2.6. Sample 6: Additional Testing Tools
### Test coverage with JaCoCo
Test coverage is a very useful tool that shows the parts of the source code that are covered by the tests. The coverage analysis is done during the tests execution, making it very precise.

The JaCoCo plugin is now part or the project, analyzing the test execution. This plugin generates a HTML report in the `target/site/jacoco` folder. This report is very useful to check if some part of the code is missing some tests.
