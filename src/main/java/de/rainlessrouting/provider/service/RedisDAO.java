package de.rainlessrouting.provider.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.rainlessrouting.common.model.PrecipitationReadingGrid;
import de.rainlessrouting.common.model.PrecipitationReadingPoint;
import de.rainlessrouting.common.model.SimplePrecipitation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.GeoCoordinate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;

public class RedisDAO {

	private static final Logger log = LoggerFactory.getLogger(RedisDAO.class);

	public final static String REDIS_SERVER = Protocol.DEFAULT_HOST; // "192.168.3.100"; // Protocol.DEFAULT_HOST
	Jedis jedis;

	HashMap<byte[], GeoCoordinate> coordinateMap;

	public RedisDAO() {

		this.jedis = new Jedis(REDIS_SERVER, Protocol.DEFAULT_PORT, 30000);
		try
		{
			jedis.connect();
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
		}
		if (jedis.isConnected())
			log.debug("RedisDAO: connected to Redis DB @ " + REDIS_SERVER);
		else
			log.error("RedisDAO: Failed to connect to Redis DB @ " + REDIS_SERVER);
		this.coordinateMap = new HashMap<byte[], GeoCoordinate>();

	}

	// method used by getPrecipitationInfo
	public long[] getCurrentTimeArray() {

		byte[] output = null;

		output = jedis.get("CurrentTimeArray".getBytes());

		long[] timeArray = null;
		try {
			timeArray = (long[]) deserializeBytes(output);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return timeArray;
	}

	public PrecipitationReadingGrid getPrecipitationReadingGrid() 
	{
		if (!jedis.isConnected())
			return null;

		if(jedis.get("PrecipitationReadingGrid_LongGridSize") == null) {
			log.warn("Grid Size ist null!!");
			return new PrecipitationReadingGrid(0,0);
		}

		int longGridSize = Integer.parseInt(jedis.get("PrecipitationReadingGrid_LongGridSize"));
		int latGridSize = Integer.parseInt(jedis.get("PrecipitationReadingGrid_LatGridSize"));
		
		PrecipitationReadingGrid precGrid = new PrecipitationReadingGrid(longGridSize, latGridSize);
		
		try 
		{
			Set<byte[]> precipitationData = jedis.zrange("PrecipitationReadingPoint".getBytes(), 0, -1);
			Iterator<byte[]> iter = precipitationData.iterator();
			int i=0;

			while (iter.hasNext())
			{
				byte[] prec = iter.next();
				
				PrecipitationReadingPoint prp = (PrecipitationReadingPoint)deserializeBytes(prec);
				precGrid.addPrecipitationReadingPoint(i, prp);
				
				i++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return precGrid;
	}

	public List<SimplePrecipitation> getSimplePrecipitationsForTimePoint(int timepoint) {
		try {
			Set<byte[]> precipitationData = jedis.zrange((timepoint + "").getBytes(), 0, -1);

			List<SimplePrecipitation> precipitationList = new ArrayList<SimplePrecipitation>();

			for (byte[] precipitation : precipitationData) {
				Object o;
				try {
					o = deserializeBytes(precipitation);
					if (o instanceof SimplePrecipitation) {
						precipitationList.add((SimplePrecipitation) o);
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			return precipitationList;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	static Object deserializeBytes(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bytesIn);
		Object obj = ois.readObject();
		ois.close();
		return obj;
	}
}
