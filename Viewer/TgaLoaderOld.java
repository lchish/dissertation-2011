import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class TgaLoaderOld {
	int width,height;
	private static int offset;
	public static final int HEADER_SIZE = 18;//
	byte [] buf;
	int[][][] data;
	
	public TgaLoaderOld(String filename){	
		try{
			File f = new File(filename);
            buf = new byte[(int)f.length()];
            
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
            bis.read(buf);
            bis.close();

         // Reading header
            for (int i=0;i<12;i++)
                    read(buf);
            width = read(buf)+(read(buf)<<8);
            height = read(buf)+(read(buf)<<8);
            read(buf);
            read(buf);
            data = new int[width][height][3];
            //reading data
            int b,g,r;
            if(buf[2] == 0x02 && buf[16]==0x18){//uncompressed bgr
            	for(int y=0;y<height;y++){
            		for(int x=0;x<width;x++){
            			b = read(buf);
            			g = read(buf);
            			r = read(buf);
            			data[x][y][0]=r;
            			data[x][y][1]=g;
            			data[x][y][2]=b;
            		}
            	}
            }else{//rle compressed
            	System.err.println("TGA encoding not yet supported!");
            	System.exit(1);
            }
		}catch(Exception e){
			System.err.println("Cannot find file " + filename);
			e.printStackTrace();
		}
	}
	public int [] getData(int x,int y){
		return data[x][y];
	}
	private static int read(byte[] buf){
		return btoi(buf[offset++]);
	}
	private static int btoi(byte b){
		int a = b;
		return (a<0?256+a:a);
	}

}
