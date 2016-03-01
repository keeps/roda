/*
 * An XML document type.
 * Localname: objectIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one objectIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ObjectIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public ObjectIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectIdentifier");
    
    
    /**
     * Gets the "objectIdentifier" element
     */
    public gov.loc.premis.v3.ObjectIdentifierComplexType getObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.ObjectIdentifierComplexType)get_store().find_element_user(OBJECTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "objectIdentifier" element
     */
    public void setObjectIdentifier(gov.loc.premis.v3.ObjectIdentifierComplexType objectIdentifier)
    {
        generatedSetterHelperImpl(objectIdentifier, OBJECTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "objectIdentifier" element
     */
    public gov.loc.premis.v3.ObjectIdentifierComplexType addNewObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.ObjectIdentifierComplexType)get_store().add_element_user(OBJECTIDENTIFIER$0);
            return target;
        }
    }
}
