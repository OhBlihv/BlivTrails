package net.auscraft.BlivTrails.runnables;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ParticleEffect.NoteColor;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleColor;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleProperty;
import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.config.TrailDefaults;
import net.auscraft.BlivTrails.config.TrailDefaults.ParticleDefaultStorage;
import net.auscraft.BlivTrails.TrailManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;

public class TrailRunnable implements Runnable
{

	private class DisplayColourableRunnable implements Runnable
	{

		private ParticleEffect particle;
		private ParticleColor data;
		private Location loc;

		public DisplayColourableRunnable(ParticleEffect particle, ParticleColor data, Location loc)
		{
			this.particle = particle;
			this.data = data;
			this.loc = loc;
		}

		public void run()
		{
			particle.display(data, loc, 32);
			//particle.display(color, center, range);
		}
	}

	private class DisplayRegularRunnable implements Runnable
	{

		private ParticleEffect particle;
		private float xOff;
		private float yOff;
		private float zOff;
		private float speed;
		private Location loc;

		public DisplayRegularRunnable(ParticleEffect particle, float xOff, float yOff, float zOff, float speed, Location loc)
		{
			this.particle = particle;
			this.xOff = xOff;
			this.yOff = yOff;
			this.zOff = zOff;
			this.speed = speed;
			this.loc = loc;
		}

		public void run()
		{
			particle.display(xOff, yOff, zOff, speed, 1, loc, 64);
			// particle.display(offsetX, offsetY, offsetZ, speed, amount,
			// center, range);
		}
	}

	private BlivTrails plugin;

	private UUID uuid;
	private Player player;
	private Random rand;

	double[] heightCfg = new double[3], variationCfg = new double[3];
	double sprayCfg;
	
	//User Configurable
	ParticleEffect particle = null;
	ParticleColor data = null;
	double height = 0.00;
	float xOff = (float) 0.0, yOff = (float) 0.0, zOff = (float) 0.0, speed = (float) 0.0;
	int length = 0, type = 0;
	int colour = 0;

	/**
	 * 
	 * @param player
	 *            Player object of the player receiving the trail
	 * @param pcfg
	 *            PlayerConfig object of the player receiving the trail
	 * @param listener
	 *            TrailManager instance
	 * @param rand
	 *            A Random() Object that is outside this function in order to
	 *            reduce/stop duplicate results
	 * @param option
	 *            Global Option Array
	 */
	public TrailRunnable(Player player, PlayerConfig pcfg, Random rand, double[] option)
	{
		this.plugin = BlivTrails.getInstance();
		this.rand = rand;
		this.player = player;
		this.uuid = player.getUniqueId();
		particle = pcfg.getParticle();
		ParticleDefaultStorage particleStorage = TrailDefaults.getDefaults(particle);

		// Height Global/Trail-Specific Overrides
		// If there isnt trail-given override, use the global value
		if (particleStorage.getFeetLocation() != 0D)
		{
			heightCfg[0] = particleStorage.getFeetLocation();
		}
		else
		{
			heightCfg[0] = option[4];
		}

		if (particleStorage.getWaistLocation() != 0D)
		{
			heightCfg[1] = particleStorage.getWaistLocation();
		}
		else
		{
			heightCfg[1] = option[5];
		}

		if (particleStorage.getHaloLocation() != 0D)
		{
			heightCfg[2] = particleStorage.getHaloLocation();
		}
		else
		{
			heightCfg[2] = option[6];
		}

		// Random Global/Trail-Specific Overrides
		if (particleStorage.getXVariation() != 0D)
		{
			variationCfg[0] = particleStorage.getXVariation();
		}
		else
		{
			variationCfg[0] = option[0];
		}

		if (particleStorage.getYVariation() != 0D)
		{
			variationCfg[1] = particleStorage.getYVariation();
		}
		else
		{
			variationCfg[1] = option[1];
		}

		if (particleStorage.getZVariation() != 0D)
		{
			variationCfg[2] = particleStorage.getZVariation();
		}
		else
		{
			variationCfg[2] = option[2];
		}

		// Dynamic Spread Global/Trail-Specific Overrides
		if (particleStorage.getSprayVariation() != 0D)
		{
			sprayCfg = particleStorage.getSprayVariation();
		}
		else
		{
			sprayCfg = option[3];
		}
		
		//User Configurable
		
		length = pcfg.getLength();
		type = pcfg.getType();
		
		if (!(type == 2)) // Standard + Dynamic
		{
			int heightInt = pcfg.getHeight();
			if (heightInt == 0) // Feet
			{
				height = heightCfg[0];
			}
			else if (heightInt == 1) // Waist
			{
				height = heightCfg[1];
			}
			else
			// Halo
			{
				height = heightCfg[2];
			}
		}
		
		colour = pcfg.getColour();
		if (particle == ParticleEffect.NOTE)
		{
			switch (colour)
			{
				// CANNOT DO BLACK
				case 1:
					data = new NoteColor(7);
					break; // Red
				case 2:
					data = new NoteColor(20);
					break; // Green
				// CANNOT DO BROWN
				case 4:
					data = new NoteColor(15);
					break; // Blue
				case 5:
					data = new NoteColor(12);
					break; // Purple
				case 6:
					data = new NoteColor(18);
					break; // Cyan
				// CANNOT DO LIGHT GREY
				// CANNOT DO GREY
				case 9:
					data = new NoteColor(10);
					break; // Pink
				case 10:
					data = new NoteColor(24);
					break; // Lime
				case 11:
					data = new NoteColor(3);
					break; // Yellow
				case 12:
					data = new NoteColor(17);
					break; // Light Blue
				case 13:
					data = new NoteColor(11);
					break; // Magenta
				case 14:
					data = new NoteColor(5);
					break; // Orange
				// CANNOT DO WHITE
				case 16:
					data = new NoteColor(rand.nextInt(24));
					break; // Random
				default:
					data = new NoteColor(24);
					break;
			}
		}
		else
		{
			switch (colour)
			{
				case 0:
					data = new ParticleEffect.OrdinaryColor(0, 0, 0);
					break; // Black
				case 1:
					data = new ParticleEffect.OrdinaryColor(255, 0, 0);
					break; // Red
				case 2:
					data = new ParticleEffect.OrdinaryColor(0, 128, 0);
					break; // Dark Green
				case 3:
					data = new ParticleEffect.OrdinaryColor(128, 128, 0);
					break; // Brown
				case 4:
					data = new ParticleEffect.OrdinaryColor(0, 0, 255);
					break; // Dark Blue
				case 5:
					data = new ParticleEffect.OrdinaryColor(128, 0, 128);
					break; // Purple
				case 6:
					data = new ParticleEffect.OrdinaryColor(0, 128, 128);
					break; // Cyan
				case 7:
					data = new ParticleEffect.OrdinaryColor(192, 192, 192);
					break; // Light Grey
				case 8:
					data = new ParticleEffect.OrdinaryColor(128, 128, 128);
					break; // Grey
				case 9:
					data = new ParticleEffect.OrdinaryColor(235, 69, 207);
					break; // Pink
				case 10:
					data = new ParticleEffect.OrdinaryColor(0, 255, 0);
					break; // Lime
				case 11:
					data = new ParticleEffect.OrdinaryColor(255, 255, 0);
					break; // Yellow
				case 12:
					data = new ParticleEffect.OrdinaryColor(0, 255, 255);
					break; // Light Blue
				case 13:
					data = new ParticleEffect.OrdinaryColor(255, 0, 255);
					break; // magenta
				case 14:
					data = new ParticleEffect.OrdinaryColor(255, 128, 0);
					break; // Orange
				case 16:
					data = new ParticleEffect.OrdinaryColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
					break;
				default:
					data = new ParticleEffect.OrdinaryColor(255, 255, 255);
					break;
			}
		}
	}

	@Override
	public void run()
	{
		try
		{
			if (TrailManager.getTrailTime().get(uuid) > 0)
			{
				final ParticleEffect finalParticle = particle;
				
				if(type == 2)
				// Random
				{
					// Randomise direction of x and z (Independent of type)
					// 0 = Negative, 1 = Positive
					int xDir = 1, yDir = 1, zDir = 1;
					// Properly change the directions
					if (rand.nextBoolean())
					{
						xDir = -1;
					}
					if (rand.nextBoolean())
					{
						yDir = -1;
					}
					if (rand.nextBoolean())
					{
						zDir = -1;
					}

					// Offset = (0.0-1.0) * (Variation) * (1 or -1)
					// Gives (0.0-1.0) * (Variation), with either positive
					// or negative x/y/z co-ordinates relative to the player
					// double xvar = variationCfg[0], yvar = variationCfg[1],
					// zvar = variationCfg[2];

					xOff = (float) (rand.nextFloat() * variationCfg[0] * xDir);
					yOff = (float) (rand.nextFloat() * variationCfg[1] * yDir);
					zOff = (float) (rand.nextFloat() * variationCfg[2] * zDir);
				}
				else if (type == 3) // Random Directions from feet (Spray)
				{
					// (0.0-1.0)/20.00 * variation (Default is 1)
					speed = (float) ((rand.nextFloat() / 20.00) * sprayCfg);
				}

				
				if (finalParticle.hasProperty(ParticleProperty.COLORABLE))
				{
					if(finalParticle == ParticleEffect.NOTE)
					{
						if(colour == 16)
						{
							data = new NoteColor(rand.nextInt(24));
						}
					}
					else
					{
						if(colour == 16)
						{
							data = new ParticleEffect.OrdinaryColor(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
						}
					}

					if(type == 2)
					{
						finalParticle.display(data, player.getLocation().add(xOff, yOff, zOff), 64);
					}
					else
					{
						finalParticle.display(data, player.getLocation().add(0.0D, height, 0.0D), 64);
					}
				}
				else
				{
					finalParticle.display(xOff, yOff, zOff, speed, 1, player.getLocation().add(0.0D, height, 0.0D), 64);
				}

				if (length > 1)
				{
					for (int i = 1; i < (length + 1); i++)
					{
						if (particle.hasProperty(ParticleProperty.COLORABLE))
						{
							if(type == 2)
							{
								Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
										new DisplayColourableRunnable(finalParticle, data, player.getLocation().add(xOff, yOff, zOff)), i * 5);
								// public DisplayColourableRunnable(ParticleEffect particle, ParticleColor data, Location loc)
							}
							else
							{
								Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
										new DisplayColourableRunnable(finalParticle, data, player.getLocation().add(0.0D, height, 0.0D)), i * 5);
								// public DisplayColourableRunnable(ParticleEffect particle, ParticleColor data, Location loc)
							}
						}
						else
						{
							Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
									new DisplayRegularRunnable(finalParticle, xOff, yOff, zOff, speed, player.getLocation().add(0.0D, height, 0.0D)), i * 5);
							// public DisplayRegularRunnable(ParticleEffect particle, float xOff, float yOff, float zOff, float speed, Location loc)
									
						}
					}
				}
			}
		}
		catch (ParticleEffect.ParticleVersionException e)
		{
			e.printStackTrace();
			try
			{
				TrailManager.getTrailMap().remove(uuid);
			}
			catch (NullPointerException e2)
			{
				//
			}

			Bukkit.getServer().getScheduler().cancelTask(TrailManager.getTaskMap().get(uuid)); // Cancel Self
		}
		catch(NullPointerException e)
		{
			Bukkit.getServer().getScheduler().cancelTask(TrailManager.getTaskMap().get(uuid)); // Cancel Self
		}
	}
	
}
