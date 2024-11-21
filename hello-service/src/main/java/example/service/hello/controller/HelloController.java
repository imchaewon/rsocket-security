package example.service.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.security.Principal;

/**
 * Controller that generates hello messages.
 */
@Controller
public class HelloController {

	@Autowired
	private MetadataExtractor metadataExtractor;

	@ConnectMapping
	public Mono<Void> handleConnection(RSocketRequester requester, @Payload(required = false) String data) {
		System.out.println("New RSocket connection established.");

		if (data != null) {
			System.out.println("Payload from client: " + data);
		} else {
			System.out.println("No payload received from client.");
		}

		// 추가적으로 연결된 클라이언트 정보나 로직을 처리할 수 있습니다.
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
        return Mono.just(String.format("Hello, %s! - from unsecured method", name));
    }

    /**
     * Return a hello message for any authenticated user.
     *
     * @param name name to put in the hello message
     * @return hello message
     */
    @MessageMapping("hello.secure")
    public Mono<String> helloSecure(Principal principal, @Payload String name) {
		System.out.println("name = " + name);

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