package net.auscraft.BlivTrails;

import com.darkblade12.ParticleEffect.ParticleEffect;

public class PlayerConfig
{
	/*
	 * Trail Config
	 * - Trail Material
	 * - Trail Type (Trace, Random, Dynamic) (1,2,3)
	 * - Length (1,2,3)
	 * - Height (Feet, Body, Halo) (0,1,2)
	 * - Width of trail (1,2,3 particles wide)
	 */
	
	private String uuid;
	private ParticleEffect particle;
	private int type;
	private int length;
	private int height;
	private int colour;
	
	//Now, its less of trail config, and just storing values
	private boolean isVanished = false;
	
	public PlayerConfig(String uuid, ParticleEffect particle, int type, int length, int height, int colour)
	{
		this.uuid = uuid;
		if(particle != null)
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
	
	public void setParticle(ParticleEffect particle)
	{
		this.particle = particle;
	}
	
	public void setType(int type)
	{
		this.type = type;
	}
	
	public void setLength(int length)
	{
		this.length = length;
	}
	
	public void setHeight(int height)
	{
		this.height = height;
	}
	
	public void setColour(int colour)
	{
		this.colour = colour;
	}
	
	public String getUUID()
	{
		return uuid;
	}
	
	public ParticleEffect getParticle()
	{
		return particle;
	}
	
	public int getType()
	{
		return type;
	}
	
	public int getLength()
	{
		return length;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getColour()
	{
		return colour;
	}
	
	//Holders
	public void setVanish(boolean isVanished)
	{
		this.isVanished = isVanished;
	}
	
	public boolean getVanish()
	{
		return isVanished;
	}
}