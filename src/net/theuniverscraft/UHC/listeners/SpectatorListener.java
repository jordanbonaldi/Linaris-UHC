package net.theuniverscraft.UHC.listeners;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.Utils.Lang;
import net.theuniverscraft.UHC.Utils.Utils;
import net.theuniverscraft.UHC.managers.PlayersManager;
import net.theuniverscraft.UHC.managers.PlayersManager.TucPlayer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class SpectatorListener implements Listener {
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		ItemStack inHand = player.getItemInHand();
		
		if(inHand == null) return;		
		
		if(tplayer.isSpectator()) {
			if(inHand.getType() == Material.COMPASS) {
				player.openInventory(Utils.getInvSpectatorTp(player));
			}
			
			if(UHC.getBoolean("enable-bungeecord") && inHand.getType() == Material.BED) {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Connect");
				out.writeUTF(UHC.getString("bungeecord-lobby"));
				player.sendPluginMessage(UHC.getInstance(), "BungeeCord", out.toByteArray());
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		final Player player = (Player) event.getWhoClicked();
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		
		if(!tplayer.isSpectator()) return;
		
		Inventory inv = event.getInventory();
				
		try {
			if(event.getCurrentItem().getType() == Material.COMPASS) {
				player.closeInventory();
				Bukkit.getScheduler().scheduleSyncDelayedTask(UHC.getInstance(), new Runnable() {
					@Override
					public void run() {
						player.openInventory(Utils.getInvSpectatorTp(player));
					}
				}, 10L);
			}
			else if(event.getCurrentItem().getType() == Material.BED) {
				Utils.tpToLobby(player);
			}
			else if(inv.getName().equalsIgnoreCase(ChatColor.stripColor(Lang.get("ITEM_SPECTATOR_COMPASS")))) {
				ItemStack is = event.getCurrentItem();
				if(is.getType() == Material.SKULL_ITEM) {
					SkullMeta meta = (SkullMeta) is.getItemMeta();
					Player to = Bukkit.getPlayer(meta.getOwner());
					if(to != null) player.teleport(to);
					player.closeInventory();
				}
			}
		} catch(NullPointerException e) {}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
		
		if(tplayer.isSpectator()) tplayer.setSpectator();
	}
	
	
	
	// SPECTATOR BASIC
	
	// ------ JUST IMPLEMENTS THAT : ----
	public boolean isSpectator(Player player) {
		return PlayersManager.getInstance().getPlayer(player).isSpectator();
	}
	// -------
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(isSpectator(event.getPlayer())) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if(isSpectator(event.getPlayer())) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
		if(isSpectator(event.getPlayer())) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		if(isSpectator(event.getPlayer())) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if(isSpectator(event.getPlayer())) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		if(isSpectator(event.getPlayer())) event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(event.getEntity() instanceof Player) {
			if(isSpectator((Player) event.getEntity())) event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player) {
			if(isSpectator((Player) event.getDamager())) event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			if(isSpectator((Player) event.getEntity())) event.setCancelled(true);
		}
	}
}
