package net.theuniverscraft.UHC.commands;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.timers.GameTimer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUHC implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(Lang.get("MSG_MUST_BE_PLAYER"));
			return true;
		}
		if(!sender.isOp()) {
			sender.sendMessage(Lang.get("MSG_MUST_BE_ADMIN"));
			return true;
		}
		
		Player player = (Player) sender;
		
		if(args.length == 1) {
			if(args[0].equalsIgnoreCase("setlobby")) {
				Location location = player.getLocation();
				player.getWorld().setSpawnLocation(location.getBlockX(), location.getBlockY(), location.getBlockZ());
				UHC.getInstance().getConfig().set("lobby", UHC.toString(location, false));
				UHC.getInstance().saveConfig();
				player.sendMessage(ChatColor.GREEN + "Lobby defined !");
				return true;
			}
			else if(args[0].equalsIgnoreCase("lobby")) {
				player.teleport(UHC.getInstance().getLobby());
				return true;
			}
			else if(args[0].equalsIgnoreCase("start")) {
				if(UHC.getInstance().getGameState() == GameState.CONFIG) {
					UHC.getInstance().getConfig().set("config-mode", false);
					UHC.getInstance().saveConfig();
					player.sendMessage(ChatColor.GREEN + "Config mode disable, please restart the server");
					return true;
				}
				else if(UHC.getInstance().getGameState() == GameState.LOBBY) {
					GameTimer.getInstance().setTime(2L);
					return true;
				}
			}
		}
		
		return false;
	}

}
