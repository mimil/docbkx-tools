### What are the current supported outputs ###
As for now the current docbook outputs are the following, the name given here is the name the the maven goal to use:
  * generate-pdf
  * generate-rtf
  * generate-html (chunked or single file)
  * generate-xhtml (chunked or single file)
  * generate-xhtml5 (chunked or single file)
  * generate-eclipse
  * generate-man
  * generate-javahelp
  * generate-epub
  * generate-epub3
  * generate-webhelp
  * generate-template

### My customization stylesheet does not want to apply ###
If you have respected the import statements format required by Docbkx plugin (`<xsl:import href="urn:docbkx:stylesheet"/>` etc), it is certainly due to the fact that our plugin is based on latest docbook-xsl namespaced version.

You just need to adapt your customization stylesheet to be namespaced compliant, here is a sample where you can notice the **d:** namespace:
```
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:d="http://docbook.org/ns/docbook">
    <xsl:import href="urn:docbkx:stylesheet"/>

    <xsl:template match="d:remark">
      <fo:inline font-style="italic" color="blue">
        <xsl:call-template name="inline.charseq"/>
      </fo:inline>
    </xsl:template>

</xsl:stylesheet>
```

### How can I pass XSL parameters to my customization stylesheet? ###
Docbkx plugin gives you shortcuts to use regular docbook-xsl parameters using the following convention: A docbook-xsl parameter named `footnote.number.format` will have a Docbkx plugin parameter named `footnoteNumberFormat`.

These regular docbook-xsl parameters are supported out-of-the-box because they are generated from the stylesheets directly, but how is it about custom xsl parameters I may have in my customization stylesheet?
In order to handle this case we added a manual way to define arbitrary xsl parameters, here is a sample:

```
<customizationParameters>
  <parameter>
    <name>key</name>
    <value>value</value>
  </parameter>
</customizationParameters>
```

From the API point of view, `AbstractTransformerMojo.getProperty()` and `AbstractTransformerMojo.setProperty()` give access to regular docbook-xsl parameters and `AbstractTransformerMojo.getCustomizationParameters()` returns a list of `Parameter` instances, each of them representing a custom xsl parameter.

### Can I define parameters from the command line? ###

Yes, it is handy to allow configuration from outside the pom file.

Since 2.0.15 you can set docbook-xsl parameter using the command line, in order to not interfere with other plugins all the parameters will be prefixed with `docbkx.` and followed by the Docbkx plugin parameter name.

```
$ mvn docbkx:generate-pdf -Ddocbkx.draftMode="true"
```

### Generation is slow ###
If your source files are specifying the docbook DTD or any other element pointing to docbook 'public identifier', you will need to setup correctly the XML catalog resolution. The plugin does the hard work, you just need to set the right dependency in your pom.

Here is a sample for Docbook 4.4:

```
<plugins>
  <plugin>
    <groupId>com.agilejava.docbkx</groupId>
    <artifactId>docbkx-maven-plugin</artifactId>
    <version>${project.version}</version>
      <dependencies>
        <dependency>
          <groupId>org.docbook</groupId>
          <artifactId>docbook-xml</artifactId>
          <version>4.4</version>
          <scope>runtime</scope>
        </dependency>
      </dependencies>
...
```

For Docbook 4.5 you will need to add sonatype repository (https://repository.sonatype.org/content/groups/public/) in your pom:

```
<repositories>
  <repository>
    <id>sonatype-public</id>
    <name>Sonatype Public</name>
    <url>http://repository.sonatype.org/content/groups/public</url>
  </repository>
</repositories>
```

And then add this dependency:

```
    <dependency>
      <groupId>docbook</groupId>
      <artifactId>docbook-xml</artifactId>
      <version>4.5</version>
    </dependency>
...
```

For Docbook 5.0 the dependency is the following:

```
    <dependency>
      <groupId>net.sf.docbook</groupId>
      <artifactId>docbook-xml</artifactId>
      <version>5.0-all</version>
      <classifier>resources</classifier>
      <type>zip</type>
      <scope>runtime</scope>
    </dependency>
...
```

### I need to see the xsl:message ###
If you need to see `<xsl:message/>` debugs from your xsl stylesheet, use the following configuration:

```
<configuration>
   <showXslMessages>true</showXslMessages>
</configuration>
```

### I need to configure xsl parameters referencing a file ###

Sometime xsl parameters are pointing to directories or file and you don't want to hard code the path, properties like `<imgSrcPath>` or `admonGraphicsPath`. You can configure them relative to the `${basedir}` of maven pom. As for example:

```
<imgSrcPath>file:///${basedir}/src/docbkx/</imgSrcPath>
```

(you can notice that there is 3 '/' for multi os issue)