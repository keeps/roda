/*
 * An XML document type.
 * Localname: hwName
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.HwNameDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one hwName(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class HwNameDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.HwNameDocument
{
    private static final long serialVersionUID = 1L;
    
    public HwNameDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName HWNAME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "hwName");
    
    
    /**
     * Gets the "hwName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getHwName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(HWNAME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "hwName" element
     */
    public void setHwName(gov.loc.premis.v3.StringPlusAuthority hwName)
    {
        generatedSetterHelperImpl(hwName, HWNAME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "hwName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewHwName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(HWNAME$0);
            return target;
        }
    }
}
