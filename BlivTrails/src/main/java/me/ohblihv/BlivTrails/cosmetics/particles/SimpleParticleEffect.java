package me.ohblihv.BlivTrails.cosmetics.particles;

import com.darkblade12.ParticleEffect.ParticleEffect;
import me.ohblihv.BlivTrails.objects.player.CheapPlayer;
import org.bukkit.Location;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 8/08/2016.
 */
public class SimpleParticleEffect extends BaseParticleEffect
{
	
	public SimpleParticleEffect(String displayName, ParticleEffect particleEffect)
	{
		super(displayName, particleEffect);
	}
	
	@Override
	public void onTick(long tick, Location location)
	{
		//Any player-directed particle effects should be using CheapPlayers managed by an ActiveCosmetic and called by the ParticleThread
		throw new IllegalArgumentException("This effect type does not support this call!");
	}
	
	@Override
	public void onTick(long tick, Location location, CopyOnWriteArraySet<CheapPlayer> nearbyPlayers)
	{
		particleEffect.displayToCheapPlayer(0F, 0F, 0F, 1F, 1, location, nearbyPlayers);
	}
}
