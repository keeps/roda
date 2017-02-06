<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet  [
	<!ENTITY crarr  "&#13;">
	<!ENTITY crarr  "&#xD;">
]>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:premis="http://www.loc.gov/premis/v3">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />

	<xsl:param name="i18n.identifierType" />
	<xsl:param name="i18n.identifierValue" />
	<xsl:param name="i18n.type" />
	<xsl:param name="i18n.dateTime" />
	<xsl:param name="i18n.detail" />
	<xsl:param name="i18n.outcome" />

	<xsl:param name="i18n.outcomeDetailNote" />
	<xsl:param name="i18n.outcomeDetailExtension" />
	<xsl:param name="onlyDetails" />
	
	<xsl:template match="/">
			<xsl:if test="$onlyDetails=('false','False')">
				<xsl:if test="//premis:eventIdentifierType/text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.identifierType" />
						</div>
						<div class="value">
							<xsl:value-of select="//premis:eventIdentifierType/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="//premis:eventIdentifierValue/text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.identifierValue" />
						</div>
						<div class="value">
							<xsl:value-of select="//premis:eventIdentifierValue/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="//premis:eventType/text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.type" />
						</div>
						<div class="value">
							<xsl:value-of select="//premis:eventType/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="//premis:eventDateTime/text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.dateTime" />
						</div>
						<div class="value">
							<xsl:value-of select="//premis:eventDateTime/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="//premis:eventDetail/text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.detail" />
						</div>
						<div class="value">
							<xsl:value-of select="//premis:eventDetail/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="//premis:eventOutcome/text()">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.outcome" />
						</div>
						<div class="value">
							<xsl:value-of select="//premis:eventOutcome/text()" />
						</div>
					</div>
				</xsl:if>
			</xsl:if>
	
			<xsl:if test="//premis:eventOutcomeDetailNote/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.outcomeDetailNote" />
					</div>
					<div class="value code-pre">
						<xsl:value-of select="//premis:eventOutcomeDetailNote/text()" />
					</div>
				</div>
			</xsl:if>
			
			<xsl:if test="//premis:eventOutcomeDetailExtension/text()">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.outcomeDetailNote" />
					</div>
					<div class="value code-pre">
						<xsl:value-of select="//premis:eventOutcomeDetailExtension/text()" />
					</div>
				</div>
			</xsl:if>
	</xsl:template>
</xsl:stylesheet>