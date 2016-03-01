/*
 * An XML document type.
 * Localname: objectIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ObjectIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one objectIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ObjectIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ObjectIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public ObjectIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECTIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "objectIdentifierValue");
    
    
    /**
     * Gets the "objectIdentifierValue" element
     */
    public java.lang.String getObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "objectIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetObjectIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OBJECTIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "objectIdentifierValue" element
     */
    public void setObjectIdentifierValue(java.lang.String objectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OBJECTIDENTIFIERVALUE$0);
            }
            target.setStringValue(objectIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "objectIdentifierValue" element
     */
    public void xsetObjectIdentifierValue(org.apache.xmlbeans.XmlString objectIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OBJECTIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OBJECTIDENTIFIERVALUE$0);
            }
            target.set(objectIdentifierValue);
        }
    }
}
