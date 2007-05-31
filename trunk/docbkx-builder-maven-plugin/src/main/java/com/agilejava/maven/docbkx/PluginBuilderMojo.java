package com.agilejava.maven.docbkx;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import com.agilejava.maven.docbkx.spec.Specification;

/**
 * A Maven plugin for generating a DocBook XSL plugin with specific setters and
 * getters for the parameters in the specific distribution of the DocBook XSL
 * stylesheets.
 * 
 * @author Wilfred Springer
 * @goal generate
 * @phase generate-sources
 */
public class PluginBuilderMojo extends AbstractBuilderMojo {

    /**
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * The classname of the Mojo that wil provide the desired functionality.
     * 
     * @parameter
     */
    private String className;

    /**
     * The package name of the Mojo that will provide the desired functionality.
     * 
     * @parameter
     */
    private String packageName;

    /**
     * The classname of the super class from which the generate Mojo will
     * inherit.
     * 
     * @parameter expression="com.agilejava.docbkx.maven.AbstractTransformerMojo"
     */
    private String superClassName;

    /**
     * The suffix to be used in the generated plugin.
     * 
     * @parameter
     */
    private String pluginSuffix;

    /**
     * Target directory.
     * 
     * @parameter expression="${project.build.directory}/generated-sources"
     */
    private File targetDirectory;

    private String getPackageName() {
        return packageName != null ? packageName : getGroupId();
    }

    /**
     * Returns the name of the class that will be used. If {@link #className} is
     * set, then that name will be used instead of the default.
     * 
     * @return The name of the class for the Mojo being generated.
     */
    private String getClassName() {
        if (className == null) {
            StringBuffer builder = new StringBuffer();
            builder.append("Docbkx");
            builder.append(Character.toUpperCase(getType().charAt(0)));
            builder.append(getType().substring(1));
            builder.append("Mojo");
            return builder.toString();
        } else {
            return className;
        }
    }

    protected void process(ZipFile zipFile) throws MojoExecutionException,
            MojoFailureException {
        File sourcesDir = new File(targetDirectory, getPackageName().replace(
                '.', '/'));
        try {
            FileUtils.forceMkdir(sourcesDir);
        } catch (IOException ioe) {
            throw new MojoExecutionException(
                    "Can't create directory for sources.", ioe);
        }
        ClassLoader loader = this.getClass().getClassLoader();
        InputStream in = loader.getResourceAsStream("plugins.stg");
        Reader reader = new InputStreamReader(in);
        StringTemplateGroup group = new StringTemplateGroup(reader);
        StringTemplate template = group.getInstanceOf("plugin");
        File targetFile = new File(sourcesDir, getClassName() + ".java");
        Specification specification = null;
        try {
            specification = createSpecification(zipFile);
            getLog().info(
                    "Number of parameters: "
                            + specification.getParameters().size());
            template.setAttribute("spec", specification);
            FileUtils.writeStringToFile(targetFile, template.toString(), null);
        } catch (IOException ioe) {
            if (specification == null) {
                throw new MojoExecutionException("Failed to read parameters.",
                        ioe);
            } else {
                throw new MojoExecutionException("Failed to create "
                        + targetFile + ".", ioe);
            }
        }
        project.addCompileSourceRoot(targetDirectory.getAbsolutePath());
    }

    private Specification createSpecification(ZipFile zipFile)
            throws IOException {
        Specification specification = extractSpecification(zipFile);
        specification.setType(getType());
        if (getStylesheetLocation() != null) {
            specification.setStylesheetLocation(getStylesheetLocation());
        } else {
            specification.setStylesheetLocation(getStylesheetRoot() + "/"
                    + getType() + "/docbook.xsl");
        }
        return specification;
    }

    protected Specification extractSpecification(ZipFile zipFile)
            throws IOException {
        File rootDir = zipFile.getExtractedFileRoot();
        File docBookXslDir = new File(rootDir, getDocBookXslPrefix());
        File paramEntities = new File(docBookXslDir, getType() + "/param.ent");
        List parameters = extractParameters(paramEntities);
        Specification specification = new Specification();
        specification.setPameters(parameters);
        specification.setClassName(getClassName());
        specification.setSuperClassName(superClassName);
        specification.setPackageName(getPackageName());
        specification.setDocbookXslVersion(getVersion());
        specification.setPluginSuffix(pluginSuffix);
        return specification;
    }

}
