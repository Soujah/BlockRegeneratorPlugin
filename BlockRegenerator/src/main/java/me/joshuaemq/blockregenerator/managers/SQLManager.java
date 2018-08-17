package me.joshuaemq.blockregenerator.managers;

import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;
import me.joshuaemq.blockregenerator.objects.BlockData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class SQLManager {

  private final String host;
  private final String port;
  private final String database;
  private final String user;
  private final String pass;

  private Connection connection;

  private static PreparedStatement initStatement;
  private static PreparedStatement addStatement;
  private static PreparedStatement removeStatement;
  private static PreparedStatement checkStatement;

  private static final String initString =
      "CREATE TABLE IF NOT EXISTS `ms_blocks` (`ID` INT NOT NULL AUTO_INCREMENT, `material` varchar(100), `respawntime` LONG, `x` INT, `y` INT, `z` INT, `world` VARCHAR(255), PRIMARY KEY (`ID`)) ;";
  private static final String addString =
      "INSERT INTO `ms_blocks` (`material`, `respawntime`, `x`, `y`, `z`, `world`) VALUES (?,?,?,?,?,?);";
  private static final String removeString =
      "DELETE FROM `ms_blocks` WHERE `ID`=?;";
  private static final String checkString =
      "SELECT * FROM `ms_blocks`;";

  public SQLManager(VersionedSmartYamlConfiguration configYML) {
    this.host = configYML.getString("MySQL.host");
    this.port = configYML.getString("MySQL.port");
    this.database = configYML.getString("MySQL.database");
    this.user = configYML.getString("MySQL.user");
    this.pass = configYML.getString("MySQL.password");
    openConnection();
    initDatabase();
  }

  private void buildStatements() {
    try {
      initStatement = connection.prepareStatement(initString);
      addStatement = connection.prepareStatement(addString);
      removeStatement = connection.prepareStatement(removeString);
      checkStatement = connection.prepareStatement(checkString);
    } catch (Exception ex) {
      Bukkit.getLogger().severe("SQL FAILED TO BUILD STATEMENTS ABORT MISSION " + ex.toString());
    }
  }

  private void openConnection() {
    try {
      if (connection != null && !connection.isClosed()) {
        return;
      }
      this.connection = DriverManager
          .getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, user, pass);
      buildStatements();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void closeConnection() {
    try {
      this.connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initDatabase() {
    this.openConnection();
    try {
      initStatement.execute();
      initStatement.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void removeItem(int id) {
    try {
      removeStatement.setInt(1, id);
      removeStatement.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void insertBlock(String material, int x, int y, int z, String world, long respawntime) {
    openConnection();
    try {
      addStatement.setString(1, material);
      addStatement.setLong(2, respawntime);
      addStatement.setInt(3, x);
      addStatement.setInt(4, y);
      addStatement.setInt(5, z);
      addStatement.setString(6, world);
      addStatement.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<BlockData> getRespawnBlocks() {
    openConnection();
    List<BlockData> list = new ArrayList<>();
    try {
      ResultSet rs1 = checkStatement.executeQuery();
      while (rs1.next()) {
        long respawnTime = rs1.getLong("respawntime");
        if (System.currentTimeMillis() < respawnTime) {
          continue;
        }
        BlockData blockData = new BlockData();
        blockData.setId(rs1.getInt("id"));
        blockData.setMaterial(rs1.getString("material"));
        blockData.setWorld(rs1.getString("world"));
        blockData.setX(rs1.getInt("x"));
        blockData.setY(rs1.getInt("y"));
        blockData.setZ(rs1.getInt("z"));
        list.add(blockData);
      }
      rs1.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return list;
  }
}