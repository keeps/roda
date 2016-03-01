/*
 * An XML document type.
 * Localname: copyrightDocumentationIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightDocumentationIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightDocumentationIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightDocumentationIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightDocumentationIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightDocumentationIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationIdentifierType");
    
    
    /**
     * Gets the "copyrightDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCopyrightDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightDocumentationIdentifierType" element
     */
    public void setCopyrightDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority copyrightDocumentationIdentifierType)
    {
        generatedSetterHelperImpl(copyrightDocumentationIdentifierType, COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCopyrightDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIERTYPE$0);
            return target;
        }
    }
}
