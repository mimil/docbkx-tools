package com.agilejava.docbkx.maven;

import junit.framework.TestCase;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class AbstractTransformerMojoTest extends AbstractMojoTestCase {

    public void ZzzzztestCreateCatalogManagerNoDep() throws Exception
    {
        final File pluginXml = new File(getBasedir(), "src/test/resources/catalog/pom-nodep.xml");
        AbstractTransformerMojo mojo = (AbstractTransformerMojo)lookupMojo("generate-pdf", pluginXml);
        assertNotNull("Unable to find requested mojo", mojo);
        CatalogManager manager = mojo.createCatalogManager();
        Catalog catalog = manager.getCatalog();
        catalog.parseAllCatalogs();

        // should find the docbook XSL or is it needed to add docbook xsl as dependency?
        findDocbookXSLCatalog(catalog);

    }

    private void findDocbookXSLCatalog(Catalog catalog) throws Exception {
        findInCatalog(catalog, "http://docbook.sourceforge.net/release/xsl-ns/current");
    }

    private void findInCatalog(Catalog catalog, String uri) throws Exception {

        try {
            String res = catalog.resolveURI(uri);
            if(res == null || res.length() == 0)
            {
                throw new Exception("Unable to resolve URI: "+uri);

            }
            res = catalog.resolveSystem(uri);
            if(res == null || res.length() == 0)
            {
                 throw new Exception("Unable to resolve System: "+uri);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
