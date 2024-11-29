package com.example.streamsecurity;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class SparkWebSocketController {

	@MessageMapping("stream")
	public Flux<String> subscribeToResponseStream(@Payload(required = false) String str, RSocketRequester rSocketRequester, @AuthenticationPrincipal String jwt) {
		System.out.println("stream message mapped");
		System.out.println("jwt = " + jwt);
		return Flux.just("hello world!");
	}

	@MessageMapping("response")
	public Mono<String> subscribeToResponse(String str, RSocketRequester rSocketRequester, @AuthenticationPrincipal String jwt) {
		System.out.println("response message mapped");
		return Mono.just("hello world!");
	}
}