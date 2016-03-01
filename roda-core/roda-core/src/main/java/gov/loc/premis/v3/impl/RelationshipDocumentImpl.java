/*
 * An XML document type.
 * Localname: relationship
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RelationshipDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one relationship(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class RelationshipDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RelationshipDocument
{
    private static final long serialVersionUID = 1L;
    
    public RelationshipDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RELATIONSHIP$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "relationship");
    
    
    /**
     * Gets the "relationship" element
     */
    public gov.loc.premis.v3.RelationshipComplexType getRelationship()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelationshipComplexType target = null;
            target = (gov.loc.premis.v3.RelationshipComplexType)get_store().find_element_user(RELATIONSHIP$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "relationship" element
     */
    public void setRelationship(gov.loc.premis.v3.RelationshipComplexType relationship)
    {
        generatedSetterHelperImpl(relationship, RELATIONSHIP$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "relationship" element
     */
    public gov.loc.premis.v3.RelationshipComplexType addNewRelationship()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RelationshipComplexType target = null;
            target = (gov.loc.premis.v3.RelationshipComplexType)get_store().add_element_user(RELATIONSHIP$0);
            return target;
        }
    }
}
