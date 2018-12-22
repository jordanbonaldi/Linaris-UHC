package net.theuniverscraft.UHC.managers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;

public class TeamsManager {
	private List<TucTeam> m_teams = new LinkedList<TucTeam>();
	private boolean m_isWinnerSay = false;
	
	private static TeamsManager instance;
	public static TeamsManager getInstance() {
		if(instance == null) instance = new TeamsManager();
		return instance;
	}
	
	private TeamsManager() {
		for(TeamColor color : TeamColor.valuesUsed()) {
			m_teams.add(new TucTeam(color));
		}
	}
	
	public List<TucTeam> getTeams() { return m_teams; }
	
	public TucTeam getTeam(TeamColor color) {
		for(TucTeam tteam : m_teams) {
			if(tteam.getColor() == color) return tteam;
		}
		return null;
	}
	
	public boolean allTeamFull() {
		for(TucTeam team : m_teams) {
			if(!team.isFull()) return false;
		}
		return true;
	}
	
	public List<TucTeam> getTeamNotFull() {
		List<TucTeam> teams = new ArrayList<TucTeam>();
		
		for(TucTeam team : m_teams) {
			if(!team.isFull()) teams.add(team);
		}
		
		Utils.sortList(teams, new Comparator<TucTeam>() {
			@Override
			public int compare(TucTeam team1, TucTeam team2) {
				return Integer.compare(team1.getPlayers().size(), team2.getPlayers().size());
			}
		});
		
		return teams;
	}
	
	public void refreshTeams() {
		for(int i = m_teams.size() - 1; i >= 0; i--) {
			refreshTeam(m_teams.get(i));
		}
	}
	
	public void refreshTeam(TucTeam team) { // retourne true si il y a un gagnant
		// On test si la team est vide
		if(team.getPlayers().size() <= 0) {
			m_teams.remove(team);
		}
		
		int team_size = TeamsManager.getInstance().getTeams().size();
		
		if(team_size == 1 && !m_isWinnerSay) { // ON A UN GAGNANT	
			TucTeam winner = m_teams.get(0);
			Bukkit.broadcastMessage(Lang.get("TEAM_WIN")
					.replaceAll("<team>", winner.getColor().getTeamName())
					.replaceAll("<team_color>", winner.getColor().getChatColor().toString()));
			m_isWinnerSay = true;
		}
		
		if(team_size <= 1) {
			UHC.getInstance().setGameState(GameState.END);
		}
	}
	
	public class TucTeam {
		private List<TucPlayer> m_players = new LinkedList<TucPlayer>();
		private TeamColor m_color;
		
		private TucTeam(TeamColor color) {
			m_color = color;
			StringBuilder prefix = new StringBuilder();
			prefix.append("[").append(m_color.getChatColor().toString()).append(m_color.getTeamName());
			prefix.append(ChatColor.RESET.toString()).append("] ");
			
			TucScoreboardManager.getInstance().refreshBukkitTeam(this);
		}
		
		public void addPlayer(TucPlayer player) {
			m_players.add(player);
			TucScoreboardManager.getInstance().refreshBukkitTeam(this);
		}
		public void removePlayer(TucPlayer player) {
			m_players.remove(player);
			TucScoreboardManager.getInstance().removeTeamPlayer(this, player.getPlayer());
		}
		
		public TeamColor getColor() { return m_color; }
		
		public List<TucPlayer> getPlayers() { return m_players; }
		
		public void sendMessage(String message) {
			for(TucPlayer player : getPlayers()) {
				player.getPlayer().sendMessage(message);
			}
		}
		
		public void teleport(Location location) {
			Random rand = new Random();
			for(TucPlayer player : getPlayers()) {
				player.getPlayer().teleport(location.clone().add(rand.nextInt(6) - 3D, 0, rand.nextInt(6) - 3D));
			}
		}
		
		public boolean isFull() {
			return getPlayers().size() >= UHC.getInt("players-by-team");
		}
	}
	
	public enum TeamColor {
		RED(DyeColor.RED, ChatColor.RED),
		GREEN(DyeColor.GREEN, ChatColor.GREEN),
		YELLOW(DyeColor.YELLOW, ChatColor.YELLOW),
		BLUE(DyeColor.BLUE, ChatColor.BLUE),
		ORANGE(DyeColor.ORANGE, ChatColor.GOLD),
		PURPLE(DyeColor.PURPLE, ChatColor.DARK_PURPLE),
		MAGENTA(DyeColor.MAGENTA, ChatColor.LIGHT_PURPLE),
		LIGHT_BLUE(DyeColor.LIGHT_BLUE, ChatColor.AQUA),
		GRAY(DyeColor.GRAY, ChatColor.GRAY);
		
		private DyeColor m_dyeColor;
		private ChatColor m_chatColor;
		TeamColor(DyeColor dyeColor, ChatColor chatColor) {
			m_dyeColor = dyeColor;
			m_chatColor = chatColor;
		}
		
		public DyeColor getDyeColor() { return m_dyeColor; }
		public ChatColor getChatColor() { return m_chatColor; }
		public String getTeamName() { return Lang.get("TEAM_NAME_" + name()); }
		
		public static TeamColor getByDyeColor(DyeColor dyeColor) {
			for(TeamColor color : valuesUsed()) {
				if(color.m_dyeColor == dyeColor) return color;
			}
			return null;
		}
		
		public static TeamColor[] valuesUsed() {
			TeamColor[] colors = new TeamColor[UHC.getInt("nb-teams")];

			for(int i = 0; i < colors.length; i++) {
				colors[i] = TeamColor.values()[i];
			}
			
			return colors;
		}
		
	}
}
