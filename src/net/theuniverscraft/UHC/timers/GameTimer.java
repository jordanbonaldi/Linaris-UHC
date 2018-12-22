package net.theuniverscraft.UHC.timers;

import java.util.LinkedList;
import java.util.List;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.listeners.Motd;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;
import net.theuniverscraft.UHC.managers.TucScoreboardManager;
import net.theuniverscraft.UHC.managers.WorldsManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GameTimer {
	private static GameTimer instance;
	public static GameTimer getInstance() {
		if(instance == null) instance = new GameTimer();
		return instance;
	}
	
	private int taskId = -1;
	private long m_time;
	private long m_pvp;
	private long m_wall;
	private long m_game;
	
	private List<GameTask> m_tasks = new LinkedList<GameTask>();
	
	private GameTimer() {
		m_time = 9999L;
		m_pvp = UHC.getInt("pvp-time");
		m_wall = UHC.getInt("wall-time");
		m_game = m_wall + WorldsManager.getWall() * 4;
	}
	
	public void start() {
		if(taskId != -1) return;
		
		taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(UHC.getInstance(), new Runnable() {
			@Override
			public void run() {
				// TASKS
				for(int i = 0; i < m_tasks.size(); i++) {
					if(m_tasks.get(i).m_time-- <= 0) {
						m_tasks.get(i).m_task.run();
						m_tasks.remove(i);
						i--;
					}
				}
				// -----
				
				int minutes = (int) (m_time / 60);
				int secondes = (int) (m_time % 60);
				
				if(UHC.getInstance().getGameState() == GameState.LOBBY) {
					// CORRECTION BUG NON-TP
					Location lobby = UHC.getInstance().getLobby();
					for(Player player : Utils.m_playersJustConnected) {
						if(!player.getLocation().getWorld().getUID().equals(lobby.getWorld().getUID()) ||
								player.getLocation().distance(lobby) > 15) {
							player.teleport(lobby);
						}
					}
					Utils.m_playersJustConnected.clear();
					// ---------------------
					
					
					if(m_time > 30) Motd.set(Lang.get("MOTD_LOBBY_WAIT"));
					else {
						Motd.set(Lang.get("MOTD_LOBBY_TIME")
								.replaceAll("<sec>", Long.toString(m_time))
								.replaceAll("<SECOND>", secondes > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")));
					}
					
					if(minutes == 0 && secondes <= 0) {
						if(Bukkit.getOnlinePlayers().size() <= 1) {
							m_time = 9999L;
							return;
						}
						UHC.getInstance().setGameState(GameState.PLATFORM);
						m_time = 15L;
						return;
					}
					TucScoreboardManager.getInstance().updateScoreboard(m_time);
				} else if(UHC.getInstance().getGameState() == GameState.PLATFORM) {
					if(m_time <= 0) {
						Bukkit.broadcastMessage(Lang.get("START"));
						Bukkit.broadcastMessage(Lang.get("CHATALLCOMMAND"));
						m_time = 9999L;
						UHC.getInstance().setGameState(GameState.GAME);
						return;
					}
					else if(m_time <= 10) {
						Bukkit.broadcastMessage(Lang.get("START_IN")
								.replaceAll("<sec>", Integer.toString(secondes))
								.replaceAll("<SECOND>", secondes > 1 ? Lang.get("SECOND_PLURAL") : Lang.get("SECOND_SINGULAR")));
					}
				}
				else if(UHC.getInstance().getGameState() == GameState.GAME) {
					// CHECK MUR
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(PlayersManager.getInstance().getPlayer(player).isSpectator()) continue;
						
						int blocs_to_limit = WorldsManager.blocsToLimit(player.getLocation());
						if(WorldsManager.isOverLimit(player.getLocation())) {
							player.sendMessage(Lang.get("OVER_PORTAL"));
							player.damage(1.0D);
						}
						else if(blocs_to_limit <= 100 && m_time % 2 == 0) {
							player.sendMessage(Lang.get("LIMIT_ARROUND")
									.replaceAll("<x>", Integer.toString(blocs_to_limit)));
						}
					}
					
					if(m_wall <= 0 && m_time % 4 == 0) {
						WorldsManager.decrementeWall();
					}
					// ----------
					TucScoreboardManager.getInstance().updateScoreboard(m_time);
					
					m_wall--;
					m_pvp--;
					m_game--;
				}
				else if(UHC.getInstance().getGameState() == GameState.END) {
					if(m_time <= 0) {
						int team_size = TeamsManager.getInstance().getTeams().size();
						
						if(team_size == 1) { // ON A UN GAGNANT								
							TucTeam winner = TeamsManager.getInstance().getTeams().get(0);
							
							for(Player player : Bukkit.getOnlinePlayers()) {
								TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
								if(tplayer.getTeamColor().equals(winner.getColor())) {
									if(UHC.getBoolean("enable-bungeecord")) Utils.tpToLobby(player);
									else tplayer.getPlayer().kickPlayer(Lang.get("KICK_YOU_HAVE_WIN"));
								}
								else {
									if(UHC.getBoolean("enable-bungeecord")) Utils.tpToLobby(player);
									else {
										tplayer.getPlayer().kickPlayer(Lang.get("KICK_TEAM_HAVE_WIN")
											.replaceAll("<team>", winner.getColor().getTeamName())
											.replaceAll("<team_color>", winner.getColor().getChatColor().toString()));
									}
								}
							}
							UHC.getInstance().setGameState(GameState.RESTART);
							m_time = 5L;
							return;
						}
					}
				}
				else if(UHC.getInstance().getGameState() == GameState.RESTART) {
					if(m_time <= 0) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
					}
				}
				
				m_time--;
				
				if(UHC.getBoolean("always-day-lobby")) {
					UHC.getInstance().getLobby().getWorld().setTime(5000L);
				}
				if(UHC.getBoolean("always-day-game")) {
					WorldsManager.getWorldUHC().setTime(5000L);
				}
			}
		}, 20, 20); // toutes les secondes
	}
	
	public long getWallTime() { return m_wall; }
	public long getPvpTime() { return m_pvp; }
	public long getTime() { return m_time; }
	public long getGameTime() { return m_game; }
	public void setTime(long time) { m_time = time; }
	public void addTask(Runnable task, long time) { m_tasks.add(new GameTask(task, time)); }
	
	
	public class GameTask {
		private Runnable m_task;
		private long m_time;
		
		public GameTask(Runnable task, long time) {
			m_task = task;
			m_time = time;
		}
	}
}
