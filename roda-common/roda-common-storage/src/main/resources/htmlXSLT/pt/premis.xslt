<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:prem="info:lc/xmlns/premis-v2"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	exclude-result-prefixes="prem">
	<xsl:strip-space elements="*" />
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:param name="events" />
	<xsl:param name="files" />
	<xsl:param name="agents" />
	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>
	<xsl:template match="prem:agent">
		<span class="agent">
			<xsl:if test="prem:agentName">
				<span class="field agentName">
					<xsl:value-of select="normalize-space(prem:agentName/text())" />
				</span>
			</xsl:if>
			<xsl:if test="prem:agentIdentifier/prem:agentIdentifierType/text()">
				<xsl:text>(</xsl:text>
				<xsl:for-each select="prem:agentIdentifier">
					<span class="identifier">
						<span class="identifierType">
							<xsl:value-of select="normalize-space(prem:agentIdentifierType/text())" />
						</span>
						<span class="identifierValue">
							<xsl:value-of
								select="normalize-space(prem:agentIdentifierValue/text())" />
						</span>
					</span>
				</xsl:for-each>
				<xsl:text>)</xsl:text>
			</xsl:if>
			<xsl:if test="prem:agentType">
				<span class="field agentType">
					<xsl:text>Tipo: </xsl:text><xsl:value-of select="normalize-space(prem:agentType/text())" />
				</span>
			</xsl:if>
		</span>
	</xsl:template>
	<xsl:template match="prem:object">
		<xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "representation")'>
			<span class="representation">
				<xsl:text>Representação</xsl:text>
				<xsl:if test="prem:objectIdentifier">
					<xsl:text>(</xsl:text>
					<xsl:for-each select="prem:objectIdentifier">
						<span class="identifier">
							<span class="identifierType">
								<xsl:value-of select="normalize-space(prem:objectIdentifierType/text())" />
							</span>
							<span class="identifierValue">
								<xsl:value-of
									select="normalize-space(prem:objectIdentifierValue/text())" />
							</span>
						</span>
					</xsl:for-each>
					<xsl:text>)</xsl:text>
				</xsl:if>
				<xsl:if test="prem:preservationLevel">
					<span class="field preservationLevel">
						<xsl:text>Nível de preservação:</xsl:text>
						<xsl:for-each select="prem:preservationLevel">
							<xsl:value-of select="prem:preservationLevelValue/text()" />
							<xsl:text> desde </xsl:text>
							<xsl:value-of select="prem:preservationLevelDateAssigned/text()" />
						</xsl:for-each>
					</span>
				</xsl:if>
				<xsl:if test="prem:relationship">
					<xsl:for-each select="prem:relationship">
						<xsl:if test="prem:relationshipType/text()='derivation'">
							<span class="field derivation">
								<xsl:text>Derivado de </xsl:text>
								<a href="/rest/object/XPTO">
									<xsl:value-of
										select="prem:relatedObjectIdentification/prem:relatedObjectIdentifierValue/text()" />
								</a>
								<xsl:text> pelo evento </xsl:text>
								<a href="/rest/event/XPTO">
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
			<span class="file">
				<xsl:text>Ficheiro</xsl:text>
				<xsl:if test="prem:originalName">
					<span class="field originalName">
						<xsl:text>"</xsl:text>
						<xsl:value-of select="normalize-space(prem:originalName/text())" />
						<xsl:text>"</xsl:text>
					</span>
				</xsl:if>
				<xsl:if test="prem:objectIdentifier">
					<xsl:text>(</xsl:text>
					<xsl:for-each select="prem:objectIdentifier">
						<span class=" field identifier">
							<span class="identifierType">
								<xsl:value-of select="normalize-space(prem:objectIdentifierType/text())" />
							</span>
							<span class="identifierValue">
								<xsl:value-of
									select="normalize-space(prem:objectIdentifierValue/text())" />
							</span>
						</span>
					</xsl:for-each>
					<xsl:text>)</xsl:text>
				</xsl:if>
				<xsl:if test="prem:objectCharacteristics/prem:size">
					<span class="field size">
						<xsl:value-of select="prem:objectCharacteristics/prem:size/text()" />
					</span>
					<xsl:text>,</xsl:text>
				</xsl:if>
				<xsl:if test="prem:objectCharacteristics/prem:format">
					<span class="field format">
						<xsl:if
							test="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName">
							<span class="formatName">
								<xsl:value-of
									select="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName/text()" />
							</span>
						</xsl:if>
						<xsl:if
							test="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryName">
							<span class="formatRegistryName">
								<xsl:value-of
									select="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryName/text()" />
							</span>
						</xsl:if>
						<xsl:if
							test="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryKey">
							<span class="formatRegistryKey">
								<xsl:value-of
									select="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryKey/text()" />
							</span>
						</xsl:if>
					</span>
				</xsl:if>
				<span class="otherInfoTitle">
					<xsl:text>Outras características</xsl:text>
				</span>
				<span class="otherFileInfo">
					<xsl:if test="prem:objectCharacteristics/prem:fixity">
						<span class="field fixity">
							<div class="title">Fixity</div>
							<table>
								<tr>
									<td>Algoritmo</td>
									<td>Valor</td>
								</tr>
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
							</table>
						</span>
					</xsl:if>
					<xsl:if test="prem:objectCharacteristics/prem:compositionLevel">
						<span class="field compositionLevel">
							<xsl:text>Nível de composição: </xsl:text>
							<xsl:value-of
								select="prem:objectCharacteristics/prem:compositionLevel/text()" />
						</span>
					</xsl:if>
	
					<xsl:if test="prem:objectCharacteristics/prem:creatingApplication">
						<span class="field creatingApplicationName">
							<xsl:text>Nome da aplicação criadora: </xsl:text>
							<xsl:value-of
								select="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationName/text()" />
						</span>
						<span class="field creatingApplicationVersion">
							<xsl:text>Versão da aplicação criadora: </xsl:text>
							<xsl:value-of
								select="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationVersion/text()" />
						</span>
					</xsl:if>
					<xsl:if
						test="prem:objectCharacteristics/prem:objectCharacteristicsExtension">
						<span class="field objectCharacteristicsExtension">
							<pre>
							<xsl:copy-of select="prem:objectCharacteristics/prem:objectCharacteristicsExtension/*"/>
							</pre>
						</span>
					</xsl:if>
				</span>
			
			</span>
		</xsl:if>

		<xsl:if test="prem:relationship">
			<span class="section files">
				<span class="sectionTitle">
					<xsl:text>Ficheiros</xsl:text>
				</span>
				<span class="sectionContent">
					<xsl:for-each select="$files">
						<xsl:value-of disable-output-escaping="yes" select="." />
						<xsl:text>&#xa;</xsl:text>
					</xsl:for-each>
				</span>
			</span>
			<span class="section events">
				<span class="sectionTitle">
					<xsl:text>Eventos</xsl:text>
				</span>
				<span class="sectionContent">
					<xsl:for-each select="$events">
						<xsl:value-of disable-output-escaping="yes" select="." />
						<xsl:text>&#xa;</xsl:text>
					</xsl:for-each>
				</span>
			</span>
			<span class="section agents">
				<span class="sectionTitle">
					<xsl:text>Agentes</xsl:text>
				</span>
				<span class="sectionContent">
					<xsl:for-each select="$agents">
						<xsl:value-of disable-output-escaping="yes" select="." />
						<xsl:text>&#xa;</xsl:text>
					</xsl:for-each>
				</span>
			</span>
		</xsl:if>
	</xsl:template>
	<xsl:template match="prem:event">
		<span class="event">
			<xsl:if test="prem:eventType">
				<span class="type">
					<xsl:value-of select="prem:eventType/text()" />
				</span>
			</xsl:if>

			<xsl:if test="prem:eventIdentifier">
				<xsl:text>(</xsl:text>
				<xsl:for-each select="prem:eventIdentifier">
					<span class="type">
						<xsl:value-of select="prem:eventIdentifierType/text()" />
					</span>
					<span class="value">
						<xsl:value-of select="prem:eventIdentifierValue/text()" />
					</span>
					<xsl:text>,</xsl:text>
				</xsl:for-each>
				<xsl:text>)</xsl:text>
			</xsl:if>
			<xsl:if test="prem:eventDateTime">
				<span class="date">
					<xsl:value-of select="prem:eventDateTime/text()" />
				</span>
			</xsl:if>
			<xsl:if test="prem:eventDetail">
				<span class="field eventDetail">
					<xsl:text>Detalhes: </xsl:text>
					<xsl:value-of select="prem:eventDetail/text()" />
				</span>
			</xsl:if>
			<span class="eventDetailsTitle"><xsl:text>Detalhes do evento</xsl:text></span>
			<span class="eventDetails">
				<xsl:if test="prem:eventIdentifier">
					<span class="field eventIdentifier">
						<div class="title">Identificadores</div>
						<table>
							<tr>
								<td>Tipo</td>
								<td>Valor</td>
							</tr>
							<xsl:for-each select="prem:eventIdentifier">
								<tr>
									<td>
										<xsl:value-of select="prem:eventIdentifierType/text()" />
									</td>
									<td>
										<xsl:value-of select="prem:eventIdentifierValue/text()" />
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</span>
				</xsl:if>
				<xsl:if test="prem:eventOutcomeInformation">
					<span class="field eventOutcomeInformation">
						<div class="title">Resultado detalhado</div>
						<table>
							<tr>
								<td>Nome</td>
								<td>Valor</td>
							</tr>
							<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcome">
								<tr>
									<td>Resultado</td>
									<td>
										<xsl:value-of
											select="prem:eventOutcomeInformation/prem:eventOutcome/text()" />
									</td>
								</tr>
							</xsl:if>
							<xsl:if
								test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote">
								<tr>
									<td>Notas</td>
									<td>
										<xsl:value-of
											select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote/text()" />
									</td>
								</tr>
							</xsl:if>
							<xsl:if
								test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension">
								<tr>
									<td>Extensão</td>
									<td>
										<xsl:value-of
											select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension/text()" />
									</td>
								</tr>
							</xsl:if>
						</table>
					</span>
				</xsl:if>
				<xsl:if test="prem:linkingAgentIdentifier">
					<div class="field linkingAgents">
						<xsl:for-each select="prem:linkingAgentIdentifier">
							<a href="/rest/agent/XPTO">
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
					</div>
				</xsl:if>
				<xsl:if test="prem:linkingObjectIdentifier">
					<div class="field linkingObjects">
						<xsl:for-each select="prem:linkingObjectIdentifier">
							<a href="/rest/object/XPTO">
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
					</div>
				</xsl:if>
			</span>
		</span>
	</xsl:template>
</xsl:stylesheet>