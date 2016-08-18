package me.ohblihv.BlivTrails.util.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.ohblihv.BlivTrails.BlivTrails;
import me.ohblihv.BlivTrails.util.BUtil;
import me.ohblihv.BlivTrails.util.GUIUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by OhBlihv (Chris) on 22/11/2015.
 */
@AllArgsConstructor
@RequiredArgsConstructor
public class GUIContainer implements Listener
{
	
	@RequiredArgsConstructor
	public static class GUISound
	{
		
		private final Sound sound;
		private final float volume,
							pitch;
		
		public void playSound(Player player)
		{
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
		
	}

	@AllArgsConstructor
	public static class GUIElement
	{
		
		public static final GUIElement DEFAULT_GUI_ELEMENT =
			new GUIElement(new GUIUtil.ItemContainer(Material.POTATO_ITEM, 0, 1, null, null, GUIUtil.ItemContainer.EnchantStatus.NO_CHANGE, null, null),
			               new ArrayDeque<>());

		@Getter
		protected final GUIUtil.ItemContainer itemContainer;

		@Getter
		//Use a queue here as actions are meant to be executed in the order they
		//were added in, and if one fails, the next one should not execute
		protected final Deque<ElementActions.ElementAction> elementActions;

		public ItemStack toItemStack(Deque<GUIVariables.GUIVariable> guiVariables, Player player)
		{
			ItemStack itemStack = itemContainer.toItemStack(player == null ? "" : player.getName());
			if(guiVariables != null && !guiVariables.isEmpty())
			{
				ItemMeta itemMeta = itemStack.getItemMeta();

				if(itemMeta.hasDisplayName() && !itemMeta.getDisplayName().isEmpty())
				{
					String displayName = itemMeta.getDisplayName();
					for(GUIVariables.GUIVariable guiVariable : guiVariables)
					{
						if(!guiVariable.containsVariable(displayName))
						{
							continue;
						}

						displayName = guiVariable.doReplacement(displayName, player);
					}
					itemMeta.setDisplayName(displayName);
				}

				if(itemMeta.hasLore() && !itemMeta.getLore().isEmpty())
				{
					List<String> lore = itemMeta.getLore();
					for(GUIVariables.GUIVariable guiVariable : guiVariables)
					{
						if(!guiVariable.containsVariable(lore))
						{
							continue;
						}

						lore = guiVariable.doReplacement(lore, player);
					}
					itemMeta.setLore(lore);
				}
				itemStack.setItemMeta(itemMeta);
			}
			return itemStack;
		}

	}

	@NonNull
	protected Pattern guiTitlePattern;

	@Getter
	@NonNull
	protected String guiTitle;

	@Getter
	@NonNull
	protected int guiSize;
	
	@Getter
	protected GUISound openSound;

	@Getter
	@Setter
	protected ItemStack fillerItem;

	@Getter
	protected GUIElement[] guiElements;

	@Getter
	protected Deque<GUIVariables.GUIVariable> guiVariables = null;

	public GUIContainer(ConfigurationSection configurationSection)
	{
		loadGUI(configurationSection);

		Bukkit.getServer().getPluginManager().registerEvents(this, BlivTrails.getInstance());
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event)
	{
		if(event.getInventory() != null && event.getInventory().getTitle() != null &&
				   compareGUITitle(event.getInventory().getTitle()) &&
			event.getRawSlot() != -999 && event.getRawSlot() < guiSize)
		{
			event.setCancelled(true);
			doActions((Player) event.getWhoClicked(), event.getClick(), event.getSlot());
		}
	}

	public boolean compareGUITitle(String inventoryTitle)
	{
		return guiTitlePattern.matcher(inventoryTitle).find();
	}

	public void openInventory(Player player)
	{
		Inventory inventory = Bukkit.createInventory(null, guiSize, guiTitle);
		inventory = getInventory(inventory, player);
		player.openInventory(inventory);
		
		if(openSound != null)
		{
			openSound.playSound(player);
		}
	}

	public boolean updateInventory(Player player)
	{
		Inventory inventory = player.getOpenInventory().getTopInventory();
		if(inventory == null || inventory.getSize() != guiSize)
		{
			return false;
		}
		inventory.clear();

		getInventory(inventory, player);
		player.updateInventory();
		return true;
	}

	Inventory getInventory(Inventory inventory, Player player)
	{
		if(fillerItem != null)
		{
			//Don't use guiSize here to preserve compatibility with updateInventory and missing guiSizes in the config
			for(int slot = 0;slot < inventory.getSize();slot++)
			{
				inventory.setItem(slot, fillerItem);
			}
		}

		if(guiElements != null)
		{
			for(int slot = 0; slot < guiElements.length;slot++)
			{
				if(guiElements[slot] == null)
				{
					continue;
				}

				inventory.setItem(slot, guiElements[slot].toItemStack(guiVariables, player));
			}
		}

		return inventory;
	}

	protected List<String> replaceVariables(List<String> original, Player player)
	{
		for(int lineNum = 0;lineNum < original.size();lineNum++)
		{
			List<String> line = new ArrayList<>();
			line.add(original.get(lineNum));
			for(GUIVariables.GUIVariable variable : guiVariables)
			{
				line = variable.doReplacement(line, player);
			}

			if(line.size() > 1)
			{
				for(int replacementLineNum = 0;replacementLineNum < line.size();replacementLineNum++)
				{
					int replaceLineNum = replacementLineNum + lineNum;
					if(replaceLineNum >= original.size())
					{
						original.add(line.get(replacementLineNum));
					}
					else
					{
						original.set(lineNum + replacementLineNum, line.get(replacementLineNum));
					}
				}
			}
			else
			{
				original.set(lineNum, line.get(0));
			}
		}
		return BUtil.translateColours(original);
	}

	public void doActions(Player player, ClickType clickAction, int slot)
	{
		if( slot < 0 || slot > guiSize || guiElements[slot] == null || guiElements[slot].getElementActions() == null)
		{
			return;
		}

		for(ElementActions.ElementAction elementAction : guiElements[slot].getElementActions())
		{
			//Only process actions tied to this click type
			if(elementAction.hasClickType() && elementAction.clickType != clickAction)
			{
				continue;
			}
			
			//Only do actions while the previous one succeeded
			if(!elementAction.onClick(player, slot))
			{
				break;
			}
		}
	}

	/*
	 * -------------------------------------------------------
	 *                  Loading Methods
	 * -------------------------------------------------------
	 */

	public void loadGUI(ConfigurationSection configurationSection)
	{
		if(configurationSection == null || configurationSection.getKeys(false).isEmpty())
		{
			BUtil.logError("Error loading GUI named: " + (configurationSection == null ? "NULL" : configurationSection.getName()));
			effectiveConstructor("INVALID CONFIG", 54, null, GUIUtil.DEFAULT_ITEMSTACK, null, null);
			return;
		}

		this.guiTitle = BUtil.translateColours(configurationSection.getString("title", "Inventory"));
		int guiSize = configurationSection.getInt("size", 54);
		
		GUISound openSound = null;
		if(configurationSection.contains("open-sound") && configurationSection.isConfigurationSection("open-sound"))
		{
			ConfigurationSection soundSection = configurationSection.getConfigurationSection("open-sound");
			Sound sound = null;
			try
			{
				sound = Sound.valueOf(soundSection.getString("sound"));
			}
			catch(IllegalArgumentException | NullPointerException e)
			{
				//Sound stays null and is caught by the next check
			}
			
			if(sound == null)
			{
				BUtil.logInfo("Could not load sound '" + (soundSection.contains("sound") ? soundSection.getString("sound", "none") : "null") + "'");
			}
			else
			{
				openSound = new GUISound(sound, (float) soundSection.getDouble("volume", 10F), (float) soundSection.getDouble("pitch", 1F));
			}
		}
		
		ItemStack fillerItem = null;
		if(configurationSection.contains("filler"))
		{
			fillerItem = GUIUtil.loadItem(configurationSection.getConfigurationSection("filler"));
		}

		GUIElement[] guiElements = loadGUIElements(configurationSection, new GUIElement[guiSize]);

		ArrayDeque<GUIVariables.GUIVariable> guiVariables = new ArrayDeque<>();

		if(configurationSection.contains("variables") && configurationSection.isConfigurationSection("variables"))
		{
			for(String variableString : configurationSection.getConfigurationSection("variables").getKeys(false))
			{
				Map<String, List<String>> replacementMap = new HashMap<>();
				ConfigurationSection optionSection = configurationSection.getConfigurationSection("variables." + variableString + ".options");
				for(String option : optionSection.getKeys(false))
				{
					List<String> replacement;
					if(optionSection.isString(option))
					{
						//String -> Single Line List
						replacement = Collections.singletonList(optionSection.getString("option"));
					}
					else
					{
						replacement = optionSection.getStringList(option);
					}
					replacementMap.put(option, replacement);
				}
				guiVariables.add(new GUIVariables.PermissionVariable(variableString, replacementMap));
			}
		}

		effectiveConstructor(guiTitle, guiSize, openSound, fillerItem, guiElements, guiVariables);
	}

	public GUIElement[] loadGUIElements(ConfigurationSection configurationSection, GUIElement[] guiElements)
	{
		if(configurationSection.contains("elements") && configurationSection.isConfigurationSection("elements"))
		{
			for(String slotString : configurationSection.getConfigurationSection("elements").getKeys(false))
			{
				if(StringUtils.isNumeric(slotString))
				{
					int slot = Integer.parseInt(slotString);
					if(slot < guiElements.length)
					{
						GUIElement guiElement = loadGUIElement(configurationSection, "elements." + slotString, slot);
						if(guiElement == GUIElement.DEFAULT_GUI_ELEMENT)
						{
							continue;
						}
						
						guiElements[slot] = guiElement;
					}
					else
					{
						BUtil.logInfo("Slot '" + slotString + "' in " + configurationSection.getCurrentPath() + " " + slotString + " is outside the gui! (" + guiElements.length + ")");
					}
				}
				else
				{
					BUtil.logInfo("Slot '" + slotString + "' in " + configurationSection.getCurrentPath() + " " + slotString + " is not a valid integer");
				}
			}
		}
		return guiElements;
	}
	
	protected GUIElement loadGUIElement(ConfigurationSection baseSection, String subSection, int slot)
	{
		return loadGUIElement(baseSection, subSection, slot, null, null);
	}
	
	protected GUIElement loadGUIElement(ConfigurationSection baseSection, String subSection, int slot,
	/*ItemContainer Specifics*/         EnumSet<GUIUtil.ItemContainer.ItemContainerVariable> checkedErrors,
	                                    Map<GUIUtil.ItemContainer.ItemContainerVariable, Object> overriddenValues)
	{
		ConfigurationSection subConfigurationSection = baseSection.getConfigurationSection(subSection);
		if(subConfigurationSection == null)
		{
			BUtil.logError("Error loading GUI Element at slot '" + slot + "' in GUI " + guiTitle + " (" + baseSection.getCurrentPath() + "." + subSection + ")");
			return GUIElement.DEFAULT_GUI_ELEMENT;
		}
		
		//By default use all warnings
		if(checkedErrors == null)
		{
			checkedErrors = EnumSet.allOf(GUIUtil.ItemContainer.ItemContainerVariable.class);
		}
		
		return loadGUIElement(subConfigurationSection, slot, checkedErrors, overriddenValues);
	}
	
	private GUIElement loadGUIElement(ConfigurationSection configurationSection, int slot,
	                                  EnumSet<GUIUtil.ItemContainer.ItemContainerVariable> checkedErrors,
	                                  Map<GUIUtil.ItemContainer.ItemContainerVariable, Object> overriddenValues)
	{
		GUIUtil.ItemContainer itemContainer =
			GUIUtil.ItemContainer.buildItemContainer(configurationSection, checkedErrors, overriddenValues);
		if(itemContainer == null)
		{
			//An error occurred and was printed to console
			return GUIElement.DEFAULT_GUI_ELEMENT;
		}
		
		return new GUIElement(itemContainer, ElementActions.GUIAction.getElementActions(configurationSection, slot));
	}

	/*Simple Setters since I can't use Lombok's auto-generated constructor*/

	private void effectiveConstructor(String guiTitle, int guiSize, GUISound openSound, ItemStack fillerItem, GUIElement[] guiElements, Deque<GUIVariables.GUIVariable> guiVariables)
	{
		this.guiTitlePattern = Pattern.compile(guiTitle.replaceAll("\\{.*\\}", ".*"));
		this.guiTitle = guiTitle;
		this.guiSize = guiSize;
		this.openSound = openSound;
		this.fillerItem = fillerItem;
		this.guiElements = guiElements;
		this.guiVariables = guiVariables;
	}

}
