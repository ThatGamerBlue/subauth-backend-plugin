package com.thatgamerblue.subauth.backendplugin.services;

import com.thatgamerblue.subauth.backendplugin.BackendEventHandler;
import net.kyori.adventure.text.Component;
import reactor.core.publisher.Mono;

public interface Service {
	String getName();

	Mono<UserData> getUserData(String uuid);

	Mono<Component> unlinkUser(String uuid);

	Mono<Component> invalidateTokens(String uuid);

	Mono<Component> handleGetTokenCommand(BackendEventHandler eventHandler, String uuid, String[] arguments);
}
