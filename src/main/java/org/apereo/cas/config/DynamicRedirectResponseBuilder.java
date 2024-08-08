package org.apereo.cas.config;

import java.util.Map;

import org.apereo.cas.authentication.principal.DefaultResponse;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.WebApplicationServiceResponseBuilder;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

public class DynamicRedirectResponseBuilder extends WebApplicationServiceResponseBuilder {

	private static final long serialVersionUID = 1L;

	public DynamicRedirectResponseBuilder(ServicesManager servicesManager, UrlValidator urlValidator) {
		super(servicesManager, urlValidator);
	}

	@Override
	public Response buildRedirect(WebApplicationService finalService, Map<String, String> parameters) {
//		System.out.print("-------------------------------------Aditya-------------------------");
//		RequestContext context = RequestContextHolder.getRequestContext();
//		if (context == null) return super.buildRedirect(finalService, parameters);
//		String redirectUrl = WebUtils.getHttpServletRequestFromExternalWebflowContext(context).getParameter("redirect");
//		if (redirectUrl == null) return super.buildRedirect(finalService, parameters);
		return DefaultResponse.getRedirectResponse("http://heelow.com", parameters);	
	}
}
