package de.rainlessrouting.provider.model;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.GeoIndexed;

@RedisHash("Area")
public class Area implements Serializable{

	@Id private String id;
	@GeoIndexed private Point location;
	
	private double minLat;
	private double minLon;
	private double maxLat;
	private double maxLon;
	private double[] niederschlaege;
	
	public Area() {}

	public Area(double minLat, double minLon, double maxLat, double maxLon, double[] nieder) {
		this.setMinLat(minLat);
		this.setMaxLat(maxLat);
		this.setMinLon(minLon);
		this.setMaxLon(maxLon);
		
		this.setLocation(new Point(minLat, minLon));
		
		this.setNiederschlaege(nieder);
	}

	// getters & setters

	public double getMinLat() {
		return minLat;
	}

	public void setMinLat(double minLat) {
		this.minLat = minLat;
	}

	public double getMinLon() {
		return minLon;
	}

	public void setMinLon(double minLon) {
		this.minLon = minLon;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public void setMaxLat(double maxLat) {
		this.maxLat = maxLat;
	}

	public double getMaxLon() {
		return maxLon;
	}

	public void setMaxLon(double maxLon) {
		this.maxLon = maxLon;
	}

	public double[] getNiederschlaege() {
		return niederschlaege;
	}

	

	public void setNiederschlaege(double[] niederschlaege) {
		this.niederschlaege = niederschlaege;
	}
	
	
	 @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (o == null || getClass() != o.getClass()) return false;

	        Area latLon = (Area) o;

	        if (Double.compare(latLon.getMinLat(), this.minLat) != 0) return false;
	        return Double.compare(latLon.getMinLon(), this.minLon) == 0;
	    }

	    @Override
	    public int hashCode() {
	        int result;
	        long temp;
	        temp = Double.doubleToLongBits(this.minLat);
	        result = (int) (temp ^ (temp >>> 32));
	        temp = Double.doubleToLongBits(this.minLon);
	        result = 31 * result + (int) (temp ^ (temp >>> 32));
	        return result;
	    }
	    
	
	    public int testLat() {
	        int result;
	        long temp;
	        temp = Double.doubleToLongBits(this.minLat);
	        result = (int) (temp ^ (temp >>> 32));
	        return result;
	    }

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public Point getLocation() {
			return location;
		}

		public void setLocation(Point location) {
			this.location = location;
		}
}
