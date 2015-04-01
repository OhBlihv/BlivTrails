package net.auscraft.BlivTrails.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;

import java.sql.SQLException;

public class ParticleStorage extends BaseDaoImpl<ParticleData, byte[]> {

  public ParticleStorage(ConnectionSource connectionSource) throws SQLException {
    super(connectionSource, ParticleData.class);
  }
}
