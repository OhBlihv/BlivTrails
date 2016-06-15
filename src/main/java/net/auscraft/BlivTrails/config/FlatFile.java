package net.auscraft.BlivTrails.config;

import com.darkblade12.ParticleEffect.ParticleEffect;
import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlatFile
{

	protected File saveFile = null;
	protected FileConfiguration save = null;

	volatile static FlatFile instance = null;
	static JavaPlugin plugin = null;

	String fileName = "config.yml";

	public static FlatFile getInstance()
	{
		if(instance == null)
		{
			instance = new FlatFile();
		}
		return instance;
	}

	//Default to config.yml. Allow other filenames through the other constructor
	FlatFile()
	{
		this("config.yml");
	}

	FlatFile(String fileName)
	{
		this.fileName = fileName;
		plugin = BlivTrails.getInstance();
		saveDefaultConfig();
		getSave();
	}

	public FileConfiguration getSave()
	{
		if (save == null)
		{
			reloadFile();
		}
		return save;
	}

	public boolean reloadFile()
	{
		if (saveFile == null)
		{
			saveFile = new File(plugin.getDataFolder(), fileName);
		}
		save = YamlConfiguration.loadConfiguration(saveFile);

		// Look for defaults in the jar
		Reader defConfigStream = null;
		try
		{
			defConfigStream = new InputStreamReader(plugin.getResource(fileName), "UTF8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	    /*if (defConfigStream != null)
	    {
	        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
	        save.setDefaults(defConfig);
	    }*/
		boolean invalid = false;
		if (this.getBoolean("misc.config-checking"))
		{
			BUtil.logInfo("Checking Config...");
			invalid = checkConfig();
			BUtil.logInfo("Config Checked!");
		}
		return invalid;
	}

	public void saveDefaultConfig()
	{
		if (saveFile == null)
		{
			saveFile = new File(plugin.getDataFolder(), fileName);
		}
		if (!saveFile.exists())
		{
			plugin.saveResource(fileName, false);
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
			save.save(saveFile);
		}
		catch (IOException ex)
		{
			BUtil.logError("Could not save config to " + saveFile);
		}
	}

	public void removeEntry(String path)
	{
		save.set(path, null);
	}

	public void saveEntry(String path, String entry)
	{
		save.set(path, entry);
		saveToFile();
	}

	public String loadEntry(String path)
	{
		return save.getString(path);
	}

	public Set<String> getChildren(String path)
	{
		return save.getConfigurationSection(path).getKeys(false);
	}

	public String getString(String path)
	{
		return getString(path, "");
	}

	public String getString(String path, String defaultString)
	{
		return save.getString(path, defaultString);
	}

	public List<String> getStringList(String path)
	{
		return save.getStringList(path);
	}

	public int getInt(String path)
	{
		return save.getInt(path);
	}

	public boolean getBoolean(String path)
	{
		return getBoolean(path, false);
	}

	public boolean getBoolean(String path, boolean defaultBoolean)
	{
		return save.getBoolean(path, defaultBoolean);
	}

	public long getLong(String path)
	{
		return save.getLong(path);
	}

	public void saveValue(String path, Object value)
	{
		save.set(path, value);
		saveToFile();
	}

	public double getDouble(String path)
	{
		return save.getDouble(path);
	}

	public float getFloat(String path)
	{
		return (float) save.getDouble(path);
	}

	public ConfigurationSection getConfigurationSection(String path)
	{
		return save.getConfigurationSection(path);
	}

	public static final Location DEFAULT_LOCATION = new Location(Bukkit.getWorlds().get(0), 0, 128, 0);

	public static Location parseLocation(String locationString)
	{
		String[] locationSplit = locationString.split("[:]");
		try
		{
			double 	x = Double.parseDouble(locationSplit[0]),
				y = Double.parseDouble(locationSplit[1]),
				z = Double.parseDouble(locationSplit[2]);
			float 	yaw = 0, pitch = 90;
			if(locationSplit.length > 3)
			{
				yaw = Float.parseFloat(locationSplit[3]);
				pitch = Float.parseFloat(locationSplit[4]);
			}

			return new Location(Bukkit.getWorlds().get(0), x, y, z, yaw, pitch);
		}
		catch(NumberFormatException e)
		{
			//TODO:
			BUtil.logError("Invalid location given by '" + locationString + "'. Returning default location.");
		}

		return DEFAULT_LOCATION;
	}

	public Location getLocation(String path)
	{
		return parseLocation(save.getString(path));
	}

	public Location getLocation(ConfigurationSection configurationSection)
	{
		if(configurationSection == null || configurationSection.getKeys(false).isEmpty())
		{
			BUtil.logInfo("Configuration Section: " + (configurationSection == null ? "NULL" : configurationSection.getCurrentPath()) + " is NULL or empty!");
			return DEFAULT_LOCATION;
		}

		World world;
		if(configurationSection.contains("world"))
		{
			String worldName = configurationSection.getString("world", null);
			if(worldName != null && Bukkit.getWorld(worldName) != null)
			{
				world = Bukkit.getWorld(worldName);
			}
			else
			{
				BUtil.logError("World name '" + worldName + "' does not correspond to a valid world on this server. (" + configurationSection.getCurrentPath() + ")");
				return DEFAULT_LOCATION;
			}
		}
		else
		{
			world = Bukkit.getWorlds().get(0);
			BUtil.logError("No world defined in '" + configurationSection.getCurrentPath() + "'. Defaulting to " + world.getName() + ". Please define a world next time.");
		}
		double  x = configurationSection.getDouble("x", 0D),
			y = configurationSection.getDouble("y", 128D),
			z = configurationSection.getDouble("z", 0D);
		float   pitch = (float) configurationSection.getDouble("pitch", 0D),
			yaw = (float) configurationSection.getDouble("yaw", 0D);

		return new Location(world, x, y, z, yaw, pitch);
	}

	/*
	 * List/Map Operations
	 */

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getListMap(String path)
	{
		return (List<Map<String, Object>>) save.get(path);
	}

	public static String getString(Map<String, Object> configurationMap, String path, String def)
	{
		Object object = configurationMap.get(path);
		if(object != null && object instanceof String && !((String) object).isEmpty())
		{
			return (String) object;
		}
		return def;
	}

	public static List<String> getStringList(Map<String, Object> configurationMap, String path)
	{
		Object object = configurationMap.get(path);
		if(object != null && object instanceof List && !((List) object).isEmpty())
		{
			return (List<String>) object;
		}
		return new ArrayList<>();
	}

	public static int getInt(Map<String, Object> configurationMap, String path, int def)
	{
		Object object = configurationMap.get(path);
		if(object instanceof Integer)
		{
			return (Integer) object;
		}
		return def;
	}

	public static Material getMaterial(Map<String, Object> configurationMap, String path, Material def)
	{
		Object object = configurationMap.get(path);
		if(object instanceof String)
		{
			Material toReturn = Material.getMaterial((String) object);
			if(toReturn != null)
			{
				return toReturn;
			}

			BUtil.logError("Invalid material: " + object);
		}
		return def;
	}

	public boolean checkConfig()
	{
		boolean invalid = false;
		if ((getInt("menu.main.size") % 9) != 0)
		{
			BUtil.logError("Your Main Menu GUI is not a multiple of 9, and cannot be displayed. (Size: " + this.getInt("menu.main.size") + ")");
			invalid = true;
		}
		if ((getInt("menu.options.size") % 9) != 0)
		{
			BUtil.logError("Your Options Menu GUI is not a multiple of 9, and cannot be displayed. (Size: " + this.getInt("menu.options.size") + ")");
			invalid = true;
		}
		String particleString;
		for (ParticleEffect pEff : ParticleEffect.values()) // Check every particle effect used in the config
		{
			/*
			 * Currently checks: Position (If outside the acceptable range)
			 * Material Exists
			 */
			particleString = BUtil.trailConfigName(pEff.toString());
			if (!particleString.isEmpty())
			{
				if ((this.getInt("trails." + particleString + ".position") >= this.getInt("menu.main.size")) || (this.getInt("trails." + particleString + ".position") < 0))
				{
					BUtil.logError("Trail " + particleString + "'s location is outside the menu bounds: " + this.getInt("trails." + particleString + ".position"));
					invalid = true;
				}
				try
				{
					Material.getMaterial(this.getString("trails." + particleString + ".material")).isBlock();
				}
				catch (NullPointerException e)
				{
					BUtil.logError("Trail " + particleString + " has an invalid material: " + this.getString("trails." + particleString + ".material"));
					invalid = true;
				}
			}
		}
		checkGUIItem();

		BUtil.logInfo("Config Checking is enabled. If you encounter any freezes/stutters, turns this off once you've hit a stable config.");
		return invalid;
	}

	public void checkGUIItem()
	{
		boolean invalid = false;
		if ((this.getInt("misc.gui-item.position") >= 36) || (this.getInt("misc.gui-item.position") < 0))
		{
			BUtil.logError("The GUI Item's location is outside the inventory bounds: " + this.getString("misc.gui-item.position"));
			invalid = true;
		}
		try
		{
			Material.getMaterial(this.getString("misc.gui-item.material")).isBlock();
		}
		catch (NullPointerException e)
		{
			BUtil.logError("GUI Item has an invalid material: " + this.getString("misc.gui-item.material"));
			invalid = true;
		}

		if (!invalid && getBoolean("misc.enabled"))
		{
			((BlivTrails) plugin).doItemListener();
		}
	}

}
