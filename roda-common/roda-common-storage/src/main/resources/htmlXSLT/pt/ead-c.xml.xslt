<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema"
	exclude-result-prefixes="eadc">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
	<xsl:template match="eadc:ead-c">
		<!-- COMPLETE REFERENCE -->
		<!-- HANDLE -->
		<xsl:if test="eadc:did/eadc:unitid/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Referência</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unitid/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Título</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unittitle/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="@level">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Nível de descrição</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="@level" />
				</div>
			</div>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="eadc:did/eadc:unitdate/@normal">
			<xsl:choose>
				<xsl:when test="contains(eadc:did/eadc:unitdate/@normal, '/')">
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">Data inicial</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-before(eadc:did/eadc:unitdate/@normal, '/')" />
							</span>
						</div>
					</div>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">Data final</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-after(eadc:did/eadc:unitdate/@normal, '/')" />
							</span>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">Data inicial</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of select="eadc:did/eadc:unitdate/@normal" />
							</span>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!-- COUNTRY CODE -->
		<xsl:if test="eadc:did/eadc:unitid/@repositorycode">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Código do repositório</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:unitid/@repositorycode" />
				</div>
			</div>
		</xsl:if>

		<xsl:if test="eadc:did/eadc:origination/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Produtor</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:origination/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:num/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Referência do produtor</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:num/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:date/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Data de aquisição</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:date/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:materialspec/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Detalhes específicos</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:did/eadc:materialspec/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Descrição física</div>
				<div class="descriptiveMetadata-field-value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:p/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:p/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:p/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="eadc:did/eadc:physdesc/eadc:date/@normal">
			<xsl:choose>
				<xsl:when test="contains(eadc:did/eadc:physdesc/eadc:date/@normal, '/')">
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">Data inicial da descrição física</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-before(eadc:did/eadc:physdesc/eadc:date/@normal, '/')" />
							</span>
						</div>
					</div>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">Data final da descrição física</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of
									select="substring-after(eadc:did/eadc:physdesc/eadc:date/@normal, '/')" />
							</span>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="descriptiveMetadata-field">
						<div class="descriptiveMetadata-field-key">Data inicial da descrição física</div>
						<div class="descriptiveMetadata-field-value">
							<span class="value">
								<xsl:value-of select="eadc:did/eadc:physdesc/eadc:date/@normal" />
							</span>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:dimensions/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Dimensões</div>
				<div class="descriptiveMetadata-field-value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:dimensions/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:dimensions/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:dimensions/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:physfacet/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Aspecto ou aparência</div>
				<div class="descriptiveMetadata-field-value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:physfacet/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:physfacet/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:physfacet/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:physdesc/eadc:extent/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Extensão</div>
				<div class="descriptiveMetadata-field-value">
					<span class="value">
						<xsl:value-of select="eadc:did/eadc:physdesc/eadc:extent/text()" />
					</span>
					<xsl:if test="eadc:did/eadc:physdesc/eadc:extent/@unit">
						<span class="unit">
							<xsl:value-of select="eadc:did/eadc:physdesc/eadc:extent/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:did/eadc:langmaterial">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Idiomas</div>
				<xsl:for-each select="eadc:did/eadc:langmaterial/eadc:language">
					<div class="descriptiveMetadata-field-value">
						<xsl:value-of select="text()" />
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="eadc:prefercite/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Citação</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:prefercite/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">História administrativa e biográfica</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:bioghist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bioghist/eadc:chronlist">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">História administrativa e biográfica</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:for-each select="eadc:bioghist/eadc:chronlist/eadc:chronitem">
						<xsl:variable name="line">
							<xsl:if test="eadc:date/@normal">
								<xsl:choose>
									<xsl:when test="contains(eadc:date/@normal, '/')">
										<span class="initialDate">
											<xsl:value-of select="substring-before(eadc:date/@normal, '/')" />
										</span>
										<span class="finalDate">
											<xsl:value-of select="substring-after(eadc:date/@normal, '/')" />
										</span>
									</xsl:when>
									<xsl:otherwise>
										<span class="initialDate">
											<xsl:value-of select="eadc:date/@normal" />
										</span>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
							<xsl:if test="eadc:event/text()">
								<span class="event">
									<xsl:value-of select="eadc:event/text()" />
								</span>
							</xsl:if>
						</xsl:variable>
						<div class="descriptiveMetadata-field-value-level1">
							<xsl:copy-of select="$line" />
						</div>
					</xsl:for-each>
				</div>
			</div>
		</xsl:if>

		<xsl:if test="eadc:custodhist/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">História custodial</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:custodhist/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:acqinfo/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Modalidades de aquisição</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:acqinfo/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Âmbito e conteúdo</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:scopecontent/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<!-- organization and ordering missing table handling -->
		<xsl:if test="eadc:arrangement/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Organização e ordenação</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:arrangement/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:arrangement/eadc:table">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Organização e ordenação</div>
				<xsl:variable name="output">
					<xsl:for-each
						select="eadc:arrangement/eadc:table/eadc:tgroup/eadc:thead/eadc:row/eadc:entry">
						<span class="header">
							<xsl:value-of select="text()" />
						</span>
					</xsl:for-each>
					<xsl:for-each
						select="eadc:arrangement/eadc:table/eadc:tgroup/eadc:tbody/eadc:row/eadc:entry">
						<span class="value">
							<xsl:value-of select="text()" />
						</span>
					</xsl:for-each>
				</xsl:variable>
				<div class="descriptiveMetadata-field-value">
					<xsl:copy-of select="$output" />
				</div>
			</div>
		</xsl:if>

		<xsl:if test="eadc:appraisal/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Avaliação, selecção e eliminação</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:appraisal/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accruals/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Ingressos adicionais</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:accruals/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:phystech/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Características físicas e requisitos técnicos</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:phystech/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:accessrestrict/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Condições de acesso</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:accessrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:userrestrict/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Condições de reprodução</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:userrestrict/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:relatedmaterial/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Materiais associados</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:relatedmaterial/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:otherfindingaid/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Unidades de descrição relacionadas</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:otherfindingaid/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:note/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Nota</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:note/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="eadc:bibliography/eadc:p/text()">
			<div class="descriptiveMetadata-field">
				<div class="descriptiveMetadata-field-key">Bibliografia</div>
				<div class="descriptiveMetadata-field-value">
					<xsl:value-of select="eadc:bibliography/eadc:p/text()" />
				</div>
			</div>
		</xsl:if>


















	</xsl:template>


</xsl:stylesheet>