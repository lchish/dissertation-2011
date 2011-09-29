import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;


public class Camera {
	public  float xRot,yRot;
	public  float xPos,yPos,zPos;

	public Camera(){
		reset();
	}
	private GLU glu = new GLU();
	public void set(GL2 gl){
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45,Renderer.RENDER_WIDTH/Renderer.RENDER_HEIGHT,10,40000);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glRotatef(xRot, 1f, 0f, 0f);
		gl.glRotatef(yRot, 0f, 1f, 0f);
		gl.glTranslatef(-xPos, -yPos, -zPos);
	}

	public void unset(GL2 gl){
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
	}
	public  void reset(){
		xRot = 20f;yRot = 27f;
		xPos = 7.7f;yPos=496f;zPos = 1505.2f;
	}
	public  void printCoordinates(){
		System.out.println("xRot: " + xRot + " yRot: " + yRot);
		System.out.println("xPos: " + xPos + " yPos: " + yPos + " zPos: " + zPos);
	}
	public void setPosition(float xPos,float yPos,float zPos,float xRot,float yRot){
		this.xPos=xPos;
		this.yPos=yPos;
		this.zPos=zPos;
		this.xRot=xRot;
		this.yRot=yRot;

	}
}
