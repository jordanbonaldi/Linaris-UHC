package net.theuniverscraft.UHC.listeners;

import net.theuniverscraft.UHC.UHC;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public class Motd implements Listener {
	private static Motd instance;
	private String motd;
	
	public static void set(String motd) {
		if(instance == null) instance = new Motd();
		instance.motd = motd;
	}
	
	private Motd() {
		Bukkit.getPluginManager().registerEvents(this, UHC.getInstance());
	}
	
	@EventHandler
	public void onServerListPing(ServerListPingEvent event) {
		if(motd == null) return;
		event.setMotd(motd);
	}
}
