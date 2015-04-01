package net.auscraft.BlivTrails.utils;

import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.config.ConfigAccessor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Utilities {

  private BlivTrails instance;
  private final String prefix = "&f[&bBlivTrails&f] ";
  private String playerPrefix = "";
  private java.util.logging.Logger log = Bukkit.getLogger();
  private boolean debug = true;

  public Utilities(BlivTrails instance) {
    this.instance = instance;
  }

  //Used in Async methods where Bukkit is inaccessible.
  //Or when the plugin instance is inaccessible
  //Variables are assumed in this mode.
  public Utilities(boolean isAsync) {
    //Empty
  }

  //------------------------------------------------------------------------------------------------------
  //Extra Setup
  //------------------------------------------------------------------------------------------------------

  public void setConfig(ConfigAccessor cfg) {
    playerPrefix = translateColours(instance.getMessages().getString("messages.prefix"));
    if (playerPrefix != "") {
      playerPrefix += " ";
    }
    debug = cfg.getBoolean("misc.debug");
  }


  //------------------------------------------------------------------------------------------------------
  //String Translation
  //------------------------------------------------------------------------------------------------------

  public String stripColours(String toFix) {
    Pattern chatColorPattern = Pattern.compile("[&](.)");
    String fixedString = chatColorPattern.matcher(toFix).replaceAll("");
    return fixedString;
  }

  public String translateConsoleColours(String toFix) //TODO: Add additional colours
  {
    toFix = Pattern.compile("(?i)(&|§)([a])").matcher(toFix).replaceAll("\u001B[32m\u001B[1m"); //Light Green
    toFix = Pattern.compile("(?i)(&|§)([b])").matcher(toFix).replaceAll("\u001B[36m"); //Aqua
    toFix = Pattern.compile("(?i)(&|§)([c])").matcher(toFix).replaceAll("\u001B[31m"); //Red
    toFix = Pattern.compile("(?i)(&|§)([d])").matcher(toFix).replaceAll("\u001B[35m\u001B[1m"); //Pink
    toFix = Pattern.compile("(?i)(&|§)([e])").matcher(toFix).replaceAll("\u001B[33m\u001B[1m"); //Yellow
    toFix = Pattern.compile("(?i)(&|§)([f])").matcher(toFix).replaceAll("\u001B[0m"); //White
    toFix = Pattern.compile("(?i)(&|§)([0])").matcher(toFix).replaceAll("\u001B[30m"); //Black
    toFix = Pattern.compile("(?i)(&|§)([1])").matcher(toFix).replaceAll("\u001B[34m"); //Dark Blue
    toFix = Pattern.compile("(?i)(&|§)([2])").matcher(toFix).replaceAll("\u001B[32m"); //Dark Green
    toFix = Pattern.compile("(?i)(&|§)([3])").matcher(toFix).replaceAll("\u001B[34m\u001B[1m"); //Light Blue
    toFix = Pattern.compile("(?i)(&|§)([4])").matcher(toFix).replaceAll("\u001B[31m"); //Dark Red
    toFix = Pattern.compile("(?i)(&|§)([5])").matcher(toFix).replaceAll("\u001B[35m"); //Purple
    toFix = Pattern.compile("(?i)(&|§)([6])").matcher(toFix).replaceAll("\u001B[33m"); //Gold
    toFix = Pattern.compile("(?i)(&|§)([7])").matcher(toFix).replaceAll("\u001B[37m"); //Light Grey
    toFix = Pattern.compile("(?i)(&|§)([8])").matcher(toFix).replaceAll("\u001B[30m\u001B[1m"); //Dark Grey
    toFix = Pattern.compile("(?i)(&|§)([9])").matcher(toFix).replaceAll("\u001B[34m"); //Dark Aqua
    toFix = Pattern.compile("(?i)(&|§)([r])").matcher(toFix).replaceAll("\u001B[0m");
    toFix += "\u001B[0m"; //Stop colour from overflowing to the next line with a reset code

    return toFix;
  }

  public String translateColours(String toFix) {
    //Convert every single colour code and formatting code, excluding 'magic' (&k), capitals and lowercase are converted.
    Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-Fa-f-l-oL-OrR])");
    String fixedString = chatColorPattern.matcher(toFix).replaceAll("\u00A7$1");
    return fixedString;
  }

  public List<String> translateColours(List<?> lines) {
    try {
      String[] lineString = null;
      if (lines.size() > 0) {
        lineString = lines.toArray(new String[lines.size()]);
      } else {
        return null;
      }
      for (int i = 0; i < lines.size(); i++) {
        Pattern chatColourPattern = Pattern.compile("(?i)&([0-9A-Fa-fk-oK-OrR])");
        lineString[i] = chatColourPattern.matcher(lineString[i]).replaceAll("\u00A7$1");
      }
      return Arrays.asList(lineString);
    } catch (NullPointerException e) {
      return null;
    }

  }

  //------------------------------------------------------------------------------------------------------
  //Printing
  //------------------------------------------------------------------------------------------------------

  public void printSuccess(CommandSender sender, String message) {
    sender.sendMessage(playerPrefix + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "SUCCESS: " + ChatColor.GREEN + translateColours(message));
  }

  public void printPlain(CommandSender sender, String message) {
    sender.sendMessage(playerPrefix + translateColours(message));
  }

  public void printInfo(CommandSender sender, String message) {
    sender.sendMessage(playerPrefix + ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "INFO: " + ChatColor.BLUE + translateColours(message));
  }

  public void printError(CommandSender sender, String message) {
    sender.sendMessage(playerPrefix + ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "ERROR: " + ChatColor.RED + translateColours(message));
  }

  //------------------------------------------------------------------------------------------------------
  //Broadcasting
  //------------------------------------------------------------------------------------------------------

  public void broadcastPlain(String message) {
    Bukkit.broadcastMessage(message);
  }


  //------------------------------------------------------------------------------------------------------
  //Logging
  //------------------------------------------------------------------------------------------------------

  public void logSuccess(String message) {
    log.log(Level.INFO, translateConsoleColours(prefix + "&2SUCCESS: &a" + message));
  }

  public void logPlain(String message) {
    log.log(Level.INFO, translateConsoleColours(prefix + message));
  }

  public void logInfo(String message) {
    log.log(Level.INFO, translateConsoleColours(prefix + "&9INFO: &b" + message));
  }

  public void logError(String message) {
    log.log(Level.WARNING, translateConsoleColours(prefix + "&4ERROR: &c" + message));
  }

  public void logDebug(String message) {
    if (debug) {
      log.log(Level.INFO, translateConsoleColours(prefix + "&2DEBUG: &a" + message));
    }
  }

  public void logSevere(String message) {
    log.log(Level.SEVERE, translateConsoleColours(prefix + "&4SEVERE: &c" + message));
  }

  //------------------------------------------------------------------------------------------------------
  //Miscellaneous
  //------------------------------------------------------------------------------------------------------

  public BlivTrails getInstance() {
    return instance;
  }

  public String trailConfigName(String particleString) {
    switch (particleString) {
      case "BARRIER":
        particleString = "barrier";
        break;
      case "CLOUD":
        particleString = "cloud";
        break;
      case "CRIT":
        particleString = "criticals";
        break;
      case "CRIT_MAGIC":
        particleString = "criticals-magic";
        break;
      case "DRIP_LAVA":
        particleString = "drip-lava";
        break;
      case "DRIP_WATER":
        particleString = "drip-water";
        break;
      case "ENCHANTMENT_TABLE":
        particleString = "enchant";
        break;
      case "EXPLOSION_NORMAL":
        particleString = "explosion-smoke";
        break;
      case "FIREWORKS_SPARK":
        particleString = "firework";
        break;
      case "FLAME":
        particleString = "flame";
        break;
      case "HEART":
        particleString = "hearts";
        break;
      case "LAVA":
        particleString = "lava";
        break;
      case "NOTE":
        particleString = "note";
        break;
      case "PORTAL":
        particleString = "portal";
        break;
      case "REDSTONE":
        particleString = "redstone";
        break;
      case "SLIME":
        particleString = "slime";
        break;
      case "SMOKE_LARGE":
        particleString = "smoke";
        break;
      case "SNOW_SHOVEL":
        particleString = "snow-shovel";
        break;
      case "SNOWBALL":
        particleString = "snow-ball";
        break;
      case "SPELL":
        particleString = "spell";
        break;
      case "SPELL_INSTANT":
        particleString = "spell-instant";
        break;
      case "SPELL_MOB":
        particleString = "spell-mob";
        break;
      case "SPELL_WITCH":
        particleString = "spell-witch";
        break;
      case "VILLAGER_ANGRY":
        particleString = "angry-villager";
        break;
      case "VILLAGER_HAPPY":
        particleString = "happy-villager";
        break;
      case "TOWN_AURA":
        particleString = "town-aura";
        break;
      case "WATER_DROP":
        particleString = "water-drop";
        break;
      case "WATER_SPLASH":
        particleString = "water-splash";
        break;
      default:
        particleString = "NULL";
        break;
    }
    return particleString;
  }
}
