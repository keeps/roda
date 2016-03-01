/*
 * An XML document type.
 * Localname: relationshipSubType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelationshipSubTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relationshipSubType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelationshipSubTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelationshipSubTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelationshipSubTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATIONSHIPSUBTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relationshipSubType");
    
    
    /**
     * Gets the "relationshipSubType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelationshipSubType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATIONSHIPSUBTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relationshipSubType" element
     */
    public void setRelationshipSubType(gov.loc.premis.v3.StringPlusAuthority relationshipSubType)
    {
        generatedSetterHelperImpl(relationshipSubType, RELATIONSHIPSUBTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relationshipSubType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelationshipSubType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATIONSHIPSUBTYPE$0);
            return target;
        }
    }
}
