package net.auscraft.BlivTrails.config;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import net.auscraft.BlivTrails.BlivTrails;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigAccessor 
{

	private File configFile = null;
	private FileConfiguration config = null;
	private BlivTrails instance;
	
	public ConfigAccessor(BlivTrails instance)
	{
		this.instance = instance;
		saveDefaultConfig();
		getMessages();
	}
	
	public FileConfiguration getMessages() 
	{
	    if (config == null)
	    {
	        reloadMessages();
	    }
	    return config;
	}
	
	public void reloadMessages() 
	{
	    if (configFile == null) 
	    {
	    	configFile = new File(instance.getDataFolder(), "config.yml");
	    }
	    config = YamlConfiguration.loadConfiguration(configFile);
	 
	    // Look for defaults in the jar
	    Reader defConfigStream = null;
		try 
		{
			defConfigStream = new InputStreamReader(instance.getResource("config.yml"), "UTF8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
	    if (defConfigStream != null)
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        config.setDefaults(defConfig);
	    }
	}
	
	public void saveDefaultConfig() 
	{
	    if (configFile == null)
	    {
	        configFile = new File(instance.getDataFolder(), "config.yml");
	    }
	    if (!configFile.exists())
	    {            
	         instance.saveResource("messages.yml", false);
	    }
	}
	
	public int getInt(String path)
	{
		int value = this.config.getInt(path);
		return value;
	}
	
	public double getDouble(String path)
	{
		double value = this.config.getDouble(path);
		return value;
	}

	public String getString(String path)
	{
		String value = this.config.getString(path);
		return value;
	}

	public boolean getBoolean(String path)
	{
		boolean value = this.config.getBoolean(path);
		return value;
	}
	
	public List<String> getStringList(String path)
	{
		List<String> values = this.config.getStringList(path);
		return values;
	}
	
}
