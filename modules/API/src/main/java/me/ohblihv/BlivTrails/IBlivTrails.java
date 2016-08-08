package me.ohblihv.BlivTrails;

import com.darkblade12.ParticleEffect.IParticlePacketFactory;
import me.ohblihv.BlivTrails.util.nms.INMSHelper;

/**
 * Created by Chris Brown (OhBlihv) on 8/08/2016.
 */
public interface IBlivTrails
{
	
	INMSHelper getNMSHelper();
	
	IParticlePacketFactory getParticleFactoryInstance() throws IllegalArgumentException;
	
}
