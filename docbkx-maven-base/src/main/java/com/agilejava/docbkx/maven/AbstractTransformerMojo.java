/*
 * #%L
 * Docbkx Maven Base
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
package com.agilejava.docbkx.maven;

import com.icl.saxon.Controller;
import com.icl.saxon.TransformerFactoryImpl;
import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;
import nu.xom.xinclude.XIncludeException;
import nu.xom.xinclude.XIncluder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.antrun.AntPropertyHelper;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.types.Path;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.StringUtils;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

/**
 * The abstract Mojo base for concrete Mojos that generate some kind of output format from DocBook. This Mojo will
 * search documents in the directory returned by {@link #getTargetDirectory()}, and apply the stylesheets on these
 * documents. This Mojo will be subclassed by Mojo's that generate a particular type of output.
 *
 * @author Wilfred Springer
 */
public abstract class AbstractTransformerMojo extends AbstractMojo {
  protected String[] catalogs = { "catalog.xml", "docbook/catalog.xml" };

  /**
   * Builds the actual output document.
   */
  public void execute() throws MojoExecutionException, MojoFailureException {
    if (isSkip()) {
      getLog().info("Skipping plugin execution");
      return;
    }

    // userland (ant tasks) pre process
    preProcess();

    final File targetDirectory = getTargetDirectory();
    final File sourceDirectory = getSourceDirectory();
    if (!sourceDirectory.exists()) {
      return; // No sources, so there is nothing to render.
    }
    if (!targetDirectory.exists()) {
      org.codehaus.plexus.util.FileUtils.mkdir(targetDirectory.getAbsolutePath());
    }

    final String[] included = scanIncludedFiles();

    // configure a resolver for catalog files
    final CatalogManager catalogManager = createCatalogManager();
    final CatalogResolver catalogResolver = new CatalogResolver(catalogManager);
    // configure a resolver for urn:dockbx:stylesheet
    final URIResolver uriResolver = createStyleSheetResolver(catalogResolver);
    // configure a resolver for xml entities
    final InjectingEntityResolver injectingResolver = createEntityResolver(catalogResolver);

    EntityResolver resolver = catalogResolver;
    if (injectingResolver != null) {
      resolver = injectingResolver;
    }

    // configure the builder for XSL Transforms
    final TransformerBuilder builder = createTransformerBuilder(uriResolver);
    // configure the XML parser
    SAXParserFactory factory = createParserFactory();

    // iterate over included source files
    for (int i = included.length - 1; i >= 0; i--) {
      try {
        if (injectingResolver != null) {
          injectingResolver.forceInjection();
        }
        final String inputFilename = included[i];
        // targetFilename is inputFilename - ".xml" + targetFile extension
        String baseTargetFile = inputFilename.substring(0, inputFilename.length() - 4);
        final String targetFilename = baseTargetFile + "." + getTargetFileExtension();
        final File sourceFile = new File(sourceDirectory, inputFilename);
        getLog().debug("SourceFile: " + sourceFile.toString());


        // creating targetFile
        File targetFile = null;
        if (isUseStandardOutput()) {
          targetFile = new File(targetDirectory, targetFilename);
          getLog().debug("TargetFile: " + targetFile.toString());
        } else {
          String name = new File(baseTargetFile).getName();
          String dir = new File(baseTargetFile).getParent();
          if (dir == null) { // file is located on root of targetDirectory
            targetFile = targetDirectory;
          } else { // else append the relative directory to targetDirectory
            targetFile = new File(targetDirectory, dir);
          }
          targetFile = new File(targetFile, name + "." + getTargetFileExtension());
          getLog().debug("TargetDirectory: " + targetDirectory.getAbsolutePath());
        }

        if (!targetFile.exists() || (targetFile.exists() && FileUtils.isFileNewer(sourceFile, targetFile)) || (targetFile.exists() && getXIncludeSupported())) {
          getLog().info("Processing input file: " + inputFilename);

          final XMLReader reader = factory.newSAXParser().getXMLReader();
          // configure XML reader
          reader.setEntityResolver(resolver);
          // eval PI
          final PreprocessingFilter filter = createPIHandler(resolver, reader);
          // configure SAXSource for XInclude
          final Source xmlSource = createSource(inputFilename, sourceFile, filter);

          configureXref(targetFile);

          // XSL Transformation setup
          final Transformer transformer = builder.build();
          adjustTransformer(transformer, sourceFile.getAbsolutePath(), targetFile);

          // configure the output file
          Result result = null;
          if(!shouldProcessResult()) {
            // if the output is not the main result of the transformation, ie xref database
            if(getLog().isDebugEnabled()) {
              result = new StreamResult(System.out);
            } else {
              result = new StreamResult(new NullOutputStream());
            }
          } else if (isUseStandardOutput()) {
            // if the output of the main result is the standard output
            result = new StreamResult(targetFile.getAbsolutePath());
          } else {
            // if the output of the main result is not the standard output
            if(getLog().isDebugEnabled()) {
              result = new StreamResult(System.out);
            } else {
              result = new StreamResult(new NullOutputStream());
            }
          }

          transformer.transform(xmlSource, result);

          if(shouldProcessResult()) {
            // if the transformation has produce the expected main results, we can continue
            // the chain of processing in the output mojos which can override postProcessResult
            postProcessResult(targetFile);

            if (isUseStandardOutput()) {
              getLog().info(targetFile + " has been generated.");
            } else {
              getLog().info("See " + targetFile.getParentFile().getAbsolutePath() + " for generated file(s)");
            }
          } else {
            // if the output is not the main result
            getLog().info("See " + targetFile.getParentFile().getAbsolutePath() + " for generated secondary file(s)");
          }

        } else {
          getLog().info(targetFile + " is up to date.");
        }
      } catch (SAXException saxe) {
        throw new MojoExecutionException("Failed to parse " + included[i] + ".", saxe);
      } catch (TransformerException te) {
        throw new MojoExecutionException("Failed to transform " + included[i] + ".", te);
      } catch (ParserConfigurationException pce) {
        throw new MojoExecutionException("Failed to construct parser.", pce);
      }
    }

    // userland (ant tasks) post process
    postProcess();
  }

  /**
   * Tells if the stylesheet generate any main outputs, if not the chain of processing will be
   * stopped.
   *
   * @return Returns true if the chain of processing have to continue
   */
  protected boolean shouldProcessResult() {
    String collectXrefTargets = this.getProperty("collectXrefTargets");
    if(collectXrefTargets == null) {
      return true;
    }

    return !"only".equalsIgnoreCase(collectXrefTargets);
  }

  /**
   * Creates a SAXSource configured with the desired XInclude mode. XOM library is used for advanced XInclude else
   * Xerces XInclude is used.
   *
   * @param inputFilename Is used for temp file generation (XOM)
   * @param sourceFile    The docbook source file.
   * @param filter        The XML PI filter.
   * @return An XInclude configured SAXSource
   * @throws MojoExecutionException
   */
  protected Source createSource(String inputFilename, File sourceFile, PreprocessingFilter filter)
      throws MojoExecutionException {
    // if both properties are set, XOM is used for a better XInclude support.
    if (getXIncludeSupported() && getGeneratedSourceDirectory() != null) {
      getLog().debug("Advanced XInclude mode entered");
      final Builder xomBuilder = new Builder();
      try {
        final nu.xom.Document doc = xomBuilder.build(sourceFile);
        XIncluder.resolveInPlace(doc);
        // TODO also dump PIs computed and Entities included
        final File dump = dumpResolvedXML(inputFilename, doc);
        return new SAXSource(filter, new InputSource(dump.getAbsolutePath()));
      } catch (ValidityException e) {
        throw new MojoExecutionException("Failed to validate source", e);
      } catch (ParsingException e) {
        throw new MojoExecutionException("Failed to parse source", e);
      } catch (IOException e) {
        throw new MojoExecutionException("Failed to read source", e);
      } catch (XIncludeException e) {
        throw new MojoExecutionException("Failed to process XInclude", e);
      }
    } else { // else fallback on Xerces XInclude support.
      getLog().debug("Xerces XInclude mode entered");
      final InputSource inputSource = new InputSource(sourceFile.getAbsolutePath());
      return new SAXSource(filter, inputSource);
    }
  }

  /**
   * Creates an XML Processing handler for the built-in docbkx <code>&lt;?eval?&gt;</code> PI. This PI resolves maven
   * properties and basic math formula.
   *
   * @param resolver The initial resolver to use.
   * @param reader   The source XML reader.
   * @return The XML PI filter.
   */
  private PreprocessingFilter createPIHandler(EntityResolver resolver, XMLReader reader) {
    PreprocessingFilter filter = new PreprocessingFilter(reader);
    ProcessingInstructionHandler resolvingHandler = new ExpressionHandler(new VariableResolver() {

      public Object resolveVariable(String name) throws ELException {
        if ("date".equals(name)) {
          return DateFormat.getDateInstance(DateFormat.LONG).format(new Date());
        } else if ("project".equals(name)) {
          return getMavenProject();
        } else {
          return getMavenProject().getProperties().get(name);
        }
      }

    }, getLog());
    filter.setHandlers(Arrays.asList(new Object[] { resolvingHandler }));
    filter.setEntityResolver(resolver);
    return filter;
  }

  /**
   * Creates an XML entity resolver.
   *
   * @param resolver The initial resolver to use.
   * @return The new XML entity resolver or null if there is no entities to resolve.
   * @see com.agilejava.docbkx.maven.InjectingEntityResolver
   */
  private InjectingEntityResolver createEntityResolver(EntityResolver resolver) {
    if (getEntities() != null) {
      return new InjectingEntityResolver(getEntities(), resolver, getType(), getLog());
    } else {
      return null;
    }
  }

  /**
   * Creates an URI resolver to handle <code>urn:docbkx:stylesheet(/)</code> as a special URI. This URI points to the
   * default docbook stylesheet location
   *
   * @param catalogResolver The initial resolver to use
   * @return The Stylesheet resolver.
   * @throws MojoExecutionException If an error occurs while reading the stylesheet
   */
  private URIResolver createStyleSheetResolver(CatalogResolver catalogResolver) throws MojoExecutionException {
    URIResolver uriResolver;
    try {
      URL url = getNonDefaultStylesheetURL() == null ? getDefaultStylesheetURL() : getNonDefaultStylesheetURL();
      getLog().debug("Using stylesheet: " + url.toExternalForm());
      uriResolver = new StylesheetResolver("urn:docbkx:stylesheet", new StreamSource(url.openStream(), url
          .toExternalForm()), catalogResolver);
    } catch (IOException ioe) {
      throw new MojoExecutionException("Failed to read stylesheet.", ioe);
    }
    return uriResolver;
  }

  /**
   * Returns the list of docbook files to include.
   */
  private String[] scanIncludedFiles() {
    final DirectoryScanner scanner = new DirectoryScanner();
    scanner.setBasedir(getSourceDirectory());
    scanner.setIncludes(getIncludes());
    scanner.scan();
    return scanner.getIncludedFiles();
  }

  /**
   * Saves the Docbook XML file with all XInclude resolved.
   *
   * @param initialFilename Filename of the root docbook source file.
   * @param doc             XOM Document resolved.
   * @return The new file generated.
   * @throws MojoExecutionException
   */
  protected File dumpResolvedXML(String initialFilename, nu.xom.Document doc) throws MojoExecutionException {
    final File file = new File(initialFilename);
    final String parent = file.getParent();
    File resolvedXML = null;
    if (parent != null) {
      resolvedXML = new File(getGeneratedSourceDirectory(), parent);
      resolvedXML.mkdirs();
      resolvedXML = new File(resolvedXML, "(gen)" + file.getName());
    } else {
      getGeneratedSourceDirectory().mkdirs();
      resolvedXML = new File(getGeneratedSourceDirectory(), "(gen)" + initialFilename);
    }

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(resolvedXML);
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException("Failed to open dump file", e);
    }
    if (fos != null) {
      getLog().info("Dumping to " + resolvedXML.getAbsolutePath());
      final BufferedOutputStream bos = new BufferedOutputStream(fos);
      final Serializer serializer = new Serializer(bos);

      try {
        serializer.write(doc);
        bos.flush();
        bos.close();
        fos.close();
        return resolvedXML;
      } catch (IOException e) {
        throw new MojoExecutionException("Failed to write to dump file", e);
      } finally {
        IOUtils.closeQuietly(bos);
        IOUtils.closeQuietly(fos);
      }
    }
    throw new MojoExecutionException("Failed to open dump file");
  }

  /**
   * Returns the SAXParserFactory used for constructing parsers.
   */
  private SAXParserFactory createParserFactory() {
    SAXParserFactory factory = new SAXParserFactoryImpl();
    factory.setXIncludeAware(getXIncludeSupported());
    return factory;
  }

  /**
   * Returns a boolean indicating if XInclude should be supported.
   *
   * @return A boolean indicating if XInclude should be supported.
   */
  protected abstract boolean getXIncludeSupported();

  /**
   * Returns the directory to use to save the resolved docbook XML before it is given to the Transformer.
   *
   * @return
   */
  protected abstract File getGeneratedSourceDirectory();

  /**
   * The stylesheet location override by a class in the mojo hierarchy.
   *
   * @return The location of the stylesheet set by one of the superclasses, or <code>null</code>.
   */
  protected String getNonDefaultStylesheetLocation() {
    return null;
  }

  /**
   * Returns false if the stylesheet is responsible to create the output file(s) using its own naming scheme.
   *
   * @return If using the standard output.
   */
  abstract protected boolean isUseStandardOutput();

  abstract void setUseStandardOutput(boolean useStandardOutput);

  /**
   * Returns true if the xslt engine have to print xsl:messages to standard output.
   *
   * @return If the xslt engine have to print messages.
   */
  abstract protected boolean isShowXslMessages();

  abstract protected void setShowXslMessages(boolean showXslMessages);

  /**
   * The operation to override when it is required to make some adjustments to the {@link Transformer} right before it is
   * applied to a certain source file. The two parameters provide some context, allowing implementers to respond to
   * specific conditions for specific files.
   *
   * @param transformer    The <code>Transformer</code> that must be adjusted.
   * @param sourceFilename The name of the source file that is being transformed.
   * @param targetFile     The target File.
   */
  public void adjustTransformer(Transformer transformer, String sourceFilename, File targetFile) {
    // To be implemented by subclasses.
  }

  /**
   * Allows subclasses to add their own specific pre-processing logic.
   *
   * @throws MojoExecutionException If the Mojo fails to pre-process the results.
   */
  public void preProcess() throws MojoExecutionException {
    // save system properties
    originalSystemProperties = (Properties) System.getProperties().clone();
    // set the new properties
    if (getSystemProperties() != null) {
      final Enumeration props = getSystemProperties().keys();
      while (props.hasMoreElements()) {
        final String key = (String) props.nextElement();
        System.setProperty(key, getSystemProperties().getProperty(key));
      }
    }

    if (getPreProcess() != null) {
      executeTasks(getPreProcess(), getMavenProject());
    }
  }

  /**
   * Allows classes to add their own specific post-processing logic.
   *
   * @throws MojoExecutionException If the Mojo fails to post-process the results.
   */
  public void postProcess() throws MojoExecutionException {
    if (getPostProcess() != null) {
      executeTasks(getPostProcess(), getMavenProject());
    }

    // restore system properties
    if (originalSystemProperties != null) {
      System.setProperties(originalSystemProperties);
    }
  }

  /**
   * Post-processes the file. (Might be changed in the future to except an XML representation instead of a file, in order
   * to prevent the file from being parsed.)
   *
   * @param result An individual result.
   */
  public void postProcessResult(File result) throws MojoExecutionException {

  }

  public void configureXref(File result) throws MojoExecutionException {
    // if supported by the output
    if(this.getProperty("targetsFilename") != null) {
      // creating xref db file
      File targetXrefFile = new File(result.getParentFile(), result.getName() + ".target.db");

      this.setProperty("targetsFilename", targetXrefFile.getAbsolutePath());
    }
  }

  /**
   * Creates a <code>CatalogManager</code>, used to resolve DTDs and other entities.
   *
   * @return A <code>CatalogManager</code> to be used for resolving DTDs and other entities.
   */
  protected CatalogManager createCatalogManager() {
    CatalogManager manager = new CatalogManager();
    manager.setIgnoreMissingProperties(true);
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    StringBuffer builder = new StringBuffer();
    boolean first = true;
    for (int i = 0; i < catalogs.length; i++) {
      final String catalog = catalogs[i];
      try {
        Enumeration enumeration = classLoader.getResources(catalog);
        while (enumeration.hasMoreElements()) {
          if (!first) {
            builder.append(';');
          } else {
            first = false;
          }
          URL resource = (URL) enumeration.nextElement();
          builder.append(resource.toExternalForm());
        }
      } catch (IOException ioe) {
        getLog().warn("Failed to search for catalog files: " + catalog);
        // Let's be a little tolerant here.
      }
    }

    String catalogFiles = builder.toString();
    if (catalogFiles.length() == 0) {
      getLog().warn("Failed to find catalog files.");
    } else {
      if (getLog().isDebugEnabled()) {
        getLog().debug("Catalogs to load: " + catalogFiles);
      }
      manager.setCatalogFiles(catalogFiles);
    }
    return manager;
  }

  /**
   * Creates a <code>DocumentBuilder</code> to be used to parse DocBook XML documents.
   *
   * @return A <code>DocumentBuilder</code> instance.
   * @throws MojoExecutionException If we cannot create an instance of the <code>DocumentBuilder</code>.
   */
  protected DocumentBuilder createDocumentBuilder() throws MojoExecutionException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder;
    } catch (ParserConfigurationException pce) {
      throw new MojoExecutionException("Failed to construct parser.", pce);
    }
  }

  /**
   * Creates an instance of an XPath expression for picking the title from a document.
   *
   * @return An XPath expression to pick the title from a document.
   * @throws MojoExecutionException If the XPath expression cannot be parsed.
   */
  protected XPath createTitleXPath() throws MojoExecutionException {
    try {
      StringBuffer builder = new StringBuffer();
      builder.append("(article/title|article/articleinfo/title|book/title|book/bookinfo/title)[position()=1]");
      return new DOMXPath(builder.toString());
    } catch (JaxenException je) {
      throw new MojoExecutionException("Failed to parse XPath.", je);
    }
  }

  /**
   * Constructs the default {@link TransformerBuilder}.
   */
  protected TransformerBuilder createTransformerBuilder(URIResolver resolver) {
    return new CachingTransformerBuilder(new DefaultTransformerBuilder(resolver));
  }

  /**
   * Returns the title of the document.
   *
   * @param document The document from which we want the title.
   * @return The title of the document, or <code>null</code> if we can't find the title.
   */
  private String getTitle(Document document) throws MojoExecutionException {
    try {
      XPath titleXPath = createTitleXPath();
      Node titleNode = (Node) titleXPath.selectSingleNode(document);
      if (titleNode != null) {
        return titleNode.getNodeValue();
      } else {
        return null;
      }
    } catch (JaxenException je) {
      getLog().debug("Failed to find title of document.");
      return null;
    }
  }

  /**
   * Configures and executes the given ant tasks, mainly preprocess and postprocess defined in the pom configuration.
   *
   * @param antTasks The tasks to execute
   * @param mavenProject The current maven project
   * @throws MojoExecutionException If something wrong occurs while executing the ant tasks.
   */
  protected void executeTasks(Target antTasks, MavenProject mavenProject) throws MojoExecutionException {
    try {
      ExpressionEvaluator exprEvaluator = (ExpressionEvaluator) antTasks.getProject().getReference(
          "maven.expressionEvaluator");
      Project antProject = antTasks.getProject();
      PropertyHelper propertyHelper = PropertyHelper.getPropertyHelper(antProject);
      propertyHelper.setNext(new AntPropertyHelper(exprEvaluator, getLog()));
      DefaultLogger antLogger = new DefaultLogger();
      antLogger.setOutputPrintStream(System.out);
      antLogger.setErrorPrintStream(System.err);
      antLogger.setMessageOutputLevel(2);
      antProject.addBuildListener(antLogger);
      antProject.setBaseDir(mavenProject.getBasedir());
      Path p = new Path(antProject);
      p.setPath(StringUtils.join(mavenProject.getArtifacts().iterator(), File.pathSeparator));
      antProject.addReference("maven.dependency.classpath", p);
      p = new Path(antProject);
      p.setPath(StringUtils.join(mavenProject.getCompileClasspathElements().iterator(), File.pathSeparator));
      antProject.addReference("maven.compile.classpath", p);
      p = new Path(antProject);
      p.setPath(StringUtils.join(mavenProject.getRuntimeClasspathElements().iterator(), File.pathSeparator));
      antProject.addReference("maven.runtime.classpath", p);
      p = new Path(antProject);
      p.setPath(StringUtils.join(mavenProject.getTestClasspathElements().iterator(), File.pathSeparator));
      antProject.addReference("maven.test.classpath", p);
      List artifacts = getArtifacts();
      List list = new ArrayList(artifacts.size());
      File file;
      for (Iterator i = artifacts.iterator(); i.hasNext(); list.add(file.getPath())) {
        Artifact a = (Artifact) i.next();
        file = a.getFile();
        if (file == null)
          throw new DependencyResolutionRequiredException(a);
      }

      p = new Path(antProject);
      p.setPath(StringUtils.join(list.iterator(), File.pathSeparator));
      antProject.addReference("maven.plugin.classpath", p);
      getLog().info("Executing tasks");
      antTasks.execute();
      getLog().info("Executed tasks");
    } catch (Exception e) {
      throw new MojoExecutionException("Error executing ant tasks", e);
    }
  }

  //------------ inner classes -----------

  /**
   * This output stream does nothing, it is void.
   */
  private class NullOutputStream extends OutputStream {

    public void write(int b) throws IOException {
    }
  }

  /**
   * The default policy for constructing Transformers.
   */
  private class DefaultTransformerBuilder implements TransformerBuilder {

    /**
     * The standard {@link URIResolver}.
     */
    private URIResolver resolver;

    public DefaultTransformerBuilder(URIResolver resolver) {
      this.resolver = resolver;
    }

    public Transformer build() throws TransformerBuilderException {
      Transformer transformer = createTransformer(resolver);
      transformer.setURIResolver(resolver);
      return transformer;
    }

    /**
     * Returns a <code>Transformer</code> capable of rendering a particular type of output from DocBook input.
     *
     * @param uriResolver
     * @return A <code>Transformer</code> capable of rendering a particular type of output from DocBook input.
     * @throws MojoExecutionException If the operation fails to create a <code>Transformer</code>.
     */
    protected Transformer createTransformer(URIResolver uriResolver) throws TransformerBuilderException {
      URL url = getStylesheetURL();
      try {
        TransformerFactory transformerFactory = new TransformerFactoryImpl();
        transformerFactory.setURIResolver(uriResolver);
        Source source = new StreamSource(url.openStream(), url.toExternalForm());
        Transformer transformer = transformerFactory.newTransformer(source);

        if (!isShowXslMessages()) {
          Controller controller = (Controller) transformer;
          try {
            controller.makeMessageEmitter();
            controller.getMessageEmitter().setWriter(new NullWriter());
          } catch (TransformerException te) {
            getLog().error("Failed to redirect xsl:message output.", te);
          }
        }

        configure(transformer);

        if (getCustomizationParameters() != null) {
          getLog().info("Applying customization parameters after docbkx parameters");
          final Iterator iterator = getCustomizationParameters().iterator();
          while (iterator.hasNext()) {
            Parameter param = (Parameter) iterator.next();
            if (param.getName() != null) // who knows
            {
              transformer.setParameter(param.getName(), param.getValue());
            }
          }
        }

        return transformer;
      } catch (IOException ioe) {
        throw new TransformerBuilderException("Failed to read stylesheet from " + url.toExternalForm(), ioe);
      } catch (TransformerConfigurationException tce) {
        throw new TransformerBuilderException("Failed to build Transformer from " + url.toExternalForm(), tce);
      }
    }

  }

  /**
   * Configure the Transformer by passing in some parameters.
   *
   * @param transformer The Transformer that needs to be configured.
   */
  protected abstract void configure(Transformer transformer);

  /**
   * Returns the target directory in which all results should be placed.
   *
   * @return The target directory in which all results should be placed.
   */
  protected abstract File getTargetDirectory();

  /**
   * Returns the source directory containing the source XML files.
   *
   * @return The source directory containing the source XML files.
   */
  protected abstract File getSourceDirectory();

  /**
   * Returns the include patterns, as a comma-seperate collection of patterns.
   */
  protected abstract String[] getIncludes();

  /**
   * Returns the URL of the stylesheet. You can override this operation to return a URL pointing to a stylesheet residing
   * on a location that can be adressed by a URL. By default, it will return a stylesheet that will be loaded from the
   * classpath, using the resource name returned by {@link #getStylesheetLocation()}.
   *
   * @return The URL of the stylesheet.
   */
  protected URL getStylesheetURL() {
    URL url = this.getClass().getClassLoader().getResource(getStylesheetLocation());
    if (url == null) {
      try {
        if (getStylesheetLocation().startsWith("http://")) {
          return new URL(getStylesheetLocation());
        }
        return new File(getStylesheetLocation()).toURL();
      } catch (MalformedURLException mue) {
        return null;
      }
    } else {
      return url;
    }
  }

  /**
   * Returns the URL of the default stylesheet.
   *
   * @return The URL of the stylesheet.
   */
  protected URL getNonDefaultStylesheetURL() {
    if (getNonDefaultStylesheetLocation() != null) {
      URL url = this.getClass().getClassLoader().getResource(getNonDefaultStylesheetLocation());
      return url;
    } else {
      return null;
    }
  }

  /**
   * Returns the URL of the default stylesheet.
   *
   * @return The URL of the stylesheet.
   */
  protected URL getDefaultStylesheetURL() {
    URL url = this.getClass().getClassLoader().getResource(getDefaultStylesheetLocation());
    return url;
  }

  /**
   * Returns the default stylesheet location within the root of the stylesheet distribution.
   *
   * @return The location of the directory containing the stylesheets.
   */
  protected abstract String getDefaultStylesheetLocation();

  /**
   * Returns the actual stylesheet location.
   *
   * @return The actual stylesheet location.
   */
  protected abstract String getStylesheetLocation();

  /**
   * Returns the extension of the target files, e.g. "html" for HTML files, etc.
   *
   * @return The extension of the target files.
   */
  protected abstract String getTargetFileExtension();

  /**
   * Sets the file extension, this can be usefull to override this value especially
   * for multiple transformations plugins (like XML to FO to PDF). 
   *
   * @param extension The file extension to set
   */
  protected abstract void setTargetFileExtension(String extension);

  /**
   * Returns a list of {@link Entity Entities}
   */
  protected abstract List getEntities();

  /**
   * A list of additional XSL parameters to give to the XSLT engine. These parameters overrides regular docbook ones as
   * they are last configured.<br/> For regular docbook parameters prefer the use of this plugin facilities offering
   * named paramters.<br/> These parameters feet well for custom properties you may have defined within your
   * customization layer.
   * <p/>
   * {@link Parameter customizationParameters}
   */
  protected abstract List getCustomizationParameters();

  /**
   * A copy of JVM system properties before plugin process.
   */
  private Properties originalSystemProperties;

  /**
   * Returns the additional System Properties. JVM System Properties are copied back if no problem have occurred
   * during the plugin process.
   *
   * @return The current forked System Properties.
   */
  protected abstract Properties getSystemProperties();

  /**
   * Returns the tasks that should be executed before the transformation.
   *
   * @return The tasks that should be executed before the transformation.
   */
  protected abstract Target getPreProcess();

  /**
   * Returns the tasks that should be executed after the transformation.
   *
   * @return The tasks that should be executed after the transformation.
   */
  protected abstract Target getPostProcess();

  /**
   * Returns a reference to the current project.
   *
   * @return A reference to the current project.
   */
  protected abstract MavenProject getMavenProject();

  /**
   * Returns the plugin dependencies.
   *
   * @return The plugin dependencies.
   */
  protected abstract List getArtifacts();

  /**
   * Returns the type of conversion.
   */
  protected abstract String getType();
  
  /**
   * @return <code>true</code> if execution should be skipped
   */
  protected abstract boolean isSkip();

  /**
   * Converts a String parameter to the type expected by the XSLT processor.
   */
  protected Object convertStringToXsltParam(String value) {
    return value;
  }

  /**
   * Converts a Boolean parameter to the type expected by the XSLT processor.
   */
  protected Object convertBooleanToXsltParam(String value) {
    String trimmed = value.trim();
    if ("false".equalsIgnoreCase(trimmed) || "0".equals(trimmed) || "no".equalsIgnoreCase(trimmed)) {
      return "0";
    } else {
      return "1";
    }
  }

  /**
   * Sets the value of a property of this object using introspection.
   *
   * @param propertyname The field name
   * @param value        The value
   */
  protected void setProperty(String propertyname, String value) {
    try {
      final Field f = this.getClass().getDeclaredField(propertyname);
      if (f.getType().equals(Boolean.class)) {
        f.set(this, convertBooleanToXsltParam(value));
      } else {
        f.set(this, value);
      }
    } catch (NoSuchFieldException e) {
      getLog().warn("Property not found in " + this.getClass().getName(), e);
    } catch (IllegalAccessException e) {
      getLog().warn("Unable to set " + propertyname + " value", e);
    }
  }

  /**
   * Returns the value of a property of this object using introspection.
   *
   * @param propertyname The filed name
   * @return The value
   */
  protected String getProperty(String propertyname) {
    try {
      final Field f = this.getClass().getDeclaredField(propertyname);
      Object o = f.get(this);
      if (o == null) {
        return null;
      } else {
        return o.toString();
      }
    } catch (NoSuchFieldException e) {
      getLog().warn("Property not found in " + this.getClass().getName());
    } catch (IllegalAccessException e) {
      getLog().warn("Unable to get " + propertyname + " value");
    }
    return null;
  }

}
