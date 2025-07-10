package com.thatgamerblue.subauth.backendplugin;

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
}
