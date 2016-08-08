package me.ohblihv.BlivTrails.cosmetics.particles;

import com.darkblade12.ParticleEffect.ParticleEffect;
import me.ohblihv.BlivTrails.cosmetics.BaseCosmetic;

/**
 * Created by Chris Brown (OhBlihv) on 6/08/2016.
 */
public abstract class BaseParticleEffect extends BaseCosmetic
{
	
	ParticleEffect particleEffect;
	
	public BaseParticleEffect(String displayName, ParticleEffect particleEffect)
	{
		super(displayName);
		
		this.particleEffect = particleEffect;
	}
	
}
