import javax.media.opengl.GL2;


public class Camera {
	public static float xRot,yRot;
	public static float xPos,yPos,zPos;

	public Camera(){
		xRot = 32f;yRot = 13f;
		xPos = -49.5f;yPos=85.5f;zPos = 177f;
	}
	public void set(GL2 gl){
		gl.glRotatef(xRot, 1f, 0f, 0f);
		gl.glRotatef(yRot, 0f, 1f, 0f);
		gl.glTranslatef(-xPos, -yPos, -zPos);
	}
	public static void reset(){
		xRot = 32f;yRot = 13f;
		xPos = -49.5f;yPos=85.5f;zPos = 177f;
	}
	public static void printCoordinates(){
		System.out.println("xRot: " + xRot + " yRot: " + yRot);
		System.out.println("xPos: " + xPos + " yPos: " + yPos + " zPos: " + zPos);
	}
}
