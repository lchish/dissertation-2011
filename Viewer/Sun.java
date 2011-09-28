import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;


public class Sun {
	public static final float MaxHeight = 1000f;
	public static final float MaxDistance = 2000f;
	public static final double latitude = -45.67778,longitude=170.5;
	public static float [] ambientLight = {0f,0f,0f,1f};
	public static float [] diffuseLight = {1f,1f,1f,1f};
	public static float [] specularLight = {1f,1f,1f,1f};
	SunAzimuthElevation sunAE;
	private float x,y,z;
	private double elevation;
	
	private GLU glu = new GLU();
	
	public Sun(Time t){
		updateNew(t);
	}

	public void draw(GL2 gl){
		gl.glPushMatrix();
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		GLUquadric quadratic = glu.gluNewQuadric();
    	glu.gluQuadricDrawStyle(quadratic, GLU.GLU_LINE);
    	glu.gluQuadricNormals(quadratic,GLU.GLU_SMOOTH);
    	gl.glTranslatef(x,y,z);
		glu.gluSphere(quadratic, 40f, 20, 20);
		gl.glPopMatrix();
	}
	public void lighting(GL2 gl) {
		float [] pos = {x,y,z,0};
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_POSITION,pos,0);
	}

	public void updateNew(Time t){
		SunAzimuthElevation sae = RedShift.getSunAzimuthElevation(t.getTime(), latitude, longitude);
		sunAE = sae;
		//System.out.println(sae);
		elevation = RedShift.RAD(sae.getElevation());
		double azimuth = RedShift.RAD(sae.getAzimuth());
		double z = -1000f;
		double x = -z*Math.tan(azimuth);
		double y = -z*Math.tan(elevation);
		//System.out.println("x: " + x +" y: " +y + " z: " + z);
		this.x = (float) x;
		this.y=(float) y;
		this.z=(float) z;
	}
	
	public void init(GL2 gl){
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_AMBIENT,ambientLight,0);
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_DIFFUSE,diffuseLight,0);
		gl.glLightfv(GL2.GL_LIGHT0,GL2.GL_SPECULAR,specularLight,0);
		gl.glEnable(GL2.GL_LIGHT0);
	}
	public Vec3f getPos(){
		return new Vec3f(x,y,z);
	}
	public double [] getAzimuthElevation(){
		double []ret = {RedShift.RAD(sunAE.getAzimuth()),RedShift.RAD(sunAE.getElevation())};
		return ret;
	}
	public Vec3f getDirectionUnitVector(){
		float length = (float)Math.sqrt((x*x) + (y*y) + (z*z));
		return new Vec3f(x/length,y/length,z/length);
	}
	public void printCoordinates(){
		System.out.println("sun x: " + x + "sun y:" + y + "sun z" + z);
	}
	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
	public float getZ() {
		return z;
	}
	public double getElevation() {
		return elevation;
	}

}
