package net.auscraft.BlivTrails.runnables;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ParticleEffect.NoteColor;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleColor;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleProperty;
import lombok.RequiredArgsConstructor;
import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.OptionType;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.config.TrailDefaults;
import net.auscraft.BlivTrails.config.TrailDefaults.ParticleDefaultStorage;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import static net.auscraft.BlivTrails.BlivTrails.rand;

public class TrailRunnable implements Runnable
{

	@RequiredArgsConstructor
	private static class DisplayColourableRunnable implements Runnable
	{

		final ParticleEffect particle;
		final ParticleColor data;
		final Location loc;

		public void run()
		{
			particle.display(data, loc, 32);
			//particle.display(color, center, range);
		}
	}

	@RequiredArgsConstructor
	private static class DisplayRegularRunnable implements Runnable
	{

		final ParticleEffect particle;
		final float xOff;
		final float yOff;
		final float zOff;
		final float speed;
		final Location loc;

		public void run()
		{
			particle.display(xOff, yOff, zOff, speed, 1, loc, 64);
			// particle.display(offsetX, offsetY, offsetZ, speed, amount,
			// center, range);
		}
	}

	private static class DragonsBreathRunnable extends DisplayRegularRunnable implements Runnable
	{

		Particle enumParticle;

		public DragonsBreathRunnable(ParticleEffect particle, float xOff, float yOff, float zOff, float speed, Location loc)
		{
			super(particle, xOff, yOff, zOff, speed, loc);

			this.enumParticle = Particle.valueOf(particle.name());
		}

		@Override
		public void run()
		{
			loc.getWorld().spawnParticle(enumParticle, loc, 1, (double) xOff, (double) yOff, (double) zOff, (double) speed);
			//void spawnParticle(Particle var1, Location var2, int var3, double var4, double var6, double var8, double var10);
		}

	}

	private static final BlivTrails plugin = BlivTrails.getInstance();

	private Player player;

	double[]    heightCfg = new double[3],
				variationCfg = new double[3];
	double sprayCfg;
	
	//User Configurable
	ParticleEffect particle = null;
	ParticleColor data = null;
	double height = 0.00;
	float xOff = (float) 0.0, yOff = (float) 0.0, zOff = (float) 0.0, speed = (float) 0.0;
	OptionType length = OptionType.NONE, type = OptionType.NONE;
	int colour = 0;

	/**
	 * 
	 * @param player
	 *            Player object of the player receiving the trail
	 * @param pcfg
	 *            PlayerConfig object of the player receiving the trail
	 * @param listener
	 *            TrailManager instance
	 * @param option
	 *            Global Option Array
	 */
	public TrailRunnable(Player player, PlayerConfig pcfg, double[] option)
	{
		this.player = player;
		particle = pcfg.getParticle();
		ParticleDefaultStorage particleStorage = TrailDefaults.getDefaults(particle);

		// Height Global/Trail-Specific Overrides
		// If there isn't trail-given override, use the global value
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
		
		if (type != OptionType.TYPE_RANDOM) // Standard + Dynamic
		{
			height = heightCfg[pcfg.getHeight().getCfgId()];
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
					data = new NoteColor(BlivTrails.rand.nextInt(24));
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
		PlayerConfig playerConfig = TrailManager.getTrailMap().get(player.getUniqueId());
		if(playerConfig == null)
		{
			BUtil.logError("Attempted to spawn particle for unloaded player " + player.getName());
			return;
		}

		if (playerConfig.canSpawnParticle())
		{
			if(type == OptionType.TYPE_RANDOM)
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
			else if (type == OptionType.TYPE_DYNAMIC) // Random Directions from feet (Spray)
			{
				// (0.0-1.0)/20.00 * variation (Default is 1)
				speed = (float) ((rand.nextFloat() / 20.00D) * sprayCfg);
			}

			try
			{
				if (particle.hasProperty(ParticleProperty.COLORABLE))
				{
					if(particle == ParticleEffect.NOTE)
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

					if(type == OptionType.TYPE_RANDOM)
					{
						particle.display(data, player.getLocation().add(xOff, yOff, zOff), 64);
					}
					else
					{
						particle.display(data, player.getLocation().add(0.0D, height, 0.0D), 64);
					}
				}
				else
				{
					if(particle == ParticleEffect.DRAGON_BREATH)
					{
						player.getWorld().spawnParticle(Particle.valueOf(particle.name()), player.getLocation().add(0.0D, height, 0.0D), 1, (double) xOff, (double) yOff, (double) zOff, (double) speed);
					}
					else
					{
						particle.display(xOff, yOff, zOff, speed, 1, player.getLocation().add(0.0D, height, 0.0D), 64);
					}
				}

				if (length.getCfgId() > 1)
				{
					for (int i = 1; i < (length.getCfgId() + 1); i++)
					{
						if (this.particle.hasProperty(ParticleProperty.COLORABLE))
						{
							if(type == OptionType.TYPE_RANDOM)
							{
								Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
								                      new DisplayColourableRunnable(particle, data, player.getLocation().add(xOff, yOff, zOff)), i * 5);
								// public DisplayColourableRunnable(ParticleEffect particle, ParticleColor data, Location loc)
							}
							else
							{
								Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
								                      new DisplayColourableRunnable(particle, data, player.getLocation().add(0.0D, height, 0.0D)), i * 5);
								// public DisplayColourableRunnable(ParticleEffect particle, ParticleColor data, Location loc)
							}
						}
						else
						{
							if(particle == ParticleEffect.DRAGON_BREATH)
							{
								Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
								                      new DragonsBreathRunnable(particle, xOff, yOff, zOff, speed, player.getLocation().add(0.0D, height, 0.0D)), i * 5);
							}
							else
							{
								Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
								                      new DisplayRegularRunnable(particle, xOff, yOff, zOff, speed, player.getLocation().add(0.0D, height, 0.0D)), i * 5);
								// public DisplayRegularRunnable(ParticleEffect particle, float xOff, float yOff, float zOff, float speed, Location loc)
							}
						}
					}
				}
			}
			catch (ParticleEffect.ParticleVersionException | ParticleEffect.ParticlePacket.VersionIncompatibleException e)
			{
				if(playerConfig.isScheduled())
				{
					Bukkit.getScheduler().cancelTask(playerConfig.getTaskId()); //Cancel Self
				}

				e.printStackTrace(); //Print Anyway :)
			}
		}
	}
	
}
