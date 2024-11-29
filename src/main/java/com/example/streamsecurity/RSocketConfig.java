package com.example.streamsecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity.AuthorizePayloadsSpec;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.web.util.pattern.PathPatternRouteMatcher;
import org.springframework.messaging.rsocket.RSocketStrategies;

@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
public class RSocketConfig {

	private final RSocketAuthenticationManager rSocketAuthenticationManager;

	public RSocketConfig(RSocketAuthenticationManager rSocketAuthenticationManager) {
		this.rSocketAuthenticationManager = rSocketAuthenticationManager;
	}

	@Bean
	public RSocketMessageHandler rSocketMessageHandler(RSocketStrategies strategies) {
		System.out.println("messageHandler initiated!");
		RSocketMessageHandler handler = new RSocketMessageHandler();
		handler.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());
		handler.setRouteMatcher(new PathPatternRouteMatcher());
		handler.setRSocketStrategies(strategies);
		return handler;
	}

	@Bean
	public PayloadSocketAcceptorInterceptor authorization(RSocketSecurity rsocket) {
		rsocket.authorizePayload((AuthorizePayloadsSpec authorize) -> authorize
			.anyRequest().authenticated()
			.anyExchange().permitAll()
		).jwt(jwtSpec -> jwtSpec.authenticationManager(rSocketAuthenticationManager));

		return rsocket.build();
	}

	@Bean
	public RSocketStrategies rSocketStrategies() {
		return RSocketStrategies.builder()
			.routeMatcher(new PathPatternRouteMatcher())
			.build();
	}
}