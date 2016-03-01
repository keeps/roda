/*
 * An XML document type.
 * Localname: eventOutcomeDetail
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EventOutcomeDetailDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one eventOutcomeDetail(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EventOutcomeDetailDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EventOutcomeDetailDocument
{
    private static final long serialVersionUID = 1L;
    
    public EventOutcomeDetailDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName EVENTOUTCOMEDETAIL$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "eventOutcomeDetail");
    
    
    /**
     * Gets the "eventOutcomeDetail" element
     */
    public gov.loc.premis.v3.EventOutcomeDetailComplexType getEventOutcomeDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeDetailComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeDetailComplexType)get_store().find_element_user(EVENTOUTCOMEDETAIL$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "eventOutcomeDetail" element
     */
    public void setEventOutcomeDetail(gov.loc.premis.v3.EventOutcomeDetailComplexType eventOutcomeDetail)
    {
        generatedSetterHelperImpl(eventOutcomeDetail, EVENTOUTCOMEDETAIL$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "eventOutcomeDetail" element
     */
    public gov.loc.premis.v3.EventOutcomeDetailComplexType addNewEventOutcomeDetail()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EventOutcomeDetailComplexType target = null;
            target = (gov.loc.premis.v3.EventOutcomeDetailComplexType)get_store().add_element_user(EVENTOUTCOMEDETAIL$0);
            return target;
        }
    }
}
