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
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.jaxen.JaxenException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.agilejava.maven.docbkx.ZipFileProcessor.ZipEntryVisitor;
import com.agilejava.maven.docbkx.spec.Parameter;
import com.agilejava.maven.docbkx.spec.Specification;

/**
 * A Maven plugin for generating a DocBook XSL plugin with specific setters and
 * getters for the parameters in the specific distribution of the DocBook XSL
 * stylesheets.
 *
 * @author Wilfred Springer
 * @goal generate
 * @phase generate-sources
 */
public class PluginBuilderMojo extends AbstractBuilderMojo {

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The classname of the Mojo that wil provide the desired functionality.
     *
     * @parameter
     */
    private String className;

    /**
     * The package name of the Mojo that will provide the desired functionality.
     *
     * @parameter
     */
    private String packageName;

    /**
     * The classname of the super class from which the generate Mojo will
     * inherit.
     *
     * @parameter expression="com.agilejava.docbkx.maven.AbstractTransformerMojo"
     */
    private String superClassName;

    /**
     * The suffix to be used in the generated plugin.
     *
     * @parameter
     */
    private String pluginSuffix;

    /**
     * Target directory.
     *
     * @parameter expression="${project.build.directory}/generated-sources"
     */
    private File targetDirectory;

    private String getPackageName() {
        return packageName != null ? packageName : getGroupId();
    }

    /**
     * Returns the name of the class that will be used. If {@link #className} is
     * set, then that name will be used instead of the default.
     *
     * @return The name of the class for the Mojo being generated.
     */
    private String getClassName() {
        if (className == null) {
            StringBuffer builder = new StringBuffer();
            builder.append("Docbkx");
            builder.append(Character.toUpperCase(getType().charAt(0)));
            builder.append(getType().substring(1));
            builder.append("Mojo");
            return builder.toString();
        } else {
            return className;
        }
    }

    protected void process(ZipFileProcessor processor)
            throws MojoExecutionException, MojoFailureException {
        File sourcesDir = new File(targetDirectory, getPackageName().replace(
                '.', '/'));
        try {
            FileUtils.forceMkdir(sourcesDir);
        } catch (IOException ioe) {
            throw new MojoExecutionException(
                    "Can't create directory for sources.", ioe);
        }
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream in = loader.getResourceAsStream("plugins.stg");
        Reader reader = new InputStreamReader(in);
        StringTemplateGroup group = new StringTemplateGroup(reader);
        StringTemplate template = group.getInstanceOf("plugin");
        File targetFile = new File(sourcesDir, getClassName() + ".java");
        Specification specification = null;
        try {
            specification = createSpecification(processor);
            getLog().info(
                    "Number of parameters: "
                            + specification.getParameters().size());
            template.setAttribute("spec", specification);
            FileUtils.writeStringToFile(targetFile, template.toString(), null);
        } catch (IOException ioe) {
            if (specification == null) {
                throw new MojoExecutionException("Failed to read parameters.",
                        ioe);
            } else {
                throw new MojoExecutionException("Failed to create "
                        + targetFile + ".", ioe);
            }
        }
        project.addCompileSourceRoot(targetDirectory.getAbsolutePath());
    }

    private Specification createSpecification(ZipFileProcessor processor)
            throws IOException {
        Specification specification = extractSpecification(processor);
        specification.setType(getType());
        if (getStylesheetLocation() != null) {
            specification.setStylesheetLocation(getStylesheetLocation());
        } else {
            specification.setStylesheetLocation(getStylesheetRoot() + "/"
                    + getType() + "/docbook.xsl");
        }
        return specification;
    }

    protected Specification extractSpecification(
            final ZipFileProcessor processor) throws IOException {
        final String directory = getDocBookXslPrefix() + "/" + getType() + "/";
        final String file = directory + "param.ent";
        final Specification specification = new Specification();
        processor.process(new ZipEntryVisitor() {
            public void visit(ZipEntry entry, InputStream in)
                    throws IOException {
                if (entry.getName().endsWith(file)) {
                    List parameters = extractParameters(in, processor,
                            directory);
                    specification.setPameters(parameters);
                    specification.setClassName(getClassName());
                    specification.setSuperClassName(superClassName);
                    specification.setPackageName(getPackageName());
                    specification.setDocbookXslVersion(getVersion());
                    specification.setPluginSuffix(pluginSuffix);
                }
            }
        });
        return specification;
    }

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
        final List parameters = new ArrayList();
        final List parameterNames = new ArrayList();
        EntityFileParser.parse(paramEntities,
                new EntityFileParser.EntityVisitor() {
                    public void visitSystemEntity(String name, String systemId) {
                        parameterNames.add(getDocBookXslPrefix()
                                + systemId.substring(systemId.indexOf('/')));
                    }
                });
        processor.process(new ZipFileProcessor.ZipEntryVisitor() {
            public void visit(ZipEntry entry, InputStream in)
                    throws IOException {
                if (parameterNames.contains(entry.getName())) {
                    String name = entry.getName().substring(
                            entry.getName().lastIndexOf('/') + 1);
                    name = name.substring(0, name.length() - 4);
                    if (!excluded.contains(name)) {
                        parameters.add(extractParameter(name, in));
                    }
                }
            }
        });
        return parameters;
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
     * @param name
     *            The name of the (XSLT) parameter.
     * @param in
     *            The InputStream providing a description (refentry) of the
     *            parameter.
     * @return The Parameter object holding the metadata.
     * @throws IOException
     *             If it appears to be impossible to read from the
     *             <code>InputStream</code>.
     * @throws SAXException
     *             If it appears to interpret the data on the
     *             <code>InputStream</code> as XML.
     */
    protected Parameter extractParameter(String name, InputStream in) {
        Parameter parameter = new Parameter();
        parameter.setName(name);
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

}
