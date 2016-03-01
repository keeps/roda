/*
 * An XML document type.
 * Localname: otherRightsDocumentationIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsDocumentationIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsDocumentationIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsDocumentationIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsDocumentationIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsDocumentationIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationIdentifier");
    
    
    /**
     * Gets the "otherRightsDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType getOtherRightsDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsDocumentationIdentifier" element
     */
    public void setOtherRightsDocumentationIdentifier(gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType otherRightsDocumentationIdentifier)
    {
        generatedSetterHelperImpl(otherRightsDocumentationIdentifier, OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType addNewOtherRightsDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsDocumentationIdentifierComplexType)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIER$0);
            return target;
        }
    }
}
