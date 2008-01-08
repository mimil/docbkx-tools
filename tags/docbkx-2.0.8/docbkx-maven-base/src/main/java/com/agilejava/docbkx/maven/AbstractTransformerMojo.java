package com.agilejava.docbkx.maven;

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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.VariableResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
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

import com.icl.saxon.Controller;
import com.icl.saxon.TransformerFactoryImpl;

/**
 * The abstract Mojo base for concrete Mojos that generate some kind of output
 * format from DocBook. This Mojo will search documents in the directory
 * returned by {@link #getTargetDirectory()}, and apply the stylesheets on
 * these documents. This Mojo will be subclassed by Mojo's that generate a
 * particular type of output.
 * 
 * @author Wilfred Springer
 */
public abstract class AbstractTransformerMojo extends AbstractMojo {

    /**
     * Builds the actual output document.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        preProcess();
        File targetDirectory = getTargetDirectory();
        File sourceDirectory = getSourceDirectory();
        if (!sourceDirectory.exists()) {
            return; // No sources, so there is nothing to render.
        }
        if (!targetDirectory.exists()) {
            org.codehaus.plexus.util.FileUtils.mkdir(targetDirectory
                    .getAbsolutePath());
        }
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(sourceDirectory);
        scanner.setIncludes(getIncludes());
        scanner.scan();
        String[] included = scanner.getIncludedFiles();
        CatalogManager catalogManager = createCatalogManager();
        CatalogResolver catalogResolver = new CatalogResolver(catalogManager);
        URIResolver uriResolver = null;
        try {
            URL url = getNonDefaultStylesheetURL() == null ? getDefaultStylesheetURL()
                    : getNonDefaultStylesheetURL();
            uriResolver = new StylesheetResolver("urn:docbkx:stylesheet",
                    new StreamSource(url.openStream(), url.toExternalForm()),
                    catalogResolver);
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to read stylesheet.", ioe);
        }
        TransformerBuilder builder = createTransformerBuilder(uriResolver);
        EntityResolver resolver = catalogResolver;
        InjectingEntityResolver injectingResolver = null;
        if (getEntities() != null) {
            injectingResolver = new InjectingEntityResolver(getEntities(),
                    resolver, getType(), getLog());
            resolver = injectingResolver;
        }
        SAXParserFactory factory = createParserFactory();
        for (int i = included.length - 1; i >= 0; i--) {
            try {
                if (injectingResolver != null) {
                    injectingResolver.forceInjection();
                }
                String filename = included[i];
                String targetFilename = filename.substring(0,
                        filename.length() - 4)
                        + "." + getTargetFileExtension();
                File targetFile = new File(targetDirectory, targetFilename);
                File sourceFile = new File(sourceDirectory, filename);
                if (!targetFile.exists()
                        || (targetFile.exists() && FileUtils.isFileNewer(
                                sourceFile, targetFile))) {
                    Result result = new StreamResult(targetFile);
                    XMLReader reader = factory.newSAXParser().getXMLReader();
                    reader.setEntityResolver(resolver);
                    PreprocessingFilter filter = new PreprocessingFilter(reader);
                    ProcessingInstructionHandler resolvingHandler = new ExpressionHandler(
                            new VariableResolver() {

                                private Map tree = ExpressionUtils
                                        .createTree(getMavenProject()
                                                .getProperties());

                                public Object resolveVariable(String name)
                                        throws ELException {
                                    if ("project".equals(name)) {
                                        return getMavenProject();
                                    } else {
                                        return tree.get(name);
                                    }
                                }

                            }, getLog());
                    filter.setHandlers(Arrays
                            .asList(new Object[] { resolvingHandler }));
                    filter.setEntityResolver(resolver);
                    getLog().info("Processing " + filename);
                    SAXSource xmlSource = new SAXSource(filter,
                            new InputSource(sourceFile.getAbsolutePath()));
                    Transformer transformer = builder.build();
                    adjustTransformer(transformer, filename, targetFile);
                    transformer.transform(xmlSource, result);
                    postProcessResult(targetFile);
                } else {
                    getLog().debug(targetFile + " is up to date.");
                }
            } catch (SAXException saxe) {
                throw new MojoExecutionException("Failed to parse "
                        + included[i] + ".", saxe);
            } catch (TransformerException te) {
                throw new MojoExecutionException("Failed to transform "
                        + included[i] + ".", te);
            } catch (ParserConfigurationException pce) {
                throw new MojoExecutionException("Failed to construct parser.",
                        pce);
            }
        }
        postProcess();
    }

    /**
     * Returns the SAXParserFactory used for constructing parsers.
     * 
     */
    private SAXParserFactory createParserFactory() {
        SAXParserFactory factory = new SAXParserFactoryImpl();
        factory.setXIncludeAware(getXIncludeSupported());
        return factory;
    }

    /**
     * Returns a boolean indicting if XInclude should be supported.
     * 
     * @return A boolean indicating if XInclude should be supported.
     */
    protected abstract boolean getXIncludeSupported();

    /**
     * The stylesheet location override by a class in the mojo hierarchy.
     * 
     * @return The location of the stylesheet set by one of the superclasses, or
     *         <code>null</code>.
     */
    protected String getNonDefaultStylesheetLocation() {
        return null;
    }

    /**
     * The operation to override when it is required to make some adjustments to
     * the {@link Transformer} right before it is applied to a certain source
     * file. The two parameters provide some context, allowing implementers to
     * respond to specific conditions for specific files.
     * 
     * @param transformer
     *            The <code>Transformer</code> that must be adjusted.
     * @param sourceFilename
     *            The name of the source file that is being transformed.
     * @param targetFile
     *            The target File.
     */
    public void adjustTransformer(Transformer transformer,
            String sourceFilename, File targetFile) {
        // To be implemented by subclasses.
    }

    /**
     * Allows subclasses to add their own specific pre-processing logic.
     * 
     * @throws MojoExecutionException
     *             If the Mojo fails to pre-process the results.
     */
    public void preProcess() throws MojoExecutionException {
        if (getPreProcess() != null) {
            executeTasks(getPreProcess(), getMavenProject());
        }
    }

    /**
     * Alles classes to add their own specific post-processing logic.
     * 
     * @throws MojoExecutionException
     *             If the Mojo fails to post-process the results.
     */
    public void postProcess() throws MojoExecutionException {
        if (getPostProcess() != null) {
            executeTasks(getPostProcess(), getMavenProject());
        }
    }

    /**
     * Post-processes the file. (Might be changed in the future to except an XML
     * representation instead of a file, in order to prevent the file from being
     * parsed.)
     * 
     * @param result
     *            An individual result.
     */
    public void postProcessResult(File result) throws MojoExecutionException {

    }

    /**
     * Creates a <code>CatalogManager</code>, used to resolve DTDs and other
     * entities.
     * 
     * @return A <code>CatalogManager</code> to be used for resolving DTDs and
     *         other entities.
     */
    protected CatalogManager createCatalogManager() {
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        StringBuffer builder = new StringBuffer();
        boolean first = true;
        try {
            Enumeration enumeration = classLoader.getResources("/catalog.xml");
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
            getLog().warn("Failed to search for catalog files.");
            // Let's be a little tolerant here.
        }
        String catalogFiles = builder.toString();
        if (catalogFiles.length() == 0) {
            getLog().warn("Failed to find catalog files.");
        } else {
            manager.setCatalogFiles(catalogFiles);
        }
        return manager;
    }

    /**
     * Creates a <code>DocumentBuilder</code> to be used to parse DocBook XML
     * documents.
     * 
     * @return A <code>DocumentBuilder</code> instance.
     * @throws MojoExecutionException
     *             If we cannot create an instance of the
     *             <code>DocumentBuilder</code>.
     */
    protected DocumentBuilder createDocumentBuilder()
            throws MojoExecutionException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder;
        } catch (ParserConfigurationException pce) {
            throw new MojoExecutionException("Failed to construct parser.", pce);
        }
    }

    /**
     * Creates an instance of an XPath expression for picking the title from a
     * document.
     * 
     * @return An XPath expression to pick the title from a document.
     * @throws MojoExecutionException
     *             If the XPath expression cannot be parsed.
     */
    protected XPath createTitleXPath() throws MojoExecutionException {
        try {
            StringBuffer builder = new StringBuffer();
            builder
                    .append("(article/title|article/articleinfo/title|book/title|book/bookinfo/title)[position()=1]");
            return new DOMXPath(builder.toString());
        } catch (JaxenException je) {
            throw new MojoExecutionException("Failed to parse XPath.", je);
        }
    }

    /**
     * Constructs the default {@link TransformerBuilder}.
     */
    protected TransformerBuilder createTransformerBuilder(URIResolver resolver) {
        return new CachingTransformerBuilder(new DefaultTransformerBuilder(
                resolver));
    }

    /**
     * Returns the title of the document.
     * 
     * @param document
     *            The document from which we want the title.
     * @return The title of the document, or <code>null</code> if we can't
     *         find the title.
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

    protected void executeTasks(Target antTasks, MavenProject mavenProject)
            throws MojoExecutionException {
        try {
            ExpressionEvaluator exprEvaluator = (ExpressionEvaluator) antTasks
                    .getProject().getReference("maven.expressionEvaluator");
            Project antProject = antTasks.getProject();
            PropertyHelper propertyHelper = PropertyHelper
                    .getPropertyHelper(antProject);
            propertyHelper.setNext(new AntPropertyHelper(exprEvaluator,
                    getLog()));
            DefaultLogger antLogger = new DefaultLogger();
            antLogger.setOutputPrintStream(System.out);
            antLogger.setErrorPrintStream(System.err);
            antLogger.setMessageOutputLevel(2);
            antProject.addBuildListener(antLogger);
            antProject.setBaseDir(mavenProject.getBasedir());
            Path p = new Path(antProject);
            p.setPath(StringUtils.join(mavenProject.getArtifacts().iterator(),
                    File.pathSeparator));
            antProject.addReference("maven.dependency.classpath", p);
            p = new Path(antProject);
            p.setPath(StringUtils.join(mavenProject
                    .getCompileClasspathElements().iterator(),
                    File.pathSeparator));
            antProject.addReference("maven.compile.classpath", p);
            p = new Path(antProject);
            p.setPath(StringUtils.join(mavenProject
                    .getRuntimeClasspathElements().iterator(),
                    File.pathSeparator));
            antProject.addReference("maven.runtime.classpath", p);
            p = new Path(antProject);
            p.setPath(StringUtils.join(mavenProject.getTestClasspathElements()
                    .iterator(), File.pathSeparator));
            antProject.addReference("maven.test.classpath", p);
            List artifacts = getArtifacts();
            List list = new ArrayList(artifacts.size());
            File file;
            for (Iterator i = artifacts.iterator(); i.hasNext(); list.add(file
                    .getPath())) {
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
         * Returns a <code>Transformer</code> capable of rendering a
         * particular type of output from DocBook input.
         * 
         * @param uriResolver
         * 
         * @return A <code>Transformer</code> capable of rendering a
         *         particular type of output from DocBook input.
         * @throws MojoExecutionException
         *             If the operation fails to create a
         *             <code>Transformer</code>.
         */
        protected Transformer createTransformer(URIResolver uriResolver)
                throws TransformerBuilderException {
            URL url = getStylesheetURL();
            try {
                TransformerFactory transformerFactory = new TransformerFactoryImpl();
                transformerFactory.setURIResolver(uriResolver);
                Source source = new StreamSource(url.openStream(), url
                        .toExternalForm());
                Transformer transformer = transformerFactory
                        .newTransformer(source);
                Controller controller = (Controller) transformer;
                try {
                    controller.makeMessageEmitter();
                    controller.getMessageEmitter().setWriter(new NullWriter());
                } catch (TransformerException te) {
                    getLog()
                            .error("Failed to redirect xsl:message output.", te);
                }
                configure(transformer);
                return transformer;
            } catch (IOException ioe) {
                throw new TransformerBuilderException(
                        "Failed to read stylesheet from "
                                + url.toExternalForm(), ioe);
            } catch (TransformerConfigurationException tce) {
                throw new TransformerBuilderException(
                        "Failed to build Transformer from "
                                + url.toExternalForm(), tce);
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
     * Returns the URL of the stylesheet. You can override this operation to
     * return a URL pointing to a stylesheet residing on a location that can be
     * adressed by a URL. By default, it will return a stylesheet that will be
     * loaded from the classpath, using the resource name returned by
     * {@link #getStylesheetLocation()}.
     * 
     * @return The URL of the stylesheet.
     */
    protected URL getStylesheetURL() {
        URL url = this.getClass().getClassLoader().getResource(
                getStylesheetLocation());
        if (url == null) {
            try {
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
            URL url = this.getClass().getClassLoader().getResource(
                    getNonDefaultStylesheetLocation());
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
        URL url = this.getClass().getClassLoader().getResource(
                getDefaultStylesheetLocation());
        return url;
    }

    /**
     * Returns the default stylesheet location within the root of the stylesheet
     * distribution.
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
     * Returns the extension of the target files, e.g. "html" for HTML files,
     * etc.
     * 
     * @return The extension of the target files.
     */
    protected abstract String getTargetFileExtension();

    /**
     * Returns a list of {@link Entity Entities}
     */
    protected abstract List getEntities();

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

}
