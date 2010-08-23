package com.agilejava.docbkx.support.fop;


import org.codehaus.plexus.PlexusTestCase;

import java.io.File;


/**
 * @author Cedric Pronzato
 */
public class FontmetricsMojoTest extends PlexusTestCase {


    public void initMojoDefault(FontmetricsMojo mojo)
    {
        mojo.sourceDirectory = new File(PlexusTestCase.getBasedir(), "src/test/resources/fonts/");
        assertTrue("Default sourceDirectory should be valid", mojo.sourceDirectory.isDirectory());
        mojo.targetDirectory = new File(PlexusTestCase.getBasedir(), "target/test/fonts/");
        mojo.targetDirectory.mkdirs();
    }

    public void validateGen(FontmetricsMojo mojo)
    {
        File expectedGen = new File(mojo.targetDirectory, "Pecita-metrics.xml");
        assertTrue("metrics file not generated: "+expectedGen.getAbsolutePath(), expectedGen.exists());
        // space in font name
        expectedGen = new File(mojo.targetDirectory, "Pe cita-metrics.xml");
        assertTrue("metrics file not generated: "+expectedGen.getAbsolutePath(), expectedGen.exists());
    }
    public void testFontmetricsGen() throws Exception
    {
        FontmetricsMojo mojo = new  FontmetricsMojo();
        initMojoDefault(mojo);
        mojo.execute();
        
        validateGen(mojo);
    }


    public void testFontmetricsGenSpaceDir() throws Exception
    {
        FontmetricsMojo mojo = new  FontmetricsMojo();
        initMojoDefault(mojo);
        mojo.targetDirectory = new File(PlexusTestCase.getBasedir(), "target/test/fon ts/");
        mojo.targetDirectory.mkdirs();
        mojo.execute();

        validateGen(mojo);
    }
}
