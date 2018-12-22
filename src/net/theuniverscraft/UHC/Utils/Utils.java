package net.theuniverscraft.UHC.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.UHC.GameState;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;
import net.theuniverscraft.UHC.managers.TeamsManager;
import net.theuniverscraft.UHC.managers.TeamsManager.TeamColor;
import net.theuniverscraft.UHC.managers.TeamsManager.TucTeam;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public abstract class Utils {
	public static List<Player> m_playersJustConnected = new LinkedList<Player>();
	
	public static void deleteDirectory(File path) {
		if(path.exists()) { 
			for(File file : path.listFiles()) {
				if(file.isDirectory()) { 
					deleteDirectory(file); 
				} 
				else { 
					file.delete(); 
				} 
			}
		}
	}

	public static <T> void sortList(List<T> list, Comparator<T> c) {
		for(int i = 0; i < list.size() - 1; i++) {
			T obj1 = list.get(i);
			T obj2 = list.get(i+1);
			if(c.compare(obj1, obj2) > 0) {
				list.set(i, obj2);
				list.set(i+1, obj1);
				i -= 2;
				if(i <= -2) i++;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void setInventory(Player player) {
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		PlayerInventory inv = player.getInventory();
		
		if(UHC.getInstance().getGameState() == GameState.LOBBY) {
			inv.clear();
			
			ItemStack itemMenu = new ItemStack(Material.NETHER_STAR);
			ItemMeta metaMenu = itemMenu.getItemMeta();
			metaMenu.setDisplayName(Lang.get("ITEM_CHOOSE_TEAM"));
			itemMenu.setItemMeta(metaMenu);
			inv.setItem(4, itemMenu);
			
			player.updateInventory();
		}
		else if(tplayer.isSpectator()) {
			inv.clear();
			ItemStack itemMenu = new ItemStack(Material.COMPASS);
			ItemMeta metaMenu = itemMenu.getItemMeta();
			metaMenu.setDisplayName(Lang.get("ITEM_SPECTATOR_COMPASS"));
			itemMenu.setItemMeta(metaMenu);
			inv.setItem(0, itemMenu);
			
			if(UHC.getBoolean("enable-bungeecord")) {
				ItemStack itemLobby = new ItemStack(Material.BED);
				ItemMeta metaLobby = itemMenu.getItemMeta();
				metaLobby.setDisplayName(Lang.get("ITEM_SPECTATOR_LOBBY"));
				itemLobby.setItemMeta(metaLobby);
				inv.setItem(8, itemLobby);
			}
			
			player.updateInventory();
		}
	}

	public static Inventory getInvSpectatorTp(Player player) {
		List<TucPlayer> gamers = PlayersManager.getInstance().getGamers();
		int slot = gamers.size();
		if(slot % 9 != 0) slot += 9 - slot % 9;
		
		Inventory inv = Bukkit.createInventory(player, slot, ChatColor.stripColor(Lang.get("ITEM_SPECTATOR_COMPASS")));
		
		for(int i = 0; i < gamers.size(); i++) {
			ItemStack skull = new ItemStack(Material.SKULL_ITEM);
			skull.setDurability((short) 3);
			SkullMeta meta = (SkullMeta) skull.getItemMeta();
			meta.setDisplayName(gamers.get(i).getPlayer().getName());
			meta.setOwner(gamers.get(i).getPlayer().getName());
			skull.setItemMeta(meta);
			
			inv.setItem(i, skull);
		}
		
		return inv;
	}
	
	@SuppressWarnings("deprecation")
	public static Inventory getGuiTeams(Player player) {
		Inventory inv = Bukkit.createInventory(player, 9, ChatColor.stripColor(Lang.get("ITEM_CHOOSE_TEAM")));
		
		for(int i = 0; i < TeamColor.valuesUsed().length; i++) {
			TeamColor color = TeamColor.valuesUsed()[i];
			
			ItemStack wool = new ItemStack(Material.WOOL);
			//wool.setData(new Wool(color.getColor()));
			wool.setDurability((short) color.getDyeColor().getWoolData());
			
			TucTeam team = TeamsManager.getInstance().getTeam(color);
			
			ItemMeta meta = wool.getItemMeta();
			
			meta.setDisplayName(team.getColor().getChatColor() + team.getColor().getTeamName());
			
			List<String> lore = new ArrayList<String>();
			for(TucPlayer a_tplayer : PlayersManager.getInstance().getPlayerByTeam(color)) {
				lore.add(ChatColor.WHITE +" - " +ChatColor.GRAY+ a_tplayer.getPlayer().getName());
			}
			
			meta.setLore(lore);
			wool.setItemMeta(meta);
			
			inv.setItem(i, wool);
		}
		
		return inv;
	}
	
	public static void tpToLobby(Player player) {
		if(UHC.getBoolean("enable-bungeecord")) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(UHC.getString("bungeecord-lobby"));
			player.sendPluginMessage(UHC.getInstance(), "BungeeCord", out.toByteArray());
		}
	}

	public static Player getNearestEnnemy(TucPlayer player) {
		Player nearest = null;
		for(TucPlayer tplayer : PlayersManager.getInstance().getGamers()) {
			if(player.getPlayer().getUniqueId() == tplayer.getPlayer().getUniqueId()) continue;
			else if(player.getTeamColor() == tplayer.getTeamColor()) continue;
			else if(player.getPlayer().getWorld().getUID() != tplayer.getPlayer().getWorld().getUID()) continue;
			
			if(nearest == null || player.getPlayer().getLocation().distanceSquared(nearest.getLocation()) >
					player.getPlayer().getLocation().distanceSquared(tplayer.getPlayer().getLocation())) {
				nearest = tplayer.getPlayer();
			}
		}
		return nearest;
	}

	public static void updatePlayersTracker() {
		for(TucPlayer tplayer : PlayersManager.getInstance().getGamers()) {
			Player nearest = Utils.getNearestEnnemy(tplayer);
			if(nearest != null) tplayer.getPlayer().setCompassTarget(nearest.getPlayer().getLocation());
			
			PlayerInventory inv = tplayer.getPlayer().getInventory();
			HashMap<Integer, ? extends ItemStack> compass = inv.all(Material.COMPASS);
			for(int slot : compass.keySet()) {
				ItemStack is = compass.get(slot);
				ItemMeta im = is.getItemMeta();
				
				if(nearest == null) {
					im.setDisplayName(Lang.get("ITEM_TRACKER_EMPTY"));
				}
				else {
					im.setDisplayName(Lang.get("ITEM_TRACKER")
							.replaceAll("<player>", nearest.getName())
							.replaceAll("<distance>", Integer.toString((int) tplayer.getPlayer().getLocation().distance(nearest.getLocation()))));
				}
				
				is.setItemMeta(im);
			}
		}
	}
	
	public static Player getPlayerByUUID(UUID uuid) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getUniqueId().equals(uuid)) return player;
		}
		return null;
	}
}
