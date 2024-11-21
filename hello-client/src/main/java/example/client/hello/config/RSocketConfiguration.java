package example.client.hello.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;

@Configuration
public class RSocketConfiguration {

    @Value("${example.service.hello.hostname}")
    private String helloServiceHostname;

    @Value("${example.service.hello.port}")
    private int helloServicePort;

//    @Bean
//    public RSocketRequester rsocketRequester() {
//        return RSocketRequester.builder()
//                .dataMimeType(MimeTypeUtils.TEXT_PLAIN)
//                .connectTcp(helloServiceHostname, helloServicePort)
//                .block();
//    }
	@Bean
	public RSocketRequester rsocketRequester() {
		return RSocketRequester.builder()
			.dataMimeType(MimeTypeUtils.TEXT_PLAIN)
			.connectWebSocket(URI.create("ws://" + helloServiceHostname + ":" + helloServicePort + "/"))
			.block();
	}
}