<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet  [
		<!ENTITY crarr  "&#13;">
		<!ENTITY crarr  "&#xD;">]>
<xsl:stylesheet version="2.0"
				xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:xs="http://www.w3.org/2001/XMLSchema"
				xmlns:my="http://www.keep.pt/xslt/functions"
				xmlns:ead="urn:isbn:1-931666-22-9" exclude-result-prefixes="ead">
	<xsl:output method="xml" indent="yes" encoding="UTF-8" omit-xml-declaration="yes" />
	<!-- SECTION -->
	<xsl:param name="i18n.identityarea" />
	<xsl:param name="i18n.level" />
	<xsl:param name="i18n.otherlevel" />
	<xsl:param name="i18n.reference" />
	<xsl:param name="i18n.repositorycode" />
	<xsl:param name="i18n.countrycode" />
	<xsl:param name="i18n.title" />
	<xsl:param name="i18n.titletype" />
	<xsl:param name="i18n.repository" />
	<xsl:param name="i18n.initialdate" />
	<xsl:param name="i18n.finaldate" />
	<xsl:param name="i18n.unitdate" />
	<xsl:param name="i18n.dimensions" />
	<xsl:param name="i18n.extents" />
	<!-- SECTION -->
	<xsl:param name="i18n.contextarea" />
	<xsl:param name="i18n.bioghist" />
	<xsl:param name="i18n.geogname" />
	<xsl:param name="i18n.legalstatus" />
	<xsl:param name="i18n.custodhist" />
	<xsl:param name="i18n.acqinfo" />
	<!-- SECTION -->
	<xsl:param name="i18n.contentstructurearea" />
	<xsl:param name="i18n.scopecontent" />
	<xsl:param name="i18n.appraisal" />
	<xsl:param name="i18n.accruals" />
	<xsl:param name="i18n.arrangement" />
	<!-- SECTION -->
	<xsl:param name="i18n.useaccessarea" />
	<xsl:param name="i18n.accessrestrict" />
	<xsl:param name="i18n.userestrict" />
	<xsl:param name="i18n.physloc" />
	<xsl:param name="i18n.langmaterial" />
	<xsl:param name="i18n.phystech" />
	<xsl:param name="i18n.otherfindaid" />
	<!-- SECTION -->
	<xsl:param name="i18n.alliedmaterialarea" />
	<xsl:param name="i18n.originalsloc" />
	<xsl:param name="i18n.altformavail" />
	<xsl:param name="i18n.relatedmaterial" />
	<xsl:param name="i18n.bibliography" />
	<!-- SECTION -->
	<xsl:param name="i18n.notesarea" />
	<xsl:param name="i18n.oddarea" />
	<!-- SECTION -->
	<xsl:param name="i18n.descriptioncontrolarea" />
	<xsl:param name="i18n.processinfo" />
	<xsl:param name="i18n.level.C" />
	<xsl:param name="i18n.level.F" />
	<xsl:param name="i18n.level.SF" />
	<xsl:param name="i18n.level.SSF" />
	<xsl:param name="i18n.level.SC" />
	<xsl:param name="i18n.level.SSC" />
	<xsl:param name="i18n.level.SSSC" />
	<xsl:param name="i18n.level.SR" />
	<xsl:param name="i18n.level.SSR" />
	<xsl:param name="i18n.level.SSSR" />
	<xsl:param name="i18n.level.UI" />
	<xsl:param name="i18n.level.DC" />
	<xsl:param name="i18n.level.D" />

	<xsl:template name="join">
		<xsl:param name="list" />
		<xsl:param name="separator" />
		<xsl:for-each select="$list">
			<xsl:value-of select="." />
			<xsl:if test="position() != last()">
				<xsl:value-of select="$separator" />
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<!-- ................................................
          escapeNewLine: replaces NL by HTML BR elements
         ................................................-->
	<xsl:function name="my:escapeNewLine">
		<xsl:param name="pText" as="xs:string"/>
		<xsl:choose>
			<xsl:when test="not(contains($pText, '&#xA;'))">
				<xsl:value-of select="$pText"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="concat(substring-before($pText, '&#xA;'),'',my:escapeNewLine(substring-after($pText, '&#xA;')))"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	<xsl:template name="showField">
		<xsl:param name="label" />
		<xsl:param name="value" />
		<xsl:if test="$value">
			<div class="row-fluid metadata">
				<div class="label">
					<xsl:value-of select="$label" />
				</div>
				<div class="value">
					<xsl:for-each select="$value">
						<xsl:if test=".">
							<xsl:value-of select="my:escapeNewLine(.)"/>
						</xsl:if>
					</xsl:for-each>
				</div>
			</div>
		</xsl:if>
	</xsl:template>
	<xsl:template name="showFieldWithAltRender">
		<xsl:param name="value" />
		<xsl:for-each select="$value">
			<div class="row-fluid metadata">
				<div class="label">
					<xsl:if test="./@altrender">
						<xsl:value-of select="my:escapeNewLine(./@altrender)"/>
					</xsl:if>
				</div>
				<div class="value">
					<xsl:if test=".">
						<xsl:value-of select="my:escapeNewLine(.)"/>
					</xsl:if>
				</div>
			</div>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="showTable">
		<xsl:param name="label" />
		<xsl:param name="value" />
		<xsl:if test="$value">
			<xsl:for-each select="$value/ead:table">
				<div class="field">
					<div class="label">
						<xsl:value-of select="$label" />
					</div>
					<xsl:variable name="output">
						<table>
							<thead>
								<tr>
									<xsl:for-each select="ead:tgroup/ead:thead/ead:row/ead:entry">
										<td>
											<xsl:value-of select="text()" />
										</td>
									</xsl:for-each>
								</tr>
							</thead>
							<tbody>
								<xsl:for-each select="ead:tgroup/ead:tbody/ead:row">
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
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	<xsl:template name="showBibliography">
		<xsl:param name="label" />
		<xsl:param name="value" />
		<xsl:if test="$value">
			<div class="row-fluid metadata">
				<div class="label">
					<xsl:value-of select="$label" />
				</div>
				<xsl:for-each select="$value/ead:bibref">
					<div class="value">
						<xsl:value-of select="my:escapeNewLine(./text())"/>
					</div>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>
	<xsl:template name="showInitialAndFinalDates">
		<xsl:choose>
			<xsl:when test="contains(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal, '/')">
				<!-- initial/final -->
				<div class="row-fluid metadata">
					<div class="label">
						<xsl:value-of select="$i18n.initialdate" />
					</div>
					<div class="value">
						<span class="value">
							<xsl:value-of select="normalize-space(substring-before(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal, '/'))" />
						</span>
					</div>
				</div>
				<div class="row-fluid metadata">
					<div class="label">
						<xsl:value-of select="$i18n.finaldate" />
					</div>
					<div class="value">
						<span class="value">
							<xsl:value-of select="normalize-space(substring-after(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal, '/'))" />
						</span>
					</div>
				</div>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="/ead:ead/ead:archdesc/ead:did/ead:unitdate[@label='UnitDateInitial']">
						<!-- initial date. internal 'hack' -->
						<div class="row-fluid metadata">
							<div class="label">
								<xsl:value-of select="$i18n.initialdate" />
							</div>
							<div class="value">
								<span class="value">
									<xsl:value-of select="normalize-space(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal)" />
								</span>
							</div>
						</div>
						<xsl:call-template name="showField">
							<xsl:with-param name="label" select="$i18n.reference" />
							<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:did/ead:unitid" />
						</xsl:call-template>
					</xsl:when>
					<xsl:when test="/ead:ead/ead:archdesc/ead:did/ead:unitdate[@label='UnitDateFinal']">
						<!-- final date. internal 'hack' -->
						<div class="row-fluid metadata">
							<div class="label">
								<xsl:value-of select="$i18n.finaldate" />
							</div>
							<div class="value">
								<span class="value">
									<xsl:value-of select="normalize-space(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal)" />
								</span>
							</div>
						</div>
					</xsl:when>
					<xsl:otherwise>
						<!-- fallback to date initial -->
						<div class="row-fluid metadata">
							<div class="label">
								<xsl:value-of select="$i18n.initialdate" />
							</div>
							<div class="value">
								<span class="value">
									<xsl:value-of select="normalize-space(/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal)" />
								</span>
							</div>
						</div>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<!-- ................................................
          Takes care of @level attribute
         ................................................-->
	<xsl:template match="ead:archdesc[@level]">
		<div class="row-fluid metadata">
			<div class="label">
				<xsl:value-of select="$i18n.level" />
			</div>
			<xsl:choose>
				<xsl:when test="@level = 'otherlevel'">
					<div class="value">
						<xsl:choose>
							<xsl:when test="@otherlevel = 'C'">
								<xsl:value-of select="$i18n.level.C" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'F'">
								<xsl:value-of select="$i18n.level.F" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SF'">
								<xsl:value-of select="$i18n.level.SF" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SSF'">
								<xsl:value-of select="$i18n.level.SSF" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SC'">
								<xsl:value-of select="$i18n.level.SC" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SSC'">
								<xsl:value-of select="$i18n.level.SSC" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SSSC'">
								<xsl:value-of select="$i18n.level.SSSC" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SR'">
								<xsl:value-of select="$i18n.level.SR" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SSR'">
								<xsl:value-of select="$i18n.level.SSR" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'SSSR'">
								<xsl:value-of select="$i18n.level.SSSR" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'UI'">
								<xsl:value-of select="$i18n.level.UI" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'DC'">
								<xsl:value-of select="$i18n.level.DC" />
							</xsl:when>
							<xsl:when test="@otherlevel = 'D'">
								<xsl:value-of select="$i18n.level.D" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="@otherlevel" />
							</xsl:otherwise>
						</xsl:choose>
					</div>
				</xsl:when>
				<xsl:otherwise>
					<div class="value">
						<xsl:value-of select="@level" />
					</div>
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>
	<xsl:template name="showDimensions">
		<xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:dimensions">
			<div class="row-fluid metadata">
				<div class="label">
					<xsl:value-of select="$i18n.dimensions" />
				</div>
				<xsl:for-each select="/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:dimensions">
					<xsl:if test=".">
						<div class="value">
							<span class="value">
								<xsl:value-of select="." />
							</span>
							<xsl:if test="./@unit">
								<span class="unit">
									<xsl:value-of select="./@unit" />
								</span>
							</xsl:if>
						</div>
					</xsl:if>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>
	<xsl:template name="showExtent">
		<xsl:if test="/ead:ead/ead:archdesc/ead:extent">
			<div class="row-fluid metadata">
				<div class="label">
					<xsl:value-of select="$i18n.extents" />
				</div>
				<xsl:for-each select="/ead:ead/ead:archdesc/ead:extent">
					<xsl:if test=".">
						<div class="value">
							<span class="value">
								<xsl:value-of select="."/>
							</span>
							<xsl:if test="./@unit">
								<span class="unit">
									<xsl:value-of select="./@unit" />
								</span>
							</xsl:if>
						</div>
					</xsl:if>
				</xsl:for-each>
			</div>
		</xsl:if>
	</xsl:template>
	<!-- Template for "identity area" -->
	<xsl:template name="showIdentityArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:repository|/ead:ead/ead:archdesc/@level|/ead:ead/ead:archdesc/ead:did/ead:unitid|/ead:ead/ead:archdesc/ead:did/ead:unitid/@repositorycode|/ead:ead/ead:archdesc/ead:did/ead:unitid/@countrycode|/ead:ead/ead:archdesc/ead:did/ead:unittitle/text()|/ead:ead/ead:archdesc/ead:did/ead:unitdate/text()|/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:dimensions|/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:extent">
			<div class="form-separator">
				<xsl:value-of select="$i18n.identityarea" />
			</div>
			<!-- ....Processing description level.... -->
			<xsl:apply-templates select="/ead:ead/ead:archdesc[@level]"/>
			<!-- ....Processing unitid....-->
			<xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:unitid">
				<div class="row-fluid metadata">
					<div class="label">
						<xsl:value-of select="$i18n.reference" />
					</div>
					<div class="value">
						<xsl:value-of select="my:escapeNewLine(/ead:ead/ead:archdesc/ead:did/ead:unitid)"/>
					</div>
				</div>
			</xsl:if>

			<!-- ....Processing @repositorycode....-->
			<!--
            <xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:unitid/@repositorycode"><div class="row-fluid metadata"><div class="label"><xsl:value-of select="$i18n.repositorycode" /></div><div class="value"><xsl:value-of select="my:escapeNewLine(/ead:ead/ead:archdesc/ead:did/ead:unitid/@repositorycode)"/></div></div></xsl:if>
            -->

			<!-- ....Processing @countrycode....-->
			<!--
            <xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:unitid/@countrycode"><div class="row-fluid metadata"><div class="label"><xsl:value-of select="$i18n.countrycode" /></div><div class="value"><xsl:value-of select="my:escapeNewLine(/ead:ead/ead:archdesc/ead:did/ead:unitid/@countrycode)"/></div></div></xsl:if>
            -->

			<!-- ....Processing unittitle.... -->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.title" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:did/ead:unittitle" />
			</xsl:call-template>

			<!-- ....Processing unittitle/@type....-->
			<xsl:variable name="titletype" select="/ead:ead/ead:archdesc/ead:did/ead:unittitle/@type" />
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.titletype" />
				<xsl:with-param name="value" select="$titletype" />
			</xsl:call-template>

			<!-- ....Processing repository....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.repository" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:did/ead:repository" />
			</xsl:call-template>

			<!-- ....Processing unitdate/@normal....-->
			<xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:unitdate/@normal">
				<xsl:call-template name="showInitialAndFinalDates" />
			</xsl:if>
			<!-- ....Processing unitdate....-->
			<!--
            <xsl:if test="/ead:ead/ead:archdesc/ead:did/ead:unitdate">
                <div class="row-fluid metadata">
                    <div class="label">
                        <xsl:value-of select="$i18n.unitdate" />
                    </div>
                    <div class="value">
                        <xsl:value-of select="my:escapeNewLine(/ead:ead/ead:archdesc/ead:did/ead:unitdate)"/>
                    </div>
                </div>
            </xsl:if>
            -->
			<!-- ....Processing dimensions....-->
			<xsl:call-template name="showDimensions" />
			<!-- ....Processing extent....-->
			<xsl:call-template name="showExtent" />
		</xsl:if>
	</xsl:template>
	<!-- Template for "context area" -->
	<xsl:template name="showContextArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:bioghist/ead:p|/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:geogname|/ead:ead/ead:archdesc/ead:legalstatus|/ead:ead/ead:archdesc/ead:custodhist/ead:p|/ead:ead/ead:archdesc/ead:acqinfo/ead:p">
			<div class="form-separator">
				<xsl:value-of select="$i18n.contextarea" />
			</div>
			<!-- ....Processing bioghist....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.bioghist" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:bioghist/ead:p" />
			</xsl:call-template>

			<!-- ....Processing geogname....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.geogname" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:did/ead:physdesc/ead:geogname" />
			</xsl:call-template>

			<!-- ....Processing legalstatus....-->
			<!--
            <xsl:call-template name="showField">
                <xsl:with-param name="label" select="$i18n.legalstatus" />
                <xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:accessrestrict/ead:legalstatus" />
            </xsl:call-template>
            -->

			<!-- ....Processing custodhist....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.custodhist" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:custodhist/ead:p" />
			</xsl:call-template>

			<!-- ....Processing acqinfo....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.acqinfo" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:acqinfo/ead:p" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!-- Template for "content structure area" -->
	<xsl:template name="showContentStructureArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:appraisal/ead:p|/ead:ead/ead:archdesc/ead:scopecontent/ead:p|/ead:ead/ead:archdesc/ead:accruals/ead:p|/ead:ead/ead:archdesc/ead:arrangement/ead:p">
			<div class="form-separator">
				<xsl:value-of select="$i18n.contentstructurearea" />
			</div>
			<!-- ....Processing scopecontent....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.scopecontent" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:scopecontent/ead:p" />
			</xsl:call-template>
			<!-- ....Processing appraisal....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.appraisal" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:appraisal/ead:p" />
			</xsl:call-template>
			<!-- ....Processing accruals....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.accruals" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:accruals/ead:p" />
			</xsl:call-template>
			<!-- ....Processing arrangement....-->
			<xsl:call-template name="showTable">
				<xsl:with-param name="label" select="$i18n.arrangement" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:arrangement" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!-- Template for "use access area" -->
	<xsl:template name="showUseAccessArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:accessrestrict/ead:p|/ead:ead/ead:archdesc/ead:userestrict/ead:p|/ead:ead/ead:archdesc/ead:did/ead:physloc/ead:p|/ead:ead/ead:archdesc/ead:did/ead:langmaterial|/ead:ead/ead:archdesc/ead:phystech/ead:p|/ead:ead/ead:archdesc/ead:otherfindaid/ead:p">
			<div class="form-separator">
				<xsl:value-of select="$i18n.useaccessarea" />
			</div>
			<!-- ....Processing accessrestrict....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.accessrestrict" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:accessrestrict/ead:p" />
			</xsl:call-template>
			<!-- ....Processing userestrict....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.userestrict" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:userestrict/ead:p" />
			</xsl:call-template>
			<!-- ....Processing physloc....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.physloc" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:did/ead:physloc/ead:p" />
			</xsl:call-template>
			<!-- ....Processing langmaterial....-->
			<xsl:variable name="langs">
				<xsl:call-template name="join">
					<xsl:with-param name="list" select="/ead:ead/ead:archdesc/ead:did/ead:langmaterial/ead:language/text()" />
					<xsl:with-param name="separator" select="', '" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:if test="$langs != ''">
				<xsl:call-template name="showField">
					<xsl:with-param name="label" select="$i18n.langmaterial" />
					<xsl:with-param name="value" select="$langs" />
				</xsl:call-template>
			</xsl:if>
			<!-- ....Processing phystech....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.phystech" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:phystech/ead:p" />
			</xsl:call-template>
			<!-- ....Processing otherfindaid....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.otherfindaid" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:otherfindaid/ead:p" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!-- Template for "use access area" -->
	<xsl:template name="showAlliedMaterialArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:originalsloc/ead:p|/ead:ead/ead:archdesc/ead:altformavail/ead:p|/ead:ead/ead:archdesc/ead:relatedmaterial/ead:p">
			<div class="form-separator">
				<xsl:value-of select="$i18n.alliedmaterialarea" />
			</div>
			<!-- ....Processing originalsloc....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.originalsloc" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:originalsloc/ead:p/text()" />
			</xsl:call-template>
			<!-- ....Processing altformavail....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.altformavail" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:altformavail/ead:p/text()" />
			</xsl:call-template>
			<!-- ....Processing relatedmaterial....-->
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.relatedmaterial" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:relatedmaterial/ead:p/text()" />
			</xsl:call-template>
			<!-- ....Processing bibliography....-->
			<xsl:call-template name="showBibliography">
				<xsl:with-param name="label" select="$i18n.bibliography" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:bibliography" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!-- Template for "use access area" -->
	<xsl:template name="showNotesArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:note/ead:p">
			<div class="form-separator">
				<xsl:value-of select="$i18n.notesarea" />
			</div>
		</xsl:if>
		<xsl:call-template name="showFieldWithAltRender">
			<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:note/ead:p" />
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="showOtherDescriptiveMetadataArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:odd/ead:p">
			<div class="form-separator">
				<xsl:value-of select="$i18n.oddarea" />
			</div>
		</xsl:if>
		<xsl:call-template name="showFieldWithAltRender">
			<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:odd/ead:p" />
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="showDescriptionControlArea">
		<xsl:if test="/ead:ead/ead:archdesc/ead:processinfo/ead:p">
			<div class="form-separator">
				<xsl:value-of select="$i18n.descriptioncontrolarea" />
			</div>
			<xsl:call-template name="showField">
				<xsl:with-param name="label" select="$i18n.processinfo" />
				<xsl:with-param name="value" select="/ead:ead/ead:archdesc/ead:processinfo/ead:p" />
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	<!--......Main template: driver......-->
	<xsl:template match="/">
		<div class="descriptiveMetadata">
			<xsl:call-template name="showIdentityArea" />
			<xsl:call-template name="showContextArea" />
			<xsl:call-template name="showContentStructureArea" />
			<xsl:call-template name="showUseAccessArea" />
			<xsl:call-template name="showAlliedMaterialArea" />
			<xsl:call-template name="showNotesArea" />
			<xsl:call-template name="showOtherDescriptiveMetadataArea" />
			<!--
                    <xsl:call-template name="showDescriptionControlArea" />
                -->
		</div>
	</xsl:template>
</xsl:stylesheet>
