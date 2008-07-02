package com.agilejava.docbkx.maven;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The base class of all other mojos. Introduced to add some common behaviour,
 * outside of the {@link AbstractTransformerMojo}.
 *
 * @author Wilfred Springer
 */
public abstract class AbstractMojoBase extends AbstractTransformerMojo {

    /**
     * Points to the XSLT Highlighting configuration file.
     *
     * @parameter
     */
    private String xslthlConfig;

    public void preProcess() throws MojoExecutionException {
        super.preProcess();
        configureXslthl();
    }

    private void configureXslthl() {
        URL url = this.getClass().getClassLoader().getResource(
                "META-INF/docbkx/highlighting/xslthl-config.xml");

        if (xslthlConfig != null) {
            // if the plugin parameter is set
            url = convertToUrl(xslthlConfig);
        } else {
            final String xslthlSysProp = System.getProperty("xslthl.config");
            if (xslthlSysProp != null) {
                // else use the current system property
                getLog().info("xslthl.config system property already set");
                convertToUrl(xslthlSysProp);
            }
            // else fallback on docbkx distrib file
        }

        if (url == null) {
            getLog().error("Error while converting XSLTHL config file");
        } else {
            System.setProperty("xslthl.config", url.toExternalForm());
        }
    }

    /**
     * Converts a conventional path to url format bzcause XSLTHL only takes as input a configuration file path given
     * as an url.
     *
     * @param path The path to format.
     * @return The formated path or null if an error occurred.
     * @throws IllegalArgumentException If the input path is null.
     */
    private URL convertToUrl(String path) {
        if(path == null) {
            throw new IllegalArgumentException("Config file path must not be null");
        }
        final String s = path.replace("file:///", "");
        final File file = new File(s);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            getLog().warn("The given XSLTHL config file seems to not be legal: " + path);
        } else {
            try {
                return file.toURL();
            } catch (MalformedURLException e) {
                getLog().error(e);
            }
        }
        return null;
    }

}
