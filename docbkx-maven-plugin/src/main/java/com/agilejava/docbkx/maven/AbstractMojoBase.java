package com.agilejava.docbkx.maven;

import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * The base class of all other mojos. Introduced to add some common behaviour,
 * outside of the {@link AbstractTransformerMojo}.
 * 
 * @author Wilfred Springer
 * 
 */
public abstract class AbstractMojoBase extends AbstractTransformerMojo {

	public void preProcess() throws MojoExecutionException {
		super.preProcess();
		configureXslthl();
	}

	private void configureXslthl() {
		URL url = this.getClass().getClassLoader().getResource(
				"META-INF/docbkx/highlighting/xslthl-config.xml");
		System.setProperty("xslthl.config", url.toExternalForm());
	}

}
