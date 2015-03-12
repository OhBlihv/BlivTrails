package net.auscraft.BlivTrails.runnables;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.auscraft.BlivTrails.PlayerConfig;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.jolbox.bonecp.BoneCPDataSource;

public class MySQLRunnable implements Runnable
{
	
	private BoneCPDataSource sql;
	private String uuid;
	private PlayerConfig pcfg;
	private ConcurrentHashMap<String, PlayerConfig> trailMap = null;
	
	//Control flag to determine if the runnable is saving or loading
	short process;
	
	public MySQLRunnable(BoneCPDataSource sql, String uuid, PlayerConfig pcfg, short process, ConcurrentHashMap<String, PlayerConfig> trailMap)
	{
		this.sql = sql;
		this.uuid = uuid;
		this.process = process;
		if(process == 0) //Seperate save/load sources
		{
			this.pcfg = pcfg;
		}
		else if(process == 1)
		{
			this.trailMap = trailMap;
		}
		else
		{
			this.pcfg = pcfg;
			this.trailMap = trailMap;
		}
	}
	
	public void run()
	{
		Connection conn = null;
		try
		{
			if(process == 0)
			{
				conn = sql.getConnection();
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT uuid FROM bliv_trails WHERE uuid='" + uuid + "';");
				if(rs.next())
				{
					st.execute("UPDATE bliv_trails SET particle='" + pcfg.getParticle().toString() + "', type='"
							+ pcfg.getType() + "', length='" + pcfg.getLength() + "', height='" + pcfg.getHeight() + "', colour='" + pcfg.getColour() + "' WHERE uuid='" + uuid + "';");
				}
				else
				{
					st.execute("INSERT INTO bliv_trails(uuid,particle,type,length,height,colour) VALUES('" + uuid + "', '" + pcfg.getParticle().toString() + "','"
							+ pcfg.getType() + "','" + pcfg.getLength() + "','" + pcfg.getHeight() + "', '" + pcfg.getColour() + "');");
				}
				conn.close();
			}
			else if (process == 1)
			{
				conn = sql.getConnection();
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery("SELECT * FROM bliv_trails WHERE uuid='" + uuid + "';");
				if(rs.next())
				{
					ParticleEffect particleEff = null;
					for(ParticleEffect pEff : ParticleEffect.values())
					{
						if(pEff.toString().equals(rs.getString("particle")))
						{
							if(pEff.equals(ParticleEffect.FOOTSTEP))
							{
								return;
							}
							particleEff = pEff;
							break;
						}
					}
					trailMap.put(uuid, new PlayerConfig(uuid, particleEff,
							rs.getInt("type"), rs.getInt("length"), rs.getInt("height"), rs.getInt("colour")));
				}
				conn.close();
			}
			else
			{
				conn = sql.getConnection();
				Statement st = conn.createStatement();
				trailMap.put(uuid.toString(), new PlayerConfig(uuid, ParticleEffect.FOOTSTEP, 0, 0, 0, 0));
				st.execute("DELETE FROM bliv_trails WHERE uuid='" + uuid + "';");
				conn.close();
				new CallablePrintout(UUID.fromString(uuid), "messages.generic.force-remove-receive");
			}
			
		}
		catch(SQLException e2)
		{
			try
			{
				conn.close();
			}
			catch(SQLException e3)
			{
				//Give up
				e2.printStackTrace();
				e3.printStackTrace();
			}
		}
	}
}
