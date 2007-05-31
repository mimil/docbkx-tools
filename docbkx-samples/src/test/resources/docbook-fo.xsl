<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

  <xsl:import href="urn:docbkx:stylesheet"/>


  <!--
  <xsl:import href="titlepage.templates.xsl"/>
  <xsl:import href="pagesetup.xsl"/>
  -->

  <xsl:param name="logo.location"/>

  <xsl:param name="default.table.width" select="'3'"/>
  <!-- check this -->
  <xsl:param name="use.extensions" select="'1'"/>

  <xsl:param name="body.font.family">Helvetica</xsl:param>
  <xsl:param name="title.font.family">Helvetica</xsl:param>
  <xsl:param name="sans.font.family">Helvetica</xsl:param>
  <xsl:param name="draft.mode" select="'0'"/>
  
  
  <xsl:param name="paper.type">A4</xsl:param>
  <xsl:param name="chapter.autolabel" select="1"/>
  <xsl:param name="section.autolabel" select="1"/>
  <xsl:param name="section.label.includes.component.label" select="1"/>
  <xsl:param name="double.sided" select="0"/>
  <xsl:param name="header.rule" select="0"/>
  <xsl:param name="footer.rule" select="0"/>
  <xsl:param name="page.margin.bottom">0.5in</xsl:param>
  <xsl:param name="body.margin.bottom">0.7in</xsl:param>
  <xsl:param name="region.after.extent" select="'0.4in'"/>
  <!--
  <xsl:param name="page.margin.inner">
    <xsl:choose>
      <xsl:when test="$double.sided != 0">1.25in</xsl:when>
      <xsl:otherwise>1in</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:param name="page.margin.outer">
    <xsl:choose>
      <xsl:when test="$double.sided != 0">0.75in</xsl:when>
      <xsl:otherwise>1in</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  -->

  <xsl:attribute-set name="footer.content.properties">
    <xsl:attribute name="font-size">
      <xsl:text>8pt</xsl:text> 
    </xsl:attribute>
  </xsl:attribute-set>

  <xsl:param name="alignment">left</xsl:param>
  <xsl:param name="fop.extensions" select="1"/>
  <xsl:param name="generate.toc">
    /appendix toc
    book      toc,example,equation
    /chapter  toc
    part      toc
    /preface  toc
    qandadiv  toc
    qandaset  toc
    reference toc
    /section  toc
    set       toc
    /article  toc
  </xsl:param>

  <xsl:attribute-set name="monospace.verbatim.properties" 
    use-attribute-sets="verbatim.properties">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$monospace.font.family"/>
    </xsl:attribute>
    <xsl:attribute name="font-size">
      <xsl:value-of select="$body.font.master * 0.9"/>
      <xsl:text>pt</xsl:text>
    </xsl:attribute>
    <xsl:attribute name="text-align">
      <xsl:text>left</xsl:text>
    </xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="formal.title.properties" 
    use-attribute-sets="normal.para.spacing">
    <xsl:attribute name="font-size">12pt</xsl:attribute>
    <xsl:attribute name="hyphenate">false</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="space-after.minimum">0.4em</xsl:attribute>
    <xsl:attribute name="space-after.optimum">0.6em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">0.8em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="component.title.properties">
    <xsl:attribute name="space-before.optimum">2em</xsl:attribute>
    <xsl:attribute name="space-before.minimum">1.8em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">2.2em</xsl:attribute>
    <xsl:attribute name="font-size">18pt</xsl:attribute>
    <xsl:attribute name="space-after.optimum">1.5em</xsl:attribute>
    <xsl:attribute name="space-after.minimum">1.3em</xsl:attribute>
    <xsl:attribute name="space-after.maximum">1.8em</xsl:attribute>
    <xsl:attribute name="hyphenate">false</xsl:attribute>
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:param name="body.margin.left">1.5cm</xsl:param>
  <xsl:param name="body.margin.right">0in</xsl:param>
  <!--
  <xsl:param name="title.margin.left">0in</xsl:param>
  <xsl:param name="city.before.postcode">1</xsl:param>
  -->

  <!-- Removing the bold font settings. -->
  <!--
  <xsl:template name="section.heading">
    <xsl:param name="level">1</xsl:param>
    <xsl:param name="title"></xsl:param>
    <xsl:variable name="fsize">
      <xsl:choose>
        <xsl:when test="$level=1">18</xsl:when>
        <xsl:when test="$level=2">16</xsl:when>
        <xsl:when test="$level=3">14</xsl:when>
        <xsl:when test="$level=4">12</xsl:when>
        <xsl:when test="$level=5">12</xsl:when>
        <xsl:otherwise>10</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format"
      font-size="{$fsize}pt"
      font-family="{$title.font.family}"
      margin-left="{$body.margin.left}"
      text-indent="-{$body.margin.left}"
      keep-with-next.within-column="always"
      space-before.minimum="1em"
      space-before.optimum="1.5em"
      space-before.maximum="2em">
      <xsl:copy-of select="$title"/>
    </fo:block>
  </xsl:template>
  -->

  <xsl:template xmlns:xsl="http://www.w3.org/1999/XSL/Transform" name="book.titlepage.before.verso">
    <fo:block space-before="1in" space-after=".25in">
      <fo:external-graphic xmlns:fo="http://www.w3.org/1999/XSL/Format" 
        width="5.890cm"
        src="{$logo.location}"/>
    </fo:block>
  </xsl:template>

  <xsl:attribute-set name="section.title.properties">
    <xsl:attribute name="font-family">
      <xsl:value-of select="$title.font.family"/>
    </xsl:attribute>
    <!-- font size is calculated dynamically by section.heading template -->
    <xsl:attribute name="keep-with-next.within-column">always</xsl:attribute>
    <xsl:attribute name="space-before.minimum">0.8em</xsl:attribute>
    <xsl:attribute name="space-before.optimum">1.0em</xsl:attribute>
    <xsl:attribute name="space-before.maximum">1.2em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="section.title.level1.properties">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="text-align">left</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
    <xsl:attribute name="padding-top">1cm</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="section.title.level2.properties">
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="text-align">left</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
    <xsl:attribute name="padding-top">1cm</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="article.titlepage.recto.style">
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="text-align">left</xsl:attribute>
    <xsl:attribute name="font-size">12pt</xsl:attribute>
  </xsl:attribute-set>

  <xsl:template match="section|sect1|sect2|sect3|sect4|sect5"
    mode="object.title.markup">
    <fo:block margin-left="{$title.margin.left}" margin-bottom="3cm" white-space-collapse="false">
      <fo:table>
        <fo:table-column column-number="1" column-width="-{$title.margin.left}"/>
        <fo:table-column column-number="2" column-width="proportional-column-width(1)"/>
        <fo:table-body>
          <fo:table-row>
            <fo:table-cell display-align="before" text-align="left">
              <fo:block>
                <xsl:apply-templates select="." mode="label.markup"/>
                <xsl:text>.</xsl:text>
              </fo:block>
            </fo:table-cell>
            <fo:table-cell display-align="before" text-align="left">
              <fo:block>
                <xsl:apply-templates select="." mode="title.markup"/>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
        </fo:table-body>
      </fo:table>
    </fo:block>
  </xsl:template>

  <xsl:template match="processing-instruction('custom-pagebreak')">
    <fo:block break-before="page"/>
  </xsl:template>


  <xsl:template name="table.of.contents.titlepage.recto">
    <fo:block xmlns:fo="http://www.w3.org/1999/XSL/Format" 
              xsl:use-attribute-sets="table.of.contents.titlepage.recto.style" 
              space-before.minimum="1em" 
              space-before.optimum="1.5em" 
              space-before.maximum="2em" 
              space-after="0.5em" 
              start-indent="0pt" 
              font-weight="bold" 
              font-family="{$title.fontset}" 
              font-size="12pt">
      <xsl:call-template name="gentext">
        <xsl:with-param name="key" select="'TableofContents'"/>
      </xsl:call-template>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
