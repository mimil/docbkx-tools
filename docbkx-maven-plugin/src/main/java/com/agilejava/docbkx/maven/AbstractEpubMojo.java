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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ZipFileSet;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.archiver.zip.ZipFile;
import org.codehaus.plexus.util.FileUtils;

import javax.xml.transform.Transformer;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A dedicated base class for plugins generating ePub output, in order to allow
 * the specific stylesheet chosen to be dependent on the {@link #chunkedOutput}
 * property.
 *
 * @author Cedric Pronzato
 * @author Brian Richard Jackson
 */
public abstract class AbstractEpubMojo extends AbstractMojoBase {


    /**
     * The Zip archiver.
     *
     * @component role="org.codehaus.plexus.archiver.Archiver" roleHint="zip"
     * @required
     */
    private ZipArchiver zipArchiver;

    private File targetFile;


    /**
     * {@inheritDoc} This implementation will set the root.filename property,
     * based on the targetFile's name.
     */
    public void adjustTransformer(Transformer transformer,
                                  String sourceFilename, File targetFile) {
        super.adjustTransformer(transformer, sourceFilename, targetFile);

        /*  if(isValidCss()) {
        transformer.setParameter("html.stylesheet", htmlStylesheet.getName());
    }    */


        String rootFilename = targetFile.getName();
        rootFilename = rootFilename.substring(0, rootFilename
                .lastIndexOf('.'));
        transformer.setParameter("root.filename", rootFilename);
        transformer.setParameter("base.dir", targetFile.getParent()
                + File.separator);
        transformer.setParameter("epub.oebps.dir", targetFile.getParent()
                + File.separator);
        transformer.setParameter("epub.metainf.dir", targetFile.getParent()
                + File.separator + "META-INF" + File.separator);

        this.targetFile = targetFile;
    }

    public void postProcess() throws MojoExecutionException {
        super.postProcess();

        // Setup Ant tasks
        AntSetup antSetup = new AntSetup().invoke();
        Project project = antSetup.getProject();
        Target target = antSetup.getTarget();

        // TODO: temporary trick, svn version of docbook xsl epub stylesheet works differently
        try {
            final URL containerURL = getClass().getResource("/epub/container.xml");
            FileUtils.copyURLToFile(containerURL, new File(targetFile.getParentFile(), "META-INF" + File.separator + "container.xml"));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy hardcoded container.xml file", e);
        }

        // copy mimetype file
        try {
            final URL mimetypeURL = getClass().getResource("/epub/mimetype");
            FileUtils.copyURLToFile(mimetypeURL, new File(targetFile.getParentFile(), "mimetype"));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to copy hardcoded mimetype file", e);
        }


        try {
            zipArchiver.addDirectory(targetFile.getParentFile());
            zipArchiver.setCompress(true); // seems to not be a problem to have mimetype compressed
            zipArchiver.setDestFile(targetFile);
            zipArchiver.createArchive();

          /*  final File css = getCss();
            if(css != null)
            {
                zipArchiver.addFile(css, "");
            }

            final File font = getFontFile();
                  */
            /*zipArchiver2.setCompress(false);
            zipArchiver2.addFile(new File(getTargetDirectory(),"mimetype"), "mimetype");
            zipArchiver2.setDestFile(new File(getMavenProject().getBasedir(), "epub.epub"));
            zipArchiver2.setUpdateMode(true);
            zipArchiver2.createArchive();    */
            getLog().debug("epub file created at: "+zipArchiver.getDestFile().getAbsolutePath());
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to zip epub file", e);
        }
        // Run the Ant tasks
        //executeTasks(target, getMavenProject());
    }

    private File getCss() {
        final String css = getProperty("htmlStylesheet");
        if (css != null) {
            final File f = new File(css);
            if (!f.exists()) {
                getLog().warn("Unable to find specified css: " + f.getAbsolutePath());
                return null;
            }

            return f;
        }
        return null;
    }

    private File getFontFile() {
        final String font = getProperty("epubEmbeddedFont");
        if (font != null) {
            final File f = new File(font);
            if (!f.exists()) {
                getLog().warn("Unable to find specified font: " + f.getAbsolutePath());
                return null;
            }
            if (!font.endsWith("otf")) {
                getLog().warn("Only otf font is supported: " + font);
                return null;
            }

            return f;
        }
        return null;
    }

    /* public void postProcess() throws MojoExecutionException {
super.postProcess();


// Setup Ant tasks
AntSetup antSetup = new AntSetup().invoke();
Project project = antSetup.getProject();
Target target = antSetup.getTarget();

// TODO: Delete the result file (will be empty for ePub, since the entire XSLT is chunked to different files)
Delete delete = new Delete();
delete.setDir(getTargetDirectory());      */
    // delete.setIncludes("**/*.epub");
    /*   initTask(delete, project, target);

// Strip absolute paths from all files under getTargetDirectory()
// HACK: Due to a pretty nasty bug in the version Saxon that Docbook uses, we
//       need to strip any absolute paths in the files
Replace replace = new Replace();
replace.setDir(getTargetDirectory());
replace.setToken(withTrailingSeparator(getTargetDirectory().getPath()));
initTask(replace, project, target);

// Create the mimetype file
Echo echo = new Echo();
final File mimetypeFile = new File(getTargetDirectory(), "mimetype");
echo.setFile(mimetypeFile);
echo.setMessage("application/epub+zip");
initTask(echo, project, target);

// Create the zip file
Zip zip = new Zip();
zip.setDestFile(bookFile);
final FileSet mimetypeFileSet = new FileSet();
mimetypeFileSet.setFile(mimetypeFile);
zip.addFileset(mimetypeFileSet);

final ZipFileSet metaFileSet = new ZipFileSet();
metaFileSet.setDir(new File(getTargetDirectory(), metainfDir));
metaFileSet.setPrefix(metainfDir);
zip.addZipfileset(metaFileSet);

final ZipFileSet oebpsFileSet = new ZipFileSet();
oebpsFileSet.setDir(new File(getTargetDirectory(), oebpsDir));
oebpsFileSet.setPrefix(oebpsDir);
zip.addZipfileset(oebpsFileSet);

if(isValidFont()) {
final FileSet fontFileSet = new FileSet();
fontFileSet.setFile(font);
zip.addFileset(fontFileSet);
}

if(isValidCss()) {
final FileSet cssFileSet = new FileSet();
cssFileSet.setFile(htmlStylesheet);
zip.addFileset(cssFileSet);
}

if(imagesDir != null && imagesDir.exists()) {
final ZipFileSet imagesFileSet = new ZipFileSet();
imagesFileSet.setDir(imagesDir);
imagesFileSet.setPrefix(oebpsDir + "/images");
// TODO: Would be nice to only include referenced images
zip.addZipfileset(imagesFileSet);
}

initTask(zip, project, target);

// Run the Ant tasks
executeTasks(target, getMavenProject());
}

private String withTrailingSeparator(String path) {
return path == null || path.endsWith(File.separator)
    ? path
    : path + File.separator;
}
              */

    private void initTask(Task task, Project project, Target target) {
        task.setProject(project);
        task.setOwningTarget(target);
        task.init();
        target.addTask(task);
    }

    private class AntSetup {
        private Project project;
        private Target target;

        public Project getProject() {
            return project;
        }

        public Target getTarget() {
            return target;
        }

        public AntSetup invoke() {
            project = new Project();
            project.setName("DummyProject");

            target = new Target();
            target.setName("");
            target.setProject(project);
            project.addTarget(target);
            return this;
        }
    }
}
