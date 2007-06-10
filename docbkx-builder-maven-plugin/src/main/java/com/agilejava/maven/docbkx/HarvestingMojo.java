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

package com.agilejava.maven.docbkx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.util.SelectorUtils;

import com.agilejava.maven.docbkx.ZipFileProcessor.ZipEntryVisitor;

/**
 * A Mojo for extracting the relevant bits of the DocBook XSL stylesheets and
 * include them as resources.
 *
 * @author Wilfred Springer
 * @phase generate-resources
 * @goal harvest
 */
public class HarvestingMojo extends AbstractBuilderMojo {

    /**
     * The maven project helper class for adding resources.
     *
     * @parameter expression="${component.org.apache.maven.project.MavenProjectHelper}"
     */
    private MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * @parameter expression="${basedir}/target/generated-resources"
     */
    private File targetDirectory;

    protected void process(ZipFileProcessor processor)
            throws MojoExecutionException {
        try {
            FileUtils.forceMkdir(targetDirectory);
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to create "
                    + targetDirectory.getAbsolutePath());
        }
        final String[] includes = getIncludes();
        try {
            processor.process(new ZipEntryVisitor() {
                public void visit(ZipEntry entry, InputStream in)
                        throws IOException {
                    for (int i = 0; i < includes.length; i++) {
                        if (SelectorUtils.match(includes[i], entry.getName())) {
                            String targetFilename = entry.getName().substring(
                                    entry.getName().indexOf('/') + 1);
                            File targetFile = new File(targetDirectory,
                                    targetFilename);
                            if (!targetFile.exists()) {
                                (targetFile.getParentFile()).mkdirs();
                                OutputStream out = null;
                                try {
                                    out = new FileOutputStream(targetFile);
                                    IOUtils.copy(in, out);
                                } finally {
                                    IOUtils.closeQuietly(out);
                                }
                            }
                            break;
                        }
                    }

                }
            });
        } catch (IOException ioe) {
            throw new MojoExecutionException("Failed to copy files.", ioe);
        }
        projectHelper.addResource(project, targetDirectory.getAbsolutePath(),
                Collections.singletonList("**/**"), Collections.EMPTY_LIST);
    }

    private String[] getIncludes() {
        return new String[] { "*/VERSION", "*/" + getType() + "/**",
                "*/common/**", "*/lib/**", "*/params/**" };
    }

}
