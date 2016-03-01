/*
 * An XML document type.
 * Localname: inhibitorTarget
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.InhibitorTargetDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one inhibitorTarget(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class InhibitorTargetDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.InhibitorTargetDocument
{
    private static final long serialVersionUID = 1L;
    
    public InhibitorTargetDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName INHIBITORTARGET$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "inhibitorTarget");
    
    
    /**
     * Gets the "inhibitorTarget" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getInhibitorTarget()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(INHIBITORTARGET$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "inhibitorTarget" element
     */
    public void setInhibitorTarget(gov.loc.premis.v3.StringPlusAuthority inhibitorTarget)
    {
        generatedSetterHelperImpl(inhibitorTarget, INHIBITORTARGET$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "inhibitorTarget" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewInhibitorTarget()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(INHIBITORTARGET$0);
            return target;
        }
    }
}
