package net.auscraft.BlivTrails.listeners;

import com.darkblade12.ParticleEffect.ParticleEffect;
import net.auscraft.BlivTrails.OptionType;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.util.BUtil;
import net.auscraft.BlivTrails.util.GUIUtil;
import org.bukkit.Bukkit;
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

	//Cache regularly used ItemStacks instead of using a method-call every time
	private static ItemStack BACK_BUTTON = null;

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

		BACK_BUTTON = GUIUtil.createItem(getVersionSafeMaterial(cfg.getString("menu.options.back-button.material")),
		                                 0,
		                                 1,
		                                 msg.getString("messages.options.titles.back"),
		                                 null,
		                                 cfg.getStringList("menu.options.back-button.lore"));
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
			PlayerConfig playerConfig = TrailManager.getPlayerConfig(player.getUniqueId());

			if (event.getRawSlot() == cfg.getInt("trails.remove-trail.position")) //Remove Trail
			{
				if (playerConfig != null)
				{
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
							if(playerConfig != null && playerConfig.isScheduled())
							{
								Bukkit.getScheduler().cancelTask(playerConfig.getTaskId());
								playerConfig.resetTaskId();

								playerConfig.setParticle(ParticleEffect.FOOTSTEP);
							}

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
			PlayerConfig playerConfig = TrailManager.getPlayerConfig(player.getUniqueId());

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
			PlayerConfig pcfg = TrailManager.getPlayerConfig(player.getUniqueId());

			//Here come the hardcoded options positions
			if (event.getRawSlot() == 3)
			{
				if(player.hasPermission("blivtrails.options.type.trace"))
				{
					pcfg.setType(OptionType.TYPE_TRACE);
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
					pcfg.setType(OptionType.TYPE_RANDOM);
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
					pcfg.setType(OptionType.TYPE_DYNAMIC);
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
			PlayerConfig pcfg = TrailManager.getPlayerConfig(player.getUniqueId());

			if (event.getRawSlot() == 3)
			{
				if(player.hasPermission("blivtrails.options.length.short"))
				{
					pcfg.setLength(OptionType.LENGTH_SHORT);
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
					pcfg.setLength(OptionType.LENGTH_MEDIUM);
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
					pcfg.setLength(OptionType.LENGTH_LONG);
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
			PlayerConfig pcfg = TrailManager.getPlayerConfig(player.getUniqueId());

			if (event.getRawSlot() == 3)
			{
				if(player.hasPermission("blivtrails.options.height.feet"))
				{
					pcfg.setHeight(OptionType.HEIGHT_FEET);
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
					pcfg.setHeight(OptionType.HEIGHT_WAIST);
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
					pcfg.setHeight(OptionType.HEIGHT_HALO);
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
			PlayerConfig pcfg = TrailManager.getPlayerConfig(player.getUniqueId());

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
		if ((pcfg = TrailManager.getPlayerConfig(player.getUniqueId())) == null)
		{
			// Use a temporary/blank PlayerConfig
			pcfg = new PlayerConfig(player.getUniqueId());
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
			                          BUtil.translateColours(cfg.getStringList("trails.options-menu.lore")), player.hasPermission("blivtrails.options"), false));
		}

		addCustomInventoryItems(inventory, "MAIN");

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
		PlayerConfig pcfg;
		if((pcfg = TrailManager.getPlayerConfig(player.getUniqueId())) == null)
		{
			return false;
		}

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.main-options"));

		addMenuOptionItemIfEnabled(inventory, cfg.getInt("menu.options.config." + OptionType.TYPE.getConfigName() + ".position"),
		                           player, OptionType.TYPE, Material.GLASS_BOTTLE);
		addMenuOptionItemIfEnabled(inventory, cfg.getInt("menu.options.config." + OptionType.LENGTH.getConfigName() + ".position"),
		                           player, OptionType.LENGTH, Material.ARROW);
		addMenuOptionItemIfEnabled(inventory, cfg.getInt("menu.options.config." + OptionType.HEIGHT.getConfigName() + ".position"),
		                           player, OptionType.HEIGHT, Material.FENCE);

		//Colour is done differently.
		if (cfg.getBoolean("menu.options.config.colour.enabled"))
		{
			setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.position"), optionsColour(player, pcfg.getParticle()));
		}
		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), BACK_BUTTON);

		addCustomInventoryItems(inventory, "OPTIONS");

		player.openInventory(inventory);
		return true;
	}

	private static void addCustomInventoryItems(Inventory inventory, String menuId)
	{
		ConfigurationSection extraItemSection = cfg.getSave().getConfigurationSection("menu.extras");
		if(extraItemSection != null)
		{
			for(String extra : extraItemSection.getKeys(false))
			{
				if(extraItemSection.getString(extra + ".menu").equals(menuId))
				{
					setInventoryItem(inventory, extraItemSection.getInt(extra + ".position"),
					                 GUIUtil.createItem(getVersionSafeMaterial(extraItemSection.getString(extra + ".material")),
					                                    extraItemSection.getInt(extra + ".damage"),
					                                    1,
					                                    extraItemSection.getString(extra + ".title"),
					                                    null,
					                                    extraItemSection.getStringList(extra + ".lore")));
				}
			}
		}
	}

	/**
	 *
	 * @param inventory
	 * @param slot
	 * @param player
	 * @param optionType
	 * @param material
	 * @return True if the item was added
	 */
	private static boolean addMenuOptionItemIfEnabled(Inventory inventory, int slot, Player player, OptionType optionType, Material material)
	{
		if (cfg.getBoolean("menu.options.config." + optionType.getConfigName() + ".enabled"))
		{
			return setInventoryItem(inventory, slot, optionMenuItem(player, material, optionType));
		}

		return false;
	}

	/**
	 *
	 * @param inventory
	 * @param slot
	 * @param player
	 * @param optionType
	 * @param playerConfig
	 * @return True if Item was added
	 */
	private static boolean addOptionItemIfEnabled(Inventory inventory, int slot, Player player, OptionType optionType, PlayerConfig playerConfig)
	{
		if (cfg.getBoolean("menu.options.config." + optionType.getConfigName()))
		{
			return setInventoryItem(inventory, slot, optionItem(player, optionType, optionType.isOptionActive(playerConfig.getEnabledOption(optionType)),
				                                             playerConfig.getParticle()));
		}

		return false;
	}

	/*
	 * Sub Options Menus
	 */

	public static void optionsMenuType(Player player)
	{
		PlayerConfig playerConfig = TrailManager.getPlayerConfig(player.getUniqueId());

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.type"));

		addOptionItemIfEnabled(inventory, 3, player, OptionType.TYPE_TRACE, playerConfig);
		addOptionItemIfEnabled(inventory, 4, player, OptionType.TYPE_RANDOM, playerConfig);
		addOptionItemIfEnabled(inventory, 5, player, OptionType.TYPE_DYNAMIC, playerConfig);

		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), BACK_BUTTON);

		addCustomInventoryItems(inventory, "TYPE");

		player.openInventory(inventory);
	}

	public static void optionsMenuLength(Player player)
	{
		PlayerConfig playerConfig = TrailManager.getPlayerConfig(player.getUniqueId());

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.length"));

		addOptionItemIfEnabled(inventory, 3, player, OptionType.LENGTH_SHORT, playerConfig);
		addOptionItemIfEnabled(inventory, 4, player, OptionType.LENGTH_MEDIUM, playerConfig);
		addOptionItemIfEnabled(inventory, 5, player, OptionType.LENGTH_LONG, playerConfig);

		setInventoryItem(inventory, 13, informationItem(msg.getStringList("messages.information.length.info")));
		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), BACK_BUTTON);

		addCustomInventoryItems(inventory, "LENGTH");

		player.openInventory(inventory);
	}

	public static void optionsMenuHeight(Player player)
	{
		PlayerConfig playerConfig = TrailManager.getPlayerConfig(player.getUniqueId());

		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.size"), msg.getString("messages.titles.height"));

		addOptionItemIfEnabled(inventory, 3, player, OptionType.HEIGHT_FEET, playerConfig);
		addOptionItemIfEnabled(inventory, 4, player, OptionType.HEIGHT_WAIST, playerConfig);
		addOptionItemIfEnabled(inventory, 5, player, OptionType.HEIGHT_HALO, playerConfig);

		setInventoryItem(inventory, 13, informationItem(msg.getStringList("messages.information.height.info")));
		setInventoryItem(inventory, cfg.getInt("menu.options.back-button.position"), BACK_BUTTON);

		addCustomInventoryItems(inventory, "HEIGHT");

		player.openInventory(inventory);
	}

	private static final String[] colourCfgName = new String[]
	{
		"black", "red", "green", "brown", "blue", "purple", "cyan", "light-grey", "grey", "pink", "lime",
	    "yellow", "light-blue", "magenta", "orange", "white", "random"
	};

	public static void optionsMenuColour(Player player)
	{
		PlayerConfig playerConfig = TrailManager.getPlayerConfig(player.getUniqueId());
		Inventory inventory = Bukkit.createInventory(null, cfg.getInt("menu.options.config.colour.size"), msg.getString("messages.titles.colours"));

		ConfigurationSection colourPositionSection = cfg.getConfigurationSection("menu.options.config.colour");
		if(colourPositionSection == null)
		{
			BUtil.logError("The colour position configuration section has been removed at 'menu.options.config.colour', but it is still enabled." +
				               " Are you sure you know what you're doing?");
			return;
		}

		for(int colourId = 0;colourId < colourCfgName.length;colourId++)
		{
			if (cfg.getInt("menu.options.config.colour." + colourCfgName[colourId]) != -1)
			{
				setInventoryItem(inventory, cfg.getInt("menu.options.config.colour." + colourCfgName[colourId]),
				                 optionsColourItem(player, playerConfig.getColour() == colourId, colourId, playerConfig.getParticle()));
			}
		}
		setInventoryItem(inventory, cfg.getInt("menu.options.config.colour.back-button-pos"), BACK_BUTTON);

		addCustomInventoryItems(inventory, "COLOUR");

		player.openInventory(inventory);
	}

	/*
	 * Item Creation Helper Methods
	 */

	/**
	 *
	 * @param inventory
	 * @param slot
	 * @param itemStack
	 * @return True if the ItemStack was added to the inventory
	 */
	private static boolean setInventoryItem(Inventory inventory, int slot, ItemStack itemStack)
	{
		if(inventory == null || itemStack == null)
		{
			return false; //We can safely ignore this entirely
		}

		if(slot < 0 || slot >= inventory.getSize())
		{
			//I've stated that a position of -1 can disable items
			if(slot != -1)
			{
				BUtil.logError("Attempted to place " + itemStack.getType() + " - " + itemStack.getItemMeta().getDisplayName() +
					               " outside of inventory " + inventory.getType() + ".");
			}
			return false;
		}

		inventory.setItem(slot, itemStack);

		return true;
	}

	public static ItemStack optionMenuItem(Player player, Material material, OptionType optionType)
	{
		ItemStack itemStack = GUIUtil.createItem(material, 0, 1, msg.getString("messages.options.titles.categories." + optionType.getConfigName()),
		                                         null, null);

		if(!player.hasPermission("blivtrails.options." + optionType.getConfigName()))
		{
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setLore(Collections.singletonList(BUtil.translateColours(cfg.getString("messages.indicators.dont-have-permission"))));
			itemStack.setItemMeta(itemMeta);
		}

		return itemStack;
	}

	public static ItemStack optionItem(Player player, OptionType optionType, boolean isEnabled, ParticleEffect particleEffect)
	{
		ItemStack itemStack = GUIUtil.createItem(Material.INK_SACK, 8, 1,
		                                         msg.getString("messages.options.titles." + optionType.getConfigName()),
												 null, null);
		if (isEnabled)
		{
			itemStack.setDurability((short) 10);

			ItemMeta itemMeta = itemStack.getItemMeta();

			List<String> lore = new ArrayList<>();
			if(!player.hasPermission("blivtrails.options." + optionType.getConfigName()))
			{
				lore.add(msg.getString("messages.indicators.dont-have-permission"));
			}
			else
			{
				lore.add(msg.getString("messages.generic.enabled-lore"));
			}

			if(optionType == OptionType.TYPE_DYNAMIC)
			{
				if (particleEffect.hasProperty(ParticleEffect.ParticleProperty.DIRECTIONAL))
				{
					lore.add(msg.getString("messages.options.supports-dynamic"));
				}
				else
				{
					lore.add(msg.getString("messages.options.doesnt-support-dynamic"));
				}
			}

			itemMeta.setLore(lore);
			itemStack.setItemMeta(itemMeta);
		}

		return itemStack;
	}

	/*
	 * Options Colour
	 */

	public static ItemStack optionsColour(Player player, ParticleEffect particle)
	{
		ItemStack itemStack = GUIUtil.createItem(Material.INK_SACK, 14, 1,
		                                    msg.getString("messages.options.titles.categories.colour"),
		                                    null, null);
		ItemMeta itemMeta = itemStack.getItemMeta();

		ArrayList<String> lore = new ArrayList<>();
		if(!player.hasPermission("blivtrails.options.colour"))
		{
			lore.add(BUtil.translateColours("messages.indicators.dont-have-permission"));
		}
		else
		{
			if (particle.hasProperty(ParticleEffect.ParticleProperty.COLORABLE))
			{
				lore.add(msg.getString("messages.options.supports-colours"));
			}
			else
			{
				lore.add(msg.getString("messages.options.doesnt-support-colours"));
			}
		}

		itemMeta.setLore(lore);
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	private static final String[] colourNames = new String[]
	{
		"§0Black", //0
		"§cRed", //1
		"§2Green", //2
		"§6Brown", //3
		"§9Blue", //4
		"§5Purple", //5
		"§3Cyan", //6
		"§7Light Grey", //7
		"§8Grey", //8
		"§dPink", //9
		"§Lime", //10
		"§eYellow", //11
		"§bLight Blue", //12
		"§dMagenta", //13
		"§6Orange", //14
		"§fWhite", //15
		"§6Random" //16
	};

	public static ItemStack optionsColourItem(Player player, boolean isEnabled, int colourId, ParticleEffect particle)
	{
		ItemStack item = new ItemStack(Material.INK_SACK, 1, (short) colourId);
		if (colourId == (colourNames.length - 1)) //Random
		{
			item = new ItemStack(Material.POTION, 1);
		}

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(colourNames[colourId]);

		ArrayList<String> lore = new ArrayList<>();

		if(!player.hasPermission("blivtrails.options.colour." + colourCfgName[colourId]))
		{
			lore.add(msg.getString("messages.indicators.dont-have-permission"));
		}
		else if (isEnabled) //Cannot be enabled with no permission.
		{
			lore.add(msg.getString("messages.generic.enabled-lore"));
		}

		if (particle == ParticleEffect.NOTE) // Disable some colours which don't exist for notes
		{
			String isDisabled = null;
			switch (colourId)
			{
				case 0: case 3: case 7: case 8: case 15:
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

	public static Material getVersionSafeMaterial(String materialName)
	{
		Material material = Material.getMaterial(materialName);
		if(material == null)
		{
			BUtil.logError("Attempted to use material: '" + materialName + "', which is INVALID at your current version: " + Bukkit.getBukkitVersion() + " reverting to POTATO_ITEM");
			return Material.POTATO_ITEM;
		}
		return material;
	}


}
