package example.service.hello.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

/**
 * Controller that generates hello messages.
 */
@Controller
public class HelloController {

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
    public Mono<String> helloSecure(@Payload String name) {
		System.out.println("name = " + name);
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