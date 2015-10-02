<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:prem="info:lc/xmlns/premis-v2"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

	<xsl:output method="html" indent="yes"/>

	<xsl:param name="fromEvent" />
	<xsl:param name="maxEvents" />
	<xsl:param name="fromAgent" />
	<xsl:param name="maxAgents" />
	<xsl:param name="fromFile" />
	<xsl:param name="maxFiles" />

    <xsl:param name="binaryToHtml.premis.representation" />
    <xsl:param name="binaryToHtml.premis.preservationlevel" />
    <xsl:param name="binaryToHtml.premis.since" />
    <xsl:param name="binaryToHtml.premis.derivedfrom" />
    <xsl:param name="binaryToHtml.premis.size" />
    <xsl:param name="binaryToHtml.premis.format" />
    <xsl:param name="binaryToHtml.premis.compositionlevel" />
    <xsl:param name="binaryToHtml.premis.creatingapplicationname" />
    <xsl:param name="binaryToHtml.premis.creatingapplicationversion" />
    <xsl:param name="binaryToHtml.premis.detailedcharacteristics" />
    <xsl:param name="binaryToHtml.premis.files" />
    <xsl:param name="binaryToHtml.premis.events" />
    <xsl:param name="binaryToHtml.premis.agents" />
    <xsl:param name="binaryToHtml.premis.details" />
    <xsl:param name="binaryToHtml.premis.result" />
    <xsl:param name="binaryToHtml.premis.resultnote" />
    <xsl:param name="binaryToHtml.premis.resultextension" />
    <xsl:param name="binaryToHtml.premis.relatedagents" />
    <xsl:param name="binaryToHtml.premis.relatedobjects" />
    <xsl:param name="binaryToHtml.premis.throughevent" />

    <xsl:variable name="filenames" select="//file"/>
    
    
    
    <xsl:variable name="agents">
      <xsl:for-each select="$filenames">
        <xsl:apply-templates select="document(.)" mode="agent" />
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="events">
      <xsl:for-each select="$filenames">
        <xsl:apply-templates select="document(.)" mode="event" />
      </xsl:for-each>
    </xsl:variable>
    <xsl:variable name="files">
      <xsl:for-each select="$filenames">
        <xsl:apply-templates select="document(.)" mode="file" />
      </xsl:for-each>
    </xsl:variable>
    
    <xsl:template match="/">
        <xsl:apply-templates />
    </xsl:template>

    <xsl:template match="prem:object" mode="agent">

    </xsl:template>
    <xsl:template match="prem:event" mode="agent">

    </xsl:template>
    <xsl:template match="prem:agent" mode="agent">
        <span class="agent">
            <xsl:attribute name="id">
                <xsl:value-of
				select="normalize-space(prem:agentIdentifier[1]/prem:agentIdentifierValue/text())" />
            </xsl:attribute>
            <span class="header">
                <xsl:if test="prem:agentName">
                    <span class="agentName">
                        <xsl:value-of select="normalize-space(prem:agentName/text())" />
                    </span>
                </xsl:if>
                <xsl:if test="prem:agentIdentifier/prem:agentIdentifierType/text()">
                    <xsl:for-each select="prem:agentIdentifier">
                        <span class="identifier field">
                            <span class="identifierType field-label">
                                <xsl:value-of select="normalize-space(prem:agentIdentifierType/text())" />
                            </span>
                            <span class="identifierValue field-value">
                                <xsl:value-of
									select="normalize-space(prem:agentIdentifierValue/text())" />
                            </span>
                        </span>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="prem:agentType">
                    <span class="field agentType">
                        <span class="field-value">
                            <xsl:value-of select="normalize-space(prem:agentType/text())" />
                        </span>
                    </span>
                </xsl:if>
            </span>
        </span>
    </xsl:template>


    <xsl:template match="prem:object" mode="representation">
        <xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "representation")'>
            <span xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				class="representation">
                <xsl:attribute name="id">
                    <xsl:value-of
					select="normalize-space(prem:objectIdentifier[1]/prem:objectIdentifierValue/text())" />
                </xsl:attribute>
                <span class="header">
                    <xsl:value-of select="$binaryToHtml.premis.representation" />
                    <xsl:if test="prem:objectIdentifier">
                        <span class="identifiers">
                            <xsl:for-each select="prem:objectIdentifier">
                                <span class="identifier field">
                                    <span class="identifierType field-label">
                                        <xsl:value-of
											select="normalize-space(prem:objectIdentifierType/text())" />
                                    </span>
                                    <span class="identifierValue field-value">
                                        <xsl:value-of
											select="normalize-space(prem:objectIdentifierValue/text())" />
                                    </span>
                                </span>
                            </xsl:for-each>
                        </span>
                    </xsl:if>
                </span>
                <xsl:if test="prem:preservationLevel">
                    <xsl:for-each select="prem:preservationLevel">
                        <span class="preservationLevel">
                            <xsl:value-of select="$binaryToHtml.premis.preservationlevel" />
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="prem:preservationLevelValue/text()" />
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="$binaryToHtml.premis.since" />
                            <xsl:text> </xsl:text>
                            <xsl:value-of select="prem:preservationLevelDateAssigned/text()" />
                        </span>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="prem:relationship">
                    <xsl:for-each select="prem:relationship">
                        <xsl:if test="prem:relationshipType/text()='derivation'">
                            <span class="derivation">
                                <xsl:value-of select="$binaryToHtml.premis.derivedfrom" />
                                <xsl:text> </xsl:text>
                                <a>
                                    <xsl:attribute name="href">
                                        #<xsl:value-of
										select="prem:relatedObjectIdentification/prem:relatedObjectIdentifierValue/text()" />
                                    </xsl:attribute>

                                    <xsl:value-of
										select="prem:relatedObjectIdentification/prem:relatedObjectIdentifierValue/text()" />
                                </a>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select="$binaryToHtml.premis.throughevent" />
                                <xsl:text> </xsl:text>
                                <a>
                                    <xsl:attribute name="href">
                                        #<xsl:value-of
										select="prem:relatedEventIdentification/prem:relatedEventIdentifierValue/text()" />
                                    </xsl:attribute>
                                    <xsl:value-of
										select="prem:relatedEventIdentification/prem:relatedEventIdentifierValue/text()" />
                                </a>
                            </span>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>
            </span>
        </xsl:if>
        <xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "file")'>
            
        </xsl:if>

        <xsl:if test="prem:relationship">
            <span class="section files">
                <span class="sectionTitle">
                    <xsl:value-of select="$binaryToHtml.premis.files" />
                </span>
                <span class="sectionContent">
                    <xsl:copy-of select="$files"/>
                </span>
            </span>
                <span class="section events">
                    <span class="sectionTitle">
                        <xsl:value-of select="$binaryToHtml.premis.events" />
                    </span>
                    <span class="sectionContent">
                        <xsl:copy-of select="$events"/>
                    </span>
                </span>
                <span class="section agents">
                    <span class="sectionTitle">
                        <xsl:value-of select="$binaryToHtml.premis.agents" />
                    </span>
                    <span class="sectionContent">
                        <xsl:copy-of select="$agents"/>
                    </span>
                </span>
        </xsl:if>
    </xsl:template>

    <xsl:template match="prem:agent" mode="representation"/>
    <xsl:template match="prem:event" mode="representation"/>
    <xsl:template match="prem:object" mode="file">
        <xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "representation")'>

        </xsl:if>
        <xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "file")'>
            <span class="file">
                <xsl:attribute name="id">
                    <xsl:value-of
					select="normalize-space(prem:objectIdentifier[1]/prem:objectIdentifierValue/text())" />
                </xsl:attribute>
                <span class="header toggle-next">
                    <xsl:if test="prem:originalName">
                        <span class="originalName">
                            <xsl:value-of select="normalize-space(prem:originalName/text())" />
                        </span>
                    </xsl:if>
                    <xsl:if test="prem:objectIdentifier">
                        <span class="identifiers">
                            <xsl:for-each select="prem:objectIdentifier">
                                <span class="identifier field">
                                    <span class="identifierType field-label">
                                        <xsl:value-of
											select="normalize-space(prem:objectIdentifierType/text())" />
                                    </span>
                                    <span class="identifierValue field-value">
                                        <xsl:value-of
											select="normalize-space(prem:objectIdentifierValue/text())" />
                                    </span>
                                </span>
                            </xsl:for-each>
                        </span>
                    </xsl:if>
                    <xsl:if test="prem:objectCharacteristics/prem:size">
                        <span class="field size">
                            <span class="field-label">
                                <xsl:value-of select="$binaryToHtml.premis.size" />
                            </span>
                            <span class="field-value">
                                <xsl:value-of select="prem:objectCharacteristics/prem:size/text()" />
                            </span>
                        </span>
                    </xsl:if>
                    <xsl:if test="prem:objectCharacteristics/prem:format">
                        <span class="field format">
                            <span class="field-label">
                                <xsl:value-of select="$binaryToHtml.premis.format" />
                            </span>
                            <span class="field-value">
                                <xsl:if
									test="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName">
                                    <span class="formatName">
                                        <xsl:value-of
											select="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName/text()" />
                                    </span>
                                </xsl:if>
                                <xsl:if
									test="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryName">
                                    <span class="formatRegistry">
                                        <span class="formatRegistryName">
                                            <xsl:value-of
												select="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryName/text()" />
                                        </span>
                                        <xsl:if
											test="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryKey">
                                            <span class="formatRegistryKey">
                                                <xsl:value-of
													select="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryKey/text()" />
                                            </span>
                                        </xsl:if>
                                    </span>
                                </xsl:if>
                            </span>
                        </span>
                    </xsl:if>
                </span>
                <span class="content">
                    <xsl:if test="prem:objectCharacteristics/prem:fixity">
                        <span class="field fixity">
                            <span class="field-label">Fixity</span>
                            <span class="field-value">
                                <table>
                                    <thead>
                                        <tr>
                                            <td>Algorithm</td>
                                            <td>Value</td>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <xsl:for-each select="prem:objectCharacteristics/prem:fixity">
                                            <tr>
                                                <td>
                                                    <xsl:value-of select="prem:messageDigestAlgorithm/text()" />
                                                </td>
                                                <td>
                                                    <xsl:value-of select="prem:messageDigest/text()" />
                                                </td>
                                            </tr>
                                        </xsl:for-each>
                                    </tbody>
                                </table>
                            </span>
                        </span>
                    </xsl:if>

                    <xsl:if test="prem:objectCharacteristics/prem:compositionLevel">
                        <span class="field compositionLevel">
                            <span class="field-label">
                                <xsl:value-of select="$binaryToHtml.premis.compositionlevel" />
                            </span>
                            <span class="field-value">
                                <xsl:value-of
									select="prem:objectCharacteristics/prem:compositionLevel/text()" />
                            </span>
                        </span>
                    </xsl:if>

                    <xsl:if test="prem:objectCharacteristics/prem:creatingApplication">
                        <xsl:if
							test="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationName">
                            <span class="field creatingApplicationName">
                                <span class="field-label">
                                    <xsl:value-of select="$binaryToHtml.premis.creatingapplicationname" />
                                </span>
                                <span class="field-value">
                                    <xsl:value-of
										select="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationName/text()" />
                                </span>
                            </span>
                        </xsl:if>
                        <xsl:if
							test="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationVersion">
                            <span class="field creatingApplicationVersion">
                                <span class="field-label">
                                    <xsl:value-of select="$binaryToHtml.premis.creatingapplicationversion" />
                                </span>
                                <span class="field-value">
                                    <xsl:value-of
										select="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationVersion/text()" />
                                </span>
                            </span>
                        </xsl:if>
                        <xsl:if test="prem:storage/prem:contentLocation">
                            <span class="field contentLocation">
                                <span class="field-label">Localização física</span>
                                <span class="field-value">
                                    <xsl:if
										test="prem:storage/prem:contentLocation/prem:contentLocationType">
                                        <span class="contentLocationType">
                                            <xsl:value-of
												select="prem:storage/prem:contentLocation/prem:contentLocationType/text()" />
                                        </span>
                                    </xsl:if>
                                    <xsl:if
										test="prem:storage/prem:contentLocation/prem:contentLocationValue">
                                        <xsl:value-of
											select="prem:storage/prem:contentLocation/prem:contentLocationValue/text()" />
                                    </xsl:if>
                                </span>
                            </span>
                        </xsl:if>
                    </xsl:if>
                    <xsl:if
						test="prem:objectCharacteristics/prem:objectCharacteristicsExtension">
                        <span class="field objectCharacteristicsExtension">
                            <span class="field-label">
                                <xsl:value-of select="$binaryToHtml.premis.detailedcharacteristics" />
                            </span>
                            <span class="field-value">
                                <pre>
                                    <code>
                                        <xsl:apply-templates mode="escape"
											select="prem:objectCharacteristics/prem:objectCharacteristicsExtension/*" />

                                        <!-- <xsl:copy-of -->
                                        <!-- select="prem:objectCharacteristics/prem:objectCharacteristicsExtension/*" 
											/> -->

                                    </code>
                                </pre>
                            </span>
                        </span>
                    </xsl:if>
                </span>
            </span>
        </xsl:if>

    </xsl:template>
    <xsl:template match="prem:agent" mode="file"/>
    <xsl:template match="prem:event" mode="file"/>
    <xsl:template match="prem:event" mode="event">
        <span class="event">
            <xsl:attribute name="id">
                <xsl:value-of
				select="normalize-space(prem:eventIdentifier[1]/prem:eventIdentifierValue/text())" />
            </xsl:attribute>
            <span class="header toggle-next">
                <xsl:if test="prem:eventType">
                    <span class="type">
                        <xsl:value-of select="prem:eventType/text()" />
                    </span>
                </xsl:if>
                <xsl:if test="prem:eventIdentifier">
                    <xsl:for-each select="prem:eventIdentifier">
                        <span class="identifier field">
                            <span class="identifierType field-label">
                                <xsl:value-of select="prem:eventIdentifierType/text()" />
                            </span>
                            <span class="identifierValue field-value">
                                <xsl:value-of select="prem:eventIdentifierValue/text()" />
                            </span>
                        </span>
                    </xsl:for-each>
                </xsl:if>
                <xsl:if test="prem:eventDateTime">
                    <span class="datetime field">
                        <span class="field-value">
                            <xsl:value-of select="prem:eventDateTime/text()" />
                        </span>
                    </span>
                </xsl:if>
            </span>
            <span class="content">
                <xsl:if test="prem:eventDetail">
                    <span class="field eventDetail">
                        <span class="field-label">
                            <xsl:value-of select="$binaryToHtml.premis.details" />
                        </span>
                        <span class="field-value">
                            <xsl:value-of select="prem:eventDetail/text()" />
                        </span>
                    </span>
                </xsl:if>
                <xsl:if test="prem:eventOutcomeInformation/prem:eventOutcome">
                    <span class="field eventOutcome">
                        <span class="field-label">
                            <xsl:value-of select="$binaryToHtml.premis.result" />
                        </span>
                        <span class="field-value">
                            <xsl:value-of
								select="prem:eventOutcomeInformation/prem:eventOutcome/text()" />
                        </span>
                    </span>
                </xsl:if>
                <xsl:if
					test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote">
                    <span class="field eventOutcome">
                        <span class="field-label">
                            <xsl:value-of select="$binaryToHtml.premis.resultnote" />
                        </span>
                        <span class="field-value">
                            <xsl:value-of
								select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote/text()" />
                        </span>
                    </span>
                </xsl:if>
                <xsl:if
					test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension">
                    <span class="field eventOutcome">
                        <span class="field-label">
                            <xsl:value-of select="$binaryToHtml.premis.resultextension" />
                        </span>
                        <span class="field-value">
                            <pre>
                                <code>
                                    <xsl:apply-templates mode="escape"
										select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension/*" />
                                </code>
                            </pre>
                        </span>
                    </span>
                </xsl:if>
                <xsl:if test="prem:linkingAgentIdentifier">
                    <span class="field linkingAgents">
                        <span class="field-label">
                            <xsl:value-of select="$binaryToHtml.premis.relatedagents" />
                        </span>
                        <span class="field-value">
                            <xsl:for-each select="prem:linkingAgentIdentifier">
                                <a>
                                    <xsl:attribute name="href">
                                        #<xsl:value-of
										select="prem:linkingAgentIdentifierValue/text()" />
                                    </xsl:attribute>
                                    <span class="linkingAgent">
                                        <span class="type">
                                            <xsl:value-of select="prem:linkingAgentIdentifierType/text()" />
                                        </span>
                                        <span class="value">
                                            <xsl:value-of select="prem:linkingAgentIdentifierValue/text()" />
                                        </span>
                                        <span class="role">
                                            <xsl:value-of select="prem:linkingAgentRole/text()" />
                                        </span>
                                    </span>
                                </a>
                            </xsl:for-each>
                        </span>
                    </span>
                </xsl:if>
                <xsl:if test="prem:linkingObjectIdentifier">
                    <span class="field linkingObjects">
                        <span class="field-label">
                            <xsl:value-of select="$binaryToHtml.premis.relatedobjects" />
                        </span>
                        <span class="field-value">
                            <xsl:for-each select="prem:linkingObjectIdentifier">
                                <a>
                                    <xsl:attribute name="href">
                                        #<xsl:value-of
										select="prem:linkingObjectIdentifierValue/text()" />
                                    </xsl:attribute>
                                    <span class="linkingObject">
                                        <span class="type">
                                            <xsl:value-of select="prem:linkingObjectIdentifierType/text()" />
                                        </span>
                                        <span class="value">
                                            <xsl:value-of select="prem:linkingObjectIdentifierValue/text()" />
                                        </span>
                                        <span class="role">
                                            <xsl:value-of select="prem:linkingObjectRole/text()" />
                                        </span>
                                    </span>
                                </a>
                            </xsl:for-each>
                        </span>
                    </span>
                </xsl:if>
            </span>
        </span>
    </xsl:template>

    <xsl:template match="prem:agent" mode="event"/>
    <xsl:template match="prem:object" mode="event"/>
    <xsl:template match="*" mode="escape">
        <!-- Begin opening tag -->
        <xsl:text>&lt;</xsl:text>
        <xsl:value-of select="name()" />

        <!-- Namespaces -->
        <!-- <xsl:for-each select="namespace::*"> <xsl:text> xmlns</xsl:text> <xsl:if 
			test="name() != ''"> <xsl:text>:</xsl:text> <xsl:value-of select="name()"/> 
			</xsl:if> <xsl:text>='</xsl:text> <xsl:call-template name="escape-xml"> <xsl:with-param 
			name="text" select="."/> </xsl:call-template> <xsl:text>'</xsl:text> </xsl:for-each> -->

        <!-- Attributes -->
        <xsl:for-each select="@*">
            <xsl:text> </xsl:text>
            <xsl:value-of select="name()" />
            <xsl:text>='</xsl:text>
            <xsl:call-template name="escape-xml">
                <xsl:with-param name="text" select="." />
            </xsl:call-template>
            <xsl:text>'</xsl:text>
        </xsl:for-each>

        <!-- End opening tag -->
        <xsl:text>&gt;</xsl:text>

        <!-- Content (child elements, text nodes, and PIs) -->
        <xsl:apply-templates select="node()" mode="escape" />

        <!-- Closing tag -->
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()" />
        <xsl:text>&gt;</xsl:text>
    </xsl:template>

    <xsl:template match="text()" mode="escape">
        <xsl:call-template name="escape-xml">
            <xsl:with-param name="text" select="." />
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="processing-instruction()" mode="escape">
        <xsl:text>&lt;?</xsl:text>
        <xsl:value-of select="name()" />
        <xsl:text> </xsl:text>
        <xsl:call-template name="escape-xml">
            <xsl:with-param name="text" select="." />
        </xsl:call-template>
        <xsl:text>?&gt;</xsl:text>
    </xsl:template>

    <xsl:template name="escape-xml">
        <xsl:param name="text" />
        <xsl:if test="$text != ''">
            <xsl:variable name="head" select="substring($text, 1, 1)" />
            <xsl:variable name="tail" select="substring($text, 2)" />
            <xsl:choose>
                <xsl:when test="$head = '&amp;'">
                    &amp;amp;
                </xsl:when>
                <xsl:when test="$head = '&lt;'">
                    &amp;lt;
                </xsl:when>
                <xsl:when test="$head = '&gt;'">
                    &amp;gt;
                </xsl:when>
                <xsl:when test="$head = '&quot;'">
                    &amp;quot;
                </xsl:when>
                <xsl:when test="$head = &quot;&apos;&quot;">
                    &amp;apos;
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$head" />
                </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="escape-xml">
                <xsl:with-param name="text" select="$tail" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>
    <xsl:template match="file">
        <xsl:apply-templates select="document(.)" mode="representation" />
    </xsl:template>


















</xsl:stylesheet>