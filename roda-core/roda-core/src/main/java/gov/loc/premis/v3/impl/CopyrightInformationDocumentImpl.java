/*
 * An XML document type.
 * Localname: copyrightInformation
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightInformationDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightInformation(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightInformationDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightInformationDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightInformationDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTINFORMATION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightInformation");
    
    
    /**
     * Gets the "copyrightInformation" element
     */
    public gov.loc.premis.v3.CopyrightInformationComplexType getCopyrightInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightInformationComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightInformationComplexType)get_store().find_element_user(COPYRIGHTINFORMATION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightInformation" element
     */
    public void setCopyrightInformation(gov.loc.premis.v3.CopyrightInformationComplexType copyrightInformation)
    {
        generatedSetterHelperImpl(copyrightInformation, COPYRIGHTINFORMATION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightInformation" element
     */
    public gov.loc.premis.v3.CopyrightInformationComplexType addNewCopyrightInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightInformationComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightInformationComplexType)get_store().add_element_user(COPYRIGHTINFORMATION$0);
            return target;
        }
    }
}
