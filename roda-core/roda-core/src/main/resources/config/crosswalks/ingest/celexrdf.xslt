<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:cdm="http://publications.europa.eu/ontology/cdm#"
    xmlns:CMRAnnotation="http://publications.europa.eu/ontology/annotation#"
    xmlns:tdm="http://publications.europa.eu/ontology/tdm#"
    xmlns:cat_language="http://publications.europa.eu/resource/authority/language"
    xmlns:cat_corporate-body="http://publications.europa.eu/resource/authority/corporate-body"
    xmlns:cat_court_formation="http://publications.europa.eu/resource/authority/formjug"
    xmlns:cat_procedure="http://publications.europa.eu/resource/authority/procedure"
    xmlns:cat_treaty="http://publications.europa.eu/resource/authority/treaty"
    xmlns:cat_event="http://publications.europa.eu/resource/authority/event"
    xmlns:cat_fd_070="http://publications.europa.eu/resource/authority/fd_070"
    xmlns:cat_fd_160="http://publications.europa.eu/resource/authority/fd_160"
    xmlns:cat_fd_577="http://publications.europa.eu/resource/authority/fd_577"
    xmlns:cat_fd_578="http://publications.europa.eu/resource/authority/fd_578"
    xmlns:cat_fd_030="http://publications.europa.eu/resource/authority/fd_030"
    xmlns:cat_fd_100="http://publications.europa.eu/resource/authority/fd_100"
    xmlns:cat_fd_110="http://publications.europa.eu/resource/authority/fd_110"
    xmlns:cat_procjur="http://publications.europa.eu/resource/authority/procjur"
    xmlns:cat_procresult="http://publications.europa.eu/resource/authority/procresult"
    xmlns:cat_country="http://publications.europa.eu/resource/authority/country"
    xmlns:cat_case_status="http://publications.europa.eu/resource/authority/case-status"
    xmlns:cat_procjur-type="http://publications.europa.eu/resource/authority/procjur-type"
    xmlns:cat_role-qualifier="http://publications.europa.eu/resource/authority/role-qualifier"
    xmlns:cat_resourcetype="http://publications.europa.eu/resource/authority/resource-type"
    xmlns:case="mynamespace/case"
    xmlns:case-event="mynamespace/case-event"
    xmlns:celex="mynamespace/celex"
    xmlns:numpub="mynamespace/numpub"
    xmlns:agent="mynamespace/agent"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:j1="http://www.w3.org/2004/02/skos/core#"
    xmlns:j.2="http://publications.europa.eu/ontology/cdm/cmr#"
    exclude-result-prefixes="xs cdm CMRAnnotation j.2 j1 owl rdf rdfs xsd agent numpub celex case-event case cat_resourcetype cat_role-qualifier cat_procjur-type cat_case_status cat_country cat_procresult cat_procjur cat_fd_110 cat_fd_100 cat_fd_030 cat_fd_578 cat_fd_577 cat_fd_160 cat_fd_070 cat_treaty cat_procedure cat_court_formation cat_corporate-body cat_language tdm cat_event"
    version="1.0">
    
    <xsl:output method="xml" indent="yes"/>
    
    <xsl:template match="/">
        <doc>
            <field name="level">work</field>
           <xsl:apply-templates/>
        </doc>
    </xsl:template>
    
    <xsl:template match="cdm:work_date_document">
        <field name="dateInitial"><xsl:value-of select="."/>T00:00:00Z</field>
    </xsl:template>
    
    <xsl:template match="cdm:work_date_creation_legacy">
        <field name="dateFinal"><xsl:value-of select="."/>T00:00:00Z</field>
    </xsl:template>
    
    <xsl:template match="cdm:expression_title[@xml:lang='en']">
        <field name="title">
            <xsl:value-of select="."/>
        </field>
    </xsl:template>
    
    
    <xsl:template match="cdm:expression_case-law_indicator_decision[@xml:lang='en']">
        <field name="description">
            <xsl:value-of select="."/>
        </field>
    </xsl:template>
    
    <xsl:template match="rdf:Description[rdf:type/@rdf:resource='http://publications.europa.eu/ontology/cdm#expression'][cdm:expression_uses_language/@rdf:resource='http://publications.europa.eu/resource/authority/language/ENG']/*[not(name(.)='cdm:expression_title')and(not(name(.)='cdm:expression_case-law_indicator_decision'))]">
        <xsl:if test="not(normalize-space(.)='')">
            <field name="{name(.)}_txt">
                <xsl:value-of select="."/>
            </field>
        </xsl:if>
        
    </xsl:template>   
    
    <xsl:template match="text()" priority="-1"/>
    
</xsl:stylesheet>