package net.auscraft.BlivTrails.config;

import java.util.concurrent.ConcurrentHashMap;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleProperty;

import net.auscraft.BlivTrails.utils.Utilities;

public class TrailDefaults
{
	// Will hold the trail defaults
	public class particleDefaultStorage
	{
		/*
		 * Defaults: type length height random.x-variation random.y-variation
		 * random.z-variation dynamic.spray-variation height.feet-location
		 * height.waist-location height.halo-location
		 */
		private int optionsInt[];
		private double optionsDouble[];

		public particleDefaultStorage(int type, int length, int height, int colour, double xvariation, double yvariation, double zvariation, double sprayvariation, double feetlocation, double waistlocation, double halolocation)
		{
			optionsInt = new int[4];
			optionsInt[0] = type;
			optionsInt[1] = length;
			optionsInt[2] = height;
			optionsInt[3] = colour;

			optionsDouble = new double[7];
			optionsDouble[0] = xvariation;
			optionsDouble[1] = yvariation;
			optionsDouble[2] = zvariation;
			optionsDouble[3] = sprayvariation;
			optionsDouble[4] = feetlocation;
			optionsDouble[5] = waistlocation;
			optionsDouble[6] = halolocation;
		}

		public int getInt(String option)
		{
			switch (option)
			{
				case "type":
					return optionsInt[0];
				case "length":
					return optionsInt[1];
				case "height":
					return optionsInt[2];
				case "colour":
					return optionsInt[3];
				default:
					return 0;
			}
		}

		public double getDouble(String option)
		{
			switch (option)
			{
				case "xvariation":
					return optionsDouble[0];
				case "yvariation":
					return optionsDouble[1];
				case "zvariation":
					return optionsDouble[2];
				case "sprayvariation":
					return optionsDouble[3];
				case "feetlocation":
					return optionsDouble[4];
				case "waistlocation":
					return optionsDouble[5];
				case "halolocation":
					return optionsDouble[6];
				default:
					return 0.0;
			}
		}
	}

	private static TrailDefaults instance = null;
	
	private ConcurrentHashMap<String, particleDefaultStorage> particleDefaults;
	
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
		ConfigAccessor cfg = ConfigAccessor.getInstance();
		String particleString = "";
		particleDefaults = new ConcurrentHashMap<String, particleDefaultStorage>();
		for (ParticleEffect particle : ParticleEffect.values())
		{
			particleString = particle.toString();
			if (Utilities.trailConfigName(particleString).length() != 0)
			{
				particleString = Utilities.trailConfigName(particleString);
				
				particleDefaults.put(particleString,
						new particleDefaultStorage(typeStringtoInt(cfg.getString("trails." + particleString + ".options.type")),
								lengthStringtoInt(cfg.getString("trails." + particleString + ".options.length")), heightStringtoInt(cfg.getString("trails." + particleString + ".options.height")),
								particle.hasProperty(ParticleProperty.COLORABLE) ? colourStringtoInt(cfg.getString("trails." + particleString + ".options.colour")) : 15,
								cfg.getDouble("trails." + particleString + ".options.defaults.random.x-variation"),
								cfg.getDouble("trails." + particleString + ".options.defaults.random.y-variation"), cfg.getDouble("trails." + particleString + ".options.defaults.random.z-variation"),
								cfg.getDouble("trails." + particleString + ".options.defaults.dynamic.spray-variation"), cfg.getDouble("trails." + particleString + ".options.defaults.height.feet-location"),
								cfg.getDouble("trails." + particleString + ".options.defaults.height.waist-location"), cfg.getDouble("trails." + particleString + ".options.defaults.height.halo-location")));
			}
		}
	}

	public particleDefaultStorage getDefaults(String particle)
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
