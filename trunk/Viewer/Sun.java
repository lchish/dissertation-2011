import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;


public class Sun {
	private float x,y,z;
	private GLU glu = new GLU();
	public Sun(float x,float y,float z){
		this.x =x; this.y=y;this.z=z;
	}
	public void draw(GL2 gl){
		gl.glPushMatrix();
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		GLUquadric quadratic = glu.gluNewQuadric();
    	glu.gluQuadricDrawStyle(quadratic, GLU.GLU_LINE);
    	glu.gluQuadricNormals(quadratic,GLU.GLU_SMOOTH);
    	gl.glTranslatef(x,y,z);
		glu.gluSphere(quadratic, 400f, 20, 20);
		
		gl.glPopMatrix();
		
	}
	public void update(){
		//move the xyz coordinates according to our time tick
	}
}
