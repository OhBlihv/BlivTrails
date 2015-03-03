package net.auscraft.BlivTrails;

import java.util.regex.Pattern;

import net.auscraft.BlivTrails.config.ConfigAccessor;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Utilities 
{
	
	private BlivTrails plugin;
	private ConfigAccessor cfg;
	private final String prefix = ChatColor.WHITE + "[" + ChatColor.DARK_AQUA + "BlivTrails" + ChatColor.WHITE + "] ";
	
	public Utilities(BlivTrails instance)
	{
		plugin = instance;
		cfg = instance.getCfg();
	}
	
	//------------------------------------------------------------------------------------------------------
	//String Translation
	//------------------------------------------------------------------------------------------------------
	
	public String translateColours(String toFix)
	{
		//Convert every single colour code and formatting code, excluding 'magic' (&k), capitals and lowercase are converted.
		Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-Fa-f-l-oL-OrR])"); // Credit to t3hk0d3 in ChatManager(With slight edits)
		String fixedString = chatColorPattern.matcher(toFix).replaceAll("\u00A7$1"); // And here too
		return fixedString;
	}
	
	//------------------------------------------------------------------------------------------------------
		//Printing
		//------------------------------------------------------------------------------------------------------
		
		public void printSuccess(CommandSender sender, String message)
		{
			sender.sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "SUCCESS: " + ChatColor.GREEN + message);
		}
		
		public void printPlain(CommandSender sender, String message)
		{
			sender.sendMessage(message);
		}
		
		public void printInfo(CommandSender sender, String message)
		{
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "INFO: " + ChatColor.BLUE + message);
		}
		
		public void printError(CommandSender sender, String message)
		{
			sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "ERROR: " + ChatColor.RED + message);
		}
		
		//------------------------------------------------------------------------------------------------------
		//Broadcasting
		//------------------------------------------------------------------------------------------------------
		
		public void broadcastPlain(String message)
		{
			Bukkit.broadcastMessage(message);
		}
		
		
		//------------------------------------------------------------------------------------------------------
		//Logging
		//------------------------------------------------------------------------------------------------------
		
		public void logSuccess(String message)
		{
			//b.getServer().getConsoleCommandSender().sendMessage(ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "SUCCESS: " + ChatColor.GREEN + message);
			plugin.getServer().getConsoleSender().sendMessage(prefix + ChatColor.DARK_GREEN + "SUCCESS: " + ChatColor.GREEN + message);
		}
		
		public void logPlain(String message)
		{
			plugin.getServer().getConsoleSender().sendMessage(prefix + message);
		}
		
		public void logInfo(String message)
		{
			plugin.getServer().getConsoleSender().sendMessage(prefix + ChatColor.DARK_AQUA + "" + "INFO: " + ChatColor.BLUE + message);
		}
		
		public void logError(String message)
		{
			plugin.getServer().getConsoleSender().sendMessage(prefix + ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + message);
		}
		
		public void logDebug(String message)
		{
			if(cfg.getBoolean("misc.debug"))
			{
				plugin.getServer().getConsoleSender().sendMessage(prefix + ChatColor.GREEN + "DEBUG: " + ChatColor.RED + message);
			}
		}
		
		public void logSevere(String message)
		{
			plugin.getServer().getConsoleSender().sendMessage(prefix + ChatColor.DARK_RED + "SEVERE: " + ChatColor.RED + message);
		}
		
		//------------------------------------------------------------------------------------------------------
		//Miscellaneous
		//------------------------------------------------------------------------------------------------------
		public BlivTrails getInstance()
		{
			return plugin;
		}
	
}
