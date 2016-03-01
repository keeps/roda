/*
 * An XML document type.
 * Localname: otherRightsInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.OtherRightsInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one otherRightsInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class OtherRightsInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.OtherRightsInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public OtherRightsInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OTHERRIGHTSINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsInformation");
    
    
    /**
     * Gets the "otherRightsInformation" element
     */
    public gov.loc.premis.v3.OtherRightsInformationComplexType getOtherRightsInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsInformationComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsInformationComplexType)get_store().find_element_user(OTHERRIGHTSINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "otherRightsInformation" element
     */
    public void setOtherRightsInformation(gov.loc.premis.v3.OtherRightsInformationComplexType otherRightsInformation)
    {
        generatedSetterHelperImpl(otherRightsInformation, OTHERRIGHTSINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsInformation" element
     */
    public gov.loc.premis.v3.OtherRightsInformationComplexType addNewOtherRightsInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsInformationComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsInformationComplexType)get_store().add_element_user(OTHERRIGHTSINFORMATION$0);
            return target;
        }
    }
}
