package net.theuniverscraft.UHC.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TucScoreboardManager_old {
	//private Map<UUID, Objective> m_obj = new HashMap<UUID, Objective>();
	private Map<UUID, BoardInfo> m_objectives = new HashMap<UUID, BoardInfo>();
	
	private int m_episode = 1;
	private int m_wait = 0;
	
	private static TucScoreboardManager_old instance;
	public static TucScoreboardManager_old getInstance() {
		if(instance == null) instance = new TucScoreboardManager_old();
		return instance;
	}
	
	private TucScoreboardManager_old() {}
	
	public BoardInfo getBoardInfo(UUID uuid) {
		if(!m_objectives.containsKey(uuid)) {
			Objective obj = Bukkit.getScoreboardManager().getNewScoreboard().registerNewObjective("SpyCraft", "dummy");
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			m_objectives.put(uuid, new BoardInfo(obj, GameState.LOBBY, Lang.get("OBJECTIVE_NAME")));
		}
		return m_objectives.get(uuid);
	}
	
	public Scoreboard getScoreboard(UUID uuid) {
		return getBoardInfo(uuid).getObjective().getScoreboard();
	}
	
	public void clearScoreboard(UUID uuid) { m_objectives.remove(uuid); }
	
	public void refreshBukkitTeam(TucTeam tucTeam) {
		for(UUID uuid : m_objectives.keySet()) {
			m_objectives.get(uuid).getBukkitTeam(tucTeam); // Refresh player
		}
	}
	
	public void removeTeamPlayer(TucTeam tucTeam, Player player) {
		for(UUID uuid : m_objectives.keySet()) {
			m_objectives.get(uuid).getBukkitTeam(tucTeam).removePlayer(player);
		}
	}
	
	public void addEpisode() { m_episode++; }
	public int getEpisode() { return m_episode; }
	
	public void setCentre(UUID uuid, int centre) {
		BoardInfo boardInfo = getBoardInfo(uuid);
		boardInfo.setText(BoardVar.GAME_CENTRE, Lang.get("OBJECTIVE_GAME_CENTRE")
				.replaceAll("<x>", Integer.toString(centre)), 3);
	}
	
	public void updateScoreboard() { updateScoreboard(GameTimer.getInstance().getTime()); }
	public void updateScoreboard(long time) {
		for(UUID uuid : m_objectives.keySet()) {
			Player player = Utils.getPlayerByUUID(uuid);
			TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
			BoardInfo boardInfo = m_objectives.get(uuid);
			
			GameState gameState = UHC.getInstance().getGameState();
			if(gameState == GameState.PLATFORM) gameState = GameState.GAME;
			
			if(boardInfo.getBoardType() != gameState) {
				for(BoardVar score : BoardVar.values()) {
					boardInfo.setText(score, null, 0);
				}
				boardInfo.setBoardType(gameState);
			}
			
			if(tplayer.isSpectator()) {
				boardInfo.setTitle(Lang.get("OBJECTIVE_SPECTATOR_NAME")
						.replaceAll("<player>", player.getName()));
			}
			else if(gameState == GameState.GAME) {
				TucTeam team = TeamsManager.getInstance().getTeam(tplayer.getTeamColor());
				boardInfo.setTitle(Lang.get("OBJECTIVE_PLAYER_NAME")
						.replaceAll("<player>", player.getName())
						.replaceAll("<team>", team.getColor().getTeamName())
						.replaceAll("<team_color>", team.getColor().getChatColor().toString()));
			}
			
			if(boardInfo.getBoardType() == GameState.LOBBY) {
				int nbPlayers = Bukkit.getOnlinePlayers().size();
				boardInfo.setText(BoardVar.LOBBY_PLAYERS, Lang.get("OBJECTIVE_LOBBY_PLAYERS")
						.replaceAll("<x>", Integer.toString(nbPlayers))
						.replaceAll("<max>", Integer.toString(UHC.getPlayerMax())), 2);
				
				if(nbPlayers >= UHC.getInt("player-min")) {
					boardInfo.setText(BoardVar.LOBBY_TIME, Lang.get("OBJECTIVE_LOBBY_START")
							.replaceAll("<sec>", Long.toString(time))
							.replaceAll("<SECOND>", time > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")), 1);
				}
				else {
					StringBuilder sb = new StringBuilder();
					sb.append(Lang.get("OBJECTIVE_LOBBY_WAIT"));
					for(int i = 0; i < m_wait; i++) sb.append(".");
					m_wait = m_wait >= 3 ? 0 : m_wait + 1;
					boardInfo.setText(BoardVar.LOBBY_TIME, sb.toString(), 1);
				}
			}
			else if(boardInfo.getBoardType() == GameState.GAME) {
				int min = (int) (time / 60);
				int sec = (int) (time % 60);
				
				int nbPlayers = PlayersManager.getInstance().getGamers().size();
				int nbTeams = TeamsManager.getInstance().getTeams().size();
				// int centre = (int) player.getLocation().distance(player.getWorld().getSpawnLocation());
							
				boardInfo.setText(BoardVar.GAME_EPISODE, Lang.get("OBJECTIVE_GAME_EPISODE").replaceAll("<x>", Integer.toString(m_episode)), 8);
				boardInfo.setText(BoardVar.GAME_PLAYERS, Lang.get("OBJECTIVE_GAME_PLAYERS")
						.replaceAll("<x>", Integer.toString(nbPlayers))
						.replaceAll("<max>", Integer.toString(UHC.getPlayerMax())), 7);
				boardInfo.setText(BoardVar.GAME_TEAMS, Lang.get("OBJECTIVE_GAME_TEAMS").replaceAll("<x>", Integer.toString(nbTeams)), 6);
					
				boardInfo.getObjective().getScore(Bukkit.getOfflinePlayer(ChatColor.BOLD + " ")).setScore(4);
				// boardInfo.setText(BoardVar.GAME_CENTRE, Lang.get("OBJECTIVE_GAME_CENTRE").replaceAll("<x>", Integer.toString(centre)), 3);
				if(m_episode == 1) {
					boardInfo.getObjective().getScore(Bukkit.getOfflinePlayer(ChatColor.BOLD + "  ")).setScore(2);
					
					boardInfo.setText(BoardVar.GAME_TIME, Lang.get("OBJECTIVE_GAME_TIME")
							.replaceAll("<min>", getVarWithZero(min))
							.replaceAll("<sec>", getVarWithZero(sec)), 1);
				}
				else {
					boardInfo.getObjective().getScoreboard().resetScores(Bukkit.getOfflinePlayer(ChatColor.BOLD + "  "));
					boardInfo.setText(BoardVar.GAME_TIME, null, 1);
				}
			}
		}
	}
	
	public static String getVarWithZero(int var) {
		return var > 9 ? Integer.toString(var) : new StringBuilder().append("0").append(var).toString();
	}
	
	private class BoardInfo {
		private Objective m_obj;
		private String m_title;
		private Map<BoardVar, String> m_boardTxt = new HashMap<BoardVar, String>();
		GameState m_boardType;
		
		private BoardInfo(Objective obj, GameState boardType, String title) {
			m_obj = obj;
			boardType = m_boardType;
			m_title = title;
			m_obj.setDisplayName(Lang.get("OBJECTIVE_NAME"));
		}
		
		public Objective getObjective() { return m_obj; }
		public GameState getBoardType() { return m_boardType; }
		public void setBoardType(GameState state) { m_boardType = state; }
		
		public void setText(BoardVar boardVar, String text, int pos) {
			if(m_boardTxt.containsKey(boardVar)) {
				String last = m_boardTxt.get(boardVar);
				if(last.equalsIgnoreCase(text)) return;
				m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer(last));
			}
			
			if(text == null) {
				m_boardTxt.remove(boardVar);
			}
			else {
				if(text.length() > 16) text = text.substring(0, 16);				
				m_boardTxt.put(boardVar, text);
				m_obj.getScore(Bukkit.getOfflinePlayer(text)).setScore(pos);
			}
		}
		
		public void setTitle(String title) {
			if(!m_title.equalsIgnoreCase(title)) {
				m_title = title;
				m_obj.setDisplayName(title);
			}
		}
		
		public Team getBukkitTeam(TucTeam tucTeam) {
			Team team = m_obj.getScoreboard().getTeam(tucTeam.getColor().name());
			if(team == null) {
				team = m_obj.getScoreboard().registerNewTeam(tucTeam.getColor().name());
				team.setAllowFriendlyFire(false);
				
				StringBuilder prefix = new StringBuilder();
				prefix.append("[").append(tucTeam.getColor().getChatColor().toString());
				prefix.append(tucTeam.getColor().getTeamName()).append(ChatColor.RESET.toString()).append("] ");
				
				team.setPrefix(prefix.toString());
			}
			
			// ON AFFECT LES JOUEURS
			for(TucPlayer tplayer : tucTeam.getPlayers()) {
				if(!team.hasPlayer(tplayer.getPlayer())) team.addPlayer(tplayer.getPlayer());
			}
			
			return team;
		}
	}
	
	public enum BoardVar {
		LOBBY_PLAYERS,
		LOBBY_TIME,
		GAME_EPISODE,
		GAME_PLAYERS,
		GAME_TEAMS,
		GAME_CENTRE,
		GAME_TIME;
	}
}
