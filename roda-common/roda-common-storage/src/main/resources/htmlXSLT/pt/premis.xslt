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
			<xsl:attribute name="id"><xsl:value-of
				select="normalize-space(prem:agentIdentifierValue/text())" /></xsl:attribute>
			<span class="header">
				<xsl:if test="prem:agentName">
					<span class="agentName">
						<xsl:value-of select="normalize-space(prem:agentName/text())" />
					</span>
				</xsl:if>
				<xsl:if test="prem:agentIdentifier/prem:agentIdentifierType/text()">
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
				</xsl:if>
			</span>
			<xsl:if test="prem:agentType">
				<span class="subheader">
					<span class="field agentType">
						<xsl:value-of select="normalize-space(prem:agentType/text())" />
					</span>
				</span>
			</xsl:if>
		</span>
	</xsl:template>
	<xsl:template match="prem:object">
		<xsl:if
			test='resolve-QName(@xsi:type, .) = QName("info:lc/xmlns/premis-v2", "representation")'>
			<span xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				class="representation">
				<span class="header">
					<xsl:text>Representação</xsl:text>
					<xsl:if test="prem:objectIdentifier">
						<span class="identifiers">
							<xsl:for-each select="prem:objectIdentifier">
								<span class="identifier">
									<span class="identifierType">
										<xsl:value-of
											select="normalize-space(prem:objectIdentifierType/text())" />
									</span>
									<span class="identifierValue">
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
							<xsl:text>Nível de preservação:</xsl:text>
							<xsl:value-of select="prem:preservationLevelValue/text()" />
							<xsl:text> desde </xsl:text>
							<xsl:value-of select="prem:preservationLevelDateAssigned/text()" />
						</span>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="prem:relationship">
					<xsl:for-each select="prem:relationship">
						<xsl:if test="prem:relationshipType/text()='derivation'">
							<span class="derivation">
								<xsl:text>Derivado de</xsl:text>
								<a>
									<xsl:attribute name="href">#<xsl:value-of
										select="prem:relatedObjectIdentification/prem:relatedObjectIdentifierValue/text()" /></xsl:attribute>

									<xsl:value-of
										select="prem:relatedObjectIdentification/prem:relatedObjectIdentifierValue/text()" />
								</a>
								<xsl:text> pelo evento </xsl:text>
								<a>
									<xsl:attribute name="href">#<xsl:value-of
										select="prem:relatedEventIdentification/prem:relatedEventIdentifierValue/text()" /></xsl:attribute>
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
				<span class="header">
					<xsl:text>Ficheiro</xsl:text>
					<xsl:if test="prem:originalName">
						<span class="originalName">
							<xsl:text>"</xsl:text>
							<xsl:value-of select="normalize-space(prem:originalName/text())" />
							<xsl:text>"</xsl:text>
						</span>
					</xsl:if>
					<xsl:if test="prem:objectIdentifier">
						<span class="identifiers">
							<xsl:for-each select="prem:objectIdentifier">
								<span class="identifier">
									<span class="identifierType">
										<xsl:value-of
											select="normalize-space(prem:objectIdentifierType/text())" />
									</span>
									<span class="identifierValue">
										<xsl:value-of
											select="normalize-space(prem:objectIdentifierValue/text())" />
									</span>
								</span>
							</xsl:for-each>
						</span>
					</xsl:if>
				</span>
				<span class="subheader">
					<xsl:if test="prem:objectCharacteristics/prem:size">
						<span class="size">
							<xsl:value-of select="prem:objectCharacteristics/prem:size/text()" />
						</span>
					</xsl:if>
					<xsl:if test="prem:objectCharacteristics/prem:format">
						<span class="format">
							<xsl:if
								test="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName">
								<span class="formatName">
									<xsl:value-of
										select="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName/text()" />
								</span>
							</xsl:if>
							<span class="formatRegistry">
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
											<td>Algoritmo</td>
											<td>Valor</td>
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
								<xsl:text>Nível de composição</xsl:text>
							</span>
							<span class="field-value">
								<xsl:value-of
									select="prem:objectCharacteristics/prem:compositionLevel/text()" />
							</span>
						</span>
					</xsl:if>

					<xsl:if test="prem:objectCharacteristics/prem:creatingApplication">
						<xsl:if test="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationName">
							<span class="field creatingApplicationName">
								<span class="field-label">
									<xsl:text>Nome da aplicação criadora</xsl:text>
								</span>
								<span class="field-value">
									<xsl:value-of
										select="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationName/text()" />
								</span>
							</span>
						</xsl:if>
						<xsl:if test="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationVersion">
							<span class="field creatingApplicationVersion">
								<span class="field-label">
									<xsl:text>Versão da aplicação criadora</xsl:text>
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
	      							<xsl:if test="prem:storage/prem:contentLocation/prem:contentLocationType">
	      								<span class="contentLocationType">
	      									<xsl:value-of select="prem:storage/prem:contentLocation/prem:contentLocationType/text()" />
	      								</span>
	      							</xsl:if>
	      							<xsl:if test="prem:storage/prem:contentLocation/prem:contentLocationValue">
										<xsl:value-of select="prem:storage/prem:contentLocation/prem:contentLocationValue/text()" />
									</xsl:if>
								</span>
							</span>
      					</xsl:if>
					</xsl:if>
					<xsl:if
						test="prem:objectCharacteristics/prem:objectCharacteristicsExtension">
						<span class="field objectCharacteristicsExtension">
							<span class="field-label">
								<xsl:text>Características detalhadas</xsl:text>
							</span>
							<span class="field-value">
								<pre>
									<code>
										<xsl:copy-of 
											select="prem:objectCharacteristics/prem:objectCharacteristicsExtension/*" />
									</code>
								</pre>
							</span>
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
			<span class="header">
				<xsl:if test="prem:eventType">
					<span class="type">
						<xsl:value-of select="prem:eventType/text()" />
					</span>
				</xsl:if>

				<xsl:if test="prem:eventIdentifier">
					<xsl:for-each select="prem:eventIdentifier">
						<span class="identifier">
							<span class="identifierType">
								<xsl:value-of select="prem:eventIdentifierType/text()" />
							</span>
							<span class="identifierValue">
								<xsl:value-of select="prem:eventIdentifierValue/text()" />
							</span>
						</span>
					</xsl:for-each>
				</xsl:if>
			</span>
			<xsl:if test="prem:eventDateTime">
				<span class="subheader">
					<span class="date">
						<xsl:value-of select="prem:eventDateTime/text()" />
					</span>
				</span>
			</xsl:if>
			<span class="content">
				<xsl:if test="prem:eventDetail">
					<span class="field eventDetail">
						<span class="field-label">
							<xsl:text>Detalhes</xsl:text>
						</span>
						<span class="field-value">
							<xsl:value-of select="prem:eventDetail/text()" />
						</span>
					</span>
				</xsl:if>
				<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcome">
					<span class="field eventOutcome">
						<span class="field-label">
							<xsl:text>Resultado</xsl:text>
						</span>
						<span class="field-value">
							<xsl:value-of
								select="prem:eventOutcomeInformation/prem:eventOutcome/text()" />
						</span>
					</span>
				</xsl:if>
				<xsl:if test="prem:linkingAgentIdentifier">
					<span class="field linkingAgents">
						<span class="field-label">
							<xsl:text>Agentes relacionados</xsl:text>
						</span>
						<span class="field-value">
							<xsl:for-each select="prem:linkingAgentIdentifier">
								<a>
									<xsl:attribute name="href">#<xsl:value-of
										select="prem:linkingAgentIdentifierValue/text()" /></xsl:attribute>
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
							<xsl:text>Objetos relacionados</xsl:text>
						</span>
						<span class="field-value">
							<xsl:for-each select="prem:linkingObjectIdentifier">
								<a>
									<xsl:attribute name="href">#<xsl:value-of
										select="prem:linkingObjectIdentifierValue/text()" /></xsl:attribute>
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
</xsl:stylesheet>