package com.thatgamerblue.subauth.backendplugin.pojo.responses;

import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@NonFinal
public class TokenResponse extends WebResponse {
	String token;

	TokenResponse(String token) {
		this.token = token;
	}

	public static TokenResponse of(String token) {
		return new TokenResponse(token);
	}
}
