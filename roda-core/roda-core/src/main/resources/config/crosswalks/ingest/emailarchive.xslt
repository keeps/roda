<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ea="https://roda-community.org/schemas/emailarchive/v1"
    exclude-result-prefixes="ea">

  <xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes"/>

  <xsl:template match="/">
    <doc>
      <xsl:apply-templates/>
    </doc>
  </xsl:template>

  <xsl:template match="*:emailArchive">
    <!-- Mailbox-level fields (parent AIP) -->
    <xsl:if test="normalize-space(*:custodian/text()) != ''">
      <field name="custodian_txt"><xsl:value-of select="normalize-space(*:custodian/text())"/></field>
    </xsl:if>
    <xsl:if test="normalize-space(*:emailAddress/text()) != ''">
      <field name="emailAddress_s"><xsl:value-of select="normalize-space(*:emailAddress/text())"/></field>
    </xsl:if>
    <xsl:if test="normalize-space(*:dateStart/text()) != ''">
      <field name="dateStart_dt"><xsl:value-of select="normalize-space(*:dateStart/text())"/>T00:00:00Z</field>
    </xsl:if>
    <xsl:if test="normalize-space(*:dateEnd/text()) != ''">
      <field name="dateEnd_dt"><xsl:value-of select="normalize-space(*:dateEnd/text())"/>T00:00:00Z</field>
    </xsl:if>
    <xsl:if test="normalize-space(*:totalMessages/text()) != ''">
      <field name="totalMessages_i"><xsl:value-of select="normalize-space(*:totalMessages/text())"/></field>
    </xsl:if>
    <xsl:if test="normalize-space(*:originalFormat/text()) != ''">
      <field name="originalFormat_s"><xsl:value-of select="normalize-space(*:originalFormat/text())"/></field>
    </xsl:if>
    <xsl:if test="normalize-space(*:archivingMotive/text()) != ''">
      <field name="archivingMotive_txt"><xsl:value-of select="normalize-space(*:archivingMotive/text())"/></field>
    </xsl:if>
    <field name="content_type">emailarchive</field>

    <!-- Nested child documents — one per email record -->
    <xsl:if test="count(*:email) &gt; 0">
      <field name="emails">
        <xsl:for-each select="*:email">
          <doc>
            <field name="content_type">email</field>
            <xsl:if test="normalize-space(*:messageId/text()) != ''">
              <field name="messageId_s"><xsl:value-of select="normalize-space(*:messageId/text())"/></field>
            </xsl:if>
            <xsl:if test="normalize-space(*:subject/text()) != ''">
              <field name="subject_txt"><xsl:value-of select="normalize-space(*:subject/text())"/></field>
            </xsl:if>
            <xsl:if test="normalize-space(*:sender/text()) != ''">
              <field name="sender_s"><xsl:value-of select="normalize-space(*:sender/text())"/></field>
            </xsl:if>
            <xsl:for-each select="*:recipients">
              <xsl:if test="normalize-space(text()) != ''">
                <field name="recipients_txt"><xsl:value-of select="normalize-space(text())"/></field>
              </xsl:if>
            </xsl:for-each>
            <xsl:if test="normalize-space(*:sentDate/text()) != ''">
              <field name="sentDate_dt"><xsl:value-of select="normalize-space(*:sentDate/text())"/></field>
            </xsl:if>
            <xsl:if test="normalize-space(*:folderPath/text()) != ''">
              <field name="folderPath_s"><xsl:value-of select="normalize-space(*:folderPath/text())"/></field>
            </xsl:if>
            <xsl:if test="normalize-space(*:hasAttachments/text()) != ''">
              <field name="hasAttachments_b"><xsl:value-of select="normalize-space(*:hasAttachments/text())"/></field>
            </xsl:if>
            <xsl:if test="normalize-space(*:filePath/text()) != ''">
              <field name="filePath_s"><xsl:value-of select="normalize-space(*:filePath/text())"/></field>
            </xsl:if>
          </doc>
        </xsl:for-each>
      </field>
    </xsl:if>

    <xsl:apply-templates/>
  </xsl:template>

  <!-- Suppress email child nodes from top-level field processing -->
  <xsl:template match="*:email"/>

</xsl:stylesheet>
