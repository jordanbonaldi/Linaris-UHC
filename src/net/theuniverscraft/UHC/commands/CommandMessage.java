package net.theuniverscraft.UHC.commands;

import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMessage implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(Lang.get("MSG_MUST_BE_PLAYER"));
			return true;
		}
		
		Player player = (Player) sender;
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		
		if(tplayer.isSpectator()) {
			sender.sendMessage(Lang.get("MSG_YOU_CANT_CHAT_WITH_GAMERS"));
			return true;
		}
		
		if(args.length > 1) {
			Player receiver = Bukkit.getPlayer(args[0]);
			
			if(receiver == null) {
				player.sendMessage(Lang.get("MSG_UNFOUND_PLAYER").replaceAll("<player>", args[0]));
				return true;
			}
			
			StringBuilder message = new StringBuilder();
			for(int i = 1; i < args.length; i++) {
				message.append(args[i]).append(" ");
			}
			
			player.sendMessage(Lang.get("CHAT_PRIVATE_SEND")
					.replaceAll("<receiver>", receiver.getName())
					.replaceAll("<sender>", player.getName())
					.replaceAll("<message>", message.toString()));
			
			receiver.sendMessage(Lang.get("CHAT_PRIVATE_RECEIVE")
					.replaceAll("<receiver>", receiver.getName())
					.replaceAll("<sender>", player.getName())
					.replaceAll("<message>", message.toString()));
			
			return true;
		}
		
		return false;
	}
}
