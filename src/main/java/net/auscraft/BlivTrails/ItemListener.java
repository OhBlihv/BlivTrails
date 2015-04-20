package net.auscraft.BlivTrails;

import net.auscraft.BlivTrails.config.ConfigAccessor;

import net.auscraft.BlivTrails.utils.Utilities;
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

	private ConfigAccessor cfg;
	private TrailListener listener;
	private Utilities util;

	public ItemListener(BlivTrails instance)
	{
		cfg = instance.getCfg();
		listener = instance.getListener();
		util = instance.getUtil();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerUse(PlayerInteractEvent event)
	{
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			Player player = event.getPlayer();
			try
			{
				if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR))
				{
					return;
				}
				else if (cfg.getString("misc.gui-item.material").equals("NULL"))
				{
					util.logDebug("Your GUI Item invalid. Either disable this feature, or select a valid material.");
					return;
				}
				else if (player.getItemInHand().getType() == Material.getMaterial(cfg.getString("misc.gui-item.material")) && player.getItemInHand().getItemMeta().getDisplayName().contains(util.stripColours(cfg.getString("misc.gui-item.name"))))
				{
					event.setCancelled(true);
					listener.mainMenu(player);
				}
			}
			catch (NullPointerException e)
			{
				if (cfg.getBoolean("misc.debug"))
				{
					e.printStackTrace();
				}
				util.logDebug("GUI Item is invalid. Check your config.");
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		if (cfg.getBoolean("misc.gui-give-on-join"))
		{
			ItemStack item = new ItemStack(Material.getMaterial(cfg.getString("misc.gui-item.material")));
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(util.translateColours(cfg.getString("misc.gui-item.name")));
			meta.setLore(util.translateColours(cfg.getStringList("misc.gui-item.lore")));
			item.setItemMeta(meta);

			if (!event.getPlayer().getInventory().contains(item)) // Can only have one
			{
				//If there is already an item in its place
				if (event.getPlayer().getInventory().getItem(cfg.getInt("misc.gui-item.position")) != null) 
				{
					event.getPlayer().getInventory().addItem(item);
				}
				else
				{
					event.getPlayer().getInventory().setItem(cfg.getInt("misc.gui-item.position"), item);
				}
			}
		}
	}
}
