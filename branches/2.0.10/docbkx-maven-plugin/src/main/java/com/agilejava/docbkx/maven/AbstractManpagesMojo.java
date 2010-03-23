package com.agilejava.docbkx.maven;

import java.io.File;

import javax.xml.transform.Transformer;

/**
 * The base class of a Mojo generating Man documentation.
 *
 * @author Cedric Pronzato
 *
 */
public abstract class AbstractManpagesMojo extends AbstractTransformerMojo {

    /**
     * {@inheritDoc} This implementation will set the root.filename property,
     * based on the targetFile's name. It will also set the
     * <code>manifest.in.base.dir</code> to a value different than '0', in
     * order to make sure that the Eclipse files are not getting generated to
     * Maven's basedir, but to the target directory instead.
     */
    public void adjustTransformer(Transformer transformer,
            String sourceFilename, File targetFile) {
        super.adjustTransformer(transformer, sourceFilename, targetFile);
        String rootFilename = targetFile.getName();
        rootFilename = rootFilename.substring(0, rootFilename.lastIndexOf('.'));
        transformer.setParameter("root.filename", rootFilename);
        transformer.setParameter("base.dir", targetFile.getParent()
                + File.separator);
        transformer.setParameter("man.output.in.separate.dir", "1");
        transformer.setParameter("man.output.subdirs.enabled", "1");
        transformer.setParameter("man.output.base.dir", targetFile.getParent()
                + File.separator);
    }

}