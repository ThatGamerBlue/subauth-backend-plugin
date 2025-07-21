package com.thatgamerblue.subauth.backendplugin;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class WSClient extends WebSocketClient {
	private final BackendEventHandler eventHandler;

	public WSClient(BackendEventHandler eventHandler, String host, int port) {
		super(URI.create("ws://" + host + ":" + port + "/backendws"));
		this.eventHandler = eventHandler;
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake) {
		send(eventHandler.getToken());
	}

	@Override
	public void onMessage(String s) {
		Mono.just(s)
			.publishOn(eventHandler.getMainThreadScheduler())
			.mapNotNull(uuid -> Bukkit.getPlayer(UUID.fromString(uuid)))
			.flatMap(p -> eventHandler.sendPlayerServiceMessages(p, p.getUniqueId().toString()))
			.subscribe();
	}

	@Override
	public void onClose(int i, String s, boolean b) {
		Mono.just(s)
			.delayElement(Duration.of(5, ChronoUnit.SECONDS))
			.then(Mono.fromRunnable(eventHandler::wsReconnect))
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe();
	}

	@Override
	public void onError(Exception e) {

	}
}
