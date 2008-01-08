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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

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

    /**
     * The fonts that should be taken into account. (Without this parameter, the
     * PDF document will only be able to reference the default fonts.)
     * 
     * @parameter
     */
    private Font[] fonts;

    public void postProcessResult(File result) throws MojoExecutionException {
        super.postProcessResult(result);
        Configuration configuration = loadFOPConfig();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = openFileForInput(result);
            out = openFileForOutput(getOutputFile(result));
            FopFactory fopFactory = FopFactory.newInstance();
            fopFactory.setUserConfig(configuration);
            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            Source src = new StreamSource(in);
            Result res = new SAXResult(fop.getDefaultHandler());
            transformer.transform(src, res);
        } catch (FOPException fope) {
            throw new MojoExecutionException("Failed to create FopFactory.",
                    fope);
        } catch (TransformerConfigurationException tce) {
            throw new MojoExecutionException("Failed to create Taransformer.",
                    tce);
        } catch (TransformerException te) {
            throw new MojoExecutionException("Failed to transform document.",
                    te);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
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
            return new FileOutputStream(file);
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

}
