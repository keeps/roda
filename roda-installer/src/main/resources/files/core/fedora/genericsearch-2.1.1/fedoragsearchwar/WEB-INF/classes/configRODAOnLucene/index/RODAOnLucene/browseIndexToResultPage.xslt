<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:zs="http://www.loc.gov/zing/srw/"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:eadc="http://roda.dgarq.gov.pt/2014/EADCSchema">
		
<!-- This xslt stylesheet generates the resultPage
     from a Lucene browseIndex.
-->
	
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>
	
	<xsl:param name="STARTTERM" select="query"/>
	<xsl:param name="TERMPAGESIZE" select="10"/>
	<xsl:param name="RESULTPAGEXSLT" select="resultPageXslt"/>
	<xsl:param name="DATETIME" select="none"/>

	<xsl:template match="lucenebrowseindex">
		<xsl:variable name="INDEXNAME" select="@indexName"/>
		<xsl:variable name="FIELDNAME" select="@fieldName"/>
		<xsl:variable name="TERMTOTAL" select="@termTotal"/>
	 	<resultPage dateTime="{$DATETIME}"
	 				indexName="{$INDEXNAME}">
	 		<browseIndex 	startTerm="{$STARTTERM}"
	 						fieldName="{$FIELDNAME}"
	 						termPageSize="{$TERMPAGESIZE}"
	 						resultPageXslt="{$RESULTPAGEXSLT}"
	 						termTotal="{$TERMTOTAL}">
				<xsl:copy-of select="fields"/>
				<xsl:copy-of select="terms"/>
			</browseIndex>
	 	</resultPage>
	</xsl:template>
	
</xsl:stylesheet>	
