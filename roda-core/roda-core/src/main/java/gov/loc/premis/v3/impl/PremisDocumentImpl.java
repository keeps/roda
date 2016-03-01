/*
 * An XML document type.
 * Localname: premis
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PremisDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one premis(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class PremisDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PremisDocument
{
    private static final long serialVersionUID = 1L;
    
    public PremisDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PREMIS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "premis");
    
    
    /**
     * Gets the "premis" element
     */
    public gov.loc.premis.v3.PremisComplexType getPremis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PremisComplexType target = null;
            target = (gov.loc.premis.v3.PremisComplexType)get_store().find_element_user(PREMIS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "premis" element
     */
    public void setPremis(gov.loc.premis.v3.PremisComplexType premis)
    {
        generatedSetterHelperImpl(premis, PREMIS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "premis" element
     */
    public gov.loc.premis.v3.PremisComplexType addNewPremis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.PremisComplexType target = null;
            target = (gov.loc.premis.v3.PremisComplexType)get_store().add_element_user(PREMIS$0);
            return target;
        }
    }
}
