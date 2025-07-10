package com.thatgamerblue.subauth.backendplugin.commands;

import com.thatgamerblue.subauth.backendplugin.BackendEventHandler;
import com.thatgamerblue.subauth.backendplugin.services.Service;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import reactor.core.scheduler.Schedulers;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.ITALIC;

@RequiredArgsConstructor
public class GetTokenCommand implements CommandExecutor {
	private final BackendEventHandler eventHandler;

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;
		if (args.length != 1) {
			return false;
		}
		String serviceName = args[0];
		Service service = eventHandler.getServiceByName(serviceName);
		if (service == null) {
			return false;
		}

		service.getSubscribeToken(player.getUniqueId().toString())
			.defaultIfEmpty("error")
			.map(token -> {
				if (token.equals("error")) {
					return text("There was an error getting your token", DARK_RED);
				}
				return text("Your Token - Click to Copy").style(style(GOLD, ITALIC))
					.clickEvent(ClickEvent.copyToClipboard(token));
			})
			.publishOn(eventHandler.getMainThreadScheduler())
			.doOnNext(player::sendMessage)
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe();

		return true;
	}
}
