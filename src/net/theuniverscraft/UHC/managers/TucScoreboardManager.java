package net.theuniverscraft.UHC.managers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager.TeamColor;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TucScoreboardManager {
	//private Map<UUID, Objective> m_obj = new HashMap<UUID, Objective>();
	private Map<String, BoardInfo> m_objectives = new HashMap<String, BoardInfo>();
	
	private int m_wait = 0;
	
	private static TucScoreboardManager instance;
	public static TucScoreboardManager getInstance() {
		if(instance == null) instance = new TucScoreboardManager();
		return instance;
	}
	
	private TucScoreboardManager() {}
	
	public BoardInfo getBoardInfo(String uuid) {
		if(!m_objectives.containsKey(uuid)) {
			Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
			Objective obj = board.registerNewObjective("SpyCraft", "dummy");
			obj.setDisplaySlot(DisplaySlot.SIDEBAR);
			m_objectives.put(uuid, new BoardInfo(obj, GameState.LOBBY, Lang.get("OBJECTIVE_NAME")));
			
			board.registerNewObjective("SpyCraft_list", "health").setDisplaySlot(DisplaySlot.PLAYER_LIST);
		}
		return m_objectives.get(uuid);
	}
	
	public Scoreboard getScoreboard(String uuid) {
		return getBoardInfo(uuid).getObjective().getScoreboard();
	}
	
	public void clearScoreboard(String uuid) { m_objectives.remove(uuid); }
	
	public void refreshBukkitTeam(TucTeam tucTeam) {
		for(String uuid : m_objectives.keySet()) {
			m_objectives.get(uuid).getBukkitTeam(tucTeam); // Refresh player
		}
	}
	
	public void removeTeamPlayer(TucTeam tucTeam, Player player) {
		for(String uuid : m_objectives.keySet()) {
			m_objectives.get(uuid).getBukkitTeam(tucTeam).removePlayer(player);
		}
	}
	
	public void setCentre(String uuid, int centre) {
		BoardInfo boardInfo = getBoardInfo(uuid);
		if(boardInfo.getBoardType() == GameState.GAME) {
			boardInfo.setText(BoardVar.GAME_CENTRE, Lang.get("OBJECTIVE_GAME_CENTRE")
					.replaceAll("<x>", Integer.toString(centre)), boardInfo.getCentrePos());
		}
	}
	
	public void setBorder(String uuid, Location loc) {
		BoardInfo boardInfo = getBoardInfo(uuid);
		if(boardInfo.getBoardType() == GameState.GAME) {
			boardInfo.setText(BoardVar.GAME_BORDER, Lang.get("OBJECTIVE_GAME_BORDER")
					.replaceAll("<x>", Integer.toString(WorldsManager.blocsToLimit(loc))), boardInfo.getBorderPos());
			
		}
	}
	
	public void updateScoreboard() { updateScoreboard(GameTimer.getInstance().getTime()); }
	public void updateScoreboard(long time) {
		for(String uuid : m_objectives.keySet()) {
			Player player = Bukkit.getPlayer(uuid);
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
			
			int pos = 12;
			title = Lang.get("OBJECTIVE_NAME");
			
			if(gameState == GameState.LOBBY) {
				TeamColor color = tplayer.getTeamColor();
				if(color == null) {
					boardInfo.setText(BoardVar.LOBBY_TEAM, "Team " + ChatColor.RED + "Aucune", pos--);
				}
				else {
					boardInfo.setText(BoardVar.LOBBY_TEAM, "Team " + color.getChatColor() + color.getTeamName(), pos--);
					
					List<String> players_list = new LinkedList<String>();
					for(TucPlayer aplayer : PlayersManager.getInstance().getPlayerByTeam(color)) {
						if(player.getName().equals(aplayer.getPlayer().getName())) continue;
												
						ChatColor acolor;
						if(aplayer.getPlayer().getHealth() <= 6D) acolor = ChatColor.RED;
						else if(aplayer.getPlayer().getHealth() <= 10D) acolor = ChatColor.YELLOW;
						else acolor = ChatColor.GREEN;
						
						players_list.add(acolor + "  " + aplayer.getPlayer().getName());
					}
					boardInfo.setMultiText(BoardVar.TEAM_PLAYER, players_list, pos--);
					pos -= players_list.size();
				}
				boardInfo.setText(BoardVar.MSG_TIMER, ChatColor.GRAY + "--- Timers --", pos--);
				
				if(nbPlayers >= UHC.getInt("player-min")) {
					boardInfo.setText(BoardVar.LOBBY_TIME, Lang.get("OBJECTIVE_LOBBY_START")
							.replaceAll("<sec>", Long.toString(time))
							.replaceAll("<SECOND>", time > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")), pos--);
				}
				else {
					StringBuilder sb = new StringBuilder();
					sb.append(Lang.get("OBJECTIVE_LOBBY_WAIT"));
					for(int i = 0; i < m_wait; i++) sb.append(".");
					boardInfo.setText(BoardVar.LOBBY_TIME, sb.toString(), pos--);
				}
			}
			else if(tplayer.isSpectator()) {
				title = Lang.get("OBJECTIVE_SPECTATOR_NAME");
			}
			else if(gameState.isGameStarted()) {
				title = Lang.get("OBJECTIVE_PLAYER_NAME")
						.replaceAll("<team>", tplayer.getTeamColor().getTeamName())
						.replaceAll("<team_color>", tplayer.getTeamColor().getChatColor().toString());
				
				TeamColor color = TeamsManager.getInstance().getTeam(tplayer.getTeamColor()).getColor();
				boardInfo.setText(BoardVar.LOBBY_TEAM, "Team " + color.getChatColor() + color.getTeamName(), pos--);
				
				List<String> players_list = new LinkedList<String>();
				for(TucPlayer aplayer : TeamsManager.getInstance().getTeam(color).getPlayers()) {
					if(player.getName().equals(aplayer.getPlayer().getName())) continue;
											
					ChatColor acolor;
					if(aplayer.getPlayer().getHealth() <= 6D) acolor = ChatColor.RED;
					else if(aplayer.getPlayer().getHealth() <= 10D) acolor = ChatColor.YELLOW;
					else acolor = ChatColor.GREEN;
					
					players_list.add(acolor + "  " + aplayer.getPlayer().getName());
				}
				boardInfo.setMultiText(BoardVar.TEAM_PLAYER, players_list, pos--);
				pos -= players_list.size();
				
				if(UHC.getInstance().getGameState() == GameState.PLATFORM) {
					boardInfo.setText(BoardVar.MSG_TIMER, ChatColor.GRAY + "--- Timers --", pos--);
					boardInfo.setText(BoardVar.MSG_TP, ChatColor.AQUA + "Téléportation", pos--);
				}
				else {
					int nbTeams = TeamsManager.getInstance().getTeams().size();
					
					boardInfo.setText(BoardVar.MSG_GAME, ChatColor.GRAY + "--- Jeu ---", pos--);
					
					boardInfo.setText(BoardVar.GAME_PLAYERS, Lang.get("OBJECTIVE_GAME_PLAYERS")
							.replaceAll("<x>", Integer.toString(nbPlayers)), pos--);
					
					boardInfo.setText(BoardVar.GAME_TEAMS, Lang.get("OBJECTIVE_GAME_TEAMS")
							.replaceAll("<x>", Integer.toString(nbTeams)), pos--);
					
					boardInfo.setCentrePos(pos--); // CENTRE
					double distance = player.getLocation().distance(player.getWorld().getSpawnLocation());
					setCentre(uuid, (int) distance);
					
					boardInfo.setBorderPos(pos--); // BORDER
					setBorder(uuid, player.getLocation());
					
					boardInfo.setText(BoardVar.MSG_TIMER, ChatColor.GRAY + "--- Timers --", pos--);
					
					if(GameTimer.getInstance().getPvpTime() > 0) {
						int min = (int) (GameTimer.getInstance().getPvpTime() / 60);
						int sec = (int) (GameTimer.getInstance().getPvpTime() % 60);
						
						boardInfo.setText(BoardVar.GAME_TIME_PVP, Lang.get("OBJECTIVE_GAME_TIME_PVP")
								.replaceAll("<min>", Integer.toString(min))
								.replaceAll("<sec>", getVarWithZero(sec)), pos--);
					}
					else {
						boardInfo.setText(BoardVar.GAME_TIME_PVP, null, pos--);
					}
					
					if(GameTimer.getInstance().getWallTime() >= 0) {
						int min = (int) (GameTimer.getInstance().getWallTime() / 60);
						int sec = (int) (GameTimer.getInstance().getWallTime() % 60);
						
						boardInfo.setText(BoardVar.GAME_TIME_MUR_JEU, Lang.get("OBJECTIVE_GAME_TIME_MUR")
								.replaceAll("<min>", Integer.toString(min))
								.replaceAll("<sec>", getVarWithZero(sec)), pos--);
					}
					else {
						int time_game = (int) (GameTimer.getInstance().getGameTime());
						int min = (int) (time_game / 60);
						int sec = (int) (time_game % 60);
							
						boardInfo.setText(BoardVar.GAME_TIME_MUR_JEU, Lang.get("OBJECTIVE_GAME_TIME_JEU")
								.replaceAll("<min>", Integer.toString(min))
								.replaceAll("<sec>", getVarWithZero(sec)), pos--);
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
		private Map<BoardVar, List<String>> m_boardMultiTxt = new HashMap<BoardVar, List<String>>();
		GameState m_boardType;
		int m_centrePos = 0;
		int m_borderPos = 0;
		
		private BoardInfo(Objective obj, GameState boardType, String title) {
			m_obj = obj;
			boardType = m_boardType;
			m_title = title;
			m_obj.setDisplayName(Lang.get("OBJECTIVE_NAME"));
		}
		
		public void clear() {
			for(BoardVar var : BoardVar.values()) { setText(var, null, 0); }
			m_boardTxt.clear();
			m_boardMultiTxt.clear();
		}

		public Objective getObjective() { return m_obj; }
		public GameState getBoardType() { return m_boardType; }
		public void setBoardType(GameState state) { m_boardType = state; }
		
		public void setCentrePos(int pos) { m_centrePos = pos; }
		public int getCentrePos() { return m_centrePos; }
		
		public void setBorderPos(int pos) { m_borderPos = pos; }
		public int getBorderPos() { return m_borderPos; }
		
		public void setText(BoardVar boardVar, String text, int pos) {
			if(pos <= 0) pos--;
			
			if(m_boardTxt.containsKey(boardVar)) {
				String last_text = m_boardTxt.get(boardVar);
				if(last_text.equalsIgnoreCase(text)) {
					m_obj.getScore(Bukkit.getOfflinePlayer(text)).setScore(pos);
					return;
				}
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
		
		public void setMultiText(BoardVar boardVar, List<String> texts, int pos) {
			List<String> last_texts;
			if(m_boardMultiTxt.containsKey(boardVar)) last_texts = m_boardMultiTxt.get(boardVar);
			else last_texts = new LinkedList<String>();
			
			for(int i = 0; i < texts.size(); i++) {
				String text = texts.get(i);
				
				if(last_texts.size() < texts.size()) last_texts.add("null");
				
				if(text != null && text.length() > 16) text = text.substring(0, 16);
				if(last_texts.get(i).equalsIgnoreCase(text)) {
					m_obj.getScore(Bukkit.getOfflinePlayer(text)).setScore(pos);
					continue;
				}
				
				m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer(last_texts.get(i)));
				
				if(text == null) {
					last_texts.remove(i);
					i--;
				}
				else {
					last_texts.set(i, text);
					m_obj.getScore(Bukkit.getOfflinePlayer(text)).setScore(pos + i);
				}
			}
			
			if(last_texts.size() > texts.size()) {
				for(int i = texts.size(); i < last_texts.size(); i++) {
					m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer(last_texts.get(i)));
					last_texts.remove(i);
					i--;
				}
			}
			
			m_boardMultiTxt.put(boardVar, last_texts);
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
		
		/*private BoardText getMultiText(BoardVar boardVar, int score) {
			for(BoardText text : m_boardMultiTxt) {
				if(text.getBoardVar() == boardVar && text.getScore() == score) return text;
			}
			return null;
		}*/
	}
	
	/*public class BoardText {
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
	}*/
	
	public enum BoardVar {
		LOBBY_TEAM,
		LOBBY_TIME,
		TEAM_PLAYER,
		
		GAME_PLAYERS,
		GAME_TEAMS,
		GAME_CENTRE,
		GAME_BORDER,
		GAME_TIME_PVP,
		GAME_TIME_MUR_JEU,
		
		MSG_TIMER,
		MSG_TP,
		MSG_GAME;
	}
}
