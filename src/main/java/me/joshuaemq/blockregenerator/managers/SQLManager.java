package me.joshuaemq.blockregenerator.managers;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import me.joshuaemq.blockregenerator.objects.BlockData;
import org.intellij.lang.annotations.Language;

public class SQLManager {

  // "CREATE TABLE IF NOT EXISTS `ms_blocks` (`ID` INT NOT NULL AUTO_INCREMENT, `material`
  // varchar(100), `respawntime` LONG, `x` INT, `y` INT, `z` INT, `world` VARCHAR(255), PRIMARY KEY
  // (`ID`))";

  @Language("MySQL")
  private static final String CREATE_TABLE =
      "CREATE TABLE IF NOT EXISTS br_regenBlocks (ID INT NOT NULL AUTO_INCREMENT PRIMARY KEY, material VARCHAR(100), respawntime BIGINT(20), x INT(11), y INT(11), z INT(11), world VARCHAR(255))";

  @Language("MySQL")
  private static final String GET_ALL_QUERY = "SELECT * FROM br_regenBlocks";

  @Language("MySQL")
  private static final String REMOVE_QUERY = "DELETE FROM br_regenBlocks WHERE ID = ?";

  @Language("MySQL")
  private static final String ADD_QUERY =
      "INSERT INTO br_regenBlocks (material, respawntime, x, y, z, world) VALUES (?,?,?,?,?,?)";

  public void initialize() {
    try {
      DB.executeUpdate(CREATE_TABLE);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void removeItem(int id) {
    try {
      DB.executeUpdateAsync(REMOVE_QUERY, id);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public int insertBlock(String material, int x, int y, int z, String world, long respawnTime) {
    try {
      return DB.executeUpdateAsync(ADD_QUERY, material, respawnTime, x, y, z, world).get();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  public List<BlockData> getRespawnBlocks() {
    List<BlockData> list = new ArrayList<>();
    try {
      CompletableFuture<List<DbRow>> rs1 = DB.getResultsAsync(GET_ALL_QUERY);
      long respawnTime;
      for (DbRow row : rs1.get()) {
        respawnTime = row.getLong("respawntime");
        if (System.currentTimeMillis() < respawnTime) {
          continue;
        }
        BlockData blockData = new BlockData();
        blockData.setId(row.getInt("ID"));
        blockData.setMaterial(row.getString("material"));
        blockData.setWorld(row.getString("world"));
        blockData.setX(row.getInt("x"));
        blockData.setY(row.getInt("y"));
        blockData.setZ(row.getInt("z"));
        list.add(blockData);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }

  public List<BlockData> getAllDespawnedBlocks() {
    List<BlockData> list = new ArrayList<>();
    try {
      CompletableFuture<List<DbRow>> rs1 = DB.getResultsAsync(GET_ALL_QUERY);
      for (DbRow row : rs1.get()) {
        BlockData blockData = new BlockData();
        blockData.setId(row.getInt("ID"));
        blockData.setMaterial(row.getString("material"));
        blockData.setWorld(row.getString("world"));
        blockData.setX(row.getInt("x"));
        blockData.setY(row.getInt("y"));
        blockData.setZ(row.getInt("z"));
        list.add(blockData);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }
}
