package com.darkblade12.ParticleEffect;

import com.darkblade12.ParticleEffect.ParticlePacket.ParticlePacket;
import com.darkblade12.ParticleEffect.ParticlePacket.ParticlePacket_Cauldron_1_7_R4;
import org.bukkit.util.Vector;

/**
 * Created by Chris Brown (OhBlihv) on 8/08/2016.
 */
public class ParticlePacketFactory_Cauldron_1_7_R4 implements IParticlePacketFactory
{
	
	@Override
	public ParticlePacket getParticlePacket(ParticleEffect effect, float offsetX, float offsetY, float offsetZ,
	                                        float speed, int amount, boolean longDistance,
	                                        ParticleEffect.ParticleData data) throws IllegalArgumentException
	{
		return new ParticlePacket_Cauldron_1_7_R4(effect, offsetX, offsetY, offsetZ,
		                                  speed, amount, longDistance, data);
	}
	
	@Override
	public ParticlePacket getParticlePacket(ParticleEffect effect, Vector direction, float speed, boolean longDistance,
	                                        ParticleEffect.ParticleData data) throws IllegalArgumentException
	{
		return new ParticlePacket_Cauldron_1_7_R4(effect, direction, speed, longDistance, data);
	}
	
	@Override
	public ParticlePacket getParticlePacket(ParticleEffect effect, ParticleEffect.ParticleColor color, boolean longDistance)
	{
		return new ParticlePacket_Cauldron_1_7_R4(effect, color, longDistance);
	}
	
}
