/*
 * An XML document type.
 * Localname: objectIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one objectIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ObjectIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public ObjectIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECTIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectIdentifierType");
    
    
    /**
     * Gets the "objectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OBJECTIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "objectIdentifierType" element
     */
    public void setObjectIdentifierType(gov.loc.premis.v3.StringPlusAuthority objectIdentifierType)
    {
        generatedSetterHelperImpl(objectIdentifierType, OBJECTIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "objectIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewObjectIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OBJECTIDENTIFIERTYPE$0);
            return target;
        }
    }
}
