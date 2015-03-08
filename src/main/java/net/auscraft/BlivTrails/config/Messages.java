package net.auscraft.BlivTrails.config;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import net.auscraft.BlivTrails.BlivTrails;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Messages 
{
	private File messagesFile = null;
	private FileConfiguration messages = null;
	private BlivTrails instance;
	
	public Messages(BlivTrails instance)
	{
		this.instance = instance;
		saveDefaultConfig();
		getMessages();
		messages.options().copyDefaults(true);
		doDefaults();
	}
	
	public void doDefaults() //Add in config options which were added in versions newer than 1.0
	{
		messages.addDefault("messages.titles.main-menu", "Trail GUI");
		messages.addDefault("messages.titles.main-options", "Trail Options");
		messages.addDefault("messages.titles.type", "Type Options");
		messages.addDefault("messages.titles.length", "Length Options");
		messages.addDefault("messages.titles.height", "Height Options");
		messages.addDefault("messages.titles.colours", "Colours Options");
	}
	
	public FileConfiguration getMessages() 
	{
	    if (messages == null)
	    {
	        reloadMessages();
	    }
	    return messages;
	}
	
	public void reloadMessages() 
	{
	    if (messagesFile == null) 
	    {
	    	messagesFile = new File(instance.getDataFolder(), "messages.yml");
	    }
	    messages = YamlConfiguration.loadConfiguration(messagesFile);
	 
	    // Look for defaults in the jar
	    Reader defConfigStream = null;
		try 
		{
			defConfigStream = new InputStreamReader(instance.getResource("messages.yml"), "UTF8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
	    if (defConfigStream != null)
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        messages.setDefaults(defConfig);
	    }
	}
	
	public void saveDefaultConfig() 
	{
	    if (messagesFile == null)
	    {
	        messagesFile = new File(instance.getDataFolder(), "messages.yml");
	    }
	    if (!messagesFile.exists())
	    {            
	         instance.saveResource("messages.yml", false);
	    }
	}

	public String getString(String path)
	{
		try
		{
			String value = translateColours(this.messages.getString(path));
			return value;
		}
		catch(NullPointerException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have a missing messages.yml entry at |" + path + "| Have you missed an update?");
			return "";
		}
	}
	
	public List<String> getStringList(String path)
	{
		List<String> values = translateColours(this.messages.getStringList(path));
		return values;
	}
	
	public static String translateColours(String toFix)
	{
		Pattern chatColourPattern = Pattern.compile("(?i)&([0-9A-Fa-fk-oK-OrR])");
		String fixedString = chatColourPattern.matcher(toFix).replaceAll("\u00A7$1");
		return fixedString;
	}
	
	public static List<String> translateColours(List<?> lines)
	{
		try
		{
			String[] lineString = null;
			if(lines.size() > 0)
			{
				lineString = lines.toArray(new String[lines.size()]);
			}
			else
			{
				return null;
			}
			for(int i = 0;i < lines.size();i++)
			{
				Pattern chatColourPattern = Pattern.compile("(?i)&([0-9A-Fa-fk-oK-OrR])");
				lineString[i] = chatColourPattern.matcher(lineString[i]).replaceAll("\u00A7$1");
			}
			return Arrays.asList(lineString);
		}
		catch(NullPointerException e)
		{
			return null;
		}
		
	}
	
}
