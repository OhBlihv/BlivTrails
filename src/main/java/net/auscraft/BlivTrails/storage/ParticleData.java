package net.auscraft.BlivTrails.storage;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import net.auscraft.BlivTrails.storage.mysql.ByteArray;
import net.auscraft.BlivTrails.utils.UUIDUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

@DatabaseTable(tableName = "particle_trails", daoClass = ParticleStorage.class)
public class ParticleData {

  @DatabaseField(id = true, persisterClass = ByteArray.class, columnDefinition = "BINARY(16) NOT NULL")
  @Getter
  private byte[] id;

  @DatabaseField(width = 50, columnDefinition = "VARCHAR(50) NOT NULL")
  @Getter
  @Setter
  private String particle;

  @DatabaseField(columnDefinition = "INT(11) NOT NULL")
  @Getter
  @Setter
  private int type;

  @DatabaseField(columnDefinition = "INT(11) NOT NULL")
  @Getter
  @Setter
  private int length;

  @DatabaseField(columnDefinition = "INT(11) NOT NULL")
  @Getter
  @Setter
  private int height;

  @DatabaseField(columnDefinition = "INT(11) NOT NULL")
  @Getter
  @Setter
  private int colour;

  private UUID uuid = null;

  ParticleData() {
  }

  public ParticleData(Player player) {
    id = UUIDUtils.toBytes(player.getUniqueId());
    uuid = player.getUniqueId();
  }

  public ParticleData(byte[] uuidBytes, String particle, int length, int height, int colour) {
    this.id= uuidBytes;
    this.particle = particle;
    this.length = length;
    this.height = height;
    this.colour = colour;
  }

  public UUID getUUID() {
    if (uuid == null) {
      uuid = UUIDUtils.fromBytes(id);
    }
    return uuid;
  }
}
