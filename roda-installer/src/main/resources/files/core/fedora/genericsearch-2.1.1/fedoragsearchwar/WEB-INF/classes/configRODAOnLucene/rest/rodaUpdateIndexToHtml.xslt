<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:param name="ERRORMESSAGE" select="''"/>

	<xsl:include href="CONFIGPATH/rest/rodaCommon.xslt"/>

	<xsl:template name="opSpecifics">
	
		<h2>updateIndex</h2>
		    <xsl:apply-templates select="updateIndex"/>
			<p/>
			<form method="get" action="rest">
				<table border="3" cellpadding="5" cellspacing="5">
					<tr>
						<td>
							<input type="hidden" name="operation" value="updateIndex"/>
							<input type="hidden" name="action" value="createEmpty"/>
							<xsl:text> </xsl:text><input type="submit" value="updateIndex createEmpty"/>
						</td>
					</tr>
				</table>
			</form>
		
			<form method="get" action="rest">
				<table border="3" cellpadding="5" cellspacing="5">
					<tr>
						<td>
							<input type="hidden" name="operation" value="updateIndex"/>
							<input type="hidden" name="action" value="optimize"/>
							<xsl:text> </xsl:text><input type="submit" value="updateIndex optimize"/>
						</td>
					</tr>
				</table>
			</form>
		
			<form method="get" action="rest">
				<table border="3" cellpadding="5" cellspacing="5">
					<tr>
						<td>
							<input type="hidden" name="operation" value="updateIndex"/>
							<input type="hidden" name="action" value="fromFoxmlFiles"/>
							Foxml files path: <input type="text" name="value" size="40" value=""/> 
							<xsl:text> </xsl:text><input type="submit" value="updateIndex fromFoxmlFiles"/>
						</td>
					</tr>
				</table>
			</form>
		
			<form method="get" action="rest">
				<table border="3" cellpadding="5" cellspacing="5">
					<tr>
						<td>
							<input type="hidden" name="operation" value="updateIndex"/>
							<input type="hidden" name="action" value="fromPid"/>
							Pid: <input type="text" name="value" size="30" value=""/> 
							<xsl:text> </xsl:text><input type="submit" value="updateIndex fromPid"/>
						</td>
					</tr>
				</table>
			</form>
		
			<form method="get" action="rest">
				<table border="3" cellpadding="5" cellspacing="5">
					<tr>
						<td>
							<input type="hidden" name="operation" value="updateIndex"/>
							<input type="hidden" name="action" value="deletePid"/>
							Pid: <input type="text" name="value" size="30" value=""/> 
							<xsl:text> </xsl:text><input type="submit" value="updateIndex deletePid"/>
						</td>
					</tr>
				</table>
			</form>
	</xsl:template>
	
	<xsl:template match="updateIndex">
		<xsl:variable name="INDEXNAME" select="@indexName"/>
		<xsl:variable name="INSERTTOTAL" select="@insertTotal"/>
		<xsl:variable name="UPDATETOTAL" select="@updateTotal"/>
		<xsl:variable name="DELETETOTAL" select="@deleteTotal"/>
		<xsl:variable name="DOCCOUNT" select="@docCount"/>
		<xsl:variable name="WARNCOUNT" select="@warnCount"/>
		
			<table border="3" cellpadding="5" cellspacing="5">
				<tr>
					<td>Index name: <b><xsl:value-of select="$INDEXNAME"/></b>
					</td>
					<td>Inserted number of index documents: <xsl:value-of select="$INSERTTOTAL"/>
					</td>
					<td>Updated number of index documents: <xsl:value-of select="$UPDATETOTAL"/>
					</td>
					<td>Deleted number of index documents: <xsl:value-of select="$DELETETOTAL"/>
					</td>
					<td>Resulting number of index documents: <xsl:value-of select="$DOCCOUNT"/>
					</td>
					<td>Number of warnings (see log): <xsl:value-of select="$WARNCOUNT"/>
					</td>
				</tr>
			</table>
	</xsl:template>
	
</xsl:stylesheet>	
