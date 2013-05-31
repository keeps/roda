<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: rodaODToLucene.xslt,v 1.0 2006/11/09 rcastro Exp $ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:foxml="info:fedora/fedora-system:def/foxml#"
	xmlns:eadc="http://roda.dgarq.gov.pt/2008/EADCSchema"
	xmlns:sparql="http://www.w3.org/2001/sw/DataAccess/rf1/result"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:roda="http://roda.dgarq.gov.pt/#">

	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

	<xsl:template name="rdfIndexer" match="rdf:RDF">

		<!-- ***  User permissions *** -->
		<xsl:for-each select="rdf:Description/roda:permission-read-user">
			<IndexField IFname="permissions.read.user" index="UN_TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="text()"/>
			</IndexField>
		</xsl:for-each>

		<!-- ***  Group permissions *** -->
		<xsl:for-each select="rdf:Description/roda:permission-read-group">
			<IndexField IFname="permissions.read.group" index="UN_TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="text()"/>
			</IndexField>
		</xsl:for-each>

	</xsl:template>

	<xsl:template name="eadcIndexer" match="eadc:ead-c">

		<xsl:param name="pid"/>

		<!-- Complete reference -->
		<IndexField IFname="ead.completereference" index="TOKENIZED" store="YES" termVector="NO">
			
			<xsl:variable name="reference"
				select="document(concat('http://RODACOREHOSTPORT/roda-core/getCompleteReference/', $pid))/completeReference/text()"/>
			
			<xsl:value-of select="$reference"/>

		</IndexField>

		<!-- ***  Identificação *** -->

		<!-- @level -->
		<xsl:if test="@level">
			<IndexField IFname="ead.level" index="UN_TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="@level"/>
			</IndexField>
		</xsl:if>

		<!-- unitid -->
		<xsl:if test="eadc:did/eadc:unitid/text()">
			<IndexField IFname="ead.unitid" index="UN_TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:unitid/text()"/>
			</IndexField>
		</xsl:if>

		<!-- repositorycode -->
		<xsl:if test="eadc:did/eadc:unitid/@repositorycode">
			<IndexField IFname="ead.repositorycode" index="UN_TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:unitid/@repositorycode"/>
			</IndexField>
		</xsl:if>

		<!-- unittitle -->
		<xsl:if test="eadc:did/eadc:unittitle/text()">
			<IndexField IFname="ead.unittitle" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:unittitle/text()"/>
			</IndexField>
		</xsl:if>

		<!-- unitdate -->
		<xsl:for-each select="eadc:did/eadc:unitdate">
			<xsl:if test="contains(@normal, '/')">
				<IndexField IFname="ead.unitdate.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.unitdate.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-after(@normal, '/'), '-', '' )"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="not(contains(@normal, '/'))">
				<IndexField IFname="ead.unitdate.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.unitdate.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/p -->
		<xsl:for-each select="eadc:did/eadc:physdesc/p">
			<IndexField IFname="ead.physdesc" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
		</xsl:for-each>

		<!-- physdesc/dimensions -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:dimensions">
			<IndexField IFname="ead.physdesc.dimensions" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./eadc:p/text()"/>
			</IndexField>
			<xsl:if test="@unit">
				<IndexField IFname="ead.physdesc.dimensions.unit" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@unit"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/date -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:date">
			<xsl:if test="contains(@normal, '/')">
				<IndexField IFname="ead.physdesc.date.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.physdesc.date.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-after(@normal, '/'), '-', '' )"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="not(contains(@normal, '/'))">
				<IndexField IFname="ead.physdesc.date.initial" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.physdesc.date.final" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- physdesc/extent -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:extent">
			<IndexField IFname="ead.physdesc.extent" index="UN_TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./text()"/>
			</IndexField>
			<xsl:if test="@unit">
				<IndexField IFname="ead.physdesc.extent.unit" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@unit"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- *** Contexto *** -->

		<!-- origination -->
		<xsl:if test="eadc:did/eadc:origination/text()">
			<IndexField IFname="ead.origination" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:origination/text()"/>
			</IndexField>
		</xsl:if>

		<!-- bioghist/p -->
		<xsl:if test="eadc:bioghist/eadc:p/text()">
			<IndexField IFname="ead.bioghist" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:bioghist/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- bioghist/chronlist/chronitem/date -->
		<xsl:for-each select="eadc:bioghist/eadc:chronlist/eadc:chronitem/eadc:date">
			<xsl:if test="contains(@normal, '/')">
				<IndexField IFname="ead.bioghist.chronitem.date.initial" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.bioghist.chronitem.date.final" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( substring-after(@normal, '/'), '-', '' )"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="not(contains(@normal, '/'))">
				<IndexField IFname="ead.bioghist.chronitem.date.initial" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
				<IndexField IFname="ead.bioghist.chronitem.date.final" index="UN_TOKENIZED"
					store="YES" termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- bioghist/chronlist/chronitem//event -->
		<xsl:for-each select="eadc:bioghist/eadc:chronlist/eadc:chronitem//eadc:event/text()">
			<IndexField IFname="ead.bioghist.chronitem.event" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="."/>
			</IndexField>
		</xsl:for-each>

		<!-- custodhist -->
		<xsl:if test="eadc:custodhist/eadc:p/text()">
			<IndexField IFname="ead.custodhist" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:custodhist/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- acqinfo -->
		<xsl:if test="eadc:acqinfo/eadc:p/text()">
			<IndexField IFname="ead.acqinfo" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:acqinfo/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- acqinfo/date -->
		<xsl:for-each select="eadc:acqinfo/eadc:date">
			<xsl:if test="contains(@normal, '/')">
				<IndexField IFname="ead.acqinfo.date" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( substring-before(@normal, '/'), '-', '' )"/>
				</IndexField>
			</xsl:if>
			<xsl:if test="not(contains(@normal, '/'))">
				<IndexField IFname="ead.acqinfo.date" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="translate( @normal, '-', '' )"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- acqinfo/num -->
		<xsl:if test="eadc:acqinfo/eadc:num/text()">
			<IndexField IFname="ead.acqinfo.num" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:acqinfo/eadc:num/text()"/>
			</IndexField>
		</xsl:if>

		<!-- *** Conteúdo e estructura *** -->

		<!-- scopecontent -->
		<xsl:if test="eadc:scopecontent/eadc:p/text()">
			<IndexField IFname="ead.scopecontent" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:scopecontent/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- appraisal -->
		<xsl:if test="eadc:appraisal/eadc:p/text()">
			<IndexField IFname="ead.appraisal" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:appraisal/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- accruals -->
		<xsl:if test="eadc:accruals/eadc:p/text()">
			<IndexField IFname="ead.accruals" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:accruals/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- arrangement -->
		<xsl:if test="eadc:arrangement/eadc:p/text()">
			<IndexField IFname="ead.arrangement" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:arrangement/eadc:p/text()"/>
			</IndexField>
		</xsl:if>


		<!-- ** Condições de accesso e utilização *** -->

		<!-- accessrestrict -->
		<xsl:if test="eadc:accessrestrict/eadc:p/text()">
			<IndexField IFname="ead.accessrestrict" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:accessrestrict/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- userestrict -->
		<xsl:if test="eadc:userestrict/eadc:p/text()">
			<IndexField IFname="ead.userestrict" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:userestrict/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- did/langmaterial/language -->
		<xsl:for-each select="eadc:did/eadc:langmaterial/eadc:language/text()">
			<IndexField IFname="ead.langmaterial.language" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="."/>
			</IndexField>
		</xsl:for-each>

		<!-- phystech -->
		<xsl:if test="eadc:phystech/eadc:p/text()">
			<IndexField IFname="ead.phystech" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:phystech/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- did/materialspec -->
		<xsl:if test="eadc:did/eadc:materialspec/text()">
			<IndexField IFname="ead.materialspec" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:did/eadc:materialspec/text()"/>
			</IndexField>
		</xsl:if>

		<!-- did/physdesc/physfacet -->
		<xsl:for-each select="eadc:did/eadc:physdesc/eadc:physfacet">
			<IndexField IFname="ead.physdesc.physfacet" index="TOKENIZED" store="YES"
				termVector="NO">
				<xsl:value-of select="./eadc:p/text()"/>
			</IndexField>
			<xsl:if test="@unit">
				<IndexField IFname="ead.physdesc.physfacet.unit" index="UN_TOKENIZED" store="YES"
					termVector="NO">
					<xsl:value-of select="./@unit"/>
				</IndexField>
			</xsl:if>
		</xsl:for-each>

		<!-- otherfindaid -->
		<xsl:if test="eadc:otherfindaid/eadc:p/text()">
			<IndexField IFname="ead.otherfindaid" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:otherfindaid/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- Documentação associada -->

		<!-- relatedmaterial -->
		<xsl:if test="eadc:relatedmaterial/eadc:p/text()">
			<IndexField IFname="ead.relatedmaterial" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:relatedmaterial/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- bibliography -->
		<xsl:if test="eadc:bibliography/eadc:p/text()">
			<IndexField IFname="ead.bibliography" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:bibliography/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

		<!-- Notas -->

		<xsl:for-each select="eadc:note/eadc:p">
			<IndexField IFname="ead.note" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="text()"/>
			</IndexField>
		</xsl:for-each>

		<!-- Controlo de descrição -->

		<!-- processinfo -->
		<!--
		<xsl:if test="eadc:processinfo/eadc:p/text()">
			<IndexField IFname="ead.processinfo" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:processinfo/eadc:p/text()"/>
			</IndexField>
		</xsl:if>
		-->

		<!-- prefercite -->
		<xsl:if test="eadc:prefercite/eadc:p/text()">
			<IndexField IFname="ead.prefercite" index="TOKENIZED" store="YES" termVector="NO">
				<xsl:value-of select="eadc:prefercite/eadc:p/text()"/>
			</IndexField>
		</xsl:if>

	</xsl:template>

</xsl:stylesheet>
