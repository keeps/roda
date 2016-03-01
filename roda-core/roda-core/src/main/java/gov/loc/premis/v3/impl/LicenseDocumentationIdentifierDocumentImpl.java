/*
 * An XML document type.
 * Localname: licenseDocumentationIdentifier
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseDocumentationIdentifierDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one licenseDocumentationIdentifier(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class LicenseDocumentationIdentifierDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseDocumentationIdentifierDocument
{
    private static final long serialVersionUID = 1L;
    
    public LicenseDocumentationIdentifierDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationIdentifier");
    
    
    /**
     * Gets the "licenseDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType getLicenseDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "licenseDocumentationIdentifier" element
     */
    public void setLicenseDocumentationIdentifier(gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType licenseDocumentationIdentifier)
    {
        generatedSetterHelperImpl(licenseDocumentationIdentifier, LICENSEDOCUMENTATIONIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseDocumentationIdentifier" element
     */
    public gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType addNewLicenseDocumentationIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIER$0);
            return target;
        }
    }
}
