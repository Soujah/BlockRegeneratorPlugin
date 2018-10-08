package me.joshuaemq.blockregenerator.listeners;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import info.faceland.strife.util.PlayerDataUtil;
import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.objects.MineReward;
import me.joshuaemq.blockregenerator.objects.RegenBlock;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import java.util.Random;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class BlockBreakListener implements Listener {

  private final BlockRegeneratorPlugin plugin;
  private final WorldGuardPlugin worldGuardPlugin;
  private final Random random;

  public BlockBreakListener(BlockRegeneratorPlugin plugin) {
    this.plugin = plugin;
    this.worldGuardPlugin = WGBukkit.getPlugin();
    this.random = new Random();
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onNormieBlockBreak(BlockBreakEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
      return;
    }
    double strifeMiningExp;
    if (event.getBlock().getType() == Material.QUARTZ_ORE) {
      strifeMiningExp = (double) event.getExpToDrop() * 0.29;
    } else {
      strifeMiningExp = (double) event.getExpToDrop() * 0.38;
    }
    if (strifeMiningExp <= 0) {
      return;
    }
    plugin.getStrifePlugin().getMiningExperienceManager().addExperience(
        event.getPlayer(), strifeMiningExp, false);
    event.setExpToDrop(0);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSpecialBlockBreak(BlockBreakEvent event) {
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
      return;
    }
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

    RegenBlock regenBlock = plugin.getBlockManager().getBlock(region.getId(), blockMaterial);
    if (regenBlock == null) {
      return;
    }

    event.setCancelled(true);

    int duration = plugin.getSettings().getInt("config.mine-fatigue-duration");
    int level = plugin.getSettings().getInt("config.mine-fatigue-intensity", 1) - 1;
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, level));

    int miningLevel = PlayerDataUtil.getMiningLevel(player);
    int effectiveMiningLevel = PlayerDataUtil.getMineSkill(player, true);
    double lootChanceMultiplier = 1 + ((double) effectiveMiningLevel / 30);

    double lootDropChance = regenBlock.getLootChance();
    if (lootDropChance * lootChanceMultiplier < random.nextDouble()) {
      return;
    }

    String reward = regenBlock.getRandomReward();
    MineReward mineReward = plugin.getMineRewardManager().getReward(reward);
    if (mineReward.getLevelRequirement() > miningLevel) {
      return;
    }

    boolean depleteOre = true;
    if (player.hasPotionEffect(PotionEffectType.LUCK)) {
      double luckyMine = plugin.getSettings().getDouble("config.luck-no-deplete-chance", 0.2);
      if (random.nextDouble() < luckyMine) {
        depleteOre = false;
      }
    }
    if (random.nextDouble() > regenBlock.getExhaustChance()) {
      depleteOre = false;
    }

    if (depleteOre) {
      plugin.getBlockManager().insertBlock(
          blockMaterial.toString(),
          brokenBlock.getX(),
          brokenBlock.getY(),
          brokenBlock.getZ(),
          brokenBlock.getWorld().getName(),
          System.currentTimeMillis() + regenBlock.getRespawnTime(),
          regenBlock.getDepleteMaterial(),
          brokenBlock.getLocation()
      );
    }

    plugin.getStrifePlugin().getMiningExperienceManager().addExperience(player,
        mineReward.getExperience(), false);
    ItemStack minedItem = mineReward.getItemStack().clone();
    minedItem.setAmount(getAdjustedDropAmount(
        player.getEquipment().getItemInMainHand(), mineReward, effectiveMiningLevel));
    Location dropLocation = brokenBlock.getLocation().clone().add(0.5, 0.5, 0.5);

    // Offsets the spawn location based on the player's location to stop items from becoming
    // trapped in blocks in rare cases
    Vector playerDirectional = dropLocation.clone().subtract(player.getLocation()).toVector();
    dropLocation.add(playerDirectional.normalize().multiply(-0.85f));

    brokenBlock.getWorld().dropItemNaturally(dropLocation, minedItem);
  }

  private int getAdjustedDropAmount(ItemStack tool, MineReward mineReward, double effectiveLevel) {
    double levelBonus = (effectiveLevel - mineReward.getLevelRequirement()) / 10;
    double fortuneBonus = 0;
    if (tool != null && tool.getEnchantments().getOrDefault(Enchantment.LOOT_BONUS_BLOCKS, 0) > 0) {
      fortuneBonus = tool.getEnchantments().get(Enchantment.LOOT_BONUS_BLOCKS) * 0.5;
    }
    double dropBonus = random.nextDouble() * (levelBonus + fortuneBonus);

    return mineReward.getItemStack().getAmount() * (int) (1 + dropBonus);
  }
}