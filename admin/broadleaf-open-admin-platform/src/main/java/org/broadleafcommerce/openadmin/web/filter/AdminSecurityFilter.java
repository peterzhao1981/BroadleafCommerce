/*
 * #%L
 * BroadleafCommerce Open Admin Platform
 * %%
 * Copyright (C) 2009 - 2013 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.broadleafcommerce.openadmin.web.filter;


import org.broadleafcommerce.common.exception.ServiceException;
import org.broadleafcommerce.common.security.handler.SecurityFilter;
import org.broadleafcommerce.common.security.service.StaleStateProtectionService;
import org.broadleafcommerce.common.security.service.StaleStateServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class attempts the work flow of the CsrfFilter, but in the event of a Csrf token mismatch 
 * (Session reset for example) the User will be redirected to login, if not session reset User is sent to previous location.
 * This class also handles stale state detection for the admin. This can occur when an admin page form is submitted
 * and the system detects that key state has changed since the time the page was originally rendered.
 * See {@link StaleStateProtectionService} for details.
 * </p>
 * applicationContext-admin-security should reference this class as follows:
 * </p>
 * {@code
 *      ...
 *       <sec:custom-filter ref="blPreSecurityFilterChain" before="CHANNEL_FILTER"/>
 *        <sec:custom-filter ref="blSecurityFilter" before="FORM_LOGIN_FILTER"/>
 *        <sec:custom-filter ref="blAdminFilterSecurityInterceptor" after="EXCEPTION_TRANSLATION_FILTER"/>
 *        <sec:custom-filter ref="blPostSecurityFilterChain" after="SWITCH_USER_FILTER"/>
 *    </sec:http>
 *   <bean id="blSecurityFilter" class="org.broadleafcommerce.openadmin.web.filter.AdminSecurityFilter" />
 *   ...
 * }
 *
 *     
 * @author trevorleffert, Jeff Fischer
 */
public class AdminSecurityFilter extends SecurityFilter {
    
    @Resource(name = "blAdminAuthenticationFailureHandler")
    protected AuthenticationFailureHandler failureHandler;
    
    public void doFilter(ServletRequest baseRequest, ServletResponse baseResponse, FilterChain chain) throws IOException, ServletException {
        try {
            super.doFilter(baseRequest, baseResponse, chain);
        } catch (ServletException e) {
            if (e.getCause() instanceof StaleStateServiceException) {
                e.printStackTrace(new PrintWriter(baseResponse.getWriter()));
                ((HttpServletResponse) baseResponse).setStatus(HttpServletResponse.SC_CONFLICT);
            } else if (e.getCause() instanceof ServiceException) {
                HttpServletRequest baseHttpRequest = (HttpServletRequest) baseRequest;
                //if authentication is null and CSRF token is invalid, must be session time out
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    baseHttpRequest.setAttribute("sessionTimeout", true);
                    failureHandler.onAuthenticationFailure((HttpServletRequest) baseRequest, (HttpServletResponse)
                            baseResponse, new SessionAuthenticationException("Session Time Out"));
                } else {
                    throw e;
                }
            } else {
                throw e;
            }
        }
    }
}
