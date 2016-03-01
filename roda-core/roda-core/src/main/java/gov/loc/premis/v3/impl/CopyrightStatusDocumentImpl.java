/*
 * An XML document type.
 * Localname: copyrightStatus
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.CopyrightStatusDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one copyrightStatus(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class CopyrightStatusDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.CopyrightStatusDocument
{
    private static final long serialVersionUID = 1L;
    
    public CopyrightStatusDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName COPYRIGHTSTATUS$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightStatus");
    
    
    /**
     * Gets the "copyrightStatus" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getCopyrightStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(COPYRIGHTSTATUS$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "copyrightStatus" element
     */
    public void setCopyrightStatus(gov.loc.premis.v3.StringPlusAuthority copyrightStatus)
    {
        generatedSetterHelperImpl(copyrightStatus, COPYRIGHTSTATUS$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightStatus" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewCopyrightStatus()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(COPYRIGHTSTATUS$0);
            return target;
        }
    }
}
