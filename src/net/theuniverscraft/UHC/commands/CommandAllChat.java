package net.theuniverscraft.UHC.commands;

import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandAllChat implements CommandExecutor {
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
		
		if(args.length > 0) {
			StringBuilder message = new StringBuilder();
			message.append("!");
			for(String arg : args) {
				message.append(arg).append(" ");
			}
			
			player.chat(message.toString());
			return true;
		}
		
		return false;
	}
}
