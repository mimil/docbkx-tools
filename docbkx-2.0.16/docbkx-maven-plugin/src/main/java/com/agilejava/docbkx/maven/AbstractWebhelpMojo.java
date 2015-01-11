/*
 * #%L
 * Docbkx Maven Plugin
 * %%
 * Copyright (C) 2006 - 2014 Wilfred Springer, Cedric Pronzato
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.agilejava.docbkx.maven;

/*
 * Copyright Cedric Pronzato
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.net.URL;

import java.util.*;

import javax.xml.transform.Transformer;

import org.apache.maven.plugin.MojoExecutionException;

import com.nexwave.nquindexer.SaxHTMLIndex;
import com.nexwave.nquindexer.WriteJSFiles;

import com.nexwave.nsidita.DirList;
import com.nexwave.nsidita.DocFileInfo;

/**
 * DOCUMENT ME! based on IndexerTask.java from docbook xsl (webhelpindexer module)
 */
public abstract class AbstractWebhelpMojo extends AbstractMojoBase {
  //File name initialization
  private static final String HTML_INFO_LIST = "htmlFileInfoList.js";
  String indexName = ".js";

  // Init the lists which will contain the words and chars to remove
  ArrayList cleanUpStrings = null;
  ArrayList cleanUpChars = null;
  Map tempDico = null;
  File targetBaseDir = null;
  File searchBaseDir = null;

  /**
   * The directory containing the webhelp template or null if using the template provided by
   * docbook xsl distribution.
   *
   * @parameter
   */
  private File templateDirectory;

    /**
     * Whether to perform stemming or not
     *
     * @parameter default-value="true"
     */
    private boolean stemming;

  /**
   * DOCUMENT ME!
   *
   * @param transformer DOCUMENT ME!
   * @param sourceFilename DOCUMENT ME!
   * @param targetFile DOCUMENT ME!
   */
  public void adjustTransformer(Transformer transformer, String sourceFilename, File targetFile) {
    super.adjustTransformer(transformer, sourceFilename, targetFile);

    String rootFilename = "index.html";
    rootFilename = rootFilename.substring(0, rootFilename.lastIndexOf('.'));
    transformer.setParameter("root.filename", rootFilename);
    transformer.setParameter("webhelp.base.dir", targetFile.getParent() + File.separator);
    targetBaseDir = targetFile.getParentFile();
    searchBaseDir = new File(targetBaseDir, "search");
    cleanUpStrings = new ArrayList();
    cleanUpChars = new ArrayList();
    tempDico = new HashMap();
  }

  /**
   * DOCUMENT ME!
   *
   * @throws MojoExecutionException DOCUMENT ME!
   */
  protected void copyTemplate() throws MojoExecutionException {
    try {
      if (templateDirectory == null) {
        getLog().debug("Copying template from docbook xsl release");

        this.copyCommonFromXslRelease();
        this.copySearchFromXslRelease();

      } else {
        getLog().debug("Copying template from custom directory: " + templateDirectory.getAbsolutePath());
        FileUtils.copyResourcesRecursively(templateDirectory.toURL(), targetBaseDir);
      }
    } catch (Exception e) {
      throw new MojoExecutionException("Unable to copy template", e);
    }
  }

  protected void copyCommonFromXslRelease() throws MojoExecutionException {
    try {
            URL url = this.getClass().getClassLoader().getResource("docbook/webhelp/template/common");
            FileUtils.copyResourcesRecursively(url, new File(targetBaseDir, "common"));
    } catch (Exception e) {
        throw new MojoExecutionException("Unable to copy common template from XSL release", e);
    }
   }

    protected void copySearchFromXslRelease() throws MojoExecutionException {
        try {
            this.copyOneSearchItemFromXslRelease("nwSearchFnt.js");
            this.copyOneSearchItemFromXslRelease("stemmers/" + "en_stemmer.js");
        } catch (Exception e) {
            throw new MojoExecutionException("Unable to copy search template from XSL release", e);
        }
    }

    protected void copyOneSearchItemFromXslRelease(String relativePath) throws IOException {
        org.apache.commons.io.FileUtils.copyURLToFile(this.getClass().getClassLoader().getResource("docbook/webhelp/template/search/" + relativePath),
              new File(targetBaseDir, "search/" + relativePath));

    }


  /**
   * DOCUMENT ME!
   *
   * @param result DOCUMENT ME!
   *
   * @throws MojoExecutionException DOCUMENT ME!
   */
  public void postProcessResult(File result) throws MojoExecutionException {
    super.postProcessResult(result);

    if (getLog().isDebugEnabled())
      getLog().debug("webhelp indexing on: " + targetBaseDir);

    copyTemplate();

    // Creating a DirList with index page excluded, and all other html pages included
    DirList nsiDoc = new DirList(targetBaseDir, new String[]{"ix01.html","^.*\\.html?$"}, 1);
    //new String[]{"ix01.html","^.*\\.html?$"}

    // topic files listed in the given directory
    ArrayList htmlFiles = nsiDoc.getListFiles();

    if (htmlFiles.isEmpty()) {
      throw new MojoExecutionException("No file *.html listed in: " + targetBaseDir);
    }

    // Get the list of all html files with relative paths
    ArrayList htmlFilesPathRel = nsiDoc.getListFilesRelTo(targetBaseDir.getAbsolutePath());

    if (htmlFilesPathRel == null) {
      throw new MojoExecutionException("No relative html files calculated.");
    }

    // Create the list of the existing html files (index starts at 0)
    searchBaseDir.mkdirs();

    final File htmlList = new File(searchBaseDir, HTML_INFO_LIST);

    WriteJSFiles.WriteHTMLList(htmlList.getAbsolutePath(), htmlFilesPathRel, stemming);

    // Parse each html file to retrieve the words:
    // ------------------------------------------

    // Retrieve the clean-up properties for indexing
    retrieveCleanUpProps();

    SaxHTMLIndex spe = new SaxHTMLIndex(cleanUpStrings, cleanUpChars); // use clean-up props files

    System.setProperty("org.xml.sax.driver", "org.ccil.cowan.tagsoup.Parser");
    System.setProperty("javax.xml.parsers.SAXParserFactory", "org.ccil.cowan.tagsoup.jaxp.SAXFactoryImpl");

    if (spe.init(tempDico) == 0) {
      //create a html file description list
      ArrayList filesDescription = new ArrayList();

      String indexerLanguage = getProperty("webhelpIndexerLanguage");
      indexerLanguage = ((indexerLanguage == null) ? "en" : indexerLanguage);
      //TODO: change this when updating webhelpindexer in order to use the new WriteJSFiles.WriteIndex method
        if (getLog().isDebugEnabled())
          getLog().debug("Indexer language is: " + indexerLanguage);

      // parse each html files
      for (int f = 0; f < htmlFiles.size(); f++) {
        File ftemp = (File) htmlFiles.get(f);

        if (getLog().isDebugEnabled())
          getLog().debug("Parsing html file: " + ftemp.getAbsolutePath());

        //tempMap.put(key, value);
        //The HTML file information are added in the list of FileInfoObject
        DocFileInfo docFileInfoTemp = new DocFileInfo(spe.runExtractData(ftemp, indexerLanguage, stemming));

        ftemp = docFileInfoTemp.getFullpath();

        String stemp = ftemp.toString();
        int i = stemp.indexOf(targetBaseDir.getAbsolutePath());

        if (i != 0) {
          System.out.println("the documentation root does not match with the documentation input!");

          return;
        }

        int ad = 1;

        if (stemp.equals(targetBaseDir.getAbsolutePath()))
          ad = 0;

        stemp = stemp.substring(i + targetBaseDir.getAbsolutePath().length() + ad); //i is redundant (i==0 always)
        ftemp = new File(stemp);
        docFileInfoTemp.setFullpath(ftemp);

        filesDescription.add(docFileInfoTemp);
      }

      /*remove empty strings from the map*/
      if (tempDico.containsKey("")) {
        tempDico.remove("");
      }

      // write the index files
      if (tempDico.isEmpty()) {
        throw new MojoExecutionException("No words have been indexed in: " + targetBaseDir);
      }

      File indexFile = new File(searchBaseDir, indexName);
      WriteJSFiles.WriteIndex(indexFile.getAbsolutePath(), tempDico, indexerLanguage);

      // write the html list file with title and shortdesc
      //create the list of the existing html files (index starts at 0)
      File htmlInfoList = new File(searchBaseDir, HTML_INFO_LIST);
      WriteJSFiles.WriteHTMLInfoList(htmlInfoList.getAbsolutePath(), filesDescription);
    } else {
      throw new MojoExecutionException("Parser initialization failed, wrong base dir");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws MojoExecutionException DOCUMENT ME!
   */
  public void postProcess() throws MojoExecutionException {
    super.postProcess();
  }

  private int retrieveCleanUpProps() {
    // Files for punctuation (only one for now)
    String[] punctuationFiles = new String[] { "punctuation.props" };
    FileInputStream input;

    // Get the list of the props file containing the words to remove (not the punctuation)
    DirList props = new DirList(targetBaseDir, new String[]{"", "^(?!(punctuation)).*\\.props$"}, 1);
    //new String[]{"", "^(?!(punctuation)).*\\.props$"}
    ArrayList wordsList = props.getListFiles();

    //		System.out.println("props files:"+wordsList);epub now handles correctly multiples input files and also zip them correctly
    //TODO all properties are taken to a single arraylist. does it ok?.
    Properties enProps = new Properties();

    try {
      // Retrieve words to remove
      for (int i = 0; i < wordsList.size(); i++) {
        File aWordsList = (File) wordsList.get(i);

        if (aWordsList.exists()) {
          enProps.load(input = new FileInputStream(aWordsList));
          input.close();
          cleanUpStrings.addAll(enProps.values());

          enProps.clear();
        }
      }

      // Retrieve char to remove (punctuation for ex.)
      for (int i = 0; i < punctuationFiles.length; i++) {
        String punctuationFile = punctuationFiles[i];
        File ftemp = new File(searchBaseDir, punctuationFile);

        if (ftemp.exists()) {
          enProps.load(input = new FileInputStream(ftemp));
          input.close();
          cleanUpChars.addAll(enProps.values());
          enProps.clear();
        }
      }
    } catch (IOException e) {
      getLog().error("Unable to read one property file", e);

      return 1;
    }

    return 0;
  }
}
