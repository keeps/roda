/*
 * An XML document type.
 * Localname: swType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.SwTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one swType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class SwTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.SwTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public SwTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName SWTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "swType");
    
    
    /**
     * Gets the "swType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getSwType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(SWTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "swType" element
     */
    public void setSwType(gov.loc.premis.v3.StringPlusAuthority swType)
    {
        generatedSetterHelperImpl(swType, SWTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "swType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewSwType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(SWTYPE$0);
            return target;
        }
    }
}
