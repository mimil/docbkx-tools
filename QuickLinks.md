Most of the external documentation is hosted on the docbkx-tools SourceForge project (http://docbkx-tools.sourceforge.net/).

You will especially notice the following documentations:
  * [User guide](http://docbkx-tools.sourceforge.net/docbkx-samples/manual.html)
  * [Advanced customization](http://docbkx-tools.sourceforge.net/advanced.html)
  * [Super advanced customization](http://docbkx-tools.sourceforge.net/builder.html)

In addition of the previous Advanced customization page a new feature has been added allowing to link to every XSL files of the original docbook release.

It can be done using same kind of syntax as **<xsl:import
href="urn:docbkx:stylesheet"/>** (which points to the main docbook XSL file) by appending **/filetolink.xsl**. Here is an example allowing to import inline.xsl: **<xsl:import
href="urn:docbkx:stylesheet/inline.xsl"/>**