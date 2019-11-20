package de.rainlessrouting.provider.service;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.rainlessrouting.common.model.PrecipitationReadingGrid;
import de.rainlessrouting.common.model.SimplePrecipitation;
import de.rainlessrouting.provider.model.PrecipitationInfo;

@Service
public class PrecipitationDataProviderServiceImpl implements IPrecipitationDataProviderService {
	private static final Logger log = LoggerFactory.getLogger(PrecipitationDataProviderServiceImpl.class);

	private PrecipitationReadingGrid precipitationGrid;
	
	public RedisDAO redis;
	
	public PrecipitationDataProviderServiceImpl() {
		
		log.debug("PrecipitationDataProviderServiceImpl(): instantiate RedisDAO and read values ...");
		this.redis = new RedisDAO();
		
       precipitationGrid = redis.getPrecipitationReadingGrid();
       
       if (precipitationGrid != null)
    	   log.debug("PrecipitationDataProviderServiceImpl(): precipitationGrid.size() = " + precipitationGrid.getLongGridSize()*precipitationGrid.getLatGridSize());
	}
	
	public static Object deserializeBytes(byte[] bytes) throws IOException, ClassNotFoundException
	{
	   ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytes);
	   ObjectInputStream ois = new ObjectInputStream(bytesIn);
	   Object obj = ois.readObject();
	   ois.close();
	   return obj;
	}

	public PrecipitationInfo getPrecipitationInfo() {
		
		return new PrecipitationInfo(this.redis.getCurrentTimeArray());
	}
	
	public PrecipitationReadingGrid getPrecipitationReadingGrid() {
	
		return precipitationGrid;
	}

	@Override
	public List<SimplePrecipitation> getSimplePrecipitationsForTimePoint(int timePoint) {
		
		return this.redis.getSimplePrecipitationsForTimePoint(timePoint);
	}

}
