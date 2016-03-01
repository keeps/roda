/*
 * XML Type:  eventOutcomeDetailComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventOutcomeDetailComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML eventOutcomeDetailComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class EventOutcomeDetailComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventOutcomeDetailComplexType
{
    private static final long serialVersionUID = 1L;
    
    public EventOutcomeDetailComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTOUTCOMEDETAILNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeDetailNote");
    private static final javax.xml.namespace.QName EVENTOUTCOMEDETAILEXTENSION$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeDetailExtension");
    
    
    /**
     * Gets the "eventOutcomeDetailNote" element
     */
    public java.lang.String getEventOutcomeDetailNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "eventOutcomeDetailNote" element
     */
    public org.apache.xmlbeans.XmlString xgetEventOutcomeDetailNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * True if has "eventOutcomeDetailNote" element
     */
    public boolean isSetEventOutcomeDetailNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTOUTCOMEDETAILNOTE$0) != 0;
        }
    }
    
    /**
     * Sets the "eventOutcomeDetailNote" element
     */
    public void setEventOutcomeDetailNote(java.lang.String eventOutcomeDetailNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(EVENTOUTCOMEDETAILNOTE$0);
            }
            target.setStringValue(eventOutcomeDetailNote);
        }
    }
    
    /**
     * Sets (as xml) the "eventOutcomeDetailNote" element
     */
    public void xsetEventOutcomeDetailNote(org.apache.xmlbeans.XmlString eventOutcomeDetailNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(EVENTOUTCOMEDETAILNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(EVENTOUTCOMEDETAILNOTE$0);
            }
            target.set(eventOutcomeDetailNote);
        }
    }
    
    /**
     * Unsets the "eventOutcomeDetailNote" element
     */
    public void unsetEventOutcomeDetailNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTOUTCOMEDETAILNOTE$0, 0);
        }
    }
    
    /**
     * Gets array of all "eventOutcomeDetailExtension" elements
     */
    public gov.loc.premis.v3.ExtensionComplexType[] getEventOutcomeDetailExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EVENTOUTCOMEDETAILEXTENSION$2, targetList);
            gov.loc.premis.v3.ExtensionComplexType[] result = new gov.loc.premis.v3.ExtensionComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "eventOutcomeDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType getEventOutcomeDetailExtensionArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(EVENTOUTCOMEDETAILEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "eventOutcomeDetailExtension" element
     */
    public int sizeOfEventOutcomeDetailExtensionArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTOUTCOMEDETAILEXTENSION$2);
        }
    }
    
    /**
     * Sets array of all "eventOutcomeDetailExtension" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setEventOutcomeDetailExtensionArray(gov.loc.premis.v3.ExtensionComplexType[] eventOutcomeDetailExtensionArray)
    {
        check_orphaned();
        arraySetterHelper(eventOutcomeDetailExtensionArray, EVENTOUTCOMEDETAILEXTENSION$2);
    }
    
    /**
     * Sets ith "eventOutcomeDetailExtension" element
     */
    public void setEventOutcomeDetailExtensionArray(int i, gov.loc.premis.v3.ExtensionComplexType eventOutcomeDetailExtension)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().find_element_user(EVENTOUTCOMEDETAILEXTENSION$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(eventOutcomeDetailExtension);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "eventOutcomeDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType insertNewEventOutcomeDetailExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().insert_element_user(EVENTOUTCOMEDETAILEXTENSION$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "eventOutcomeDetailExtension" element
     */
    public gov.loc.premis.v3.ExtensionComplexType addNewEventOutcomeDetailExtension()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.ExtensionComplexType target = null;
            target = (gov.loc.premis.v3.ExtensionComplexType)get_store().add_element_user(EVENTOUTCOMEDETAILEXTENSION$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "eventOutcomeDetailExtension" element
     */
    public void removeEventOutcomeDetailExtension(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTOUTCOMEDETAILEXTENSION$2, i);
        }
    }
}
