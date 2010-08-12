package com.agilejava.docbkx.support.fop;

import org.apache.fop.fonts.apps.TTFReader;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.security.Permission;

/**
 * @author Cedric Pronzato
 */
public class FOPReaderExitTest extends PlexusTestCase {

    protected void setUp() throws Exception {
        super.setUp();
        System.setSecurityManager(new NoExitSecurityManager());
    }

    protected void tearDown() throws Exception {
        System.setSecurityManager(null); // or save and restore original
        super.tearDown();
    }


    public void initMojoDefault(FontmetricsMojo mojo) {
        mojo.sourceDirectory = new File(PlexusTestCase.getBasedir(), "src/test/resources/fontsko/");
        assertTrue("Default sourceDirectory should be valid", mojo.sourceDirectory.isDirectory());
        mojo.targetDirectory = new File(PlexusTestCase.getBasedir(), "target/test/fontsko/");
        mojo.targetDirectory.mkdirs();
    }

    public void testFopExistsOnInvalidFont() throws Exception {
        File font = new File(PlexusTestCase.getBasedir(), "src/test/resources/fontsko/test.ttf");
        File metrics = new File(PlexusTestCase.getBasedir(), "target/test/fontsko/test-metrics.xml");
        metrics.getParentFile().mkdirs();

        try {
            TTFReader.main(new String[]{font.getCanonicalPath(), metrics.getCanonicalPath()});
            fail("TTFreader should have fail in loading corrupted font");
        } catch (ExitException e) {
            assertEquals("Exit status", -1, e.status);
        }
    }

    public void testFontmetricsMojoOnInvalidFont() throws Exception {
        FontmetricsMojo mojo = new FontmetricsMojo();
        initMojoDefault(mojo);
        try {
            mojo.execute();
            File expectedGen = new File(mojo.targetDirectory, "test-metrics.xml");
            assertTrue("metrics file not generated: "+expectedGen.getAbsolutePath(), !expectedGen.exists());
        } catch (ExitException e) {
            fail("FontmetricsMojo should not exits");
        }
    }

    public void testExit() throws Exception {
        try {
            System.exit(42);
        } catch (ExitException e) {
            assertEquals("Exit status", 42, e.status);
        }
    }


    //
    // classes to intercept System.exit()
    //

    protected static class ExitException extends SecurityException {
        public final int status;

        public ExitException(int status) {
            super("There is no escape!");
            this.status = status;
        }
    }


    private static class NoExitSecurityManager extends SecurityManager {

        public void checkPermission(Permission perm) {
            // allow anything.
        }

        public void checkPermission(Permission perm, Object context) {
            // allow anything.
        }

        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }
}
