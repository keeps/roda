<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output indent="yes" method="text" encoding="UTF-8" media-type="text/plain"></xsl:output>


    <xsl:template match="/TaggedPDF-doc">
        <xsl:apply-templates select="Table"></xsl:apply-templates>
    </xsl:template>


    <xsl:template match="Table">
        <xsl:result-document href="risks_json/urn:drambora:r{position()}.json">
            {

            <xsl:variable name="categories" select="string-join(./TR/TD/text()[contains(., 'X ')]/../preceding-sibling::TD/normalize-space(text()), '; ')"/>
            "category": "<xsl:value-of select="$categories"/>",

                <xsl:apply-templates select="TR"/>
                "identifiedBy":"DRAMBORA - Digital Repository Audit Method Based on Risk Assessment",
                "mitigationOwnerType": "Human",
                "createdBy": "admin",
                "createdOn": 1459465200000,
                "updatedBy": "admin",
                "updatedOn": 1459465200000,
                "identifiedOn": "1459465200000"
            }
        </xsl:result-document>
    </xsl:template>

    <xsl:template match="TR/TH[contains(text(), 'Risk Identifier:')]">
        "id": "urn:drambora:<xsl:value-of select="lower-case(normalize-space(following-sibling::TH/text()))"/>",
    </xsl:template>

    <xsl:template match="TR/TH[contains(text(), 'Risk Name:')]">
        "name": "<xsl:value-of select="normalize-space(following-sibling::TD/text())"/>",
    </xsl:template>

    <xsl:template match="TR/TH[contains(text(), 'Risk Description:')]">
        "description": "<xsl:value-of select="replace(following-sibling::TD/text(), '&#10;', '\\n')"/>",
    </xsl:template>

    <!--xsl:template match="TR/TH[contains(text(), 'Nature of Risk:')]">
        "category": "<xsl:value-of select="following-sibling::TD/text()"/>",
    </xsl:template-->

    <!--
    <xsl:template match="TR/TD[text() = 'X ']">
        "category": "<xsl:value-of select="normalize-space(preceding-sibling::TD/text())"/>",
    </xsl:template>
    -->

    <xsl:template match="TR/TH[contains(text(), 'Is this Risk Relevant?:')]">
        "notes": "<xsl:value-of select="replace(following-sibling::TD/text(), '&#10;', '\\n')"/>\n\nThe full description of this risk has been obtained from examples provided in DRAMBORA - Digital Repository Audit Method Based on Risk Assessment, March 2007, Digital Curation Centre (DCC) and Digital Preservation Europe (DPE), http://www.repositoryaudit.eu/",
    </xsl:template>

    <xsl:template match="TR/TH[contains(text(), 'Risk Probability:')]">
        "preMitigationProbability": <xsl:value-of select="following-sibling::TD/text()"/>,
    </xsl:template>

    <xsl:template match="TR/TH[contains(text(), 'Risk Potential Impact:')]">
        "preMitigationImpact": <xsl:value-of select="normalize-space(following-sibling::TD/text())"/>,
    </xsl:template>

    <xsl:template match="TR/TH[contains(text(), 'Risk Severity:')]">
        "preMitigationSeverity": <xsl:value-of select="normalize-space(following-sibling::TD/text())"/>,
        "preMitigationSeverityLevel": "MODERATE",
    </xsl:template>


    <xsl:template match="TR/TH[contains(text(), 'Example Risk Manifestation(s):')]">
        "preMitigationNotes": "<xsl:value-of select="replace(following-sibling::TD/text(), '&#10;', '\\n')"/>",
    </xsl:template>

    <xsl:template match="TR/TH[contains(text(), 'Mitigation strategy(ies):')]">
        "mitigationStrategy": "<xsl:value-of select="replace(following-sibling::TD/text(), '&#10;', '\\n')"/>",
    </xsl:template>


    <xsl:template match="TR/TH[starts-with(text(), 'Owner:')]">
        "mitigationOwner": "<xsl:value-of select="normalize-space(following-sibling::TD/text())"/>",
    </xsl:template>


    <xsl:template match="text()" priority="-1"></xsl:template>


</xsl:stylesheet>
