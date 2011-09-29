import java.util.Calendar;
import java.util.GregorianCalendar;


public class RedShift {

	public static double RAD(double d){
		return  d  * (Math.PI/180);
	}
	
	public static double DEG(double d){
		return  d  * (180/Math.PI);
	}
	
	private static double jd_from_epoch(double t){
		return (t / 86400.0) + 2440587.5;
	}
	private static double epoch_from_jd(double t){
		return (t - 2440587.5) * 86400.0;
	}
	private static  double jcent_from_jd(double jd){
		return (jd - 2451545.0) / 36525.0;
	}
	
	private static double jd_from_jcent(double t)
	{
		return 36525.0*t + 2451545.0;
	}
	
	private static double mean_ecliptic_obliquity(double t)
	{
		double sec = 21.448 - t*(46.815 + t*(0.00059 - t*0.001813));
		return RAD(23.0 + (26.0 + (sec/60.0))/60.0);
	}
	private static double obliquity_corr(double t)
	{
		double e_0 = mean_ecliptic_obliquity(t);
		double omega = 125.04 - t*1934.136;
		return RAD(DEG(e_0) + 0.00256*Math.cos(RAD(omega)));
	}
	
	private static double sun_geom_mean_lon(double t)
	{
		/* FIXME returned value should always be positive */
		return RAD((280.46646 + t*(36000.76983 + t*0.0003032)) % 360);
	}
	
	private static double earth_orbit_eccentricity(double t){
		return 0.016708634 - t*(0.000042037 + t*0.0000001267);
	}
	private static double sun_geom_mean_anomaly(double t)
	{
		return RAD(357.52911 + t*(35999.05029 - t*0.0001537));
	}
	
	private static double equation_of_time(double t){
		double epsilon = obliquity_corr(t);
		double l_0 = sun_geom_mean_lon(t);
		double e = earth_orbit_eccentricity(t);
		double m = sun_geom_mean_anomaly(t);
		double y = Math.pow(Math.tan(epsilon/2.0), 2.0);

		double eq_time = y*Math.sin(2.0*l_0) - 2*e*Math.sin(m) +
			4*e*y*Math.sin(m)*Math.cos(2.0*l_0) -
			0.5*y*y*Math.sin(4.0*l_0) -
			1.25*e*e*Math.sin(2.0*m);
		return 4.0*DEG(eq_time);
	}
	private static double sun_equation_of_center(double t){
		/* Use the first three terms of the equation. */
		double m = sun_geom_mean_anomaly(t);
		double c = Math.sin(m)*(1.914602 - t*(0.004817 + 0.000014*t)) +
				Math.sin(2.0*m)*(0.019993 - 0.000101*t) +
				Math.sin(3.0*m)*0.000289;
		return RAD(c);
	}
	
	private static double sun_true_lon(double t)
	{
		double l_0 = sun_geom_mean_lon(t);
		double c = sun_equation_of_center(t);
		return l_0 + c;
	}
	
	private static double sun_apparent_lon(double t)
	{
		double o = sun_true_lon(t);
		return RAD(DEG(o) - 0.00569 - 0.00478*Math.sin(RAD(125.04 - 1934.136*t)));
	}
	
	private static double solar_declination(double t)
	{
		double e = obliquity_corr(t);
		double lambda = sun_apparent_lon(t);
		return Math.asin(Math.sin(e)*Math.sin(lambda));
	}
	private static double elevation_from_hour_angle(double lat, double decl, double ha)
	{
		return Math.asin(Math.cos(ha)*Math.cos(RAD(lat))*Math.cos(decl) +
				Math.sin(RAD(lat))*Math.sin(decl));
	}
	/*private static double azimuth(double elevation,double ha,double declination,double latitude){
		return Math.acos((Math.sin(declination) - Math.sin(elevation) * Math.sin(RAD(latitude)))/ Math.cos(elevation)*Math.cos(RAD(latitude)));
	}
	private static double azimuth2(double elevation,double ha,double declination,double latitude){
		return Math.acos((Math.sin(declination) - Math.sin(elevation) * Math.sin(latitude))/
				Math.cos(elevation) * Math.cos(latitude));
	}
	private static double azimuth3(double elevation,double ha,double declination,double latitude){
		return Math.asin((-Math.sin(ha)*Math.cos(declination))/Math.cos(elevation));
	}
	private static double azimuth4(double decl,double ha,double lat){
		return Math.atan2(-Math.sin(ha),Math.tan(decl)*Math.cos(RAD(lat)) - Math.sin(RAD(lat)) * Math.cos(ha));
	}
	private static double azimuth5(double ha,double decl,double latitude){
		return Math.atan2(Math.sin(ha) * Math.cos(decl),Math.cos(ha)*Math.cos(decl) * Math.cos(RAD(90.0 - latitude)));
	}*/
	private static double azimuth6(double declination,double latitude,double ha){
		double x_azm = Math.sin(ha) * Math.cos(declination);
		double y_azm = (-(Math.cos(ha)) * Math.cos(declination)*Math.sin(RAD(latitude)) + Math.cos(RAD(latitude)) * Math.sin(declination));
		double azi =Math.atan(x_azm/y_azm); 
		return azi < 0 ? azi+(2*Math.PI) : azi;
	}
	
	private static  SunAzimuthElevation solar_elevation_from_time(double t, double lat, double lon){
		/* Minutes from midnight */
		double jd = jd_from_jcent(t);
		double offset = (jd - Math.round(jd) - 0.5)*1440.0;

		double eq_time = equation_of_time(t);
		double ha = RAD((720 - offset - eq_time)/4 - lon);
		double decl = solar_declination(t);
		return new SunAzimuthElevation(DEG(azimuth6(decl,lat,ha)), DEG(elevation_from_hour_angle(lat, decl, ha)));
		
	}

	public static  SunAzimuthElevation getSunAzimuthElevation(double date,double lat,double lon){
		double jd = jd_from_epoch(date);
		return solar_elevation_from_time(jcent_from_jd(jd), lat, lon);
		
	}
	
	public static SunRiseAndSet Sunrise(double date,double lat,double lon){
		double jd = Math.ceil(jd_from_epoch(date));
		//System.out.println(jd);
		double n = Math.round((jd - 2451545 - 0.0009 ) - (lon/360));
		//System.out.println(n);
		double jStar = 2451545 + 0.0009 + (lon/360) + n;
		//System.out.println(jStar);
		double M = 0,C,lambda = 0,jTransit=jStar;
		for(int i =0;i<3;i++){
			M = RAD((357.5291+0.98560028*(jTransit-2451545))%360);
			//System.out.println(DEG(M));

			C = (1.9148*Math.sin(M) + (0.02 * Math.sin(2*M)) + (0.0003 *Math.sin(3*M)));
			//System.out.println(C);
			lambda = RAD((DEG(M)+102.9372+C+180) %360);
			//System.out.println(DEG(lambda));
			//double jTransit = J + (0.0053*Math.sin(M)) - (0.0069 *Math.sin(2*lambda));0.
			double a = (0.0053*Math.sin(M));
			double b =(0.0069 *Math.sin(2*lambda));
			//System.out.println(a + " " + b);
			jTransit = jStar + a - b;
			//System.out.println(DEG(M) + " " +C + " " + DEG(lambda) + " " + jTransit);
		}
		double sigma = Math.asin(Math.sin(lambda) * Math.sin(RAD(23.45)));
		//System.out.println(DEG(sigma));
		double H = Math.acos(  (Math.sin(RAD(-0.83)) - Math.sin(RAD(lat)) * Math.sin(sigma) ) / (Math.cos(RAD(lat)) * Math.cos(sigma)) );
		//System.out.println("HH" + DEG(H));
		double newJ = 2451545 +0.0009 +((DEG(H)+lon)/360) +n;
		//System.out.println(newJ);
		double jSet = newJ + (0.0053 * Math.sin(M)) - (0.0069 * Math.sin(2*lambda))+0.041666666666666667;
		//System.out.println("sunset" + jSet);
		double jRise = jTransit -( jSet - jTransit)+0.0833333333333;
		//System.out.println("sunrise" + jRise);
		
		//System.out.println(date + " " + jd_from_epoch(date) + " " + epoch_from_jd(jd));
		
		//System.out.println(epoch_from_jd(jRise));
		return new SunRiseAndSet((long)epoch_from_jd(jRise), (long)epoch_from_jd(jSet));		
	}
}
