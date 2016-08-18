package me.ohblihv.BlivTrails.cosmetics.options;

import me.ohblihv.BlivTrails.cosmetics.BaseCosmetic;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by Chris Brown (OhBlihv) on 18/08/2016.
 */
public class BlankOptionStorage extends CosmeticOptionStorage
{
	
	public BlankOptionStorage(ConfigurationSection configurationSection)
	{
		super(null);
	}
	
	@Override
	public void applyDefaults(BaseCosmetic baseCosmetic)
	{
		// Blank
	}
	
}
