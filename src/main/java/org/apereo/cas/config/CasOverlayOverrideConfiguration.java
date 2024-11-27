package org.apereo.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.util.EncryptionOptionalSigningOptionalJwtCryptographyProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.token.JwtTicketBuilder;
import org.apereo.cas.token.JwtTokenCipherSigningPublicKeyEndpoint;
import org.apereo.cas.token.TokenTicketBuilder;
import org.apereo.cas.token.authentication.TokenCredential;
import org.apereo.cas.token.cipher.JwtTicketCipherExecutor;
import org.apereo.cas.token.cipher.RegisteredServiceJwtTicketCipherExecutor;
import org.apereo.cas.util.InternalTicketValidator;
import org.apereo.cas.util.cipher.CipherExecutorUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.ComponentSerializationPlanConfigurer;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import org.apereo.cas.validation.TicketValidator;
import org.apereo.cas.web.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ScopedProxyMode;

@EnableConfigurationProperties(CasConfigurationProperties.class)
@Configuration
@AutoConfigureBefore(CasCoreServicesConfiguration.class)
public class CasOverlayOverrideConfiguration {

	private static Logger logger = LoggerFactory.getLogger(CasOverlayOverrideConfiguration.class);

	// Replacement of TokenWebApplicationServiceResponseBuilder.class Bean present in file TokenTicketsConfiguration.class for custom redirect URL
	@Bean(name = "webApplicationServiceResponseBuilder")
	@Primary
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder(
			@Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
			@Qualifier(TokenTicketBuilder.BEAN_NAME) final TokenTicketBuilder tokenTicketBuilder,
			@Qualifier(UrlValidator.BEAN_NAME) final UrlValidator urlValidator) {
		return new DynamicRedirectResponseBuilder(servicesManager, tokenTicketBuilder, urlValidator);
	}

	// Required beans as we disabled tokens feature for TokenCoreComponentSerializationConfiguration.class
	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	public ComponentSerializationPlanConfigurer tokenComponentSerializationPlanConfigurer() {
		return plan -> plan.registerSerializableClass(TokenCredential.class);
	}

	// Required beans as we disabled tokens feature for TokenCoreConfiguration.class

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@ConditionalOnMissingBean(name = "tokenTicketValidator")
	public TicketValidator tokenTicketValidator(
			@Qualifier(WebApplicationService.BEAN_NAME_FACTORY) final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
			@Qualifier(AuthenticationAttributeReleasePolicy.BEAN_NAME) final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy,
			@Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
			@Qualifier(CentralAuthenticationService.BEAN_NAME) final CentralAuthenticationService centralAuthenticationService) {
		return new InternalTicketValidator(centralAuthenticationService, webApplicationServiceFactory,
				authenticationAttributeReleasePolicy, servicesManager);
	}

	@Bean
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	public CipherExecutor tokenCipherExecutor(final CasConfigurationProperties casProperties) {
		EncryptionOptionalSigningOptionalJwtCryptographyProperties crypto = casProperties.getAuthn().getToken()
				.getCrypto();
		Boolean enabled = FunctionUtils
				.doIf(!crypto.isEnabled() && StringUtils.isNotBlank(crypto.getEncryption().getKey())
						&& StringUtils.isNotBlank(crypto.getSigning().getKey()), () -> {
							logger.warn(
									"Token encryption/signing is not enabled explicitly in the configuration, yet signing/encryption keys "
											+ "are defined for operations. CAS will proceed to enable the token encryption/signing functionality.");
							return Boolean.TRUE;
						}, crypto::isEnabled)
				.get();
		if (enabled) {
			return CipherExecutorUtils.newStringCipherExecutor(crypto, JwtTicketCipherExecutor.class);
		}
		logger.info("Token cookie encryption/signing is turned off. This "
				+ "MAY NOT be safe in a production environment. Consider using other choices to handle encryption, "
				+ "signing and verification of generated tokens.");
		return CipherExecutor.noOp();
	}

	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@Bean
	 public JwtBuilder tokenTicketJwtBuilder(
	            final ConfigurableApplicationContext applicationContext,
	            final CasConfigurationProperties casProperties,
	            @Qualifier(PrincipalResolver.BEAN_NAME_PRINCIPAL_RESOLVER)
	            final PrincipalResolver defaultPrincipalResolver,
	            @Qualifier("tokenCipherExecutor") final CipherExecutor tokenCipherExecutor,
	            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
	            return new JwtBuilder(tokenCipherExecutor, applicationContext, servicesManager, defaultPrincipalResolver,
	                new RegisteredServiceJwtTicketCipherExecutor(), casProperties);
	        }

	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	@Bean
	public TokenTicketBuilder tokenTicketBuilder(
            final CasConfigurationProperties casProperties,
            @Qualifier("tokenTicketValidator") final TicketValidator tokenTicketValidator,
            @Qualifier(JwtBuilder.TICKET_JWT_BUILDER_BEAN_NAME) final JwtBuilder tokenTicketJwtBuilder,
            @Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager,
            @Qualifier(TicketFactory.BEAN_NAME)
            final TicketFactory ticketFactory) {
            return new JwtTicketBuilder(tokenTicketValidator, ticketFactory,
                tokenTicketJwtBuilder, servicesManager, casProperties);
        }

	@Bean
	@ConditionalOnAvailableEndpoint
	@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
	public JwtTokenCipherSigningPublicKeyEndpoint jwtTokenCipherSigningPublicKeyEndpoint(
			@Qualifier(WebApplicationService.BEAN_NAME_FACTORY) final ServiceFactory<WebApplicationService> webApplicationServiceFactory,
			final CasConfigurationProperties casProperties,
			@Qualifier("tokenCipherExecutor") final CipherExecutor tokenCipherExecutor,
			@Qualifier(ServicesManager.BEAN_NAME) final ServicesManager servicesManager) {
		return new JwtTokenCipherSigningPublicKeyEndpoint(casProperties, tokenCipherExecutor, servicesManager,
				webApplicationServiceFactory);
	}

}
