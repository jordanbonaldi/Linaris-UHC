package net.theuniverscraft.UHC.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager.TeamColor;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TucScoreboardManager_new {
	//private Map<UUID, Objective> m_obj = new HashMap<UUID, Objective>();
	private Map<UUID, BoardInfo> m_objectives = new HashMap<UUID, BoardInfo>();
	
	private int m_wait = 0;
	
	private static TucScoreboardManager_new instance;
	public static TucScoreboardManager_new getInstance() {
		if(instance == null) instance = new TucScoreboardManager_new();
		return instance;
	}
	
	private TucScoreboardManager_new() {}
	
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
	
	public void setCentre(UUID uuid, int centre) {
		BoardInfo boardInfo = getBoardInfo(uuid);
		boardInfo.setText(BoardVar.GAME_CENTRE, Lang.get("OBJECTIVE_GAME_CENTRE")
				.replaceAll("<x>", Integer.toString(centre)), 2);
	}
	
	public void updateScoreboard() { updateScoreboard(GameTimer.getInstance().getTime()); }
	public void updateScoreboard(long time) {
		for(UUID uuid : m_objectives.keySet()) {
			Player player = Utils.getPlayerByUUID(uuid);
			TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
			BoardInfo boardInfo = m_objectives.get(uuid);
			
			GameState gameState = UHC.getInstance().getGameState();
			
			if(boardInfo.getBoardType() != gameState) { // Refresh des valeurs !
				boardInfo.clear();
				boardInfo.setBoardType(gameState);
			} // ---------------------------------------------------------------
			
			String title = null;
			int nbPlayers;
			if(gameState == GameState.GAME) nbPlayers = PlayersManager.getInstance().getGamers().size();
			else nbPlayers = Bukkit.getOnlinePlayers().size();
			
			int pos = 10;
			if(gameState == GameState.LOBBY) {
				title = Lang.get("OBJECTIVE_NAME");
				
				TeamColor color = tplayer.getTeamColor();
				if(color == null) {
					boardInfo.setText(BoardVar.LOBBY_TEAM, "Team " + ChatColor.RED + "Aucune", pos--);
				}
				else {
					boardInfo.setText(BoardVar.LOBBY_TEAM, "Team " + color.getChatColor() + color.getTeamName(), pos--);
					for(TucPlayer aplayer : PlayersManager.getInstance().getPlayerByTeam(color)) {
						if(player.getUniqueId().equals(aplayer.getPlayer().getUniqueId())) continue;
												
						ChatColor acolor;
						if(player.getHealth() <= 6D) acolor = ChatColor.RED;
						else if(player.getHealth() <= 10D) acolor = ChatColor.YELLOW;
						else acolor = ChatColor.GREEN;
						
						boardInfo.setText(BoardVar.TEAM_PLAYER, acolor.toString() + "  " + aplayer.getPlayer().getName(), pos--);
					}
				}
				pos = 5;
				boardInfo.setText(BoardVar.FIX_INFO, ChatColor.GRAY + "--- Timers --", pos--);
				
				if(nbPlayers >= UHC.getInt("player-min")) {
					boardInfo.setText(BoardVar.LOBBY_TIME, Lang.get("OBJECTIVE_LOBBY_START")
							.replaceAll("<sec>", Long.toString(time))
							.replaceAll("<SECOND>", time > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")), 1);
				}
				else {
					StringBuilder sb = new StringBuilder();
					sb.append(Lang.get("OBJECTIVE_LOBBY_WAIT"));
					for(int i = 0; i < m_wait; i++) sb.append(".");
					boardInfo.setText(BoardVar.LOBBY_TIME, sb.toString(), 1);
				}
			}
			else if(tplayer.isSpectator()) {
				title = Lang.get("OBJECTIVE_SPECTATOR_NAME");
			}
			else if(gameState.isGameStarted()) {
				title = Lang.get("OBJECTIVE_PLAYER_NAME");
				
				TeamColor color = TeamsManager.getInstance().getTeam(tplayer.getTeamColor()).getColor();
				boardInfo.setText(BoardVar.LOBBY_TEAM, "Team " + color.getChatColor() + color.getTeamName(), pos--);
				
				for(TucPlayer aplayer : TeamsManager.getInstance().getTeam(color).getPlayers()) {
					if(player.getUniqueId().equals(aplayer.getPlayer().getUniqueId())) continue;
											
					ChatColor acolor;
					if(player.getHealth() <= 6D) acolor = ChatColor.RED;
					else if(player.getHealth() <= 10D) acolor = ChatColor.YELLOW;
					else acolor = ChatColor.GREEN;
					
					boardInfo.setText(BoardVar.TEAM_PLAYER, acolor.toString() + "  " + aplayer.getPlayer().getName(), pos--);
				}
				pos = 5;
				
				if(UHC.getInstance().getGameState() == GameState.PLATFORM) {
					boardInfo.setText(BoardVar.FIX_INFO, ChatColor.GRAY + "--- Timers --", pos--);
					boardInfo.setText(BoardVar.FIX_INFO, ChatColor.AQUA + "Téléportation", pos--);
				}
				else {
					int nbTeams = TeamsManager.getInstance().getTeams().size();
					
					boardInfo.setText(BoardVar.FIX_INFO, ChatColor.GRAY + "--- Jeu ---", pos--);
					
					boardInfo.setText(BoardVar.GAME_PLAYERS, Lang.get("OBJECTIVE_GAME_PLAYERS")
							.replaceAll("<x>", Integer.toString(nbPlayers)), pos--);
					
					boardInfo.setText(BoardVar.GAME_TEAMS, Lang.get("OBJECTIVE_GAME_TEAMS")
							.replaceAll("<x>", Integer.toString(nbTeams)), pos--);
					
					pos--; // CENTRE
					
					
					boardInfo.setText(BoardVar.FIX_INFO, ChatColor.GRAY + "--- Timers --", pos--);
					
					if(GameTimer.getInstance().getPvpTime() > 0) {
						int min = (int) (GameTimer.getInstance().getTime() / 60);
						int sec = (int) (GameTimer.getInstance().getTime() % 60);
						
						boardInfo.setText(BoardVar.GAME_TIME_PVP, Lang.get("OBJECTIVE_GAME_TIME_PVP")
								.replaceAll("<min>", Integer.toString(min))
								.replaceAll("<sec>", Integer.toString(sec)), pos--);
					}
					else {
						boardInfo.setText(BoardVar.GAME_TIME_PVP, null, pos--);
					}
					
					if(GameTimer.getInstance().getWallTime() >= 0) {
						int min = (int) (GameTimer.getInstance().getWallTime() / 60);
						int sec = (int) (GameTimer.getInstance().getWallTime() % 60);
						
						boardInfo.setText(BoardVar.GAME_TIME_MUR_JEU, Lang.get("OBJECTIVE_GAME_TIME_MUR")
								.replaceAll("<min>", Integer.toString(min))
								.replaceAll("<sec>", Integer.toString(sec)), pos--);
					}
					else {
						int time_game = (int) (GameTimer.getInstance().getTime() % 4 + WorldsManager.getWall()*4);
						int min = (int) (time_game / 60);
						int sec = (int) (time_game % 60);
							
						boardInfo.setText(BoardVar.GAME_TIME_MUR_JEU, Lang.get("OBJECTIVE_GAME_TIME_JEU")
								.replaceAll("<min>", Integer.toString(min))
								.replaceAll("<sec>", Integer.toString(sec)), pos--);
					}
				}
			}
			
			boardInfo.setTitle(title.replaceAll("<player>", player.getName())
					.replaceAll("<online>", Integer.toString(nbPlayers))
					.replaceAll("<max>", Integer.toString(UHC.getPlayerMax())));
		}
		
		m_wait = m_wait >= 3 ? 0 : m_wait + 1;
	}
	
	public static String getVarWithZero(int var) {
		return var > 9 ? Integer.toString(var) : new StringBuilder().append("0").append(var).toString();
	}
	
	private class BoardInfo {
		private Objective m_obj;
		private String m_title;
		private Map<BoardVar, String> m_boardTxt = new HashMap<BoardVar, String>();
		private List<BoardText> m_boardMultiTxt = new LinkedList<BoardText>();
		GameState m_boardType;
		
		private BoardInfo(Objective obj, GameState boardType, String title) {
			m_obj = obj;
			boardType = m_boardType;
			m_title = title;
			m_obj.setDisplayName(Lang.get("OBJECTIVE_NAME"));
		}
		
		public void clear() {
			for(BoardVar var : BoardVar.values()) { setText(var, null, 0); }
			for(BoardText var : m_boardMultiTxt) {
				m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer(var.getText()));
			}
			m_boardTxt.clear();
			m_boardMultiTxt.clear();
		}

		public Objective getObjective() { return m_obj; }
		public GameState getBoardType() { return m_boardType; }
		public void setBoardType(GameState state) { m_boardType = state; }
		
		public void setText(BoardVar boardVar, String text, int pos) {
			if(text != null && text.length() > 16) text = text.substring(0, 16);
			
			if(boardVar.isUnique()) {
				if(m_boardTxt.containsKey(boardVar)) {
					String last_text = m_boardTxt.get(boardVar);
					if(last_text.equalsIgnoreCase(text)) return;
					m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer(last_text));
				}
				
				if(text == null) {
					m_boardTxt.remove(boardVar);
				}
				else {
					m_boardTxt.put(boardVar, text);
					m_obj.getScore(Bukkit.getOfflinePlayer(text)).setScore(pos);
				}
			}
			else {
				/*if(text == null) Bukkit.broadcastMessage(boardVar.name() + " = null");
				else Bukkit.broadcastMessage(boardVar.name() + " = " + text);*/
				
				BoardText boardText = getMultiText(boardVar, pos);
				if(boardText != null) {
					if(boardText.getText().equalsIgnoreCase(text) && boardText.getScore() == pos) return;
					m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer(boardText.getText()));
				}
				else {
					boardText = new BoardText(boardVar, text, pos);
					m_boardMultiTxt.add(boardText);
				}
				
				if(text == null) {
					m_boardMultiTxt.remove(boardText);
				}
				else {
					boardText.setText(text);
					m_obj.getScore(Bukkit.getOfflinePlayer(text)).setScore(pos);
				}
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
		
		private BoardText getMultiText(BoardVar boardVar, int score) {
			for(BoardText text : m_boardMultiTxt) {
				if(text.getBoardVar() == boardVar && text.getScore() == score) return text;
			}
			return null;
		}
	}
	
	public class BoardText {
		private BoardVar m_boardVar;
		private String m_text;
		private int m_score;
		
		public BoardText(BoardVar boardVar, String text, int score) {
			m_boardVar = boardVar;
			m_text = text;
			m_score = score;
		}
		
		public BoardVar getBoardVar() { return m_boardVar; }
		
		public String getText() { return m_text; }
		public void setText(String text) { m_text = text; }
		
		public int getScore() { return m_score; }
	}
	
	public enum BoardVar {
		LOBBY_TEAM,
		LOBBY_TIME,
		TEAM_PLAYER,
		
		GAME_PLAYERS,
		GAME_TEAMS,
		GAME_CENTRE,
		GAME_TIME_PVP,
		GAME_TIME_MUR_JEU,
		
		FIX_INFO;
		
		public boolean isUnique() { return this != TEAM_PLAYER && this != FIX_INFO; }
	}
}
