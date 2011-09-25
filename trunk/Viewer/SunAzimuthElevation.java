
public class SunAzimuthElevation{
	private double azimuth;
	public double getAzimuth() {
		return azimuth;
	}
	public double getElevation() {
		return elevation;
	}
	private double elevation;
	
	public SunAzimuthElevation(double a,double e){
		azimuth=a;
		elevation=e;
	}
	public String toString(){
		return "Azimuth: " + azimuth + " elevation: " + elevation;
	}
}