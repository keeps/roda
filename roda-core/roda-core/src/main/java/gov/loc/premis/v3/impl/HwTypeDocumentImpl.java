/*
 * An XML document type.
 * Localname: hwType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.HwTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one hwType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class HwTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.HwTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public HwTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HWTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "hwType");
    
    
    /**
     * Gets the "hwType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getHwType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(HWTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "hwType" element
     */
    public void setHwType(gov.loc.premis.v3.StringPlusAuthority hwType)
    {
        generatedSetterHelperImpl(hwType, HWTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "hwType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewHwType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(HWTYPE$0);
            return target;
        }
    }
}
