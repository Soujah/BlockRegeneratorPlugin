package me.joshuaemq.blockregenerator.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import org.bukkit.command.CommandSender;
import se.ranzdo.bukkit.methodcommand.Command;

public class BaseCommand {

  private BlockRegeneratorPlugin plugin;

  public BaseCommand(BlockRegeneratorPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(identifier = "blockregen reload", permissions = "blockregen.command.reload", onlyPlayers = false)
  public void reloadCommand(CommandSender sender) {
    plugin.onEnable();
    plugin.onDisable();
    sendMessage(sender, "&aBlockRegenerator Reloaded!");
  }
}
