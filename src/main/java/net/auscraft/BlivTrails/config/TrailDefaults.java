package net.auscraft.BlivTrails.config;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.auscraft.BlivTrails.OptionType;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.concurrent.ConcurrentHashMap;

public class TrailDefaults
{
	// Will hold the trail defaults
	@AllArgsConstructor
	public static class ParticleDefaultStorage
	{
		/*
		 * Defaults: type length height random.x-variation random.y-variation
		 * random.z-variation dynamic.spray-variation height.feet-location
		 * height.waist-location height.halo-location
		 */
		@Getter
		private String displayName;

		@Getter
		private OptionType  type,
							length,
							height;

		@Getter
		private int     colour,
						displaySpeed;

		@Getter
		private double  xVariation,
						yVariation,
						zVariation,
						sprayVariation,
						feetLocation,
						waistLocation,
						haloLocation;

	}

	private static TrailDefaults instance = null;

	@Getter
	private static ConcurrentHashMap<ParticleEffect, ParticleDefaultStorage> particleDefaults = new ConcurrentHashMap<>();

	private static ParticleDefaultStorage defaultParticleOptions = null;
	
	public static TrailDefaults getInstance()
	{
		if(instance == null)
		{
			instance = new TrailDefaults();
		}
		return instance;
	}
	
	private TrailDefaults()
	{
		FlatFile cfg = FlatFile.getInstance();

		defaultParticleOptions = new ParticleDefaultStorage("", OptionType.TYPE_TRACE, OptionType.LENGTH_SHORT, OptionType.HEIGHT_FEET,
		                                                    0, 1,
		                                                    TrailManager.getOption()[0],
		                                                    TrailManager.getOption()[1],
		                                                    TrailManager.getOption()[2],
		                                                    TrailManager.getOption()[3],
		                                                    TrailManager.getOption()[4],
		                                                    TrailManager.getOption()[5],
		                                                    TrailManager.getOption()[6]);

		String particleString;
		ConfigurationSection trailSection = cfg.getConfigurationSection("trails");
		for (ParticleEffect particle : TrailManager.usedTrails)
		{
			particleString = BUtil.trailConfigName(particle.name());
			if (!particleString.isEmpty())
			{
				ConfigurationSection trailDefaultSection = trailSection.getConfigurationSection(particleString);
				int particleColour = 15;
				if(particle.hasProperty(ParticleProperty.COLORABLE))
				{
					switch(particle)
					{
						case DRAGON_BREATH:
						{
							particleColour = 13;
							break;
						}
						case REDSTONE:
						{
							particleColour = 1;
							break;
						}
						default:
						{
							OptionType.parseColourString(trailDefaultSection.getString("options.colour"));
							break;
						}

					}
				}

				particleDefaults.put(particle,
				                     new ParticleDefaultStorage(BUtil.translateColours(trailDefaultSection.getString("name")),
				                                                OptionType.parseTypeString(trailDefaultSection.getString("options.type")),
				                                                OptionType.parseLengthString(trailDefaultSection.getString("options.length")),
				                                                OptionType.parseHeightString(trailDefaultSection.getString("options.height")),
				                                                particleColour,
				                                                trailDefaultSection.getInt("options.display-speed"),
				                                                trailDefaultSection.getInt("options.defaults.random.x-variation"),
				                                                trailDefaultSection.getInt("options.defaults.random.y-variation"),
				                                                trailDefaultSection.getInt("options.defaults.random.z-variation"),
				                                                trailDefaultSection.getInt("options.defaults.dynamic.spray-variation"),
				                                                trailDefaultSection.getInt("options.defaults.height.feet-location"),
				                                                trailDefaultSection.getInt("options.defaults.height.waist-location"),
				                                                trailDefaultSection.getInt("options.defaults.height.halo-location")));
			}
		}
	}

	public static ParticleDefaultStorage getDefaults(ParticleEffect particle)
	{
		ParticleDefaultStorage particleDefaultOptions = particleDefaults.get(particle);
		if(particleDefaultOptions == null)
		{
			return defaultParticleOptions;
		}
		return particleDefaultOptions;
	}

}
