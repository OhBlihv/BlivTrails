package com.darkblade12.ParticleEffect.ParticlePacket;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ReflectionUtils;
import me.ohblihv.BlivTrails.objects.player.CheapPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Chris Brown (OhBlihv) on 8/08/2016.
 */
public class ParticlePacket_Cauldron_1_7_R4 extends ParticlePacket
{
	
	/*
	 * This class will use reflection for the time being due to the format of the provided (and used)
	 * cauldron net.minecraft.server JAR.
	 */
	
	private static Constructor<?> packetConstructor;
	private static Method getHandle;
	private static Field playerConnection;
	private static Method sendPacket;
	private static boolean initialized;
	
	//Reflected Packet Object
	private Object packet;
	
	public ParticlePacket_Cauldron_1_7_R4(ParticleEffect effect, float offsetX, float offsetY, float offsetZ, float speed, int amount, boolean longDistance, ParticleEffect.ParticleData data) throws IllegalArgumentException
	{
		super(effect, offsetX, offsetY, offsetZ, speed, amount, longDistance, data);
	}
	
	public ParticlePacket_Cauldron_1_7_R4(ParticleEffect effect, Vector direction, float speed, boolean longDistance, ParticleEffect.ParticleData data) throws IllegalArgumentException
	{
		super(effect, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, 0, longDistance, data);
	}
	
	public ParticlePacket_Cauldron_1_7_R4(ParticleEffect effect, ParticleEffect.ParticleColor color, boolean longDistance)
	{
		super(effect, color, longDistance);
	}
	
	@Override
	//The 'center' Location remains unused, but is kept to satisfy the abstraction.
	public void initialize(Location center)
	{
		if (initialized) {
			return;
		}
		try {
			Class<?> packetClass = Class.forName("hb");
			packetConstructor = ReflectionUtils.getConstructor(packetClass);
			getHandle = ReflectionUtils.getMethod("CraftPlayer", ReflectionUtils.PackageType.CRAFTBUKKIT_ENTITY, "getHandle");
			playerConnection = ReflectionUtils.getField(Class.forName("mw"), false, "field_71135_a"); //Normally 'a', but remapping -> 'field_71135_a'
			sendPacket = ReflectionUtils.getMethod(Class.forName("nh"), "func_147359_a", Class.forName("ft")); //Normally 'a', but remapping -> 'func_147359_a'
		} catch (Exception exception) {
			throw new VersionIncompatibleException("Your current bukkit version seems to be incompatible with this library", exception);
		}
		initialized = true;
	}
	
	/**
	 * Initializes {@link #packet} with all set values
	 *
	 * @param center Center location of the effect
	 * @throws PacketInstantiationException If instantion fails due to an unknown error
	 */
	private void initializePacket(Location center) throws PacketInstantiationException {
		if (packet != null) {
			return;
		}
		try {
			packet = packetConstructor.newInstance();
			
			ReflectionUtils.setValue(packet, true, "field_149236_a", effect.getName());
			ReflectionUtils.setValue(packet, true, "field_149234_b", (float) center.getX());
			ReflectionUtils.setValue(packet, true, "field_149235_c", (float) center.getY());
			ReflectionUtils.setValue(packet, true, "field_149232_d", (float) center.getZ());
			ReflectionUtils.setValue(packet, true, "field_149233_e", offsetX);
			ReflectionUtils.setValue(packet, true, "field_149230_f", offsetY);
			ReflectionUtils.setValue(packet, true, "field_149231_g", offsetZ);
			ReflectionUtils.setValue(packet, true, "field_149237_h", speed);
			ReflectionUtils.setValue(packet, true, "field_149238_i", amount);
		} catch (Exception exception) {
			throw new PacketInstantiationException("Packet instantiation failed", exception);
		}
	}
	
	@Override
	public void sendTo(Location center, Player player) throws PacketInstantiationException, PacketSendingException
	{
		initialize(null); //null is used since #initialize(Location center) is used for setting up reflection in this implementation.
		
		initializePacket(center);
		
		try
		{
			sendPacket.invoke(playerConnection.get(getHandle.invoke(player)), packet);
		}
		catch(Exception exception)
		{
			throw new PacketSendingException("Failed to send the packet to player '" + player.getName() + "'", exception);
		}
	}
	
	@Override
	public void sendToCheapPlayer(Location center, CheapPlayer player) throws PacketInstantiationException, PacketSendingException
	{
		initialize(center);
		
		try
		{
			sendPacket.invoke(player.getPlayerConnection(), packet);
		}
		catch(Exception exception)
		{
			throw new PacketSendingException("Failed to send the packet to player '" + player.getPlayer() + "'", exception);
		}
	}
	
}
