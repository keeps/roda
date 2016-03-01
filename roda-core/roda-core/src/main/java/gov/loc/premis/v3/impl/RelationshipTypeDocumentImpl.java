/*
 * An XML document type.
 * Localname: relationshipType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelationshipTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relationshipType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelationshipTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelationshipTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelationshipTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATIONSHIPTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relationshipType");
    
    
    /**
     * Gets the "relationshipType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRelationshipType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RELATIONSHIPTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relationshipType" element
     */
    public void setRelationshipType(gov.loc.premis.v3.StringPlusAuthority relationshipType)
    {
        generatedSetterHelperImpl(relationshipType, RELATIONSHIPTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relationshipType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRelationshipType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RELATIONSHIPTYPE$0);
            return target;
        }
    }
}
