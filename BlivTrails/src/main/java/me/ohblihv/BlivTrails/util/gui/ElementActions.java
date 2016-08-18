package me.ohblihv.BlivTrails.util.gui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.ohblihv.BlivTrails.util.BUtil;
import me.ohblihv.BlivTrails.util.GUIUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by OhBlihv (Chris) on 23/11/2015.
 */
public abstract class ElementActions
{

	public enum GUIAction
	{

		COMMAND,
		ITEM,
		CHANGE_ITEM,
		CLOSE_INVENTORY,
		SOUND,

		//Plugin Specific Actions
		
		NONE;

		public static GUIAction getAction(String input)
		{
			for(GUIAction action : values())
			{
				if(input.equals(action.name()))
				{
					return action;
				}
			}
			return NONE;
		}

		public static Deque<ElementAction> getElementActions(ConfigurationSection configurationSection, int slot)
		{
			Deque<ElementAction> elementActionDeque = new ArrayDeque<>();
			if(configurationSection == null || !configurationSection.contains("actions") || !configurationSection.isString("actions"))
			{
				//Quick Exit since there are no actions defined, yet we still want to return the new deque
				return elementActionDeque;
			}

			ConfigurationSection actionSection = configurationSection.getConfigurationSection("actions");
			for(String clickTypeString : actionSection.getKeys(false))
			{
				ClickType clickType = null;
				if(!clickTypeString.equals("ALL"))  //Allow actions to listen on all click types
				{
					try
					{
						clickType = ClickType.valueOf(clickTypeString);
					}
					catch(IllegalArgumentException e)
					{
						BUtil.logInfo("Unknown click type provided: '" + clickTypeString + "' at key " + actionSection.getCurrentPath() + "." + clickTypeString);
						continue;
					}
				}
				
				ConfigurationSection clickSection = actionSection.getConfigurationSection(clickTypeString);
				
				String elementActionString = clickSection.getString("actions", "");
				
				//Split by comma and any number of spaces
				for(String elementActionStringLoop : elementActionString.split(",[ ]*"))
				{
					GUIAction action = GUIAction.getAction(elementActionStringLoop);
					if(action != null && action != GUIAction.NONE)
					{
						ElementAction elementAction = getAction(clickType, action, clickSection.getConfigurationSection("options." + action.name()));
						if(elementAction != null)
						{
							elementActionDeque.add(elementAction);
						}
					}
					else
					{
						BUtil.logInfo("Slot '" + slot + "' in " + configurationSection.getCurrentPath() + " " + slot + " has an invalid action: " + elementActionStringLoop);
					}
				}
			}
			
			return elementActionDeque;
		}
		
		private static ElementAction getAction(ClickType clickType, GUIAction action, ConfigurationSection actionSection)
		{
			switch(action)
			{
				case COMMAND:
				{
					return CommandAction.loadAction(clickType, actionSection);
				}
				case ITEM:
				{
					return new ItemAction(clickType, GUIUtil.ItemContainer.buildItemContainer(actionSection));
				}
				case CHANGE_ITEM:
				{
					return new ChangeItemAction(clickType, GUIUtil.ItemContainer.buildItemContainer(actionSection));
				}
				case CLOSE_INVENTORY:
				{
					return new CloseInventoryAction(clickType);
				}
				case SOUND:
				{
					return SoundAction.loadAction(clickType, actionSection);
				}
				/*
				 * Plugin-Specific Actions
				 */
				
				default: break;
			}
			
			return null;
		}

	}

	@RequiredArgsConstructor
	public static abstract class ElementAction
	{
		
		/*
		 * If an action includes a click type, this action will only be performed when that click is provided.
		 * Actions without click types are executed regardless of the click type.
		 */
		final ClickType clickType;
		
		abstract boolean onClick(Player player, int slot);
		
		public boolean hasClickType()
		{
			return clickType != null;
		}

	}
	
	public static class CommandAction extends ElementAction
	{

		@AllArgsConstructor
		private static class CommandContainer
		{

			@Getter
			private String command;

			@Getter
			private CommandActionExecutor executor;

		}

		public enum CommandActionExecutor
		{

			CONSOLE,
			PLAYER

		}

		@Getter
		private final Deque<CommandContainer> commands;
		
		public CommandAction(ClickType clickType, Deque<CommandContainer> commands)
		{
			super(clickType);
			
			this.commands = commands;
		}

		@Override
		public boolean onClick(Player player, int slot)
		{
			for(CommandContainer commandContainer : commands)
			{
				if(commandContainer.getExecutor() == CommandActionExecutor.CONSOLE)
				{
					String finalCommand = commandContainer.getCommand().replaceAll("\\{player\\}", player.getName());
					//TODO: Re-enable these with a toggle-able debug
					//BUtil.logInfo("Executing as CONSOLE: " + finalCommand);
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
				}
				else //Change this if more executors come up. But I doubt it
				{
					String finalCommand = "/" + commandContainer.getCommand().replaceAll("\\{player\\}", player.getName());
					//BUtil.logInfo("Executing as PLAYER: " + finalCommand);
					player.chat(finalCommand);
				}
			}
			return true;
		}

		public static CommandAction loadAction(ClickType clickType, ConfigurationSection configurationSection)
		{
			Deque<CommandContainer> commandContainers = new ArrayDeque<>();

			if(configurationSection.getKeys(false).isEmpty())
			{
				BUtil.logError( "No commands defined for COMMAND action. Configure as such: \n" +
								"actions: COMMAND\n" +
								"options:\n" +
								"   COMMAND:\n" +
								"       \"say Example\":\n" +
								"           execute-as: CONSOLE");
			}
			else
			{
				for(String commandName : configurationSection.getKeys(false))
				{
					CommandActionExecutor commandActionExecutor = CommandActionExecutor.CONSOLE;
					if(configurationSection.isConfigurationSection(commandName))
					{
						ConfigurationSection commandSection = configurationSection.getConfigurationSection(commandName);

						if(!configurationSection.isConfigurationSection(commandName) || !commandSection.contains("execute-as"))
						{
							BUtil.logInfo("'" + commandName + "' command does not contain an explicit execute-as, and has been ignored.");
							continue;
						}

						if(commandSection.getString("execute-as").equalsIgnoreCase("PLAYER"))
						{
							commandActionExecutor = CommandActionExecutor.PLAYER;
						}
					}

					commandContainers.add(new CommandContainer(commandName, commandActionExecutor));
				}
			}

			return new CommandAction(clickType, commandContainers);
		}

	}
	
	public static class ItemAction extends ElementAction
	{

		@Getter
		//Possibly allow for more than one item to be stored?
		private final GUIUtil.ItemContainer item;
		
		public ItemAction(ClickType clickType, GUIUtil.ItemContainer item)
		{
			super(clickType);
			
			this.item = item;
		}

		@Override
		public boolean onClick(Player player, int slot)
		{
			//TODO: Implement this better to factor in full inventories etc?
			player.getInventory().addItem(item.toItemStack(player.getName()));
			//TODO: Return false when the inventory is full? Or any other failure case?
			return true;
		}
		
	}
	
	public static class ChangeItemAction extends ElementAction
	{

		private GUIUtil.ItemContainer toItem;
		
		public ChangeItemAction(ClickType clickType, GUIUtil.ItemContainer toItem)
		{
			super(clickType);
			
			this.toItem = toItem;
		}

		@Override
		public boolean onClick(Player player, int slot)
		{
			player.getOpenInventory().getTopInventory().setItem(slot,
			                         toItem.replaceItemStack(player.getOpenInventory().getTopInventory().getItem(slot), player.getName()));
			player.updateInventory();
			return true;
		}
		
	}

	public static class CloseInventoryAction extends ElementAction
	{
		
		public CloseInventoryAction(ClickType clickType)
		{
			super(clickType);
		}

		@Override
		public boolean onClick(Player player, int slot)
		{
			player.closeInventory();
			return true;
		}

	}
	
	public static class SoundAction extends ElementAction
	{
		
		private final GUIContainer.GUISound sound;
		
		public SoundAction(ClickType clickType, GUIContainer.GUISound sound)
		{
			super(clickType);
			
			this.sound = sound;
		}
		
		@Override
		public boolean onClick(Player player, int slot)
		{
			sound.playSound(player);
			return true;
		}
		
		public static SoundAction loadAction(ClickType clickType, ConfigurationSection configurationSection)
		{
			Sound sound = null;
			if(configurationSection.contains("sound") && configurationSection.isString("sound"))
			{
				try
				{
					sound = Sound.valueOf(configurationSection.getString("sound"));
				}
				catch(IllegalArgumentException e)
				{
					//Sound stays null and is caught by the next check
				}
			}
			
			if(sound == null)
			{
				BUtil.logInfo("Could not load sound '" + (configurationSection.contains("sound") ? configurationSection.getString("sound", "none") : "null") + "'");
				//Default to the first sound. This must simply be a valid sound since the issue is already indicated in console.
				return new SoundAction(clickType, new GUIContainer.GUISound(Sound.values()[0], 10F, 1F));
			}
			
			return new SoundAction(clickType, new GUIContainer.GUISound(sound, (float) configurationSection.getDouble("volume", 10F), (float) configurationSection.getDouble("pitch", 1F)));
		}
		
	}

	/*
	 * Plugin-Specific Actions
	 */

}
