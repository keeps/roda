/*
 * An XML document type.
 * Localname: inhibitors
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.InhibitorsDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one inhibitors(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class InhibitorsDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.InhibitorsDocument
{
    private static final long serialVersionUID = 1L;
    
    public InhibitorsDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName INHIBITORS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitors");
    
    
    /**
     * Gets the "inhibitors" element
     */
    public gov.loc.premis.v3.InhibitorsComplexType getInhibitors()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.InhibitorsComplexType target = null;
            target = (gov.loc.premis.v3.InhibitorsComplexType)get_store().find_element_user(INHIBITORS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "inhibitors" element
     */
    public void setInhibitors(gov.loc.premis.v3.InhibitorsComplexType inhibitors)
    {
        generatedSetterHelperImpl(inhibitors, INHIBITORS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "inhibitors" element
     */
    public gov.loc.premis.v3.InhibitorsComplexType addNewInhibitors()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.InhibitorsComplexType target = null;
            target = (gov.loc.premis.v3.InhibitorsComplexType)get_store().add_element_user(INHIBITORS$0);
            return target;
        }
    }
}
