package net.theuniverscraft.UHC.listeners;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;
import net.theuniverscraft.UHC.managers.TucScoreboardManager;
import net.theuniverscraft.UHC.managers.WorldsManager;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GameListener implements Listener {
	// private HashMap<UUID, Location> m_lastCorrectPos = new HashMap<UUID, Location>();
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if(UHC.getInstance().getGameState().isGameStarted()) {
			Player player = event.getEntity();
			TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
			tplayer.setSpectator();
			// event.setDeathMessage(Lang.get("DEATH_MESSAGE").replaceAll("<player>", player.getName()));
			event.setDeathMessage(" "+ChatColor.YELLOW +player.getName() + ChatColor.GRAY + " " + 
			ChatColor.GRAY+(player.getKiller() == null ? "a succombé." : "a été tué par "+
			ChatColor.YELLOW + (player.getKiller()).getName()));
			
			if(UHC.getBoolean("player-death-lightning")) {
				player.getWorld().strikeLightningEffect(player.getLocation());
			}
			
			if(player.getKiller() != null) {
				double heal = player.getKiller().getHealth() + 1.0D;
				double max_heal = player.getKiller().getMaxHealth();
				player.getKiller().setHealth(heal > max_heal ? max_heal : heal);
			}
			
			// Gestion du nb de team
			playerQuitGame(tplayer);
		}
		else {
			event.setDeathMessage(null);
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if(GameTimer.getInstance().getPvpTime() > 0) event.setFoodLevel(20);
	}
	
	// first-episode-pvp
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
			if(GameTimer.getInstance().getPvpTime() > 0) {
				Entity damager = event.getDamager();
				
				if(damager instanceof Player) {
					event.setCancelled(true);
				}
				else if(damager instanceof Projectile) {
					if(((Projectile) damager).getShooter() instanceof Player) {
						event.setCancelled(true);
						return;
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		
		if(UHC.getInstance().getGameState().isGameStarted() && !tplayer.isSpectator()) {
			playerQuitGame(tplayer);
		}
	}
	
	private void playerQuitGame(TucPlayer tplayer) {
		TucTeam team = TeamsManager.getInstance().getTeam(tplayer.getTeamColor());
		if(team != null) {
			team.removePlayer(tplayer);
			TeamsManager.getInstance().refreshTeam(team);
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent event) {
		if(event.getEntity() instanceof Player) {
			RegainReason reason = event.getRegainReason();
			if(reason != RegainReason.MAGIC && reason != RegainReason.MAGIC_REGEN) event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(!UHC.getInstance().getGameState().isGameStarted()) return;
		
		Player player = event.getPlayer();
		
		Location from = event.getFrom();
		Location to = event.getTo();
		
		double distance = to.distance(to.getWorld().getSpawnLocation());
		
		if(UHC.getBoolean("enable-tracker")) {
			Utils.updatePlayersTracker();
		}
		
		if(UHC.getInstance().getGameState() == GameState.PLATFORM) {
			from.setYaw(to.getYaw());
			from.setPitch(to.getPitch());
			
			if(from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())
				player.teleport(from);
		}
		/*else if(UHC.getInstance().getGameState() == GameState.GAME ||
				UHC.getInstance().getGameState() == GameState.END) {
			if(WorldsManager.isOverLimit(to)) {
				player.teleport(m_lastCorrectPos.get(player.getUniqueId()));
			}
			else {
				m_lastCorrectPos.put(player.getUniqueId(), to);
			}
		}*/
		
		TucScoreboardManager.getInstance().setCentre(player.getName(), (int) distance);
		TucScoreboardManager.getInstance().setBorder(player.getName(), player.getLocation());
	}
	
	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent event) {
		if(UHC.getInstance().getGameState() != GameState.GAME) return;
		
		// Le nether est un monde à part : WorldsManager.getWorldNetehr()
		if(!UHC.getBoolean("allow-nether")) {
			event.setCancelled(true);
			return;
		}
		
		// Taille de la map
		int map_radius = event.getFrom().getWorld().getEnvironment() == Environment.NORMAL ? 
					UHC.getInt("map-nether-radius") : UHC.getInt("map-radius");
		
		
		event.useTravelAgent(true);
		TravelAgent travel = event.getPortalTravelAgent();
		
		
		int max_distance = 0;
		if(event.getFrom().getWorld().getEnvironment() == Environment.NORMAL) { // Direction nether
			// event.setTo(WorldsManager.getWorldNether().getSpawnLocation());
			Location loc = event.getFrom().clone();
			loc.setWorld(WorldsManager.getWorldNether());
			event.setTo(loc);
			max_distance = (int) (map_radius - loc.distance(WorldsManager.getWorldNether().getSpawnLocation()));
		}
		else if(event.getFrom().getWorld().getEnvironment() == Environment.NETHER) {
			// event.setTo(WorldsManager.getWorldUHC().getSpawnLocation());
			Location loc = event.getFrom().clone();
			loc.setWorld(WorldsManager.getWorldUHC());
			event.setTo(loc);
			max_distance = (int) (map_radius - loc.distance(WorldsManager.getWorldUHC().getSpawnLocation()));
		}
		
		if(max_distance > 50) max_distance = 50;
		
		double distance = event.getTo().distance(event.getTo().getWorld().getSpawnLocation());
		if(distance > map_radius) {
			// HORS DE LA ZONE
			if(event.getFrom().getWorld().getEnvironment() == Environment.NORMAL) { // Direction nether
				event.setTo(WorldsManager.getWorldNether().getSpawnLocation());
			}
			else if(event.getFrom().getWorld().getEnvironment() == Environment.NETHER) {
				event.setTo(WorldsManager.getWorldUHC().getSpawnLocation());
			}
		}
		
		travel.setSearchRadius(max_distance);
		travel.setCreationRadius(max_distance);
		travel.findOrCreate(event.getTo());
	}
}
