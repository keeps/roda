/*
 * An XML document type.
 * Localname: copyrightJurisdiction
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightJurisdictionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightJurisdiction(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightJurisdictionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightJurisdictionDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightJurisdictionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTJURISDICTION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightJurisdiction");
    
    
    /**
     * Gets the "copyrightJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode getCopyrightJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().find_element_user(COPYRIGHTJURISDICTION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightJurisdiction" element
     */
    public void setCopyrightJurisdiction(gov.loc.premis.v3.CountryCode copyrightJurisdiction)
    {
        generatedSetterHelperImpl(copyrightJurisdiction, COPYRIGHTJURISDICTION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightJurisdiction" element
     */
    public gov.loc.premis.v3.CountryCode addNewCopyrightJurisdiction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CountryCode target = null;
            target = (gov.loc.premis.v3.CountryCode)get_store().add_element_user(COPYRIGHTJURISDICTION$0);
            return target;
        }
    }
}
