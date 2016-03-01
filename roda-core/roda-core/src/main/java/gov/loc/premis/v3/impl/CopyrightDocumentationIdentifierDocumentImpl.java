/*
 * An XML document type.
 * Localname: copyrightDocumentationIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightDocumentationIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightDocumentationIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightDocumentationIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightDocumentationIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightDocumentationIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTDOCUMENTATIONIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightDocumentationIdentifier");
    
    
    /**
     * Gets the "copyrightDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType getCopyrightDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType)get_store().find_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightDocumentationIdentifier" element
     */
    public void setCopyrightDocumentationIdentifier(gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType copyrightDocumentationIdentifier)
    {
        generatedSetterHelperImpl(copyrightDocumentationIdentifier, COPYRIGHTDOCUMENTATIONIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType addNewCopyrightDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightDocumentationIdentifierComplexType)get_store().add_element_user(COPYRIGHTDOCUMENTATIONIDENTIFIER$0);
            return target;
        }
    }
}
