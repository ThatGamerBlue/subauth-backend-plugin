package com.thatgamerblue.subauth.backendplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldLoadEvent;

import static net.kyori.adventure.text.Component.text;

public class EnforcerEventHandler implements Listener {
	private final SubAuthBackend plugin;

	public EnforcerEventHandler(SubAuthBackend plugin) {
		this.plugin = plugin;

		Bukkit.getScheduler().runTaskTimer(plugin, this::removeAllEntities, 1000, 1000);
	}

	public void removeAllEntities() {
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (!(entity instanceof Player)) {
					entity.remove();
				}
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		hideAllPlayers();

		Player player = event.getPlayer();
		player.getInventory().clear();
		event.joinMessage(null);

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (player.isOnline()) {
				player.getPlayer().kick(text("Kicked for inactivity."));
			}
		}, 5 * 60 * 1000);
	}

	@EventHandler
	public void onPlayerChat(AsyncChatEvent event) {
		event.setCancelled(true);
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
		world.setGameRule(GameRule.DO_FIRE_TICK, false);
		world.setGameRule(GameRule.MOB_GRIEFING, false);
		world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
		world.setGameRule(GameRule.KEEP_INVENTORY, true);
		world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
		world.setGameRule(GameRule.DISABLE_RAIDS, true);
		world.setGameRule(GameRule.DO_INSOMNIA, false);
		world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
		world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
		world.setGameRule(GameRule.GLOBAL_SOUND_EVENTS, false);
		world.setGameRule(GameRule.TNT_EXPLODES, false);
		world.setGameRule(GameRule.SPAWN_RADIUS, 1);
		world.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 1);
		world.setGameRule(GameRule.LOCATOR_BAR, false);
		world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
		world.setGameRule(GameRule.DO_TILE_DROPS, false);
		world.setGameRule(GameRule.DO_MOB_LOOT, false);
		world.setGameRule(GameRule.DO_MOB_SPAWNING, false);

		world.setDifficulty(Difficulty.PEACEFUL);
		world.setTime(6000);
		world.getWorldBorder().setSize(500, 1);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getPlayer().isOp()) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getPlayer().isOp()) {
			return;
		}
		event.setCancelled(true);
	}

	@EventHandler
	public void onItemPickup(EntityPickupItemEvent event) {
		event.setCancelled(true);
	}

	private void hideAllPlayers() {
		for (Player playerOne : Bukkit.getServer().getOnlinePlayers()) {
			for (Player playerTwo : Bukkit.getServer().getOnlinePlayers()) {
				if (playerOne == playerTwo) {
					continue;
				}

				playerOne.hidePlayer(plugin, playerTwo);
			}
		}
	}
}
