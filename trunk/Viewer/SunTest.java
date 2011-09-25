import java.util.GregorianCalendar;

public class SunTest {
	
	public static void main(String [] args){
		//double time = r.getTime();
		Time t = new Time(2011,11,20,20,0,0);
		System.out.println(t.getCalendar().getTime());
		double time = t.getTime();/*
		//System.out.println("r gettime:" + r.getTime() + " calendar gettime: " + (double)t.getTime());
		SunAzimuthElevation s = RedShift.getSunAzimuthElevation(time, -45.86666666667, 170.5);
		System.out.println(s);*/
		SunRiseAndSet ras = RedShift.Sunrise(time, -45.86666666667, 170.5);
		GregorianCalendar cal = new GregorianCalendar();
		System.out.println(time +  (86400 * 1000));
		cal.setTimeInMillis((long) (ras.sunRise)  * 1000);
		System.out.println(cal.getTime());
		cal.setTimeInMillis((long) (ras.sunSet)  * 1000);
		System.out.println(cal.getTime());
	}
	
}
