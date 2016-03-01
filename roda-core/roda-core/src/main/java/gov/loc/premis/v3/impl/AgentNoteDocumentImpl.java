/*
 * An XML document type.
 * Localname: agentNote
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.AgentNoteDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one agentNote(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class AgentNoteDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.AgentNoteDocument
{
    private static final long serialVersionUID = 1L;
    
    public AgentNoteDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName AGENTNOTE$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "agentNote");
    
    
    /**
     * Gets the "agentNote" element
     */
    public java.lang.String getAgentNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTNOTE$0, 0);
            if (target == null)
            {
                return null;
            }
            return target.getStringValue();
        }
    }
    
    /**
     * Gets (as xml) the "agentNote" element
     */
    public org.apache.xmlbeans.XmlString xgetAgentNote()
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTNOTE$0, 0);
            return target;
        }
    }
    
    /**
     * Sets the "agentNote" element
     */
    public void setAgentNote(java.lang.String agentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.SimpleValue target = null;
            target = (org.apache.xmlbeans.SimpleValue)get_store().find_element_user(AGENTNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.SimpleValue)get_store().add_element_user(AGENTNOTE$0);
            }
            target.setStringValue(agentNote);
        }
    }
    
    /**
     * Sets (as xml) the "agentNote" element
     */
    public void xsetAgentNote(org.apache.xmlbeans.XmlString agentNote)
    {
        synchronized (monitor())
        {
            check_orphaned();
            org.apache.xmlbeans.XmlString target = null;
            target = (org.apache.xmlbeans.XmlString)get_store().find_element_user(AGENTNOTE$0, 0);
            if (target == null)
            {
                target = (org.apache.xmlbeans.XmlString)get_store().add_element_user(AGENTNOTE$0);
            }
            target.set(agentNote);
        }
    }
}
