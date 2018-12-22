package net.theuniverscraft.UHC.listeners;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager.TeamColor;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class LobbyListener implements Listener {
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		
		event.setJoinMessage(Lang.get("JOIN_MESSAGE")
				.replaceAll("<player>", player.getName()));
		
		if(UHC.getInstance().getGameState() == GameState.LOBBY) {
			PlayersManager.getInstance().getPlayer(player);
			
			// Correction bug non-tp
			event.getPlayer().teleport(UHC.getInstance().getLobby());
			Utils.m_playersJustConnected.add(player);
			
			if(Bukkit.getOnlinePlayers().size() >= UHC.getInt("player-min")) {
				if(GameTimer.getInstance().getTime() > 30L) {
					GameTimer.getInstance().setTime(30L);
				}
			}
			else {
				GameTimer.getInstance().setTime(9999L);
			}
			
			player.setGameMode(GameMode.SURVIVAL);
			player.setHealth(20D);
			player.setFoodLevel(20);
			player.setFoodLevel(20);
			player.getInventory().clear();
			player.getInventory().setHelmet(null);
			player.getInventory().setChestplate(null);
			player.getInventory().setLeggings(null);
			player.getInventory().setBoots(null);

			
			Utils.setInventory(player);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent event) { // CHOIX DE LA TEAM
		Player player = event.getPlayer();
		ItemStack inHand = player.getItemInHand();
		
		if(inHand == null) return;		
		
		if(UHC.getInstance().getGameState() == GameState.LOBBY) {
			if(inHand.getType() == Material.NETHER_STAR) {
				player.openInventory(Utils.getGuiTeams(player));
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	@SuppressWarnings("deprecation")
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		
		if(UHC.getInstance().getGameState() != GameState.LOBBY) return;
		
		try {
			ItemStack is = event.getCurrentItem();
			if(is.getType() == Material.NETHER_STAR) {
				player.closeInventory();
				Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getInstance(), new Runnable() {
					@Override
					public void run() {
						player.openInventory(Utils.getGuiTeams(player));
					}
				}, 10L);
				event.setCancelled(true);
			}
			else if(is.getType() == Material.WOOL) {
				player.closeInventory();
				TeamColor lastColor = tplayer.getTeamColor();
				// TeamColor color = TeamColor.getByDyeColor(((Wool) inHand.getData()).getColor());
				TeamColor color = TeamColor.getByDyeColor(DyeColor.getByWoolData((byte) is.getDurability()));
				
				if(color.equals(lastColor)) return;
				
				int nbPlayers = PlayersManager.getInstance().getPlayerByTeam(color).size();
				
				if(nbPlayers < UHC.getInt("players-by-team")) {
					tplayer.setTeamColor(color);
					player.sendMessage(Lang.get("YOU_HAVE_JOIN_TEAM")
							.replaceAll("<team>", color.getTeamName())
							.replaceAll("<team_color>", color.getChatColor().toString()));
				}
				else {
					player.sendMessage(Lang.get("TOO_MUCH_PLAYER_IN_TEAM"));
				}
				event.setCancelled(true);
			}
		} catch(NullPointerException e) {}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(UHC.getInstance().getGameState() == GameState.LOBBY) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		if(UHC.getInstance().getGameState() == GameState.LOBBY) event.setCancelled(true);
	}
}
