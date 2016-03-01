/*
 * An XML document type.
 * Localname: statuteDocumentationIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteDocumentationIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteDocumentationIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteDocumentationIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteDocumentationIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteDocumentationIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEDOCUMENTATIONIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteDocumentationIdentifier");
    
    
    /**
     * Gets the "statuteDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType getStatuteDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType)get_store().find_element_user(STATUTEDOCUMENTATIONIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteDocumentationIdentifier" element
     */
    public void setStatuteDocumentationIdentifier(gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType statuteDocumentationIdentifier)
    {
        generatedSetterHelperImpl(statuteDocumentationIdentifier, STATUTEDOCUMENTATIONIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType addNewStatuteDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.StatuteDocumentationIdentifierComplexType)get_store().add_element_user(STATUTEDOCUMENTATIONIDENTIFIER$0);
            return target;
        }
    }
}
