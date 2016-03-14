<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ead="http://ead3.archivists.org/schema/" 
	exclude-result-prefixes="ead">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="ead:archdesc">
		<xsl:if test="@level">
			<field name="level">
				<xsl:value-of select="@level" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:unittitle/text()">
			<field name="title">
				<xsl:value-of select="ead:did/ead:unittitle/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:scopecontent/ead:p/text()">
			<field name="description">
				<xsl:value-of select="ead:scopecontent/ead:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="ead:did/ead:unitdate/@normal">
			<xsl:choose>
				<xsl:when test="contains(ead:did/ead:unitdate/@normal, '/')">	<!-- initial/final -->
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-01-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(2)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(3)" /><xsl:text>T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(2)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>-01-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(2)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(3)" /><xsl:text>T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(2)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:when>
				<xsl:otherwise>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-01-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" /><xsl:text>T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$"
	select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-</xsl:text><xsl:value-of select="regex-group(2)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="ead:did/ead:unitdatestructured">
			<xsl:if test="ead:did/ead:unitdatestructured/ead:daterange/ead:fromdate/@standarddate">
				<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdatestructured/ead:daterange/ead:fromdate/@standarddate)">
					<xsl:matching-substring>
						<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>T00:00:00Z</xsl:text></field>
					</xsl:matching-substring>
				</xsl:analyze-string>
				<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdatestructured/ead:daterange/ead:fromdate/@standarddate)">
					<xsl:matching-substring>
						<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
					</xsl:matching-substring>
				</xsl:analyze-string>
				<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(ead:did/ead:unitdatestructured/ead:daterange/ead:fromdate/@standarddate)">
					<xsl:matching-substring>
						<field name="dateInitial"><xsl:value-of select="regex-group(1)" /><xsl:text>-01-01T00:00:00Z</xsl:text></field>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:if>
			<xsl:if test="ead:did/ead:unitdatestructured/ead:daterange/ead:todate/@standarddate">
				<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdatestructured/ead:daterange/ead:todate/@standarddate)">
					<xsl:matching-substring>
						<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>T00:00:00Z</xsl:text></field>
					</xsl:matching-substring>
				</xsl:analyze-string>
				<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdatestructured/ead:daterange/ead:todate/@standarddate)">
					<xsl:matching-substring>
						<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>-01T00:00:00Z</xsl:text></field>
					</xsl:matching-substring>
				</xsl:analyze-string>
				<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(ead:did/ead:unitdatestructured/ead:daterange/ead:todate/@standarddate)">
					<xsl:matching-substring>
						<field name="dateFinal"><xsl:value-of select="regex-group(1)" /><xsl:text>-01-01T00:00:00Z</xsl:text></field>
					</xsl:matching-substring>
				</xsl:analyze-string>
			</xsl:if>
		</xsl:if>
		
	</xsl:template>
</xsl:stylesheet>