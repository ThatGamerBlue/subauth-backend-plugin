package com.thatgamerblue.subauth.backendplugin.services;

import net.kyori.adventure.text.Component;
import reactor.core.publisher.Mono;

public interface Service {
	String getName();

	Mono<UserData> getUserData(String uuid);

	Mono<Component> unlinkUser(String uuid);

	Mono<String> getSubscribeToken(String uuid);
}
