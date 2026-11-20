<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ea="https://roda-community.org/schemas/emailarchive/v1"
    exclude-result-prefixes="xs ea">

  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>

  <xsl:template match="/">
    <doc>
      <xsl:apply-templates/>
    </doc>
  </xsl:template>

  <xsl:template match="*:emailArchive">
    <!-- Parent-level Solr fields -->
    <field name="custodian_txt"><xsl:value-of select="*:custodian/text()"/></field>
    <field name="emailAddress_s"><xsl:value-of select="*:emailAddress/text()"/></field>
    <!-- These fields are only emitted when present, to avoid possible issues with emitting empty fields -->
    <xsl:if test="*:dateStart castable as xs:date">
      <field name="dateStart_dt"><xsl:value-of select="format-date(xs:date(*:dateStart), '[Y0001]-[M01]-[D01]')"/>T00:00:00Z</field>
    </xsl:if>
    <xsl:if test="*:dateEnd castable as xs:date">
      <field name="dateEnd_dt"><xsl:value-of select="format-date(xs:date(*:dateEnd), '[Y0001]-[M01]-[D01]')"/>T00:00:00Z</field>
    </xsl:if>
    <xsl:if test="*:totalMessages castable as xs:integer">
      <field name="totalMessages_i"><xsl:value-of select="xs:integer(*:totalMessages)"/></field>
    </xsl:if>
    <field name="originalFormat_s"><xsl:value-of select="*:originalFormat/text()"/></field>
    <field name="archivingMotive_txt"><xsl:value-of select="*:archivingMotive/text()"/></field>
    <field name="content_type">emailarchive</field>

    <!-- NESTED DOCUMENTS — one Solr child document per email -->
    <xsl:if test="*:email">
      <field name="emails">
        <xsl:for-each select="*:email">
          <doc>
            <field name="content_type">email</field>
            <field name="messageId_s"><xsl:value-of select="*:messageId/text()"/></field>
            <field name="subject_txt"><xsl:value-of select="*:subject/text()"/></field>
            <field name="sender_s"><xsl:value-of select="*:sender/text()"/></field>
            <field name="recipients_txt"><xsl:value-of select="*:recipients/text()"/></field>
            <xsl:if test="*:sentDate castable as xs:dateTime">
              <!-- Normalize to UTC; a value without timezone is taken as UTC -->
              <field name="sentDate_dt"><xsl:value-of select="format-dateTime(adjust-dateTime-to-timezone(xs:dateTime(*:sentDate), xs:dayTimeDuration('PT0S')), '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]')"/>Z</field>
            </xsl:if>
            <field name="folderPath_s"><xsl:value-of select="*:folderPath/text()"/></field>
            <xsl:if test="*:hasAttachments castable as xs:boolean">
              <field name="hasAttachments_b"><xsl:value-of select="xs:boolean(*:hasAttachments)"/></field>
            </xsl:if>
            <field name="filePath_s"><xsl:value-of select="*:filePath/text()"/></field>
          </doc>
        </xsl:for-each>
      </field>
    </xsl:if>

    <xsl:apply-templates/>
  </xsl:template>

  <!-- Suppress email child nodes from top-level field processing -->
  <xsl:template match="*:email"/>

</xsl:stylesheet>
