import java.util.GregorianCalendar;


public class Time {
	GregorianCalendar calendar;
	int timeSpeed = 60;
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
	public void update(){
		System.out.println("local time: " + this.getTime() + " sunset: " + sunTimes.sunSet);
		if(this.getTime()  >= sunTimes.getSunSet()+1800){//give the program 30 minutes leeway
			System.out.println("After sunset");
			sunTimes = RedShift.Sunrise((this.getTime() + /*4320*/0),-45.86666666667, 170.5);
			System.out.println(this.toString());
			//System.out.println(sunRise);
			calendar.setTimeInMillis((long)(sunTimes.getSunRise()-1800) * 1000);
			System.out.println(this.toString());
			//calendar.setTimeInMillis(((long) RedShift.Sunrise(this.getTime()+  (0), -45.86666666667, 170.5)  * 1000));
		}else{
			calendar.add(GregorianCalendar.SECOND, (int) (1*timeSpeed));
		}
		
	}
	public String toString(){
		return calendar.getTime().toString();
	}
}
