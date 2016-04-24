package net.auscraft.BlivTrails;

import com.darkblade12.ParticleEffect.ParticleEffect;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.auscraft.BlivTrails.listeners.GUIListener;

import java.util.UUID;

public class PlayerConfig
{
	/*
	 * Trail Config - Trail Material - Trail Type (Trace, Random, Dynamic)
	 * (1,2,3) - Length (1,2,3) - Height (Feet, Body, Halo) (0,1,2) - Width of
	 * trail (1,2,3 particles wide)
	 */

	@Getter
	@NonNull
	private UUID uuid;

	@Getter
	@Setter
	private ParticleEffect particle = null;

	@Getter
	@Setter
	private int type = 0;

	@Getter
	@Setter
	private int length = 0;

	@Getter
	@Setter
	private int height = 0;

	@Getter
	@Setter
	private int colour = 0;

	// Now, it's less of trail config, and just storing values
	@Getter
	@Setter
	private boolean vanished = false;

	public PlayerConfig(UUID uuid, ParticleEffect particle, int type, int length, int height, int colour)
	{
		this.uuid = uuid;
		if (particle != null)
		{
			this.particle = particle;
		}
		else
		{
			this.particle = ParticleEffect.FOOTSTEP;
		}

		this.type = type;
		this.length = length;
		this.height = height;
		this.colour = colour;
	}

	public PlayerConfig(UUID uuid)
	{
		this.uuid = uuid;

		//Defaults are already set
	}

	public int getEnabledOption(GUIListener.OptionType optionType)
	{
		switch(optionType)
		{
			case TYPE:
			case TYPE_TRACE:
			case TYPE_RANDOM:
			case TYPE_DYNAMIC:
				return getType();

			case LENGTH:
			case LENGTH_SHORT:
			case LENGTH_MEDIUM:
			case LENGTH_LONG:
				return getLength();

			case HEIGHT:
			case HEIGHT_FEET:
			case HEIGHT_WAIST:
			case HEIGHT_HALO:
				return getHeight();

			default: return 0; //Base Case -- Nothing is enabled.
		}
	}

}
