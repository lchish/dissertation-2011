import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.media.opengl.GL2;


public class DunedinMap {

	public int MapWidth=1237,MapHeight=883;
	//public int MapWidth=200,MapHeight=200;
	public float [][] Map;
	private static final int STEP_SIZE = 32;
	
	public DunedinMap(File f){
		try {
			Map = new float[MapWidth][MapHeight];
			Scanner sc  = new Scanner(f);
			for(int y = 0; sc.hasNextLine();y++){
				Scanner line = new Scanner(sc.nextLine());
				for(int x = 0;line.hasNextFloat();x++){
					float tmp = line.nextFloat();
					Map[x][y] = tmp < 0.0f ? 0.0f : tmp;
				}
			}

		} catch (FileNotFoundException e) {
			System.err.println("Unable to open height data: " + f.getName());
		}
	}
	
	public void draw(GL2 gl){
		gl.glBegin(GL2.GL_QUADS);
		gl.glColor3f(0f, 1f, 0f);
		float halfWidth = (MapWidth/2) * STEP_SIZE;
		float halfHeight = (MapHeight/2) * STEP_SIZE;
		for(int x = 0;x<MapWidth-1;x++){
			
			for(int z = 0;z <MapHeight-1;z++){
				//bottom left vertex
				float y = Map[x][z];
				//gl.glColor3f(0f, 0f, Map[x][z]%256);
				gl.glVertex3f((float)x*STEP_SIZE - halfWidth, y, (float)z*STEP_SIZE - halfHeight);
				//top left Vertex
				y = Map[x][z+1];
				//gl.glColor3f(0f, 0f, Map[x][z+1]%256);
				gl.glVertex3f((float)(x*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight);
				//top right Vertex
				y = Map[x+1][z+1];
				//gl.glColor3f(0f, 0f, Map[x+1][z+1]%256);
				gl.glVertex3f((float)((x+1)*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight);
				//bottom right Vertex
				y = Map[x+1][z];
				//gl.glColor3f(0f, 0f, Map[x+1][z]%256);
				gl.glVertex3f((float)(x+1)*STEP_SIZE- halfWidth, y, (float)z*STEP_SIZE- halfHeight);
			}
		}
		gl.glEnd();
	}
}
