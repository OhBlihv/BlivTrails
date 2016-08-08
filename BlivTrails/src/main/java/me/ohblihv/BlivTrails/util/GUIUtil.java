package me.ohblihv.BlivTrails.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.ohblihv.BlivTrails.BlivTrails;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.bukkit.Material.AIR;

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

		public enum ItemContainerVariable
		{

			MATERIAL,
			DAMAGE,
			AMOUNT,
			DISPLAYNAME,
			LORE,
			ENCHANTMENTS,
			ENCHANTED,
			OWNER

		}

		public enum EnchantStatus
		{

			NO_CHANGE,
			ADD,
			REMOVE;

			public ItemStack alterEnchantmentStatus(ItemStack itemStack)
			{
				switch(this)
				{
					case ADD: return BlivTrails.getInstance().getNMSHelper().addEnchantmentEffect(itemStack);
					case REMOVE: return BlivTrails.getInstance().getNMSHelper().removeEnchantmentEffect(itemStack);
				}
				return itemStack; //NO_CHANGE
			}

			public static EnchantStatus getEnchantStatus(int enchantValue)
			{
				//+1 since the old NO_CHANGE was -1, and the ordinal values start at 0
				return values()[enchantValue + 1];
			}

		}

		//Set all to a 'default' unusable value to indicate if it needs changing
		@Getter
		private final Material material;

		@Getter
		private final int   damage,
							amount;
		@Getter
		private final String displayName;

		@Getter
		private final List<String> lore;

		private final EnchantStatus enchantStatus;

		@Getter
		private final Map<Enchantment, Integer> enchantmentMap;

		@Getter
		private final String owner;

		public int getMaxStackSize()
		{
			return material.getMaxStackSize();
		}

		public int getMaxDurability()
		{
			return material.getMaxDurability();
		}

		//'Legacy' Constructor
		public ItemContainer(Material material, int damage, int amount, String displayName, List<String> lore,
		                     int isEnchanted, Map<Enchantment, Integer> enchantmentMap, String owner)
		{
			this(material, damage, amount, displayName, lore, EnchantStatus.getEnchantStatus(isEnchanted), enchantmentMap, owner);
		}

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
			if(playerName != null && meta.hasDisplayName())
			{
				meta.setDisplayName(meta.getDisplayName().replace("{player}", playerName));
			}
			if(lore != null)
			{
				meta.setLore(lore);
			}

			original = enchantStatus.alterEnchantmentStatus(original);

			if(enchantmentMap != null && !enchantmentMap.isEmpty())
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
			if(material == AIR)
			{
				return null;
			}

			ItemStack itemStack = new ItemStack(material, amount, (short) damage);
			ItemMeta itemMeta = itemStack.getItemMeta();

			if(displayName != null)
			{
				itemMeta.setDisplayName(displayName);
			}
			if((material == Material.SKULL_ITEM || material == Material.SKULL) && playerName != null)
			{
				((SkullMeta) itemMeta).setOwner(playerName);
				if(itemMeta.hasDisplayName())
				{
					itemMeta.setDisplayName(itemMeta.getDisplayName().replace("{player}", playerName));
				}
			}
			if(lore != null)
			{
				itemMeta.setLore(lore);
			}
			if(enchantmentMap != null)
			{
				itemStack.addEnchantments(enchantmentMap);
			}

			itemStack.setItemMeta(itemMeta);

			return enchantStatus.alterEnchantmentStatus(itemStack);
		}

		public static ItemContainer buildItemContainer(ConfigurationSection configurationSection)
		{
			return buildItemContainer(configurationSection,
			                          EnumSet.allOf(ItemContainerVariable.class),
			                          null);
		}

		//Use the same method, but with a control flag to avoid the bad material check.
		//Useful for creating new items, but not useful for replacing old ones
		@SuppressWarnings("unchecked")
		public static ItemContainer buildItemContainer(ConfigurationSection configurationSection,
		                                               EnumSet<ItemContainerVariable> checkedErrors,
		                                               Map<ItemContainerVariable, Object> overriddenValues)
		{
			if(configurationSection == null)
			{
				BUtil.logError("One of the gui-item's configuration sections is invalid! Please check your configs for any blank sections.");
				return null;
			}

			if(overriddenValues == null)
			{
				overriddenValues = Collections.emptyMap();
			}

			Material material = null;
			if(overriddenValues.containsKey(ItemContainerVariable.MATERIAL))
			{
				material = (Material) overriddenValues.get(ItemContainerVariable.MATERIAL);
			}
			else
			{
				String materialString = configurationSection.getString("material", null);
				if(materialString != null)
				{
					material = Material.getMaterial(materialString);
				}

				if(material == null)
				{
					if(checkedErrors.contains(ItemContainerVariable.MATERIAL))
					{
						BUtil.logError(getErrorMessage(configurationSection, "material", materialString));
					}
					
					//Still override the material for now, even if we aren't asked to
					material = Material.POTATO_ITEM;
				}
			}
			int damage = overriddenValues.containsKey(ItemContainerVariable.DAMAGE) ?
				             (int) overriddenValues.get(ItemContainerVariable.DAMAGE) :
				             configurationSection.getInt("damage", 0),
				amount = overriddenValues.containsKey(ItemContainerVariable.AMOUNT) ?
					         (int) overriddenValues.get(ItemContainerVariable.AMOUNT) :
						     configurationSection.getInt("amount", 1);

			String displayName = overriddenValues.containsKey(ItemContainerVariable.DISPLAYNAME) ?
				                     BUtil.translateColours((String) overriddenValues.get(ItemContainerVariable.DISPLAYNAME)) :
					                 BUtil.translateColours(configurationSection.getString("displayname", ""));

			Map<Enchantment, Integer> enchantmentMap = (Map<Enchantment, Integer>) overriddenValues.get(ItemContainerVariable.ENCHANTMENTS);
			int isEnchanted = (int) overriddenValues.getOrDefault(ItemContainerVariable.ENCHANTED, -1);
			if((isEnchanted != -1 || enchantmentMap != null) && configurationSection.get("enchanted") != null)
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

			List<String> lore = overriddenValues.containsKey(ItemContainerVariable.LORE) ?
				                    BUtil.translateColours((List<String>) overriddenValues.get(ItemContainerVariable.LORE)) :
					                BUtil.translateColours(configurationSection.getStringList("lore"));

			String owner = (String) overriddenValues.get(ItemContainerVariable.OWNER);
			if(owner == null && configurationSection.contains("owner"))
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
		 *               Very useful if map doesn't directly contain the ConfigurationSection containing the item configuration.
		 *               If the item is actually under the Map -> item:, adding item as the prefix adds "<prefix>." to all
		 *               map queries, properly retrieving the item even when nested.
		 * @return ItemContainer
		 */
		public static ItemContainer buildItemContainer(Map<String, Object> configurationMap, String path)
		{
			if(configurationMap == null || configurationMap.isEmpty())
			{
				BUtil.logError("One of the gui-item's configuration sections is invalid! Please check your configs for any blank sections.");
				return null;
			}
			Material material = FlatFile.getMaterial(configurationMap, "material", null);
			if(material == null)
			{
				BUtil.logError("Material: '" + FlatFile.getString(configurationMap, "material", "null") + "' is not a valid material");
				return null;
			}

			String  displayName = BUtil.translateColours(FlatFile.getString(configurationMap, "displayname", null)),
					owner = FlatFile.getString(configurationMap, "owner", null);
			int damage = FlatFile.getInt(configurationMap, "damage", 0),
				amount = FlatFile.getInt(configurationMap, "amount", 1);
			List<String> lore = FlatFile.getStringList(configurationMap, "lore");

			Map<Enchantment, Integer> enchantmentMap = null;
			int enchanted = -1;
			{
				Object enchantmentObj = configurationMap.get("enchanted");
				//Instanceof won't detect a List object type if it is null
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

			return new ItemContainer(material, damage, amount, displayName, lore, enchanted, enchantmentMap, owner);
		}

	}

	private static final int STACK_SIZE = 64;
	public static final ItemStack DEFAULT_ITEMSTACK = BlivTrails.getInstance().getNMSHelper().addEnchantmentEffect(new ItemStack(Material.POTATO_ITEM, 1, (short) 15));

	public static ItemStack loadItem(ConfigurationSection configurationSection)
	{
		ItemContainer itemContainer = ItemContainer.buildItemContainer(configurationSection,
		                                                               EnumSet.allOf(ItemContainer.ItemContainerVariable.class),
		                                                               null);
		if(itemContainer == null)
		{
			return DEFAULT_ITEMSTACK;
		}

		return itemContainer.toItemStack(null);
	}

	private static String getErrorMessage(ConfigurationSection configurationSection, String path, String inputValue)
	{
		return "Error reading " + path + " at (" + configurationSection.getCurrentPath() + ")\n" +
				       "Make sure this is a valid entry: " + (inputValue != null ? String.valueOf(inputValue) : "<BLANK>");
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
	
	public static int countEmpty(Inventory inventory)
	{
		int count = 0;
		for(ItemStack item : inventory)
		{
			if(item == null)
			{
				count++;
			}
		}
		return count;
	}

	public static int countMaterial(Inventory inventory, Material material)
	{
		int count = 0;
		for(ItemStack item : inventory.all(material).values())
		{
			count += item.getAmount();
		}
		return count;
	}

	public static int countItem(Inventory inventory, Material material, short durability)
	{
		int count = 0;
		for(ItemStack item : inventory.all(material).values())
		{
			if(item.getDurability() == durability)
			{
				count += item.getAmount();
			}
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
			//Items shouldn't be over their stack size
			if(item.getAmount() >= stackSize)
			{
				ItemStack tempItem = mergedStacks[currentStackCount];
				mergedStacks[currentStackCount++] = item;
				mergedStacks[currentStackCount] = tempItem;
				continue;
			}

			ItemStack currentStack = mergedStacks[currentStackCount];

			if(currentStack == null)
			{
				mergedStacks[currentStackCount] = item;
				continue;
			}

			int newAmount = currentStack.getAmount() + item.getAmount();
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

	public static final ItemStack BLANK_ITEM = new ItemStack(AIR, 0);

	public static void removeItemCount(Inventory inventory, Material material, short damage, int count)
	{
		Iterator<? extends Map.Entry<Integer, ? extends ItemStack>> itemStackItr = inventory.all(material).entrySet().iterator();
		while(count > 0 && itemStackItr.hasNext())
		{
			Map.Entry<Integer, ? extends ItemStack> entry = itemStackItr.next();
			if(entry.getValue().getDurability() != damage)
			{
				continue; //Only remove what is matched
			}

			if(count > entry.getValue().getAmount())
			{
				count -= entry.getValue().getAmount();
				inventory.setItem(entry.getKey(), BLANK_ITEM);
			}
			else //Set
			{
				int setAmount = entry.getValue().getAmount() - count;
				entry.getValue().setAmount(setAmount);
				inventory.setItem(entry.getKey(), entry.getValue()); //May be useless, find out.
				break;
			}
		}
	}

}
