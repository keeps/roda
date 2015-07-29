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
			<field name="level">
				<xsl:value-of select="@level" />
			</field>
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
		<xsl:if test="eadc:did/eadc:unitdate/@normal">
			<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})?/(\d{{4}}-\d{{2}}-\d{{2}})?$"
				select="eadc:did/eadc:unitdate/@normal">
				<xsl:matching-substring>
					<xsl:variable name="dateInitial">
						<xsl:value-of select="regex-group(1)" />
					</xsl:variable>
					<xsl:variable name="dateFinal">
						<xsl:value-of select="regex-group(2)" />
					</xsl:variable>
					<xsl:if test="not(normalize-space($dateInitial)='')">
						<field name="dateInitial"><xsl:value-of select="$dateInitial" />T00:00:00Z</field>
					</xsl:if>
					<xsl:if test="not(normalize-space($dateFinal)='')">
						<field name="dateFinal"><xsl:value-of select="$dateFinal" />T00:00:00Z</field>
					</xsl:if>
				</xsl:matching-substring>
			</xsl:analyze-string>
			<xsl:analyze-string regex="^(\d{{4}})?/(\d{{4}}-\d{{2}}-\d{{2}})?$"
				select="eadc:did/eadc:unitdate/@normal">
				<xsl:matching-substring>
					<xsl:variable name="dateInitial">
						<xsl:value-of select="regex-group(1)" />
					</xsl:variable>
					<xsl:variable name="dateFinal">
						<xsl:value-of select="regex-group(2)" />
					</xsl:variable>
					<xsl:if test="not(normalize-space($dateInitial)='')">
						<field name="dateInitial"><xsl:value-of select="$dateInitial" />-01-01T00:00:00Z</field>
					</xsl:if>
					<xsl:if test="not(normalize-space($dateFinal)='')">
						<field name="dateFinal"><xsl:value-of select="$dateFinal" />T00:00:00Z</field>
					</xsl:if>
				</xsl:matching-substring>
			</xsl:analyze-string>
			<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})?/(\d{{4}})?$"
				select="eadc:did/eadc:unitdate/@normal">
				<xsl:matching-substring>
					<xsl:variable name="dateInitial">
						<xsl:value-of select="regex-group(1)" />
					</xsl:variable>
					<xsl:variable name="dateFinal">
						<xsl:value-of select="regex-group(2)" />
					</xsl:variable>
					<xsl:if test="not(normalize-space($dateInitial)='')">
						<field name="dateInitial"><xsl:value-of select="$dateInitial" />T00:00:00Z</field>
					</xsl:if>
					<xsl:if test="not(normalize-space($dateFinal)='')">
						<field name="dateFinal"><xsl:value-of select="$dateFinal" />-01-01T00:00:00Z</field>
					</xsl:if>
				</xsl:matching-substring>
			</xsl:analyze-string>
			<xsl:analyze-string regex="^(\d{{4}})?/(\d{{4}})?$"
				select="eadc:did/eadc:unitdate/@normal">
				<xsl:matching-substring>
					<xsl:variable name="dateInitial">
						<xsl:value-of select="regex-group(1)" />
					</xsl:variable>
					<xsl:variable name="dateFinal">
						<xsl:value-of select="regex-group(2)" />
					</xsl:variable>
					<xsl:if test="not(normalize-space($dateInitial)='')">
						<field name="dateInitial"><xsl:value-of select="$dateInitial" />-01-01T00:00:00Z</field>
					</xsl:if>
					<xsl:if test="not(normalize-space($dateFinal)='')">
						<field name="dateFinal"><xsl:value-of select="$dateFinal" />-01-01T00:00:00Z</field>
					</xsl:if>
				</xsl:matching-substring>
			</xsl:analyze-string>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>