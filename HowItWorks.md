# Introduction #

In an ideal world Documentum development would follow the same pattern as mainstream Enterprise Java development practices where tools such as Maven have become popular in standardising and (arguably) simplifying build and deployment processes. [Continuous Integration](http://martinfowler.com/articles/continuousIntegration.html) has also helped in fostering good build practices.

..but Documentum development has always been different ;-/

This project is an experiment in making Documentum development a bit more mainstream.

# Tools for the Job #

## Source Code Repository Server ##
  * [Subversion Server](http://subversion.apache.org)

## Nexus Maven Repository Server ##
  * [Java](http://www.java.com)
  * [Apache Tomcat](http://tomcat.apache.org)
  * [Apache Maven](http://maven.apache.org)

## Jenkins Build Server ##
  * [Java](http://www.java.com)
  * [Apache Tomcat](http://tomcat.apache.org)
  * [Apache Maven](http://maven.apache.org)
  * [Apache Ant](http://ant.apache.org)
  * [Subversion Client](http://subversion.apache.org)
  * [Nexus Maven Repository Manager](http://nexus.sonatype.org)
  * [Documentum Headless Composer](http://www.documentum.com)

## Developer Workstation ##
  * [Java](http://www.java.com)
  * [Apache Ant](http://ant.apache.org)
  * [Apache Maven](http://maven.apache.org)
  * [Subversion Client](http://subversion.apache.org)
  * [Documentum Composer](http://www.documentum.com)

Please note that  [Maven Ant Tasks](http://maven.apache.org/ant-tasks) are downloaded on demand by the Ant build scripts.

As Nexus and Jenkins are both Java web applications they can be hosted on the same Tomcat instance.

Please refer to the relevant websites for installation details.

# Approach to Development #
Using the setup described in this project it should be possible for developers to work in the following way:

  * checkout source code from source code repository (e.g. Subversion)
  * run `mvn eclipse:eclipse` or `mvn idea:idea` to create IDE project files
  * import projects in IDE
  * use IDE support for Ant to compile, run JUnit tests, etc
  * commit source code to source code repository
  * build server triggers build job from code commit
  * build job compiles code, runs JUnit tests, creates JAR, WAR and DAR artifacts
  * build job deploys JAR, WAR and DAR artifacts to Maven repository
  * build job optionally deploys DAR artifacts to Documentum repository
  * post-build Jenkins task deploys WAR to application server

# Build System #
<p>
I tried, and failed, to use a 100% Maven-based approach but the main problem was that Maven only supports the generation of multiple artifacts (e.g. BOF JARs) if you set up 1 POM per artifact. This basically means that you have to set up a shedload of sub-projects if your Documentum solution uses a bunch of JarDefs (and most projects do). This is impractical. The things that I really like about Maven though were dependency management, standardisation of project structure and standardisation of project goals (or targets in Ant lingo).<br>
</p>
<p>
I like Ant. It's simple and flexible. It can be used for multi-project development with a bit of care but there's no OOTB support for dependency management and there's no standard project structure. But we can add dependency management through the use of Maven Ant tasks and standard project structure is really just a matter of following a convention as you'll see below.<br>
</p>

## Maven Dependencies ##
<p>
Unfortunately Documentum artifacts such as the DFS and DFC are not available in public Maven repositories. This isn't ideal but we can deploy Documentum artifacts to our Nexus server. There are a couple of ways that we can do this. We can use the Nexus UI to upload artifacts. We can also use Maven's command line utility (e.g. <code>mvn deploy:deploy-file</code>). There are quite a few JARs in the DFS so this is a little time consuming. It's probably more sensible to script this.<br>
</p>
<p>
There's a useful <a href='http://code.google.com/p/dctm-commons/source/browse/trunk/scripts/dependencies/dfs-sdk/prep_publish_dfs.groovy'>script</a> that illustrates how to scan the DFS distribution and construct a shell script that deploys the JARs using invocations of the aforementioned Maven command line utility. It's written in <a href='http://groovy.codehaus.org'>Groovy</a>. If Groovy isn't your thing then don't worry. The script is easy to read and it should be straightforward to port it to whatever language you prefer.<br>
</p>
<p>
If you decide to deploy the artifacts via script then you'll most likely run into an authentication error. You can get around this by specifying credentials in your Maven settings file at ~/.m2/settings.xml. the following snippet defines credentials for two of the default repositories that exist in a standard Nexus installation:<br>
</p>
```
<settings>
	<servers>
		<server>
			<id>releases</id>
			<username>admin</username>
			<password>admin123</password>
		</server>
		<server>
			<id>snapshots</id>
			<username>admin</username>
			<password>admin123</password>
		</server>
	</servers>
</settings>
```
## Project Structure ##
<p>
The project structure is pretty straightforward. Projects will exist under one of the top level folders:<br>
</p>
  * **java-projects**: contains sub-projects that generate JARs
  * **composer-projects**: contains sub-projects that generate DARs
  * **web-projects**: contains sub-projects that generate WARs
<img src='http://dctm-multi-module.googlecode.com/svn/trunk/wiki-images/project-structure.png' />
<p>
The following files exist in the project root:<br>
</p>
  * **build.xml**: master build script used to build all sub-projects
  * **common.properties**: contains properties that are referenced by shared macros and targets
  * **common.xml**: contains shared targets and macros that are used by sub-projects
  * **composer.xml**: contains targets used to build DARs (is referenced by the Jenkins build script)
  * **jenkins-composer-build-script-example.txt**: Example DAR build/deploy script for Jenkins
  * **README.txt**: Read-me file

### java-projects ###
<p>
In the below screenshot you can see that we have a single Java project named bof-java. The project structure is based on Maven's standard project structure. All source files exist under <code>src</code>. Artifacts such as JARs, JUnit reports and Javadocs are generated under <code>target</code>.<br>
</p>
<img src='http://dctm-multi-module.googlecode.com/svn/trunk/wiki-images/java-projects.png' />
<p>
The bof-java project contains the following files and folders:<br>
</p>
  * **build.xml**: This is the project build script
  * **build.properties**: The contains project-specific properties that are referenced by build.xml
  * **pom.xml**: This is a Maven configuration file that defines dependencies that are needed to compile the JARs
<p>
In the pom.xml we declare the Maven repository that we solve JAR dependencies from. In the following snippet you can see that we're referencing a host called buildserver:<br>
</p>
```
    <repositories>
        <repository>
            <id>snapshots</id>
            <name>snapshots</name>
            <url>http://buildserver:8080/nexus/content/repositories/snapshots</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>daily</updatePolicy>
            </snapshots>
        </repository>
        <repository>
            <id>releases</id>
            <name>releases</name>
            <url>http://buildserver:8080/nexus/content/repositories/releases</url>
            <releases>
                <enabled>true</enabled>
                <updatePolicy>daily</updatePolicy>
            </releases>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
```
<p>
We also declare the JAR dependencies here. In the following snippet you can see that we're referencing the DFS SDK:<br>
</p>
```
	<dependencies>
	...
        <dependency>
            <groupId>emc.dfs.sdk</groupId>
            <artifactId>emc.dfs.sdk</artifactId>
            <version>6.7.0</version>
            <type>pom</type>
        </dependency>
	...
	</dependencies>
```
<p>
We can create IDE project files for Eclipse using the command <code>mvn eclipse:eclipse</code>. If you prefer Intelli-J IDEA use <code>mvn idea:idea</code>. You can now import the project into the IDE and use the built-in Ant support to build the project. You will find the following Ant targets by running <code>ant -p</code>:<br>
</p>
  * **clean**: Cleans any generated artifacts
  * **compile**: Compiles the module java source
  * **deploy**: Deploys the JARs to the remote Maven repository
  * **install**: Installs the JARs to the local Maven repository
  * **javadoc**: Generates Javadoc documentation
  * **package**: Creates the JARs
  * **test**: Runs the JUnit tests
<p>
Please note that the dependencies are automatically added to the classpath. Some of the targets such as <code>compile</code> and <code>clean</code> are inherited from common.xml. The <code>package</code> target creates JARs, some of which we'll use in the bof-java Composer projects and some that we'll use in the Webtop web project.<br>
</p>
```
	<target name="package" depends="compile" description="Creates the JARs">
		<echo message="Creating module JARs" />
		<makejar name="numberservice-api.jar" includes="org/markdav/projectx/bof/numberservice/api/**" />
		<makejar name="numberservice-impl.jar" includes="org/markdav/projectx/bof/numberservice/impl/**" />
	</target>
```
<p>
The <code>install</code> target installs the packaged artifacts to the local Maven repository and the <code>deploy</code> target deploys the packaged artifacts to the remote Maven repository. Remember that other projects reference dependencies from Maven repositories so simply packaging the artifact is not sufficient.<br>
</p>
```
	<target name="install" depends="package" description="Installs the JARs to the local Maven repository">
	    <echo message="Installing module JARs" />
		<mvninstall file="${target.dir}/numberservice-api.jar"
		            groupId="com.markdav.projectx" artifactId="numberservice-api"
		            version="1.0.0-SNAPSHOT" packaging="jar" uniqueVersion="false" />
		<mvninstall file="${target.dir}/numberservice-impl.jar"
		            groupId="com.markdav.projectx" artifactId="numberservice-impl"
		            version="1.0.0-SNAPSHOT" packaging="jar" uniqueVersion="false" />
	</target>
	
	<target name="deploy" depends="package" description="Deploys the JARs to the remote Maven repository">
	    <echo message="Deploying module JARs" />
		<mvndeploy file="${target.dir}/numberservice-api.jar"
		           groupId="com.markdav.projectx" artifactId="numberservice-api"
		           version="1.0.0-SNAPSHOT" packaging="jar" uniqueVersion="false"
		           url="${mvn.snapshots.url}" repositoryId="${mvn.snapshots.id}" />
		<mvndeploy file="${target.dir}/numberservice-impl.jar"
		           groupId="com.markdav.projectx" artifactId="numberservice-impl"
		           version="1.0.0-SNAPSHOT" packaging="jar" uniqueVersion="false"
		           url="${mvn.snapshots.url}" repositoryId="${mvn.snapshots.id}" />
	</target>
```

### composer-projects ###
<p>
In the below screenshot you can see that we have two Composer projects named projectx-base and projectx-sbo.<br>
</p>
<img src='http://dctm-multi-module.googlecode.com/svn/trunk/wiki-images/composer-projects.png' />
  * **build.xml**: This is the project build script
  * **pom.xml**: This is a Maven configuration file that defines project dependencies
<p>
The projectx-sbo project is used to deploy SBOs and consequently defines a couple of JarDefs. The JARs that these JarDefs reference are built by the bof-java project. We reference these JARs through dependencies that are defined in pom.xml.<br>
</p>
```
	<dependencies>
        <dependency>
        	<groupId>com.markdav.projectx</groupId>
        	<artifactId>numberservice-api</artifactId>
        	<version>1.0.0-SNAPSHOT</version>
        </dependency>
        <dependency>
        	<groupId>com.markdav.projectx</groupId>
        	<artifactId>numberservice-impl</artifactId>
        	<version>1.0.0-SNAPSHOT</version>
        </dependency>
	</dependencies>
```
<p>
The ant build script, build.xml, just imports the commons.xml, specifying that the default target is to resolve any dependencies. The Composer project configuration has been modified to invoke a new AntBuilder - i.e. it invokes the build.xml script and therefore fetches the JARs that we need before building the DAR.<br>
<p>
<pre><code>&lt;project name="projectx-sbo" default="composerdependencies" basedir="."&gt;<br>
	&lt;dirname property="project.dir" file="${ant.file.imported}"/&gt;<br>
	&lt;import file="${project.dir}/../../common.xml" /&gt;<br>
&lt;/project&gt;<br>
</code></pre>
</p>
We can use the imported targets and <code>installdar2maven</code> and <code>deploydar2maven</code> to make the DAR available through Maven.<br>
</p>

### web-projects ###
<p>
In the below screenshot you can see that we have a single WDK project named webtop.<br>
</p>
<img src='http://dctm-multi-module.googlecode.com/svn/trunk/wiki-images/web-projects.png' />
<p>
The webtop project contains the following files and folders:<br>
</p>
  * **build.xml**: This is the project build script
  * **pom.xml**: This is a Maven configuration file that defines dependencies that are needed to compile the WAR
<p>
The <code>webtop</code> project follows the same general pattern as the <code>bof-java</code> project. Again we have a pom.xml for defining dependencies and a build.xml in which we define how to package, install and deploy the WAR.<br>
<p>
The available Ant targets are as follows:<br>
</p>
</li></ul><ul><li><b>clean</b>: Cleans any generated artifacts<br>
</li><li><b>compile</b>: Compiles the module java source<br>
</li><li><b>deploy</b>: Deploys the WAR to the remote Maven repository<br>
</li><li><b>install</b>: Installs the WAR to the local Maven repository<br>
</li><li><b>javadoc</b>: Generates Javadoc documentation<br>
</li><li><b>package</b>: Creates the WAR<br>
</li><li><b>test</b>: Runs the JUnit tests<br>
</li><li><b>war</b>: Builds the webapp<br>
<p>
The WAR dependencies are displayed below. You can see that it includes the SBO API JAR and what appears to be two copies of Webtop. The first reference to Webtop ensures that the Webtop JARs are included in the classpath. The second reference to Webtop is used for a different purpose. It is used so that the Webtop WAR can be unpacked into the project. Our Webtop customisations will be merged with the standard Webtop WAR contents. This is done automatically through the <code>war</code> target. Please note that the <code>war</code> target only assembles the web application files. It is the <code>package</code> target that actually constructs the WAR.<br>
</p>
<pre><code>	&lt;dependencies&gt;<br>
        &lt;dependency&gt;<br>
        	&lt;groupId&gt;com.markdav.projectx&lt;/groupId&gt;<br>
        	&lt;artifactId&gt;numberservice-api&lt;/artifactId&gt;<br>
        	&lt;version&gt;1.0.0-SNAPSHOT&lt;/version&gt;<br>
        &lt;/dependency&gt;<br>
        &lt;dependency&gt;<br>
        	&lt;groupId&gt;emc.webtop&lt;/groupId&gt;<br>
        	&lt;artifactId&gt;emc.webtop&lt;/artifactId&gt;<br>
        	&lt;version&gt;6.7.1&lt;/version&gt;<br>
        	&lt;type&gt;pom&lt;/type&gt;<br>
        	&lt;scope&gt;provided&lt;/scope&gt;<br>
        &lt;/dependency&gt;<br>
        &lt;dependency&gt;<br>
        	&lt;groupId&gt;emc.webtop&lt;/groupId&gt;<br>
        	&lt;artifactId&gt;emc.webtop.war&lt;/artifactId&gt;<br>
        	&lt;version&gt;6.7.1&lt;/version&gt;<br>
        	&lt;type&gt;war&lt;/type&gt;<br>
        	&lt;scope&gt;provided&lt;/scope&gt;<br>
        &lt;/dependency&gt;<br>
	&lt;/dependencies&gt;<br>
</code></pre>
<p>
Unlike Maven, Ant doesn't have support for profiles. This is an very useful feature that would allow you to create environment-specific configurations. For example, we could deploy different versions of dfc.properties and log4j properties. The <code>war</code> target is configured to substitute tokens that exist in XML and properties files that exist under the WDK custom directory and properties files that exist under WEB-INF/classes. It resolves the actual values from the following files:<br>
</p>
</li><li><b>src/main/resources/development.properties</b>
</li><li><b>src/main/resources/staging.properties</b>
</li><li><b>src/main/resources/production.properties</b>
<p>
You can specify which environment to build for by specifying the <code>profile</code> in the system properties. For example, <code>ant install -Dprofile=production</code> would build the production WAR, package it and install it to the local Maven repository. The default profile is development.<br>
</p></li></ul>

<h2>Continuous Integration ##
All the steps up to this point facilitates multi-module project builds using Maven-managed dependencies. What we're going to do now is to automate the build using the Jenkins build server using the following steps.

### Create Job to Build JARs Using Jenkins UI ###
  * Create free-style software project (e.g. I named this projectx-java)
  * Specify the SVN endpoint (e.g. svn://buildserver/projectx/trunk)
  * Specify the build trigger (e.g. poll Subversion every 5 minutes by specifying `'*/5 * * * *'` as the schedule)
  * Specify that the master script '/build.xml' is invoked using targets 'install' and 'deploy'
  * Specify that artifacts should be archived (I used the pattern `java-projects/bof-java/target/*.jar,web-projects/**/*.war`)
  * Specify that projectx-composer job is invoked by post-build trigger

Please note that the post-build trigger can only be configured after the target job has been created. You can optionally specify that the WAR is deployed to an application server as a post-build activity. Jenkins makes this easy - you just have to specify the endpoint to deploy to and the administrator credentials.

### Create Job to Build DARs Using Jenkins UI ###

  * Create free-style software project (e.g. I named this projectx-composer)
  * Specify the SVN endpoint (e.g. svn://buildserver/projectx/trunk)
  * Paste the contents of jenkins-composer-build-script-example.txt into the shell window
  * Specify that artifacts should be archived (I used the pattern `*.dar`)

Please note that you may need to modify the script to reflect your environment. At the beginning of the script are a number of variables that allow you to specify values such as the path to Headless Composer, the workspace path and deployment credentials.

At the end of the script are calls function to the create\_dar and deploy\_dar functions. Comment out the deploy\_dar calls if you do not require the DARs to be automatically deployed.

# Results #
If all goes well you will end up with an automated build system that will:

  * facilitate dependency management
  * facilitate multi-module development
  * check that developers have checked in all relevant source files (otherwise the compile would fail)
  * runs potentially lengthy JUnit tests and alert developers to failures
  * build the latest JARs, WARs and DARs and make them available through Nexus
  * automatically deploy WARs to application servers and DARs to Documentum repositories

The JARs and WARs will be available in Jenkins:

<img src='http://dctm-multi-module.googlecode.com/svn/trunk/wiki-images/projectx-java.png' />

The DARs will be also available in Jenkins:

<img src='http://dctm-multi-module.googlecode.com/svn/trunk/wiki-images/projectx-composer.png' />

All generated JAR, WAR and DAR artifacts will also be deployed to the Nexus Maven Repository:

<img src='http://dctm-multi-module.googlecode.com/svn/trunk/wiki-images/nexus.png' />