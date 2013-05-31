<?xml version="1.0" encoding="UTF-8"?> 
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
		
<!-- This xslt stylesheet presents a simple search page,
     including hits, if any.
-->
	<xsl:output method="html" indent="yes" encoding="UTF-8"/>

	<xsl:param name="ERRORMESSAGE" select="''"/>
	
	<xsl:include href="CONFIGPATH/rest/rodaCommon.xslt"/>

	<xsl:template name="opSpecifics">
		<xsl:variable name="INDEXNAME" select="@indexName"/>
		<xsl:variable name="QUERY" select="gfindObjects/@query"/>
		<xsl:variable name="HITPAGESTART" select="gfindObjects/@hitPageStart"/>
		<xsl:variable name="HITPAGESIZE" select="gfindObjects/@hitPageSize"/>
		<xsl:variable name="HITTOTAL" select="gfindObjects/@hitTotal"/>
		<xsl:variable name="HITPAGEEND">
			<xsl:choose>
				<xsl:when test="$HITPAGESTART + $HITPAGESIZE - 1 > $HITTOTAL">
					<xsl:value-of select="$HITTOTAL"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$HITPAGESTART + $HITPAGESIZE - 1"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="HITPAGENO" select="ceiling($HITPAGESTART div $HITPAGESIZE)"/>
		<xsl:variable name="HITPAGENOLAST" select="ceiling($HITTOTAL div $HITPAGESIZE)"/>
		<xsl:variable name="HITPAGESTART" select="(($HITPAGENO - 1) * $HITPAGESIZE + 1)"/>
		
		<h2>gfindObjects</h2>
			<form method="get" action="rest">
				<table border="3" cellpadding="5" cellspacing="0">
					<tr>
						<td>
							<input type="hidden" name="operation" value="gfindObjects"/>
							Query: <input type="text" name="query" size="50" value="{$QUERY}"/> 
							<xsl:text> </xsl:text>Hit page size: <input type="text" name="hitPageSize" size="4" value="{$HITPAGESIZE}"/> 
							<xsl:text> </xsl:text><input type="submit" value="Search"/>
						</td>
					</tr>
				</table>
			</form>
			<p/>
			<xsl:if test="$HITTOTAL = 0 and $QUERY and $QUERY != '' ">
				<p>No hits!</p>
	 		</xsl:if>
			<xsl:if test="$HITTOTAL > 0">
	 			<table border="3" cellpadding="5" cellspacing="0">
					<tr>
						<td>This is hit page number <b><xsl:value-of select="$HITPAGENO"/></b>
							of <xsl:value-of select="$HITPAGENOLAST"/> hit pages,
							showing hit number <b><xsl:value-of select="$HITPAGESTART"/></b>
		 					to <b><xsl:value-of select="$HITPAGEEND"/></b>
		 					of <xsl:value-of select="$HITTOTAL"/> hits.
						</td>
					</tr>
				</table>
	 			<table border="0" cellpadding="5" cellspacing="0">
					<tr>
						<td>
					<xsl:if test="$HITPAGENO > 1">
						<form method="get" action="rest">
							<input type="hidden" name="operation" value="gfindObjects"/>
							<input type="hidden" name="indexName" value="{$INDEXNAME}"/>
							<input type="hidden" name="query" value="{$QUERY}"/>
							<input type="hidden" name="hitPageStart" value="1"/>
							<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}"/>
							<input type="submit" value="First hit page"/>
						</form>
	 				</xsl:if>
	 					</td>
	 					<td>
					<xsl:if test="$HITPAGENO > 2">
						<form method="get" action="rest">
							<input type="hidden" name="operation" value="gfindObjects"/>
							<input type="hidden" name="indexName" value="{$INDEXNAME}"/>
							<input type="hidden" name="query" value="{$QUERY}"/>
							<input type="hidden" name="hitPageStart" value="{$HITPAGESTART - $HITPAGESIZE}"/>
							<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}"/>
							<input type="submit" value="Previous hit page"/>
						</form>
	 				</xsl:if>
	 					</td>
	 					<td>
					<xsl:if test="$HITPAGENOLAST > $HITPAGENO + 1">
						<form method="get" action="rest">
							<input type="hidden" name="operation" value="gfindObjects"/>
							<input type="hidden" name="indexName" value="{$INDEXNAME}"/>
							<input type="hidden" name="query" value="{$QUERY}"/>
							<input type="hidden" name="hitPageStart" value="{$HITPAGESTART + $HITPAGESIZE}"/>
							<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}"/>
							<input type="submit" value="Next hit page"/>
						</form>
	 				</xsl:if>
	 					</td>
	 					<td>
					<xsl:if test="$HITPAGENOLAST > $HITPAGENO">
						<form method="get" action="rest">
							<input type="hidden" name="operation" value="gfindObjects"/>
							<input type="hidden" name="indexName" value="{$INDEXNAME}"/>
							<input type="hidden" name="query" value="{$QUERY}"/>
							<input type="hidden" name="hitPageStart" value="{$HITPAGESIZE * ($HITPAGENOLAST - 1) + 1}"/>
							<input type="hidden" name="hitPageSize" value="{$HITPAGESIZE}"/>
							<input type="submit" value="Last hit page"/>
						</form>
	 				</xsl:if>
						</td>
					</tr>
				</table>
				<table border="3" cellpadding="5" cellspacing="0" bgcolor="silver" width="784">
					<xsl:apply-templates select="gfindObjects/objects"/>
				</table>
	 		</xsl:if>
	</xsl:template>

	<xsl:template match="object">
		<tr><td>
			<table border="0" cellpadding="5" cellspacing="0" bgcolor="silver" width="784">
				<tr>
					<td width="100">
						<span class="hitno">
							<xsl:value-of select="@no"/>
							<xsl:value-of select="'. '"/>
						</span>
						<a>
							<xsl:variable name="PIDVALUE">
								<xsl:choose>
									<xsl:when test="@PID">
								 		<xsl:value-of select="@PID"/>
									</xsl:when>
									<xsl:otherwise>
								 		<xsl:value-of select="field[@name='PID']/text()"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<xsl:attribute name="href">/fedora/get/<xsl:value-of select="$PIDVALUE"/>
							</xsl:attribute>
							<xsl:value-of select="$PIDVALUE"/>
						</a>
						<br/>
						<span class="hitscore">
							(<xsl:value-of select="@score"/>)
						</span>
					</td>
					<td>
						<span class="hittitle">
							<xsl:copy-of select="field[@name='dc.title']/node()"/>
						</span>
					</td>
				</tr>
				<xsl:for-each select="field[@snippet='yes']">
				  <xsl:if test="@name!='dc.title'">
					<tr>
						<td width="100" valign="top">
							<span class="hitfield">
								<xsl:value-of select="@name"/>
							</span>
						</td>
						<td>
							<span class="hitsnippet">
								<xsl:copy-of select="node()"/>
							</span>
						</td>
					</tr>
				  </xsl:if>
				</xsl:for-each>
			</table>
		</td></tr>
	</xsl:template>

  <!-- disable all default text node output -->
  <xsl:template match="text()"/>
	
</xsl:stylesheet>	
