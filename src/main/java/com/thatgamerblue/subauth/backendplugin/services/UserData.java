package com.thatgamerblue.subauth.backendplugin.services;

import lombok.Value;

@Value
public class UserData {
	String serviceName;
	boolean isConnected;
	String username;
	String connectUrl;
	boolean isError;

	public static UserData connected(String serviceName, String username) {
		return new UserData(serviceName, true, username, null, false);
	}

	public static UserData disconnected(String serviceName, String connectUrl) {
		return new UserData(serviceName, false, null, connectUrl, false);
	}

	public static UserData error(String serviceName) {
		return new UserData(serviceName, false, null, null, true);
	}
}
