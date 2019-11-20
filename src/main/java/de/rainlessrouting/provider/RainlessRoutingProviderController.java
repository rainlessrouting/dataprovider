package de.rainlessrouting.provider;


import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.rainlessrouting.common.coder.CoderFactory;
import de.rainlessrouting.common.db.DBGridInfo;
import de.rainlessrouting.common.db.DBValueGrid;
import de.rainlessrouting.common.db.IDatabaseHandler;
import de.rainlessrouting.common.db.SqliteHandler;
import de.rainlessrouting.common.model.Message;
import de.rainlessrouting.common.model.MessagePayload;
import de.rainlessrouting.common.util.DateTimeFormatter;

@Controller
public class RainlessRoutingProviderController {
	private static final Logger log = LoggerFactory.getLogger(RainlessRoutingProviderController.class);

	private IDatabaseHandler dbHandler;
	
	public RainlessRoutingProviderController()
	{
		dbHandler = new SqliteHandler();
		try {
			dbHandler.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@GetMapping("/gridinfo")
    public @ResponseBody Message getGridInfo(@RequestParam(value="gridid", defaultValue="live") String gridId, @RequestParam(value="encoding", defaultValue=CoderFactory.JSON) String encoding) {
		
		Message message;
		
		try
		{
			DBGridInfo gridInfo = dbHandler.loadGridInfo(gridId);
		
			// todo if (encoding.equals(""))
			// byte[] cbor = CborCoder.encode(list);
			// byte[] zip = CompressionCoder.compress(cbor);
			// String s = Base64Coder.encode(cbor);

			if (!encoding.equalsIgnoreCase(CoderFactory.JSON))
				throw new RuntimeException("Encoding '" + encoding + "' not supported. Please use one of CoderFactory's encodings!");
			
			if (gridInfo == null)
				message = new Message(false, "No grid with gridId '" + gridId + "' found");
			else
			{
				String payload = CoderFactory.encode(gridInfo, CoderFactory.JSON);
				MessagePayload msgPayload = new MessagePayload(payload, CoderFactory.JSON);
				message = new Message(true, msgPayload);
			}
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
			message = new Message(false, exc.getMessage());
		}
		
		return message;
    }
	
	/**
	 * Get the most recent time and corresponding offsets.
	 * A real measurement is represented by a timestamp with offset 0.
	 * When asking for times, one gets the latest timestamp with all offsets + all timestamps of the last n hours with their corresponding offset=0 (=real measurement) 
	 * @param gridId
	 * @param from
	 * @param encoding
	 * @return
	 */
	@GetMapping("/times")
    public @ResponseBody Message getTimes(
    		@RequestParam(value="gridid", defaultValue="live") String gridId, 
    		@RequestParam(value="from", defaultValue="0") String from, 
    		@RequestParam(value="hours", defaultValue="72") String hoursStr,
    		@RequestParam(value="max", defaultValue="100") String maxStr,
    		@RequestParam(value="encoding", defaultValue=CoderFactory.JSON) String encoding) {
		
		long fromTimestamp = Long.parseLong(from);
		int hours = Integer.parseInt(hoursStr);
		int max = Integer.parseInt(maxStr);
		
		Message message;
		
		try
		{
			if (gridId.equals("replay")) // when replaying historical scenes, timestamp and hoursBack do not matter.
			{
				hours = Integer.MAX_VALUE;
				fromTimestamp = 0;
			}
			
			Map<Long, List<Long>> timesAscending = dbHandler.loadTimes(gridId, fromTimestamp, hours);
			
			// apply max and filter out older values...
			Map<Long, List<Long>> times = new TreeMap<>(Collections.reverseOrder()); // create a new sorted set with new values first
			times.putAll(timesAscending);
			
			int counter = 0;
			Iterator<Long> iter = times.keySet().iterator();
			while (iter.hasNext())
			{
				long timestamp = iter.next();
				counter += times.get(timestamp).size();
				if (counter > max)
					iter.remove();
			}
			
			if (!encoding.equalsIgnoreCase(CoderFactory.JSON))
				throw new RuntimeException("Encoding '" + encoding + "' not supported. Please use one of CoderFactory's encodings!");
			
			if ((times == null) || (times.size() == 0))
				message = new Message(false, "No times with gridId '" + gridId + "' and timestamp 'from=" + DateTimeFormatter.getDateTime(fromTimestamp) + "'(long=" + fromTimestamp + ") found");
			else
			{
				String payload = CoderFactory.encode(times, CoderFactory.JSON);
				MessagePayload msgPayload = new MessagePayload(payload, CoderFactory.JSON);
				message = new Message(true, msgPayload);
				
				log.debug("RainlessRoutingProviderController.getTimes: reply with {} times (max={})", times.size(), max);
			}
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
			message = new Message(false, exc.getMessage());
		}
		
		return message;
    }
	
	@GetMapping("/values")
    public @ResponseBody Message getValues(
    		@RequestParam(value="gridid", defaultValue="live") String gridId, 
    		@RequestParam(value="timestamp", defaultValue="0") String timestampStr, 
    		@RequestParam(value="offset", defaultValue="0") String offsetStr, 
    		@RequestParam(value="encoding", defaultValue=CoderFactory.JSON) String encoding) {
		
		long timestamp = Long.parseLong(timestampStr);
		long offset = Long.parseLong(offsetStr);
		
		Message message;
		
		try
		{
			DBValueGrid valueGrid = dbHandler.loadValueGrid(gridId, timestamp, offset);
			
			// log.debug("RainlessRoutingProviderController: dbValueGrid=" + valueGrid.toFullString());
			// todo if (encoding.equals(""))
			// byte[] cbor = CborCoder.encode(list);
			// byte[] zip = CompressionCoder.compress(cbor);
			// String s = Base64Coder.encode(cbor);

			if (!encoding.equalsIgnoreCase(CoderFactory.JSON))
				throw new RuntimeException("Encoding '" + encoding + "' not supported. Please use one of CoderFactory's encodings!");
			
			if (valueGrid == null)
				throw new RuntimeException("No grid with gridId '" + gridId + "' and timestamp '" + DateTimeFormatter.getDateTime(timestamp) + "'(long=" + timestamp + ") and offset '" + offset + "' found");
			
			String payload = CoderFactory.encode(valueGrid, CoderFactory.JSON);
			MessagePayload msgPayload = new MessagePayload(payload, CoderFactory.JSON);
			message = new Message(true, msgPayload);
		}
		catch(Exception exc)
		{
			exc.printStackTrace();
			message = new Message(false, exc.getMessage());
		}
		
		return message;
    }
}
