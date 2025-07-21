package com.thatgamerblue.subauth.backendplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thatgamerblue.subauth.backendplugin.services.Service;
import com.thatgamerblue.subauth.backendplugin.services.TwitchService;
import com.thatgamerblue.subauth.backendplugin.services.UserData;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

public class BackendEventHandler implements Listener {
	private static final Component SERVER_JOIN_MESSAGE =
		text("SubAuth", YELLOW)
			.append(text(" by ", GOLD))
			.append(text("ThatGamerBlue", YELLOW))
			.appendNewline()
			.appendNewline();

	private final SubAuthBackend plugin;
	@Getter
	private final List<Service> services;
	@Getter
	private final HttpClient httpClient;
	@Getter
	private final String token;
	@Getter
	private final Gson gson;
	@Getter
	private final Scheduler mainThreadScheduler;
	private WSClient wsClient;

	public BackendEventHandler(SubAuthBackend plugin) {
		var config = plugin.getConfig();
		this.plugin = plugin;
		this.httpClient = HttpClient.create().host(config.getString("backend_url")).port(config.getInt("backend_port"));
		this.token = config.getString("token_psk");
		this.services = List.of(
			new TwitchService(this)
		);
		this.gson = new GsonBuilder().disableHtmlEscaping().create();
		this.mainThreadScheduler = Schedulers.fromExecutor(Bukkit.getScheduler().getMainThreadExecutor(plugin));
		wsReconnect();

		httpClient.warmup().block();
	}

	public void wsReconnect() {
		this.wsClient = new WSClient(this, plugin.getConfig().getString("backend_url"), plugin.getConfig().getInt("backend_port"));
		wsClient.connect();
	}

	public Service getServiceByName(String serviceName) {
		for (Service service : services) {
			if (service.getName().equals(serviceName)) {
				return service;
			}
		}
		return null;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.getPlayer().sendMessage(SERVER_JOIN_MESSAGE);

		sendPlayerServiceMessages(event.getPlayer(), event.getPlayer().getUniqueId().toString()).subscribe();
	}

	public Mono<Void> sendPlayerServiceMessages(Player player, String uuid) {
		player.sendMessage(text("Current links:", AQUA));

		return Flux.fromIterable(services).flatMap(service -> service.getUserData(uuid))
			.sort(Comparator.comparing(UserData::getServiceName))
			.map(data -> {
				Component component;
				if (data.isError()) {
					component = text("-  ", DARK_RED)
						.append(text("An error occurred fetching data for ", DARK_RED))
						.append(text(data.getServiceName(), DARK_RED));
				} else if (data.isConnected()) {
					component = text("-  ", GREEN)
						.append(text(data.getServiceName(), GREEN))
						.append(text(" is linked with: ", GREEN))
						.append(text(data.getUsername(), GOLD))
						.append(text("  Unlink").style(style(RED, ITALIC)).clickEvent(ClickEvent.runCommand("/unlink " + data.getServiceName())))
						.append(text("  Get Token").style(style(GREEN, ITALIC)).clickEvent(ClickEvent.runCommand("/get_token " + data.getServiceName())));
				} else {
					component = text("-  ", GOLD)
						.append(text(data.getServiceName(), GOLD))
						.append(text(" is not currently linked.  ", GOLD))
						.append(text("Click Here").style(style(GOLD, ITALIC)).clickEvent(ClickEvent.openUrl(data.getConnectUrl())));
				}

				return component;
			})
			.publishOn(mainThreadScheduler)
			.doOnNext(player::sendMessage)
			.onErrorResume(t -> Mono.empty())
			.subscribeOn(Schedulers.boundedElastic()).then();
	}
}
