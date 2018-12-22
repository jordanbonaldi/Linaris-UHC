package net.theuniverscraft.UHC.listeners;

import net.theuniverscraft.UHC.UHC;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldListener implements Listener {
	private boolean m_ignoreNextWeatherChange = false;

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		if(chunk.getWorld().getName().equalsIgnoreCase("world_uhc")) {
			Location loc1 = chunk.getBlock(0, 60, 0).getLocation();
			Location loc2 = chunk.getBlock(15, 60, 15).getLocation();
			
			final int map_radius = UHC.getInt("map-radius");
			
			if(!(Math.abs(loc1.getX()) <= map_radius || Math.abs(loc1.getZ()) <= map_radius || 
					Math.abs(loc2.getX()) <= map_radius || Math.abs(loc2.getZ()) <= map_radius)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		if(m_ignoreNextWeatherChange) {
			m_ignoreNextWeatherChange = false;
			return;
		}
		
		if(!event.getWorld().hasStorm()) { // Il n'y avais deja pas de pluie, on conserve
			event.setCancelled(true);
		}
		else {
			m_ignoreNextWeatherChange  = true;
			event.getWorld().setStorm(false);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(!event.getLocation().getWorld().getName().equalsIgnoreCase("world_uhc")) event.setCancelled(true);
	}
}