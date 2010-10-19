package com.agilejava.docbkx.maven;

import com.agilejava.docbkx.maven.DocbkxPdfMojo;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

import java.io.File;
import java.util.Arrays;


public class DocbkxPdfMojoTest extends PlexusTestCase {

    DocbkxPdfMojo mojo;

    public void setUp() throws Exception {
        super.setUp();
        mojo = new DocbkxPdfMojo();
        /*container.getLoggerManager().setThreshold(Logger.LEVEL_DEBUG);
        Log mojoLogger = new DefaultLog(container.getLoggerManager().getLoggerForComponent(Mojo.ROLE));
        mojo.setLog(mojoLogger);  */
    }

    public void testLoadFOPConfigEmpty() throws MojoExecutionException {
        Configuration configuration = mojo.loadFOPConfig();
        Configuration target = configuration.getChild("target-resolution", false);
        assertNull("default configuration does not set targetResolution", target);
        Configuration source = configuration.getChild("source-resolution", false);
        assertNull("default configuration does not set sourceResolution", source);
    }

    public void testLoadFOPConfigWithParam() throws MojoExecutionException {
        mojo.sourceResolution = 10;
        mojo.targetResolution = 10;
        Configuration configuration = mojo.loadFOPConfig();

        Configuration target = configuration.getChild("target-resolution", false);
        assertNotNull("targetResolution should be set", target);

        Configuration source = configuration.getChild("source-resolution", false);
        assertNotNull("sourceResolution should be set", source);
    }

    public void testLoadFOPConfigInvalidFile() {
        mojo.externalFOPConfiguration = new File("/doesnotexist/fop.xconf");
        try {
            mojo.loadFOPConfig();
            fail("Should have failed with an invalid external fop configuration file");
        } catch (MojoExecutionException e) {

        }
    }

    public void testLoadFOPConfigValidFile() throws MojoExecutionException {
        mojo.externalFOPConfiguration = new File(PlexusTestCase.getBasedir(), "src/test/resources/fop.xconf");

        mojo.loadFOPConfig();
    }
}
