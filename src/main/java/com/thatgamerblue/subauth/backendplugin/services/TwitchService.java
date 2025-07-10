package com.thatgamerblue.subauth.backendplugin.services;

import com.thatgamerblue.subauth.backendplugin.BackendEventHandler;
import com.thatgamerblue.subauth.backendplugin.HttpHelper;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.TokenAndRedirectResponse;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.TokenResponse;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.WebResponse;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.twitch.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import reactor.core.publisher.Mono;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;

@RequiredArgsConstructor
public class TwitchService implements Service {
	private final BackendEventHandler eventHandler;

	@Override
	public String getName() {
		return "Twitch";
	}

	@Override
	public Mono<UserData> getUserData(String uuid) {
		return HttpHelper.request(eventHandler.getHttpClient(), "/twitch/get_user_info?token=" + eventHandler.getToken() + "&mcUuid=" + uuid)
			.map(str -> eventHandler.getGson().fromJson(str, UserInfoResponse.class))
			.flatMap(response -> {
				if (response.getError() != null) {
					if (response.getError().equals("user not found")) {
						return HttpHelper.request(eventHandler.getHttpClient(), "/twitch/generate_state_token?token=" + eventHandler.getToken() + "&mcUuid=" + uuid)
							.map(str -> eventHandler.getGson().fromJson(str, TokenAndRedirectResponse.class))
							.map(r -> UserData.disconnected(getName(), r.getRedirect()));
					}
					return Mono.just(UserData.error(getName()));
				}
				return Mono.just(UserData.connected(getName(), response.getLogin()));
			});
	}

	@Override
	public Mono<Component> unlinkUser(String uuid) {
		return HttpHelper.request(eventHandler.getHttpClient(), "/twitch/unlink?token=" + eventHandler.getToken() + "&mcUuid=" + uuid)
			.map(str -> eventHandler.getGson().fromJson(str, WebResponse.class))
			.map(response -> {
				if (response.getError() != null) {
					return text("There was an error unlinking your account", DARK_RED);
				} else {
					return text("Successfully unlinked your account!", GREEN);
				}
			});
	}

	@Override
	public Mono<String> getSubscribeToken(String uuid) {
		return HttpHelper.request(eventHandler.getHttpClient(), "/twitch/generate_subscribe_token?token=" + eventHandler.getToken() + "&mcUuid=" + uuid)
			.map(str -> eventHandler.getGson().fromJson(str, TokenResponse.class))
			.flatMap(response -> {
				if (response.getError() != null) {
					return Mono.empty();
				}
				return Mono.just(response.getToken());
			});
	}
}
