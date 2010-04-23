package com.agilejava.docbkx.maven;

import junit.framework.TestCase;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.xml.resolver.CatalogManager;

import java.io.File;

/**
 *
 */
public class AbstractTransformerMojoTest extends AbstractMojoTestCase {

    public void ZzzztestCreateCatalogManagerNoDep() throws Exception
    {
        final File pluginXml = new File(getBasedir(), "src/test/resources/catalog/pom-nodep.xml");
        AbstractTransformerMojo mojo = (AbstractTransformerMojo)lookupMojo("generate-pdf", pluginXml);
        assertNotNull("Unable to find requested mojo", mojo);
        CatalogManager manager = mojo.createCatalogManager();
        int nbFiles = manager.getCatalogFiles().size();
        assertEquals("No catalog files should be found", 0 , nbFiles);
    }

}
