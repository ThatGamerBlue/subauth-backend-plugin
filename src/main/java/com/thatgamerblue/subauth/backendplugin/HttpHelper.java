package com.thatgamerblue.subauth.backendplugin;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

public class HttpHelper {
	public static Mono<String> request(HttpClient httpClient, String url) {
		return httpClient
			.get()
			.uri(url)
			.responseContent()
			.aggregate()
			.asString();
	}

	public static String urlEncode(String in) {
		return URLEncoder.encode(in, StandardCharsets.UTF_8);
	}
}
