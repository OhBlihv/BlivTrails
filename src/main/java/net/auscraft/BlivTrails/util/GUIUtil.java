package net.auscraft.BlivTrails.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class GUIUtil
{

	//Used for Unit Testing outside CraftBukkit
	private static class CustomItemStack extends ItemStack
	{

		private Material material;
		private int amount;

		public CustomItemStack(Material material, int amount)
		{
			this.material = material;
			this.amount = amount;
		}

		@Override
		public int getMaxStackSize()
		{
			return 64;
		}

		@Override
		public int getAmount()
		{
			return this.amount;
		}

		@Override
		public String toString()
		{
			return material.name() + " " + amount;
		}

	}

	@AllArgsConstructor
	public static class ItemContainer
	{

		//Set all to a 'default' unusable value to indicate if it needs changing
		@Getter
		private Material material = null;
		@Getter
		private int damage = -1;
		@Getter
		private int amount = -1;
		@Getter
		private String displayName = null;
		@Getter
		private List<String> lore = null;
		//Cannot use true/false for this, since the only two options (both) are valid
		//1 -> Add | 0 -> Remove | -1 -> Invalid/Do not change
		@Getter
		private int isEnchanted = -1;
		@Getter
		private Map<Enchantment, Integer> enchantmentMap = null;

		@Getter
		private String owner = null;

		public ItemStack replaceItemStack(ItemStack original, String playerName)
		{
			ItemMeta meta = original.getItemMeta();
			//Cannot clone, since it loses attributes for some reason.
			if(material != null)
			{
				original.setType(material);
			}
			if((material == Material.SKULL_ITEM || material == Material.SKULL) && playerName != null)
			{
				((SkullMeta) meta).setOwner(playerName);
			}
			if(damage != -1)
			{
				original.setDurability((short) damage);
			}
			if(amount != -1)
			{
				original.setAmount(amount);
			}
			if(displayName != null)
			{
				meta.setDisplayName(displayName);
			}
			if(meta.hasDisplayName() && playerName != null)
			{
				meta.setDisplayName(meta.getDisplayName().replace("{player}", playerName));
			}
			if(lore != null)
			{
				meta.setLore(lore);
			}
			if(enchantmentMap != null)
			{
				for(Enchantment enchantment : original.getEnchantments().keySet())
				{
					original.removeEnchantment(enchantment);
				}
				original.addEnchantments(enchantmentMap);
			}

			original.setItemMeta(meta);
			return original;
		}

		public ItemStack toItemStack(String playerName)
		{
			ItemStack itemStack = new ItemStack(material, amount, (short) damage);
			ItemMeta itemMeta = itemStack.getItemMeta();

			if(displayName != null)
			{
				itemMeta.setDisplayName(displayName);
			}
			/*if((material == Material.SKULL_ITEM || material == Material.SKULL) && playerName != null)
			{
				((SkullMeta) itemMeta).setOwner(playerName);
				if(itemMeta.hasDisplayName())
				{
					itemMeta.setDisplayName(itemMeta.getDisplayName().replace("{player}", playerName));
				}
			}*/
			if(lore != null)
			{
				itemMeta.setLore(lore);
			}
			if(enchantmentMap != null)
			{
				itemStack.addEnchantments(enchantmentMap);
			}

			itemStack.setItemMeta(itemMeta);

			return itemStack;
		}

		//Use the same method, but with a control flag to avoid the bad material check.
		//Useful for creating new items, but not useful for replacing old ones
		public static ItemContainer buildItemContainer(ConfigurationSection configurationSection, boolean errorChecking)
		{
			if(configurationSection == null)
			{
				BUtil.logError("One of the gui-item's configuration sections is invalid! Please check your configs for any blank sections.");
				return null;
			}
			Material material = null;
			{
				String materialString = configurationSection.getString("material", null);
				if(materialString != null)
				{
					material = Material.getMaterial(materialString);
				}

				if(material == null && errorChecking)
				{
					BUtil.logError(getErrorMessage(configurationSection, "material", materialString));
					return null;
				}
			}
			int damage = configurationSection.getInt("damage", 0), amount = configurationSection.getInt("amount", 1);
			String displayName = BUtil.translateColours(configurationSection.getString("displayname", ""));
			if(displayName.isEmpty())
			{
				//Support for the BlivTrails gui-item
				displayName = BUtil.translateColours(configurationSection.getString("name", ""));
			}

			Map<Enchantment, Integer> enchantmentMap = null;
			int isEnchanted = -1;
			if(configurationSection.get("enchanted") != null)
			{
				if(configurationSection.isBoolean("enchanted"))
				{
					isEnchanted = configurationSection.getBoolean("enchanted", false) ? 1 : 0;
				}
				else
				{
					enchantmentMap = addEnchantments(configurationSection.getStringList("enchanted"));
				}
			}

			List<String> lore = BUtil.translateColours(configurationSection.getStringList("lore"));

			String owner = null;
			if(configurationSection.contains("owner"))
			{
				owner = configurationSection.getString("owner");
			}

			return new ItemContainer(material, damage, amount, displayName, lore, isEnchanted, enchantmentMap, owner);
		}

		private static final Pattern PATTERN_SEPERATOR = Pattern.compile(":");

		/**
		 *
		 * @param configurationMap Map containing item structure
		 * @param prefix Path prefix to load Item Configuration From
		 *               Very useful if map doesnt directly contain the ConfigurationSection containing the item configuration.
		 *               If the item is actually under the Map -> item:, adding item as the prefix adds "<prefix>." to all
		 *               map queries, properly retrieving the item even when nested.
		 * @return ItemContainer
		 */
		public static ItemContainer buildItemContainer(Map<String, Object> configurationMap, String prefix)
		{
			/*if(prefix != null && prefix.length() > 0)
			{
				prefix += ".";
			}*/
			prefix = ""; //Temporary Test
			if(configurationMap == null || configurationMap.isEmpty())
			{
				BUtil.logError("One of the gui-item's configuration sections is invalid! Please check your configs for any blank sections.");
				return null;
			}
			Material material;
			{
				Object materialObj = configurationMap.get(prefix + "material");
				if(materialObj != null && !materialObj.toString().isEmpty())
				{
					material = Material.getMaterial(materialObj.toString());
					if(material == null)
					{
						BUtil.logError("Material: '" + materialObj.toString() + "' is not a valid material");
						return null;
					}
				}
				else
				{
					if(materialObj != null)
					{
						BUtil.logError("Material: '" + materialObj.toString() + "' is not a valid material");
					}
					else
					{
						BUtil.logError("Material of reward was null!");
					}
					return null;
				}
			}
			String displayName = "";
			{
				Object displayNameObj = configurationMap.get(prefix + "displayname");
				if(displayNameObj != null && !displayNameObj.toString().isEmpty())
				{
					displayName = BUtil.translateColours(displayNameObj.toString());
				}
			}
			int damage = 0;
			{
				Object damageObj = configurationMap.get(prefix + "damage");
				if(damageObj != null && Integer.parseInt(damageObj.toString()) > 0)
				{
					damage = Integer.parseInt(damageObj.toString());
				}
			}
			List<String> lore = null;
			{
				Object loreObj = configurationMap.get(prefix + "lore");
				if(loreObj != null && !((List<String>) loreObj).isEmpty())
				{
					lore = BUtil.translateColours((List<String>) loreObj);
				}
			}

			Map<Enchantment, Integer> enchantmentMap = null;
			int enchanted = -1;
			{
				Object enchantmentObj = configurationMap.get(prefix + "enchanted");
				//Instanceof wont detect a List object type if it is null
				if(enchantmentObj instanceof List)
				{
					enchantmentMap = new HashMap<>();
					if(!((List<String>) enchantmentObj).isEmpty())
					{
						List<String> enchantmentList = (List<String>) enchantmentObj;
						for(String enchantmentLine : enchantmentList)
						{
							Enchantment enchantment = Enchantment.getByName(PATTERN_SEPERATOR.split(enchantmentLine)[0]);
							if(enchantment == null)
							{
								BUtil.logError("Enchantment '" + enchantmentLine + "' is not a valid enchantment configuration. Check your config.");
								continue;
							}

							try
							{
								enchantmentMap.put(enchantment, Integer.parseInt(PATTERN_SEPERATOR.split(enchantmentLine)[1]));
								/*if(displayName.contains("Nex"))
								{
									BUtil.logInfo("Loaded enchantment " + enchantment.getName() + " " + PATTERN_SEPERATOR.split(enchantmentLine)[1]);
								}*/
							}
							catch(NumberFormatException e)
							{
								BUtil.logError("Enchantment " + enchantment.getName() + " has an invalid level");
							}
						}

						if(enchantmentList.isEmpty())
						{
							enchantmentMap = null;
						}
					}
				}
				else if(enchantmentObj instanceof Boolean)
				{
					enchanted = Boolean.parseBoolean(enchantmentObj.toString()) ? 1 : 0;
				}
			}
			int amount = 1;
			{
				Object amountObj = configurationMap.get(prefix + "amount");
				if(amountObj != null && Integer.parseInt(amountObj.toString()) > 0)
				{
					amount = Integer.parseInt(amountObj.toString());
				}
			}

			String owner = null;
			{
				Object ownerObj = configurationMap.get(prefix + "owner");
				if(ownerObj != null && !ownerObj.toString().isEmpty())
				{
					owner = BUtil.translateColours(ownerObj.toString());
				}
			}

			return new ItemContainer(material, damage, amount, displayName, lore, enchanted, enchantmentMap, owner);
		}

	}

	private static final int STACK_SIZE = 64;
	public static final ItemStack DEFAULT_ITEMSTACK = new ItemStack(Material.POTATO_ITEM, 1, (short) 15);

	public static ItemStack loadItem(ConfigurationSection configurationSection)
	{
		ItemContainer itemContainer = ItemContainer.buildItemContainer(configurationSection, true);
		if(itemContainer == null)
		{
			return DEFAULT_ITEMSTACK;
		}

		return createItem(itemContainer);
	}

	private static String getErrorMessage(ConfigurationSection configurationSection, String path, String inputValue)
	{
		return "Error reading " + path + " at (" + configurationSection.getCurrentPath() + ")\n" +
				       "Make sure this is a valid entry: " + (inputValue != null ? String.valueOf(inputValue) : "<BLANK>");
	}

	public static ItemStack createItem(ItemContainer itemContainer)
	{
		return createItem(itemContainer.getMaterial(), itemContainer.getDamage(), itemContainer.getAmount(), itemContainer.getDisplayName(), itemContainer.getEnchantmentMap(), itemContainer.getLore());
	}

	public static ItemStack createItem(Material material, int damage, int amount, String displayName, Map<Enchantment, Integer> enchants, List<String> lore)
	{
		ItemStack item = new ItemStack(material, amount, (short) damage);
		ItemMeta meta = item.getItemMeta();

		if(displayName != null && !displayName.isEmpty())
		{
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
		}

		if(lore != null && !lore.isEmpty())
		{
			meta.setLore(BUtil.translateColours(lore));
		}

		item.setItemMeta(meta);

		if(enchants != null && !enchants.isEmpty())
		{
			if(material == Material.ENCHANTED_BOOK)
			{
				EnchantmentStorageMeta encMeta = (EnchantmentStorageMeta) item.getItemMeta();
				for(Entry<Enchantment, Integer> enchantment : enchants.entrySet())
				{
					encMeta.addStoredEnchant(enchantment.getKey(), enchantment.getValue(), true);
				}
				item.setItemMeta(encMeta);
			}
			else
			{
				item.addUnsafeEnchantments(enchants);
			}
		}

		return item;
	}

	private static final Pattern COLON_SPLIT = Pattern.compile("[:]");
	
	public static Map<Enchantment, Integer> addEnchantments(List<String> enchantList)
	{
		if(enchantList == null || enchantList.isEmpty())
		{
			return null;
		}
		
		HashMap<Enchantment, Integer> enchantMap = new HashMap<>();
		for(String enchantLine : enchantList)
		{
			String[] split = COLON_SPLIT.split(enchantLine);
			enchantMap.put(Enchantment.getByName(split[0]), Integer.parseInt(split[1]));
		}
		
		return enchantMap;
	}
	
	public static int countEmpty(Inventory inv)
	{
		int count = 0;
		for(ItemStack item : inv)
		{
			if(item == null)
			{
				count++;
			}
		}
		return count;
	}

	public static int countMaterial(Inventory inv, Material material)
	{
		int count = 0;
		for(ItemStack item : inv.all(material).values())
		{
			count += item.getAmount();
		}
		return count;
	}

	/**
	 * Overly simplistic 'merge stacks' function.
	 * Required improvements:
	 *          - .isSimilar() checks
	 *          - Stricter requirements for equality
	 *
	 * @param itemStacks Input ItemStacks
	 * @return Minimum ItemStacks required
	 */
	public static ItemStack[] mergeStacks(Collection<? extends ItemStack> itemStacks)
	{
		if(itemStacks.isEmpty())
		{
			return null;
		}

		if(itemStacks.size() == 1)
		{
			//Only way to access the first element outside anything overly complicated for this use-case
			return new ItemStack[] {itemStacks.iterator().next()};
		}

		int currentStackCount = 0, leftOver = 0, stackSize;
		Material material;
		//Seems to be the only way to get any item of a 'Collection'
		{
			ItemStack item = itemStacks.iterator().next();
			stackSize = STACK_SIZE;
			material = item.getType();
		}

		if(material == null)
		{
			return null;
		}

		//Create a larger array the size of the collection, so that is is definitely large enough. Resize it later.
		ItemStack[] mergedStacks = new ItemStack[itemStacks.size() + 1];
		for(ItemStack item : itemStacks)
		{
			//BUtil.logInfo("Processing: " + item.toString());
			//Items shouldn't be over their stack size
			if(item.getAmount() >= stackSize)
			{
				ItemStack tempItem = mergedStacks[currentStackCount];
				mergedStacks[currentStackCount++] = item;
				mergedStacks[currentStackCount] = tempItem;
				//BUtil.logInfo("Over Stack Size!");
				continue;
			}

			ItemStack currentStack = mergedStacks[currentStackCount];

			if(currentStack == null)
			{
				mergedStacks[currentStackCount] = item;
				//BUtil.logInfo("Replacing Null Object");
				continue;
			}

			int newAmount = currentStack.getAmount() + item.getAmount();
			//BUtil.logInfo("New Amount " + newAmount + " | Current Stack: " + currentStack.getAmount() + " | Item " + item.getAmount());
			if(newAmount <= stackSize)
			{
				mergedStacks[currentStackCount].setAmount(newAmount);
				if(newAmount == stackSize)
				{
					currentStackCount++;
				}
			}
			else //Add any left over items to the leftOver variable, to be compiled later
			{
				int tempLeftOver = newAmount - stackSize;
				mergedStacks[currentStackCount++].setAmount(newAmount - tempLeftOver);
				leftOver += tempLeftOver;
				//BUtil.logInfo("Adding " + tempLeftOver + " to leftOver equalling " + leftOver);
			}
		}

		while(leftOver > 0)
		{
			if(leftOver >= stackSize)
			{
				mergedStacks[currentStackCount++] = new ItemStack(material, stackSize);
				leftOver -= stackSize;
			}
			else
			{
				mergedStacks[currentStackCount++] = new ItemStack(material, leftOver);
				leftOver = 0;
			}
		}

		//Move the array into the most appropriate size (no empty/null elements)
		int usedElements = 0;
		for(ItemStack stack : mergedStacks)
		{
			if(stack == null)
			{
				break;
			}
			usedElements++;
		}

		if(usedElements != mergedStacks.length)
		{
			ItemStack[] mergedStacksTemp = new ItemStack[usedElements];
			//Copy the array to a proper size
			System.arraycopy(mergedStacks, 0, mergedStacksTemp, 0, usedElements);
			mergedStacks = mergedStacksTemp;
		}

		BUtil.logInfo("Returning a set of " + Arrays.toString(mergedStacks));

		return mergedStacks;
	}

	/**
	 * Returns an array of slots vertical to the slot input.
	 * Useful for stacking multiple stacks of the same material
	 * in easy to reach slots.
	 *
	 * @param slot (Most likely) Hotbar slot
	 * @return Array of slots above (and including) the input slot
	 */
	public static int[] getVerticalSlots(int slot, int requiredSlots)
	{
		if(requiredSlots <= 1)
		{
			return new int[] {slot};
		}

		int[] verticalSlots = new int[requiredSlots];
		verticalSlots[0] = slot;

		//Is on the hotbar
		if(slot < 9)
		{
			int currentSlot = slot;
			for(int invSlot = 1;invSlot < verticalSlots.length;invSlot++)
			{
				if(currentSlot == slot)
				{
					currentSlot = 27 + slot; //Receive the slot directly above the hotbar
				}
				else
				{
					//If the current slot is already on the top bar, move down to the next column
					if(currentSlot <= 17)
					{
						currentSlot = 27 + (slot + 1);
					}
					else
					{
						//Else, move up one row
						currentSlot -= 9;
					}
				}

				verticalSlots[invSlot] = currentSlot;
			}
		}

		//BUtil.logInfo("Vertical Slots: " + Arrays.toString(verticalSlots));
		return verticalSlots;
	}
	
	public static boolean canAddItem(Inventory inv, ItemStack item)
	{
		for(ItemStack itemLoop : inv)
		{
			if(itemLoop == null)
			{
				return true;
			}
			
			if(itemLoop.isSimilar(item))
			{
				if((itemLoop.getAmount() + item.getAmount()) < itemLoop.getMaxStackSize())
				{
					return true;
				}
			}
		}
		
		return false;
	}

}
