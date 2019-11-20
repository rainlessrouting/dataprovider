package de.rainlessrouting.provider;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.rainlessrouting.common.coder.Base64Coder;
import de.rainlessrouting.common.coder.CborCoder;
import de.rainlessrouting.common.coder.CoderFactory;
import de.rainlessrouting.common.model.Message;
import de.rainlessrouting.common.model.MessagePayload;
import de.rainlessrouting.common.model.PrecipitationReadingGrid;
import de.rainlessrouting.common.model.PrecipitationReadingPoint;
import de.rainlessrouting.common.model.SimplePrecipitation;
import de.rainlessrouting.provider.service.IPrecipitationDataProviderService;

@Controller
public class PrecipitationDataProviderController {
	private static final Logger log = LoggerFactory.getLogger(PrecipitationDataProviderController.class);

	@Autowired
	IPrecipitationDataProviderService precipitationData;
	
	String routingUrl = "http://10.0.2.2:8989/route?ch.disable=true&algorithm=dijkstra&precipitation_measurement=true&details=time&";
	String rainRoutingUrl = "http://10.0.2.2:8989/route?ch.disable=true&precipitation_weighting=edges&spatial_interpolation=nn&temporal_interpolation=static&algorithm=dijkstra&details=time&precipitation_measurement=true&";
	
	
	private final int PACKAGESIZE = 1000; 
	
	@GetMapping("/precipitationList")
    public @ResponseBody Message getPrecipitationList(@RequestParam(value="index", defaultValue="0") String packageIndex) {
		
		Message result;
		PrecipitationReadingGrid precGrid = this.precipitationData.getPrecipitationReadingGrid();
		
		// The whole precipitation data comes in 'packagecount' different packages. The client requests a package by providing an index.
		int packageCount = precGrid.getLatGridSize() * precGrid.getLongGridSize() / this.PACKAGESIZE;
		int index = Integer.parseInt(packageIndex);
				
		log.debug("PrecipitationDataProviderController: /precipitationList get package {}/{}", packageIndex,  packageCount);
		
		if(index >= 0 && index < packageCount) 
		{
			int startIndex = index * this.PACKAGESIZE; 
			int endIndex = (index + 1) * this.PACKAGESIZE;
			
//			List<PrecipitationReadingPoint> list = new ArrayList<PrecipitationReadingPoint>();
//			list.add(new PrecipitationReadingPoint(1.234, 5.6789, new byte[] {1,2,3,0,4,0,5,0,6,7,8,9}));
//			byte[] cbor = CborCoder.encode(list);
//			
//			System.out.println("Array:" + Arrays.toString(cbor));
//			System.out.println("BASE:" + new String(Base64Coder.encode(cbor)));
//			
			List<PrecipitationReadingPoint> list = precGrid.getPrecipitationReadingPoints(startIndex, endIndex);

			byte[] cbor = CborCoder.encode(list);
//			byte[] zip = CompressionCoder.compress(cbor);
//			String s = Base64Coder.encode(cbor);
			
			MessagePayload msgPayload = new MessagePayload(cbor, CoderFactory.CBOR_BASE64);
			
			result = new Message(true, msgPayload);
			result.addMetadata(Message.METADATA_KEY_INDEX, index);
			result.addMetadata(Message.METADATA_KEY_PACKAGE_COUNT, packageCount);
			result.addMetadata(Message.METADATA_KEY_LATITUDE_COUNT, precGrid.getLatGridSize());
			result.addMetadata(Message.METADATA_KEY_LONGITUDE_COUNT, precGrid.getLongGridSize());
		}
		else if(index == packageCount) 
		{
			int startIndex = index * this.PACKAGESIZE;

			byte[] cbor = CborCoder.encode(precGrid.getPrecipitationReadingPoints(startIndex, (precGrid.getLatGridSize() * precGrid.getLongGridSize()) - 1));
//			byte[] zip = CompressionCoder.compress(cbor);
			String s = Base64Coder.encode(cbor);
			MessagePayload msgPayload = new MessagePayload(cbor, CoderFactory.CBOR_BASE64);
			
			result = new Message(true, msgPayload);
			result.addMetadata(Message.METADATA_KEY_INDEX, index);
			result.addMetadata(Message.METADATA_KEY_PACKAGE_COUNT, packageCount);
			result.addMetadata(Message.METADATA_KEY_LATITUDE_COUNT, precGrid.getLatGridSize());
			result.addMetadata(Message.METADATA_KEY_LONGITUDE_COUNT, precGrid.getLongGridSize());
		}
		else
		{
			result = new Message(false, null);
			result.addMetadata(Message.METADATA_KEY_INDEX, index);
			result.addMetadata(Message.METADATA_KEY_PACKAGE_COUNT, packageCount);
		}

		// each message is about 90.000 Byte and contains 1000 PrecipitationReadingPoints and each point contains lat/lon + 60 precipitation values
		
		// log.debug("PrecipitationDataProviderController: /precipitationList result success=" + result.isSuccess() + " index=" + result.getIndex() + " length=" + result.getLength() + " data=" + (result.getPayload() != null && result.getPayload() instanceof List? "List.size()=" + ((List)result.getPayload()).size() : result.getPayload() == null ? "null" : result.getPayload().getClass().getName()));
        
		return result;
    }
	
	@GetMapping("/precipitationList2")
    public @ResponseBody Message getPrecipitationForTimePointList(@RequestParam(value="index", defaultValue="0") String indexString, @RequestParam(value="timepoint", defaultValue="0") int timepoint) {
		
		log.debug("PrecipitationDataProviderController: /precipitationList2 indexString={} timepoint={}", indexString, timepoint);
		
		
		int index = Integer.parseInt(indexString);
		
		List<SimplePrecipitation> precipitations = precipitationData.getSimplePrecipitationsForTimePoint(timepoint); 
		
		int packageCount = precipitations.size() / this.PACKAGESIZE;
		
		Message result;
		
		if(index >= 0 && index < packageCount) {
			int startIndex = index * this.PACKAGESIZE; 
			int endIndex = (index + 1) * this.PACKAGESIZE;	
			result = new Message(true, precipitations.subList(startIndex, endIndex));
		}
		else if(index == packageCount) {
			int startIndex = index * this.PACKAGESIZE; 
			result = new Message(true, precipitations.subList(startIndex, precipitations.size() - 1));
		}
		else
		{
			result = new Message(false, null);
			result.addMetadata(Message.METADATA_KEY_INDEX, index);
			result.addMetadata(Message.METADATA_KEY_PACKAGE_COUNT, packageCount);
			
		}

        return result;
    		
    }
	
	
	
	@GetMapping("/precipitationInfo")
    public @ResponseBody Message getPrecipitationInfo() {
		
		log.debug("PrecipitationDataProviderController: /precipitationInfo");
		Message result = new Message(false, this.precipitationData.getPrecipitationInfo());

        return result;
    		
    }
	
	
	@GetMapping("/routing")
	protected String routingRedirect(HttpServletRequest request) 
	{
		
		String result = "redirect:" + routingUrl + request.getQueryString();
		
		log.debug("PrecipitationDataProviderController: /routing " + result);
		
	    return result;
	}
	

	
	@PostMapping("/changeRouting")
    public @ResponseBody String changeRouting(@RequestBody String urlString) {
    		
    	    if(!urlString.isEmpty()) {
    	    	  this.routingUrl = urlString;
    	    }
    	
    	    log.debug("PrecipitationDataProviderController: /changeRouting urlString=" + urlString);
    	    
    		return this.routingUrl; 	
		
	}
	
	
	
	@GetMapping("/rainRouting")
	protected String rainRoutingRedirect(HttpServletRequest request) 
	{
		
		String result = "redirect:" + rainRoutingUrl + request.getQueryString();
		
		log.debug("PrecipitationDataProviderController: /rainRouting " + result);
		
	    return result;
	}
	

	
	@PostMapping("/changeRainRouting")
    public @ResponseBody String changeRainRouting(@RequestBody String urlString) {
    		
    	    if(!urlString.isEmpty()) {
    	    	  this.rainRoutingUrl = urlString;
    	    }
    		
    	    log.debug("PrecipitationDataProviderController: /changeRainRouting urlString=" + urlString);
    	    
    		return this.rainRoutingUrl; 	
		
	}
	

}
