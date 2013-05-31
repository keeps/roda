<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:zs="http://www.loc.gov/zing/srw/"
		xmlns:foxml="info:fedora/fedora-system:def/foxml#"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
		xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema">
		
<!-- This xslt stylesheet generates the resultPage
     from a Lucene updateIndex.
-->
	
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>

	<xsl:template match="luceneUpdateIndex">
		<xsl:variable name="INDEXNAME" select="@indexName"/>
		<xsl:variable name="INSERTTOTAL" select="counts/@insertTotal"/>
		<xsl:variable name="UPDATETOTAL" select="counts/@updateTotal"/>
		<xsl:variable name="DELETETOTAL" select="counts/@deleteTotal"/>
		<xsl:variable name="DOCCOUNT" select="counts/@docCount"/>
		<xsl:variable name="WARNCOUNT" select="counts/@warnCount"/>
	 		<updateIndex 	indexName="{$INDEXNAME}"
	 						insertTotal="{$INSERTTOTAL}"
	 						updateTotal="{$UPDATETOTAL}"
	 						deleteTotal="{$DELETETOTAL}"
	 						docCount="{$DOCCOUNT}"
	 						warnCount="{$WARNCOUNT}">
			</updateIndex>
	</xsl:template>
	
</xsl:stylesheet>	




				




