package net.auscraft.BlivTrails.config;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.util.BUtil;

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
		private int type,
					length,
					height,
					colour,
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
	
	public static TrailDefaults getInstance()
	{
		if(instance == null)
		{
			instance = new TrailDefaults();
		}
		return instance;
	}

	@Getter
	private static int defaultSpeed = 1;
	
	private TrailDefaults()
	{
		FlatFile cfg = FlatFile.getInstance();

		defaultSpeed = cfg.getInt("trails.defaults.display-speed");

		String particleString;
		for (ParticleEffect particle : TrailManager.usedTrails)
		{
			particleString = BUtil.trailConfigName(particle.toString());
			if (!particleString.isEmpty())
			{
				particleString = BUtil.trailConfigName(particleString);
				
				particleDefaults.put(particle,
				                     new ParticleDefaultStorage(BUtil.translateColours(cfg.getString("trails." + particleString + ".name")),
						                                        typeStringtoInt(cfg.getString("trails." + particleString + ".options.type")),
				                                                lengthStringtoInt(cfg.getString("trails." + particleString + ".options.length")), heightStringtoInt(cfg.getString("trails." + particleString + ".options.height")),
				                                                particle.hasProperty(ParticleProperty.COLORABLE) ? colourStringtoInt(cfg.getString("trails." + particleString + ".options.colour")) : 15,
				                                                cfg.getInt("trails." + particleString + ".options.display-speed"),
				                                                cfg.getDouble("trails." + particleString + ".options.defaults.random.x-variation"),
				                                                cfg.getDouble("trails." + particleString + ".options.defaults.random.y-variation"), cfg.getDouble("trails." + particleString + ".options.defaults.random.z-variation"),
				                                                cfg.getDouble("trails." + particleString + ".options.defaults.dynamic.spray-variation"), cfg.getDouble("trails." + particleString + ".options.defaults.height.feet-location"),
				                                                cfg.getDouble("trails." + particleString + ".options.defaults.height.waist-location"), cfg.getDouble("trails." + particleString + ".options.defaults.height.halo-location")));
			}
		}
	}

	public static ParticleDefaultStorage getDefaults(ParticleEffect particle)
	{
		return particleDefaults.get(particle);
	}

	public int typeStringtoInt(String typeString)
	{
		if(typeString == null) return 1;
		
		switch (typeString)
		{
			case "random":
				return 2;
			case "dynamic":
				return 3;
			default:
				return 1;
		}
	}

	public int lengthStringtoInt(String lengthString)
	{
		if(lengthString == null) return 1;
		
		switch (lengthString)
		{
			case "medium":
				return 2;
			case "long":
				return 3;
			default:
				return 1;
		}
	}

	public int heightStringtoInt(String heightString)
	{
		if(heightString == null) return 0;
		
		switch (heightString)
		{
			case "waist":
				return 1;
			case "halo":
				return 2;
			default:
				return 0;
		}
	}

	public int colourStringtoInt(String colourString)
	{
		if(colourString == null) return 0;
		
		switch (colourString)
		{
			case "red":
				return 1;
			case "dark green":
				return 2;
			case "brown":
				return 3;
			case "dark blue":
				return 4;
			case "purple":
				return 5;
			case "cyan":
				return 6;
			case "light grey":
			case "light gray":
				return 7;
			case "grey":
			case "gray":
				return 8;
			case "pink":
				return 9;
			case "lime":
				return 10;
			case "yellow":
				return 11;
			case "light blue":
				return 12;
			case "magenta":
				return 13;
			case "orange":
				return 14;
			case "black":
				return 15;
			case "random":
				return 16;
			default:
				return 0;
		}
	}
}
