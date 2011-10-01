import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.*;
import java.util.Scanner;

import javax.media.opengl.GL2;

import com.jogamp.common.nio.Buffers;

public class DunedinMap {

	public int MapWidth=1237,MapHeight=881;
	private int [] vertexBuffer,colourBuffer,normalBuffer,textureBuffer;
	private int numVerticies;
	public float [][] Map;
	public BufferedImage terrain, suburbs;
	static public final float HEIGHT_SCALE = 20f;
	public DunedinMap(String heightData,String terrainData,String suburbData){
		try {
			Map = new float[MapWidth][MapHeight];
			terrain = TargaReader.getImage(terrainData);
			suburbs = TargaReader.getImage(suburbData);
			Scanner sc  = new Scanner(new File(heightData));
			
			for(int y = 0; sc.hasNextLine() && y < MapHeight;y++){
				Scanner line = new Scanner(sc.nextLine());
				for(int x = 0;line.hasNextFloat() && x <MapWidth;x++){
					float tmp = line.nextFloat();
					if((terrain.getRGB(x,y) & 0xFF) == 0xFF){
						Map[x][y] = 0f;
					}else{
						Map[x][y] = tmp/HEIGHT_SCALE;
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to open height data: " + heightData);
			e.printStackTrace();
		}
		
	}
	public void init(GL2 gl){
		createVertexBuffer(gl);
	}
	
	public boolean inWater(int x,int y){
		return (terrain.getRGB(x, y) &0xFF) == 0xFF;
	}
	public boolean inSuburb(int x,int y){
		return (terrain.getRGB(x, y) != 0);
	}
	public static int [] getPixelAsFloat(int pixel) {
		int [] ret = new int[4];
	    ret[0] = (pixel >> 16) & 0xff;//red
	    ret[1] = (pixel >> 8) & 0xff;//green
	    ret[2] = pixel & 0xff;//blue
	    ret[3] = (pixel >> 24) & 0xff;//alpha
	    return ret;
	  }
	
	public void createVertexBuffer(GL2 gl){
		numVerticies = (MapWidth -1) * (MapHeight-1)*4;
		int numFloatValues = numVerticies *3;
		int numFloatValesTex = numVerticies *2;
		vertexBuffer = new int[1];
		colourBuffer = new int[1];
		normalBuffer = new int[1];
		textureBuffer= new int[1];
		FloatBuffer points = Buffers.newDirectFloatBuffer(numFloatValues);
		FloatBuffer colours = Buffers.newDirectFloatBuffer(numFloatValues);
		FloatBuffer normals = Buffers.newDirectFloatBuffer(numFloatValues);
		FloatBuffer textureCoord = Buffers.newDirectFloatBuffer(numFloatValesTex);
		gl.glGenBuffers(vertexBuffer.length, vertexBuffer,0);
		gl.glGenBuffers(colourBuffer.length,colourBuffer,0);
		gl.glGenBuffers(normalBuffer.length,normalBuffer,0);
		gl.glGenBuffers(textureBuffer.length,textureBuffer,0);
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		
		for(int x = 0;x<MapWidth-1;x++){
			for(int z = 0;z <MapHeight-1;z++){
				float xFloat = (float)x,zFloat = (float)z;
				//bottom left vertex
				float y = Map[x][z];
				int [] tmp;
				tmp = getPixelAsFloat(suburbs.getRGB(x,z));
				float [] colour1 = {tmp[0]/255f,tmp[1]/255f,tmp[2]/255f};
				colours.put(colour1);
				float [] pos1 = {(float)x , y, zFloat};
				points.put(pos1);
				float []texPos1 = {xFloat/MapWidth, (zFloat/MapHeight)};
				textureCoord.put(texPos1);
				//top left Vertex
				y = Map[x][z+1];
				tmp = getPixelAsFloat(suburbs.getRGB(x,z));
				float [] colour2 = {tmp[0]/255f,tmp[1]/255f,tmp[2]/255f};
				colours.put(colour2);
				float [] pos2 = {(float)(x), y, zFloat+1.0f};
				points.put(pos2);
				float []texPos2 = {xFloat/MapWidth, ((zFloat+1.0f)/MapHeight)};
				textureCoord.put(texPos2);
				//top right Vertex
				y = Map[x+1][z+1];
				tmp = getPixelAsFloat(suburbs.getRGB(x,z));

				float [] colour3 = {tmp[0]/255f,tmp[1]/255f,tmp[2]/255f};
				colours.put(colour3);
				float [] pos3 = {(float)((x+1)), y, (float)((z+1))};
				points.put(pos3);
				float []texPos3 = {(xFloat+1.0f)/MapWidth, ((zFloat+1.0f)/MapHeight)};
				textureCoord.put(texPos3);
				//bottom right Vertex
				y = Map[x+1][z];
				tmp = getPixelAsFloat(suburbs.getRGB(x,z));
				float [] colour4 = {tmp[0]/255f,tmp[1]/255f,tmp[2]/255f};
				colours.put(colour4);
				float [] pos4 = {(float)(x+1), y, (float)z};
				points.put(pos4);
				float []texPos4 = {(xFloat+1.0f)/MapWidth,(zFloat/MapHeight)};
				textureCoord.put(texPos4);
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
		textureCoord.rewind();
		//send the vertex and colour data to the gfx card
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*points.capacity(), points, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colourBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*colours.capacity(), colours, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*normals.capacity(), normals, GL2.GL_STATIC_DRAW);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, textureBuffer[0]);
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, 4*textureCoord.capacity(), textureCoord, GL2.GL_STATIC_DRAW);
		//cleanup
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);
		points.clear();
		colours.clear();
		normals.clear();
		textureCoord.clear();
	}
	public void draw(GL2 gl){
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, colourBuffer[0]);
		gl.glColorPointer(3, GL2.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexBuffer[0]);
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalBuffer[0]);
		gl.glNormalPointer(GL2.GL_FLOAT, 0, 0);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, textureBuffer[0]);
		gl.glTexCoordPointer(2,GL2.GL_FLOAT,0,0);
		gl.glDrawArrays(GL2.GL_QUADS, 0, numVerticies);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
		gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,0);
	}
	public void deleteVertexBuffer(GL2 gl){
		gl.glDeleteBuffers(vertexBuffer.length, vertexBuffer, 0);
		gl.glDeleteBuffers(colourBuffer.length,colourBuffer,0);
		gl.glDeleteBuffers(normalBuffer.length,normalBuffer,0);
		gl.glDeleteBuffers(textureBuffer.length,textureBuffer,0);
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
	public int getWidth(){
		return MapWidth;
	}
	public int getHeight(){
		return MapHeight;
	}
	public double getMapHeight(int x,int y){
		return Map[x][y];
	}
}
