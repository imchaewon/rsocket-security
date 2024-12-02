package com.example.streamsecurity;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

//@Component
//public class RSocketAuthenticationManager implements ReactiveAuthenticationManager {
//
//	@Override
//	public Mono<Authentication> authenticate(Authentication authentication) {
//		System.out.println("authenticate called with key " + authentication.getCredentials().toString());
//		List<GrantedAuthority> authorities = new ArrayList<>();
//		return Mono.just(new UsernamePasswordAuthenticationToken("test", null, authorities));
//	}
//}