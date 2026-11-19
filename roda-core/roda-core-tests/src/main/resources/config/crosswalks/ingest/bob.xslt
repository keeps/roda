<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<!-- Mark this document's block type for nested queries -->
			<field name="content_type"><xsl:text>BOB</xsl:text></field>

			<!-- Process prelDecision elements as nested child documents -->
			<xsl:apply-templates select="BOB/prelDecision" />
		</doc>
	</xsl:template>

	<!-- Template for prelDecision child documents -->
	<xsl:template match="prelDecision">
		<field name="prelDecision">
			<doc>
				<!-- Each child needs a unique ID for Solr's uniqueKey -->
				<field name="id">
					<xsl:text>prelDecision-</xsl:text>
					<xsl:value-of select="position()" />
				</field>
				<field name="content_type"><xsl:text>prelDecision</xsl:text></field>

				<!-- Extract the identity number and expose it for searching -->
				<xsl:if test="perIdentityNo1_txt">
					<field name="perIdentityNo1_txt">
						<xsl:value-of select="perIdentityNo1_txt" />
					</field>
				</xsl:if>
			</doc>
		</field>
	</xsl:template>

</xsl:stylesheet>
