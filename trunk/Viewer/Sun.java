import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;


public class Sun {
	public static float x,y,z;
	public static float [] ambientLight = {0f,0f,0f,1f};
	public static float [] diffuseLight = {1f,1f,1f,1f};
	public static float [] specularLight = {1f,1f,1f,1f};
	
	private GLU glu = new GLU();
	public Sun(float x,float y,float z){
		Sun.x =x; Sun.y=y;Sun.z=z;
	}
	public void draw(GL2 gl){
		gl.glPushMatrix();
		
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		GLUquadric quadratic = glu.gluNewQuadric();
    	glu.gluQuadricDrawStyle(quadratic, GLU.GLU_LINE);
    	glu.gluQuadricNormals(quadratic,GLU.GLU_SMOOTH);
    	gl.glTranslatef(x,y,z);
    	lighting(gl);
		glu.gluSphere(quadratic, 40f, 20, 20);
		gl.glPopMatrix();
	}
	private void lighting(GL2 gl) {
		float [] pos = {x,y,z,0};
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_POSITION,pos,0);
		
	}
	public void update(){
		//move the xyz coordinates according to our time tick
	}
	public void init(GL2 gl){
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_AMBIENT,ambientLight,0);
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_DIFFUSE,diffuseLight,0);
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_SPECULAR,specularLight,0);
		gl.glEnable(GL2.GL_LIGHT0);
	}
}
