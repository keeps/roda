/*
 * An XML document type.
 * Localname: preservationLevelRole
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PreservationLevelRoleDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one preservationLevelRole(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class PreservationLevelRoleDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PreservationLevelRoleDocument
{
    private static final long serialVersionUID = 1L;
    
    public PreservationLevelRoleDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PRESERVATIONLEVELROLE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "preservationLevelRole");
    
    
    /**
     * Gets the "preservationLevelRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getPreservationLevelRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(PRESERVATIONLEVELROLE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "preservationLevelRole" element
     */
    public void setPreservationLevelRole(gov.loc.premis.v3.StringPlusAuthority preservationLevelRole)
    {
        generatedSetterHelperImpl(preservationLevelRole, PRESERVATIONLEVELROLE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "preservationLevelRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewPreservationLevelRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(PRESERVATIONLEVELROLE$0);
            return target;
        }
    }
}
