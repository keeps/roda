<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:prem="http://www.loc.gov/premis/v3"
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
			<field name="name">
				<xsl:value-of select="prem:agentName/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:agentType">
			<field name="type">
				<xsl:value-of select="prem:agentType/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:agentExtension">
			<field name="extension">
				<xsl:value-of select="prem:agentExtension/text()" />
			</field>
		</xsl:if>
		<xsl:if test="prem:agentVersion">
			<field name="version">
				<xsl:value-of select="prem:agentVersion/text()" />
			</field>
		</xsl:if>
	<xsl:if test="prem:agentNote">
			<field name="note">
				<xsl:value-of select="prem:agentNote/text()" />
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
		<xsl:if test="prem:eventDetailInformation/prem:eventDetail">
			<field name="eventDetail">
				<xsl:value-of select="prem:eventDetailInformation/prem:eventDetail/text()" />
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
	</xsl:template>
</xsl:stylesheet>
