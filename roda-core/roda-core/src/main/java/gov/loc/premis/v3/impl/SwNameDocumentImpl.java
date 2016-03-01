/*
 * An XML document type.
 * Localname: swName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SwNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one swName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SwNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SwNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public SwNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SWNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "swName");
    
    
    /**
     * Gets the "swName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSwName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SWNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "swName" element
     */
    public void setSwName(gov.loc.premis.v3.StringPlusAuthority swName)
    {
        generatedSetterHelperImpl(swName, SWNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "swName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSwName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SWNAME$0);
            return target;
        }
    }
}
