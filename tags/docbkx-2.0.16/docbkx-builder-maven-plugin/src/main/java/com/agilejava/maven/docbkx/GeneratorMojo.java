/*
 * #%L
 * Docbkx Maven Plugin Builder
 * %%
 * Copyright (C) 2006 - 2014 Wilfred Springer, Cedric Pronzato
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.agilejava.maven.docbkx;

import com.agilejava.maven.docbkx.spec.Parameter;
import com.agilejava.maven.docbkx.spec.Specification;

import com.icl.saxon.TransformerFactoryImpl;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.jaxen.JaxenException;

import org.jaxen.dom.DOMXPath;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * The base class for all Mojo's that perform some work based on a DocBook XSL *
 * distribution.
 *
 * @author Wilfred Springer
 * @goal build
 * @phase generate-sources
 * @requiresDependencyResolution compile
 */
public class GeneratorMojo extends AbstractMojo {
  /**
   * The name of the stylesheet used as the basis of the {@link Transformer}
   * returned by {@link #createParamListTransformer()}.
   */
  private static String TRANSFORMER_LOCATION = "extract-params.xsl";

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

  /**
   * The maven project helper class for adding resources.
   *
   * @component role="org.apache.maven.project.MavenProjectHelper"
   */
  private MavenProjectHelper projectHelper;

  /**
   * The directory where all new resources need to be stored.
   *
   * @parameter expression="${basedir}/target/generated-resources"
   */
  private File targetResourcesDirectory;

  /**
   * A reference to the project.
   *
   * @parameter expression="${project}"
   * @required
   */
  private MavenProject project;

  /**
   * The type of output to be generated. (Currently matches the name of the
   * directory in the DocBook XSL distribution holding the relevant
   * stylesheets.)
   *
   * @parameter default-value="html"
   * @required
   */
  private String type;

  /**
   * The extension of the target file
   *
   * @parameter
   */
  private String targetFileExtension;

  /**
   * False if the stylesheet is responsible to create the output file(s) using its own naming scheme.
   *
   * @parameter default-value=true
   */
  private boolean useStandardOutput = true;

  /**
   * An XPath expression for selecting the description.
   */
  protected DOMXPath selectDescription;

  /**
   * An XPath expression for selecting the datatype.
   */
  private DOMXPath selectType;

  /**
   * The directory in the jar file in which the DocBook XSL artifacts will be
   * stored.
   *
   * @parameter default-value="docbook"
   */
  private String stylesheetTargetRoot;

  /**
   * The location of the stylesheet in the destination jar file. Normally this
   * would be derived from the {@link #stylesheetTargetRoot} and
   * {@link #stylesheetPath}. By default:
   * <code>${stylesheetTargetRoot}/${stylesheetPath}</code>.
   *
   * @parameter
   */
  private String stylesheetTargetLocation;

  /**
   * The default location of the stylesheet within the distribution. By
   * default: <code>${type}/docbook.xsl</code>.
   *
   * @parameter
   */
  private String stylesheetPath;

  /**
   * The groupId of any results coming out of this plugin.
   *
   * @parameter expression="net.sf.docbook";
   */
  private String groupId;

  /**
   * The version of the DocBook XSL stylesheets.
   *
   */
  private String version;

  /**
   * The DocBook-XSL distribution. By default
   * <code>${basedir}/lib/docbook-xsl-${version}.zip</code>.
   *
   */
  private File distribution;

  /**
   * The root directory within the source zip file containing the DocBook XSL
   * stylesheet distribution. Default: <code>docbook-xsl-${version}/</code>.
   *
   * @parameter
   */
  private String sourceRootDirectory = "docbook/";

  /**
   * A comma-separated list of properties that should be exluded from the
   * generated code.
   *
   * @parameter
   */
  protected String excludedProperties;

  /**
   * Specify source file encoding; e.g., UTF-8
   *
   * @parameter expression="${project.build.sourceEncoding}"
   */
  private String encoding;

  public GeneratorMojo() {
    try {
      selectDescription = new DOMXPath("//refsection[position()=1]/para[position()=1]/text()");
      selectType = new DOMXPath("//refmiscinfo[@class='other' and @otherclass='datatype']/text()");
    } catch (JaxenException e) {
      throw new IllegalStateException("Failed to parse XPath expression.");

      // This would render the object to be unusable.
    }
  }

  // JavaDoc inherited
  public void execute() throws MojoExecutionException, MojoFailureException {
    completeConfiguration();
    generateSourceCode();
  }

  /**
   * Completes configuration.
   */
  private void completeConfiguration() throws MojoExecutionException {
    if (distribution == null) {
      boolean found = false;

      Set artifacts = project.getDependencyArtifacts();

      if (artifacts != null) {
        Iterator i = artifacts.iterator();

        while (i.hasNext()) {
          Artifact artifact = (Artifact) i.next();

          if ("net.sf.docbook".equals(artifact.getGroupId()) && "docbook-xsl".equals(artifact.getArtifactId())) {
            distribution = artifact.getFile();
            version = artifact.getVersion();
            found = true;
            getLog().debug(
                "Docbook artifact used for generation: " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
                    + artifact.getVersion() + ":" + artifact.getClassifier());

            break;
          }
        }
      }

      if (!found) {
        throw new MojoExecutionException("Unable to find a valid docbook depencency artifact");
      }
    }

    if (stylesheetPath == null) {
      // ${type}/docbook.xsl
      stylesheetPath = type + "/docbook.xsl";
    }

    if (stylesheetTargetLocation == null) {
      // ${stylesheetTargetRoot}/${stylesheetPath}
      stylesheetTargetLocation = stylesheetTargetRoot + "/" + stylesheetPath;
    }
  }

  /**
   * Constructs a new {@link DocumentBuilder}.
   *
   * @return A new {@link DocumentBuilder} instance.
   * @throws ParserConfigurationException
   *             If we can't construct a parser.
   */
  private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    return factory.newDocumentBuilder();
  }

  /**
   * Generate the source code of the plugin supporting the {@link #type}.
   *
   * @throws MojoExecutionException
   *             If we fail to generate the source code.
   */
  private void generateSourceCode() throws MojoExecutionException {
    File sourcesDir = new File(targetDirectory, getPackageName().replace('.', '/'));

    try {
      FileUtils.forceMkdir(sourcesDir);
    } catch (IOException ioe) {
      throw new MojoExecutionException("Can't create directory for sources.", ioe);
    }

    ClassLoader loader = this.getClass().getClassLoader();
    InputStream in = loader.getResourceAsStream("plugins.stg");
    Reader reader = new InputStreamReader(in, Charset.forName(encoding));
    StringTemplateGroup group = new StringTemplateGroup(reader);
    StringTemplate template = group.getInstanceOf("plugin");
    File targetFile = new File(sourcesDir, getClassName() + ".java");
    Specification specification = null;

    try {
      specification = createSpecification();
      getLog().info("Number of parameters: " + specification.getParameters().size());
      template.setAttribute("spec", specification);
      FileUtils.writeStringToFile(targetFile, template.toString(), encoding);
    } catch (IOException ioe) {
      if (specification == null) {
        throw new MojoExecutionException("Failed to read parameters.", ioe);
      } else {
        throw new MojoExecutionException("Failed to create " + targetFile + ".", ioe);
      }
    }

    project.addCompileSourceRoot(targetDirectory.getAbsolutePath());
  }

  /**
   * Creates the {@link Specification} used for generating the plugin code.
   *
   * @return The {@link Specification} uesd for generating the plugin source
   *         code.
   * @throws MojoExecutionException
   *             If the {@link Specification} cannot be created.
   */
  private Specification createSpecification() throws MojoExecutionException {
    String stylesheetLocation = (stylesheetTargetLocation == null) ? (stylesheetTargetRoot + "/" + type + "/docbook.xsl")
        : stylesheetTargetLocation;
    Specification specification = new Specification();
    specification.setType(type);
    specification.setStylesheetLocation(stylesheetLocation);
    specification.setClassName(getClassName());
    specification.setSuperClassName(superClassName);
    specification.setPackageName(getPackageName());
    specification.setDocbookXslVersion(version);
    specification.setPluginSuffix(pluginSuffix);
    specification.setParameters(extractParameters());
    specification.setUseStandardOutput(useStandardOutput);
    specification.setTargetFileExtension(targetFileExtension);

    return specification;
  }

  /**
   * Extracts the {@link Parameter} definitions from the stylesheets.
   *
   * @return A <code>List</code> of {@link Parameter} elements defining the
   *         parameters of the plugin.
   * @throws MojoExecutionException
   *             If we can't create the list of parameters.
   */
  private List extractParameters() throws MojoExecutionException {
    String stylesheetURL = createURL(stylesheetPath);
    Collection parameterNames = getParameterNames(stylesheetURL);
    List parameters = new ArrayList();
    Iterator iterator = parameterNames.iterator();
    Collection excluded = getExcludedProperties();

    while (iterator.hasNext()) {
      String name = (String) iterator.next();

      if (!excluded.contains(name)) {
        parameters.add(extractParameter(name));
      }
    }

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
   * @return The Parameter object holding the metadata.
   */
  private Parameter extractParameter(String name) throws MojoExecutionException {
    String url = createURL("params/" + name + ".xml");
    Parameter parameter = new Parameter();
    parameter.setName(name);

    try {
      DocumentBuilder builder = createDocumentBuilder();
      Document document = builder.parse(url);
      Node node = (Node) selectDescription.selectSingleNode(document);

      if (node == null) {
        getLog().warn("Failed to parse description for " + name);

        return parameter;
      }

      String result = node.getNodeValue();
      result = result.substring(0, result.indexOf('.') + 1);
      result = result.trim();
      result = result.replace('\n', ' ');
      parameter.setDescription(result);
      node = (Node) selectType.selectSingleNode(document);

      if (node != null) {
        parameter.setTypeFromRefType(node.getNodeValue());
      } else {
        getLog().warn("Missing type info for " + name);
      }
    } catch (FileNotFoundException fnfe) {
      logMissingDescription(name, fnfe);
    } catch (IOException ioe) {
      logMissingDescription(name, ioe);
    } catch (ParserConfigurationException pce) {
      logMissingDescription(name, pce);
    } catch (SAXException se) {
      logMissingDescription(name, se);
    } catch (JaxenException je) {
      logMissingDescription(name, je);
    }

    return parameter;
  }

  /**
   * Logs a missing description of a parameter.
   *
   * @param name
   *            The name of the parameter.
   * @param cause
   *            The exception causing the problem.
   */
  private void logMissingDescription(String name, Throwable cause) {
    getLog().warn("Failed to obtain description for " + name);
    getLog().debug(cause);
  }

  /**
   * Returns a String version of the URL pointing the specific file in the
   * distribution. (Note that the filename passed in is expected to leave out
   * the version specific directory name. It will be included by this
   * operation.)
   *
   * @param filename
   *            The filename for which we need a URL.
   * @return A URL pointing to the specific filename.
   * @throws MojoExecutionException
   *             If we can't create a URL from the filename passed in.
   */
  private String createURL(String filename) throws MojoExecutionException {
    try {
      StringBuilder builder = new StringBuilder();
      builder.append("jar:");
      builder.append(distribution.toURL().toExternalForm());
      builder.append("!/");
      builder.append(sourceRootDirectory);
      builder.append(filename);

      return builder.toString();
    } catch (MalformedURLException mue) {
      throw new MojoExecutionException("Failed to construct URL for " + filename + ".", mue);
    }
  }

  /**
   * Returns a {@link Collection} of all parameter names defined in the
   * stylesheet or in one of the stylesheets imported or included in the
   * stylesheet.
   *
   * @param url
   *            The location of the stylesheet to analyze.
   * @return A {@link Collection} of all parameter names found in the
   *         stylesheet pinpointed by the <code>url</code> argument.
   *
   * @throws MojoExecutionException
   *             If the operation fails to detect parameter names.
   */
  private Collection getParameterNames(String url) throws MojoExecutionException {
    ByteArrayOutputStream out = null;

    try {
      Transformer transformer = createParamListTransformer();
      Source source = new StreamSource(url);
      out = new ByteArrayOutputStream();

      Result result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      if(out.size() != 0) {
        // at least one param has been found, because the split with return an empty string if there is no data
        String[] paramNames = new String(out.toByteArray()).split("\n");
        return new HashSet(Arrays.asList(paramNames));
      } else {
        // else no param found
        return new HashSet();
      }
    } catch (IOException ioe) {
      // Impossible, but let's satisfy PMD and FindBugs
      getLog().warn("Failed to flush ByteArrayOutputStream.");
    } catch (TransformerConfigurationException tce) {
      throw new MojoExecutionException("Failed to create Transformer for retrieving parameter names", tce);
    } catch (TransformerException te) {
      throw new MojoExecutionException("Failed to apply Transformer for retrieving parameter names.", te);
    } finally {
      IOUtils.closeQuietly(out);
    }

    return Collections.EMPTY_SET;
  }

  /**
   * Creates a {@link Transformer} with the ability to transitively detect the
   * names of all parameters defined on global level for a certain XSLT
   * stylesheet.
   *
   * @return The {@link Transformer} that takes a stylesheet as input, and
   *         outputs a text stream containing the parameter names.
   * @throws TransformerConfigurationException
   *             If we can't create the <code>Transformer</code>.
   */
  private Transformer createParamListTransformer() throws TransformerConfigurationException {
    TransformerFactory factory = new TransformerFactoryImpl();
    URL stylesheet = Thread.currentThread().getContextClassLoader().getResource(TRANSFORMER_LOCATION);
    Source source = new StreamSource(stylesheet.toExternalForm());

    return factory.newTransformer(source);
  }

  /**
   * Returns the package name to be used for the generated plugin.
   *
   * @return The package name to be used for the generated plugin.
   */
  private String getPackageName() {
    return (packageName != null) ? packageName : groupId;
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
      builder.append(Character.toUpperCase(type.charAt(0)));
      builder.append(type.substring(1));
      builder.append("Mojo");

      return builder.toString();
    } else {
      return className;
    }
  }
}
