package pt.gov.dgarq.roda.wui.filter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
 
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
 
public class OverridableHttpRequest implements HttpServletRequest {
 
    private HttpServletRequest wrappedRequest;
    private Map<String, String> newParams;
    private Set<String> removedParams;
 
    public OverridableHttpRequest(HttpServletRequest requestToWrap) {
        this.wrappedRequest = requestToWrap;
        this.newParams = new HashMap<String, String>();
        this.removedParams = new HashSet<String>();
    }
 
    // these things we add so that params can be overridden
    public void setParameter(String name, String value) {
        this.removedParams.remove(name);
        this.newParams.put(name, value);
    }
 
    public void removeParameter(String name) {
        this.newParams.remove(name);
        this.removedParams.add(name);
    }
 
    // these things we need to override so that the correct state is exposed through the standard API
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getParameterNames() {
        Set<String> result = new HashSet<String>();
        Enumeration requestParams = this.wrappedRequest.getParameterNames();
        while (requestParams.hasMoreElements()) {
            Object param = requestParams.nextElement();
            if (!removedParams.contains(param)) {
                result.add((String) param);
            }
        }
        result.addAll(newParams.keySet());
 
        return Collections.enumeration(result);
    }
 
    @Override
    public String[] getParameterValues(String arg0) {
        //NOTE:  not strictly to spec
        String[] result = new String[1];
        result[0] = this.getParameter(arg0);
 
        return result;
    }
 
    @Override
    public String getParameter(String arg0) {
        if (removedParams.contains(arg0)) {
            return null;
        }
        if (newParams.containsKey(arg0)) {
            return newParams.get(arg0);
        }
        return this.wrappedRequest.getParameter(arg0);
    }
 
    @SuppressWarnings("rawtypes")
    @Override
    public Map getParameterMap() {
        Map<String, String[]> result = new HashMap<String, String[]>();
        for (Object key : this.wrappedRequest.getParameterMap().keySet()) {
            result.put((String)key, (String[])this.wrappedRequest.getParameterMap().get(key));
        }
        for (String key : this.newParams.keySet()) {
            result.put(key, new String[] {this.newParams.get(key)});
        }
        for (String key : this.removedParams) {
            result.remove(key);
        }
 
        return result;
    }
 
    // these things we should probably override but don't right now
    @Override
    public String getRequestURI() {
        // FIXME: should return a modified URI based upon current state
        return this.wrappedRequest.getRequestURI();
    }
 
    @Override
    public StringBuffer getRequestURL() {
        // FIXME: should return a modified URL based upon current state
        return this.wrappedRequest.getRequestURL();
    }
 
    @Override
    public String getQueryString() {
        // FIXME: should return a modified String based upon current state
        return this.wrappedRequest.getQueryString();
    }
 
    // everything else just passes through
    @Override
    public Object getAttribute(String arg0) {
        return this.wrappedRequest.getAttribute(arg0);
    }
 
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getAttributeNames() {
        return this.wrappedRequest.getAttributeNames();
    }
 
    @Override
    public String getCharacterEncoding() {
        return this.wrappedRequest.getCharacterEncoding();
    }
 
    @Override
    public int getContentLength() {
        return this.wrappedRequest.getContentLength();
    }
 
    @Override
    public String getContentType() {
        return this.wrappedRequest.getContentType();
    }
 
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.wrappedRequest.getInputStream();
    }
 
    @Override
    public String getLocalAddr() {
        return this.wrappedRequest.getLocalAddr();
    }
 
    @Override
    public String getLocalName() {
        return this.wrappedRequest.getLocalName();
    }
 
    @Override
    public int getLocalPort() {
        return this.wrappedRequest.getLocalPort();
    }
 
    @Override
    public Locale getLocale() {
        return this.wrappedRequest.getLocale();
    }
 
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getLocales() {
        return this.wrappedRequest.getLocales();
    }
 
    @Override
    public String getProtocol() {
        return this.wrappedRequest.getProtocol();
    }
 
    @Override
    public BufferedReader getReader() throws IOException {
        return this.wrappedRequest.getReader();
    }
 
    @SuppressWarnings("deprecation")
    @Override
    public String getRealPath(String arg0) {
        return this.wrappedRequest.getRealPath(arg0);
    }
 
    @Override
    public String getRemoteAddr() {
        return this.wrappedRequest.getRemoteAddr();
    }
 
    @Override
    public String getRemoteHost() {
        return this.wrappedRequest.getRemoteHost();
    }
 
    @Override
    public int getRemotePort() {
        return this.wrappedRequest.getRemotePort();
    }
 
    @Override
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return this.wrappedRequest.getRequestDispatcher(arg0);
    }
 
    @Override
    public String getScheme() {
        return this.wrappedRequest.getScheme();
    }
 
    @Override
    public String getServerName() {
        return this.wrappedRequest.getServerName();
    }
 
    @Override
    public int getServerPort() {
        return this.wrappedRequest.getServerPort();
    }
 
    @Override
    public boolean isSecure() {
        return this.wrappedRequest.isSecure();
    }
 
    @Override
    public void removeAttribute(String arg0) {
        this.wrappedRequest.removeAttribute(arg0);
    }
 
    @Override
    public void setAttribute(String arg0, Object arg1) {
        this.wrappedRequest.setAttribute(arg0, arg1);
    }
 
    @Override
    public void setCharacterEncoding(String arg0)
            throws UnsupportedEncodingException {
        this.wrappedRequest.setCharacterEncoding(arg0);
    }
 
    @Override
    public String getAuthType() {
        return this.wrappedRequest.getAuthType();
    }
 
    @Override
    public String getContextPath() {
        return this.wrappedRequest.getContextPath();
    }
 
    @Override
    public Cookie[] getCookies() {
        return this.wrappedRequest.getCookies();
    }
 
    @Override
    public long getDateHeader(String arg0) {
        return this.wrappedRequest.getDateHeader(arg0);
    }
 
    @Override
    public String getHeader(String arg0) {
        return this.wrappedRequest.getHeader(arg0);
    }
 
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaderNames() {
        return this.wrappedRequest.getHeaderNames();
    }
 
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getHeaders(String arg0) {
        return this.wrappedRequest.getHeaders(arg0);
    }
 
    @Override
    public int getIntHeader(String arg0) {
        return this.wrappedRequest.getIntHeader(arg0);
    }
 
    @Override
    public String getMethod() {
        return this.wrappedRequest.getMethod();
    }
 
    @Override
    public String getPathInfo() {
        return this.wrappedRequest.getPathInfo();
    }
 
    @Override
    public String getPathTranslated() {
        return this.wrappedRequest.getPathTranslated();
    }
 
    @Override
    public String getRemoteUser() {
        return this.wrappedRequest.getRemoteUser();
    }
 
    @Override
    public String getRequestedSessionId() {
        return this.wrappedRequest.getRequestedSessionId();
    }
 
    @Override
    public String getServletPath() {
        return this.wrappedRequest.getServletPath();
    }
 
    @Override
    public HttpSession getSession() {
        return this.wrappedRequest.getSession();
    }
 
    @Override
    public HttpSession getSession(boolean arg0) {
        return this.wrappedRequest.getSession(arg0);
    }
 
    @Override
    public Principal getUserPrincipal() {
        return this.wrappedRequest.getUserPrincipal();
    }
 
    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.wrappedRequest.isRequestedSessionIdFromCookie();
    }
 
    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.wrappedRequest.isRequestedSessionIdFromURL();
    }
 
    @SuppressWarnings("deprecation")
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return this.wrappedRequest.isRequestedSessionIdFromUrl();
    }
 
    @Override
    public boolean isRequestedSessionIdValid() {
        return this.wrappedRequest.isRequestedSessionIdValid();
    }
 
    @Override
    public boolean isUserInRole(String arg0) {
        return this.wrappedRequest.isUserInRole(arg0);
    }
 
}