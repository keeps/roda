/*
 * XML Type:  premisComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.PremisComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML premisComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class PremisComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.PremisComplexType
{
    private static final long serialVersionUID = 1L;
    
    public PremisComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName OBJECT$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "object");
    private static final javax.xml.namespace.QName EVENT$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "event");
    private static final javax.xml.namespace.QName AGENT$4 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agent");
    private static final javax.xml.namespace.QName RIGHTS$6 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "rights");
    private static final javax.xml.namespace.QName VERSION$8 = 
        new javax.xml.namespace.QName("", "version");
    
    
    /**
     * Gets array of all "object" elements
     */
    public gov.loc.premis.v3.ObjectComplexType[] getObjectArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(OBJECT$0, targetList);
            gov.loc.premis.v3.ObjectComplexType[] result = new gov.loc.premis.v3.ObjectComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "object" element
     */
    public gov.loc.premis.v3.ObjectComplexType getObjectArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectComplexType target = null;
            target = (gov.loc.premis.v3.ObjectComplexType)get_store().find_element_user(OBJECT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "object" element
     */
    public int sizeOfObjectArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(OBJECT$0);
        }
    }
    
    /**
     * Sets array of all "object" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setObjectArray(gov.loc.premis.v3.ObjectComplexType[] objectArray)
    {
        check_orphaned();
        arraySetterHelper(objectArray, OBJECT$0);
    }
    
    /**
     * Sets ith "object" element
     */
    public void setObjectArray(int i, gov.loc.premis.v3.ObjectComplexType object)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectComplexType target = null;
            target = (gov.loc.premis.v3.ObjectComplexType)get_store().find_element_user(OBJECT$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(object);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "object" element
     */
    public gov.loc.premis.v3.ObjectComplexType insertNewObject(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectComplexType target = null;
            target = (gov.loc.premis.v3.ObjectComplexType)get_store().insert_element_user(OBJECT$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "object" element
     */
    public gov.loc.premis.v3.ObjectComplexType addNewObject()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ObjectComplexType target = null;
            target = (gov.loc.premis.v3.ObjectComplexType)get_store().add_element_user(OBJECT$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "object" element
     */
    public void removeObject(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(OBJECT$0, i);
        }
    }
    
    /**
     * Gets array of all "event" elements
     */
    public gov.loc.premis.v3.EventComplexType[] getEventArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EVENT$2, targetList);
            gov.loc.premis.v3.EventComplexType[] result = new gov.loc.premis.v3.EventComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "event" element
     */
    public gov.loc.premis.v3.EventComplexType getEventArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventComplexType target = null;
            target = (gov.loc.premis.v3.EventComplexType)get_store().find_element_user(EVENT$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "event" element
     */
    public int sizeOfEventArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENT$2);
        }
    }
    
    /**
     * Sets array of all "event" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setEventArray(gov.loc.premis.v3.EventComplexType[] eventArray)
    {
        check_orphaned();
        arraySetterHelper(eventArray, EVENT$2);
    }
    
    /**
     * Sets ith "event" element
     */
    public void setEventArray(int i, gov.loc.premis.v3.EventComplexType event)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventComplexType target = null;
            target = (gov.loc.premis.v3.EventComplexType)get_store().find_element_user(EVENT$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(event);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "event" element
     */
    public gov.loc.premis.v3.EventComplexType insertNewEvent(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventComplexType target = null;
            target = (gov.loc.premis.v3.EventComplexType)get_store().insert_element_user(EVENT$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "event" element
     */
    public gov.loc.premis.v3.EventComplexType addNewEvent()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventComplexType target = null;
            target = (gov.loc.premis.v3.EventComplexType)get_store().add_element_user(EVENT$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "event" element
     */
    public void removeEvent(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENT$2, i);
        }
    }
    
    /**
     * Gets array of all "agent" elements
     */
    public gov.loc.premis.v3.AgentComplexType[] getAgentArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(AGENT$4, targetList);
            gov.loc.premis.v3.AgentComplexType[] result = new gov.loc.premis.v3.AgentComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "agent" element
     */
    public gov.loc.premis.v3.AgentComplexType getAgentArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentComplexType target = null;
            target = (gov.loc.premis.v3.AgentComplexType)get_store().find_element_user(AGENT$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "agent" element
     */
    public int sizeOfAgentArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(AGENT$4);
        }
    }
    
    /**
     * Sets array of all "agent" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setAgentArray(gov.loc.premis.v3.AgentComplexType[] agentArray)
    {
        check_orphaned();
        arraySetterHelper(agentArray, AGENT$4);
    }
    
    /**
     * Sets ith "agent" element
     */
    public void setAgentArray(int i, gov.loc.premis.v3.AgentComplexType agent)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentComplexType target = null;
            target = (gov.loc.premis.v3.AgentComplexType)get_store().find_element_user(AGENT$4, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(agent);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "agent" element
     */
    public gov.loc.premis.v3.AgentComplexType insertNewAgent(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentComplexType target = null;
            target = (gov.loc.premis.v3.AgentComplexType)get_store().insert_element_user(AGENT$4, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "agent" element
     */
    public gov.loc.premis.v3.AgentComplexType addNewAgent()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.AgentComplexType target = null;
            target = (gov.loc.premis.v3.AgentComplexType)get_store().add_element_user(AGENT$4);
            return target;
        }
    }
    
    /**
     * Removes the ith "agent" element
     */
    public void removeAgent(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(AGENT$4, i);
        }
    }
    
    /**
     * Gets array of all "rights" elements
     */
    public gov.loc.premis.v3.RightsComplexType[] getRightsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(RIGHTS$6, targetList);
            gov.loc.premis.v3.RightsComplexType[] result = new gov.loc.premis.v3.RightsComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "rights" element
     */
    public gov.loc.premis.v3.RightsComplexType getRightsArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsComplexType target = null;
            target = (gov.loc.premis.v3.RightsComplexType)get_store().find_element_user(RIGHTS$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "rights" element
     */
    public int sizeOfRightsArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(RIGHTS$6);
        }
    }
    
    /**
     * Sets array of all "rights" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setRightsArray(gov.loc.premis.v3.RightsComplexType[] rightsArray)
    {
        check_orphaned();
        arraySetterHelper(rightsArray, RIGHTS$6);
    }
    
    /**
     * Sets ith "rights" element
     */
    public void setRightsArray(int i, gov.loc.premis.v3.RightsComplexType rights)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsComplexType target = null;
            target = (gov.loc.premis.v3.RightsComplexType)get_store().find_element_user(RIGHTS$6, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(rights);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "rights" element
     */
    public gov.loc.premis.v3.RightsComplexType insertNewRights(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsComplexType target = null;
            target = (gov.loc.premis.v3.RightsComplexType)get_store().insert_element_user(RIGHTS$6, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "rights" element
     */
    public gov.loc.premis.v3.RightsComplexType addNewRights()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.RightsComplexType target = null;
            target = (gov.loc.premis.v3.RightsComplexType)get_store().add_element_user(RIGHTS$6);
            return target;
        }
    }
    
    /**
     * Removes the ith "rights" element
     */
    public void removeRights(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(RIGHTS$6, i);
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$8);
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
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$8);
            return target;
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
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_attribute_user(VERSION$8);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_attribute_user(VERSION$8);
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
            target = (gov.loc.premis.v3.Version3)get_store().find_attribute_user(VERSION$8);
            if (target == null)
            {
                target = (gov.loc.premis.v3.Version3)get_store().add_attribute_user(VERSION$8);
            }
            target.set(version);
        }
    }
}
