<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
	exclude-result-prefixes="dc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<div class="descriptiveMetadata">
		<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="metadata">
		<xsl:if test="dc:title/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Title</div>
				<xsl:for-each select="dc:title">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:description/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Description</div>
				<xsl:for-each select="dc:description">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:contributor/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Contributor</div>
				<xsl:for-each select="dc:contributor">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:coverage/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Coverage</div>
				<xsl:for-each select="dc:coverage">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:creator/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Creator</div>
				<xsl:for-each select="dc:creator">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:date/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Date</div>
				<xsl:for-each select="dc:date">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:format/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Format</div>
				<xsl:for-each select="dc:format">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:identifier/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Identifier</div>
				<xsl:for-each select="dc:identifier">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:language/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Language</div>
				<xsl:for-each select="dc:language">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:publisher/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Publisher</div>
				<xsl:for-each select="dc:publisher">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:relation/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Relation</div>
				<xsl:for-each select="dc:relation">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:rights/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Rights</div>
				<xsl:for-each select="dc:rights">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:source/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Source</div>
				<xsl:for-each select="dc:source">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:subject/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Subject</div>
				<xsl:for-each select="dc:subject">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:type/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Type</div>
				<xsl:for-each select="dc:type">
					<div class="descriptiveMetadata-field-value"><xsl:value-of select="text()" /></div>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>