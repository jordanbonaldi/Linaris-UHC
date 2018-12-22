package net.theuniverscraft.UHC.listeners;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.WorldsManager;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.Inventory;

public class BasicPlayerListener implements Listener {	
	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if(UHC.getInstance().getGameState() != GameState.CONFIG && 
				UHC.getInstance().getGameState() != GameState.LOBBY) {
			event.disallow(Result.KICK_OTHER, Lang.get("KICK_GAME_START"));
		}
		else if(Bukkit.getOnlinePlayers().size() >= UHC.getPlayerMax()) {
			event.disallow(Result.KICK_OTHER, Lang.get("KICK_GAME_FULL"));
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(event.getPlayer());
		
		if(tplayer.isSpectator()) event.setQuitMessage(null);
		else event.setQuitMessage(Lang.get("QUIT_MESSAGE").replaceAll("<player>", event.getPlayer().getName()));
		
		PlayersManager.getInstance().removePlayer(event.getPlayer());
		
		if(Bukkit.getOnlinePlayers().size() < UHC.getInt("player-min") && 
				UHC.getInstance().getGameState() == GameState.LOBBY) {
			GameTimer.getInstance().setTime(9999L);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		Inventory inv = event.getInventory();
				
		try {
			if(inv.getType() == InventoryType.CRAFTING) {
				if(UHC.getInstance().getGameState() == GameState.LOBBY ||
						UHC.getInstance().getGameState() == GameState.PLATFORM) {
					event.setCancelled(true);
					player.closeInventory();
					return;
				}
			}
		} catch(NullPointerException e) {}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {		
		if(event.getEntity() instanceof Player) {
			TucPlayer tplayer = PlayersManager.getInstance().getPlayer((Player) event.getEntity());
			if(!tplayer.isMortal()) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(UHC.getInstance().getGameState() != GameState.GAME && 
				UHC.getInstance().getGameState() != GameState.CONFIG) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(event.getCause() == TeleportCause.ENDER_PEARL && UHC.getBoolean("enderpearls-no-damage")) {
			event.setCancelled(true);
			event.getPlayer().teleport(event.getTo());
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(UHC.getInstance().getGameState() != GameState.GAME && 
				UHC.getInstance().getGameState() != GameState.CONFIG) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {		
		if(UHC.getInstance().getGameState() == GameState.LOBBY) {
			event.setRespawnLocation(UHC.getInstance().getLobby());
			event.getPlayer().setGameMode(GameMode.SURVIVAL);
		}
		else if(UHC.getInstance().getGameState().isGameStarted() ||
				UHC.getInstance().getGameState() == GameState.END) {
			if(UHC.getBoolean("allow-spectator")) {
				event.setRespawnLocation(WorldsManager.getWorldUHC().getSpawnLocation());
			}
			else {
				if(UHC.getBoolean("enable-bungeecord")) {
					event.getPlayer().sendMessage(Lang.get("KICK_LOOSE"));
					Utils.tpToLobby(event.getPlayer());
				}
				else {
					event.getPlayer().kickPlayer(Lang.get("KICK_LOOSE"));
				}
			}
		}
		Utils.setInventory(event.getPlayer());
	}
}
