<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ead="http://ead3.archivists.org/schema/" 
	exclude-result-prefixes="ead">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		omit-xml-declaration="yes" />

	<xsl:param name="i18n.title" />
	<xsl:param name="i18n.titletype" />
	<xsl:param name="i18n.level" />
	<xsl:param name="i18n.initialdate" />
	<xsl:param name="i18n.finaldate" />
	<xsl:param name="i18n.repositorycode" />
	<xsl:param name="i18n.reference" />
	<xsl:param name="i18n.acquisitionnumber" />
	<xsl:param name="i18n.origination" />
	<xsl:param name="i18n.acquisitiondate" />
	<xsl:param name="i18n.materialspecification" />
	<xsl:param name="i18n.physicaldescription" />
	<xsl:param name="i18n.dateofinitialphysicaldescription" />
	<xsl:param name="i18n.dateoffinalphysicaldescription" />
	<xsl:param name="i18n.dimensions" />
	<xsl:param name="i18n.facetorappearance" />
	<xsl:param name="i18n.extent" />
	<xsl:param name="i18n.languages" />
	<xsl:param name="i18n.quote" />
	<xsl:param name="i18n.administrativeandbiographicalhistory" />
	<xsl:param name="i18n.custodialhistory" />
	<xsl:param name="i18n.acquisitioninformation" />
	<xsl:param name="i18n.description" />
	<xsl:param name="i18n.organizationandordering" />
	<xsl:param name="i18n.appraisal" />
	<xsl:param name="i18n.accruals" />
	<xsl:param name="i18n.physicalcharacteristicsandtechnicalrequirements" />
	<xsl:param name="i18n.accessrestrictions" />
	<xsl:param name="i18n.altformavail" />
	<xsl:param name="i18n.reproductionrestrictions" />
	<xsl:param name="i18n.relatedmaterials" />
	<xsl:param name="i18n.otherfindaids" />
	<xsl:param name="i18n.userestrict" />
	<xsl:param name="i18n.notes" />
	<xsl:param name="i18n.originalsloc" />
	<xsl:param name="i18n.bibliography" />
	<xsl:param name="i18n.unitdate" />
	<xsl:param name="i18n.control.recordid"  />
	<xsl:param name="i18n.control.otherrecordid" />
	<xsl:param name="i18n.control.maintenancestatus" />
	<xsl:param name="i18n.control.publicationstatus" />
	<xsl:param name="i18n.control.languagedeclaration.language" />
	<xsl:param name="i18n.control.languagedeclaration.script" />
	<xsl:param name="i18n.control.filedesc.titlestmt" />
	<xsl:param name="i18n.control.filedesc.publicationstmt" />
	<xsl:param name="i18n.control.maintenanceagency" />
	<xsl:param name="i18n.control" />
	<xsl:param name="i18n.archdesc" />
	<xsl:param name="i18n.control.titlestmt.titleproper" />
	<xsl:param name="i18n.control.titlestmt.subtitle" />
	<xsl:param name="i18n.control.titlestmt.author" />
	<xsl:param name="i18n.control.titlestmt.sponsor" />
	<xsl:param name="i18n.control.maintenanceagency.agencycode" />
	<xsl:param name="i18n.control.maintenanceagency.otheragencycode" />
	<xsl:param name="i18n.control.maintenanceagency.agencyname" />
	<xsl:param name="i18n.control.maintenanceagency.descriptivenote" />

	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:apply-templates />
		</div>
	</xsl:template>
    <xsl:template match="ead:eadheader">
        <xsl:if test="normalize-space(string-join(ead:profiledesc/ead:langusage/ead:language/text(),''))!=''">
            <div class="field">
                <div class="label">
                    <xsl:value-of select="$i18n.languages"/>
                </div>
                <xsl:for-each select="ead:profiledesc/ead:langusage/ead:language">
                	<xsl:if test="normalize-space(text())!=''">
		                <div class="value">
		                    <xsl:value-of select="text()" />
		                </div>
		            </xsl:if>
		        </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>
    <xsl:template match="ead:archdesc">
        <h5><xsl:value-of select="$i18n.archdesc" /></h5>
    
		<!-- COMPLETE REFERENCE -->
		<!-- HANDLE -->
		<xsl:if test="normalize-space(ead:did/ead:unittitle/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.title"/>
				</div>
				<div class="value">
					<xsl:value-of select="ead:did/ead:unittitle/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="ead:did/ead:unittitle/@type">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.titletype" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:did/ead:unittitle/@type" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(@level)!=''">
			<xsl:choose>
				<xsl:when test="@level = 'otherlevel'">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.level" />
						</div>
						<div class="value">
							<xsl:value-of select="@otherlevel" />
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.level" />
						</div>
						<div class="value">
							<xsl:value-of select="@level" />
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="normalize-space(ead:did/ead:unitdate/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.unitdate" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="ead:did/ead:unitdate/text()" />
					</span>
				</div>
			</div>
		</xsl:if>
		
		<xsl:if test="ead:did/ead:unitdatestructured">
			<xsl:if test="ead:did/ead:unitdatestructured/ead:daterange/ead:fromdate/@standarddate">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.initialdate" />
					</div>
					<div class="value">
						<span class="value">
							<xsl:value-of select="ead:did/ead:unitdatestructured/ead:daterange/ead:fromdate/@standarddate" />
						</span>
					</div>
				</div>
			</xsl:if>
			<xsl:if test="ead:did/ead:unitdatestructured/ead:daterange/ead:todate/@standarddate">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.finaldate" />
					</div>
					<div class="value">
						<span class="value">
							<xsl:value-of select="ead:did/ead:unitdatestructured/ead:daterange/ead:todate/@standarddate" />
						</span>
					</div>
				</div>
			</xsl:if>
		</xsl:if>
		
		
		
		<xsl:if test="normalize-space(ead:did/ead:unitdate/@normal)!=''">
			<xsl:choose>
				<xsl:when test="contains(ead:did/ead:unitdate/@normal, '/')">	<!-- initial/final -->
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-before(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
				
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(substring-after(ead:did/ead:unitdate/@normal, '/'))">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.finaldate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:when>
				<xsl:otherwise>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}}-\d{{2}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-01-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})(\d{{2}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-<xsl:value-of select="regex-group(3)" />
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
					<xsl:analyze-string regex="^(\d{{4}})(\d{{2}})$" select="normalize-space(ead:did/ead:unitdate/@normal)">
						<xsl:matching-substring>
							<div class="field">
								<div class="label">
									<xsl:value-of select="$i18n.initialdate" />
								</div>
								<div class="value">
									<span class="value">
										<xsl:value-of select="regex-group(1)" />-<xsl:value-of select="regex-group(2)" />-01
									</span>
								</div>
							</div>
						</xsl:matching-substring>
					</xsl:analyze-string>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<!-- COUNTRY CODE -->
		<xsl:if test="normalize-space(ead:did/ead:unitid/@repositorycode)!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.repositorycode" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:did/ead:unitid/@repositorycode" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:did/ead:unitid/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.reference" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:did/ead:unitid/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(string-join(ead:did/ead:origination/text(),''))!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.origination" />
				</div>
				<xsl:for-each select="ead:did/ead:origination">
					<xsl:if test="normalize-space(text())!=''">
						<div class="value">
							<xsl:value-of select="text()" />
						</div>
					</xsl:if>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:acqinfo/ead:num/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.acquisitionnumber" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:acqinfo/ead:num/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:acqinfo/ead:date/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.acquisitiondate" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:acqinfo/ead:date/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:did/ead:materialspec/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.materialspecification" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:did/ead:materialspec/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:did/ead:physdesc/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.physicaldescription" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="ead:did/ead:physdesc/ead:p/text()" />
					</span>
					<xsl:if test="ead:did/ead:physdesc/ead:p/@unit">
						<span class="unit">
							<xsl:value-of select="ead:did/ead:physdesc/ead:p/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<!-- HANDLE DATE BETTER??? -->
		<xsl:if test="normalize-space(ead:did/ead:physdesc/ead:date/@normal)!=''">
			<xsl:choose>
				<xsl:when test="contains(ead:did/ead:physdesc/ead:date/@normal, '/')">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.dateofinitialphysicaldescription" />
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of
									select="substring-before(ead:did/ead:physdesc/ead:date/@normal, '/')" />
							</span>
						</div>
					</div>
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.dateoffinalphysicaldescription" />
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of
									select="substring-after(ead:did/ead:physdesc/ead:date/@normal, '/')" />
							</span>
						</div>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.dateofinitialphysicaldescription" />
						</div>
						<div class="value">
							<span class="value">
								<xsl:value-of select="ead:did/ead:physdesc/ead:date/@normal" />
							</span>
						</div>
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		<xsl:if test="normalize-space(ead:did/ead:physdesc/ead:dimensions/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.dimensions" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="ead:did/ead:physdesc/ead:dimensions/text()" />
					</span>
					<xsl:if test="ead:did/ead:physdesc/ead:dimensions/@unit">
						<span class="unit">
							<xsl:value-of select="ead:did/ead:physdesc/ead:dimensions/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:did/ead:physdesc/ead:physfacet/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.facetorappearance" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="ead:did/ead:physdesc/ead:physfacet/text()" />
					</span>
					<xsl:if test="ead:did/ead:physdesc/ead:physfacet/@unit">
						<span class="unit">
							<xsl:value-of select="ead:did/ead:physdesc/ead:physfacet/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:did/ead:physdesc/ead:extent/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.extent" />
				</div>
				<div class="value">
					<span class="value">
						<xsl:value-of select="ead:did/ead:physdesc/ead:extent/text()" />
					</span>
					<xsl:if test="ead:did/ead:physdesc/ead:extent/@unit">
						<span class="unit">
							<xsl:value-of select="ead:did/ead:physdesc/ead:extent/@unit" />
						</span>
					</xsl:if>
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(string-join(ead:did/ead:langmaterial/ead:language/text(),''))!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.languages" />
				</div>
				<xsl:for-each select="ead:did/ead:langmaterial/ead:language">
					<xsl:if test="normalize-space(text())!=''">
						<div class="value">
							<xsl:value-of select="text()" />
						</div>
					</xsl:if>
				</xsl:for-each>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:prefercite/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.quote" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:prefercite/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:bioghist/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.administrativeandbiographicalhistory" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:bioghist/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="ead:bioghist/ead:chronlist">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.administrativeandbiographicalhistory" />
				</div>
				<div class="value">
					<xsl:for-each select="ead:bioghist/ead:chronlist/ead:chronitem">
						<xsl:variable name="line">
							<xsl:if test="ead:date/@normal">
								<xsl:choose>
									<xsl:when test="contains(ead:date/@normal, '/')">
										<span class="initialDate">
											<xsl:value-of select="substring-before(ead:date/@normal, '/')" />
										</span>
										<span class="finalDate">
											<xsl:value-of select="substring-after(ead:date/@normal, '/')" />
										</span>
									</xsl:when>
									<xsl:otherwise>
										<span class="initialDate">
											<xsl:value-of select="ead:date/@normal" />
										</span>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
							<xsl:if test="ead:event/text()">
								<span class="event">
									<xsl:value-of select="ead:event/text()" />
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

		<xsl:if test="normalize-space(ead:custodhist/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.custodialhistory" />
				</div>
				<div class="value prewrap">
					<xsl:value-of select="ead:custodhist/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:acqinfo/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.acquisitioninformation" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:acqinfo/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(string-join(ead:scopecontent/ead:p/text(),''))!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.description" />
				</div>
				<div class="value prewrap">
					<xsl:value-of select="ead:scopecontent/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:arrangement/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.organizationandordering" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:arrangement/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="ead:arrangement/ead:table">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.organizationandordering" />
				</div>
				<xsl:variable name="output">
					<table>
						<thead>
							<tr>
								<xsl:for-each
									select="ead:arrangement/ead:table/ead:tgroup/ead:thead/ead:row/ead:entry">
									<td>
										<xsl:value-of select="text()" />
									</td>
								</xsl:for-each>
							</tr>
						</thead>
						<tbody>
							<xsl:for-each
								select="ead:arrangement/ead:table/ead:tgroup/ead:tbody/ead:row">
								<tr>
									<xsl:for-each select="ead:entry">
										<td>
											<xsl:value-of select="text()" />
										</td>
									</xsl:for-each>
								</tr>
							</xsl:for-each>
						</tbody>
					</table>
				</xsl:variable>
				<div class="value">
					<xsl:copy-of select="$output" />
				</div>
			</div>
		</xsl:if>

		<xsl:if test="normalize-space(ead:appraisal/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.appraisal" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:appraisal/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:accruals/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.accruals" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:accruals/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:phystech/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of
						select="$i18n.physicalcharacteristicsandtechnicalrequirements" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:phystech/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:accessrestrict/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.accessrestrictions" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:accessrestrict/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:altformavail/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.altformavail" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:altformavail/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:userrestrict/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.reproductionrestrictions" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:userrestrict/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(string-join(ead:relatedmaterial/ead:p/text(),''))!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.relatedmaterials" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:relatedmaterial/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:otherfindingaid/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.otherfindaids" />
				</div>
				<div class="value prewrap">
					<xsl:value-of select="ead:otherfindingaid/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="ead:userestrict/ead:p/text()">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.userestrict" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:userestrict/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:note/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.notes" />
				</div>
				<div class="value prewrap">
					<xsl:value-of select="ead:note/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		
		<xsl:if test="normalize-space(ead:originalsloc/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.originalsloc" />
				</div>
				<div class="value prewrap">
					<xsl:value-of select="ead:originalsloc/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		
		<xsl:if test="normalize-space(ead:bibliography/ead:p/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.bibliography" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:bibliography/ead:p/text()" />
				</div>
			</div>
		</xsl:if>
		
	</xsl:template>
	<xsl:template match="ead:control">
		<h5><xsl:value-of select="$i18n.control" /></h5>
		
		<xsl:if test="normalize-space(ead:recordid/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.control.recordid" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:recordid/text()" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:otherrecordid/text())!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.control.otherrecordid" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:otherrecordid/text()" />
				</div>
			</div>
		</xsl:if>
		
		<xsl:if test="ead:filedesc">
			<xsl:if test="ead:filedesc/ead:titlestmt">
				<xsl:if test="normalize-space(ead:filedesc/ead:titlestmt/ead:titleproper/text())!=''">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.control.titlestmt.titleproper" />
						</div>
						<div class="value">
							<xsl:value-of select="ead:filedesc/ead:titlestmt/ead:titleproper/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="normalize-space(ead:filedesc/ead:titlestmt/ead:subtitle/text())!=''">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.control.titlestmt.subtitle" />
						</div>
						<div class="value">
							<xsl:value-of select="ead:filedesc/ead:titlestmt/ead:subtitle/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="normalize-space(ead:filedesc/ead:titlestmt/ead:author/text())!=''">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.control.titlestmt.author" />
						</div>
						<div class="value">
							<xsl:value-of select="ead:filedesc/ead:titlestmt/ead:author/text()" />
						</div>
					</div>
				</xsl:if>
				<xsl:if test="normalize-space(ead:filedesc/ead:titlestmt/ead:sponsor/text())!=''">
					<div class="field">
						<div class="label">
							<xsl:value-of select="$i18n.control.titlestmt.sponsor" />
						</div>
						<div class="value">
							<xsl:value-of select="ead:filedesc/ead:titlestmt/ead:sponsor/text()" />
						</div>
					</div>
				</xsl:if>
				
			</xsl:if>
			<xsl:if test="normalize-space(string-join(ead:filedesc/ead:publicationstmt/text(),''))!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.control.filedesc.publicationstmt" />
					</div>
					<div class="value">
						<xsl:for-each select="ead:filedesc/ead:publicationstmt/ead:p">
							<xsl:value-of select="."/>
						</xsl:for-each>
					</div>
				</div>
			</xsl:if>
		</xsl:if>
		
		<xsl:if test="ead:maintenanceagency">
			<xsl:if test="normalize-space(ead:maintenanceagency/ead:agencycode/text())!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.control.maintenanceagency.agencycode" />
					</div>
					<div class="value">
						<xsl:value-of select="ead:maintenanceagency/ead:agencycode/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="normalize-space(ead:maintenanceagency/ead:otheragencycode/text())!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.control.maintenanceagency.otheragencycode" />
					</div>
					<div class="value">
						<xsl:value-of select="ead:maintenanceagency/ead:otheragencycode/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="normalize-space(ead:maintenanceagency/ead:agencyname/text())!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.control.maintenanceagency.agencyname" />
					</div>
					<div class="value">
						<xsl:value-of select="ead:maintenanceagency/ead:agencyname/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="normalize-space(ead:maintenanceagency/ead:descriptivenote/text())!=''">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.control.maintenanceagency.descriptivenote" />
					</div>
					<div class="value">
						<xsl:value-of select="ead:maintenanceagency/ead:descriptivenote/text()" />
					</div>
				</div>
			</xsl:if>
		</xsl:if>

		<xsl:if test="normalize-space(ead:maintenancestatus/@value)!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.control.maintenancestatus" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:maintenancestatus/@value" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(ead:publicationstatus/@value)!=''">
			<div class="field">
				<div class="label">
					<xsl:value-of select="$i18n.control.publicationstatus" />
				</div>
				<div class="value">
					<xsl:value-of select="ead:publicationstatus/@value" />
				</div>
			</div>
		</xsl:if>
		<xsl:if test="normalize-space(string-join(ead:languagedeclaration/ead:language/text(),''))!=''">
			<xsl:if test="ead:languagedeclaration/ead:language">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.control.languagedeclaration.language" />
					</div>
					<div class="value">
						<xsl:value-of select="ead:languagedeclaration/ead:language/text()" />
					</div>
				</div>
			</xsl:if>
			<xsl:if test="ead:languagedeclaration/ead:script">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$i18n.control.languagedeclaration.script" />
					</div>
					<div class="value">
						<xsl:value-of select="ead:languagedeclaration/ead:script/text()" />
					</div>
				</div>
			</xsl:if>
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>