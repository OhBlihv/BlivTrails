package net.auscraft.BlivTrails;

import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.listeners.GUIListener;
import net.auscraft.BlivTrails.util.GUIUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemListener implements Listener
{

	private TrailManager listener;

	private ItemStack guiItem = null;
	private int guiItemSlot = 0;

	private Material guiItemMaterial;
	private String guiItemDisplayname;

	public ItemListener()
	{
		FlatFile cfg = FlatFile.getInstance();

		if(cfg.getConfigurationSection("misc.gui-item") != null)
		{
			GUIUtil.ItemContainer tempContainer = GUIUtil.ItemContainer.buildItemContainer(cfg.getConfigurationSection("misc.gui-item"), true);
			if(tempContainer != null)
			{
				guiItem = tempContainer.toItemStack(null);

				ItemMeta itemMeta = guiItem.getItemMeta();

				guiItemMaterial = guiItem.getType();
				guiItemDisplayname = itemMeta.getDisplayName();

				guiItemSlot = cfg.getInt("misc.gui-item.position");
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event)
	{
		if ((event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) && guiItem != null)
		{
			if (event.getItem() == null || event.getItem().getType().equals(Material.AIR))
			{
				return;
			}

			Player player = event.getPlayer();

			if (event.getItem().getType() == guiItemMaterial && event.getItem().getItemMeta().getDisplayName().equals(guiItemDisplayname))
			{
				event.setCancelled(true);
				GUIListener.mainMenu(player);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (guiItem != null && !event.getPlayer().getInventory().contains(guiItem))
		{
			//If there is already an item in its place
			if (event.getPlayer().getInventory().getItem(guiItemSlot) != null)
			{
				event.getPlayer().getInventory().addItem(guiItem);
			}
			else
			{
				event.getPlayer().getInventory().setItem(guiItemSlot, guiItem);
			}
		}
	}
}
