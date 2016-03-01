/*
 * An XML document type.
 * Localname: environmentFunction
 * Namespace: http://www.loc.gov/premis/v3
 * Java type: gov.loc.premis.v3.EnvironmentFunctionDocument
 *
 * Automatically generated - do not modify.
 */
package gov.loc.premis.v3.impl;
/**
 * A document containing one environmentFunction(@http://www.loc.gov/premis/v3) element.
 *
 * This is a complex type.
 */
public class EnvironmentFunctionDocumentImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements gov.loc.premis.v3.EnvironmentFunctionDocument
{
    private static final long serialVersionUID = 1L;
    
    public EnvironmentFunctionDocumentImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName ENVIRONMENTFUNCTION$0 = 
        new javax.xml.namespace.QName("http://www.loc.gov/premis/v3", "environmentFunction");
    
    
    /**
     * Gets the "environmentFunction" element
     */
    public gov.loc.premis.v3.EnvironmentFunctionComplexType getEnvironmentFunction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EnvironmentFunctionComplexType target = null;
            target = (gov.loc.premis.v3.EnvironmentFunctionComplexType)get_store().find_element_user(ENVIRONMENTFUNCTION$0, 0);
            if (target == null)
            {
                return null;
            }
            return target;
        }
    }
    
    /**
     * Sets the "environmentFunction" element
     */
    public void setEnvironmentFunction(gov.loc.premis.v3.EnvironmentFunctionComplexType environmentFunction)
    {
        generatedSetterHelperImpl(environmentFunction, ENVIRONMENTFUNCTION$0, 0, org.apache.xmlbeans.impl.values.XmlObjectBase.KIND_SETTERHELPER_SINGLETON);
    }
    
    /**
     * Appends and returns a new empty "environmentFunction" element
     */
    public gov.loc.premis.v3.EnvironmentFunctionComplexType addNewEnvironmentFunction()
    {
        synchronized (monitor())
        {
            check_orphaned();
            gov.loc.premis.v3.EnvironmentFunctionComplexType target = null;
            target = (gov.loc.premis.v3.EnvironmentFunctionComplexType)get_store().add_element_user(ENVIRONMENTFUNCTION$0);
            return target;
        }
    }
}
