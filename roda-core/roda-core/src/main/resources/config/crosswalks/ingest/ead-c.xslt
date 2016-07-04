<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema"
	exclude-result-prefixes="eadc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="eadc:ead-c">
		<xsl:if test="@level">
			<xsl:choose>
				<xsl:when test="@level = 'otherlevel'">
					<field name="level">
						<xsl:value-of select="@otherlevel" />
					</field>
				</xsl:when>
				<xsl:otherwise>
					<field name="level">
						<xsl:value-of select="@level" />
					</field>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<field name="title">
				<xsl:value-of select="eadc:did/eadc:unittitle/text()" />
			</field>
		</xsl:if>
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<field name="description">
				<xsl:value-of select="eadc:scopecontent/eadc:p/text()" />
			</field>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:origination/text()">
			<field name="origination_txt">
				<xsl:value-of select="eadc:did/eadc:origination/text()" />
			</field>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unitdate/@normal">
			<xsl:choose>
				<xsl:when test="contains(eadc:did/eadc:unitdate/@normal, '/')">	<!-- initial/final -->
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-01-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-before(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" />T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" />-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" />-01-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-after(eadc:did/eadc:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<field name="dateFinal"><xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:when>
				<xsl:otherwise>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-01-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(eadc:did/eadc:unitdate/@normal)">
						<xsl:matching-substring>
							<field name="dateInitial"><xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01T00:00:00Z</field>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>