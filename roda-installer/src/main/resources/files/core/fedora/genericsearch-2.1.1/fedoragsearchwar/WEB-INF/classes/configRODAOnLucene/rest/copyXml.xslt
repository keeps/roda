<?xml version="1.0" encoding="UTF-8"?> 
<!-- $Id: copyXml.xslt,v 1.1 2006/10/12 13:02:51 rcastro Exp $ -->
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:zs="http://www.loc.gov/zing/srw/"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema">

	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

	<xsl:template match="/">
      <xsl:copy-of select="/"/>
	</xsl:template>
	
</xsl:stylesheet>	




				




