/*
 * XML Type:  eventOutcomeInformationComplexType
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventOutcomeInformationComplexType
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * An XML eventOutcomeInformationComplexType(@http://www.loc.gov/premis/v3).
 *
 * This is a complex type.
 */
public class EventOutcomeInformationComplexTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventOutcomeInformationComplexType
{
    private static final long serialVersionUID = 1L;
    
    public EventOutcomeInformationComplexTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTOUTCOME$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcome");
    private static final javax.xml.namespace.QName EVENTOUTCOMEDETAIL$2 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeDetail");
    
    
    /**
     * Gets the "eventOutcome" element
     */
    public gov.loc.premis.v3.StringPlusAuthority getEventOutcome()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().find_element_user(EVENTOUTCOME$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * True if has "eventOutcome" element
     */
    public boolean isSetEventOutcome()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTOUTCOME$0) != 0;
        }
    }
    
    /**
     * Sets the "eventOutcome" element
     */
    public void setEventOutcome(gov.loc.premis.v3.StringPlusAuthority eventOutcome)
    {
        generatedSetterHelperImpl(eventOutcome, EVENTOUTCOME$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventOutcome" element
     */
    public gov.loc.premis.v3.StringPlusAuthority addNewEventOutcome()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.StringPlusAuthority target = null;
            target = (gov.loc.premis.v3.StringPlusAuthority)get_store().add_element_user(EVENTOUTCOME$0);
            return target;
        }
    }
    
    /**
     * Unsets the "eventOutcome" element
     */
    public void unsetEventOutcome()
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTOUTCOME$0, 0);
        }
    }
    
    /**
     * Gets array of all "eventOutcomeDetail" elements
     */
    public gov.loc.premis.v3.EventOutcomeDetailComplexType[] getEventOutcomeDetailArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(EVENTOUTCOMEDETAIL$2, targetList);
            gov.loc.premis.v3.EventOutcomeDetailComplexType[] result = new gov.loc.premis.v3.EventOutcomeDetailComplexType[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "eventOutcomeDetail" element
     */
    public gov.loc.premis.v3.EventOutcomeDetailComplexType getEventOutcomeDetailArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeDetailComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeDetailComplexType)get_store().find_element_user(EVENTOUTCOMEDETAIL$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "eventOutcomeDetail" element
     */
    public int sizeOfEventOutcomeDetailArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(EVENTOUTCOMEDETAIL$2);
        }
    }
    
    /**
     * Sets array of all "eventOutcomeDetail" element  WARNING: This method is not atomicaly synchronized.
     */
    public void setEventOutcomeDetailArray(gov.loc.premis.v3.EventOutcomeDetailComplexType[] eventOutcomeDetailArray)
    {
        check_orphaned();
        arraySetterHelper(eventOutcomeDetailArray, EVENTOUTCOMEDETAIL$2);
    }
    
    /**
     * Sets ith "eventOutcomeDetail" element
     */
    public void setEventOutcomeDetailArray(int i, gov.loc.premis.v3.EventOutcomeDetailComplexType eventOutcomeDetail)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeDetailComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeDetailComplexType)get_store().find_element_user(EVENTOUTCOMEDETAIL$2, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(eventOutcomeDetail);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "eventOutcomeDetail" element
     */
    public gov.loc.premis.v3.EventOutcomeDetailComplexType insertNewEventOutcomeDetail(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeDetailComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeDetailComplexType)get_store().insert_element_user(EVENTOUTCOMEDETAIL$2, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "eventOutcomeDetail" element
     */
    public gov.loc.premis.v3.EventOutcomeDetailComplexType addNewEventOutcomeDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeDetailComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeDetailComplexType)get_store().add_element_user(EVENTOUTCOMEDETAIL$2);
            return target;
        }
    }
    
    /**
     * Removes the ith "eventOutcomeDetail" element
     */
    public void removeEventOutcomeDetail(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(EVENTOUTCOMEDETAIL$2, i);
        }
    }
}
