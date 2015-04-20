package net.auscraft.BlivTrails.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import net.auscraft.BlivTrails.BlivTrails;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class FlatFile
{

	private File saveFile = null;
	private FileConfiguration save = null;
	private BlivTrails instance;

	public FlatFile(BlivTrails instance)
	{
		this.instance = instance;
		saveDefaultConfig();
		getSave();
	}

	public FileConfiguration getSave()
	{
		if (save == null)
		{
			reloadMessages();
		}
		return save;
	}

	public void reloadMessages()
	{
		if (saveFile == null)
		{
			saveFile = new File(instance.getDataFolder(), "trails.yml");
		}
		save = YamlConfiguration.loadConfiguration(saveFile);

		// Look for defaults in the jar
		Reader defConfigStream = null;
		try
		{
			defConfigStream = new InputStreamReader(instance.getResource("trails.yml"), "UTF8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		if (defConfigStream != null)
		{
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			save.setDefaults(defConfig);
		}
	}

	public void saveDefaultConfig()
	{
		if (saveFile == null)
		{
			saveFile = new File(instance.getDataFolder(), "trails.yml");
		}
		if (!saveFile.exists())
		{
			instance.saveResource("trails.yml", false);
		}
	}

	public void saveToFile()
	{
		if (save == null || saveFile == null)
		{
			return;
		}
		try
		{
			getSave().save(saveFile);
		}
		catch (IOException ex)
		{
			instance.getLogger().log(Level.SEVERE, "Could not save config to " + saveFile, ex);
		}
	}

	public void removeEntry(String path)
	{
		this.getSave().set(path, null);
	}

	public void saveEntry(String path, String entry)
	{
		this.getSave().set(path, entry);
		saveToFile();
	}

	public String loadEntry(String path)
	{
		String value = this.save.getString(path);
		return value;
	}

}
