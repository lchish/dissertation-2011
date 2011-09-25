import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;


public class FasterHeightInfo {
	public static int MapWidth=1237;
	public static int MapHeight=883;
	
	
	public static void main(String[] args){
		System.out.println("asd");
		try {
			File f = new File("dunedin.dat");
			DataOutputStream bis = new DataOutputStream(new FileOutputStream(f));
			
			Scanner sc  = new Scanner(new File("dunedin.txt"));
			System.out.println("asd2");
			while(sc.hasNextLine()){
				Scanner lineScan = new Scanner(sc.nextLine());
				while (lineScan.hasNextFloat()){
					bis.writeFloat(lineScan.nextFloat());
				}
				bis.writeFloat(-Float.MAX_VALUE);
			}
			bis.flush();
			bis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
