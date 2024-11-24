package example.service.hello.controller;

import io.rsocket.util.DefaultPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import io.rsocket.metadata.CompositeMetadata;
import io.rsocket.metadata.CompositeMetadataCodec;

import java.security.Principal;
import java.util.Map;

/**
 * Controller that generates hello messages.
 */
@Controller
public class HelloController {

	@Autowired
	private MetadataExtractor metadataExtractor;

	@ConnectMapping
//	public Mono<Void> handleConnection(RSocketRequester requester, @Payload(required = false) String data) {
	public Mono<Void> handleConnection(RSocketRequester requester, @Headers Map<String, Object> headers) {
		System.out.println("New RSocket connection established.");

		headers.forEach((key, value) -> {
			System.out.println("Header Key: " + key + ", Value: " + value);
		});

		return Mono.empty();
	}

    /**
     * Return a hello message.
     *
     * @param name name to put in the hello message
     * @return hello message
     */
    @MessageMapping("hello")
    public Mono<String> hello(String name) {
		System.out.println("name = " + name);
        return Mono.just(String.format("Hello, %s! - from unsecured method", name));
    }

	@MessageMapping("message.send")
	public Mono<Void> handleMessageSend(@Payload String data, @Headers Map<String, Object> metadata) {
		System.out.println("Payload received: " + data);

		// Metadata를 직접 추출
		if (metadata != null) {
			metadata.forEach((key, value) -> {
				System.out.println("Metadata Key: " + key + ", Value: " + value);
			});
		}

		return Mono.empty();
	}

    /**
     * Return a hello message for any authenticated user.
     *
     * @param name name to put in the hello message
     * @return hello message
     */
    @MessageMapping("hello.secure")
    public Mono<String> helloSecure(Principal principal, @Payload String name, @Header("route") String route) {
		System.out.println("name = " + name);
		System.out.println("Route: " + route);

		if (principal instanceof JwtAuthenticationToken) {
			JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) principal;
			String jwtToken = jwtAuth.getToken().getTokenValue();
			System.out.println("JWT Token: " + jwtToken);
			System.out.println("JWT Claims: " + jwtAuth.getTokenAttributes());
		} else {
			System.out.println("Principal is not JWT-based.");
		}

        return Mono.just(String.format("Hello, %s! - from secured method", name));
    }

    /**
     * Return a hello message only for authenticated admin users.
     *
     * @param name name to put in the hello message
     * @return hello message
     */
    @MessageMapping("hello.secure.adminonly")
    public Mono<String> helloSecureAdminOnly(String name) {
        return Mono.just(String.format("Hello, %s! - from secured method [admin only]", name));
    }
}