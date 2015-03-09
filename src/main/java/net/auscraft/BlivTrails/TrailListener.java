package net.auscraft.BlivTrails;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import net.auscraft.BlivTrails.config.ConfigAccessor;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.config.TrailDefaults;
import net.auscraft.BlivTrails.config.TrailDefaults.particleDefaultStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.kitteh.vanish.VanishPlugin;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.darkblade12.ParticleEffect.ParticleEffect.NoteColor;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleColor;
import com.darkblade12.ParticleEffect.ParticleEffect.ParticleProperty;
import com.jolbox.bonecp.BoneCPDataSource;

public class TrailListener implements Listener
{
	public final ParticleEffect[] usedTrails = {ParticleEffect.VILLAGER_ANGRY, ParticleEffect.BARRIER, ParticleEffect.CLOUD, ParticleEffect.CRIT, ParticleEffect.CRIT_MAGIC, ParticleEffect.DRIP_LAVA,
			ParticleEffect.DRIP_WATER, ParticleEffect.ENCHANTMENT_TABLE, ParticleEffect.EXPLOSION_NORMAL, ParticleEffect.FIREWORKS_SPARK, ParticleEffect.FLAME,
			ParticleEffect.HEART, ParticleEffect.LAVA, ParticleEffect.NOTE, ParticleEffect.PORTAL, ParticleEffect.REDSTONE, ParticleEffect.SLIME, ParticleEffect.SMOKE_LARGE,
			ParticleEffect.SNOW_SHOVEL, ParticleEffect.SNOWBALL, ParticleEffect.SPELL, ParticleEffect.SPELL_INSTANT, ParticleEffect.SPELL_MOB, ParticleEffect.SPELL_WITCH,
			ParticleEffect.TOWN_AURA, ParticleEffect.VILLAGER_HAPPY, ParticleEffect.WATER_DROP, ParticleEffect.WATER_SPLASH};
	private HashMap<String, PlayerConfig> trailMap;
	private BlivTrails instance;
	private Utilities util;
	private Random rand;
	private BoneCPDataSource sql = null;
	private FlatFile flatfile = null;
	private ConfigAccessor cfg;
	private static Messages msg;
	private double option[];
	private TrailDefaults trailDefaults;
	private boolean vanishEnabled;
	/*
	 * vanishHook
	 * 0 = disabled
	 * 1 = VanishNoPacket
	 * 2 = Essentials Vanish
	 */
	private int vanishHook;
	
	public TrailListener(BlivTrails instance)
	{
		this.instance = instance;
		util = instance.getUtil();
		cfg = instance.getCfg();
		util.setConfig(cfg);
		loadDefaultOptions();
		instance.setListener(this);
		rand = new Random(System.currentTimeMillis());
		vanishEnabled = false;
		vanishHook = 0;
		trailMap = new HashMap<String, PlayerConfig>();
		Object saveLoc = instance.getSave();
		if(saveLoc instanceof BoneCPDataSource)
		{
			sql = (BoneCPDataSource) saveLoc;
		}
		else
		{
			flatfile = (FlatFile) saveLoc;
		}
		msg = instance.getMessages();
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			loadTrail(player);
		}
	}
	
	public void doDisable()
	{
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(trailMap.containsKey(player.getUniqueId().toString()))
			{
				saveTrail(player);
			}
		}
	}
	
	public void loadDefaultOptions()
	{
		/*
		 * option[0] = random.x-variation
		 * option[1] = random.y-variation
		 * option[2] = random.z-variation
		 * 
		 * option[3] = dynamic.spray-variation
		 * 
		 * option[4] = height.feet-location
		 * option[5] = height.waist-location
		 * option[6] = height.halo-location
		 */
		option = new double[7];
		option[0] = cfg.getDouble("trails.defaults.type.random.x-variation");
		option[1] = cfg.getDouble("trails.defaults.type.random.y-variation");
		option[2] = cfg.getDouble("trails.defaults.type.random.z-variation");
		
		option[3] = cfg.getDouble("trails.defaults.type.dynamic.spray-variation");
		
		option[4] = cfg.getDouble("trails.defaults.height.feet-location");
		option[5] = cfg.getDouble("trails.defaults.height.waist-location");
		option[6] = cfg.getDouble("trails.defaults.height.halo-location");
		
		trailDefaults = new TrailDefaults(cfg);
		util.logDebug("Finished Loading Defaults!");
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		loadTrail(event.getPlayer());
		try
		{
			if(vanishEnabled)
			{
				if(vanishHook == 1) //VanishNoPacket
				{
					if(isVanished(event.getPlayer()))
					{
						if(trailMap.containsKey(event.getPlayer().getUniqueId().toString()))
						{
							trailMap.get(event.getPlayer().getUniqueId().toString()).setVanish(true);
						}
						else
						{
							util.logDebug("Player doesnt have a trail to hide");
						}
					}
					else
					{
						util.logDebug("Player is not vanished");
					}
				}
				else
				{
					//Essentials Vanish does not have vanish join
					//Else, do nothing
				}
			}
		}
		catch(ClassNotFoundException e)
		{
			util.logDebug("Vanish Hooking Failed. Did you unload a plugin?");
		}
		
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		if(trailMap.containsKey(event.getPlayer().getUniqueId().toString()))
		{
			util.logDebug(event.getPlayer().getName() + " has a trail.");
			saveTrail(event.getPlayer());
			trailMap.remove(event.getPlayer().getUniqueId().toString());
		}
		else
		{
			util.logDebug(event.getPlayer().getName() + " doesn't have an active trail");
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		try
		{
			if(trailMap.containsKey(event.getPlayer().getUniqueId().toString()))
			{
				//Stop the trail from working while the player isnt technically moving
				if(event.getFrom().getX() == event.getTo().getX() && event.getFrom().getY() == event.getTo().getY() && event.getFrom().getZ() == event.getTo().getZ())
				{
					return;
				}
				PlayerConfig pcfg = trailMap.get(event.getPlayer().getUniqueId().toString());
				if(vanishEnabled)
				{
					if(pcfg.getVanish() == true)
					{
						return; //If Vanished, dont do the trail.
					}
				}
				if(pcfg.getParticle().equals(ParticleEffect.FOOTSTEP))
				{
					return;
				}
				
				final ParticleEffect particle = pcfg.getParticle();
				
				int length = pcfg.getLength();
				particleDefaultStorage pDef = trailDefaults.getDefaults(util.trailConfigName(particle.toString()));
				
				double height = 0.00;
				int type = pcfg.getType();
				float xOff = (float) 0.0, yOff = (float) 0.0, zOff = (float) 0.0;
				float speed = (float) 0.0;
				if(!(type == 2)) //Standard + Dynamic
				{
					int heightInt = pcfg.getHeight();
					if(heightInt == 0) //Feet
					{
						//If there isnt trail-given override, use the global value
						if(pDef.getDouble("feetlocation") != 0.0)
						{
							height = pDef.getDouble("feetlocation");
						}
						else
						{
							height = option[4];
						}
						
					}
					else if(heightInt == 1) //Waist
					{
						if(pDef.getDouble("waistlocation") != 0.0)
						{
							height = pDef.getDouble("waistlocation");
						}
						else
						{
							height = option[5];
						}
						
					}
					else //Halo
					{
						if(pDef.getDouble("halolocation") != 0.0)
						{
							height = pDef.getDouble("halolocation");
						}
						else
						{
							height = option[6];
						}
						
					}
				}
				else //Random
				{
					//Randomise direction of x and z (Independent of type)
					//0 = Negative, 1 = Positive
					int xDir = 1, yDir = 1, zDir = 1;
					//Properly change the directions
					if(rand.nextBoolean())
					{
						xDir = -1;
					}
					if(rand.nextBoolean())
					{
						yDir = -1;
					}
					if(rand.nextBoolean())
					{
						zDir = -1;
					}
					
					//Offset = (0.0-1.0) * (Variation) * (1 or -1)
					//Gives (0.0-1.0) * (Variation), with either positive
					//or negative x/y/z co-ordinates relative to the player
					double xvar = 0.0, yvar = 0.0, zvar = 0.0;
					if(pDef.getDouble("xvariation") != 0.0)
					{
						xvar = pDef.getDouble("xvariation");
					}
					else
					{
						xvar = option[0];
					}
					if(pDef.getDouble("yvariation") != 0.0)
					{
						yvar = pDef.getDouble("yvariation");
					}
					else
					{
						yvar = option[1];
					}
					if(pDef.getDouble("zvariation") != 0.0)
					{
						zvar = pDef.getDouble("zvariation");
					}
					else
					{
						zvar = option[2];
					}
					xOff = (float) (rand.nextFloat() * xvar * xDir);
					yOff = (float) (rand.nextFloat() * yvar * yDir);
					zOff = (float) (rand.nextFloat() * zvar * zDir);
				}
				if(type == 3) //Random Directions from feet (Spray)
				{
					//(0.0-1.0)/10.00 * variation (Default is 1)
					double sprayVar = 0.0;
					if(pDef.getDouble("sprayvariation") != 0.0)
					{
						sprayVar = pDef.getDouble("sprayvariation");
					}
					else
					{
						sprayVar = option[3];
					}
					speed = (float) ((rand.nextFloat()/20.00) * sprayVar);
				}
				
				ParticleColor data = null;
				if(particle.hasProperty(ParticleProperty.COLORABLE))
				{
					int colour = pcfg.getColour();
					if(particle == ParticleEffect.NOTE)
					{
						switch(colour)
						{
							//CANNOT DO BLACK
							case 1: data = new NoteColor(7); break; //Red
							case 2: data = new NoteColor(20); break; //Green
							//CANNOT DO BROWN
							case 4: data = new NoteColor(15); break; //Blue
							case 5: data = new NoteColor(12); break; //Purple
							case 6: data = new NoteColor(18); break; //Cyan
							//CANNOT DO LIGHT GREY
							//CANNOT DO GREY
							case 9: data = new NoteColor(10); break; //Pink
							case 10: data = new NoteColor(24); break; //Lime
							case 11: data = new NoteColor(3); break; //Yellow
							case 12: data = new NoteColor(17); break; //Light Blue
							case 13: data = new NoteColor(11); break; //Magenta
							case 14: data = new NoteColor(5); break; //Orange
							//CANNOT DO WHITE
							case 16: data = new NoteColor(rand.nextInt(24)); break; //Random
							default: data = new NoteColor(24); break;
						}
					}
					else
					{
						switch(colour)
						{
							case 0: data = new ParticleEffect.OrdinaryColor(0,0,0); break; //Black
							case 1: data = new ParticleEffect.OrdinaryColor(255,0,0); break; //Red
							case 2: data = new ParticleEffect.OrdinaryColor(0,128,0); break; //Dark Green
							case 3: data = new ParticleEffect.OrdinaryColor(128,128,0); break; //Brown
							case 4: data = new ParticleEffect.OrdinaryColor(0,0,255); break; //Dark Blue
							case 5: data = new ParticleEffect.OrdinaryColor(128,0,128); break; //Purple
							case 6: data = new ParticleEffect.OrdinaryColor(0,128,128); break; //Cyan
							case 7: data = new ParticleEffect.OrdinaryColor(192,192,192); break; //Light Grey
							case 8: data = new ParticleEffect.OrdinaryColor(128,128,128); break; //Grey
							case 9: data = new ParticleEffect.OrdinaryColor(235,69,207); break; //Pink
							case 10: data = new ParticleEffect.OrdinaryColor(0,255,0); break; //Lime
							case 11: data = new ParticleEffect.OrdinaryColor(255,255,0); break; //Yellow
							case 12: data = new ParticleEffect.OrdinaryColor(0,255,255); break; //Light Blue
							case 13: data = new ParticleEffect.OrdinaryColor(255,0,255); break; //magenta
							case 14: data = new ParticleEffect.OrdinaryColor(255,128,0); break; //Orange
							case 16: data = new ParticleEffect.OrdinaryColor(rand.nextInt(255),rand.nextInt(255),rand.nextInt(255)); break;
							default: data = new ParticleEffect.OrdinaryColor(255,255,255); break;
						}
					}
					
					 particle.display(data, event.getPlayer().getLocation().add(0.0D, height, 0.0D), 64);
					
				}
				else
				{
					particle.display(xOff, yOff, zOff, speed, 1, event.getPlayer().getLocation().add(0.0D, height, 0.0D), 64);
				}
				
				
				//particle.display(offsetX, offsetY, offsetZ, speedInit, amount, center, range);
				
				if(length > 1)
				{
					final float xOffFinal = xOff, yOffFinal = yOff, zOffFinal = zOff, speedFinal = speed;
					final Location locFinal = event.getPlayer().getLocation().add(0.0D, height, 0.0D);
					final ParticleColor dataFinal = data;
					for(int i = 1;i < (length + 1);i++)
					{
						if(particle.hasProperty(ParticleProperty.COLORABLE))
						{
							Bukkit.getScheduler().runTaskLater(instance, new Runnable()
							{
								public void run() 
								{
									particle.display(dataFinal, locFinal, 32);
									//particle.display(color, center, range);
								}
					        }, i * 5);
						}
						else
						{
							Bukkit.getScheduler().runTaskLater(instance, new Runnable()
							{
								public void run() 
								{
									particle.display(xOffFinal, yOffFinal, zOffFinal, speedFinal, 1, locFinal, 64);
									//particle.display(offsetX, offsetY, offsetZ, speed, amount, center, range);
								}
					        }, i * 5);
						}
					}
				}
			}
		}
		catch(NullPointerException | ParticleEffect.ParticleVersionException e)
		{
			trailMap.remove(event.getPlayer().getUniqueId().toString());
			removePlayer(event.getPlayer().getUniqueId().toString());
			if(cfg.getBoolean("misc.debug"))
			{
				e.printStackTrace();
			}
			util.printPlain(event.getPlayer(), msg.getString("messages.error.trail-error"));
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(util.stripColours(event.getInventory().getTitle()).contains(util.stripColours(msg.getString("messages.titles.main-menu"))))
		{
			event.setCancelled(true);
			Player player = (Player)event.getWhoClicked();
			//Slot was empty
			if(event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR))
			{
                return;
			}
			else if(event.getRawSlot() >= cfg.getInt("menu.main.size")) //If player clicked a slot in their inventory/hotbar
			{
				return;
			}
			else if(event.getCurrentItem().getType().equals(Material.getMaterial(cfg.getString("trails.remove-trail.material"))))
			{
				if(trailMap.containsKey(player.getUniqueId().toString()))
				{
					trailMap.put(player.getUniqueId().toString(), new PlayerConfig(player.getUniqueId().toString(), ParticleEffect.FOOTSTEP, 0, 0, 0, 0));
					util.printPlain(player, msg.getString("messages.generic.trail-removed"));
				}
				else
				{
					util.printPlain(player, msg.getString("messages.error.no-trail-remove"));
				}
				if(cfg.getBoolean("menu.main.minimise-on-select"))
				{
					player.closeInventory();
				}
				
			}
			else if(event.getCurrentItem().getType().equals(Material.getMaterial(cfg.getString("trails.options-menu.material"))))
			{
				if(player.hasPermission("blivtrails.options"))
				{
					try
					{
						optionsMenu(player);
					}
					catch(NullPointerException e)
					{
						e.printStackTrace();
						util.printPlain(((Player) event.getWhoClicked()), msg.getString("messages.error.no-trail"));
					}
				}
				else
				{
					util.printPlain(player, msg.getString("messages.error.no-permission.options"));
				}
			}
			else
			{
				String particleString = null;
				for(ParticleEffect particleEff : usedTrails)
				{
					particleString = util.trailConfigName(particleEff.toString());
					if(event.getCurrentItem().getType().equals(Material.getMaterial(cfg.getString("trails." + particleString + ".material"))))
					{
						if(player.hasPermission("blivtrails." + particleString))
						{
							doDefaultTrail(player.getUniqueId(), particleEff);
							if(cfg.getBoolean("menu.main.minimise-on-select"))
							{
								player.closeInventory();
							}
						}
						else
						{
							util.printPlain(player, msg.getString("messages.error.no-permission-trail"));
						}
					}
				}
			}
		}
		else if(util.stripColours(event.getInventory().getTitle()).contains(util.stripColours(msg.getString("messages.titles.main-options"))))
		{
			event.setCancelled(true);
			Player player = (Player)event.getWhoClicked();
			PlayerConfig pcfg = getPlayerConfig().get(player.getUniqueId().toString());
			//Slot was empty
			if(event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR))
			{
                return;
			}
			else if(event.getRawSlot() >= cfg.getInt("menu.options.size")) //If player clicked a slot in their inventory/hotbar
			{
				return;
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().equals(msg.getString("messages.options.titles.categories.type")))
			{
				optionsMenuType(player);
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.categories.length")))
			{
				optionsMenuLength(player);
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.categories.height")))
			{
				optionsMenuHeight(player);
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.categories.colour")))
			{
				if(pcfg.getParticle().hasProperty(ParticleProperty.COLORABLE))
				{
					optionsMenuColour(player);
				}
				else
				{
					util.printError(player, msg.getString("messages.error.option-trail-no-support"));
				}
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.back")))
			{
				mainMenu(player);
			}
			else if(event.getCurrentItem().getType().equals(Material.BOOK))
			{
				//Do nothing -- Is just information
			}
			else
			{
				util.printPlain(player, msg.getString("messages.error.no-exist"));
			}
		}
		/*
		 * Sub-Options-Menu Handling
		 */
		else if(util.stripColours(event.getInventory().getTitle()).contains(util.stripColours(msg.getString("messages.titles.type"))))
		{
			event.setCancelled(true);
			Player player = (Player)event.getWhoClicked();
			PlayerConfig pcfg = getPlayerConfig().get(player.getUniqueId().toString());
			//Slot was empty
			if(event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR))
			{
                return;
			}
			else if(event.getRawSlot() >= cfg.getInt("menu.options.size")) //If player clicked a slot in their inventory/hotbar
			{
				return;
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.type.trace")))
			{
				pcfg.setType(1);
				optionsMenuType(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.type.random")))
			{
				if(pcfg.getParticle().hasProperty(ParticleProperty.COLORABLE)) //If the particle is colourable, it is not directional/support randomisation
		        {
		        	util.printError(player, msg.getString("messages.error.option-trail-no-support"));
		        	return;
		        }
				pcfg.setType(2);
				optionsMenuType(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.type.dynamic")))
			{
				if(pcfg.getParticle().hasProperty(ParticleProperty.COLORABLE)) //If the particle is colourable, it is not directional/support randomisation
		        {
		        	util.printError(player, msg.getString("messages.error.option-trail-no-support"));
		        	return;
		        }
				pcfg.setType(3);
				optionsMenuType(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.back")))
			{
				optionsMenu(player);
			}
			else if(event.getCurrentItem().getType().equals(Material.BOOK))
			{
				//Do nothing -- Is just information
			}
			else
			{
				util.printPlain(player, msg.getString("messages.error.no-exist"));
			}
		}
		else if(util.stripColours(event.getInventory().getTitle()).contains(util.stripColours(msg.getString("messages.titles.length"))))
		{
			event.setCancelled(true);
			Player player = (Player)event.getWhoClicked();
			PlayerConfig pcfg = getPlayerConfig().get(player.getUniqueId().toString());
			//Slot was empty
			if(event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR))
			{
                return;
			}
			else if(event.getRawSlot() >= cfg.getInt("menu.options.size")) //If player clicked a slot in their inventory/hotbar
			{
				return;
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.length.short")))
			{
				pcfg.setLength(1);
				optionsMenuLength(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.length.medium")))
			{
				pcfg.setLength(2);
				optionsMenuLength(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.length.long")))
			{
				pcfg.setLength(3);
				optionsMenuLength(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.back")))
			{
				optionsMenu(player);
			}
			else if(event.getCurrentItem().getType().equals(Material.BOOK))
			{
				//Do nothing -- Is just information
			}
			else
			{
				util.printPlain(player, msg.getString("messages.error.no-exist"));
			}
		}
		else if(util.stripColours(event.getInventory().getTitle()).contains(util.stripColours(msg.getString("messages.titles.height"))))
		{
			event.setCancelled(true);
			Player player = (Player)event.getWhoClicked();
			PlayerConfig pcfg = getPlayerConfig().get(player.getUniqueId().toString());
			//Slot was empty
			if(event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR))
			{
                return;
			}
			else if(event.getRawSlot() >= cfg.getInt("menu.options.size")) //If player clicked a slot in their inventory/hotbar
			{
				return;
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.height.feet")))
			{
				pcfg.setHeight(0);
				optionsMenuHeight(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.height.waist")))
			{
				pcfg.setHeight(1);
				optionsMenuHeight(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.height.halo")))
			{
				pcfg.setHeight(2);
				optionsMenuHeight(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getType().equals(Material.BOOK))
			{
				//Do nothing -- Is just information
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.back")))
			{
				optionsMenu(player);
			}
			else
			{
				util.printPlain(player, msg.getString("messages.error.no-exist"));
			}
		}
		else if(util.stripColours(event.getInventory().getTitle()).contains(util.stripColours(msg.getString("messages.titles.colours"))))
		{
			event.setCancelled(true);
			Player player = (Player)event.getWhoClicked();
			PlayerConfig pcfg = getPlayerConfig().get(player.getUniqueId().toString());
			//Slot was empty
			if(event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR))
			{
                return;
			}
			else if(event.getRawSlot() >= cfg.getInt("menu.options.config.colour.size")) //If player clicked a slot in their inventory/hotbar
			{
				return;
			}
			else if(event.getCurrentItem().getItemMeta().getDisplayName().contains(msg.getString("messages.options.titles.back")))
			{
				optionsMenu(player);
			}
			else if(event.getCurrentItem().getType().equals(Material.INK_SACK))
			{
		        if(pcfg.getParticle() == ParticleEffect.NOTE) //Disable some colours which dont exist for notes
		        {
		        	switch(event.getCurrentItem().getDurability())
		        	{
		        		case 0: case 3: case 7: case 8: case 15: util.printError(player, msg.getString("messages.error.option-trail-no-support"));
		        			return;
		        	}
		        }
				pcfg.setColour(event.getCurrentItem().getDurability());
				optionsMenuColour(player); //Set the type, and reload the menu
			}
			else if(event.getCurrentItem().getType().equals(Material.POTION))
			{
				pcfg.setColour(16);
				optionsMenuColour(player); //Set the type, and reload the menu
			}
			else
			{
				util.printPlain(player, msg.getString("messages.error.no-exist"));
			}
		}
	}
	
	public void mainMenu(Player player)
	{
		//try
		//{
			PlayerConfig pcfg = null;
			if(trailMap.containsKey(player.getUniqueId().toString()))
			{
				pcfg = trailMap.get(player.getUniqueId().toString());
			}
			else //Put a temp entry in
			{
				pcfg = new PlayerConfig(player.getUniqueId().toString(), null, 0, 0, 0, 0);
			}
			try
			{
				Inventory inv = Bukkit.createInventory(null, cfg.getInt("menu.main.size"), msg.getString("messages.titles.main-menu"));
				if(cfg.getBoolean("trails.remove-trail.display"))
			    {
					inv.setItem(cfg.getInt("trails.remove-trail.position"), menuItem(cfg.getString("trails.remove-trail.material"), util.translateColours(cfg.getString("trails.remove-trail.name")),
							util.translateColours(cfg.getStringList("trails.remove-trail.lore")), player.hasPermission("blivtrails.remove-trail"), false));
			    }
				String particleString;
				for(ParticleEffect particleEff : usedTrails)
				{
					particleString = util.trailConfigName(particleEff.toString());
					if(!particleString.equals("barrier"))
					{
						inv.setItem(cfg.getInt("trails." + particleString + ".position"), menuItem(cfg.getString("trails." + particleString + ".material"), util.translateColours(cfg.getString("trails." + particleString + ".name")),
								util.translateColours(cfg.getStringList("trails." + particleString + ".lore")), player.hasPermission("blivtrails." + particleString), pcfg.getParticle() == particleEff));
					}
				}
				if(player.hasPermission("blivtrails.trail.barrier"))
			    {
					if(cfg.getBoolean("trails.barrier.display"))
				    {
						inv.setItem(cfg.getInt("trails.barrier.position"), menuItem(cfg.getString("trails.barrier.material"), util.translateColours(cfg.getString("trails.barrier.name")),
								util.translateColours(cfg.getStringList("trails.barrier.lore")), player.hasPermission("blivtrails.barrier"), pcfg.getParticle() == ParticleEffect.BARRIER));
				    }
			    }
			    if(cfg.getBoolean("trails.options-menu.display"))
			    {
			    	inv.setItem(cfg.getInt("trails.options-menu.position"), menuItem(cfg.getString("trails.options-menu.material"), util.translateColours(cfg.getString("trails.options-menu.name")),
			    		util.translateColours(cfg.getStringList("trails.options-menu.lore")), player.hasPermission("blivtrails.options-menu"), false));
			    }
			    player.openInventory(inv);
			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				util.printError(player, msg.getString("messages.error.player-misplaced-gui-option"));
				util.logError(msg.getString("messages.error.misplaced-gui-option") + "\n" + e.getMessage());
			}
	}
	
	
	/*
	 * Options Menus --------------------------------------------------------------------
	 */
	
	public void optionsMenu(Player player) throws NullPointerException
	{
		PlayerConfig pcfg = trailMap.get(player.getUniqueId().toString());
		try
		{
			Inventory inv = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.main-options"));
			if(cfg.getBoolean("menu.options.config.type.enabled"))
			{
				inv.setItem(cfg.getInt("menu.options.config.type.position"), optionsType());
			}
			if(cfg.getBoolean("menu.options.config.length.enabled"))
			{
				inv.setItem(cfg.getInt("menu.options.config.length.position"), optionsLength());
			}
			if(cfg.getBoolean("menu.options.config.height.enabled"))
			{
				inv.setItem(cfg.getInt("menu.options.config.height.position"), optionsHeight());
			}
			if(cfg.getBoolean("menu.options.config.colour.enabled"))
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.position"), optionsColour(pcfg.getParticle()));
			}
	        inv.setItem(cfg.getInt("menu.options.back-button-pos"), backButton());
	        player.openInventory(inv);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			util.printError(player, msg.getString("messages.error.player-misplaced-gui-option"));
			util.logError(msg.getString("messages.error.misplaced-gui-option") + "\n" + e.getMessage());
		}
	}
	
	/*
	 * Sub Options Menus
	 */
	
	public void optionsMenuType(Player player)
	{
		PlayerConfig pcfg = trailMap.get(player.getUniqueId().toString());
		try
		{
			Inventory inv = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.type"));
			if(cfg.getBoolean("menu.options.config.type.trace"))
			{
				inv.setItem(3, optionsTypeTrace(pcfg.getType() == 1));
				inv.setItem(12, informationItem(msg.getStringList("messages.information.type.trace")));
			}
			if(cfg.getBoolean("menu.options.config.type.random"))
			{
				inv.setItem(4, optionsTypeRandom(pcfg.getType() == 2, pcfg.getParticle()));
				inv.setItem(13, informationItem(msg.getStringList("messages.information.type.random")));
			}
			if(cfg.getBoolean("menu.options.config.type.dynamic"))
			{
				inv.setItem(5, optionsTypeDynamic(pcfg.getType() == 3, pcfg.getParticle()));
				inv.setItem(14, informationItem(msg.getStringList("messages.information.type.dynamic")));
			}
	        inv.setItem(cfg.getInt("menu.options.back-button-pos"), backButton());
	        player.openInventory(inv);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			util.printError(player, msg.getString("messages.error.player-misplaced-gui-option"));
			util.logError(msg.getString("messages.error.misplaced-gui-option") + "\n" + e.getMessage());
		}
		
	}
	
	public void optionsMenuLength(Player player)
	{
		PlayerConfig pcfg = trailMap.get(player.getUniqueId().toString());
		try
		{
			Inventory inv = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.length"));
			if(cfg.getBoolean("menu.options.config.length.short"))
			{
				inv.setItem(3, optionsLengthShort(pcfg.getLength() == 1));
			}
			if(cfg.getBoolean("menu.options.config.length.medium"))
			{
				inv.setItem(4, optionsLengthMedium(pcfg.getLength() == 2));
			}
			if(cfg.getBoolean("menu.options.config.length.long"))
			{
				inv.setItem(5, optionsLengthLong(pcfg.getLength() == 3));
			}
	        inv.setItem(13, informationItem(msg.getStringList("messages.information.length.info")));
	        inv.setItem(cfg.getInt("menu.options.back-button-pos"), backButton());
	        player.openInventory(inv);
		}
        catch(ArrayIndexOutOfBoundsException e)
		{
			util.printError(player, msg.getString("messages.error.player-misplaced-gui-option"));
			util.logError(msg.getString("messages.error.misplaced-gui-option") + "\n" + e.getMessage());
		}
	}
	
	public void optionsMenuHeight(Player player)
	{
		try
		{
			PlayerConfig pcfg = trailMap.get(player.getUniqueId().toString());
			Inventory inv = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.height"));
			if(cfg.getBoolean("menu.options.config.height.feet"))
			{
				inv.setItem(3, optionsHeightFeet(pcfg.getHeight() == 0));
			}
			if(cfg.getBoolean("menu.options.config.height.waist"))
			{
				inv.setItem(4, optionsHeightWaist(pcfg.getHeight() == 1));
			}
			if(cfg.getBoolean("menu.options.config.height.halo"))
			{
				inv.setItem(5, optionsHeightHead(pcfg.getHeight() == 2));
			}
	        inv.setItem(13, informationItem(msg.getStringList("messages.information.height.info")));
	        inv.setItem(cfg.getInt("menu.options.back-button-pos"), backButton());
	        player.openInventory(inv);
		}
        catch(ArrayIndexOutOfBoundsException e)
		{
			util.printError(player, msg.getString("messages.error.player-misplaced-gui-option"));
			util.logError(msg.getString("messages.error.misplaced-gui-option") + "\n" + e.getMessage());
		}
	}
	
	public void optionsMenuColour(Player player)
	{
		try
		{
			PlayerConfig pcfg = trailMap.get(player.getUniqueId().toString());
			Inventory inv = Bukkit.createInventory(null, cfg.getInt("menu.options.config.colour.size"), msg.getString("messages.titles.colours"));
			if(cfg.getInt("menu.options.config.colour.black-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.black-pos"), optionsColourItem(pcfg.getColour() == 0, 0, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.red-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.red-pos"), optionsColourItem(pcfg.getColour() == 1, 1, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.green-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.green-pos"), optionsColourItem(pcfg.getColour() == 2, 2, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.brown-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.brown-pos"), optionsColourItem(pcfg.getColour() == 3, 3, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.blue-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.blue-pos"), optionsColourItem(pcfg.getColour() == 4, 4, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.purple-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.purple-pos"), optionsColourItem(pcfg.getColour() == 5, 5, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.cyan-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.cyan-pos"), optionsColourItem(pcfg.getColour() == 6, 6, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.light-grey-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.light-grey-pos"), optionsColourItem(pcfg.getColour() == 7, 7, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.grey-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.grey-pos"), optionsColourItem(pcfg.getColour() == 8, 8, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.pink-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.pink-pos"), optionsColourItem(pcfg.getColour() == 9, 9, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.lime-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.pink-pos"), optionsColourItem(pcfg.getColour() == 10, 10, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.lime-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.yellow-pos"), optionsColourItem(pcfg.getColour() == 11, 11, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.light-blue-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.light-blue-pos"), optionsColourItem(pcfg.getColour() == 12, 12, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.magenta-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.magenta-pos"), optionsColourItem(pcfg.getColour() == 13, 13, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.orange-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.orange-pos"), optionsColourItem(pcfg.getColour() == 14, 14, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.white-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.white-pos"), optionsColourItem(pcfg.getColour() == 15, 15, pcfg.getParticle()));
			}
			if(cfg.getInt("menu.options.config.colour.random-pos") != -1)
			{
				inv.setItem(cfg.getInt("menu.options.config.colour.random-pos"), optionsColourItem(pcfg.getColour() == 16, 16, pcfg.getParticle()));
			}
	        inv.setItem(cfg.getInt("menu.options.config.colour.back-button-pos"), backButton());
	        player.openInventory(inv);
		}
        catch(ArrayIndexOutOfBoundsException e)
		{
			util.printError(player, msg.getString("messages.error.player-misplaced-gui-option"));
			util.logError(msg.getString("messages.error.misplaced-gui-option") + "\n" + e.getMessage());
		}
	}
	
	/*
	 * Options Type
	 */
	
	public ItemStack optionsType()
	{
		ItemStack item = new ItemStack(Material.GLASS_BOTTLE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.categories.type"));
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsTypeTrace(boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.type.trace"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsTypeRandom(boolean isEnabled, ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.type.random"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        List<String> canUse = Arrays.asList(msg.getString("messages.options.supports-randomisation"));
        if(particle.hasProperty(ParticleProperty.COLORABLE))
        {
        	canUse = Arrays.asList(msg.getString("messages.options.doesnt-support-randomisation"));
        }
        if(meta.getLore() == null)
        {
            meta.setLore(canUse);
        }
        else
        {
        	meta.getLore().add(canUse.toString());
        }
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsTypeDynamic(boolean isEnabled, ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.type.dynamic"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        List<String> canUse = Arrays.asList(msg.getString("messages.options.doesnt-support-dynamic"));
        if(particle.hasProperty(ParticleProperty.DIRECTIONAL))
        {
        	canUse = Arrays.asList(msg.getString("messages.options.supports-dynamic"));
        }
        if(meta.getLore() == null)
        {
            meta.setLore(canUse);
        }
        else
        {
        	meta.getLore().add(canUse.toString());
        }
        item.setItemMeta(meta);
        return item;
	}
	
	/*
	 * Options Length
	 */
	
	public ItemStack optionsLength()
	{
		ItemStack item = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.categories.length"));
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsLengthShort(boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.length.short"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsLengthMedium(boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.length.medium"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsLengthLong(boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.length.long"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        item.setItemMeta(meta);
        return item;
	}
	
	/*
	 * Options Height
	 */
	
	public ItemStack optionsHeight()
	{
		ItemStack item = new ItemStack(Material.FENCE, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.categories.height"));
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsHeightFeet(boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.height.feet"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsHeightWaist(boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.height.waist"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsHeightHead(boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)8);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.height.halo"));
        if(isEnabled)
        {
        	meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	item.setDurability((short) 10);
        }
        item.setItemMeta(meta);
        return item;
	}
	
	/*
	 * Options Colour
	 */
	
	public ItemStack optionsColour(ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 14);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.categories.colour"));
        List<String> canUse = Arrays.asList(msg.getString("messages.options.doesnt-support-colours"));
        if(particle.hasProperty(ParticleProperty.COLORABLE))
        {
        	canUse = Arrays.asList(msg.getString("messages.options.supports-colours"));
        }
        meta.setLore(canUse);
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack optionsColourItem(boolean isEnabled, int value, ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short)value);
		if(value == 16)
		{
			item = new ItemStack(Material.POTION, 1);
		}
        ItemMeta meta = item.getItemMeta();
        String colour = "";
        switch(value)
        {
	        case 0: colour = ChatColor.DARK_GRAY + "Black"; break;
	        case 1: colour = ChatColor.RED + "Red"; break;
	        case 2: colour = ChatColor.DARK_GREEN + "Green"; break;
	        case 3: colour = ChatColor.GOLD + "Brown"; break;
	        case 4: colour = ChatColor.BLUE + "Blue"; break;
	        case 5: colour = ChatColor.DARK_PURPLE + "Purple"; break;
	        case 6: colour = ChatColor.DARK_AQUA + "Cyan"; break;
	        case 7: colour = ChatColor.GRAY + "Light Grey"; break;
	        case 8: colour = ChatColor.DARK_GRAY + "Gray"; break;
	        case 9: colour = ChatColor.LIGHT_PURPLE + "Pink"; break;
	        case 10: colour = ChatColor.GREEN + "Lime"; break;
	        case 11: colour = ChatColor.YELLOW + "Yellow"; break;
	        case 12: colour = ChatColor.AQUA + "Light Blue"; break;
	        case 13: colour = ChatColor.LIGHT_PURPLE + "Magenta"; break;
	        case 14: colour = ChatColor.GOLD + "Orange"; break;
	        case 16: colour = "Random"; break;
	        default: colour = "White"; break;
        }
        meta.setDisplayName(colour);
        if(isEnabled)
        {
        	if(meta.getLore() == null)
        	{
        		meta.setLore(Arrays.asList(msg.getString("messages.generic.enabled-lore")));
        	}
        	else
        	{
        		List<String> enabled = meta.getLore();
        		enabled.add(msg.getString("messages.generic.enabled-lore"));
        		meta.setLore(enabled);
        	}
        }
        if(particle == ParticleEffect.NOTE) //Disable some colours which dont exist for notes
        {
        	String isDisabled = null;
        	switch(value)
        	{
        		case 0: case 3: case 7: case 8: case 15: isDisabled = msg.getString("messages.options.doesnt-apply-to-note"); break;
        	}
        	if(isDisabled != null)
        	{
        		if(meta.getLore() == null)
            	{
            		meta.setLore(Arrays.asList(isDisabled));
            	}
            	else
            	{
            		List<String> lore = meta.getLore();
            		lore.add(isDisabled);
            		meta.setLore(lore);
            	}
        	}
        }
        item.setItemMeta(meta);
        return item;
	}
	
	/*
	 * Other
	 */
	
	public ItemStack backButton()
	{
		ItemStack item = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.back"));
        item.setItemMeta(meta);
        return item;
	}
	
	public ItemStack informationItem(List<String> list)
	{
		ItemStack item = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(msg.getString("messages.options.titles.information"));
        meta.setLore(list);
        item.setItemMeta(meta);
        return item;
	}
	
	/**
	 * 
	 * @param toReplace The string which includes the variable to be replaced
	 * @param replacee The string including the variable to be used in the toReplace String
	 * @return
	 */
	public String addVariable(String toReplace, String replacee)
	{
		Pattern variablePattern = Pattern.compile("[%](\\S+)[%]");
		String fixedString = variablePattern.matcher(toReplace).replaceAll(replacee);
		return fixedString;
	}
	
	public void doDefaultTrail(UUID uuid, ParticleEffect particle)
	{
		String particleString = particle.toString();
		particleString = util.trailConfigName(particleString);
		//Do Particle Defaults
		String typeString = cfg.getString("trails." + particleString + ".options.type"), lengthString = cfg.getString("trails." + particleString + ".options.length"),
				heightString = cfg.getString("trails." + particleString + ".options.height"), colourString = cfg.getString("trails." + particleString + ".options.colour");
		int type = 1, length = 1, height = 0, colour = 0;
		
		//Set it to the first, and change it if its different.
		if(lengthString != null)
		{
			switch(typeString)
			{
				case "random": type = 2; break;
				case "dynamic": type = 3; break;
			}
		}
		if(lengthString != null)
		{
			switch(lengthString)
			{
				case "medium": length = 2; break;
				case "long": length = 3; break;
			}
		}
		if(heightString != null)
		{
			switch(heightString)
			{
				case "waist": height = 1; break;
				case "halo": height = 2; break;
			}
		}
		if(colourString != null)
		{
			switch(colourString)
			{
				case "red": colour = 1; break;
				case "dark green": colour = 2; break;
				case "brown": colour = 3; break;
				case "dark blue": colour = 4; break;
				case "purple": colour = 5; break;
				case "cyan": colour = 6; break;
				case "light grey": case "light gray": colour = 7; break;
				case "grey": case "gray": colour = 8; break;
				case "pink": colour = 9; break;
				case "lime": colour = 10; break;
				case "yellow": colour = 11; break;
				case "light blue": colour = 12; break;
				case "magenta": colour = 13; break;
				case "orange": colour = 14; break;
				case "black": colour = 15;  break;
				case "random": colour = 16; break;
			}
		}
		
		
		/*//Player has had a trail before //TODO: Implement player options that traverse trails
		if(trailMap.containsKey(uuid))
		{
			PlayerConfig pcfg = trailMap.get(uuid);
			pcfg.setParticle(particle);
			if(type != 0)
			{
				pcfg.setType(type);
			}
			if(length != 0)
			{
				pcfg.setLength(length);
			}
			pcfg.setHeight(height);
			util.printPlain(Bukkit.getPlayer(uuid), addVariable(msg.getString("messages.generic.trail-applied"), cfg.getString("trails" + util.trailConfigName(particle.toString()) + ".name")));
		}*/
		
		//Trail for the first time
		String trailName = msg.getString("messages.generic.trail-applied");
		if(!cfg.getBoolean("misc.trail-name-colour"))
		{
			trailName = util.stripColours(trailName);
		}
		util.printPlain(Bukkit.getPlayer(uuid), addVariable(trailName, cfg.getString("trails." + util.trailConfigName(particle.toString()) + ".name")));
		getPlayerConfig().put(uuid.toString(), new PlayerConfig(uuid.toString(), particle, type, length, height, colour));
	}
	
	public void loadTrail(Player player)
	{
		if(flatfile == null)
		{
			try
			{
				Connection conn = null;
				try
				{
					conn = sql.getConnection();
					Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery("SELECT * FROM bliv_trails WHERE uuid='" + player.getUniqueId().toString() + "';");
					if(rs.next())
					{
						ParticleEffect particleEff = null;
						for(ParticleEffect pEff : ParticleEffect.values())
						{
							if(pEff.toString().equals(rs.getString("particle")))
							{
								if(pEff.equals(ParticleEffect.FOOTSTEP))
								{
									return;
								}
								particleEff = pEff;
								util.logDebug("Equal to " + pEff.toString());
								break;
							}
						}
						//sqlite.query("CREATE TABLE table_name (uuid VARCHAR(50) PRIMARY_KEY, particle VARCHAR(50), type INT, length INT, height INT);");
						trailMap.put(player.getUniqueId().toString(), new PlayerConfig(player.getUniqueId().toString(), particleEff,
								rs.getInt("type"), rs.getInt("length"), rs.getInt("height"), rs.getInt("colour")));
						util.logDebug("Loaded " + player.getName());
						util.logDebug(player.getUniqueId().toString() + particleEff +
								rs.getInt("type") + rs.getInt("length") + rs.getInt("height") + rs.getInt("colour"));
					}
					conn.close();
				}
				catch(SQLException e)
				{
					e.printStackTrace();
					util.logError("SQLException for: " + player.getName());
					try
					{
						conn.close();
					} 
					catch (SQLException e2)
					{
						e.printStackTrace();
					}
				}
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
				//Player has no trail config -- Do nothing
			}
		}
		else
		{
			/*
			 * dataSplit[0] == Particle String
			 * dataSplit[1] == Type
			 * dataSplit[2] == Length
			 * dataSplit[3] == Height
			 * dataSplit[4] == Colour
			 */
			String data = flatfile.loadEntry(player.getUniqueId().toString());
			
			try
			{
				data.length();
			}
			catch(NullPointerException e)
			{
				util.logDebug("Player " + player.getName() + " has no trail config.");
				return;
			}
			
			try
			{
				String[] dataSplit = data.split("[,]");
				ParticleEffect particleEff = null;
				for(ParticleEffect pEff : ParticleEffect.values())
				{
					if(pEff.toString().equals(dataSplit[0]))
					{
						if(pEff.equals(ParticleEffect.FOOTSTEP))
						{
							return;
						}
						particleEff = pEff;
						util.logDebug("Equal to " + pEff.toString());
						break;
					}
				}
				trailMap.put(player.getUniqueId().toString(), new PlayerConfig(player.getUniqueId().toString(), particleEff,
						Integer.parseInt(dataSplit[1]), Integer.parseInt(dataSplit[2]), Integer.parseInt(dataSplit[3]), Integer.parseInt(dataSplit[4])));
			}
			catch(NullPointerException e)
			{
				util.logDebug("Player failed loading: (NPE) " + player.getName());
				if(cfg.getBoolean("misc.debug"))
				{
					e.printStackTrace();
				}
				//Player is not in the file
			}
		}
	}
	
	public void saveTrail(OfflinePlayer player)
	{
		PlayerConfig pcfg = trailMap.get(player.getUniqueId().toString());
		if(pcfg.getParticle().equals(ParticleEffect.FOOTSTEP))
		{
			removePlayer(player.getUniqueId().toString());
			return;
		}
		if(pcfg != null)
		{
			util.logDebug(player.getName() + "'s trail config is not null");
			if(flatfile == null)
			{
				util.logDebug("Using MySQL to save " + player.getName() + "'s trail data");
				try
				{
					Connection conn = null;
					try
					{
						conn = sql.getConnection();
						Statement st = conn.createStatement();
						ResultSet rs = st.executeQuery("SELECT uuid FROM bliv_trails WHERE uuid='" + player.getUniqueId() + "';");
						if(pcfg.getParticle().equals(ParticleEffect.FOOTSTEP))
						{
							removePlayer(player.getUniqueId().toString());
							return;
						}
						if(rs.next())
						{
							st.execute("UPDATE bliv_trails SET particle='" + pcfg.getParticle().toString() + "', type='"
									+ pcfg.getType() + "', length='" + pcfg.getLength() + "', height='" + pcfg.getHeight() + "', colour='" + pcfg.getColour() + "' WHERE uuid='" + pcfg.getUUID() + "';");
						}
						else
						{
							st.execute("INSERT INTO bliv_trails(uuid,particle,type,length,height,colour) VALUES('" + pcfg.getUUID() + "', '" + pcfg.getParticle().toString() + "','"
									+ pcfg.getType() + "','" + pcfg.getLength() + "','" + pcfg.getHeight() + "', '" + pcfg.getColour() + "');");
						}
						util.logDebug("Saved " + player.getName() + "'s trail config to file");
						util.logDebug("UPDATE bliv_trails SET particle='" + pcfg.getParticle().toString() + "', type='"
						+ pcfg.getType() + "', length='" + pcfg.getLength() + "', height='" + pcfg.getHeight() + "', colour='" + pcfg.getColour() + "' WHERE uuid='" + pcfg.getUUID() + "';");
						conn.close();
					}
					catch(SQLException e2)
					{
						try
						{
							conn.close();
						}
						catch(SQLException e3)
						{
							//Give up
							e2.printStackTrace();
							e3.printStackTrace();
							util.logError("SQLException for: " + player.getName());
						}
					}
				}
				catch(NullPointerException e)
				{
					//Player has no trail config
					e.printStackTrace();
				}
			}
			else
			{
				/*
				 * dataSplit[0] == UUID
				 * dataSplit[1] == Particle String
				 * dataSplit[2] == Type
				 * dataSplit[3] == Length
				 * dataSplit[4] == Height
				 * dataSplit[5] == Colour
				 */
				try
				{
					String data = pcfg.getParticle().toString() + "," + pcfg.getType() + "," + pcfg.getLength() + "," + pcfg.getHeight() + "," + pcfg.getColour();
					flatfile.saveEntry(player.getUniqueId().toString(), data);
					util.logDebug(pcfg.getParticle().toString() + "," + pcfg.getType() + "," + pcfg.getLength() + "," + pcfg.getHeight() + "," + pcfg.getColour());
					util.logDebug("Successfully saved " + player.getName() + "'s trail config to file");
				}
				catch(NullPointerException e)
				{
					//No data
				}
			}
		}
	}
	
	/**
	 * 
	 * @param uuid UUID String of target player
	 * @param particleString ENUM name of the particle
	 * @param typeString String representation of type (from config)
	 * @param lengthString String representation of length (from config)
	 * @param heightString String representation of height (from config)
	 * @param colourString String representation of colour (from config)
	 * @return Success/Error Message Output
	 */
	public String addTrail(String uuid, String particleString, String typeString, String lengthString, String heightString, String colourString)
	{
		/*
		 * Args format:
		 * args[0] = uuid
		 * args[0] = particle
		 * args[1] = type
		 * args[2] = length
		 * args[3] = height
		 * args[4] = colour
		 */
		ParticleEffect particleEff = ParticleEffect.FOOTSTEP;
		String particleStringLoop = "";
		for(ParticleEffect pEff : usedTrails)
		{
			particleStringLoop = util.stripColours(cfg.getString("trails." + util.trailConfigName(pEff.toString()) + ".name"));
			if(particleStringLoop.equalsIgnoreCase(particleString))
			{
				particleEff = pEff;
				util.logDebug(particleString + " equal to " + particleStringLoop);
				break;
			}
			else
			{
				util.logDebug(particleString + " not equal to " + particleStringLoop);
			}
		}
		if(particleEff.equals(ParticleEffect.FOOTSTEP))
		{
			return ChatColor.RED + "Trail effect does not exist. (/trailadmin particles)";
		}
		
		int type = 1, length = 1, height = 0, colour = 0;
		if(particleEff.equals(ParticleEffect.BARRIER)) 
		{
			//Barriers dont support anything. Give up. Leave everything default
		}
		if(typeString.toLowerCase().equals("")) //Use Trail Defaults
		{
			particleDefaultStorage defaults = trailDefaults.getDefaults(trailDefaults.trailConfigName(particleEff.toString()));
			type = defaults.getInt("type");
			length = defaults.getInt("length");
			height = defaults.getInt("height");
			colour = defaults.getInt("colour");
		}
		else //Use defined inputs
		{
			if(typeString != null) //type
			{
				switch(typeString.toLowerCase())
				{
					case "trace": type = 1; break;
					case "random": type = 2; break;
					case "dynamic": type = 3; break;
					default: return "&cInvalid Type | (trace, random, dynamic)";
				}
			}
			if(lengthString != null)
			{
				switch(lengthString.toLowerCase())
				{
					case "short": length = 1; break;
					case "medium": length = 2; break;
					case "long": length = 3; break;
					default: return "&cInvalid Length | (short, medium, long)";
				}
			}
			if(heightString != null)
			{
				switch(heightString.toLowerCase())
				{
					case "feet": height = 0; break;
					case "waist": height = 1; break;
					case "halo": height = 2; break;
					default: return "&cInvalid Height | (feet, waist, halo)";
				}
			}
			if(colourString != null)
			{
				switch(colourString.toLowerCase())
				{
					case "white": colour = 0; break;
					case "red": colour = 1; break;
					case "dark green": colour = 2; break;
					case "brown": colour = 3; break;
					case "dark blue": colour = 4; break;
					case "purple": colour = 5; break;
					case "cyan": colour = 6; break;
					case "light grey": case "light gray": colour = 7; break;
					case "grey": case "gray": colour = 8; break;
					case "pink": colour = 9; break;
					case "lime": colour = 10; break;
					case "yellow": colour = 11; break;
					case "light blue": colour = 12; break;
					case "magenta": colour = 13; break;
					case "orange": colour = 14; break;
					case "black": colour = 15;  break;
					case "random": colour = 16; break;
					default: return "&cInvalid Colour. See /trailadmin colours | for colours";
				}
			}
		}
		trailMap.put(uuid.toString(), new PlayerConfig(uuid.toString(), particleEff, type, length, height, colour));
		util.logDebug("Trail Successfully Applied");
		return "&aTrail Successfully Applied";
	}
	
	public String removePlayer(String uuid)
	{
		PlayerConfig pcfg = trailMap.get(uuid);
		if(pcfg != null)
		{
			util.logDebug(Bukkit.getPlayer(UUID.fromString(uuid)).getName() + "'s trail config is not null");
			if(flatfile == null)
			{
				util.logDebug("Using MySQL to remove " + Bukkit.getPlayer(UUID.fromString(uuid)).getName() + "'s trail data");
				Connection conn = null;
				try
				{
					conn = sql.getConnection();
					Statement st = conn.createStatement();
					trailMap.put(uuid.toString(), new PlayerConfig(uuid, ParticleEffect.FOOTSTEP, 0, 0, 0, 0));
					boolean rs = st.execute("DELETE FROM bliv_trails WHERE uuid='" + uuid + "';");
					conn.close();
					if(rs == true) //If player has SQL Entry and HashMap Entry
					{
						util.printPlain(Bukkit.getPlayer(UUID.fromString(uuid)), addVariable(msg.getString("messages.generic.force-remove-player"), Bukkit.getPlayer(UUID.fromString(uuid)).getName()));
						util.logDebug("Player " + Bukkit.getPlayer(UUID.fromString(uuid)).getName() + " was removed from the db");
						return msg.getString("messages.generic.force-remove-receive");
					}
					else //If player only has HashMap entry
					{
						return msg.getString("messages.generic.force-remove-receive");
					}
				}
				catch(SQLException e2)
				{
					try
					{
						conn.close();
						return msg.getString("messages.error.unexpected");
					}
					catch(SQLException e3)
					{
						//Give up
						e2.printStackTrace();
						e3.printStackTrace();
						return msg.getString("messages.error.unexpected");
					}
				}
			}
			else
			{
				try
				{
					util.logDebug("Using Flatfile to remove " + Bukkit.getPlayer(UUID.fromString(uuid)).getName() + "'s trail data");
					flatfile.removeEntry(uuid);
					flatfile.saveToFile();
					util.printPlain(Bukkit.getPlayer(UUID.fromString(uuid)), addVariable(msg.getString("messages.generic.force-remove-player"), Bukkit.getPlayer(UUID.fromString(uuid)).getName()));
				}
				catch(NullPointerException e)
				{
					//No data
				}
			}
		}
		else
		{
			return msg.getString("messages.error.player-no-trail");
		}
		return msg.getString("messages.error.unexpected");
	}
	
	public static ItemStack menuItem(String material, String name, List<String> lore, boolean hasPermission, boolean isSelected)
	{
		ItemStack item = new ItemStack(Material.getMaterial(material), 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        if(hasPermission)
        {
        	if(meta.getLore() == null)
        	{
        		meta.setLore(Arrays.asList(msg.getString("messages.indicators.have-permission")));
        	}
        	else
        	{
        		List<String> lorePerm = meta.getLore();
        		lorePerm.add(msg.getString("messages.indicators.have-permission"));
        		meta.setLore(lorePerm);
        	}
        }
        else
        {
        	if(meta.getLore() == null)
        	{
        		meta.setLore(Arrays.asList(msg.getString("messages.indicators.dont-have-permission")));
        	}
        	else
        	{
        		List<String> lorePerm = meta.getLore();
        		lorePerm.add(msg.getString("messages.indicators.dont-have-permission"));
        		meta.setLore(lorePerm);
        	}
        }
        if(isSelected)
        {
        	if(meta.getLore() == null)
        	{
        		meta.setLore(Arrays.asList(msg.getString("messages.indicators.trail-selected")));
        	}
        	else
        	{
        		List<String> lorePerm = meta.getLore();
        		lorePerm.add(msg.getString("messages.indicators.trail-selected"));
        		meta.setLore(lorePerm);
        	}
        }
        item.setItemMeta(meta);
        return item;
	}
	
	public boolean isVanished(Player player) throws ClassNotFoundException //TODO: Add Essentials Vanish
	{
		boolean isVanished = false;
		if(vanishHook == 1) //VanishNoPacket
		{
			try
			{
				isVanished = ((VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager().isVanished(player);
			}
			catch(NullPointerException | NoClassDefFoundError e)
			{
				util.logDebug("VanishNoPacket was called, but isn't loaded.");
				//VanishNoPacket isnt loaded on the server
			}
		}
		else if(vanishHook == 2) //Essentials Vanish
		{
			
		}
		
        return isVanished;
    }
	
	public void vanishEnabled(boolean enabled)
	{
		vanishEnabled = enabled;
	}
	
	public void vanishHook(int i)
	{
		vanishHook = i;
	}
	
	public HashMap<String, PlayerConfig> getPlayerConfig()
	{
		return trailMap;
	}
	
}