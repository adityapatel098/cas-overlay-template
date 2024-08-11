package org.apereo.cas.config;

import java.util.Map;

import org.apereo.cas.authentication.principal.DefaultResponse;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.authentication.principal.TokenWebApplicationServiceResponseBuilder;
import org.apereo.cas.web.UrlValidator;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

public class DynamicRedirectResponseBuilder extends TokenWebApplicationServiceResponseBuilder {

	private static final long serialVersionUID = 1L;
	
	public DynamicRedirectResponseBuilder(ServicesManager servicesManager, TokenTicketBuilder tokenTicketBuilder,
			UrlValidator urlValidator) {
		super(servicesManager, tokenTicketBuilder, urlValidator);
	}

	@Override
	public Response buildRedirect(WebApplicationService finalService, Map<String, String> parameters) {
		RequestContext context = RequestContextHolder.getRequestContext();
		if (context == null) return super.buildRedirect(finalService, parameters);
		String redirectUrl = WebUtils.getHttpServletRequestFromExternalWebflowContext(context).getParameter("redirectUrl");
		if (redirectUrl == null) return super.buildRedirect(finalService, parameters);
		return DefaultResponse.getRedirectResponse(redirectUrl, parameters);	
	}
}
