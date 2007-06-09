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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jaxen.JaxenException;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.agilejava.maven.docbkx.spec.Parameter;

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
    private DOMXPath selectDescription;

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
     * The prefix of any artifactId coming out of this plugin.
     *
     * @parameter expression="docbkx";
     */
    private String artifactIdPrefix;

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
     * The list of parameters in use.
     */
    private List parameters;

    /**
     * A comma-separated list of properties that should be exluded from the
     * generated code.
     *
     * @parameter
     */
    private String excludedProperties;

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
     * Returns a list of parameters to be passed to the plugin.
     *
     * @param paramEntities
     * @return A List of {@link Parameter} objects.
     * @throws IOException
     *             If we can't read (or fail to parse) from the parameter entity
     *             file passed in.
     */
    protected List extractParameters(InputStream paramEntities,
            ZipFileProcessor processor, final String directory)
            throws IOException {
        final List excluded = getExcludedProperties();
        if (parameters != null) {
            return parameters;
        } else {
            parameters = new ArrayList();
            final List parameterNames = new ArrayList();
            EntityFileParser.parse(paramEntities,
                    new EntityFileParser.EntityVisitor() {
                        public void visitSystemEntity(String name,
                                String systemId) {
                            parameterNames
                                    .add(getDocBookXslPrefix()
                                            + systemId.substring(systemId
                                                    .indexOf('/')));
                        }
                    });
            processor.process(new ZipFileProcessor.ZipEntryVisitor() {
                public void visit(ZipEntry entry, InputStream in)
                        throws IOException {
                    if (parameterNames.contains(entry.getName())) {
                        String name = entry.getName().substring(
                                entry.getName().lastIndexOf('/') + 1);
                        parameters.add(extractParameter(name, in));
                    }
                }
            });
            return parameters;
        }
    }

    /**
     * Returns a <code>List</code> of property names that will be excluded
     * from the code generation.
     *
     * @return A <code>List</code> of property names, identifying the
     *         properties that must be excluded from code generation.
     */
    private List getExcludedProperties() {
        List excluded;
        if (excludedProperties != null) {
            excluded = Arrays.asList(excludedProperties.split(",[ ]*"));
        } else {
            excluded = Collections.EMPTY_LIST;
        }
        return excluded;
    }

    /**
     * Extracts the Parameter metadata from the parameter metadata file.
     *
     * @param paramMetadata
     *            The file containing the metadata.
     * @return The Parameter object holding the metadata.
     * @throws IOException
     * @throws SAXException
     */
    protected Parameter extractParameter(String filename, InputStream in) {
        Parameter parameter = new Parameter();
        parameter.setName(filename.substring(0, filename.length() - 4));
        try {
            DocumentBuilder builder = createDocumentBuilder();
            Document document = builder.parse(in);
            Node node = (Node) selectDescription.selectSingleNode(document);
            String result = node.getNodeValue();
            result = result.substring(0, result.indexOf('.') + 1);
            result = result.trim();
            result = result.replace('\n', ' ');
            parameter.setDescription(result);
        } catch (IOException ioe) {
            getLog().warn(
                    "Failed to obtain description for " + parameter.getName(),
                    ioe);
        } catch (ParserConfigurationException pce) {
            throw new IllegalStateException("Failed to create DocumentBuilder.");
        } catch (SAXException se) {
            getLog().warn(
                    "Failed to obtain description for " + parameter.getName(),
                    se);
        } catch (JaxenException je) {
            getLog().warn(
                    "Failed to obtain description for " + parameter.getName(),
                    je);
        }
        return parameter;
    }

    private DocumentBuilder createDocumentBuilder()
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
