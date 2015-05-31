package net.auscraft.BlivTrails.config;

import java.util.concurrent.ConcurrentHashMap;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleProperty;

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
					break;
			}
			return 0;
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
					break;
			}
			return 0.0;
		}
	}

	private ConcurrentHashMap<String, particleDefaultStorage> particleDefaults;

	public TrailDefaults(ConfigAccessor cfg)
	{
		String particleString = "NULL";
		particleDefaults = new ConcurrentHashMap<String, particleDefaultStorage>();
		for (ParticleEffect particle : ParticleEffect.values())
		{
			particleString = particle.toString();
			if (!trailConfigName(particleString).equals("NULL"))
			{
				particleString = trailConfigName(particleString);
				
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
		int type = 1;
		try
		{
			switch (typeString)
			{
				case "random":
					type = 2;
					break;
				case "dynamic":
					type = 3;
					break;
				default:
					break;
			}
			return type;
		}
		catch (NullPointerException e)
		{
			return 1;
		}
	}

	public int lengthStringtoInt(String lengthString)
	{
		int length = 1;
		try
		{
			switch (lengthString)
			{
				case "medium":
					length = 2;
					break;
				case "long":
					length = 3;
					break;
				default:
					break;
			}
			return length;
		}
		catch (NullPointerException e)
		{
			return 1;
		}
	}

	public int heightStringtoInt(String heightString)
	{
		int height = 0;
		try
		{
			switch (heightString)
			{
				case "waist":
					height = 1;
					break;
				case "halo":
					height = 2;
					break;
				default:
					break;
			}
			return height;
		}
		catch (NullPointerException e)
		{
			return 0;
		}

	}

	public int colourStringtoInt(String colourString)
	{
		int colour = 0;
		try
		{
			switch (colourString)
			{
				case "red":
					colour = 1;
					break;
				case "dark green":
					colour = 2;
					break;
				case "brown":
					colour = 3;
					break;
				case "dark blue":
					colour = 4;
					break;
				case "purple":
					colour = 5;
					break;
				case "cyan":
					colour = 6;
					break;
				case "light grey":
				case "light gray":
					colour = 7;
					break;
				case "grey":
				case "gray":
					colour = 8;
					break;
				case "pink":
					colour = 9;
					break;
				case "lime":
					colour = 10;
					break;
				case "yellow":
					colour = 11;
					break;
				case "light blue":
					colour = 12;
					break;
				case "magenta":
					colour = 13;
					break;
				case "orange":
					colour = 14;
					break;
				case "black":
					colour = 15;
					break;
				case "random":
					colour = 16;
					break;
				default:
					break;
			}
		}
		catch (NullPointerException e)
		{
			// Null
		}
		return colour;
	}

	public String trailConfigName(String particleString)
	{
		switch (particleString)
		{
			case "BARRIER":
				particleString = "barrier";
				break;
			case "CLOUD":
				particleString = "cloud";
				break;
			case "CRIT":
				particleString = "criticals";
				break;
			case "CRIT_MAGIC":
				particleString = "criticals-magic";
				break;
			case "DRIP_LAVA":
				particleString = "drip-lava";
				break;
			case "DRIP_WATER":
				particleString = "drip-water";
				break;
			case "ENCHANTMENT_TABLE":
				particleString = "enchant";
				break;
			case "EXPLOSION_NORMAL":
				particleString = "explosion-smoke";
				break;
			case "FIREWORKS_SPARK":
				particleString = "firework";
				break;
			case "FLAME":
				particleString = "flame";
				break;
			case "HEART":
				particleString = "hearts";
				break;
			case "LAVA":
				particleString = "lava";
				break;
			case "NOTE":
				particleString = "note";
				break;
			case "PORTAL":
				particleString = "portal";
				break;
			case "REDSTONE":
				particleString = "redstone";
				break;
			case "SLIME":
				particleString = "slime";
				break;
			case "SMOKE_LARGE":
				particleString = "smoke";
				break;
			case "SNOW_SHOVEL":
				particleString = "snow-shovel";
				break;
			case "SNOWBALL":
				particleString = "snow-ball";
				break;
			case "SPELL":
				particleString = "spell";
				break;
			case "SPELL_INSTANT":
				particleString = "spell-instant";
				break;
			case "SPELL_MOB":
				particleString = "spell-mob";
				break;
			case "SPELL_WITCH":
				particleString = "spell-witch";
				break;
			case "VILLAGER_ANGRY":
				particleString = "angry-villager";
				break;
			case "VILLAGER_HAPPY":
				particleString = "happy-villager";
				break;
			case "TOWN_AURA":
				particleString = "town-aura";
				break;
			case "WATER_DROP":
				particleString = "water-drop";
				break;
			case "WATER_SPLASH":
				particleString = "water-splash";
				break;
			default:
				particleString = "NULL";
				break;
		}
		return particleString;
	}
}
