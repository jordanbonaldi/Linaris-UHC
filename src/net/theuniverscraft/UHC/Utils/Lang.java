package net.theuniverscraft.UHC.Utils;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.theuniverscraft.UHC.UHC;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
	//////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// REFONTE DU SYSTEME /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	private Map<String, String> m_lang = new LinkedHashMap<String, String>();
	private File m_file;
	private FileConfiguration m_yaml;
	
	private static Lang instance = null;
	public static String get(String key) {
		if(instance == null) instance = new Lang();
		
		if(!instance.m_lang.containsKey(key)) {
			System.out.println("Key \""+key+"\" unfound !");
			return null;
		}
		
		return ChatColor.translateAlternateColorCodes('&', instance.m_lang.get(key)
				.replaceAll("<egrave>", "è")
				.replaceAll("<eacute>", "é")
				.replaceAll("<ecirc>", "ê")
				.replaceAll("<euml>", "ë")
				.replaceAll("<uacute>", "ù")
				.replaceAll("<agrave>", "à"));
	}
	
	private Lang() {
		try {
			File dirs = new File("plugins/" + UHC.getInstance().getName());
			dirs.mkdirs();
			
			/* 
			 * §0 Black
			 * §1 Dark Blue
			 * §2 Dark Green
			 * §3 Dark Aqua
			 * §4 Dark Red
			 * §5 Dark Purple
			 * §6 Gold
			 * §7 Gray
			 * §8 Dark Gray
			 * §9 Blue
			 * §a Green
			 * §b Aqua
			 * §c Red
			 * §d Light Purple
			 * §e Yellow
			 * §f White 
			 * */
			
			// Valeurs par defaut
			m_lang.put("TEAM_NAME_RED", "red");
			m_lang.put("TEAM_NAME_GREEN", "green");
			m_lang.put("TEAM_NAME_YELLOW", "yellow");
			m_lang.put("TEAM_NAME_BLUE", "blue");
			m_lang.put("TEAM_NAME_ORANGE", "orange");
			m_lang.put("TEAM_NAME_PURPLE", "purple");
			m_lang.put("TEAM_NAME_MAGENTA", "magenta");
			m_lang.put("TEAM_NAME_LIGHT_BLUE", "light blue");
			m_lang.put("TEAM_NAME_GRAY", "gray");
			
			m_lang.put("MSG_MUST_BE_PLAYER", "Vous devez <ecirc>tre connect<eacute> en jeu !");
			m_lang.put("MSG_MUST_BE_ADMIN", "&cVous devez <ecirc>tre administarteur");
			m_lang.put("MSG_UNFOUND_PLAYER", "&cLe joueur <player> n'est pas connecter !");
			m_lang.put("MSG_YOU_CANT_CHAT_WITH_GAMERS", "&cVous ne pouvez pas parler avec les joueurs");
			
			m_lang.put("SECOND_SINGULAR", "seconde");
			m_lang.put("SECOND_PLURAL", "secondes");
			
			m_lang.put("TABLIST_NAME", "[<team_color><team>&r] <player>");
			m_lang.put("TABLIST_NAME_NO_TEAM", "<player>");
			
			m_lang.put("MOTD_LOADING", "Chargement...");
			m_lang.put("MOTD_LOADING_PERCENT", "Chargement (<percent>%)");
			m_lang.put("MOTD_CONFIG", "Configuration...");
			m_lang.put("MOTD_LOBBY_WAIT", "Attente de joueurs !");
			m_lang.put("MOTD_LOBBY_TIME", "Debut dans <sec> <SECOND>");
			m_lang.put("MOTD_GAME", "&cGame started !");
			
			m_lang.put("KICK_GAME_FULL", "La partie est complete !");
			m_lang.put("KICK_GAME_START", "La partie est d<eacute>j<agrave> commencer !");
			m_lang.put("KICK_LOOSE", "Vous avez perdu !");
			m_lang.put("KICK_YOU_HAVE_WIN", "Votre team a gagn<eacute> !");
			m_lang.put("KICK_TEAM_HAVE_WIN", "La team <team_color><team>&r a gagner !");
			
			m_lang.put("TOO_MUCH_PLAYER_IN_TEAM", "&cIl y a trop de joueur dans la team");
			m_lang.put("YOU_HAVE_JOIN_TEAM", "&6Vous avez rejoint la team <team_color><team>&6 !");
			
			m_lang.put("START_IN", "&6La partie commence dans &b<sec> &6<SECOND>");
			m_lang.put("START", "&6La partie commence !");
			
			m_lang.put("DEGAT_ENABLE", "&cVous n'<ecirc>tes plus invincible !");
			m_lang.put("OVER_PORTAL", "&cVous <ecirc>tes derri<egrave>re la limite");
			m_lang.put("LIMIT_ARROUND", "&c/!\\ Vous etes a <x> blocs de la bordure, éloignez vous /!\\");
			
			m_lang.put("END_EPISODE", "&b--------- Fin de l'<eacute>pisode <x> ---------");
			
			m_lang.put("CHAT_LOBBY", "<<player>> <message>");
			m_lang.put("CHAT_GLOBAL", "[<team_color><team>&r] <<player>> <message>");
			m_lang.put("CHAT_TEAM", "[<team_color>Team&r] <<player>> <message>");
			m_lang.put("CHAT_SPECTATORS", "[Spectateur] <<player>> <message>");
			
			m_lang.put("CHAT_PRIVATE_SEND", "[Priv<eacute>e] To <receiver> : <message>");
			m_lang.put("CHAT_PRIVATE_RECEIVE", "[Priv<eacute>e] From <receiver> : <message>");
			
			m_lang.put("JOIN_MESSAGE", "&6<player> &e<agrave> rejoint la partie");
			m_lang.put("QUIT_MESSAGE", "&6<player> &e<agrave> quitter la partie");
			m_lang.put("DEATH_MESSAGE", "&6<player> &7est mort");
			
			m_lang.put("OBJECTIVE_NAME", "UHC");
			m_lang.put("OBJECTIVE_SPECTATOR_NAME", "&6Spectateur");
			m_lang.put("OBJECTIVE_PLAYER_NAME", "[<team_color><team>&r]");
			
			m_lang.put("OBJECTIVE_LOBBY_PLAYERS", "Joueurs &a<x>/<max>");
			m_lang.put("OBJECTIVE_LOBBY_WAIT", "Attente");
			m_lang.put("OBJECTIVE_LOBBY_START", "Debut dans &a<sec>s");
			
			m_lang.put("OBJECTIVE_GAME_EPISODE", "&7Episode &r<x>");
			m_lang.put("OBJECTIVE_GAME_PLAYERS", "<x> &7joueurs");
			m_lang.put("OBJECTIVE_GAME_TEAMS", "<x> &7teams");
			m_lang.put("OBJECTIVE_GAME_CENTRE", "&6Centre: &e<x>");
			m_lang.put("OBJECTIVE_GAME_BORDER", "&6Border: &e<x>");
			m_lang.put("OBJECTIVE_GAME_TIME_PVP", "&7Pvp: &e<min>:<sec>");
			m_lang.put("OBJECTIVE_GAME_TIME_MUR", "&7Mur: &e<min>:<sec>");
			m_lang.put("OBJECTIVE_GAME_TIME_JEU", "&7Jeu: &e<min>:<sec>");
			
			m_lang.put("ITEM_CHOOSE_TEAM", "&b&lChoisir une team");
			m_lang.put("ITEM_SPECTATOR_COMPASS", "&bVoir un joueur");
			m_lang.put("ITEM_SPECTATOR_LOBBY", "&6Retournez au lobby");
			m_lang.put("ITEM_TRACKER", "&b<player> &r- &6<distance>&bm");
			m_lang.put("ITEM_TRACKER_EMPTY", "Boussole");
			m_lang.put("CHATALLCOMMAND","&a&lMettez un &e&l! &a&ldevant vos phrases pour parler en chat public.");
			
			m_lang.put("TEAM_WIN", "&bLes <team_color><team> &bon gagn<eacute>s");
			
			
			if(!UHC.DEV_MODE) {
				m_file = new File("plugins/" + UHC.getInstance().getName() + "/lang.yml");
				if(!m_file.exists()) m_file.createNewFile();
				m_yaml = YamlConfiguration.loadConfiguration(m_file);
				
				Map<String, String> tmpLang = new LinkedHashMap<String, String>();
				
				Iterator<String> i = m_lang.keySet().iterator();
				while (i.hasNext()){
					String key = i.next();
					if(m_yaml.contains(key)) {
						tmpLang.put(key, m_yaml.getString(key));
					}
					else {
						m_yaml.set(key, m_lang.get(key));
						tmpLang.put(key, m_lang.get(key));
					}
				}
				
				m_lang = tmpLang;
				
				m_yaml.save(m_file);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}
