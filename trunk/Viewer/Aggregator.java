import java.io.*;
import java.util.Calendar;


public class Aggregator {
	private DunedinMap dunedinMap;
	private Sun sun;
	private Time time;
	private long [][] data;
	
	private static byte toSignedByte(int b){
		int a = b;
		return (byte) ((a>128?a-256:a) & 0xFF);
	}
	
	
	
	public static void main(String [] args){
		Aggregator a = new Aggregator(new Time(2011,0,1,0,0,0));
		a.runForYear("output.csv");
		
	}
	public void writeTga(long [][] data){
		File f = new File("aggregator.tga");
		BufferedOutputStream bis = null;
		try{
			bis = new BufferedOutputStream(new FileOutputStream(f));
			byte []  buf = new byte[18];
			buf[2] = 2;
			buf[12] =  toSignedByte(dunedinMap.MapWidth &0xFF);
			buf[13] =  toSignedByte(dunedinMap.MapWidth >> 8 &0xFF);
			buf[14] =  toSignedByte(dunedinMap.MapHeight &0xFF);
			buf[15] =  toSignedByte(dunedinMap.MapHeight >> 8 &0xFF);
			buf[16] =  toSignedByte(0x18);
			buf[17] =  toSignedByte(0x0);

			bis.write(buf);
			
			for(int y=dunedinMap.MapHeight-1;y>=0;y--){
				for(int x = 0;x<dunedinMap.MapWidth;x++){
					int tmp = rayLighting(x, y) ? 0: 255;
					byte []buf2 = new byte[3];
					buf2[0] = toSignedByte(tmp);
					buf2[1] = toSignedByte(tmp);
					buf2[2] = toSignedByte(tmp);
					bis.write(buf2);
				}
				System.out.println();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				bis.flush();
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void runForYear(String outputFileName){
		for(int x = 0;x<dunedinMap.MapWidth;x++){ //zero the data probably unnecessary
			for(int y=0;y<dunedinMap.MapHeight;y++){
				data[x][y] = 0;
			}
		}
		while(time.getCalendar().get(Calendar.DAY_OF_MONTH) < 2){
			for(int x = 0;x<dunedinMap.MapWidth;x++){
				for(int y=0;y<dunedinMap.MapHeight;y++){
					if(!rayLighting(x, y)){//if not in shadow
						data[x][y]++;
					}
				}
			}
			time.update();
			sun.updateNew(time);
		}
		FileWriter writer = null;
		try{
			writer = new FileWriter(outputFileName);
			for(int x = 0;x<dunedinMap.MapWidth;x++){
				for(int y=0;y<dunedinMap.MapHeight;y++){
					if(y != dunedinMap.MapHeight-1)
						writer.append(data[x][y] + ",");
					else
						writer.append(data[x][y] + "\n");
				}
			}
			writer.flush();
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public Aggregator(Time t){
		time = t;
		dunedinMap = new DunedinMap("dunedin.txt","terrain.tga","suburbs.tga");
		sun = new Sun(time);
		data = new long[dunedinMap.MapWidth][dunedinMap.MapHeight];
	}
	/**
	 * 
	 * @param x
	 * @param z
	 * @return true if in shadow
	 */
	public boolean rayLighting(int x,int z){
		Vec3f direction= sun.getDirectionUnitVector();
		double initialPointHeight = dunedinMap.getMapHeight(x, z);
		float tdx=1/direction.x,tdz=1/direction.z;//get the ball rolling
		int xPos=x,zPos=z;
		while(xPos <dunedinMap.getWidth()&&xPos>=0 && zPos<dunedinMap.getHeight() && zPos >=0){
			if(heightTest(xPos,zPos,direction,tdx>tdz?tdx:tdz,initialPointHeight))
				return true;
			//System.out.println("xPos: " + xPos + " zPos: " + zPos);
			//System.out.println("tdx:" +tdx + " tdz: "+ tdz);
			if(tdx<Math.abs(tdz)){
				//step along x
				float dx = 1/direction.x;
				xPos += tdx> 0 ? 1 : -1;
				tdx+=dx;
			}else{// tdz<tdx
				float dz = 1/direction.z;
				zPos += tdz> 0 ? 1 : -1;
				tdz+=dz;
			}
		}
		return false;
	}
	/**
	 * 
	 * @param xPos
	 * @param yPos
	 * @return true if in shadow
	 */
	private boolean heightTest(int xPos,int yPos,Vec3f toSun,double t,double initialPointHeight){
		double sunHeight = toSun.y * t;
		double mapHeight = dunedinMap.getMapHeight(xPos, yPos);
		//System.out.println("xpos:" + xPos + " Ypos: " + yPos);
		//System.out.println("Sunheight: " + sunHeight + "mapHeight: " + mapHeight);
		if(sunHeight+initialPointHeight < mapHeight){
			//System.out.println("In shadow");
			return true;
		}
		else
			return false;
	}
}
