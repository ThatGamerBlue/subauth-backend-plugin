package com.thatgamerblue.subauth.backendplugin;

import com.thatgamerblue.subauth.backendplugin.commands.GetTokenCommand;
import com.thatgamerblue.subauth.backendplugin.commands.UnlinkCommand;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Locale;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SubAuthBackend extends JavaPlugin {
	private BackendEventHandler eventHandler;

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		this.eventHandler = new BackendEventHandler(this);

		Bukkit.getPluginCommand("unlink").setExecutor(new UnlinkCommand(eventHandler));
		Bukkit.getPluginCommand("get_token").setExecutor(new GetTokenCommand(eventHandler));

		// handles events to stop players causing grief
		Bukkit.getPluginManager().registerEvents(new EnforcerEventHandler(this), this);
		// handles events to help players link accounts
		Bukkit.getPluginManager().registerEvents(eventHandler, this);
	}

	public void handleChatCommand(AsyncChatEvent event) {
		Player p = event.getPlayer();
		if (!p.isOp()) {
			return;
		}

		String message = MiniMessage.miniMessage().stripTags(MiniMessage.miniMessage().serialize(event.originalMessage())).toLowerCase(Locale.ROOT);
		String[] split = message.split(" ", 2);
		handleChatCommand(p, split[0], split[1].split(" "));
	}

	private void handleChatCommand(Player p, String command, String[] args) {
		switch (command) {
			case "info": {
				if (args.length == 0) {
					return;
				}
				OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
				eventHandler.sendPlayerServiceMessages(p, target.getUniqueId().toString()).subscribe();
				break;
			}
		}
	}
}
