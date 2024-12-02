package com.example.streamsecurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import reactor.core.publisher.Mono;

import java.util.List;

@Configuration
@EnableRSocketSecurity
public class RSocketConfig {
	@Bean
	public PayloadSocketAcceptorInterceptor payloadSocketAcceptorInterceptor(RSocketSecurity security) {
		return security
			.authorizePayload(authorize -> authorize
				.anyRequest().authenticated() // 모든 요청 인증 필요
				.anyExchange().permitAll()     // 특정 요청 허용
				.setup().authenticated()  // setup 요청은 인증 필요
				.route("stream").hasRole("USER") // 특정 route에 권한 지정 (예: USER 권한 필요)
				.anyRequest().permitAll() // 나머지 요청 허용
			)
			.jwt(jwtSpec -> jwtSpec.authenticationManager(authentication -> {
				String token = authentication.getCredentials().toString();
				System.out.println("token = " + token);
				// JWT 검증 로직
				if ("valid-token".equals(token)) { // 토큰 검증 예제
					return Mono.just(
						new UsernamePasswordAuthenticationToken(
							"user", token, List.of(new SimpleGrantedAuthority("ROLE_USER")) // 권한 부여
						)
					);
				} else {
					return Mono.error(new BadCredentialsException("Invalid token"));
				}
			}))
			.build();
	}

	@Bean
	public RSocketMessageHandler messageHandler(RSocketStrategies rSocketStrategies) {
		RSocketMessageHandler handler = new RSocketMessageHandler();
		handler.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());
		handler.setRSocketStrategies(rSocketStrategies);
		return handler;
	}

	@Bean
	public RSocketStrategies rSocketStrategies() {
		return RSocketStrategies.builder()
			.encoders(encoders -> encoders.add(new Jackson2JsonEncoder()))
			.decoders(decoders -> decoders.add(new Jackson2JsonDecoder()))
			.build();
	}
}