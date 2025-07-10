package com.thatgamerblue.subauth.backendplugin;

import com.thatgamerblue.subauth.backendplugin.commands.GetTokenCommand;
import com.thatgamerblue.subauth.backendplugin.commands.UnlinkCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SubAuthBackend extends JavaPlugin {
	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		BackendEventHandler eventHandler = new BackendEventHandler(this);

		Bukkit.getPluginCommand("unlink").setExecutor(new UnlinkCommand(eventHandler));
		Bukkit.getPluginCommand("get_token").setExecutor(new GetTokenCommand(eventHandler));

		// handles events to stop players causing grief
		Bukkit.getPluginManager().registerEvents(new EnforcerEventHandler(this), this);
		// handles events to help players link accounts
		Bukkit.getPluginManager().registerEvents(eventHandler, this);
	}
}
