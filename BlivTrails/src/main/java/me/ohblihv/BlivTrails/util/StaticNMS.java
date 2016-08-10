package me.ohblihv.BlivTrails.util;

import com.darkblade12.ParticleEffect.IParticlePacketFactory;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_10_R1;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_7_R1;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_7_R2;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_7_R3;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_7_R4;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_8_R1;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_8_R2;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_8_R3;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_9_R1;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_1_9_R2;
import com.darkblade12.ParticleEffect.ParticlePacketFactory_Cauldron_1_7_R4;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_10_R1;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_7_R1;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_7_R2;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_7_R3;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_7_R4;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_8_R1;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_8_R2;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_8_R3;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_9_R1;
import me.ohblihv.BlivTrails.objects.player.CheapPlayerFactory_1_9_R2;
import me.ohblihv.BlivTrails.objects.player.ICheapPlayerFactory;
import me.ohblihv.BlivTrails.util.nms.INMSHelper;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_10_R1;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R1;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R2;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R3;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R4;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_8_R1;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_8_R2;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_8_R3;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_9_R2;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;

/**
 * Created by Chris Brown (OhBlihv) on 9/08/2016.
 */
public class StaticNMS
{
	
	private static boolean isForge = false;
	private static String serverName = "null";
	static
	{
		String packageServerName = "null";
		try //Forge is always the slowest D:
		{
			Class craftServerClass = Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + ".CraftServer");
			Field serverNameField = craftServerClass.getDeclaredField("serverName");
			serverNameField.setAccessible(true);
			
			packageServerName = (String) serverNameField.get(Bukkit.getServer());
			
			//Very primitive Forge check, only really tested with Thermos
			if(packageServerName.equals("Cauldron"))
			{
				isForge = true;
			}
		}
		catch(Exception e)
		{
			//Handled below if particleFactoryInstance is not set.
		}
		
		serverName = packageServerName;
	}
	
	private static INMSHelper nmsHelper = null;
	public static INMSHelper getNMSHelper() throws IllegalArgumentException
	{
		if(nmsHelper == null)
		{
			BUtil.logInfo(BUtil.getNMSVersion());
			switch(BUtil.getNMSVersion())
			{
				case "v1_7_R1": nmsHelper = new NMSHelper_1_7_R1(); break;
				case "v1_7_R2": nmsHelper = new NMSHelper_1_7_R2(); break;
				case "v1_7_R3": nmsHelper = new NMSHelper_1_7_R3(); break;
				case "v1_7_R4": nmsHelper = new NMSHelper_1_7_R4(); break;
				case "v1_8_R1": nmsHelper = new NMSHelper_1_8_R1(); break;
				case "v1_8_R2": nmsHelper = new NMSHelper_1_8_R2(); break;
				case "v1_8_R3": nmsHelper = new NMSHelper_1_8_R3(); break;
				case "v1_9_R1": nmsHelper = new NMSHelper_1_9_R2(); break;
				case "v1_9_R2": nmsHelper = new NMSHelper_1_9_R2(); break;
				case "v1_10_R1": nmsHelper = new NMSHelper_1_10_R1(); break;
				default: //Check if we're running forge
				{
					if(isForge)
					{
						//Cauldron is 1.7.10 -> v1_7_R4
						nmsHelper = new NMSHelper_1_7_R4();
					}
					
					if(nmsHelper == null)
					{
						throw new IllegalArgumentException("This server version is not supported '" + serverName + "'");
					}
				}
			}
		}
		
		return nmsHelper;
	}
	
	private static IParticlePacketFactory particleFactoryInstance = null;
	public static IParticlePacketFactory getParticleFactoryInstance() throws IllegalArgumentException
	{
		if(particleFactoryInstance != null)
		{
			switch(BUtil.getNMSVersion())
			{
				//TODO: Convert to Factory
				case "v1_7_R1": particleFactoryInstance = new ParticlePacketFactory_1_7_R1(); break;
				case "v1_7_R2": particleFactoryInstance = new ParticlePacketFactory_1_7_R2(); break;
				case "v1_7_R3": particleFactoryInstance = new ParticlePacketFactory_1_7_R3(); break;
				case "v1_7_R4": particleFactoryInstance = new ParticlePacketFactory_1_7_R4(); break;
				case "v1_8_R1": particleFactoryInstance = new ParticlePacketFactory_1_8_R1(); break;
				case "v1_8_R2": particleFactoryInstance = new ParticlePacketFactory_1_8_R2(); break;
				case "v1_8_R3": particleFactoryInstance = new ParticlePacketFactory_1_8_R3(); break;
				case "v1_9_R1": particleFactoryInstance = new ParticlePacketFactory_1_9_R1(); break;
				case "v1_9_R2": particleFactoryInstance = new ParticlePacketFactory_1_9_R2(); break;
				case "v1_10_R1": particleFactoryInstance = new ParticlePacketFactory_1_10_R1(); break;
				default: //Check if we're running forge
				{
					if(isForge)
					{
						//Cauldron is 1.7.10 -> v1_7_R4
						particleFactoryInstance = new ParticlePacketFactory_Cauldron_1_7_R4();
						break;
					}
					
					if(particleFactoryInstance == null)
					{
						throw new IllegalArgumentException("This server version is not supported '" + serverName + "'");
					}
				}
			}
		}
		
		return particleFactoryInstance;
	}
	
	private static ICheapPlayerFactory cheapPlayerFactoryInstance = null;
	public static ICheapPlayerFactory getCheapPlayerFactoryInstance() throws IllegalArgumentException
	{
		if(cheapPlayerFactoryInstance != null)
		{
			switch(BUtil.getNMSVersion())
			{
				//TODO: Convert to Factory
				case "v1_7_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R1(); break;
				case "v1_7_R2": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R2(); break;
				case "v1_7_R3": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R3(); break;
				case "v1_7_R4": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R4(); break;
				case "v1_8_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_8_R1(); break;
				case "v1_8_R2": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_8_R2(); break;
				case "v1_8_R3": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_8_R3(); break;
				case "v1_9_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_9_R1(); break;
				case "v1_9_R2": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_9_R2(); break;
				case "v1_10_R1": cheapPlayerFactoryInstance = new CheapPlayerFactory_1_10_R1(); break;
				default: //Check if we're running forge
				{
					if(isForge)
					{
						//Cauldron is 1.7.10 -> v1_7_R4
						cheapPlayerFactoryInstance = new CheapPlayerFactory_1_7_R4();
						break;
					}
					
					if(cheapPlayerFactoryInstance == null)
					{
						throw new IllegalArgumentException("This server version is not supported '" + serverName + "'");
					}
				}
			}
		}
		
		return cheapPlayerFactoryInstance;
	}
	
}
