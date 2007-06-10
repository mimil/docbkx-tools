/*
 * Copyright 2006 Wilfred Springer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.agilejava.maven.docbkx;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;


/**
 * The base class for all Mojo's that perform some work based on a DocBook XSL
 * distribution.
 *
 *
 * @author Wilfred Springer
 */
public abstract class AbstractBuilderMojo extends AbstractMojo {

    /**
     * The type of output to be generated. (Currently matches the name of the
     * directory in the DocBook XSL distribution holding the relevant
     * stylesheets.)
     *
     * @parameter default-value="html"
     */
    private String type;

    /**
     * An XPath expression for selecting the description.
     */
    protected DOMXPath selectDescription;

    /**
     * The directory in the jar file in which the DocBook XSL artifacts will be
     * stored.
     *
     * @parameter default-value="META-INF/docbkx"
     */
    private String stylesheetRoot;

    /**
     * The location of the stylesheet. Normally this would be derived from the
     * {@link #stylesheetRoot} variable and the type.
     *
     * @parameter
     */
    private String stylesheetLocation;

    /**
     * The groupId of any results coming out of this plugin.
     *
     * @parameter expression="net.sf.docbook";
     */
    private String groupId;

    /**
     * The DocBook-XSL distribution.
     *
     * @parameter expression="${distribution}"
     * @required
     */
    private File distribution;

    /**
     * A comma-separated list of properties that should be exluded from the
     * generated code.
     *
     * @parameter
     */
    protected String excludedProperties;

    /**
     * Constructs a new instance.
     *
     */
    public AbstractBuilderMojo() {
        try {
            selectDescription = new DOMXPath(
                    "//refsect1[position()=1]/para[position()=1]/text()");
        } catch (JaxenException e) {
            throw new IllegalStateException("Failed to parse XPath expression.");
            // This would render the object to be unusable.
        }
    }

    /**
     * Returns the version of the docBookXslDistribution.
     *
     * @return The version number of the DocBook XSL distribution.
     */
    public String getVersion() {
        String name = distribution.getName();
        int versionStart = name.lastIndexOf('-');
        if (versionStart < 0) {
            return null;
        }
        name = name.substring(versionStart + 1);
        name = name.substring(0, name.length() - 4);
        return name;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        ZipFileProcessor processor = new ZipFileProcessor(distribution);
        process(processor);
    }

    private String getCatalogKey() {
        return "DOCBOOK-XSL-EXTRACTED-" + getVersion();
    }

    protected String getGroupId() {
        return groupId;
    }

    protected String getDocBookXslPrefix() {
        return "docbook-xsl-" + getVersion();
    }

    protected abstract void process(ZipFileProcessor zipFile)
            throws MojoExecutionException, MojoFailureException;

    /**
     * Constructs a new {@link DocumentBuilder}.
     *
     * @return A new {@link DocumentBuilder} instance.
     * @throws ParserConfigurationException
     *             If we can't construct a parser.
     */
    protected DocumentBuilder createDocumentBuilder()
            throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder();
    }

    public String getStylesheetRoot() {
        return stylesheetRoot;
    }

    public String getStylesheetLocation() {
        return stylesheetLocation;
    }

    public String getType() {
        return type;
    }

}
