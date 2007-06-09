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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * A Mojo for creating bundles from DocBook XSL distributions.
 *
 * @author Wilfred Springer
 * @goal bundle
 */
public class CreateBundleMojo extends AbstractMojo {

    /**
     * @parameter expression="${component.org.codehaus.plexus.archiver.UnArchiver#zip}
     */
    private UnArchiver unArchiver;

    /**
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}
     */
    private Archiver archiver;

    /**
     * Server Id to map on the &lt;id&gt; under &lt;server&gt; section of
     * settings.xml
     *
     * @parameter expression="${repositoryId}"
     * @required
     */
    private String repositoryId;

    /**
     * @component
     */
    private ArtifactRepositoryLayout layout;

    /**
     * URL where the artifact will be deployed. <br/> ie ( file://C:\m2-repo )
     *
     * @parameter expression="${url}"
     * @required
     */
    private String url;

    /**
     * @component
     */
    private ArtifactRepositoryFactory repositoryFactory;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @parameter expression="${component.org.apache.maven.artifact.deployer.ArtifactDeployer}"
     * @required
     * @readonly
     */
    private ArtifactDeployer deployer;

    /**
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${file}"
     * @required
     */
    private File file;

    /**
     * @parameter expression="${basedir}/target/docbkx-bundles"
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!targetDirectory.exists()) {
            FileUtils.mkdir(targetDirectory.getAbsolutePath());
        }
        System.out.println("File: " + file);
        String basename = FileUtils.basename(file.getAbsolutePath());
        ArtifactRepository deploymentRepository = repositoryFactory
                .createDeploymentArtifactRepository(repositoryId, url, layout,
                        true);
        String protocol = deploymentRepository.getProtocol();

        if ("".equals(protocol) || protocol == null) {
            throw new MojoExecutionException("No transfer protocol found.");
        }
        String groupId = "net.sf.docbook";
        String artifactId = "docbook-xsl";
        String version = basename.substring(artifactId.length() + 1);
        String packaging = "jar";
        Artifact artifact = artifactFactory.createArtifactWithClassifier(
                groupId, artifactId, version, packaging, null);
        ArtifactMetadata metadata = new ProjectArtifactMetadata(artifact,
                generatePomFile(groupId, artifactId, version, packaging));
        artifact.addMetadata(metadata);
        File jarFile = generateJarFile(file);
        try {
            if (jarFile != null) {
                deployer.deploy(jarFile, artifact, deploymentRepository,
                        localRepository);
            } else {
                getLog().error("Failed to create jar file.");
            }
        } catch (ArtifactDeploymentException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    /**
     * Generates the jar file containing all required artifacts from the
     * docbook-xsl distribution passed in.
     *
     *
     * @return A <code>File</code> object, pointing to a repackaged
     *         docbook-xsl distribution.
     */
    private File generateJarFile(File file) {
        try {
            File jarFile = FileUtils.createTempFile("docbkx", "jar", null);
            jarFile.deleteOnExit();
            File tempDir = FileUtils.createTempFile("extracted", "jar", null);
            tempDir.deleteOnExit();
            FileUtils.mkdir(tempDir.getAbsolutePath());
            unArchiver.setDestDirectory(tempDir);
            unArchiver.setSourceFile(file);
            unArchiver.extract();
            File paramFile = new File(tempDir,
                    "docbook-xsl-1.69.1/html/param.ent");
            if (!paramFile.exists()) {
                System.err.println("Entity file " + paramFile.getAbsolutePath()
                        + " does not exist.");
            } else {
                EntityFileParser.parse(new FileInputStream(paramFile),
                        new EntityFileParser.EntityVisitor() {

                            public void visitSystemEntity(String name,
                                    String systemId) {
                                System.out.println("Name: " + name);
                                System.out.println("SystemId: " + systemId);
                            }

                        });
            }
            archiver.setDestFile(jarFile);
            archiver.addDirectory(new File(tempDir, "docbook-xsl-1.69.1"),
                    new String[] { "html/**/**", "common/**/**", "lib/**/**",
                            "params/**/**" }, new String[] { "" });
            archiver.createArchive();
            return jarFile;
        } catch (IOException ioe) {

        } catch (ArchiverException ae) {
            // TODO Auto-generated catch block
            ae.printStackTrace();
        }
        return null;
    }

    private File generatePomFile(String groupId, String artifactId,
            String version, String packaging) throws MojoExecutionException {
        FileWriter fw = null;
        try {
            File tempFile = File.createTempFile("mvninstall", ".pom");
            tempFile.deleteOnExit();

            Model model = new Model();
            model.setModelVersion("4.0.0");
            model.setGroupId(groupId);
            model.setArtifactId(artifactId);
            model.setVersion(version);
            model.setPackaging(packaging);
            model.setName("DocBook XSL Stylesheets");
            model.setDescription("This project provides the DocBook XSL "
                    + "stylesheets, and repackages them in order to "
                    + "allow them to be distributed using the Maven "
                    + "repository mechanism.");

            fw = new FileWriter(tempFile);
            new MavenXpp3Writer().write(fw, model);

            return tempFile;
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Error writing temporary pom file: " + e.getMessage(), e);
        } finally {
            IOUtil.close(fw);
        }
    }
}
