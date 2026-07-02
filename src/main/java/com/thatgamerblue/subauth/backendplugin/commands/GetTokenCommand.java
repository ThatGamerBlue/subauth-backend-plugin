package com.thatgamerblue.subauth.backendplugin.commands;

import com.thatgamerblue.subauth.backendplugin.BackendEventHandler;
import com.thatgamerblue.subauth.backendplugin.services.Service;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import reactor.core.scheduler.Schedulers;

@RequiredArgsConstructor
public class GetTokenCommand implements CommandExecutor {
	private final BackendEventHandler eventHandler;

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;
		if (args.length == 0) {
			return false;
		}
		String serviceName = args[0];
		Service service = eventHandler.getServiceByName(serviceName);
		if (service == null) {
			return false;
		}

		service.handleGetTokenCommand(eventHandler, player.getUniqueId().toString(), args)
			.publishOn(eventHandler.getMainThreadScheduler())
			.doOnSuccess(player::sendMessage)
			.subscribeOn(Schedulers.boundedElastic())
			.subscribe();

		return true;
	}
}
