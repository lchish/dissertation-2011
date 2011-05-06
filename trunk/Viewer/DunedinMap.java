import java.io.File;
import java.io.FileNotFoundException;
import java.nio.*;
import java.util.Scanner;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

public class DunedinMap {

	public int MapWidth=1237,MapHeight=883;
	//public int MapWidth=100,MapHeight=100;
	private int [] vertexBuffer;;
	private int numVerticies;
	public float [][] Map;
	private static final int STEP_SIZE = 4;
	
	public DunedinMap(File f){
		try {
			Map = new float[MapWidth][MapHeight];
			Scanner sc  = new Scanner(f);
			for(int y = 0; sc.hasNextLine() && y < MapHeight;y++){
				Scanner line = new Scanner(sc.nextLine());
				for(int x = 0;line.hasNextFloat() && x <MapWidth;x++){
					float tmp = line.nextFloat();
					Map[x][y] = tmp < 0f ? -1f : tmp;
				}
			}

		} catch (FileNotFoundException e) {
			System.err.println("Unable to open height data: " + f.getName());
		}
		
	}
	public void init(GL2 gl){
		createVertexBuffer(gl);
	}
	public void createVertexBuffer(GL2 gl){
		float halfWidth = (MapWidth/2) * STEP_SIZE;
		float halfHeight = (MapHeight/2) * STEP_SIZE;
		numVerticies = (MapWidth -1) * (MapHeight-1)*4;
		int numFloatValues = numVerticies *3;
		vertexBuffer = new int[1];
		FloatBuffer points = Buffers.newDirectFloatBuffer(numFloatValues);
		gl.glGenBuffers(vertexBuffer.length, vertexBuffer,0);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		
		for(int x = 0;x<MapWidth-1;x++){
			for(int z = 0;z <MapHeight-1;z++){
				//gl.glNormal3f(0,1,0);
				//bottom left vertex
				float y = Map[x][z];
				float [] pos1 = {(float)x*STEP_SIZE - halfWidth, y, (float)z*STEP_SIZE - halfHeight};
				points.put(pos1);
				//gl.glVertex3f((float)x*STEP_SIZE - halfWidth, y, (float)z*STEP_SIZE - halfHeight);
				//top left Vertex
				y = Map[x][z+1];
				//gl.glVertex3f((float)(x*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight);
				float [] pos2 = {(float)(x*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight};
				points.put(pos2);
				//top right Vertex
				y = Map[x+1][z+1];
				//gl.glVertex3f((float)((x+1)*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight);
				float [] pos3 = {(float)((x+1)*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight};
				points.put(pos3);
				//bottom right Vertex
				y = Map[x+1][z];
				//gl.glVertex3f((float)(x+1)*STEP_SIZE- halfWidth, y, (float)z*STEP_SIZE- halfHeight);
				float [] pos4 = {(float)(x+1)*STEP_SIZE- halfWidth, y, (float)z*STEP_SIZE- halfHeight};
				points.put(pos4);
			}
		}
		points.rewind();
		//send the vertex data to the gfx card
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*points.capacity(), points, GL2.GL_STATIC_DRAW);
		//cleanup
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);
	}
	public void draw(GL2 gl){
		gl.glColor3f(0f, 1f, 0f);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,vertexBuffer[0]);
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
		gl.glDrawArrays(GL2.GL_QUADS, 0, numVerticies);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);
	}
	public void deleteVertexBuffer(GL2 gl){
		gl.glDeleteBuffers(vertexBuffer.length, vertexBuffer, 0);
	}
}
