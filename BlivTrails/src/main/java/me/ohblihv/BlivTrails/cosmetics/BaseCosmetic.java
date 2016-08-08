package me.ohblihv.BlivTrails.cosmetics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.ohblihv.BlivTrails.objects.player.CheapPlayer;
import org.bukkit.Location;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 6/08/2016.
 */
@RequiredArgsConstructor
public abstract class BaseCosmetic
{
	
	@Getter
	private final String displayName;
	
	public abstract void onTick(long tick, Location location);
	
	public abstract void onTick(long tick, Location location, CopyOnWriteArraySet<CheapPlayer> nearbyPlayers);
	
}
