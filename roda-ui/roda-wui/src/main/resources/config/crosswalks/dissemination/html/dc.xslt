<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/"
	exclude-result-prefixes="dc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:param name="i18n.title" />
	<xsl:param name="i18n.description" />
	<xsl:param name="i18n.contributor" />
	<xsl:param name="i18n.coverage" />
	<xsl:param name="i18n.creator" />
	<xsl:param name="i18n.date" />
	<xsl:param name="i18n.format" />
	<xsl:param name="i18n.identifier" />
	<xsl:param name="i18n.language" />
	<xsl:param name="i18n.publisher" />
	<xsl:param name="i18n.relation" />
	<xsl:param name="i18n.rights" />
	<xsl:param name="i18n.source" />
	<xsl:param name="i18n.subject" />
	<xsl:param name="i18n.type" />



	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="metadata">
		<xsl:if test="dc:title/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.title" />
				</div>
				<xsl:for-each select="dc:title">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:description/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.description" />
				</div>
				<xsl:for-each select="dc:description">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:contributor/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.contributor" />
				</div>
				<xsl:for-each select="dc:contributor">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:coverage/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.coverage" />
				</div>
				<xsl:for-each select="dc:coverage">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:creator/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.creator" />
				</div>
				<xsl:for-each select="dc:creator">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:date/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.date" />
				</div>
				<xsl:for-each select="dc:date">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:format/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.format" />
				</div>
				<xsl:for-each select="dc:format">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:identifier/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.identifier" />
				</div>
				<xsl:for-each select="dc:identifier">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:language/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.language" />
				</div>
				<xsl:for-each select="dc:language">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:publisher/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.publisher" />
				</div>
				<xsl:for-each select="dc:publisher">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:relation/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.relation" />
				</div>
				<xsl:for-each select="dc:relation">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:rights/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.rights" />
				</div>
				<xsl:for-each select="dc:rights">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:source/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.source" />
				</div>
				<xsl:for-each select="dc:source">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:subject/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.subject" />
				</div>
				<xsl:for-each select="dc:subject">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="dc:type/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">
					<xsl:value-of select="$i18n.type" />
				</div>
				<xsl:for-each select="dc:type">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
