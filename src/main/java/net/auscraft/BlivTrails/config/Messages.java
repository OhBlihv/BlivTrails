package net.auscraft.BlivTrails.config;

import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Messages extends FlatFile
{
	private static Messages instance = null;
	public static Messages getInstance()
	{
		if(instance == null)
		{
			instance = new Messages();
		}
		return instance;
	}

	private Messages()
	{
		super("messages.yml");

		save.options().copyDefaults(true);
		doDefaults();
	}

	public void doDefaults() // Add in config options which were added in versions newer than 1.0
	{
		save.addDefault("messages.titles.main-menu", "Trail GUI");
		save.addDefault("messages.titles.main-options", "Trail Options");
		save.addDefault("messages.titles.type", "Type Options");
		save.addDefault("messages.titles.length", "Length Options");
		save.addDefault("messages.titles.height", "Height Options");
		save.addDefault("messages.titles.colours", "Colours Options");
	}

	public String getString(String path)
	{
		try
		{
			return BUtil.translateColours(super.getString(path));
		}
		catch (NullPointerException e)
		{
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "You have a missing messages.yml entry at |" + path + "| Have you missed an update?");
			return "";
		}
	}

}
