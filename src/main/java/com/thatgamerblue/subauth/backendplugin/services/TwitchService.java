package com.thatgamerblue.subauth.backendplugin.services;

import com.thatgamerblue.subauth.backendplugin.BackendEventHandler;
import com.thatgamerblue.subauth.backendplugin.HttpHelper;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.TokenAndRedirectResponse;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.TokenResponse;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.WebResponse;
import com.thatgamerblue.subauth.backendplugin.pojo.responses.twitch.UserInfoResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import reactor.core.publisher.Mono;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

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
	public Mono<Component> invalidateTokens(String uuid) {
		return HttpHelper.request(eventHandler.getHttpClient(), "/twitch/invalidate_tokens?token=" + eventHandler.getToken() + "&mcUuid=" + uuid)
			.map(str -> eventHandler.getGson().fromJson(str, WebResponse.class))
			.map(response -> {
				if (response.getError() != null) {
					return text("There was an error invalidating your tokens", DARK_RED);
				} else {
					return text("Successfully invalidated your tokens!", GREEN);
				}
			});
	}

	@Override
	public Mono<Component> handleGetTokenCommand(BackendEventHandler eventHandler, String uuid, String[] args) {
		if (args.length == 1) {
			// no tier - ask for which tier
			Component component = text("-  Minimum Tier:  ", GREEN);
			SubscriptionLevel[] tiers = SubscriptionLevel.values();
			int len = tiers.length;
			for (int i = 0; i < len; i++) {
				SubscriptionLevel tier = tiers[i];
				component = component
					.append(text(tier.name).style(style(GOLD, ITALIC)).clickEvent(ClickEvent.runCommand("/get_token " + getName() + " " + tier.twitchTier)));

				if (i < len - 1) {
					component = component.append(text("  "));
				}
			}
			return Mono.just(component);
		} else {
			// get the token for the corresponding tier
			return getSubscribeToken(uuid, args[1])
				.defaultIfEmpty("error")
				.map(token -> {
					if (token.equals("error")) {
						return text("There was an error getting your token", DARK_RED);
					}
					return text("Your Token - Click to Copy").style(style(GOLD, ITALIC))
						.clickEvent(ClickEvent.copyToClipboard(token));
				});
		}
	}

	private Mono<String> getSubscribeToken(String uuid, String tier) {
		return HttpHelper.request(eventHandler.getHttpClient(), "/twitch/generate_subscribe_token?token=" + eventHandler.getToken() + "&mcUuid=" + uuid + "&tier=" + HttpHelper.urlEncode(tier))
			.map(str -> eventHandler.getGson().fromJson(str, TokenResponse.class))
			.flatMap(response -> {
				if (response.getError() != null) {
					return Mono.empty();
				}
				return Mono.just(response.getToken());
			});
	}

	@Getter
	@RequiredArgsConstructor
	private enum SubscriptionLevel {
		TIER_1("Tier 1", "1000"),
		TIER_2("Tier 2", "2000"),
		TIER_3("Tier 3", "3000");

		private final String name;
		private final String twitchTier;
	}
}
