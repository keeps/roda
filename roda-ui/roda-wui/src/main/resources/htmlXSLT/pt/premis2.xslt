<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:prem="info:lc/xmlns/premis-v2"
	exclude-result-prefixes="prem">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />
	<xsl:param name="events" />
	<xsl:param name="files" />
	<xsl:param name="agents" />
	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>
	<xsl:template match="prem:agent">
		<span type="prem:agent">
			<xsl:if test="prem:agentIdentifier/prem:agentIdentifierType/text()">
				<div class="preservationMetadata-field agentIdentifierType">
					<div class="preservationMetadata-field-key">Identificador</div>
					<div class="preservationMetadata-field-value">
						<xsl:for-each select="prem:agentIdentifier">
							<span class="line">
								<span class="identifierType">
									<xsl:value-of select="prem:agentIdentifierType/text()" />
								</span>
								<span class="identifierValue">
									<xsl:value-of select="prem:agentIdentifierValue/text()" />
								</span>
							</span>
						</xsl:for-each>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:agentName">
				<div class="preservationMetadata-field agentName">
					<div class="preservationMetadata-field-key">Nome do agente</div>
					<div class="preservationMetadata-field-value">
						<xsl:value-of select="prem:agentName/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:agentType">
				<div class="preservationMetadata-field agentType">
					<div class="preservationMetadata-field-key">Tipo do agente</div>
					<div class="preservationMetadata-field-value">
						<xsl:value-of select="prem:agentType/text()" />
					</div>
				</div>
			</xsl:if>
		</span>
	</xsl:template>
	<xsl:template match="prem:object">
		<span type="prem:object">
			<xsl:if test="prem:objectIdentifier/prem:objectIdentifierType/text()">
				<div class="preservationMetadata-field objectIdentifierType">
					<div class="preservationMetadata-field-key">Identificador</div>
					<div class="preservationMetadata-field-value">
						<xsl:for-each select="prem:objectIdentifier">
							<span class="line">
								<span class="identifierType">
									<xsl:value-of select="prem:objectIdentifierType/text()" />
								</span>
								<span class="identifierValue">
									<xsl:value-of select="prem:objectIdentifierValue/text()" />
								</span>
							</span>
						</xsl:for-each>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:preservationLevel">
				<div class="preservationMetadata-field preservationLevel">
					<div class="preservationMetadata-field-key">Nível de preservação</div>
					<div class="preservationMetadata-field-value">
						<xsl:for-each select="prem:preservationLevel">
							<span class="line">
								<span class="preservationLevelValue">
									<xsl:value-of select="prem:preservationLevelValue/text()" />
								</span>
								<span class="preservationLevelDateAssigned">
									<xsl:value-of select="prem:preservationLevelDateAssigned/text()" />
								</span>
							</span>
						</xsl:for-each>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:objectCharacteristics">
				<xsl:if test="prem:objectCharacteristics/prem:compositionLevel">
					<div class="preservationMetadata-field compositionLevel">
						<div class="preservationMetadata-field-key">Nível de composição</div>
						<div class="preservationMetadata-field-value">
							<xsl:value-of select="prem:objectCharacteristics/prem:compositionLevel/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="prem:objectCharacteristics/prem:fixity">
					<div class="preservationMetadata-field fixity">
						<div class="preservationMetadata-field-key">Fixity</div>
						<div class="preservationMetadata-field-value">
							<xsl:for-each select="prem:objectCharacteristics/prem:fixity">
								<span class="line">
									<span class="messageDigestAlgorithm">
										<xsl:value-of select="prem:messageDigestAlgorithm/text()" />
									</span>
									<span class="messageDigest">
										<xsl:value-of select="prem:messageDigest/text()" />
									</span>
								</span>
							</xsl:for-each>
						</div>
					</div>
				</xsl:if>
				<xsl:if test="prem:objectCharacteristics/prem:size">
					<div class="preservationMetadata-field size">
						<div class="preservationMetadata-field-key">Tamanho</div>
						<div class="preservationMetadata-field-value">
							<xsl:value-of select="prem:objectCharacteristics/prem:size/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="prem:objectCharacteristics/prem:format">
					<div class="preservationMetadata-field format">
						<div class="preservationMetadata-field-key">Formato</div>
						<div class="preservationMetadata-field-value">
							<span class="line">
        						<xsl:if test="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName">
        							<span class="formatName">
										<xsl:value-of select="prem:objectCharacteristics/prem:format/prem:formatDesignation/prem:formatName/text()" />
									</span>
        						</xsl:if>
        						<xsl:if test="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryName">
        							<span class="formatRegistryName">
										<xsl:value-of select="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryName/text()" />
									</span>
        						</xsl:if>
        						<xsl:if test="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryKey">
        							<span class="formatRegistryKey">
										<xsl:value-of select="prem:objectCharacteristics/prem:format/prem:formatRegistry/prem:formatRegistryKey/text()" />
									</span>
        						</xsl:if>
							</span>
						</div>
					</div>
				</xsl:if>
				<xsl:if test="prem:objectCharacteristics/prem:creatingApplication">
					<div class="preservationMetadata-field creatingApplication">
						<div class="preservationMetadata-field-key">Aplicação</div>
						<div class="preservationMetadata-field-value">
							<span class="line">
        						<xsl:if test="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationName">
        							<span class="creatingApplicationName">
										<xsl:value-of select="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationName/text()" />
									</span>
        						</xsl:if>
        						<xsl:if test="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationVersion">
        							<span class="creatingApplicationVersion">
										<xsl:value-of select="prem:objectCharacteristics/prem:creatingApplication/prem:creatingApplicationVersion/text()" />
									</span>
        						</xsl:if>
							</span>
						</div>
					</div>
				</xsl:if>
			</xsl:if>
			<xsl:if test="prem:objectCharacteristics/prem:objectCharacteristicsExtension">
				<div class="preservationMetadata-field objectCharacteristicsExtension">
					<div class="preservationMetadata-field-key">Outras características</div>
					<div class="preservationMetadata-field-value">
						<xsl:value-of select="prem:objectCharacteristics/prem:objectCharacteristicsExtension"></xsl:value-of>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:originalName">
				<div class="preservationMetadata-field originalName">
					<div class="preservationMetadata-field-key">Nome original</div>
					<div class="preservationMetadata-field-value">
						<xsl:value-of select="prem:originalName/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:storage">
					<div class="preservationMetadata-field storage">
						<div class="preservationMetadata-field-key">Armazenamento</div>
						<div class="preservationMetadata-field-value">
							<span class="line">
        						<xsl:if test="prem:storage/prem:contentLocation/prem:contentLocationType">
        							<span class="contentLocationType">
										<xsl:value-of select="prem:storage/prem:contentLocation/prem:contentLocationType/text()" />
									</span>
        						</xsl:if>
        						<xsl:if test="prem:storage/prem:contentLocation/prem:contentLocationValue">
        							<span class="contentLocationValue">
										<xsl:value-of select="prem:storage/prem:contentLocation/prem:contentLocationValue/text()" />
									</span>
        						</xsl:if>
							</span>
						</div>
					</div>
				</xsl:if>
			<xsl:if test="prem:relationship">
				<span file="events">
					<xsl:for-each select="$events">
						<xsl:value-of disable-output-escaping="yes" select="." />
						<xsl:text>&#xa;</xsl:text>
					</xsl:for-each>
				</span>
				<span field="files">
					<xsl:for-each select="$files">
						<xsl:value-of disable-output-escaping="yes" select="." />
						<xsl:text>&#xa;</xsl:text>
					</xsl:for-each>
				</span>
				<span field="agents">
					<xsl:for-each select="$agents">
						<xsl:value-of disable-output-escaping="yes" select="." />
						<xsl:text>&#xa;</xsl:text>
					</xsl:for-each>
				</span>
			</xsl:if>
		</span>
	</xsl:template>
	<xsl:template match="prem:event">
		<span type="prem:event">
			<xsl:if test="prem:eventIdentifier">
				<div class="preservationMetadata-field eventIdentifier">
					<div class="preservationMetadata-field-key">Identificador</div>
					<div class="preservationMetadata-field-value">
						<xsl:for-each select="prem:eventIdentifier">
							<span class="line">
								<span class="identifierType">
									<xsl:value-of select="prem:eventIdentifierType/text()" />
								</span>
								<span class="identifierValue">
									<xsl:value-of select="prem:eventIdentifierValue/text()" />
								</span>
							</span>
						</xsl:for-each>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:eventType">
				<div class="preservationMetadata-field eventType">
					<div class="preservationMetadata-field-key">Tipo de evento</div>
					<div class="preservationMetadata-field-value">
						<xsl:value-of select="prem:eventType/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:eventDateTime">
				<div class="preservationMetadata-field eventDateTime">
					<div class="preservationMetadata-field-key">Data</div>
					<div class="preservationMetadata-field-value">
						<xsl:value-of select="prem:eventDateTime/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:eventDetail">
				<div class="preservationMetadata-field eventDetail">
					<div class="preservationMetadata-field-key">Detalhes do evento</div>
					<div class="preservationMetadata-field-value">
						<xsl:value-of select="prem:eventDetail/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:eventOutcomeInformation">
				<div class="preservationMetadata-field eventOutcomeInformation">
					<div class="preservationMetadata-field-key">Resultado</div>
					<div class="preservationMetadata-field-value">
						<span class="line">
							<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcome">
								<span class="eventOutcome">
									<xsl:value-of select="prem:eventOutcomeInformation/prem:eventOutcome/text()" />
								</span>
							</xsl:if>
							<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote">
								<span class="eventOutcomeDetailNote">
									<xsl:value-of select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailNote/text()" />
								</span>
							</xsl:if>
							<xsl:if test="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension">
								<span class="eventOutcomeDetailExtension">
									<xsl:value-of select="prem:eventOutcomeInformation/prem:eventOutcomeDetail/prem:eventOutcomeDetailExtension/text()" />
								</span>
							</xsl:if>
						</span>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="prem:linkingAgentIdentifier">
				<div class="linkingAgents">
					<xsl:for-each select="prem:linkingAgentIdentifier">
						<span class="line">
							<span class="identifierType">
								<xsl:value-of select="prem:linkingAgentIdentifierType/text()" />
							</span>
							<span class="identifierValue">
								<xsl:value-of select="prem:linkingAgentIdentifierValue/text()" />
							</span>
							<span class="role">
								<xsl:value-of select="prem:linkingAgentRole/text()" />
							</span>
						</span>
					</xsl:for-each>
				</div>
			</xsl:if>
			<xsl:if test="prem:linkingObjectIdentifier">
				<div class="linkingObjects">
					<xsl:for-each select="prem:linkingObjectIdentifier">
						<span class="line">
							<span class="identifierType">
								<xsl:value-of select="prem:linkingObjectIdentifierType/text()" />
							</span>
							<span class="identifierValue">
								<xsl:value-of select="prem:linkingObjectIdentifierValue/text()" />
							</span>
							<span class="role">
								<xsl:value-of select="prem:linkingObjectRole/text()" />
							</span>
						</span>
					</xsl:for-each>
				</div>
			</xsl:if>
		</span>
	</xsl:template>
</xsl:stylesheet>