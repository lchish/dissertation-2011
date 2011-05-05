import javax.media.opengl.GL2;


public class Camera {
	public static float xRot,yRot;
	public static float xPos,yPos,zPos;
	public Camera(){
	}
	public void set(GL2 gl){
		gl.glRotatef(xRot, 1f, 0f, 0f);
		gl.glRotatef(yRot, 0f, 1f, 0f);
		gl.glTranslatef(-xPos, -yPos, -zPos);
	}
	public static void reset(){
		xRot=yRot=xPos=yPos=zPos=0;
	}
}
