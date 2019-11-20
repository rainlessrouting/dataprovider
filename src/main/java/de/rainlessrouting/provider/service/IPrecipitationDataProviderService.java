package de.rainlessrouting.provider.service;

import java.util.List;

import de.rainlessrouting.common.model.PrecipitationReadingGrid;
import de.rainlessrouting.common.model.SimplePrecipitation;
import de.rainlessrouting.common.model.PrecipitationInfo;

public interface IPrecipitationDataProviderService {
	
	public PrecipitationReadingGrid getPrecipitationReadingGrid();
	
	public PrecipitationInfo getPrecipitationInfo();
	
	public List<SimplePrecipitation> getSimplePrecipitationsForTimePoint(int timePoint);

}
