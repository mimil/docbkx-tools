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
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;

/**
 * A replacement base class, to be inherited by the FO building plugin. This
 * base class will generate PDF from the FO output by overriding
 * {@link #postProcessResult(File)}.
 * 
 * @author Wilfred Springer
 * 
 */
public abstract class AbstractPdfMojo extends AbstractMojoBase {

    public void postProcessResult(File result) throws MojoExecutionException {
        super.postProcessResult(result);
        InputStream in = null;
        OutputStream out = null;
        try {
            in = openFileForInput(result);
            out = openFileForOutput(getOutputFile(result));
            Logger logger = new AvalonMavenBridgeLogger(getLog(), true, true);
            Fop fop = FopFactory.newInstance().newFop(MimeConstants.MIME_PDF,
                    out);
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

}
