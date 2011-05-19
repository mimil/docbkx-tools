package com.agilejava.docbkx.maven;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

import org.codehaus.plexus.configuration.PlexusConfiguration;

import com.agilejava.docbkx.maven.DocbkxPdfMojo;

import junit.framework.TestCase;

/**
 * 
 */
public class AbstractTransformerMojoTest extends AbstractMojoTestCase {
  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  protected void setUp() throws Exception {
    // required for mojo lookups to work
    super.setUp();
  }

  /**
   * DOCUMENT ME!
   */
  public void testNothing() {
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  public void ZzzztestCreateCatalogManagerNoDep() throws Exception {
    final File pluginXml = new File(getBasedir(), "src/test/resources/catalog/pom-nodep.xml");

    //AbstractTransformerMojo mojo = (AbstractTransformerMojo)lookupMojo("generate-pdf", pluginXml);
    DocbkxPdfMojo mojo = new DocbkxPdfMojo();
    PlexusConfiguration pluginConfiguration = extractPluginConfiguration("docbkx-maven-plugin", pluginXml);
    configureMojo(mojo, pluginConfiguration);

    //assertNotNull("Unable to find requested mojo", mojo);
    mojo.execute();

    CatalogManager manager = mojo.createCatalogManager();

    //Catalog catalog = manager.getCatalog();
    //catalog.parseAllCatalogs();

    // should find the docbook XSL or is it needed to add docbook xsl as dependency?
    //findDocbookXSLCatalog(catalog);
    //printClassPath(mojo);
  }

  private void printClassPath(AbstractTransformerMojo mojo) {
    ClassLoader loader = mojo.getClass().getClassLoader();
    URL[] urls = ((URLClassLoader) loader).getURLs();

    for (int i = 0; i < urls.length; i++) {
      System.out.println(urls[i].getFile());
    }
  }

  private void findDocbookXSLCatalog(Catalog catalog) throws Exception {
    findInCatalog(catalog, "http://docbook.sourceforge.net/release/xsl-ns/current");
  }

  private void findInCatalog(Catalog catalog, String uri) throws Exception {
    try {
      String res = catalog.resolveURI(uri);

      if ((res == null) || (res.length() == 0)) {
        throw new Exception("Unable to resolve URI: " + uri);
      }

      res = catalog.resolveSystem(uri);

      if ((res == null) || (res.length() == 0)) {
        throw new Exception("Unable to resolve System: " + uri);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
