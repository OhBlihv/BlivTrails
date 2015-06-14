package net.auscraft.BlivTrails.config;

import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import net.auscraft.BlivTrails.utils.Utilities;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import com.darkblade12.ParticleEffect.ParticleEffect;

public class ConfigAccessor extends FlatFile
{

	protected static ConfigAccessor instance;
	
	public static ConfigAccessor getInstance()
	{
		if(instance == null)
		{
			fileName = "config.yml";
			instance = new ConfigAccessor();
		}
		return instance;
	}
	
	private ConfigAccessor()
	{
		super();
	}
	
	public boolean reloadConfig()
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
		if (defConfigStream != null)
		{
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			save.setDefaults(defConfig);
		}
		boolean invalid = false;
		if (this.getBoolean("misc.config-checking"))
		{
			Utilities.logInfo("Checking Config...");
			invalid = checkConfig();
			Utilities.logInfo("Config Checked!");
		}
		return invalid;
	}

	public boolean checkConfig()
	{
		boolean invalid = false;
		if ((getInt("menu.main.size") % 9) != 0)
		{
			Utilities.logError("Your Main Menu GUI is not a multiple of 9, and cannot be displayed. (Size: " + this.getInt("menu.main.size") + ")");
			invalid = true;
		}
		if ((getInt("menu.options.size") % 9) != 0)
		{
			Utilities.logError("Your Options Menu GUI is not a multiple of 9, and cannot be displayed. (Size: " + this.getInt("menu.options.size") + ")");
			invalid = true;
		}
		String particleString = "";
		for (ParticleEffect pEff : ParticleEffect.values()) // Check every particle effect used in the config
		{
			/*
			 * Currently checks: Position (If outside the acceptable range)
			 * Material Exists
			 */
			particleString = Utilities.trailConfigName(pEff.toString());
			if (particleString.length() != 0)
			{
				if ((this.getInt("trails." + particleString + ".position") >= this.getInt("menu.main.size")) || (this.getInt("trails." + particleString + ".position") < 0))
				{
					Utilities.logError("Trail " + particleString + "'s location is outside the menu bounds: " + this.getInt("trails." + particleString + ".position"));
					invalid = true;
				}
				try
				{
					Material.getMaterial(this.getString("trails." + particleString + ".material")).isBlock();
				}
				catch (NullPointerException e)
				{
					Utilities.logError("Trail " + particleString + " has an invalid material: " + this.getString("trails." + particleString + ".material"));
					invalid = true;
				}
			}
		}
		checkGUIItem();

		Utilities.logInfo("Config Checking is enabled. If you encounter any freezes/stutters, turns this off once you've hit a stable config.");
		return invalid;
	}

	public void checkGUIItem()
	{
		boolean invalid = false;
		if ((this.getInt("misc.gui-item.position") >= 36) || (this.getInt("misc.gui-item.position") < 0))
		{
			Utilities.logError("The GUI Item's location is outside the inventory bounds: " + this.getString("misc.gui-item.position"));
			invalid = true;
		}
		try
		{
			Material.getMaterial(this.getString("misc.gui-item.material")).isBlock();
		}
		catch (NullPointerException e)
		{
			Utilities.logError("GUI Item has an invalid material: " + this.getString("misc.gui-item.material"));
			invalid = true;
		}

		if (!invalid && getBoolean("misc.enabled"))
		{
			plugin.doItemListener();
		}
	}
}
