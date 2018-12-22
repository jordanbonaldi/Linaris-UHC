package net.theuniverscraft.UHC.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.theuniverscraft.UHC.UHC;
import net.theuniverscraft.UHC.managers.TeamsManager.TeamColor;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

public class WorldsManager {
	private World m_worldUHC;
	private World m_worldUHC_nether;
	
	private int m_worldLimit = 0;
	private int m_worldNetherLimit = 0;
	
	private Map<TeamColor, Platform> m_platform = new HashMap<TeamColor, Platform>();
	
	private static WorldsManager instance;
	public static WorldsManager getInstance() {
		if(instance == null) instance = new WorldsManager();
		return instance;
	}
	
	private WorldsManager() {
		WorldCreator options = new WorldCreator("world_uhc");
		m_worldUHC = options.createWorld();
				
		int y_spawn = m_worldUHC.getHighestBlockYAt(0, 0) + 2;
		m_worldUHC.setSpawnLocation(0, y_spawn, 0);
		m_worldLimit = UHC.getInt("map-radius");
		
		final int map_radius = UHC.getInstance().getConfig().getInt("map-radius");
				
		// LOAD DES CHUNKS
		// Load chunks
		int load_radius = Math.min(map_radius / 16 + 1, 33);
		for(int cx = -load_radius; cx <= load_radius; cx++) {
			for(int cz = -load_radius; cz <= load_radius; cz++) {
				Chunk chunk = m_worldUHC.getChunkAt(cx,  cz);
				if(!chunk.isLoaded()) chunk.load();
			}
		}
		// ---------------
		
		
		// ON GENERE EN AVANCE LE SPAWN DES JOUEURS POUR LOAD LES CHUNK EN QUESTION
		Random rand = new Random();	
		final double angle_to_add = 2.0D / TeamColor.valuesUsed().length * Math.PI;
		double angle = 0;
		
		for(TeamColor color : TeamColor.valuesUsed()) {
			Biome biome = Biome.OCEAN;
			
			double distance = 0;
			double x = 0;
			double z = 0;
			
			for(int i = 0; (biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN) && i < 5; i++) {
				distance = rand.nextInt((int) (map_radius / (2.0D + 0.25D*i))) + map_radius / 4.0D;
				x = Math.cos(angle) * distance;
				z = Math.sin(angle) * distance;
				biome = m_worldUHC.getBiome((int) x, (int) z);
			}
			
			Location loc = new Location(m_worldUHC, x, 255, z);
			loc.setY(m_worldUHC.getHighestBlockYAt(loc) + 50);
			if(loc.getBlockY() > 255) loc.setY(255);
				
			// ON CREER LA PLATFORM
			Platform platform = new Platform(5, loc);
			platform.setGlass();
			m_platform.put(color, platform);
			
			angle += angle_to_add;
		}
		
		// Nether y max = 128
		if(UHC.getBoolean("allow-nether")) {
			WorldCreator options_nether = new WorldCreator("world_uhc_nether");
			options_nether.environment(Environment.NETHER);
			m_worldUHC_nether = options_nether.createWorld();
			m_worldUHC_nether.setSpawnLocation(0, 50, 0);
		}
		m_worldNetherLimit = UHC.getInt("map-nether-radius");
	}
	
	public static World getWorldUHC() { return getInstance().m_worldUHC; }
	public static World getWorldNether() { return getInstance().m_worldUHC_nether; }
	
	public static void decrementeWall() {
		getInstance().m_worldLimit--;
		getInstance().m_worldNetherLimit--;
	}
	
	public static int getWall() { return getInstance().m_worldLimit; }
	
	public static boolean isOverLimit(Location location) {
		// Taille de la map
		int max_distance = location.getWorld().getEnvironment() == Environment.NETHER ? 
						getInstance().m_worldNetherLimit : getInstance().m_worldLimit;
		
		return Math.abs(location.getX()) > max_distance || Math.abs(location.getZ()) > max_distance;
	}
	
	public static int blocsToLimit(Location location) {
		// Taille de la map
		int max_distance = (location.getWorld().getEnvironment() == Environment.NETHER ? 
					getInstance().m_worldNetherLimit : getInstance().m_worldLimit);
		
		int max_coord = Math.max(Math.abs(location.getBlockX()), Math.abs(location.getBlockZ()));
		
		return max_distance - max_coord;
	}
	
	public static Platform getPlatform(TeamColor color) { return getInstance().m_platform.get(color); }
	
	public class Platform {
		private Location m_loc1;
		private Location m_loc2;
		private List<Location> m_spawns = new ArrayList<Location>();
		
		private Platform(int nbJoueur, Location base) {
			int longueur = 2*nbJoueur + 1;
			
			m_loc1 = new Location(base.getWorld(), 
					base.getBlockX() - nbJoueur,
					base.getBlockY(),
					base.getBlockZ() - 1);
			
			m_loc2 = new Location(base.getWorld(), 
					base.getBlockX() + nbJoueur,
					base.getBlockY(),
					base.getBlockZ() + 1);
			
			for(int i = 1; i < longueur; i += 2) {
				m_spawns.add(new Location(base.getWorld(), 
						m_loc1.getBlockX() + i + 0.5D, 
						base.getBlockY() + 1,
						base.getBlockZ() + 0.5D));
			}
		}
		
		public void setGlass() {
			for(int x = m_loc1.getBlockX(); x <= m_loc2.getBlockX(); x++) {
				for(int z = m_loc1.getBlockZ(); z <= m_loc2.getBlockZ(); z++) {
					m_worldUHC.getBlockAt(x, m_loc1.getBlockY(), z).setType(Material.GLASS);
				}
			}
		}
		
		public void breakGlass() {
			for(int x = m_loc1.getBlockX(); x <= m_loc2.getBlockX(); x++) {
				for(int z = m_loc1.getBlockZ(); z <= m_loc2.getBlockZ(); z++) {
					m_worldUHC.getBlockAt(x, m_loc1.getBlockY(), z).setType(Material.AIR);
				}
			}
			
			for(Player player : Bukkit.getOnlinePlayers()) {
				player.playSound(player.getLocation(), Sound.GLASS, 1.0F, 1.0F);
			}
		}
		
		public Location getSpawn(int index) {
			return m_spawns.get(index);
		}
	}
	
	public enum MapShape {
		CIRCLE,
		SQUARE;
		
		public static MapShape getMapShape(String str) {
			try {
				MapShape shape = MapShape.valueOf(str);
				return shape;
			} catch(Exception e) { return CIRCLE; }
		}
	}
}
