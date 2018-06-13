package me.joshuaemq.blockregenerator.listeners;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.objects.BlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Random;

public class BlockBreakListener implements Listener {

  private final BlockRegeneratorPlugin plugin;
  private final WorldGuardPlugin worldGuardPlugin;
  private final Random random;

  public BlockBreakListener(BlockRegeneratorPlugin plugin) {
    this.plugin = plugin;
    this.worldGuardPlugin = WGBukkit.getPlugin();
    this.random = new Random();
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    Block brokenBlock = event.getBlock();

    ApplicableRegionSet regionSet =
        worldGuardPlugin.getRegionManager(player.getWorld())
            .getApplicableRegions(brokenBlock.getLocation());

    if (regionSet.size() == 0) {
      return;
    }

    ProtectedRegion region = null;
    int priority = -9999;
    for (ProtectedRegion p : regionSet.getRegions()) {
      if (p.getPriority() > priority) {
        region = p;
      }
    }
    if (region == null || !plugin.getBlockManager().containsRegion(region.getId())) {
      return;
    }

    Material blockMaterial = event.getBlock().getType();

    if (!plugin.getBlockManager().getValidMaterials(region.getId()).contains(blockMaterial)) {
      return;
    }

    BlockData blockData = plugin.getBlockManager().getBlock(region.getId(), blockMaterial);
    if (blockData == null) {
      return;
    }

    event.setCancelled(true);

    double lootDropChance = blockData.getLootChance();
    if (lootDropChance >= random.nextDouble()) {
      sendMessage(player, "&aYou mined a block!");
      String reward = blockData.getRandomReward();
      ItemStack minedReward = plugin.getMineRewardManager().getReward(reward).getItemStack();
      Location dropLocation = brokenBlock.getLocation().clone().add(0.5, 0.5, 0.5);
      brokenBlock.getWorld().dropItemNaturally(dropLocation, minedReward);

      if (blockData.getExhaustChance() >= random.nextDouble()) {
        brokenBlock.getLocation().getBlock().setType(blockData.getDepleteMaterial());

        int respawnDelay = blockData.getRespawnTime();
        int x = brokenBlock.getX();
        int y = brokenBlock.getY();
        int z = brokenBlock.getZ();
        long currentTime = System.currentTimeMillis();

        plugin.getSQLManager().insertBlock(blockMaterial.toString(), x, y, z,
            brokenBlock.getWorld().getName(), currentTime + respawnDelay);
      }
    }
  }
}