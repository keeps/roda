<?xml version="1.0" encoding="UTF-8"?>
<!-- Premis V2 to V3 -->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:prem2="info:lc/xmlns/premis-v2"
xmlns="http://www.loc.gov/premis/v3"
>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
    <xsl:template match="prem2:event/prem2:eventDetail">
        <eventDetailInformation>
        	<eventDetail>
        		<xsl:apply-templates select="@*|node()" />
        	</eventDetail>
        </eventDetailInformation>
    </xsl:template>
    <xsl:template match="//prem2:relatedObjectIdentification">
        <relatedObjectIdentifier>
        		<xsl:apply-templates select="@*|node()" />
        </relatedObjectIdentifier>
    </xsl:template>
    <xsl:template match="//prem2:relatedEventIdentification">
        <relatedEventIdentifier>
        		<xsl:apply-templates select="@*|node()" />
        </relatedEventIdentifier>
    </xsl:template>
    
    <xsl:template match="prem2:*" xmlns:client="info:lc/xmlns/premis-v2">
	  <xsl:element name="{name()}" namespace="http://www.loc.gov/premis/v3">
	    <xsl:copy-of select="@*"/>
	    <xsl:apply-templates/>
	  </xsl:element>
	</xsl:template>
</xsl:stylesheet>