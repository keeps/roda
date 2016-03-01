/*
 * XML Type:  agentComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML agentComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class AgentComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentComplexType
{
    private static final long serialVersionUID = 1L;
    
    public AgentComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTIDENTIFIER$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentIdentifier");
    private static final javax.xml.namespace.QName AGENTNAME$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentName");
    private static final javax.xml.namespace.QName AGENTTYPE$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentType");
    private static final javax.xml.namespace.QName AGENTVERSION$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentVersion");
    private static final javax.xml.namespace.QName AGENTNOTE$8 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentNote");
    private static final javax.xml.namespace.QName AGENTEXTENSION$10 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentExtension");
    private static final javax.xml.namespace.QName LINKINGEVENTIDENTIFIER$12 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEventIdentifier");
    private static final javax.xml.namespace.QName LINKINGRIGHTSSTATEMENTIDENTIFIER$14 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingRightsStatementIdentifier");
    private static final javax.xml.namespace.QName LINKINGENVIRONMENTIDENTIFIER$16 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "linkingEnvironmentIdentifier");
    private static final javax.xml.namespace.QName XMLID$18 = 
        new javax.xml.namespace.QName("", "xmlID");
    private static final javax.xml.namespace.QName VERSION$20 = 
        new javax.xml.namespace.QName("", "version");
    
    
    /**
     * Gets array of all "agentIdentifier" elements
     */
    public gov.loc.premis.v3.AgentIdentifierComplexType[] getAgentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AGENTIDENTIFIER$0, targetList);
            gov.loc.premis.v3.AgentIdentifierComplexType[] result = new gov.loc.premis.v3.AgentIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "agentIdentifier" element
     */
    public gov.loc.premis.v3.AgentIdentifierComplexType getAgentIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.AgentIdentifierComplexType)get_store().find_element_user(AGENTIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "agentIdentifier" element
     */
    public int sizeOfAgentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AGENTIDENTIFIER$0);
        }
    }
    
    /**
     * Sets array of all "agentIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setAgentIdentifierArray(gov.loc.premis.v3.AgentIdentifierComplexType[] agentIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(agentIdentifierArray, AGENTIDENTIFIER$0);
    }
    
    /**
     * Sets ith "agentIdentifier" element
     */
    public void setAgentIdentifierArray(int i, gov.loc.premis.v3.AgentIdentifierComplexType agentIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.AgentIdentifierComplexType)get_store().find_element_user(AGENTIDENTIFIER$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(agentIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentIdentifier" element
     */
    public gov.loc.premis.v3.AgentIdentifierComplexType insertNewAgentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.AgentIdentifierComplexType)get_store().insert_element_user(AGENTIDENTIFIER$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentIdentifier" element
     */
    public gov.loc.premis.v3.AgentIdentifierComplexType addNewAgentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.AgentIdentifierComplexType)get_store().add_element_user(AGENTIDENTIFIER$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "agentIdentifier" element
     */
    public void removeAgentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AGENTIDENTIFIER$0, i);
        }
    }
    
    /**
     * Gets array of all "agentName" elements
     */
    public gov.loc.premis.v3.StringPlusAuthority[] getAgentNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AGENTNAME$2, targetList);
            gov.loc.premis.v3.StringPlusAuthority[] result = new gov.loc.premis.v3.StringPlusAuthority[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "agentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getAgentNameArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(AGENTNAME$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "agentName" element
     */
    public int sizeOfAgentNameArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AGENTNAME$2);
        }
    }
    
    /**
     * Sets array of all "agentName" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setAgentNameArray(gov.loc.premis.v3.StringPlusAuthority[] agentNameArray)
    {
        check_orphaned();
        arraySetterHelper(agentNameArray, AGENTNAME$2);
    }
    
    /**
     * Sets ith "agentName" element
     */
    public void setAgentNameArray(int i, gov.loc.premis.v3.StringPlusAuthority agentName)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(AGENTNAME$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(agentName);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority insertNewAgentName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().insert_element_user(AGENTNAME$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentName" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewAgentName()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(AGENTNAME$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "agentName" element
     */
    public void removeAgentName(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AGENTNAME$2, i);
        }
    }
    
    /**
     * Gets the "agentType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getAgentType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(AGENTTYPE$4, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "agentType" element
     */
    public boolean isSetAgentType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AGENTTYPE$4) != 0;
        }
    }
    
    /**
     * Sets the "agentType" element
     */
    public void setAgentType(gov.loc.premis.v3.StringPlusAuthority agentType)
    {
        generatedSetterHelperImpl(agentType, AGENTTYPE$4, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "agentType" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewAgentType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(AGENTTYPE$4);
            return target;
        }
    }
    
    /**
     * Unsets the "agentType" element
     */
    public void unsetAgentType()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AGENTTYPE$4, 0);
        }
    }
    
    /**
     * Gets the "agentVersion" element
     */
    public java.lang.String getAgentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTVERSION$6, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "agentVersion" element
     */
    public org.apache.xmlbeans.XmlString xgetAgentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTVERSION$6, 0);
            return target;
        }
    }
    
    /**
     * True if has "agentVersion" element
     */
    public boolean isSetAgentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AGENTVERSION$6) != 0;
        }
    }
    
    /**
     * Sets the "agentVersion" element
     */
    public void setAgentVersion(java.lang.String agentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTVERSION$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AGENTVERSION$6);
            }
            target.setStringValue(agentVersion);
        }
    }
    
    /**
     * Sets (as xml) the "agentVersion" element
     */
    public void xsetAgentVersion(org.apache.xmlbeans.XmlString agentVersion)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTVERSION$6, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AGENTVERSION$6);
            }
            target.set(agentVersion);
        }
    }
    
    /**
     * Unsets the "agentVersion" element
     */
    public void unsetAgentVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AGENTVERSION$6, 0);
        }
    }
    
    /**
     * Gets array of all "agentNote" elements
     */
    public java.lang.String[] getAgentNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AGENTNOTE$8, targetList);
            java.lang.String[] result = new java.lang.String[targetList.size()];
            for (int i = 0, len = targetList.size() ; i < len ; i++)
                result[i] = ((org.apache.xmlbeans.SimpleValue)targetList.get(i)).getStringValue();
            return result;
        }
    }
    
    /**
     * Gets ith "agentNote" element
     */
    public java.lang.String getAgentNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) array of all "agentNote" elements
     */
    public org.apache.xmlbeans.XmlString[] xgetAgentNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AGENTNOTE$8, targetList);
            org.apache.xmlbeans.XmlString[] result = new org.apache.xmlbeans.XmlString[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets (as xml) ith "agentNote" element
     */
    public org.apache.xmlbeans.XmlString xgetAgentNoteArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "agentNote" element
     */
    public int sizeOfAgentNoteArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AGENTNOTE$8);
        }
    }
    
    /**
     * Sets array of all "agentNote" element
     */
    public void setAgentNoteArray(java.lang.String[] agentNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(agentNoteArray, AGENTNOTE$8);
        }
    }
    
    /**
     * Sets ith "agentNote" element
     */
    public void setAgentNoteArray(int i, java.lang.String agentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.setStringValue(agentNote);
        }
    }
    
    /**
     * Sets (as xml) array of all "agentNote" element
     */
    public void xsetAgentNoteArray(org.apache.xmlbeans.XmlString[]agentNoteArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(agentNoteArray, AGENTNOTE$8);
        }
    }
    
    /**
     * Sets (as xml) ith "agentNote" element
     */
    public void xsetAgentNoteArray(int i, org.apache.xmlbeans.XmlString agentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTNOTE$8, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(agentNote);
        }
    }
    
    /**
     * Inserts the value as the ith "agentNote" element
     */
    public void insertAgentNote(int i, java.lang.String agentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = 
                (org.apache.xmlbeans.SimpleValue)get_store().insert_element_user(AGENTNOTE$8, i);
            target.setStringValue(agentNote);
        }
    }
    
    /**
     * Appends the value as the last "agentNote" element
     */
    public void addAgentNote(java.lang.String agentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AGENTNOTE$8);
            target.setStringValue(agentNote);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentNote" element
     */
    public org.apache.xmlbeans.XmlString insertNewAgentNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().insert_element_user(AGENTNOTE$8, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentNote" element
     */
    public org.apache.xmlbeans.XmlString addNewAgentNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AGENTNOTE$8);
            return target;
        }
    }
    
    /**
     * Removes the ith "agentNote" element
     */
    public void removeAgentNote(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AGENTNOTE$8, i);
        }
    }
    
    /**
     * Gets array of all "agentExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getAgentExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AGENTEXTENSION$10, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "agentExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getAgentExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(AGENTEXTENSION$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "agentExtension" element
     */
    public int sizeOfAgentExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AGENTEXTENSION$10);
        }
    }
    
    /**
     * Sets array of all "agentExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setAgentExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] agentExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(agentExtensionArray, AGENTEXTENSION$10);
    }
    
    /**
     * Sets ith "agentExtension" element
     */
    public void setAgentExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType agentExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(AGENTEXTENSION$10, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(agentExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agentExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewAgentExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(AGENTEXTENSION$10, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agentExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewAgentExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(AGENTEXTENSION$10);
            return target;
        }
    }
    
    /**
     * Removes the ith "agentExtension" element
     */
    public void removeAgentExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AGENTEXTENSION$10, i);
        }
    }
    
    /**
     * Gets array of all "linkingEventIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType[] getLinkingEventIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGEVENTIDENTIFIER$12, targetList);
            gov.loc.premis.v3.LinkingEventIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingEventIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType getLinkingEventIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().find_element_user(LINKINGEVENTIDENTIFIER$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingEventIdentifier" element
     */
    public int sizeOfLinkingEventIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGEVENTIDENTIFIER$12);
        }
    }
    
    /**
     * Sets array of all "linkingEventIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingEventIdentifierArray(gov.loc.premis.v3.LinkingEventIdentifierComplexType[] linkingEventIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingEventIdentifierArray, LINKINGEVENTIDENTIFIER$12);
    }
    
    /**
     * Sets ith "linkingEventIdentifier" element
     */
    public void setLinkingEventIdentifierArray(int i, gov.loc.premis.v3.LinkingEventIdentifierComplexType linkingEventIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().find_element_user(LINKINGEVENTIDENTIFIER$12, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingEventIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType insertNewLinkingEventIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().insert_element_user(LINKINGEVENTIDENTIFIER$12, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingEventIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEventIdentifierComplexType addNewLinkingEventIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEventIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEventIdentifierComplexType)get_store().add_element_user(LINKINGEVENTIDENTIFIER$12);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingEventIdentifier" element
     */
    public void removeLinkingEventIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGEVENTIDENTIFIER$12, i);
        }
    }
    
    /**
     * Gets array of all "linkingRightsStatementIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] getLinkingRightsStatementIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGRIGHTSSTATEMENTIDENTIFIER$14, targetList);
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType getLinkingRightsStatementIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$14, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingRightsStatementIdentifier" element
     */
    public int sizeOfLinkingRightsStatementIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGRIGHTSSTATEMENTIDENTIFIER$14);
        }
    }
    
    /**
     * Sets array of all "linkingRightsStatementIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingRightsStatementIdentifierArray(gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType[] linkingRightsStatementIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingRightsStatementIdentifierArray, LINKINGRIGHTSSTATEMENTIDENTIFIER$14);
    }
    
    /**
     * Sets ith "linkingRightsStatementIdentifier" element
     */
    public void setLinkingRightsStatementIdentifierArray(int i, gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType linkingRightsStatementIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().find_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$14, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingRightsStatementIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType insertNewLinkingRightsStatementIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().insert_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$14, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingRightsStatementIdentifier" element
     */
    public gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType addNewLinkingRightsStatementIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingRightsStatementIdentifierComplexType)get_store().add_element_user(LINKINGRIGHTSSTATEMENTIDENTIFIER$14);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingRightsStatementIdentifier" element
     */
    public void removeLinkingRightsStatementIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGRIGHTSSTATEMENTIDENTIFIER$14, i);
        }
    }
    
    /**
     * Gets array of all "linkingEnvironmentIdentifier" elements
     */
    public gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType[] getLinkingEnvironmentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(LINKINGENVIRONMENTIDENTIFIER$16, targetList);
            gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType[] result = new gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "linkingEnvironmentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType getLinkingEnvironmentIdentifierArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIER$16, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "linkingEnvironmentIdentifier" element
     */
    public int sizeOfLinkingEnvironmentIdentifierArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(LINKINGENVIRONMENTIDENTIFIER$16);
        }
    }
    
    /**
     * Sets array of all "linkingEnvironmentIdentifier" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setLinkingEnvironmentIdentifierArray(gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType[] linkingEnvironmentIdentifierArray)
    {
        check_orphaned();
        arraySetterHelper(linkingEnvironmentIdentifierArray, LINKINGENVIRONMENTIDENTIFIER$16);
    }
    
    /**
     * Sets ith "linkingEnvironmentIdentifier" element
     */
    public void setLinkingEnvironmentIdentifierArray(int i, gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType linkingEnvironmentIdentifier)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType)get_store().find_element_user(LINKINGENVIRONMENTIDENTIFIER$16, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(linkingEnvironmentIdentifier);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "linkingEnvironmentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType insertNewLinkingEnvironmentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType)get_store().insert_element_user(LINKINGENVIRONMENTIDENTIFIER$16, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "linkingEnvironmentIdentifier" element
     */
    public gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType addNewLinkingEnvironmentIdentifier()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType target = null;
            target = (gov.loc.premis.v3.LinkingEnvironmentIdentifierComplexType)get_store().add_element_user(LINKINGENVIRONMENTIDENTIFIER$16);
            return target;
        }
    }
    
    /**
     * Removes the ith "linkingEnvironmentIdentifier" element
     */
    public void removeLinkingEnvironmentIdentifier(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(LINKINGENVIRONMENTIDENTIFIER$16, i);
        }
    }
    
    /**
     * Gets the "xmlID" attribute
     */
    public java.lang.String getXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$18);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "xmlID" attribute
     */
    public org.apache.xmlbeans.XmlID xgetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$18);
            return target;
        }
    }
    
    /**
     * True if has "xmlID" attribute
     */
    public boolean isSetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(XMLID$18) != null;
        }
    }
    
    /**
     * Sets the "xmlID" attribute
     */
    public void setXmlID(java.lang.String xmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(XMLID$18);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(XMLID$18);
            }
            target.setStringValue(xmlID);
        }
    }
    
    /**
     * Sets (as xml) the "xmlID" attribute
     */
    public void xsetXmlID(org.apache.xmlbeans.XmlID xmlID)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlID target = null;
            target = (org.apache.xmlbeans.XmlID)get_store().find_attribute_user(XMLID$18);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlID)get_store().add_attribute_user(XMLID$18);
            }
            target.set(xmlID);
        }
    }
    
    /**
     * Unsets the "xmlID" attribute
     */
    public void unsetXmlID()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(XMLID$18);
        }
    }
    
    /**
     * Gets the "version" attribute
     */
    public gov.loc.premis.v3.Version3.Enum getVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$20);
            if (target == null)
            {
                return null;
            }
            return (gov.loc.premis.v3.Version3.Enum)target.getEnumValue();
        }
    }
    
    /**
     * Gets (as xml) the "version" attribute
     */
    public gov.loc.premis.v3.Version3 xgetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.Version3 target = null;
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$20);
            return target;
        }
    }
    
    /**
     * True if has "version" attribute
     */
    public boolean isSetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().find_attribute_user(VERSION$20) != null;
        }
    }
    
    /**
     * Sets the "version" attribute
     */
    public void setVersion(gov.loc.premis.v3.Version3.Enum version)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$20);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VERSION$20);
            }
            target.setEnumValue(version);
        }
    }
    
    /**
     * Sets (as xml) the "version" attribute
     */
    public void xsetVersion(gov.loc.premis.v3.Version3 version)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.Version3 target = null;
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$20);
            if (target == null)
            {
                target = (gov.loc.premis.v3.Version3)get_store().add_attribute_user(VERSION$20);
            }
            target.set(version);
        }
    }
    
    /**
     * Unsets the "version" attribute
     */
    public void unsetVersion()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_attribute(VERSION$20);
        }
    }
}
