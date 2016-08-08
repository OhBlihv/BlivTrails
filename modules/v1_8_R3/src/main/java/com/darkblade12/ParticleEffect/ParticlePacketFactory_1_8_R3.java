package com.darkblade12.ParticleEffect;

import com.darkblade12.ParticleEffect.ParticlePacket.ParticlePacket;
import com.darkblade12.ParticleEffect.ParticlePacket.ParticlePacket_1_8_R3;
import org.bukkit.util.Vector;

/**
 * Created by Chris Brown (OhBlihv) on 8/08/2016.
 */
public class ParticlePacketFactory_1_8_R3 implements IParticlePacketFactory
{
	
	@Override
	public ParticlePacket getParticlePacket(ParticleEffect effect, float offsetX, float offsetY, float offsetZ,
	                                        float speed, int amount, boolean longDistance,
	                                        ParticleEffect.ParticleData data) throws IllegalArgumentException
	{
		return new ParticlePacket_1_8_R3(effect, offsetX, offsetY, offsetZ,
		                                 speed, amount, longDistance, data);
	}
	
	@Override
	public ParticlePacket getParticlePacket(ParticleEffect effect, Vector direction, float speed, boolean longDistance,
	                                        ParticleEffect.ParticleData data) throws IllegalArgumentException
	{
		return new ParticlePacket_1_8_R3(effect, direction, speed, longDistance, data);
	}
	
	@Override
	public ParticlePacket getParticlePacket(ParticleEffect effect, ParticleEffect.ParticleColor color, boolean longDistance)
	{
		return new ParticlePacket_1_8_R3(effect, color, longDistance);
	}
	
}
