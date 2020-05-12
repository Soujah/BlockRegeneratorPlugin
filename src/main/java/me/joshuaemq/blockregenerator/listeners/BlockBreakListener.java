package me.joshuaemq.blockregenerator.listeners;

import static com.sk89q.worldedit.math.BlockVector3.at;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.StringMatcher;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.Random;
import me.joshuaemq.blockregenerator.BlockRegeneratorPlugin;
import me.joshuaemq.blockregenerator.events.RegenOreMinedEvent;
import me.joshuaemq.blockregenerator.objects.MineReward;
import me.joshuaemq.blockregenerator.objects.RegenBlock;
import me.joshuaemq.blockregenerator.strife.StrifeAdapter;
import org.bukkit.Bukkit;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BlockBreakListener implements Listener {

  private final BlockRegeneratorPlugin plugin;
  private final Random random;

  private RegionContainer regionContainer =
      WorldGuard.getInstance().getPlatform().getRegionContainer();
  private StringMatcher stringMatcher = WorldGuard.getInstance().getPlatform().getMatcher();

  public BlockBreakListener(BlockRegeneratorPlugin plugin) {
    this.plugin = plugin;
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
    if (event.getBlock().getType() == Material.NETHER_QUARTZ_ORE) {
      strifeMiningExp = (double) event.getExpToDrop() * 0.29;
    } else {
      strifeMiningExp = (double) event.getExpToDrop() * 0.38;
    }
    if (strifeMiningExp <= 0) {
      return;
    }
    StrifeAdapter.getAdapter().addMiningExperience(event.getPlayer(), strifeMiningExp, false);
    event.setExpToDrop(0);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSpecialBlockBreak(BlockBreakEvent event) {
    if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
      return;
    }
    Player player = event.getPlayer();
    Block brokenBlock = event.getBlock();

    BlockVector3 vectorLoc = at(brokenBlock.getX(), brokenBlock.getY(), brokenBlock.getZ());
    World world = stringMatcher.getWorldByName(brokenBlock.getWorld().getName());
    RegionManager manager = regionContainer.get(world);
    ApplicableRegionSet regions = manager.getApplicableRegions(vectorLoc);

    if (regions.getRegions().size() == 0) {
      return;
    }

    ProtectedRegion region = null;
    int priority = -9999;
    for (ProtectedRegion p : regions.getRegions()) {
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

    player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, level));

    int miningLevel = StrifeAdapter.getAdapter().getMiningSkillLevel(player);
    double effectiveMiningLevel = StrifeAdapter.getAdapter().getEffectiveMiningSkillLevel(player);
    double bonusSuccess = plugin.getSettings().getDouble("config.bonus-success-per-level", 0);
    double lootChanceMultiplier = 1 + (effectiveMiningLevel * bonusSuccess);

    if (random.nextDouble() * lootChanceMultiplier < 1D - regenBlock.getLootChance()) {
      return;
    }

    String reward = RegenBlock.getRandomReward(regenBlock);
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

    if (player.hasPermission("blockregen.inspect")) {
      MessageUtils.sendMessage(player, "BlockRegen Details:");
      MessageUtils.sendMessage(player, " OreID: " + regenBlock.getId());
      MessageUtils.sendMessage(player, " RewardID: " + reward);
      MessageUtils.sendMessage(player, " RegionID: " + region.getId());
      MessageUtils.sendMessage(player, " Depleting: " + depleteOre);
    }

    RegenOreMinedEvent oreEvent =
        new RegenOreMinedEvent(player, reward, regenBlock.getId(), region.getId(), blockMaterial);
    Bukkit.getPluginManager().callEvent(oreEvent);

    if (oreEvent.isCancelled()) {
      return;
    }

    if (depleteOre) {
      plugin
          .getBlockManager()
          .insertBlock(
              blockMaterial.toString(),
              brokenBlock.getX(),
              brokenBlock.getY(),
              brokenBlock.getZ(),
              brokenBlock.getWorld().getName(),
              System.currentTimeMillis() + regenBlock.getRespawnTime(),
              regenBlock.getDepleteMaterial(),
              brokenBlock.getLocation());
    }

    StrifeAdapter.getAdapter().addMiningExperience(player, mineReward.getExperience(), false);
    ItemStack minedItem = mineReward.getItemStack().clone();
    minedItem.setAmount(
        getAdjustedDropAmount(
            player.getEquipment().getItemInMainHand(), mineReward, effectiveMiningLevel));
    Location dropLocation = brokenBlock.getLocation().clone();

    // Offsets the spawn location based on the player's location to stop items from becoming
    // trapped in blocks in rare cases
    dropLocation.add(player.getEyeLocation().getDirection().multiply(-0.85f));
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
