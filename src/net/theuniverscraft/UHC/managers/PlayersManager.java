package net.theuniverscraft.UHC.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.theuniverscraft.UHC.managers.TeamsManager.TeamColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;


public class PlayersManager {
	private Map<String, TucPlayer> m_players = new HashMap<String, TucPlayer>();
	
	private static PlayersManager instance;
	public static PlayersManager getInstance() {
		if(instance == null) instance = new PlayersManager();
		return instance;
	}
	
	private PlayersManager() {
		
	}
		
	public void removePlayer(Player player) {
		TucScoreboardManager.getInstance().clearScoreboard(player.getName());
		m_players.remove(player.getName());
	}
	
	public TucPlayer getPlayer(Player player) {
		if(m_players.containsKey(player.getName())) return m_players.get(player.getName());
		
		TucPlayer tplayer = new TucPlayer(player);
		m_players.put(player.getName(), tplayer);
		player.setScoreboard(TucScoreboardManager.getInstance().getScoreboard(player.getName()));
		return tplayer;
	}
	
	public Collection<TucPlayer> getPlayers() { return m_players.values(); }
	
	public List<TucPlayer> getGamers() {
		List<TucPlayer> players = new LinkedList<TucPlayer>();
		
		for(TucPlayer player : m_players.values()) {
			if(!player.isSpectator()) players.add(player);
		}
		
		return players;
	}
	
	public List<TucPlayer> getSpectators() {
		List<TucPlayer> players = new LinkedList<TucPlayer>();
		
		for(TucPlayer player : m_players.values()) {
			if(player.isSpectator()) players.add(player);
		}
		
		return players;
	}
	
	public List<TucPlayer> getPlayerByTeam(TeamColor color) {
		List<TucPlayer> players = new LinkedList<TucPlayer>();
		
		for(TucPlayer player : m_players.values()) {
			if(player.getTeamColor() == color) players.add(player);
		}
		
		return players;
	}
	
	public class TucPlayer {
		private Player m_player;
		private boolean m_spectator;
		private boolean m_mortal;
		private TeamColor m_teamColor;
		
		private TucPlayer(Player player) {
			m_player = player;
			m_spectator = false;
			m_mortal = false;
			m_teamColor = null;
		}
		
		public Player getPlayer() { return m_player; }
		
		public void setSpectator() {
			m_spectator = true;
			m_player.setGameMode(GameMode.SURVIVAL);
			m_player.setAllowFlight(true);
			m_player.setFlying(true);
			
			for(Player player : Bukkit.getOnlinePlayers()) {
				player.hidePlayer(m_player.getPlayer());
			}
		}
		public boolean isSpectator() { return m_spectator; }
		
		public void setMortal() { m_mortal = true; }
		public boolean isMortal() { return m_mortal; }
		
		public void setTeamColor(TeamColor color) { m_teamColor = color; }
		public TeamColor getTeamColor() { return m_teamColor; }
	}
}
