import java.io.File;
import java.io.FileNotFoundException;
import java.nio.*;
import java.util.Scanner;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

public class DunedinMap {

	public int MapWidth=1237,MapHeight=883;
	//public int MapWidth=100,MapHeight=100;
	private int [] vertexBuffer,colourBuffer,normalBuffer;
	private int numVerticies;
	public float [][] Map;
	private static final int STEP_SIZE = 4;
	
	public DunedinMap(String filename){
		try {
			Map = new float[MapWidth][MapHeight];
			Scanner sc  = new Scanner(new File(filename));
			for(int y = 0; sc.hasNextLine() && y < MapHeight;y++){
				Scanner line = new Scanner(sc.nextLine());
				for(int x = 0;line.hasNextFloat() && x <MapWidth;x++){
					float tmp = line.nextFloat();
					Map[x][y] = tmp < 0.01f ? -1f : tmp;
				}
			}

		} catch (FileNotFoundException e) {
			System.err.println("Unable to open height data: " + filename);
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
		colourBuffer = new int[1];
		normalBuffer = new int[1];
		FloatBuffer points = Buffers.newDirectFloatBuffer(numFloatValues);
		FloatBuffer colours = Buffers.newDirectFloatBuffer(numFloatValues);
		FloatBuffer normals = Buffers.newDirectFloatBuffer(numFloatValues);
		gl.glGenBuffers(vertexBuffer.length, vertexBuffer,0);
		gl.glGenBuffers(colourBuffer.length,colourBuffer,0);
		gl.glGenBuffers(normalBuffer.length,normalBuffer,0);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		for(int x = 0;x<MapWidth-1;x++){
			for(int z = 0;z <MapHeight-1;z++){
				
				//bottom left vertex
				float y = Map[x][z];
				if(y <0.01f){
					float [] colour = {0f,0f,1f};
					colours.put(colour);
				}else{
					float [] colour = {0,Map[x][z]/512,0};
					colours.put(colour);
				}
				float [] pos1 = {(float)x*STEP_SIZE - halfWidth, y, (float)z*STEP_SIZE - halfHeight};
				points.put(pos1);
				//top left Vertex
				y = Map[x][z+1];
				if(y <0.01f){
					float [] colour = {0f,0f,1f};
					colours.put(colour);
				}else{
					float [] colour = {0,Map[x][z]/512,0};
					colours.put(colour);
				}
				float [] pos2 = {(float)(x*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight};
				points.put(pos2);
				//top right Vertex
				y = Map[x+1][z+1];
				if(y <0.01f){
					float [] colour = {0f,0f,1f};
					colours.put(colour);
				}else{
					float [] colour = {0,Map[x][z]/512,0};
					colours.put(colour);
				}
				float [] pos3 = {(float)((x+1)*STEP_SIZE)- halfWidth, y, (float)((z+1)*STEP_SIZE)- halfHeight};
				points.put(pos3);
				//bottom right Vertex
				y = Map[x+1][z];
				if(y <0.01f){
					float [] colour = {0f,0f,1f};
					colours.put(colour);
				}else{
					float [] colour = {0,Map[x][z]/512,0};
					colours.put(colour);
				}
				float [] pos4 = {(float)(x+1)*STEP_SIZE- halfWidth, y, (float)z*STEP_SIZE- halfHeight};
				points.put(pos4);
				//calculate the normals for all of the vertices
				Vec3f normal = calculateNormal(new Vec3f(pos1),new Vec3f(pos2),new Vec3f(pos3));
				for(int i =0;i<4;i++){
					normals.put(normal.toFloatArray());
				}
			}
		}
		points.rewind();
		colours.rewind();
		normals.rewind();
		//send the vertex and colour data to the gfx card
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*points.capacity(), points, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colourBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*colours.capacity(), colours, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*normals.capacity(), normals, GL2.GL_STATIC_DRAW);
		//cleanup
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);
	}
	public void draw(GL2 gl){
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colourBuffer[0]);
		gl.glColorPointer(3, GL2.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer[0]);
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalBuffer[0]);
		gl.glNormalPointer(GL2.GL_FLOAT, 0, 0);
		gl.glDrawArrays(GL2.GL_QUADS, 0, numVerticies);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);
	}
	public void deleteVertexBuffer(GL2 gl){
		gl.glDeleteBuffers(vertexBuffer.length, vertexBuffer, 0);
		gl.glDeleteBuffers(colourBuffer.length,colourBuffer,0);
		gl.glDeleteBuffers(normalBuffer.length,normalBuffer,0);
	}
	private class Vec3f{
		float x,y,z;
		public Vec3f(float[] a){
			x=a[0];
			y=a[1];
			z=a[2];
		}
		public Vec3f(){x=y=z=0f;};
		public float [] toFloatArray(){
			float [] ret = new float[3];
			ret[0]=x;ret[1]=y;ret[2]=z;
			return ret;
		}
	}
	public Vec3f calculateNormal(Vec3f pos1,Vec3f pos2,Vec3f pos3)
    {
		Vec3f normal = new Vec3f();
        Vec3f a = new Vec3f();
        Vec3f b = new Vec3f();
         
        a.x = pos2.x - pos1.x;
        a.y = pos2.y - pos1.y;
        a.z = pos2.z - pos1.z;
         
        b.x = pos3.x - pos1.x;
        b.y = pos3.y - pos1.y;
        b.z = pos3.z - pos1.z;
         
        normal.x = (a.y * b.z) - (a.z * b.y);
        normal.y = (a.z * b.x) - (a.x * b.z);
        normal.z = (a.x * b.y) - (a.y * b.x);
         
        float length = (float) Math.sqrt((normal.x*normal.x) + (normal.y*normal.y) + (normal.z*normal.z));
         if(length == 0)//don't divide by zero
        	 length = 1.0f;
        normal.x /= length;
        normal.y /= length;
        normal.z /= length;
        return normal;
    }
}
