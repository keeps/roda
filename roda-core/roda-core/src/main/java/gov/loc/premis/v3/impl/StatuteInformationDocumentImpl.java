/*
 * An XML document type.
 * Localname: statuteInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.StatuteInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one statuteInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class StatuteInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.StatuteInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public StatuteInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName STATUTEINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteInformation");
    
    
    /**
     * Gets the "statuteInformation" element
     */
    public gov.loc.premis.v3.StatuteInformationComplexType getStatuteInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteInformationComplexType target = null;
            target = (gov.loc.premis.v3.StatuteInformationComplexType)get_store().find_element_user(STATUTEINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "statuteInformation" element
     */
    public void setStatuteInformation(gov.loc.premis.v3.StatuteInformationComplexType statuteInformation)
    {
        generatedSetterHelperImpl(statuteInformation, STATUTEINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "statuteInformation" element
     */
    public gov.loc.premis.v3.StatuteInformationComplexType addNewStatuteInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteInformationComplexType target = null;
            target = (gov.loc.premis.v3.StatuteInformationComplexType)get_store().add_element_user(STATUTEINFORMATION$0);
            return target;
        }
    }
}
