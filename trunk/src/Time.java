import java.util.Calendar;
import java.util.GregorianCalendar;


public class Time {
	GregorianCalendar calendar;
	int timeSpeed = 0;
	SunRiseAndSet sunTimes;

	public GregorianCalendar getCalendar() {
		return calendar;
	}
	public long getTime(){
		return calendar.getTimeInMillis()/1000L;
	}
	public Time(int year,int month,int day, int hour,int minute,int second){
		calendar = new GregorianCalendar(year, month, day, hour, minute, second);//set the exact time
		sunTimes = RedShift.Sunrise(this.getTime(),-45.86666666667, 170.5);
	}
	public Time(){
		calendar = new GregorianCalendar();//use current time
		sunTimes = RedShift.Sunrise(this.getTime(),-45.86666666667, 170.5);
	}

	public void setTimeSpeed(int d){
		timeSpeed = d;
	}
	public int getTimeSpeed(){
		return timeSpeed;
	}
	public void update(Sun sun){
		if(sun.getElevation() < 0.0){//in the afternoon
			if(calendar.get(Calendar.HOUR_OF_DAY) > 12){
				calendar.add(GregorianCalendar.HOUR, 6);
				while(sun.getElevation() <0.0){
					calendar.add(GregorianCalendar.MINUTE, 1);
					sun.updateNew(this);
				}
			}else{
				calendar.add(GregorianCalendar.HOUR, -6);
				while(sun.getElevation() <0.0){
					calendar.add(GregorianCalendar.MINUTE, -1);
					sun.updateNew(this);
				}
			}
		}else{
			calendar.add(GregorianCalendar.SECOND, (int) (1*timeSpeed));
		}

	}
	public String toString(){
		return calendar.getTime().toString();
	}
}
