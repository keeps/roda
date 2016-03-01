/*
 * An XML document type.
 * Localname: copyrightDocumentationIdentifierValue
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightDocumentationIdentifierValueDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightDocumentationIdentifierValue(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightDocumentationIdentifierValueDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightDocumentationIdentifierValueDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightDocumentationIdentifierValueDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationIdentifierValue");
    
    
    /**
     * Gets the "copyrightDocumentationIdentifierValue" element
     */
    public java.lang.String getCopyrightDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "copyrightDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetCopyrightDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "copyrightDocumentationIdentifierValue" element
     */
    public void setCopyrightDocumentationIdentifierValue(java.lang.String copyrightDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.setStringValue(copyrightDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "copyrightDocumentationIdentifierValue" element
     */
    public void xsetCopyrightDocumentationIdentifierValue(org.apache.xmlbeans.XmlString copyrightDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERVALUE$0);
            }
            target.set(copyrightDocumentationIdentifierValue);
        }
    }
}
