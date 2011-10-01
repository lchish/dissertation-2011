import java.io.*;


public class Aggregator {
	private DunedinMap dunedinMap;
	private Sun sun;
	private Time time;
	public long [][] totalData;
	public boolean [][] stepData;
	public long steps;//number of seconds the during the current run. ratio of sunlight hours is sunlightSeconds / steps
	private boolean runOnce;
	private static byte toSignedByte(int b){
		int a = b;
		return (byte) ((a>128?a-256:a) & 0xFF);
	}



	public static void main(String [] args){
		Time time = new Time(2011,9,1,17,0,0);
		Aggregator a = new Aggregator(time,new DunedinMap("dunedin.txt","terrain.tga","suburbs.tga"),new Sun(time));
		//a.runForYear("output.csv");
		//a.run();
		a.runOptimised();
		a.writeTga(a.stepData,"aggregator.tga");
		//a.writeCSV(a.stepData,"out.csv");
	}
	public void run(){
		if(time.getTimeSpeed()!=0  && sun.getElevation() > 0.0|| !runOnce){
			runOnce = true;
			
			rayLightingNonOptimised(0, 0, dunedinMap.getWidth(), dunedinMap.getHeight());
			steps+=time.getTimeSpeed();
		}
	}

	public void runOptimised(){
		if(time.getTimeSpeed()!=0  && sun.getElevation() > 0.0|| !runOnce){
			runOnce = true;
			int stepSize  = 10,x,y;
			for(x=0;x+stepSize<dunedinMap.getWidth();x+= stepSize){
				for(y=0;y+stepSize<dunedinMap.getHeight();y+=stepSize){
					rayLightingOptimised(x, y, stepSize, stepSize);
				}
				//finish off the final squares
				//rayLightingNonOptimised(x, y, stepSize, dunedinMap.getHeight()-y);
			}
			//rayLightingNonOptimised(x, 0, dunedinMap.getWidth()-x,dunedinMap.getHeight());
			steps+=time.getTimeSpeed();
		}
	}
	
	public void update(){//update the aggregator ONLY USE IF NOT USING THE VIEWERS UPDATE
		time.update(sun);
		sun.updateNew(time);
	}
	public void writeCSV(boolean [][] data,String filename){
		try{
			FileWriter f = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(f);
			for(int y=dunedinMap.MapHeight-1;y>=0;y--){
				for(int x = 0;x<dunedinMap.MapWidth;x++){
					long tmp = totalData[x][y];
					if(tmp != 0)
						out.write(tmp+",");
					else{
						out.write(",");
					}
				}
				out.write("\n");
			}
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void writeCSV(long [][] data,String filename){
		try{
			FileWriter f = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(f);
			for(int y=dunedinMap.MapHeight-1;y>=0;y--){
				for(int x = 0;x<dunedinMap.MapWidth;x++){
					long tmp = totalData[x][y];
					if(tmp != 0)
						out.write(tmp+",");
					else{
						out.write(",");
					}
				}
				out.write("\n");
			}
			out.flush();
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	public void writeTga(boolean [][] data,String filename){//writes out the shadows at a given time step
		File f = new File(filename);
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
					if(dunedinMap.inWater(x, y)){
						byte []buf2 = new byte[3];
						buf2[0] = toSignedByte(0xff);
						buf2[1] = toSignedByte(0);
						buf2[2] = toSignedByte(0);
						bis.write(buf2);
					}else{
						int tmp = stepData[x][y] ? 0: 255;
						byte []buf2 = new byte[3];
						buf2[0] = toSignedByte(tmp);
						buf2[1] = toSignedByte(tmp);
						buf2[2] = toSignedByte(tmp);
						bis.write(buf2);
					}
				}
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

	public void writeTga(long [][] data,String filename){//writes out the "heat" of an area aka the amount of sun it recieved
		File f = new File(filename);
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
					if(dunedinMap.inWater(x, y)){
						byte []buf2 = new byte[3];
						buf2[0] = toSignedByte(0);
						buf2[1] = toSignedByte(0);
						buf2[2] = toSignedByte(0xff);
						bis.write(buf2);
					}else{
						int tmp = (int) (totalData[x][y] /steps);
						byte []buf2 = new byte[3];
						buf2[0] = toSignedByte(tmp);
						buf2[1] = toSignedByte(tmp);
						buf2[2] = toSignedByte(tmp);
						bis.write(buf2);
					}
				}
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

	public Aggregator(Time t,DunedinMap d,Sun s){
		time = t;
		dunedinMap =d;
		sun = s;
		totalData = new long[dunedinMap.MapWidth][dunedinMap.MapHeight];
		stepData = new boolean[dunedinMap.MapWidth][dunedinMap.MapHeight];
		runOnce = false;
	}

	private void rayLightingNonOptimised(int x,int z, int width,int height){
		for(int xOff = x;xOff < x+width;xOff++){
			for(int zOff = z; zOff < z+height;zOff++){
				if(!dunedinMap.inWater(xOff, zOff) && dunedinMap.inSuburb(xOff, zOff)){
					if(!rayLighting(xOff, zOff)){//if not in shadow
						stepData[xOff][zOff] = false;
						totalData[xOff][zOff]++;
					}else{
						stepData[xOff][zOff] = true;
					}
				}
			}
		}
	}
	
	private void  rayLightingOptimised(int x,int z, int width,int height) {
		//scan around the edges of the area
		int xOff,zOff;
		for(xOff = x,zOff=z; xOff < x+width;xOff++){//bottom left to bottom right
				if(!rayLighting(xOff, zOff)){//if not in shadow
					stepData[xOff][zOff] = false;
					totalData[xOff][zOff]++;
				}else{
					stepData[xOff][zOff] = true;
					rayLightingNonOptimised(x, z, width, height);
					return;
				}
		}
		for(xOff = x,zOff = z+height-1; xOff < x+width;xOff++){//top left to top right
				if(!rayLighting(xOff, zOff)){//if not in shadow
					stepData[xOff][zOff] = false;
					totalData[xOff][zOff]++;
				}else{
					stepData[xOff][zOff] = true;
					rayLightingNonOptimised(x, z, width, height);
					return;
				}
		}
		for( zOff = z,xOff = x; zOff < z+height;zOff++){//bottom left to top left
				if(!rayLighting(xOff, zOff)){//if not in shadow
					stepData[xOff][zOff] = false;
					totalData[xOff][zOff]++;
				}else{
					stepData[xOff][zOff] = true;
					rayLightingNonOptimised(x, z, width, height);
					return;
				}
		}
		for(xOff = x+width-1, zOff = z; zOff < z+height;zOff++){//bottom right to top right
				if(!rayLighting(xOff, zOff)){//if not in shadow
					stepData[xOff][zOff] = false;
					totalData[xOff][zOff]++;
				}else{
					stepData[xOff][zOff] = true;
					rayLightingNonOptimised(x, z, width, height);
					return;
				}
		}
		//woohoo now to assume that there are no shadows whithin the region we will fill in the remainder
		for( xOff = x+1;xOff < width-1;xOff++){
			for( zOff = z+1; zOff < height-1;zOff++){
				stepData[xOff][zOff] =false;
				totalData[xOff][zOff]++;
			}
		}
	}
	
	public boolean rayLighting(int x,int z){
		Vec3f direction= sun.getDirectionUnitVector();
		double initialPointHeight = dunedinMap.getMapHeight(x, z);
		float tdx=1/direction.x,tdz=1/direction.z;//get the ball rolling
		int xPos=x,zPos=z;
		while(xPos <dunedinMap.getWidth()&&xPos>=0 && zPos<dunedinMap.getHeight() && zPos >=0){
			if(heightTest(xPos,zPos,direction,Math.abs(tdx>tdz?tdx:tdz),initialPointHeight))
				return true;
			if(Math.abs(tdx)<Math.abs(tdz)){// a move in x is less than a move in z
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
		if(sunHeight+initialPointHeight < mapHeight){
			return true;
		}else{
			return false;
		}
	}
}
