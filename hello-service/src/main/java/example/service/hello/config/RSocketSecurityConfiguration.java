package example.service.hello.config;

import io.rsocket.metadata.WellKnownMimeType;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.DefaultMetadataExtractor;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;

@Configuration
@EnableRSocketSecurity
public class RSocketSecurityConfiguration {

	@Bean
	public MetadataExtractor metadataExtractor() {
		return new DefaultMetadataExtractor();
	}

    @Bean
    public PayloadSocketAcceptorInterceptor rsocketInterceptor(RSocketSecurity rsocket) {
        rsocket.authorizePayload(authorize ->
                authorize
                        .route("hello")
                            .permitAll()
                        .route("hello.secure.adminonly")
                            .hasRole("ADMIN")
                        .anyRequest()
                            .authenticated()
                        .anyExchange()
                            .permitAll()
        )
                .jwt(jwtSpec -> {
					System.out.println("jwtSpec = " + jwtSpec);
                    try {
                        jwtSpec.authenticationManager(jwtReactiveAuthenticationManager(reactiveJwtDecoder()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        return rsocket.build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec("JAC1O17W1F3QB9E8B4B1MT6QKYOQB36V".getBytes(), mac.getAlgorithm());

        return NimbusReactiveJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager(ReactiveJwtDecoder reactiveJwtDecoder) {
        JwtReactiveAuthenticationManager jwtReactiveAuthenticationManager = new JwtReactiveAuthenticationManager(reactiveJwtDecoder);

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        jwtReactiveAuthenticationManager.setJwtAuthenticationConverter( new ReactiveJwtAuthenticationConverterAdapter(authenticationConverter));
        return jwtReactiveAuthenticationManager;
    }

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOrigin("http://localhost:8081"); // Vue 앱의 주소
		configuration.addAllowedMethod("*");
		configuration.addAllowedHeader("*");
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

//	@Bean
//	public RSocketStrategies rSocketStrategies() {
//		return RSocketStrategies.builder()
//			.metadataExtractorRegistry(registry -> {
//				// 라우팅 정보 추출 설정
//				registry.metadataToExtract(
//					MimeType.valueOf(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.getString()),
//					String.class, "route"
//				);
//				// 인증 정보 추출 설정
//				registry.metadataToExtract(
//					MimeType.valueOf(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString()),
//					String.class, "bearer"
//				);
//			})
//			.build();
//	}

//	@Bean
//	public RSocketStrategies rSocketStrategies() {
//		return RSocketStrategies.builder()
//			.metadataExtractorRegistry(registry -> {
//				registry.metadataToExtract(
//					MimeType.valueOf("application/json"), // JSON 타입으로 Metadata 처리
//					Map.class, "jsonMetadata"
//				);
//			})
//			.build();
//	}

//	@Bean
//	public RSocketStrategies rSocketStrategies() {
//		return RSocketStrategies.builder()
//			.metadataExtractorRegistry(registry -> {
//				registry.metadataToExtract(
//					MimeType.valueOf(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.getString()),
//					String.class, "route"
//				);
//				registry.metadataToExtract(
//					MimeType.valueOf("application/json"),
//					Map.class, "jsonMetadata"
//				);
//			})
//			.decoders(decoders -> {
//				decoders.add(new Jackson2JsonDecoder());
//			})
//			.build();
//	}

//	@Bean
//	public RSocketStrategies rSocketStrategies() {
//		RSocketStrategies strategies = RSocketStrategies.builder()
//			.metadataExtractorRegistry(registry -> {
//				registry.metadataToExtract(
//					MimeType.valueOf("application/json"),
//					Map.class, "jsonMetadata"
//				);
//			})
//			.decoders(decoders -> {
//				decoders.add(new Jackson2JsonDecoder());
//			})
//			.build();
//
//		// 등록된 디코더 확인
//		strategies.decoders().forEach(decoder -> {
//			System.out.println("Registered decoder: " + decoder);
//		});
//
//		return strategies;
//	}

//	@Bean
//	public RSocketStrategies rSocketStrategies() {
//		return RSocketStrategies.builder()
//			.metadataExtractorRegistry(registry -> {
//				registry.metadataToExtract(
//					MimeType.valueOf("application/json"),
//					Map.class, "jsonMetadata"
//				);
//			})
//			.decoders(decoders -> {
//				decoders.add(new Jackson2JsonDecoder());
//			})
//			.build();
//	}

	@Bean
	public RSocketStrategies rSocketStrategies() {
		return RSocketStrategies.builder()
			.metadataExtractorRegistry(registry -> {
				// Routing 정보 추출
				registry.metadataToExtract(
					MimeType.valueOf("message/x.rsocket.routing.v0"),
					String.class, "route"
				);

				// JWT Bearer 토큰 추출
				registry.metadataToExtract(
					MimeType.valueOf("message/x.rsocket.authentication.bearer.v0"),
					String.class, "bearer"
				);
			})
			.build();
	}

//	@Bean
//	public RSocketStrategies rSocketStrategies() {
//		return RSocketStrategies.builder()
//			.encoder(new Jackson2JsonEncoder())  // JSON Encoder 추가
//			.decoder(new Jackson2JsonDecoder())  // JSON Decoder 추가
//			.build();
//	}
}