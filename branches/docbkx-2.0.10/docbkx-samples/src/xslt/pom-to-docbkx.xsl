<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:pom="http://maven.apache.org/POM/4.0.0"
                version="1.0">

  <xsl:output method="xml"
    doctype-public="-//OASIS//DTD DocBook XML V4.4//EN"
    doctype-system="http://www.oasis-open.org/docbook/xml/4.4/docbookx.dtd"/>

  <xsl:variable name="nl"><xsl:text disable-output-escaping="yes">&amp;#10;</xsl:text></xsl:variable>
  <xsl:variable name="indent-increment" select="'  '" />

  <xsl:template match="/">
    <article>
      <articleinfo>
        <title>User Guide</title>
        <subtitle>Docbkx Maven Plugin</subtitle>
        <releaseinfo>
          <xsl:text disable-output-escaping="yes">&amp;</xsl:text>
          <xsl:text>version;</xsl:text>
        </releaseinfo>
      </articleinfo>
      <bridgehead>Introduction</bridgehead>
      <para>
        This document provides an overview of the different features
        of the Maven Docbkx Plugin. With almost 200+ customizable
        properties per type of output format, it would be impossible
        list all of those in this document. For more detailed
        information on these customizable properties, please consult
        the online plugin documentation. Instead of being a reference
        guide, this document is intended to get you started, and at
        least to get some flavour of the scope of this plugin.
      </para>
      <para>
        In this document, we will discuss every feature with an
        example, showing the relevant configuration code. You are
        encouraged to work through these examples in the given order.
      </para>
      <bridgehead>Example Usage</bridgehead>
      <xsl:apply-templates select="//pom:profiles" mode="doc"/>
    </article>
  </xsl:template>

  <xsl:template match="pom:profiles" mode="doc">
    <xsl:apply-templates 
      select="comment()|pom:profile[not(pom:id='docbkx.release')]" 
      mode="doc"/>
  </xsl:template>

  <xsl:template match="comment()" mode="doc">
    <para>
      <xsl:value-of select="normalize-space(.)"/>
    </para>
  </xsl:template>

  <xsl:template match="pom:profile" mode="doc">
    <example>
      <title>
        <xsl:value-of select="pom:properties/pom:example.title"/>
      </title>
      <programlisting>
        <xsl:apply-templates select="*" mode="escape-xml"/>
      </programlisting>
    </example>
  </xsl:template>

  <xsl:template name="write-starttag">
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:apply-templates select="@*" mode="write-attribute"/>
    <xsl:if test="not(*|text()|comment()|processing-instruction())">/</xsl:if>
    <xsl:text>></xsl:text>
  </xsl:template>
  
  <xsl:template name="write-endtag">
    <xsl:text>&lt;/</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>></xsl:text>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template name="write-attribute">
    <xsl:text> </xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:template>
  
  <xsl:template match="*" mode="escape-xml">
    <xsl:param name="indent-string" select="$indent-increment"/>
    <xsl:value-of select="$indent-string"/>
    <xsl:apply-templates mode="write-starttag" select="."/>
    <xsl:if test="*">
      <xsl:text>
</xsl:text>
      <xsl:apply-templates mode="escape-xml" select="*">
        <xsl:with-param name="indent-string" select="concat($indent-string, $indent-increment)"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="text()">
      <xsl:value-of select="normalize-space(text())"/>
    </xsl:if>
    <xsl:if test="*">
        <xsl:value-of select="$indent-string" />
    </xsl:if>
    <xsl:if test="*|text()|comment()|processing-instruction()"><xsl:apply-templates
    mode="write-endtag" select="."/></xsl:if>
  </xsl:template>

  <xsl:template match="pom:id" mode="escape-xml">
  </xsl:template>

  <xsl:template match="pom:properties" mode="escape-xml">
  </xsl:template>
  
  <xsl:template match="@*" mode="write-attribute">
    <xsl:call-template name="write-attribute"/>
  </xsl:template>
  
  <xsl:template match="*" mode="write-starttag">
    <xsl:call-template name="write-starttag"/>
  </xsl:template>
  
  <xsl:template match="*" mode="write-endtag">
    <xsl:call-template name="write-endtag"/>
  </xsl:template>
       
</xsl:stylesheet>
