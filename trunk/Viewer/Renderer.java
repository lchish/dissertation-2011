import java.io.File;


import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;


public class Renderer implements GLEventListener{
	private GLU glu = new GLU();
	DunedinMap dunedinMap;
	Sun sun;
	Camera camera;
	public static float scale = 0.01f;
	public static boolean wireframe = false;
	public Renderer(File f){
		dunedinMap = new DunedinMap(f);
		sun = new Sun(0,100000,0);
		camera = new Camera();
	}
	public void update(){
	}
	@Override
	public void display(GLAutoDrawable drawable) {
		update();
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();                                 // Reset The Matrix
		camera.set(gl);
		//glu.gluLookAt(212f, 60f, 194f, 186f, 20f, 171f, 0, 1, 0);
		gl.glScalef(scale, scale*2.0f, scale);
		if(wireframe)
			gl.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_LINE );
		else
			gl.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_FILL );
		dunedinMap.draw(gl);
		sun.draw(gl);
	}

	@Override
	public void dispose(GLAutoDrawable arg0) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glShadeModel(GL2.GL_SMOOTH);             // Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);   // Black Background
		gl.glClearDepth(1.0f);                     // Depth Buffer Setup
		gl.glEnable(GL.GL_DEPTH_TEST);             // Enables Depth Testing
		gl.glDepthFunc(GL.GL_LEQUAL);              // The Type Of Depth Testing To Do

		// Really Nice Perspective Calculations
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

	}

	@Override
	public void reshape(GLAutoDrawable drawable, int xstart, int ystart, int width,
			int height) {
		GL2 gl = drawable.getGL().getGL2();

        height = (height == 0) ? 1 : height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(45, (float) width / height, 1, 1000);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();		
	}

}
