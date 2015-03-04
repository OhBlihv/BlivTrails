package net.auscraft.BlivTrails.config;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.Utilities;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.darkblade12.ParticleEffect.ParticleEffect;

public class ConfigAccessor 
{

	private File configFile = null;
	private FileConfiguration config = null;
	private BlivTrails instance;
	private Utilities util;
	
	public ConfigAccessor(BlivTrails instance)
	{
		this.instance = instance;
		this.util = instance.getUtil();
		saveDefaultConfig();
		getMessages();
	}
	
	public boolean checkConfig()
	{
		boolean invalid = false;
		if((this.getInt("menu.main.size") % 9) != 0)
		{
			util.logError("Your Main Menu GUI is not a multiple of 9, and cannot be displayed. (Size: " + this.getInt("menu.main.size") + ")");
			invalid = true;
		}
		if((this.getInt("menu.options.size") % 9) != 0)
		{
			util.logError("Your Options Menu GUI is not a multiple of 9, and cannot be displayed. (Size: " + this.getInt("menu.options.size") + ")");
			invalid = true;
		}
		String particleString = "";
		for(ParticleEffect pEff : ParticleEffect.values()) //Check every particle effect used in the config
		{
			/*
			 * Currently checks:
			 * Position (If outside the acceptable range)
			 * Material Exists
			 */
			particleString = util.trailConfigName(pEff.toString());
			if(!particleString.equals("NULL"))
			{
				if((this.getInt("trails." + particleString + ".position") >= this.getInt("menu.main.size")) || (this.getInt("trails." + particleString + ".position") < 0))
				{
					util.logError("Trail " + particleString + "'s location is outside the menu bounds: " + this.getInt("trails." + particleString + ".position"));
					invalid = true;
				}
				try
				{
					Material.getMaterial(this.getString("trails." + particleString + ".material")).isBlock();
				}
				catch(NullPointerException e)
				{
					util.logError("Trail " + particleString + " has an invalid material: " + this.getString("trails." + particleString + ".material"));
					invalid = true;
				}
			}
		}
		util.logInfo("Config Checking is enabled. If you encounter any freezes/stutters, turns this off once you've hit a stable config.");
		return invalid;
	}
	
	public void addDefaults() //TODO:
	{
		/*String particleString = "";
		for(ParticleEffect pEff : ParticleEffect.values()) //Check every particle effect used in the config
		{
			particleString = util.trailConfigName(pEff.toString());
			if(!particleString.equals("NULL"))
			{
				config.addDefault("", value);
			}
		}*/
		config.addDefault("misc.debug", false);
		config.addDefault("misc.config-checking", true);
	}
	
	public FileConfiguration getMessages() 
	{
	    if (config == null)
	    {
	        reloadMessages();
	    }
	    return config;
	}
	
	public boolean reloadMessages() 
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
	    boolean invalid = false;
	    if(this.getBoolean("misc.config-checking"))
		{
			util.logInfo("Checking Config...");
			invalid = checkConfig();
			util.logInfo("Config Checked!");
		}
	    return invalid;
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
