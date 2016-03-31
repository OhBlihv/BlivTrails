package net.auscraft.BlivTrails;

import lombok.Getter;
import lombok.Setter;

import com.darkblade12.ParticleEffect.ParticleEffect;

import java.util.UUID;

public class PlayerConfig
{
	/*
	 * Trail Config - Trail Material - Trail Type (Trace, Random, Dynamic)
	 * (1,2,3) - Length (1,2,3) - Height (Feet, Body, Halo) (0,1,2) - Width of
	 * trail (1,2,3 particles wide)
	 */

	@Getter
	private UUID uuid;

	@Getter
	@Setter
	private ParticleEffect particle;

	@Getter
	@Setter
	private int type;

	@Getter
	@Setter
	private int length;

	@Getter
	@Setter
	private int height;

	@Getter
	@Setter
	private int colour;

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

}
