/*
 * XML Type:  licenseDocumentationIdentifierComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML licenseDocumentationIdentifierComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class LicenseDocumentationIdentifierComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.LicenseDocumentationIdentifierComplexType
{
    private static final long serialVersionUID = 1L;
    
    public LicenseDocumentationIdentifierComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONIDENTIFIERTYPE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationIdentifierType");
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONIDENTIFIERVALUE$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationIdentifierValue");
    private static final javax.xml.namespace.QName LICENSEDOCUMENTATIONROLE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseDocumentationRole");
    
    
    /**
     * Gets the "licenseDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLicenseDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERTYPE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "licenseDocumentationIdentifierType" element
     */
    public void setLicenseDocumentationIdentifierType(gov.loc.premis.v3.StringPlusAuthority licenseDocumentationIdentifierType)
    {
        generatedSetterHelperImpl(licenseDocumentationIdentifierType, LICENSEDOCUMENTATIONIDENTIFIERTYPE$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseDocumentationIdentifierType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLicenseDocumentationIdentifierType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIERTYPE$0);
            return target;
        }
    }
    
    /**
     * Gets the "licenseDocumentationIdentifierValue" element
     */
    public java.lang.String getLicenseDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "licenseDocumentationIdentifierValue" element
     */
    public org.apache.xmlbeans.XmlString xgetLicenseDocumentationIdentifierValue()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            return target;
        }
    }
    
    /**
     * Sets the "licenseDocumentationIdentifierValue" element
     */
    public void setLicenseDocumentationIdentifierValue(java.lang.String licenseDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.setStringValue(licenseDocumentationIdentifierValue);
        }
    }
    
    /**
     * Sets (as xml) the "licenseDocumentationIdentifierValue" element
     */
    public void xsetLicenseDocumentationIdentifierValue(org.apache.xmlbeans.XmlString licenseDocumentationIdentifierValue)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$2, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(LICENSEDOCUMENTATIONIDENTIFIERVALUE$2);
            }
            target.set(licenseDocumentationIdentifierValue);
        }
    }
    
    /**
     * Gets the "licenseDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getLicenseDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(LICENSEDOCUMENTATIONROLE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "licenseDocumentationRole" element
     */
    public boolean isSetLicenseDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LICENSEDOCUMENTATIONROLE$4) != 0;
        }
    }
    
    /**
     * Sets the "licenseDocumentationRole" element
     */
    public void setLicenseDocumentationRole(gov.loc.premis.v3.StringPlusAuthority licenseDocumentationRole)
    {
        generatedSetterHelperImpl(licenseDocumentationRole, LICENSEDOCUMENTATIONROLE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseDocumentationRole" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewLicenseDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(LICENSEDOCUMENTATIONROLE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "licenseDocumentationRole" element
     */
    public void unsetLicenseDocumentationRole()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LICENSEDOCUMENTATIONROLE$4, 0);
        }
    }
}
