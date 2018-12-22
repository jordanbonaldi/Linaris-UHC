package net.theuniverscraft.UHC;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.commands.CommandAllChat;
import net.theuniverscraft.UHC.commands.CommandMessage;
import net.theuniverscraft.UHC.commands.CommandUHC;
import net.theuniverscraft.UHC.listeners.BasicPlayerListener;
import net.theuniverscraft.UHC.listeners.ChatListener;
import net.theuniverscraft.UHC.listeners.GameListener;
import net.theuniverscraft.UHC.listeners.LobbyListener;
import net.theuniverscraft.UHC.listeners.Motd;
import net.theuniverscraft.UHC.listeners.SpectatorListener;
import net.theuniverscraft.UHC.listeners.WorldListener;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager;
import net.theuniverscraft.UHC.managers.TeamsManager.TeamColor;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;
import net.theuniverscraft.UHC.managers.TucScoreboardManager;
import net.theuniverscraft.UHC.managers.WorldsManager;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class UHC extends JavaPlugin {
	///// A METTRE SUR FALSE !!!
	public static final boolean DEV_MODE = false;
	
	private static UHC instance;
	public static UHC getInstance() { return instance; }
	
	private GameState m_gameState;
	
	@Override
	public void onEnable() {
		instance = this;
		
		if(!UHC.DEV_MODE) {
			Utils.deleteDirectory(new File("world_uhc"));
			Utils.deleteDirectory(new File("world_uhc_nether"));
		}
		
		saveDefaultConfig();
		reloadConfig();
		
		if(UHC.getBoolean("enable-bungeecord")) {
			getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		}
		
		m_gameState = UHC.getBoolean("config-mode") ? GameState.CONFIG : GameState.LOADING;
		
		PluginManager pm = getServer().getPluginManager();
		
		if(m_gameState == GameState.LOADING) {
			if(!UHC.DEV_MODE) {
				if(UHC.getInt("players-by-team") < 3) {
					getConfig().set("players-by-team", 3);
					System.out.println("[" + getName() + "] Bad config : little 5 teams");
					saveConfig();
				}
				if(UHC.getInt("nb-teams") < 2) {
					getConfig().set("nb-teams", 2);
					System.out.println("[" + getName() + "] Bad config : little 2 teams");
					saveConfig();
				}
				else if(UHC.getInt("nb-teams") > 9) {
					getConfig().set("nb-teams", 9);
					System.out.println("[" + getName() + "] Bad config : maximum 9 teams");
					saveConfig();
				}
				
				/*int player_min = UHC.getInt("players-by-team") * (UHC.getInt("nb-teams")-1) + 1;
				if(UHC.getInt("player-min") < player_min) {
					getConfig().set("player-min", player_min);
					System.out.println("[" + getName() + "] Bad config : at less "+player_min+" players by game");
					saveConfig();
				}*/
			}
			
			Motd.set(Lang.get("MOTD_LOADING"));
			
			pm.registerEvents(new BasicPlayerListener(), this);
			pm.registerEvents(new ChatListener(), this);
			pm.registerEvents(new GameListener(), this);
			pm.registerEvents(new LobbyListener(), this);
			pm.registerEvents(new SpectatorListener(), this);
			pm.registerEvents(new WorldListener(), this);
			
			// ------ GENERATION DU MONDE ------
			WorldsManager.getInstance();
			// ---------------------------------
			
			m_gameState = GameState.LOBBY;
			GameTimer.getInstance().start();getLobby().getWorld().setStorm(false);
		}
		else {
			Motd.set(Lang.get("MOTD_CONFIG"));
			System.out.println("[" + getName() + "] Config mode (type /uhc start, to stop configuration)");
		}
		
		getCommand("uhc").setExecutor(new CommandUHC());
		getCommand("all").setExecutor(new CommandAllChat());
		getCommand("message").setExecutor(new CommandMessage());
	}
	
	@Override
	public void onDisable() {
		// SUPPRESSION DU MONDE
		if(m_gameState != GameState.CONFIG) {
			Bukkit.unloadWorld(WorldsManager.getWorldUHC(), false);
		}
	}
	
	public Location getLobby() {
		if(!getConfig().contains("lobby")) return Bukkit.getWorlds().get(0).getSpawnLocation();
		else return toLocation(getConfig().getString("lobby"), false);
	}
	public GameState getGameState() { return m_gameState; }
	
	public void setGameState(GameState gameState) {
		m_gameState = gameState;
		
		if(m_gameState == GameState.PLATFORM) {
			Motd.set(Lang.get("MOTD_GAME"));
			// On attribut les joueurs à une team
			List<TucPlayer> withoutTeam = new LinkedList<TucPlayer>();
			
			for(TucPlayer tplayer : PlayersManager.getInstance().getPlayers()) {
				if(tplayer.getTeamColor() == null) withoutTeam.add(tplayer);
				else TeamsManager.getInstance().getTeam(tplayer.getTeamColor()).addPlayer(tplayer);
			}
			
			while(!TeamsManager.getInstance().allTeamFull() && !withoutTeam.isEmpty()) {
				TucTeam team = TeamsManager.getInstance().getTeamNotFull().get(0);
				team.addPlayer(withoutTeam.get(0));
				withoutTeam.get(0).setTeamColor(team.getColor());
				withoutTeam.remove(0);
			}
			
			
			// Téléporter les joueurs aléatoirement dans la map (max 600 du centre)			
			for(TucTeam team : TeamsManager.getInstance().getTeams()) {
				int i = 0;
				for(TucPlayer tplayer : team.getPlayers()) {
					tplayer.getPlayer().damage(1.0D);
					tplayer.getPlayer().setGameMode(GameMode.SURVIVAL);
					tplayer.getPlayer().setHealth(20D);
					tplayer.getPlayer().setFoodLevel(20);
					tplayer.getPlayer().setFoodLevel(20);
					tplayer.getPlayer().getInventory().clear();
					tplayer.getPlayer().getInventory().setHelmet(null);
					tplayer.getPlayer().getInventory().setChestplate(null);
					tplayer.getPlayer().getInventory().setLeggings(null);
					tplayer.getPlayer().getInventory().setBoots(null);
					tplayer.getPlayer().updateInventory();
					
					tplayer.getPlayer().teleport(WorldsManager.getPlatform(team.getColor()).getSpawn(i));
					i++;
				}
			}
			TucScoreboardManager.getInstance().updateScoreboard(UHC.getInstance().getConfig().getInt("episode-time"));
		}
		else if(m_gameState == GameState.GAME) {
			for(TeamColor color : TeamColor.valuesUsed()) {
				WorldsManager.getPlatform(color).breakGlass();
			}
			
			TeamsManager.getInstance().refreshTeams();
			
			GameTimer.getInstance().addTask(new Runnable() {
				@Override
				public void run() {
					for(TucPlayer player : PlayersManager.getInstance().getPlayers()) {
						player.setMortal();
						player.getPlayer().sendMessage(Lang.get("DEGAT_ENABLE"));
					}
				}
			}, getConfig().getLong("immortal-time"));
			
			
		}
		else if(m_gameState == GameState.END) {
			GameTimer.getInstance().setTime(10L);
		}
	}
	
	public static Location toLocation(final String string, final boolean block) {
        final String[] splitted = string.split("_");
        final World world = Bukkit.getWorld(splitted[0]);
        
        if (world == null || splitted.length < 4) return null;
        
        Location location;
        if(block) {
        	location = new Location(world, Integer.parseInt(splitted[1]), Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]));        	
        }
        else {
        	location = new Location(world, Double.parseDouble(splitted[1]), Double.parseDouble(splitted[2]), Double.parseDouble(splitted[3]));
        	if(splitted.length >= 6) {
        		location.setYaw(Float.parseFloat(splitted[4]));
        		location.setPitch(Float.parseFloat(splitted[5]));
        	}
        }
        return location;
    }

    public static String toString(final Location l, final boolean block) {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(l.getWorld().getName()).append("_");
    	if(block) {
    		sb.append(l.getBlockX()).append("_").append(l.getBlockY()).append("_").append(l.getBlockZ()).append("_");
    	}
    	else {
	    	sb.append(l.getX()).append("_").append(l.getY()).append("_").append(l.getZ()).append("_");
	    	sb.append(l.getYaw()).append("_").append(l.getPitch());
    	}
    	
        return sb.toString();
    }
    
    // RACOURCI
    public static int getInt(String key) { return instance.getConfig().getInt(key); }
    public static boolean getBoolean(String key) { return instance.getConfig().getBoolean(key); }
    public static String getString(String key) { return instance.getConfig().getString(key); }
    public static int getPlayerMax() { return UHC.getInt("players-by-team") * (UHC.getInt("nb-teams")); }
    
    public enum GameState {
    	CONFIG,
    	LOADING,
    	LOBBY,
    	PLATFORM,
    	GAME,
    	END,
    	RESTART;
    	
    	public boolean isGameStarted() {
    		return this == PLATFORM || this == GAME;
    	}
    }
}
