/*
 * An XML document type.
 * Localname: otherRightsDocumentationIdentifierType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsDocumentationIdentifierTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsDocumentationIdentifierType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsDocumentationIdentifierTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsDocumentationIdentifierTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsDocumentationIdentifierTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsDocumentationIdentifierType");
    
    
    /**
     * Gets the "otherRightsDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getOtherRightsDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsDocumentationIdentifierType" element
     */
    public void setOtherRightsDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority otherRightsDocumentationIdentifierType)
    {
        generatedSetterHelperImpl(otherRightsDocumentationIdentifierType, OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewOtherRightsDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(OTHERRIGHTSDOCUMENTATIONIDENTIFIERTYPE$0);
            return target;
        }
    }
}
