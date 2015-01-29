<%--

    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="includes/roda-login-top.jsp" />

	<div id="roda-login">
		<div id="roda-login-logo">
		</div>
		<div id="roda-login-panel">
			<form:form method="post" id="fm1" cssClass="fm-v clearfix" commandName="${commandName}" htmlEscape="true" onsubmit="return prepareSubmit(this);">
                  		<form:errors path="*" id="msg" cssClass="errors" element="div" />
				<h2><spring:message code="screen.welcome.instructions" /></h2>
                    		<div class="row fl-controls-left">
                        		<label for="username" class="fl-label"><spring:message code="screen.welcome.label.netid" /></label>
					<c:if test="${not empty sessionScope.openIdLocalId}">
							<strong>${sessionScope.openIdLocalId}</strong>
						<input type="hidden" id="username" name="username" value="${sessionScope.openIdLocalId}" />
					</c:if>

					<c:if test="${empty sessionScope.openIdLocalId}">
						<spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
						<form:input cssClass="required" cssErrorClass="error" id="username" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="false" htmlEscape="true" />
					</c:if>
                    		</div>
                    		<div class="row fl-controls-left">
                        		<label for="password" class="fl-label"><spring:message code="screen.welcome.label.password" /></label>
						
					<spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
					<form:password cssClass="required" cssErrorClass="error" id="password" tabindex="2" path="password"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
                    		</div>
				<input type="hidden" name="lt" value="${loginTicket}" />
				<input type="hidden" name="execution" value="${flowExecutionKey}" />
				<input type="hidden" name="_eventId" value="submit" />

                    		<div class="row check">
					<input id="warn" name="warn" value="true" tabindex="3" accesskey="<spring:message code="screen.welcome.label.warn.accesskey" />" type="checkbox" />
                        		<label for="warn"><spring:message code="screen.welcome.label.warn" /></label>
                    		</div>
                    		<div class="row btn-row">
					<div id="languages-div">
						<!--<label>Languages:</label>-->
						<select name="locale" id="locale" onchange="onChange()">
                               				<option value="en">English</option>
                               				<option value="es">Spanish</option>
                               				<option value="fr">French</option>
                               				<option value="ru">Russian</option>
                               				<option value="nl">Nederlands</option>
                               				<option value="sv">Svenska</option>
                               				<option value="it">Italiano</option>
                               				<option value="ur">Urdu</option>
                               				<option value="zh_CN">Chinese (Simplified)</option>
                               				<option value="zh_TW">Chinese (Traditional)</option>
                               				<option value="de">Deutsch</option>
                               				<option value="ja">Japanese</option>
                               				<option value="hr">Croatian</option>
                               				<option value="cs">Czech</option>
                               				<option value="sl">Slovenian</option>
                               				<option value="pl">Polish</option>
                               				<option value="ca">Catalan</option>
                               				<option value="mk">Macedonian</option>
                               				<option value="fa">Farsi</option>
                               				<option value="ar">Arabic</option>
                               				<option value="pt_PT">Portuguese</option>
                               				<option value="pt_BR">Portuguese (Brazil)</option>
                           			</select>
					</div>
                           		
					<input type="hidden" name="lt" value="${locale}" />
					<input type="hidden" name="lt" value="${loginTicket}" />
					<input type="hidden" name="selected_language" value="${loginTicket}" />
					<input type="hidden" name="execution" value="${flowExecutionKey}" />
					<input type="hidden" name="_eventId" value="submit" />

					<div id="buttons">
                        			<input class="btn-submit" name="submit" accesskey="l" value="<spring:message code="screen.welcome.button.login" />" tabindex="4" type="submit" />
                        			<input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="5" type="reset" />
					</div>
                    		</div>
            		</form:form>
		</div>
		<div id="roda-login-errors">
		</div>
	</div>
<jsp:directive.include file="includes/roda-login-bottom.jsp" />
