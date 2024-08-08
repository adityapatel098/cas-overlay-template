package org.apereo.cas.config;

import org.apereo.cas.authentication.principal.ResponseBuilder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@EnableAsync(proxyTargetClass = false)
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@AutoConfiguration
public class CasOverlayOverrideConfiguration {
	
	private static Logger logger = LoggerFactory.getLogger(CasOverlayOverrideConfiguration.class);
	
	@Configuration(value = "CasCoreServicesResponseBuilderConfigurationCustom", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    public static class CasCoreServicesResponseBuilderConfigurationCustom {
        @Bean
        @ConditionalOnMissingBean(name = "webApplicationServiceResponseBuilder")
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public ResponseBuilder<WebApplicationService> webApplicationServiceResponseBuilder(
            @Qualifier(ServicesManager.BEAN_NAME)
            final ServicesManager servicesManager,
            @Qualifier(UrlValidator.BEAN_NAME)
            final UrlValidator urlValidator) {
        	logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!DynamicRedirectResponseBuilder IS INITIZALIZED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        	return new DynamicRedirectResponseBuilder(servicesManager, urlValidator);
        }
    }
	
}
