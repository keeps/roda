/*
 * An XML document type.
 * Localname: act
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.ActDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one act(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class ActDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.ActDocument
{
    private static final long serialVersionUID = 1L;
    
    public ActDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ACT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "act");
    
    
    /**
     * Gets the "act" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getAct()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(ACT$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "act" element
     */
    public void setAct(gov.loc.premis.v3.StringPlusAuthority act)
    {
        generatedSetterHelperImpl(act, ACT$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "act" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewAct()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(ACT$0);
            return target;
        }
    }
}
