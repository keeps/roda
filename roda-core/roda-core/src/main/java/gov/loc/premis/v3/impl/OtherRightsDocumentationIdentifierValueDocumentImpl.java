/*
 * An XML document type.
 * Localname: otherRightsDocumentationIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsDocumentationIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsDocumentationIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsDocumentationIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsDocumentationIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsDocumentationIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationIdentifierValue");
    
    
    /**
     * Gets the "otherRightsDocumentationIdentifierValue" element
     */
    public java.lang.String getOtherRightsDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "otherRightsDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetOtherRightsDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsDocumentationIdentifierValue" element
     */
    public void setOtherRightsDocumentationIdentifierValue(java.lang.String otherRightsDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.setStringValue(otherRightsDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "otherRightsDocumentationIdentifierValue" element
     */
    public void xsetOtherRightsDocumentationIdentifierValue(org.apache.xmlbeans.XmlString otherRightsDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.set(otherRightsDocumentationIdentifierValue);
        }
    }
}
