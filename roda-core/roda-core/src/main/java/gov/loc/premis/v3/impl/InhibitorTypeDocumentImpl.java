/*
 * An XML document type.
 * Localname: inhibitorType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.InhibitorTypeDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one inhibitorType(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class InhibitorTypeDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.InhibitorTypeDocument
{
    private static final long serialVersionUID = 1L;
    
    public InhibitorTypeDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName INHIBITORTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitorType");
    
    
    /**
     * Gets the "inhibitorType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getInhibitorType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(INHIBITORTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "inhibitorType" element
     */
    public void setInhibitorType(gov.loc.premis.v3.StringPlusAuthority inhibitorType)
    {
        generatedSetterHelperImpl(inhibitorType, INHIBITORTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "inhibitorType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewInhibitorType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(INHIBITORTYPE$0);
            return target;
        }
    }
}
