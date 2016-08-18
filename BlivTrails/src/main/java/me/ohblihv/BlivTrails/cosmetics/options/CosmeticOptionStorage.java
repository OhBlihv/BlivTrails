package me.ohblihv.BlivTrails.cosmetics.options;

import me.ohblihv.BlivTrails.cosmetics.BaseCosmetic;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Chris Brown (OhBlihv) on 18/08/2016.
 */
public abstract class CosmeticOptionStorage
{
	
	private Map<CosmeticModifier, Double> modifierMap = new HashMap<>();
	
	public CosmeticOptionStorage(ConfigurationSection configurationSection)
	{
		//Invalid configuration section, cannot use.
		if(configurationSection == null || configurationSection.getKeys(false).isEmpty())
		{
			return;
		}
		
		for(CosmeticModifier cosmeticModifier : CosmeticModifier.values())
		{
			//If the default is not set in configuration, default to our hardcoded value
			double value = cosmeticModifier.defaultValue;
			if(configurationSection.contains(cosmeticModifier.configurationKey))
			{
				value = configurationSection.getDouble(cosmeticModifier.configurationKey);
			}
			//'Simple' Configuration (No Min/Max Provided)
			else if(configurationSection.contains(cosmeticModifier.configurationKey.split("[.]")[0]))
			{
				value = configurationSection.getDouble(cosmeticModifier.configurationKey.split("[.]")[0]);
			}
			//Else, use default value
			
			if(value <= 0 &&
				   (cosmeticModifier != CosmeticModifier.HEIGHT && cosmeticModifier != CosmeticModifier.HEIGHT_MIN && cosmeticModifier != CosmeticModifier.HEIGHT_MAX))
			{
				throw new IllegalArgumentException("Default value provided for '" + cosmeticModifier.name() + "' -> '" + value + "' is invalid." +
					                                   " Please use a value above 0 for this option.");
			}
			
			modifierMap.put(cosmeticModifier, value);
		}
	}
	
	public abstract void applyDefaults(BaseCosmetic baseCosmetic);
	
	public double getModifier(CosmeticModifier cosmeticModifier)
	{
		return modifierMap.get(cosmeticModifier);
	}
	
}
