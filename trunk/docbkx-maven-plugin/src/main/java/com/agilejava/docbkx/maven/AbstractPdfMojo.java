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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.xml.sax.SAXException;

/**
 * A replacement base class, to be inherited by the FO building plugin. This
 * base class will generate PDF from the FO output by overriding
 * {@link #postProcessResult(File)}.
 * 
 * @author Wilfred Springer
 * 
 */
public abstract class AbstractPdfMojo extends AbstractMojoBase {
  
  private String baseUrl;
  
      /**
     * The fonts that should be taken into account. (Without this parameter, the
     * PDF document will only be able to reference the default fonts.)
     * 
     * @parameter
     */
    private Font[] fonts;

  public void postProcessResult(File result) throws MojoExecutionException {
    super.postProcessResult(result);
    
    final FopFactory fopFactory = FopFactory.newInstance();
    final FOUserAgent userAgent = fopFactory.newFOUserAgent();
    userAgent.setBaseURL(baseUrl);
    // FOUserAgent can be used to set PDF metadata

	Configuration configuration = loadFOPConfig();
    InputStream in = null;
    OutputStream out = null;
    
    try
    {
      in = openFileForInput(result);
      out = openFileForOutput(getOutputFile(result));
      fopFactory.setUserConfig(configuration);
      Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, userAgent, out);
            
      // Setup JAXP using identity transformer
      TransformerFactory factory = TransformerFactory.newInstance();
      Transformer transformer = factory.newTransformer(); // identity transformer
      
      // Setup input stream
      Source src = new StreamSource(in);

      // Resulting SAX events (the generated FO) must be piped through to FOP
      Result res = new SAXResult(fop.getDefaultHandler());
      
      // Start XSLT transformation and FOP processing
      transformer.transform(src, res);
    }
    catch (FOPException e)
    {
      throw new MojoExecutionException("Failed to convert to PDF", e);
    }
    catch (TransformerConfigurationException e)
    {
      throw new MojoExecutionException("Failed to load JAXP configuration", e);
    }
    catch (TransformerException e)
    {
      throw new MojoExecutionException("Failed to transform to PDF", e);
    }
    finally
    {
      IOUtils.closeQuietly(out);
      IOUtils.closeQuietly(in);
    }
  }

    private InputStream openFileForInput(File file)
            throws MojoExecutionException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException fnfe) {
            throw new MojoExecutionException("Failed to open " + file
                    + " for input.");
        }
    }

    private File getOutputFile(File inputFile) {
        String basename = FileUtils.basename(inputFile.getAbsolutePath());
        return new File(getTargetDirectory(), basename + "pdf");
    }

    private OutputStream openFileForOutput(File file)
            throws MojoExecutionException {
        try {
          return new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException fnfe) {
            throw new MojoExecutionException("Failed to open " + file
                    + " for output.");
        }
    }
    
    private Configuration loadFOPConfig() throws MojoExecutionException {
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream in = loader.getResourceAsStream("fonts.stg");
        Reader reader = new InputStreamReader(in);
        StringTemplateGroup group = new StringTemplateGroup(reader);
        StringTemplate template = group.getInstanceOf("config");
        template.setAttribute("fonts", fonts);
        DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
        final String config = template.toString();
        if (getLog().isDebugEnabled()) {
            getLog().debug(config);
        }
        try {
            return builder.build(IOUtils.toInputStream(config));
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to load FOP config.", ioe);
        } catch (SAXException saxe) {
            throw new MojoExecutionException("Failed to parse FOP config.",
                    saxe);
        } catch (ConfigurationException e) {
            throw new MojoExecutionException(
                    "Failed to do something Avalon requires....", e);
        }
    }

    public void adjustTransformer(Transformer transformer,
        String sourceFilename, File targetFile)
    {
      super.adjustTransformer(transformer, sourceFilename, targetFile);
      
        try
        {
          final String str = (new File(sourceFilename)).getParentFile().toURL().toExternalForm();
          baseUrl = str.replace("file:/", "file:///");
        }
        catch (MalformedURLException e)
        {
          getLog().warn("Failed to get FO basedir", e);
        }
    }
}
