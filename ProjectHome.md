# Docbkx Tools #

(Latest version: 2.0.15)

&lt;wiki:gadget url="http://www.ohloh.net/p/18933/widgets/project\_users\_logo.xml" height="43" border="0"/&gt;&lt;wiki:gadget url="http://www.ohloh.net/p/18933/widgets/project\_partner\_badge.xml" height="53" border="0"/&gt;


## What is it? ##

The Docbkx Tools project provides a number of tools supporting DocBook in a Maven environment. This may seem odd to you, since 1) Maven 2 is supposed to support DocBook natively, relying on Doxia, and 2) there is already another DocBook plugin at mojo.codehaus.org.

The thruth however is that DocBook support in Doxia is fairly limited, mainly because Doxia as a framework supports only a small fraction of the concepts found in DocBook. The subset of DocBook supported by Doxia is not even close to simplified DocBook.

The DocBook plugin at mojo.codehaus.org is supporting a wider range of DocBook markup, and is in fact more similar to the DocBook tools provided with this project. There are however some significant differences.

## Differences with other Maven plugins ##

  * The focus is on ease of use.
  * You should not be required to install additional stuff to your hard disk in order to generate content from your DocBook sources. Simply adding a reference to the plugin in your POM should be sufficient.
  * This project focuses on providing dedicated support for particular DocBook XSL stylesheet distributions. That means you can rely on the dedicated parameterization mechanism of Maven Plugins to pass in the XSLT parameters defined for a particular version and type of XSLT stylesheet.
  * In the DocBook Plugin found at mojo.codehaus.org, you will be required to download a specific version of the DocBook XSL stylesheets manually. The plugins packaged contain the stylesheets as well. (In this project, a particular version of the stylesheets is closely tied to a particular version of the plugin. That you means you can always rely on the plugin's documentation to know which parameters you could pass in.)
  * The DocBook plugin found at mojo.codehaus.org requires you to have access to the Internet in order allow the plugin to resolve URI's. The plugins provided in this project act differently: if your DocBook sources are referening to a DTD, then you can simply add a dependency to a jar file containing the DTD and related entities, and the plugin will make sure that all references will be resolved correctly.

## How does this compare the DocBook Tools at SourceForge? ##

This project builds on these tools; the main purpose is to provide something that integrates easily with Maven based builds.

## I want to know more ##

Find the User Guide of the Maven Docbkx Plugin http://docbkx-tools.sourceforge.net/docbkx-maven-plugin/plugin-info.html.

