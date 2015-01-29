package pt.keep.cas;

/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.SearchControls;

import org.apache.commons.lang.StringUtils;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao;
import org.jasig.services.persondir.support.CaseInsensitiveAttributeNamedPersonImpl;
import org.jasig.services.persondir.support.CaseInsensitiveNamedPersonImpl;
import org.jasig.services.persondir.support.QueryType;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.util.Assert;


public class RolesLdapPersonAttributeDao extends AbstractQueryPersonAttributeDao<LogicalFilterWrapper> implements InitializingBean {
    private static final Pattern QUERY_PLACEHOLDER = Pattern.compile("\\{0\\}");
    private final static AttributesMapper MAPPER = new AttributeMapAttributesMapper();

    /**
     * The LdapTemplate to use to execute queries on the DirContext
     */
    private LdapTemplate ldapTemplate = null;

    private String baseDN = "";
    private String groupsDN = "";
    private String rolesDN = "";
    
    private String queryTemplate = null;
    private ContextSource contextSource = null;
    private SearchControls searchControls = new SearchControls();
    private boolean setReturningAttributes = true;
    private QueryType queryType = QueryType.AND;
    
    
    public RolesLdapPersonAttributeDao() {
        this.searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        this.searchControls.setReturningObjFlag(false);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        final Map<String, Set<String>> resultAttributeMapping = this.getResultAttributeMapping();
        if (this.setReturningAttributes && resultAttributeMapping != null) {
            this.searchControls.setReturningAttributes(resultAttributeMapping.keySet().toArray(new String[resultAttributeMapping.size()]));
        }
        
        if (this.contextSource == null) {
            throw new BeanCreationException("contextSource must be set");
        }
    }

    
    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#appendAttributeToQuery(java.lang.Object, java.lang.String, java.util.List)
     */
    @Override
    protected LogicalFilterWrapper appendAttributeToQuery(LogicalFilterWrapper queryBuilder, String dataAttribute, List<Object> queryValues) {
    	if (queryBuilder == null) {
            queryBuilder = new LogicalFilterWrapper(this.queryType);
        }
        
        for (final Object queryValue : queryValues) {
            final String queryValueString = queryValue == null ? null : queryValue.toString();
            
            if (StringUtils.isNotBlank(queryValueString)) {
                final Filter filter;
                if (!queryValueString.contains("*")) {
                    filter = new EqualsFilter(dataAttribute, queryValueString);
                }
                else {
                    filter = new LikeFilter(dataAttribute, queryValueString);
                }
                
                queryBuilder.append(filter);
            }
        }
        
        return queryBuilder;
    }

    /* (non-Javadoc)
     * @see org.jasig.services.persondir.support.AbstractQueryPersonAttributeDao#getPeopleForQuery(java.lang.Object, java.lang.String)
     */
    @Override
    protected List<IPersonAttributes> getPeopleForQuery(LogicalFilterWrapper queryBuilder, String queryUserName) {
        final String generatedLdapQuery = queryBuilder.encode();
        
        //If no query is generated return null since the query cannot be run
        if (StringUtils.isBlank(generatedLdapQuery)) {
            return null;
        }
        
        //Insert the generated query into the template if it is configured
        final String ldapQuery;
        if (this.queryTemplate == null) {
            ldapQuery = generatedLdapQuery;
        }
        else {
            final Matcher queryMatcher = QUERY_PLACEHOLDER.matcher(this.queryTemplate);
            ldapQuery = queryMatcher.replaceAll(generatedLdapQuery);
        }
        
        
        
        
        //Execute the query
        @SuppressWarnings("unchecked")
        final List<Map<String, List<Object>>> queryResults = this.ldapTemplate.search(this.baseDN, ldapQuery, this.searchControls, MAPPER);
       
        String groupQuery = "(uniqueMember=uid\\="+queryUserName+"\\,ou\\=users\\,dc\\=keep\\,dc\\=pt)";
        final List<Map<String, List<Object>>> groupsResults = this.ldapTemplate.search(this.groupsDN, groupQuery, this.searchControls, MAPPER);

        
        List<String> directGroupsCN = new ArrayList<String>();
        for (final Map<String, List<Object>> groupResult : groupsResults) {
        	directGroupsCN.add((String) groupResult.get("cn").get(0));
        }
        
        List<String> parentGroupsCN = new ArrayList<String>();
        
        for(String directGroup : directGroupsCN){
        	boolean parents = true;
        	
        	while(parents){
        		String p = "(uniqueMember=cn\\="+directGroup+"\\,ou\\=groups\\,dc\\=keep\\,dc\\=pt)";
        		final List<Map<String, List<Object>>> parentGroupResults = this.ldapTemplate.search(this.groupsDN, p, this.searchControls, MAPPER);
        		if(parentGroupResults==null || parentGroupResults.size()==0){
        			parents=false;
        		}else{
        			for (final Map<String, List<Object>> parentGroupResult : parentGroupResults) {
        				parentGroupsCN.add((String) parentGroupResult.get("cn").get(0));
        				directGroup = (String) parentGroupResult.get("cn").get(0);
        	        }
        			
        		}
        	
        	}
        }
        directGroupsCN.addAll(parentGroupsCN);
        
        List<String> groupsRoleOccupant = new ArrayList<String>();
        for(String cn : directGroupsCN){
        	groupsRoleOccupant.add("cn\\="+cn+"\\,ou\\=groups\\,dc\\=keep\\,dc\\=pt");
        	//groupsRoleOccupant.addAll(getParentGroups(cn));
        }
        
        
        
        Set<String> directRoles = new HashSet<String>();
        Set<String> userRoles = new HashSet<String>();
        String rolesQuery = "(roleOccupant=uid\\="+queryUserName+"\\,ou\\=users\\,dc\\=keep\\,dc\\=pt)";
        final List<Map<String, List<Object>>> rolesResults = this.ldapTemplate.search(this.rolesDN, rolesQuery, this.searchControls, MAPPER);
        for (final Map<String, List<Object>> roleResult : rolesResults) {
        	directRoles.add((String) roleResult.get("cn").get(0));
        }
        
        for(String groupRoleOccupant : groupsRoleOccupant){
        	String rolesQueryGroups = "(roleOccupant="+groupRoleOccupant+")";
        	final List<Map<String, List<Object>>> rolesResultsGroup = this.ldapTemplate.search(this.rolesDN, rolesQueryGroups, this.searchControls, MAPPER);
        	for (final Map<String, List<Object>> roleResult : rolesResultsGroup) {
        		userRoles.add((String) roleResult.get("cn").get(0));
        	}
        }
        
        if(directRoles!=null && directRoles.size()>0){
        	userRoles.addAll(directRoles);
        }

        final List<IPersonAttributes> peopleAttributes = new ArrayList<IPersonAttributes>(queryResults.size());
        for (final Map<String, List<Object>> queryResult : queryResults) {
        	queryResult.put("directRoles", new ArrayList<Object>(directRoles));
        	queryResult.put("roles", new ArrayList<Object>(userRoles));
        	queryResult.put("groups", new ArrayList<Object>(directGroupsCN));
            final IPersonAttributes person;
            if (queryUserName != null) {
                person = new CaseInsensitiveNamedPersonImpl(queryUserName, queryResult);
            }
            else {
                //Create the IPersonAttributes doing a best-guess at a userName attribute
                final String userNameAttribute = this.getConfiguredUserNameAttribute();
                person = new CaseInsensitiveAttributeNamedPersonImpl(userNameAttribute, queryResult);
            }
            peopleAttributes.add(person);
        }
        
        
        return peopleAttributes;
    }

    /**
     * @see javax.naming.directory.SearchControls#getTimeLimit()
     * @deprecated Set the property on the {@link SearchControls} and set that via {@link #setSearchControls(SearchControls)}
     */
    @Deprecated
    public int getTimeLimit() {
        return this.searchControls.getTimeLimit();
    }

    /**
     * @see javax.naming.directory.SearchControls#setTimeLimit(int)
     * @deprecated
     */
    @Deprecated
    public void setTimeLimit(int ms) {
        this.searchControls.setTimeLimit(ms);
    }
    
    /**
     * @return The base distinguished name to use for queries.
     */
    public String getBaseDN() {
        return this.baseDN;
    }
    
    
    

    public String getGroupsDN() {
		return groupsDN;
	}

	public void setGroupsDN(String groupsDN) {
		this.groupsDN = groupsDN;
	}

	public String getRolesDN() {
		return rolesDN;
	}

	public void setRolesDN(String rolesDN) {
		this.rolesDN = rolesDN;
	}

	/**
     * @param baseDN The base distinguished name to use for queries.
     */
    public void setBaseDN(String baseDN) {
        if (baseDN == null) {
            baseDN = "";
        }

        this.baseDN = baseDN;
    }

    /**
     * @return The ContextSource to get DirContext objects for queries from.
     */
    public ContextSource getContextSource() {
        return this.contextSource;
    }
    
    /**
     * @param contextSource The ContextSource to get DirContext objects for queries from.
     */
    public synchronized void setContextSource(final ContextSource contextSource) {
        Assert.notNull(contextSource, "contextSource can not be null");
        this.contextSource = contextSource;
        this.ldapTemplate = new LdapTemplate(this.contextSource);
    }

    /**
     * Sets the LdapTemplate, and thus the ContextSource (implicitly).
     *
     * @param ldapTemplate the LdapTemplate to query the LDAP server from.  CANNOT be NULL.
     */
    public synchronized void setLdapTemplate(final LdapTemplate ldapTemplate) {
        Assert.notNull(ldapTemplate, "ldapTemplate cannot be null");
        this.ldapTemplate = ldapTemplate;
        this.contextSource = this.ldapTemplate.getContextSource();
    }

    /**
     * @return Search controls to use for LDAP queries
     */
    public SearchControls getSearchControls() {
        return this.searchControls;
    }
    /**
     * @param searchControls Search controls to use for LDAP queries
     */
    public void setSearchControls(SearchControls searchControls) {
        Assert.notNull(searchControls, "searchControls can not be null");
        this.searchControls = searchControls;
    }

    /**
     * @return the queryType
     */
    public QueryType getQueryType() {
        return queryType;
    }
    /**
     * Type of logical operator to use when joining WHERE clause components
     * 
     * @param queryType the queryType to set
     */
    public void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    public String getQueryTemplate() {
        return this.queryTemplate;
    }
    /**
     * Optional wrapper template for the generated part of the query. Use {0} as a placeholder for where the generated query should be inserted.
     */
    public void setQueryTemplate(String queryTemplate) {
        this.queryTemplate = queryTemplate;
    }
}
