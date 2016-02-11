<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:prem="info:lc/xmlns/premis-v2"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	exclude-result-prefixes="prem">
	<!-- <xsl:strip-space elements="*" /> -->
	<xsl:preserve-space elements="*" />
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:param name="aipID" />
	<xsl:param name="representationID" />
	<xsl:param name="fileID" />
	
	<xsl:template match="/">
		<doc>
			<xsl:apply-templates />
		</doc>
	</xsl:template>
	<xsl:template match="prem:agent">
		<xsl:if test="prem:agentIdentifier/prem:agentIdentifierValue">
			<field name="id">
				<xsl:value-of select="prem:agentIdentifier/prem:agentIdentifierValue/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:agentName">
			<field name="title">
				<xsl:value-of select="prem:agentName/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:agentType">
			<field name="type">
				<xsl:value-of select="prem:agentType/text()" />
			</field>
		</xsl:if>

	</xsl:template>
	<xsl:template match="prem:object">
		<xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "representation")'>
			<!-- INDEX REPRESENTATION PROPERTIES -->
		</xsl:if>
		<xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "file")'>
		</xsl:if>
	</xsl:template>
	<xsl:template match="prem:event">
		<xsl:if test="$aipID">
			<field name="aipID">
				<xsl:value-of select="$aipID" />
			</field>
		</xsl:if>
		<xsl:if test="$representationID">
			<field name="representationID">
				<xsl:value-of select="$representationID" />
			</field>
		</xsl:if>
		<xsl:if test="$fileID">
			<field name="fileID">
				<xsl:value-of select="$fileID" />
			</field>
		</xsl:if>
		<xsl:if test="prem:eventDateTime">
			<field name="eventDateTime">
				<xsl:value-of select="prem:eventDateTime/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:eventDetail">
			<field name="eventDetail">
				<xsl:value-of select="prem:eventDetail/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:eventType">
			<field name="eventType">
				<xsl:value-of select="prem:eventType/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:eventOutcomeInformation">
			<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcome">
				<field name="eventOutcome">
					<xsl:value-of select="prem:eventOutcomeInformation/prem:eventOutcome/text()" />
				</field>
			</xsl:if>
			<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcomeDetail">
				<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension">
					<field name="eventOutcomeDetailExtension">
						<xsl:value-of select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension/text()" />
					</field>
				</xsl:if>
				<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote">
					<field name="eventOutcomeDetailNote">
						<xsl:value-of select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote/text()" />
					</field>
				</xsl:if>
			</xsl:if>
			
		</xsl:if>
		<xsl:if test="prem:eventIdentifier">
			<xsl:if test="prem:eventIdentifier/prem:eventIdentifierValue">
				<field name="id">
					<xsl:value-of select="prem:eventIdentifier/prem:eventIdentifierValue/text()" />
				</field>
			</xsl:if>
		</xsl:if>
		<xsl:if test="prem:linkingAgentIdentifier">
			<xsl:for-each select="prem:linkingAgentIdentifier">
				<xsl:if test="prem:linkingAgentIdentifierValue">
					<field name="linkingAgentIdentifier">
						<xsl:value-of select="prem:linkingAgentIdentifierValue/text()" />
					</field>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>
		<xsl:if test="prem:linkingObjectIdentifier">
			<xsl:for-each select="prem:linkingObjectIdentifier">
				<xsl:if test="prem:linkingObjectIdentifierValue">
					<xsl:if test="prem:linkingObjectIdentifierType/text()='outcome'">
						<field name="linkingOutcomeObjectIdentifier">
							<xsl:value-of select="prem:linkingObjectIdentifierValue/text()" />
						</field>
					</xsl:if>
					<xsl:if test="prem:linkingObjectIdentifierType/text()='source'">
						<field name="linkingSourceObjectIdentifier">
							<xsl:value-of select="prem:linkingObjectIdentifierValue/text()" />
						</field>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
