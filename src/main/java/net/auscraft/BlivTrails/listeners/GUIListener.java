package net.auscraft.BlivTrails.listeners;

import com.darkblade12.ParticleEffect.ParticleEffect;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.util.BUtil;
import net.auscraft.BlivTrails.util.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.auscraft.BlivTrails.TrailManager.usedTrails;

/**
 * Created by OhBlihv (Chris) on 31/03/2016.
 * This file is part of the project BlivTrails
 */
public class GUIListener implements Listener
{

	//Keeping these here since 'caching' all variables is bad.
	private static FlatFile cfg = null;
	private static Messages msg = null;

	public static void reload()
	{
		if(cfg == null)
		{
			cfg = FlatFile.getInstance();
		}
		else
		{
			cfg.reloadFile();
		}

		if(msg == null)
		{
			msg = Messages.getInstance();
		}
		else
		{
			msg.reloadFile();
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(event.getInventory() == null || event.getInventory().getTitle() == null ||
				   event.getRawSlot() < 0 || event.getRawSlot() >= 54)
		{
			return;
		}

		if (event.getInventory().getTitle().equals(msg.getString("messages.titles.main-menu")))
		{
			event.setCancelled(true);

			if (event.getRawSlot() >= cfg.getInt("menu.main.size"))
			{
				return;
			}

			Player player = (Player) event.getWhoClicked();

			if (event.getRawSlot() == cfg.getInt("trails.remove-trail.position")) //Remove Trail
			{
				if (TrailManager.getTrailMap().containsKey(player.getUniqueId()))
				{
					TrailManager.getTrailMap().put(player.getUniqueId(), new PlayerConfig(player.getUniqueId(), ParticleEffect.FOOTSTEP, 0, 0, 0, 0));
					int taskId = TrailManager.getTaskMap().remove(player.getUniqueId());
					if(taskId > 0)
					{
						Bukkit.getServer().getScheduler().cancelTask(taskId);
					}
					TrailManager.removePlayer(player.getUniqueId());
					BUtil.printPlain(player, msg.getString("messages.generic.trail-removed"));
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.error.no-trail-remove"));
				}
				if (cfg.getBoolean("menu.main.minimise-on-select"))
				{
					player.closeInventory();
				}

			}
			else if (event.getRawSlot() == cfg.getInt("trails.options-menu.position"))
			{
				if (player.hasPermission("blivtrails.options"))
				{
					PlayerConfig playerConfig = TrailManager.getTrailMap().get(player.getUniqueId());
					if(playerConfig == null || playerConfig.getParticle() == null || playerConfig.getParticle() == ParticleEffect.FOOTSTEP)
					{
						BUtil.printPlain(event.getWhoClicked(), msg.getString("messages.error.no-trail"));
						return;
					}

					if(!optionsMenu(player))
					{
						BUtil.printPlain(event.getWhoClicked(), msg.getString("messages.error.no-trail"));
					}
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.options.base"));
				}
			}
			else
			{
				String particleString;
				for (ParticleEffect particleEff : usedTrails)
				{
					particleString = BUtil.trailConfigName(particleEff.toString());
					if (event.getCurrentItem().getType().equals(Material.getMaterial(cfg.getString("trails." + particleString + ".material"))))
					{
						if(!particleEff.isSupported())
						{
							player.sendMessage("§cThis trail is not supported by your server version.");
							return;
						}

						if (player.hasPermission("blivtrails." + particleString))
						{
							TrailManager.doDefaultTrail(player.getUniqueId(), particleEff);
							if (cfg.getBoolean("menu.main.minimise-on-select"))
							{
								player.closeInventory();
							}
						}
						else
						{
							BUtil.printPlain(player, msg.getString("messages.no-permission.trail"));
						}
						break; //Don't loop through all trails once we've found ours
					}
				}
			}
		}
		else if (event.getInventory().getTitle().equals(msg.getString("messages.titles.main-options")))
		{
			event.setCancelled(true);

			if (event.getRawSlot() >= 18)
			{
				return;
			}

			Player player = (Player) event.getWhoClicked();
			PlayerConfig playerConfig = TrailManager.getTrailMap().get(player.getUniqueId());

			if (event.getRawSlot() == cfg.getInt("menu.options.config.type.position"))
			{
				if(player.hasPermission("blivtrails.options.type"))
				{
					optionsMenuType(player);
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.type.base"));
				}

			}
			else if (event.getRawSlot() == cfg.getInt("menu.options.config.length.position"))
			{
				if(player.hasPermission("blivtrails.options.length"))
				{
					optionsMenuLength(player);
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.length.base"));
				}
			}
			else if (event.getRawSlot() == cfg.getInt("menu.options.config.height.position"))
			{
				if(player.hasPermission("blivtrails.options.height"))
				{
					optionsMenuHeight(player);
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.height.base"));
				}
			}
			else if (event.getRawSlot() == cfg.getInt("menu.options.config.colour.position"))
			{
				if (playerConfig.getParticle().hasProperty(ParticleEffect.ParticleProperty.COLORABLE) && !playerConfig.getParticle().equals(ParticleEffect.FOOTSTEP))
				{
					if(player.hasPermission("blivtrails.options.colour"))
					{
						optionsMenuColour(player);
					}
					else
					{
						BUtil.printPlain(player, msg.getString("messages.no-permission.length.base"));
					}
				}
				else
				{
					BUtil.printError(player, msg.getString("messages.error.option-trail-no-support"));
				}
			}
			else if (event.getRawSlot() == cfg.getInt("menu.options.back-button.position"))
			{
				mainMenu(player);
			}
		}
		/*
		 * Sub-Options-Menu Handling
		 */
		else if (event.getInventory().getTitle().equals(msg.getString("messages.titles.type")))
		{
			event.setCancelled(true);

			if (event.getRawSlot() >= 18)
			{
				return;
			}

			Player player = (Player) event.getWhoClicked();
			PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());

			//Here come the hardcoded options positions
			if (event.getRawSlot() == 3)
			{
				if(player.hasPermission("blivtrails.options.type.trace"))
				{
					pcfg.setType(1);
					optionsMenuType(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.type.trace"));
				}
			}
			if (event.getRawSlot() == 4)
			{
				if(player.hasPermission("blivtrails.options.type.random"))
				{
					pcfg.setType(2);
					optionsMenuType(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.type.random"));
				}
			}
			if (event.getRawSlot() == 5)
			{
				if (pcfg.getParticle().hasProperty(ParticleEffect.ParticleProperty.COLORABLE) ||
						!pcfg.getParticle().hasProperty(ParticleEffect.ParticleProperty.DIRECTIONAL))
				{
					BUtil.printError(player, msg.getString("messages.error.option-trail-no-support"));
					return;
				}

				if(player.hasPermission("blivtrails.options.type.dynamic"))
				{
					pcfg.setType(3);
					optionsMenuType(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.type.dynamic"));
				}
			}
			else if (event.getRawSlot() == cfg.getInt("menu.options.back-button.position"))
			{
				optionsMenu(player);
			}
		}
		else if (event.getInventory().getTitle().equals(msg.getString("messages.titles.length")))
		{
			event.setCancelled(true);

			if (event.getRawSlot() >= 18)
			{
				return;
			}

			Player player = (Player) event.getWhoClicked();
			PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());

			if (event.getRawSlot() == 3)
			{
				if(player.hasPermission("blivtrails.options.length.short"))
				{
					pcfg.setLength(1);
					optionsMenuLength(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.length.short"));
				}
			}
			else if (event.getRawSlot() == 4)
			{
				if(player.hasPermission("blivtrails.options.length.medium"))
				{
					pcfg.setLength(2);
					optionsMenuLength(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.length.medium"));
				}
			}
			else if (event.getRawSlot() == 5)
			{
				if(player.hasPermission("blivtrails.options.length.long"))
				{
					pcfg.setLength(3);
					optionsMenuLength(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.length.long"));
				}
			}
			else if (event.getRawSlot() == cfg.getInt("menu.options.back-button.position"))
			{
				optionsMenu(player);
			}
		}
		else if (event.getInventory().getTitle().equals(msg.getString("messages.titles.height")))
		{
			event.setCancelled(true);

			if (event.getRawSlot() >= 18)
			{
				return;
			}

			Player player = (Player) event.getWhoClicked();
			PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());

			if (event.getRawSlot() == 3)
			{
				if(player.hasPermission("blivtrails.options.height.feet"))
				{
					pcfg.setHeight(0);
					optionsMenuHeight(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.height.feet"));
				}
			}
			if (event.getRawSlot() == 4)
			{
				if(player.hasPermission("blivtrails.options.height.waist"))
				{
					pcfg.setHeight(1);
					optionsMenuHeight(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.height.waist"));
				}
			}
			if (event.getRawSlot() == 5)
			{
				if(player.hasPermission("blivtrails.options.height.halo"))
				{
					pcfg.setHeight(2);
					optionsMenuHeight(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.height.halo"));
				}
			}
			else if (event.getRawSlot() == cfg.getInt("menu.options.back-button.position"))
			{
				optionsMenu(player);
			}
		}
		else if (BUtil.stripColours(event.getInventory().getTitle()).contains(BUtil.stripColours(msg.getString("messages.titles.colours"))))
		{
			event.setCancelled(true);

			if (event.getRawSlot() >= 18)
			{
				return;
			}

			Player player = (Player) event.getWhoClicked();
			PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());

			if (event.getRawSlot() == cfg.getInt("menu.options.back-button.position"))
			{
				optionsMenu(player);
			}
			else if (event.getCurrentItem().getType().equals(Material.INK_SACK))
			{
				if (pcfg.getParticle() == ParticleEffect.NOTE) // Disable some colours which don't exist for notes
				{
					switch (event.getCurrentItem().getDurability())
					{
						case 0:
						case 3:
						case 7:
						case 8:
						case 15:
							BUtil.printError(player, msg.getString("messages.error.option-trail-no-support"));
							return;
						default:
							break;
					}
				}
				if(player.hasPermission("blivtrails.options.colour." + BUtil.intToColour(event.getCurrentItem().getDurability())))
				{
					pcfg.setColour(event.getCurrentItem().getDurability());
					optionsMenuColour(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.colour.colour"));
				}
			}
			else if (event.getCurrentItem().getType().equals(Material.POTION))
			{
				if(player.hasPermission("blivtrails.options.colour.random"))
				{
					pcfg.setColour(16);
					optionsMenuColour(player); // Set the type, and reload the menu
				}
				else
				{
					BUtil.printPlain(player, msg.getString("messages.no-permission.colour.random"));
				}
			}
			else
			{
				BUtil.printPlain(player, msg.getString("messages.error.no-exist"));
			}
		}
	}

	public static void mainMenu(Player player)
	{
		PlayerConfig pcfg;
		if (TrailManager.getTrailMap().containsKey(player.getUniqueId()))
		{
			pcfg = TrailManager.getTrailMap().get(player.getUniqueId());
		}
		else
		// Use a temporary/blank PlayerConfig
		{
			pcfg = new PlayerConfig(player.getUniqueId(), null, 0, 0, 0, 0);
		}

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.main.size"), msg.getString("messages.titles.main-menu"));
		if (cfg.getBoolean("trails.remove-trail.display"))
		{
			setInventoryItem(inventory, cfg.getInt("trails.remove-trail.position"),
			                 menuItem(cfg.getString("trails.remove-trail.material"), BUtil.translateColours(cfg.getString("trails.remove-trail.name")),
			                          BUtil.translateColours(cfg.getStringList("trails.remove-trail.lore")), player.hasPermission("blivtrails.remove-trail"), false));
		}
		String particleString;
		for (ParticleEffect particleEff : usedTrails)
		{
			particleString = BUtil.trailConfigName(particleEff.toString());
			if(!cfg.getBoolean("trails." + particleString + ".display"))
			{
				continue;
			}

			setInventoryItem(inventory, cfg.getInt("trails." + particleString + ".position"),
			                 menuItem(cfg.getString("trails." + particleString + ".material"), BUtil.translateColours(cfg.getString("trails." + particleString + ".name")),
			                          BUtil.translateColours(cfg.getStringList("trails." + particleString + ".lore")),
			                          player.hasPermission("blivtrails." + particleString), pcfg.getParticle() == particleEff));
		}
		if (cfg.getBoolean("trails.options-menu.display"))
		{
			setInventoryItem(inventory, cfg.getInt("trails.options-menu.position"),
			                 menuItem(cfg.getString("trails.options-menu.material"), BUtil.translateColours(cfg.getString("trails.options-menu.name")),
			                          BUtil.translateColours(cfg.getStringList("trails.options-menu.lore")), player.hasPermission("blivtrails.options-menu"), false));
		}

		ConfigurationSection extraItemSection = cfg.getSave().getConfigurationSection("menu.extras");
		if(extraItemSection != null)
		{
			for(String extra : extraItemSection.getKeys(false))
			{
				if(extraItemSection.getString(extra + ".menu").equals("MAIN"))
				{
					setInventoryItem(inventory, extraItemSection.getInt(extra + ".position"),
					                 GUIUtil.createItem(Material.getMaterial(extraItemSection.getString(extra + ".material")),
					                                    extraItemSection.getInt(extra + ".damage"),
					                                    1,
					                                    extraItemSection.getString("menu.extras." + extra + ".title"),
					                                    null,
					                                    extraItemSection.getStringList("menu.extras." + extra + ".lore")));
				}
			}
		}

		player.openInventory(inventory);
	}

	/*
	 * Options Menus
	 * --------------------------------------------------------------------
	 */

	/**
	 *
	 * @param player
	 * @return False is an error occurred, or if the player does not have an active trail
	 */
	public static boolean optionsMenu(Player player)
	{
		PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());
		if(pcfg == null)
		{
			return false;
		}

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.main-options"));
		if (cfg.getBoolean("menu.options.config.type.enabled"))
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.type.position"), optionsType(player));
		}
		if (cfg.getBoolean("menu.options.config.length.enabled"))
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.length.position"), optionsLength(player));
		}
		if (cfg.getBoolean("menu.options.config.height.enabled"))
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.height.position"), optionsHeight(player));
		}
		if (cfg.getBoolean("menu.options.config.colour.enabled"))
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.position"), optionsColour(player, pcfg.getParticle()));
		}
		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), backButton());

		ConfigurationSection extraItemSection = cfg.getSave().getConfigurationSection("menu.extras");
		if(extraItemSection != null)
		{
			for(String extra : extraItemSection.getKeys(false))
			{
				if(extraItemSection.getString(extra + ".menu").equals("OPTIONS"))
				{
					setInventoryItem(inventory, extraItemSection.getInt(extra + ".position"),
					                 GUIUtil.createItem(Material.getMaterial(extraItemSection.getString(extra + ".material")),
					                                    extraItemSection.getInt(extra + ".damage"),
					                                    1,
					                                    extraItemSection.getString("menu.extras." + extra + ".title"),
					                                    null,
					                                    extraItemSection.getStringList("menu.extras." + extra + ".lore")));
				}
			}
		}

		player.openInventory(inventory);
		return true;
	}

	private static void setInventoryItem(Inventory inventory, int slot, ItemStack itemStack)
	{
		if(inventory == null || itemStack == null)
		{
			return; //We can safely ignore this entirely
		}

		if(slot < 0 || slot >= inventory.getSize())
		{
			BUtil.logError("Attempted to place " + itemStack.getType() + " - " + itemStack.getItemMeta().getDisplayName() +
				               " outside of inventory " + inventory.getType() + ".");
			return;
		}

		inventory.setItem(slot, itemStack);
	}

	/*
	 * Sub Options Menus
	 */

	public static void optionsMenuType(Player player)
	{
		PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.type"));
		if (cfg.getBoolean("menu.options.config.type.trace"))
		{
			setInventoryItem(inventory, 3, optionsTypeTrace(player, pcfg.getType() == 1));
			setInventoryItem(inventory, 12, informationItem(msg.getStringList("messages.information.type.trace")));
		}
		if (cfg.getBoolean("menu.options.config.type.random"))
		{
			setInventoryItem(inventory, 4, optionsTypeRandom(player, pcfg.getType() == 2));
			setInventoryItem(inventory, 13, informationItem(msg.getStringList("messages.information.type.random")));
		}
		if (cfg.getBoolean("menu.options.config.type.dynamic"))
		{
			setInventoryItem(inventory, 5, optionsTypeDynamic(player, pcfg.getType() == 3, pcfg.getParticle()));
			setInventoryItem(inventory, 14, informationItem(msg.getStringList("messages.information.type.dynamic")));
		}
		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), backButton());

		ConfigurationSection extraItemSection = cfg.getSave().getConfigurationSection("menu.extras");
		if(extraItemSection != null)
		{
			for(String extra : extraItemSection.getKeys(false))
			{
				if(extraItemSection.getString(extra + ".menu").equals("TYPE"))
				{
					setInventoryItem(inventory, extraItemSection.getInt(extra + ".position"),
					                 GUIUtil.createItem(Material.getMaterial(extraItemSection.getString(extra + ".material")),
					                                    extraItemSection.getInt(extra + ".damage"),
					                                    1,
					                                    extraItemSection.getString("menu.extras." + extra + ".title"),
					                                    null,
					                                    extraItemSection.getStringList("menu.extras." + extra + ".lore")));
				}
			}
		}

		player.openInventory(inventory);

	}

	public static void optionsMenuLength(Player player)
	{
		PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.length"));
		if (cfg.getBoolean("menu.options.config.length.short"))
		{
			setInventoryItem(inventory, 3, optionsLengthShort(player, pcfg.getLength() == 1));
		}
		if (cfg.getBoolean("menu.options.config.length.medium"))
		{
			setInventoryItem(inventory, 4, optionsLengthMedium(player, pcfg.getLength() == 2));
		}
		if (cfg.getBoolean("menu.options.config.length.long"))
		{
			setInventoryItem(inventory, 5, optionsLengthLong(player, pcfg.getLength() == 3));
		}
		setInventoryItem(inventory, 13, informationItem(msg.getStringList("messages.information.length.info")));
		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), backButton());

		ConfigurationSection extraItemSection = cfg.getSave().getConfigurationSection("menu.extras");
		if(extraItemSection != null)
		{
			for(String extra : extraItemSection.getKeys(false))
			{
				if(extraItemSection.getString(extra + ".menu").equals("LENGTH"))
				{
					setInventoryItem(inventory, extraItemSection.getInt(extra + ".position"),
					                 GUIUtil.createItem(Material.getMaterial(extraItemSection.getString(extra + ".material")),
					                                    extraItemSection.getInt(extra + ".damage"),
					                                    1,
					                                    extraItemSection.getString("menu.extras." + extra + ".title"),
					                                    null,
					                                    extraItemSection.getStringList("menu.extras." + extra + ".lore")));
				}
			}
		}

		player.openInventory(inventory);
	}

	public static void optionsMenuHeight(Player player)
	{
		PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.height"));
		if (cfg.getBoolean("menu.options.config.height.feet"))
		{
			setInventoryItem(inventory, 3, optionsHeightFeet(player, pcfg.getHeight() == 0));
		}
		if (cfg.getBoolean("menu.options.config.height.waist"))
		{
			setInventoryItem(inventory, 4, optionsHeightWaist(player, pcfg.getHeight() == 1));
		}
		if (cfg.getBoolean("menu.options.config.height.halo"))
		{
			setInventoryItem(inventory, 5, optionsHeightHead(player, pcfg.getHeight() == 2));
		}
		setInventoryItem(inventory, 13, informationItem(msg.getStringList("messages.information.height.info")));
		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), backButton());

		ConfigurationSection extraItemSection = cfg.getSave().getConfigurationSection("menu.extras");
		if(extraItemSection != null)
		{
			for(String extra : extraItemSection.getKeys(false))
			{
				if(extraItemSection.getString(extra + ".menu").equals("HEIGHT"))
				{
					setInventoryItem(inventory, extraItemSection.getInt(extra + ".position"),
					                 GUIUtil.createItem(Material.getMaterial(extraItemSection.getString(extra + ".material")),
					                                    extraItemSection.getInt(extra + ".damage"),
					                                    1,
					                                    extraItemSection.getString("menu.extras." + extra + ".title"),
					                                    null,
					                                    extraItemSection.getStringList("menu.extras." + extra + ".lore")));
				}
			}
		}

		player.openInventory(inventory);
	}

	public static void optionsMenuColour(Player player)
	{
		PlayerConfig pcfg = TrailManager.getTrailMap().get(player.getUniqueId());
		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.config.colour.size"), msg.getString("messages.titles.colours"));
		if (cfg.getInt("menu.options.config.colour.black-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.black-pos"), optionsColourItem(player, pcfg.getColour() == 0, 0, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.red-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.red-pos"), optionsColourItem(player, pcfg.getColour() == 1, 1, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.green-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.green-pos"), optionsColourItem(player, pcfg.getColour() == 2, 2, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.brown-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.brown-pos"), optionsColourItem(player, pcfg.getColour() == 3, 3, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.blue-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.blue-pos"), optionsColourItem(player, pcfg.getColour() == 4, 4, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.purple-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.purple-pos"), optionsColourItem(player, pcfg.getColour() == 5, 5, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.cyan-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.cyan-pos"), optionsColourItem(player, pcfg.getColour() == 6, 6, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.light-grey-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.light-grey-pos"), optionsColourItem(player, pcfg.getColour() == 7, 7, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.grey-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.grey-pos"), optionsColourItem(player, pcfg.getColour() == 8, 8, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.pink-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.pink-pos"), optionsColourItem(player, pcfg.getColour() == 9, 9, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.lime-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.pink-pos"), optionsColourItem(player, pcfg.getColour() == 10, 10, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.lime-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.yellow-pos"), optionsColourItem(player, pcfg.getColour() == 11, 11, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.light-blue-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.light-blue-pos"), optionsColourItem(player, pcfg.getColour() == 12, 12, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.magenta-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.magenta-pos"), optionsColourItem(player, pcfg.getColour() == 13, 13, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.orange-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.orange-pos"), optionsColourItem(player, pcfg.getColour() == 14, 14, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.white-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.white-pos"), optionsColourItem(player, pcfg.getColour() == 15, 15, pcfg.getParticle()));
		}
		if (cfg.getInt("menu.options.config.colour.random-pos") != -1)
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.random-pos"), optionsColourItem(player, pcfg.getColour() == 16, 16, pcfg.getParticle()));
		}
		setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.back-button-pos"), backButton());

		ConfigurationSection extraItemSection = cfg.getSave().getConfigurationSection("menu.extras");
		if(extraItemSection != null)
		{
			for(String extra : extraItemSection.getKeys(false))
			{
				if(extraItemSection.getString(extra + ".menu").equals("COLOUR"))
				{
					setInventoryItem(inventory, extraItemSection.getInt(extra + ".position"),
					                 GUIUtil.createItem(Material.getMaterial(extraItemSection.getString(extra + ".material")),
					                                    extraItemSection.getInt(extra + ".damage"),
					                                    1,
					                                    extraItemSection.getString("menu.extras." + extra + ".title"),
					                                    null,
					                                    extraItemSection.getStringList("menu.extras." + extra + ".lore")));
				}
			}
		}

		player.openInventory(inventory);
	}

	/*
	 * Options Type
	 */

	public static ItemStack optionsType(Player player)
	{
		ItemStack item = new ItemStack(Material.GLASS_BOTTLE, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.categories.type"));
		if(!player.hasPermission("blivtrails.options.type"))
		{
			meta.setLore(Arrays.asList(msg.getString("messages.indicators.dont-have-permission")));
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsTypeTrace(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.type.trace"));

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.type.trace"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
			item.setDurability((short) 10);
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsTypeRandom(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.type.random"));
		ArrayList<String> lore = new ArrayList<>();

		if(!player.hasPermission("blivtrails.options.type.random"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));

			item.setDurability((short) 10);
		}

		lore.add(msg.getString("messages.options.supports-randomisation"));
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsTypeDynamic(Player player, boolean isEnabled, ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.type.dynamic"));
		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.type.dynamic"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));

			item.setDurability((short) 10);
		}

		if (particle.hasProperty(ParticleEffect.ParticleProperty.DIRECTIONAL))
		{
			lore.add(msg.getString("messages.options.supports-dynamic"));
		}
		else
		{
			lore.add(msg.getString("messages.options.doesnt-support-dynamic"));
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	/*
	 * Options Length
	 */

	public static ItemStack optionsLength(Player player)
	{
		ItemStack item = new ItemStack(Material.ARROW, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.categories.length"));
		if(!player.hasPermission("blivtrails.options.length"))
		{
			meta.setLore(Collections.singletonList(BUtil.translateColours("messages.indicators.dont-have-permission")));
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsLengthShort(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.length.short"));

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.length.short"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
			item.setDurability((short) 10);
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsLengthMedium(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.length.medium"));

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.length.medium"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
			item.setDurability((short) 10);
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsLengthLong(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.length.long"));

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.length.long"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
			item.setDurability((short) 10);
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	/*
	 * Options Height
	 */

	public static ItemStack optionsHeight(Player player)
	{
		ItemStack item = new ItemStack(Material.FENCE, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.categories.height"));
		if(!player.hasPermission("blivtrails.options.height"))
		{
			meta.setLore(Arrays.asList(BUtil.translateColours("messages.indicators.dont-have-permission")));
		}
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsHeightFeet(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.height.feet"));

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.height.feet"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
			item.setDurability((short) 10);
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsHeightWaist(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.height.waist"));

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.height.waist"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
			item.setDurability((short) 10);
		}

		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsHeightHead(Player player, boolean isEnabled)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 8);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.height.halo"));

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.height.halo"))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
			meta.setLore(lore);
			item.setDurability((short) 10);
		}
		item.setItemMeta(meta);
		return item;
	}

	/*
	 * Options Colour
	 */

	public static ItemStack optionsColour(Player player, ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) 14);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.categories.colour"));
		ArrayList<String> lore = new ArrayList<>();
		if (particle.hasProperty(ParticleEffect.ParticleProperty.COLORABLE))
		{
			lore.add(msg.getString("messages.options.supports-colours"));
		}
		else
		{
			lore.add(msg.getString("messages.options.doesnt-support-colours"));
		}
		if(!player.hasPermission("blivtrails.options.colour"))
		{
			lore.add(BUtil.translateColours("messages.indicators.dont-have-permission"));
		}
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack optionsColourItem(Player player, boolean isEnabled, int value, ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) value);
		if (value == 16)
		{
			item = new ItemStack(Material.POTION, 1);
		}
		ItemMeta meta = item.getItemMeta();
		String colour;
		switch (value)
		{
			case 0:
				colour = ChatColor.DARK_GRAY + "Black";
				break;
			case 1:
				colour = ChatColor.RED + "Red";
				break;
			case 2:
				colour = ChatColor.DARK_GREEN + "Green";
				break;
			case 3:
				colour = ChatColor.GOLD + "Brown";
				break;
			case 4:
				colour = ChatColor.BLUE + "Blue";
				break;
			case 5:
				colour = ChatColor.DARK_PURPLE + "Purple";
				break;
			case 6:
				colour = ChatColor.DARK_AQUA + "Cyan";
				break;
			case 7:
				colour = ChatColor.GRAY + "Light Grey";
				break;
			case 8:
				colour = ChatColor.DARK_GRAY + "Gray";
				break;
			case 9:
				colour = ChatColor.LIGHT_PURPLE + "Pink";
				break;
			case 10:
				colour = ChatColor.GREEN + "Lime";
				break;
			case 11:
				colour = ChatColor.YELLOW + "Yellow";
				break;
			case 12:
				colour = ChatColor.AQUA + "Light Blue";
				break;
			case 13:
				colour = ChatColor.LIGHT_PURPLE + "Magenta";
				break;
			case 14:
				colour = ChatColor.GOLD + "Orange";
				break;
			case 16:
				colour = "Random";
				break;
			default:
				colour = "White";
				break;
		}
		meta.setDisplayName(colour);

		ArrayList<String> lore = new ArrayList<>();

		if(!player.hasPermission("blivtrails.options.colour." + ChatColor.stripColor(colour.toLowerCase().replaceAll("[ ]", ""))))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}

		if (isEnabled)
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
		}
		if (particle == ParticleEffect.NOTE) // Disable some colours which don't exist for notes
		{
			String isDisabled = null;
			switch (value)
			{
				case 0:
				case 3:
				case 7:
				case 8:
				case 15:
					isDisabled = msg.getString("messages.options.doesnt-apply-to-note");
					break;
				default:
					break;
			}
			if (isDisabled != null)
			{
				lore.add(isDisabled);
			}
		}
		meta.setLore(lore);

		item.setItemMeta(meta);
		return item;
	}

	/*
	 * Other
	 */

	public static ItemStack backButton()
	{
		ItemStack item = getVersionSafeItemStack(cfg.getString("menu.options.back-button.material"));
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(msg.getString("messages.options.titles.back"));
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack informationItem(List<String> list)
	{
		return GUIUtil.createItem(Material.BOOK, 0, 1, msg.getString("messages.options.titles.information"), null, list);
	}

	public static ItemStack menuItem(String material, String name, List<String> lore, boolean hasPermission, boolean isSelected)
	{
		ItemStack item = getVersionSafeItemStack(material);
		if(item == INVALID_ITEM) //Don't process the stack if it's invalid.
		{
			return item;
		}

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		if (hasPermission)
		{
			if (meta.getLore() == null)
			{
				meta.setLore(Collections.singletonList(msg.getString("messages.indicators.have-permission")));
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
			if (meta.getLore() == null)
			{
				meta.setLore(Collections.singletonList(msg.getString("messages.indicators.dont-have-permission")));
			}
			else
			{
				List<String> lorePerm = meta.getLore();
				lorePerm.add(msg.getString("messages.indicators.dont-have-permission"));
				meta.setLore(lorePerm);
			}
		}
		if (isSelected)
		{
			if (meta.getLore() == null)
			{
				meta.setLore(Collections.singletonList(msg.getString("messages.indicators.trail-selected")));
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

	private static final ItemStack INVALID_ITEM = GUIUtil.createItem(Material.POTATO_ITEM, 0, 32, "\u00A7cVersion does not support this Material!", null, null);

	private static ItemStack getVersionSafeItemStack(String materialName)
	{
		Material material = Material.getMaterial(materialName);
		if(material == null)
		{
			BUtil.logError("Attempted to use material: '" + materialName + "', which is INVALID at your current version: " + Bukkit.getBukkitVersion() + " reverting to POTATO_ITEM");
			return INVALID_ITEM;
		}
		return new ItemStack(material, 1);
	}

}
