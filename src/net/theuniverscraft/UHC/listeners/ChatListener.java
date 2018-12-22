package net.theuniverscraft.UHC.listeners;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		
		// Format : <%1$s> %2$s		
		String message = event.getMessage();
		
		if(message.startsWith("!") || UHC.getInstance().getGameState() == GameState.LOBBY) {
			if(tplayer.isSpectator()) {
				player.sendMessage(Lang.get("MSG_YOU_CANT_CHAT_WITH_GAMERS"));
				return;
			}
			
			TucTeam team = TeamsManager.getInstance().getTeam(tplayer.getTeamColor());
			if(team == null) {
				event.setFormat(Lang.get("CHAT_LOBBY")
						.replaceAll("<player>", "%1\\$s")
						.replaceAll("<message>", "%2\\$s"));
			}
			else {
				event.setFormat(Lang.get("CHAT_GLOBAL")
						.replaceAll("<player>", "%1\\$s")
						.replaceAll("<message>", "%2\\$s")
						.replaceAll("<team>", team.getColor().getTeamName())
						.replaceAll("<team_color>", team.getColor().getChatColor().toString()));
			}
			
			if(message.startsWith("!")) event.setMessage(message.substring(1));

			return;
		}
		
		if(UHC.getInstance().getGameState().isGameStarted()) {
			if(tplayer.isSpectator()) {
				// Chat spectateur
				for(TucPlayer aplayer : PlayersManager.getInstance().getSpectators()) {
					aplayer.getPlayer().sendMessage(Lang.get("CHAT_SPECTATORS")
							.replaceAll("<player>", player.getName())
							.replaceAll("<message>", message));
				}
			}
			else {
				// Chat de team
				TucTeam team = TeamsManager.getInstance().getTeam(tplayer.getTeamColor());
				team.sendMessage(Lang.get("CHAT_TEAM")
						.replaceAll("<player>", player.getName())
						.replaceAll("<message>", message)
						.replaceAll("<team_color>", team.getColor().getChatColor().toString()));
			}
			
			event.setCancelled(true);
		}
	}
}
