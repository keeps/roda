/*
 * XML Type:  rightsStatementComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.RightsStatementComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML rightsStatementComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class RightsStatementComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.RightsStatementComplexType
{
    private static final long serialVersionUID = 1L;
    
    public RightsStatementComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName RIGHTSSTATEMENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsStatementIdentifier");
    private static final javax.xml.namespace.QName RIGHTSBASIS$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsBasis");
    private static final javax.xml.namespace.QName COPYRIGHTINFORMATION$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "copyrightInformation");
    private static final javax.xml.namespace.QName LICENSEINFORMATION$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "licenseInformation");
    private static final javax.xml.namespace.QName STATUTEINFORMATION$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "statuteInformation");
    private static final javax.xml.namespace.QName OTHERRIGHTSINFORMATION$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "otherRightsInformation");
    private static final javax.xml.namespace.QName RIGHTSGRANTED$12 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rightsGranted");
    private static final javax.xml.namespace.QName LINKINGOBJECTIDENTIFIER$14 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingObjectIdentifier");
    private static final javax.xml.namespace.QName LINKINGAGENTIDENTIFIER$16 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingAgentIdentifier");
    
    
    /**
     * Gets the "rightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.RightsStatementIdentifierComplexType getRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementIdentifierComplexType)get_store().find_element_user(RIGHTSSTATEMENTIDENTIFIER$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsStatementIdentifier" element
     */
    public void setRightsStatementIdentifier(gov.loc.premis.v3.RightsStatementIdentifierComplexType rightsStatementIdentifier)
    {
        generatedSetterHelperImpl(rightsStatementIdentifier, RIGHTSSTATEMENTIDENTIFIER$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.RightsStatementIdentifierComplexType addNewRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.RightsStatementIdentifierComplexType)get_store().add_element_user(RIGHTSSTATEMENTIDENTIFIER$0);
            return target;
        }
    }
    
    /**
     * Gets the "rightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(RIGHTSBASIS$2, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "rightsBasis" element
     */
    public void setRightsBasis(gov.loc.premis.v3.StringPlusAuthority rightsBasis)
    {
        generatedSetterHelperImpl(rightsBasis, RIGHTSBASIS$2, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "rightsBasis" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewRightsBasis()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(RIGHTSBASIS$2);
            return target;
        }
    }
    
    /**
     * Gets the "copyrightInformation" element
     */
    public gov.loc.premis.v3.CopyrightInformationComplexType getCopyrightInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightInformationComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightInformationComplexType)get_store().find_element_user(COPYRIGHTINFORMATION$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "copyrightInformation" element
     */
    public boolean isSetCopyrightInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(COPYRIGHTINFORMATION$4) != 0;
        }
    }
    
    /**
     * Sets the "copyrightInformation" element
     */
    public void setCopyrightInformation(gov.loc.premis.v3.CopyrightInformationComplexType copyrightInformation)
    {
        generatedSetterHelperImpl(copyrightInformation, COPYRIGHTINFORMATION$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "copyrightInformation" element
     */
    public gov.loc.premis.v3.CopyrightInformationComplexType addNewCopyrightInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.CopyrightInformationComplexType target = null;
            target = (gov.loc.premis.v3.CopyrightInformationComplexType)get_store().add_element_user(COPYRIGHTINFORMATION$4);
            return target;
        }
    }
    
    /**
     * Unsets the "copyrightInformation" element
     */
    public void unsetCopyrightInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(COPYRIGHTINFORMATION$4, 0);
        }
    }
    
    /**
     * Gets the "licenseInformation" element
     */
    public gov.loc.premis.v3.LicenseInformationComplexType getLicenseInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseInformationComplexType target = null;
            target = (gov.loc.premis.v3.LicenseInformationComplexType)get_store().find_element_user(LICENSEINFORMATION$6, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "licenseInformation" element
     */
    public boolean isSetLicenseInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LICENSEINFORMATION$6) != 0;
        }
    }
    
    /**
     * Sets the "licenseInformation" element
     */
    public void setLicenseInformation(gov.loc.premis.v3.LicenseInformationComplexType licenseInformation)
    {
        generatedSetterHelperImpl(licenseInformation, LICENSEINFORMATION$6, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "licenseInformation" element
     */
    public gov.loc.premis.v3.LicenseInformationComplexType addNewLicenseInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LicenseInformationComplexType target = null;
            target = (gov.loc.premis.v3.LicenseInformationComplexType)get_store().add_element_user(LICENSEINFORMATION$6);
            return target;
        }
    }
    
    /**
     * Unsets the "licenseInformation" element
     */
    public void unsetLicenseInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LICENSEINFORMATION$6, 0);
        }
    }
    
    /**
     * Gets array of all "statuteInformation" elements
     */
    public gov.loc.premis.v3.StatuteInformationComplexType[] getStatuteInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(STATUTEINFORMATION$8, targetList);
            gov.loc.premis.v3.StatuteInformationComplexType[] result = new gov.loc.premis.v3.StatuteInformationComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "statuteInformation" element
     */
    public gov.loc.premis.v3.StatuteInformationComplexType getStatuteInformationArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteInformationComplexType target = null;
            target = (gov.loc.premis.v3.StatuteInformationComplexType)get_store().find_element_user(STATUTEINFORMATION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "statuteInformation" element
     */
    public int sizeOfStatuteInformationArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(STATUTEINFORMATION$8);
        }
    }
    
    /**
     * Sets array of all "statuteInformation" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setStatuteInformationArray(gov.loc.premis.v3.StatuteInformationComplexType[] statuteInformationArray)
    {
        check_orphaned();
        arraySetterHelper(statuteInformationArray, STATUTEINFORMATION$8);
    }
    
    /**
     * Sets ith "statuteInformation" element
     */
    public void setStatuteInformationArray(int i, gov.loc.premis.v3.StatuteInformationComplexType statuteInformation)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteInformationComplexType target = null;
            target = (gov.loc.premis.v3.StatuteInformationComplexType)get_store().find_element_user(STATUTEINFORMATION$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(statuteInformation);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "statuteInformation" element
     */
    public gov.loc.premis.v3.StatuteInformationComplexType insertNewStatuteInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteInformationComplexType target = null;
            target = (gov.loc.premis.v3.StatuteInformationComplexType)get_store().insert_element_user(STATUTEINFORMATION$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "statuteInformation" element
     */
    public gov.loc.premis.v3.StatuteInformationComplexType addNewStatuteInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StatuteInformationComplexType target = null;
            target = (gov.loc.premis.v3.StatuteInformationComplexType)get_store().add_element_user(STATUTEINFORMATION$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "statuteInformation" element
     */
    public void removeStatuteInformation(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(STATUTEINFORMATION$8, i);
        }
    }
    
    /**
     * Gets the "otherRightsInformation" element
     */
    public gov.loc.premis.v3.OtherRightsInformationComplexType getOtherRightsInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsInformationComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsInformationComplexType)get_store().find_element_user(OTHERRIGHTSINFORMATION$10, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "otherRightsInformation" element
     */
    public boolean isSetOtherRightsInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OTHERRIGHTSINFORMATION$10) != 0;
        }
    }
    
    /**
     * Sets the "otherRightsInformation" element
     */
    public void setOtherRightsInformation(gov.loc.premis.v3.OtherRightsInformationComplexType otherRightsInformation)
    {
        generatedSetterHelperImpl(otherRightsInformation, OTHERRIGHTSINFORMATION$10, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "otherRightsInformation" element
     */
    public gov.loc.premis.v3.OtherRightsInformationComplexType addNewOtherRightsInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.OtherRightsInformationComplexType target = null;
            target = (gov.loc.premis.v3.OtherRightsInformationComplexType)get_store().add_element_user(OTHERRIGHTSINFORMATION$10);
            return target;
        }
    }
    
    /**
     * Unsets the "otherRightsInformation" element
     */
    public void unsetOtherRightsInformation()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OTHERRIGHTSINFORMATION$10, 0);
        }
    }
    
    /**
     * Gets array of all "rightsGranted" elements
     */
    public gov.loc.premis.v3.RightsGrantedComplexType[] getRightsGrantedArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RIGHTSGRANTED$12, targetList);
            gov.loc.premis.v3.RightsGrantedComplexType[] result = new gov.loc.premis.v3.RightsGrantedComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "rightsGranted" element
     */
    public gov.loc.premis.v3.RightsGrantedComplexType getRightsGrantedArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsGrantedComplexType target = null;
            target = (gov.loc.premis.v3.RightsGrantedComplexType)get_store().find_element_user(RIGHTSGRANTED$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "rightsGranted" element
     */
    public int sizeOfRightsGrantedArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RIGHTSGRANTED$12);
        }
    }
    
    /**
     * Sets array of all "rightsGranted" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRightsGrantedArray(gov.loc.premis.v3.RightsGrantedComplexType[] rightsGrantedArray)
    {
        check_orphaned();
        arraySetterHelper(rightsGrantedArray, RIGHTSGRANTED$12);
    }
    
    /**
     * Sets ith "rightsGranted" element
     */
    public void setRightsGrantedArray(int i, gov.loc.premis.v3.RightsGrantedComplexType rightsGranted)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsGrantedComplexType target = null;
            target = (gov.loc.premis.v3.RightsGrantedComplexType)get_store().find_element_user(RIGHTSGRANTED$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(rightsGranted);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rightsGranted" element
     */
    public gov.loc.premis.v3.RightsGrantedComplexType insertNewRightsGranted(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsGrantedComplexType target = null;
            target = (gov.loc.premis.v3.RightsGrantedComplexType)get_store().insert_element_user(RIGHTSGRANTED$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rightsGranted" element
     */
    public gov.loc.premis.v3.RightsGrantedComplexType addNewRightsGranted()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsGrantedComplexType target = null;
            target = (gov.loc.premis.v3.RightsGrantedComplexType)get_store().add_element_user(RIGHTSGRANTED$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "rightsGranted" element
     */
    public void removeRightsGranted(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RIGHTSGRANTED$12, i);
        }
    }
    
    /**
     * Gets array of all "linkingObjectIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] getLinkingObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGOBJECTIDENTIFIER$14, targetList);
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingObjectIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType getLinkingObjectIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().find_element_user(LINKINGOBJECTIDENTIFIER$14, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingObjectIdentifier" element
     */
    public int sizeOfLinkingObjectIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGOBJECTIDENTIFIER$14);
        }
    }
    
    /**
     * Sets array of all "linkingObjectIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingObjectIdentifierArray(gov.loc.premis.v3.LinkingObjectIdentifierComplexType[] linkingObjectIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingObjectIdentifierArray, LINKINGOBJECTIDENTIFIER$14);
    }
    
    /**
     * Sets ith "linkingObjectIdentifier" element
     */
    public void setLinkingObjectIdentifierArray(int i, gov.loc.premis.v3.LinkingObjectIdentifierComplexType linkingObjectIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().find_element_user(LINKINGOBJECTIDENTIFIER$14, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingObjectIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType insertNewLinkingObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().insert_element_user(LINKINGOBJECTIDENTIFIER$14, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingObjectIdentifier" element
     */
    public gov.loc.premis.v3.LinkingObjectIdentifierComplexType addNewLinkingObjectIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingObjectIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingObjectIdentifierComplexType)get_store().add_element_user(LINKINGOBJECTIDENTIFIER$14);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingObjectIdentifier" element
     */
    public void removeLinkingObjectIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGOBJECTIDENTIFIER$14, i);
        }
    }
    
    /**
     * Gets array of all "linkingAgentIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] getLinkingAgentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGAGENTIDENTIFIER$16, targetList);
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingAgentIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType getLinkingAgentIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().find_element_user(LINKINGAGENTIDENTIFIER$16, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingAgentIdentifier" element
     */
    public int sizeOfLinkingAgentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGAGENTIDENTIFIER$16);
        }
    }
    
    /**
     * Sets array of all "linkingAgentIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingAgentIdentifierArray(gov.loc.premis.v3.LinkingAgentIdentifierComplexType[] linkingAgentIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingAgentIdentifierArray, LINKINGAGENTIDENTIFIER$16);
    }
    
    /**
     * Sets ith "linkingAgentIdentifier" element
     */
    public void setLinkingAgentIdentifierArray(int i, gov.loc.premis.v3.LinkingAgentIdentifierComplexType linkingAgentIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().find_element_user(LINKINGAGENTIDENTIFIER$16, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingAgentIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType insertNewLinkingAgentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().insert_element_user(LINKINGAGENTIDENTIFIER$16, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingAgentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingAgentIdentifierComplexType addNewLinkingAgentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingAgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingAgentIdentifierComplexType)get_store().add_element_user(LINKINGAGENTIDENTIFIER$16);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingAgentIdentifier" element
     */
    public void removeLinkingAgentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGAGENTIDENTIFIER$16, i);
        }
    }
}
