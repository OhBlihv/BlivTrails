package net.auscraft.BlivTrails;

import com.darkblade12.ParticleEffect.ParticleEffect;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

public class PlayerConfig
{

	@Getter
	@NonNull
	private UUID uuid;

	@Getter
	@Setter
	private ParticleEffect particle = null;

	@Getter
	@Setter
	private OptionType type = OptionType.TYPE;

	@Getter
	@Setter
	private OptionType length = OptionType.LENGTH;

	@Getter
	@Setter
	private OptionType height = OptionType.HEIGHT;

	@Getter
	@Setter
	private int colour = 0;

	// Now, it's less of trail config, and just storing values
	@Getter
	@Setter
	private boolean vanished = false;

	@Getter
	@Setter
	private int taskId = -1;

	@Getter
	@Setter
	private float trailTime = -1;

	public PlayerConfig(UUID uuid, ParticleEffect particle, OptionType type, OptionType length, OptionType height, int colour)
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
		this.uuid = uuid; //Other defaults are already set
	}

	public OptionType getEnabledOption(OptionType optionType)
	{
		switch(optionType)
		{
			case TYPE: case TYPE_TRACE: case TYPE_RANDOM: case TYPE_DYNAMIC:
				return getType();

			case LENGTH: case LENGTH_SHORT:	case LENGTH_MEDIUM:	case LENGTH_LONG:
				return getLength();

			case HEIGHT: case HEIGHT_FEET: case HEIGHT_WAIST: case HEIGHT_HALO:
				return getHeight();

		}
		return OptionType.TYPE; //Base case: 0
	}

	public boolean isScheduled()
	{
		return taskId != -1;
	}

	public boolean hasValidParticle()
	{
		return particle != null && particle != ParticleEffect.FOOTSTEP;
	}

	public boolean canSpawnParticle()
	{
		return trailTime > 0;
	}

}
