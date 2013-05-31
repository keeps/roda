<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
<!-- This xslt stylesheet is common to 
     rodaGfindObjectsToHtml, rodaGetIndexInfoToHtml and rodaUpdateIndexToHtml.
-->
	
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>
	
	<xsl:param name="TIMEUSEDMS" select="''"/>

	<xsl:template match="/resultPage">
		<html>
			<head>
				<title>RODA on Lucene of Fedora Generic Search Service</title>
				<link rel="stylesheet" type="text/css" href="css/demo.css"/>
				<style type="text/css">
					.highlight {
						background: yellow;
					}
				</style>
				<script language="javascript">
				</script>
			</head>
			<body>
				<div id="header">
					<a href="" id="logo"></a>
					<div id="title">
						<h1>RODA on Lucene of Fedora Generic Search Service</h1>
					</div>
				</div>
				<table cellspacing="10" cellpadding="10">
					<tr>
					<th><a href="?operation=updateIndex">updateIndex</a></th>
					<th><a href="?operation=gfindObjects">gfindObjects</a></th>
					<th><a href="?operation=browseIndex">browseIndex</a></th>
					<th><a href="?operation=getRepositoryInfo">getRepositoryInfo</a></th>
					<th><a href="?operation=getIndexInfo">getIndexInfo</a></th>
					<td>(<xsl:value-of select="$TIMEUSEDMS"/> milliseconds)</td>
					</tr>
				</table>
				<p/>
				<!-- 
				<xsl:if test="$ERRORMESSAGE">
					<xsl:call-template name="error"/>
	 			</xsl:if>
	 			 -->
        		<xsl:apply-templates select="error"/>
				<xsl:call-template name="opSpecifics"/>
				<div id="footer">
   					<div id="copyright">
						Copyright &#xA9; 2008 Direcção Geral de Arquivos, RODA Project
					</div>
					<div id="lastModified">
						Last Modified
						<script type="text/javascript">
							//<![CDATA[
							var cvsDate = "$Date: 2008-11-20 16:27:00 (Thu, 20 Nov 2008) $";
							var parts = cvsDate.split(" ");
							var modifiedDate = parts[1];
							document.write(modifiedDate);
							//]]>
						</script>
						by Rui Castro
					</div>
				</div>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="error">
		<p>
			<font color="red">
				<xsl:value-of select="$ERRORMESSAGE"/>
			</font>
		</p>			
	</xsl:template>
	
	<xsl:template match="message">
		<p>
			<font color="red">
				<xsl:value-of select="./text()"/>
			</font>
		</p>			
	</xsl:template>
	
</xsl:stylesheet>
