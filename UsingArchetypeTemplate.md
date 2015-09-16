# Introduction #

A maven archetype is a template system to initialize a new maven project. It allows you to quick start as a default layout and additional files (with meaning for the current project) are created.

**docbkx-quickstart-archetype** creates a default project with:
  * a pom.xml file already configured
  * a sample docbook document
  * a docbook stylesheet customization for fop
  * a plugin execution to generate a pdf file

# How to use #
## Fresh install ##
If you don't have nay docbkx-tools plugins already installed in your locale maven repository (2.0.10-SNAPSHOT +) you will have to use the following command line to initialize a project:

```
mvn archetype:create 
  -DgroupId=[your group id]
  -DartifactId=[your artifact id]
  -DarchetypeGroupId=com.agilejava.docbkx
  -DarchetypeArtifactId=docbkx-quickstart-archetype
  -DarchetypeVersion=2.0.10-SNAPSHOT
  -DremoteRepositories=http://docbkx-tools.sourceforge.net/snapshots/
```

## Latest docbkx-tools already installed ##

You should be able to use `mvn archetype:generate` and expect something like this:
```
[INFO] ...
Choose archetype:
1: local -> docbkx-quickstart-archetype (A number of tools for handling DocBook XML transformations.)
2: internal -> appfuse-basic-jsf (AppFuse archetype for creating a web application with Hibernate, Spring and JSF)
3: internal -> appfuse-basic-spring (AppFuse archetype for creating a web application with Hibernate, Spring and Spring MVC)
4: internal -> appfuse-basic-struts (AppFuse archetype for creating a web application with Hibernate, Spring and Struts 2)
5: internal -> appfuse-basic-tapestry (AppFuse archetype for creating a web application with Hibernate, Spring and ...

Choose a number:  (1/2/3/4/5 ...
```

And choose the number corresponding to _docbkx-quickstart-archetype_

If you are not prompted for a local repository you can try this command:

```
mvn org.apache.maven.plugins:maven-archetype-plugin:2.0:generate -DarchetypeArtifactId=docbkx-quickstart-archetype -DarchetypeGroupId=com.agilejava.docbkx -DarchetypeCatalog=local
```

## Run ##
If everything runs fine you should have your project.

Now enter you project and run `mvn pre-site` to start the preconfigured execution and you should notice a _target/docbkx/pdf/book.pdf_ file.

_As for now only 2.0.10-SNAPSHOT supports the archetype feature, so you will also need to add our snapshot repository to either the generated pom.xml or within you $home/settings.xml - Have a look at HowToUseRepositories for more information about this_

# Still lost? #

Well, you should have a _pom.xml_ which looks like this one:

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>mygroupd</groupId>
  <artifactId>myartifactId</artifactId>
  <version>1.0-SNAPSHOT</version>
  <name>Docbkx Maven Quikstart Archetype</name>
  <packaging>pom</packaging>


  <build>
    <plugins>
      <plugin>
        <groupId>com.agilejava.docbkx</groupId>
        <artifactId>docbkx-maven-plugin</artifactId>
        <version>2.0.10-SNAPSHOT</version>
        <executions>
          <!-- -->
          <execution>
            <id>documentation identifier</id>
            <phase>pre-site</phase>
            <goals>
              <!--
              <goal>generate-html</goal>
              <goal>generate-pdf</goal>
              <goal>generate-...</goal>
              -->
              <goal>generate-pdf</goal>
            </goals>
            <configuration>
              <!-- per execution configuration -->
              <includes>book.xml</includes>
              <draftMode>yes</draftMode>
            </configuration>
          </execution>
        </executions>
        <configuration>
          <!-- shared configuration -->
          <generatedSourceDirectory>${project.build.directory}/docbkx/generated</generatedSourceDirectory>
          <xincludeSupported>true</xincludeSupported>
          <paperType>A4</paperType>
          <fop1Extensions>1</fop1Extensions>

          <foCustomization>src/docbkx-stylesheet/fo/docbook.xsl</foCustomization>
          
          <customizationParameters>
            <!-- additional XSLT parameters-->
            <parameter>
              <name>key</name>
              <value>value</value>
            </parameter>
          </customizationParameters>
        </configuration>
      </plugin>
    </plugins>
  </build>
  
<pluginRepositories>
  <pluginRepository>
    <id>docbkx.snapshots</id>
    <name>Maven Plugin Snapshots</name>
    <url>http://docbkx-tools.sourceforge.net/snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </pluginRepository>
</pluginRepositories>

</project>
```