import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.awt.Overlay;
import com.jogamp.opengl.util.awt.TextRenderer;


public class Renderer implements GLEventListener{
	private GLU glu = new GLU();
	private DunedinMap dunedinMap;
	private Sun sun;
	private Camera camera;
	private int [] frameBufferID = new int[1];
	private int [] depthTextureID = new int[1];
	private int shadowMapUniform;
	private int shaderId;
	public static float scale = 0.1f;
	public boolean wireframe = false;
	public static final int RENDER_WIDTH = 1024,RENDER_HEIGHT = 768,SHADOW_MAP_RATIO = 8;
	public Overlay overlay;
	private Time time;
	
	public Renderer(String heightData,String terrainData,String suburbData, Time time){
		dunedinMap = new DunedinMap(heightData,terrainData,suburbData);
		sun = new Sun(time);
		camera = new Camera();
		this.time = time;
	}
	public void update(){
		//sun.update(dunedinMap);
		time.update();
		sun.updateNew(time);
	}
	
	void setupMatrices(GL2 gl,float position_x,float position_y,float position_z,float lookAt_x,float lookAt_y,float lookAt_z){
	  gl.glMatrixMode(GL2.GL_PROJECTION);
	  gl.glLoadIdentity();
	  glu.gluPerspective(45,RENDER_WIDTH/RENDER_HEIGHT,10,40000);//change this to orthographic for the sun!
	  gl.glMatrixMode(GL2.GL_MODELVIEW);
	  gl.glLoadIdentity();
	  glu.gluLookAt(position_x,position_y,position_z,lookAt_x,lookAt_y,lookAt_z,0,1,0);
	}
	void setupMatricesOrtho(GL2 gl,float position_x,float position_y,float position_z,float lookAt_x,float lookAt_y,float lookAt_z){
		  gl.glMatrixMode(GL2.GL_PROJECTION);
		  gl.glLoadIdentity();
		  gl.glOrtho(-1000.0f, 1000.0f, -1000.0f, 1000.0f, 10, 40000);
		  gl.glMatrixMode(GL2.GL_MODELVIEW);
		  gl.glLoadIdentity();
		  glu.gluLookAt(position_x,position_y,position_z,lookAt_x,lookAt_y,lookAt_z,0,1,0);
		}
	void setupMatricesCamera(GL2 gl){
	  gl.glMatrixMode(GL2.GL_PROJECTION);
	  gl.glLoadIdentity();
	  glu.gluPerspective(45,RENDER_WIDTH/RENDER_HEIGHT,10,40000);
	  gl.glMatrixMode(GL2.GL_MODELVIEW);
	  gl.glLoadIdentity();
	}
	
	private void drawData(GL2 gl){
		gl.glUseProgramObjectARB(0);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0,RENDER_WIDTH,0,RENDER_HEIGHT,1,20);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		//gl.glColor4f(1,1,1,1);
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTranslated(0,0,-1);
		TextRenderer t =new TextRenderer(new Font("Ariel",Font.BOLD,9));
		t.draw(time.toString(),RENDER_WIDTH-300 , RENDER_HEIGHT-50);
		double [] tmp = sun.getAzimuthElevation();
		t.draw("Azimuth: " + tmp[0]*(180/Math.PI) + " Elevation: " + tmp[1]*(180/Math.PI), RENDER_WIDTH-300 , RENDER_HEIGHT-60);
		t.flush();
		gl.glDisable(GL2.GL_TEXTURE_2D);
	}
	@Override
	public void display(GLAutoDrawable drawable) {
		
		
		update();
		
		GL2 gl = drawable.getGL().getGL2();
		
		
		//First step: Render from the light POV to a FBO, store depth values only
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER,frameBufferID[0]);	//Rendering offscreen		
		gl.glUseProgramObjectARB(0); //use the fixed function pipeline

		// In the case we render the shadowmap to a higher resolution, the viewport must be modified accordingly.
		gl.glViewport(0,0,RENDER_WIDTH * SHADOW_MAP_RATIO,RENDER_HEIGHT* SHADOW_MAP_RATIO);

		// Clear previous frame values
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glColorMask(false, false, false, false);
		setupMatricesOrtho(gl,sun.getX(),sun.getY(),sun.getZ(),1237/2,0,883/2);
		
		gl.glCullFace(GL2.GL_FRONT);
		// Culling switching, rendering only backface, this is done to avoid self-shadowing
		dunedinMap.draw(gl);
		//Save modelview/projection matrices into texture7, also add a bias
		setTextureMatrix(gl);
		
		// Now rendering from the camera POV, using the FBO to generate shadows
		gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER,0);
		gl.glViewport(0,0,RENDER_WIDTH,RENDER_HEIGHT);
		
		//Enabling color write (previously disabled for light POV z-buffer rendering)
		gl.glColorMask(true,true,true,true); 

		// Clear previous frame values
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

		//Using the shadow shader
		gl.glUseProgramObjectARB(shaderId);
		gl.glUniform1i(shadowMapUniform,7);
		gl.glActiveTexture(GL2.GL_TEXTURE7);
		gl.glBindTexture(GL2.GL_TEXTURE_2D,depthTextureID[0]);

		//setupMatrices(gl,Sun.x,Sun.y,Sun.z,1237/2,0,883/2);
		//setupMatrices(gl,7.7f,496f,1505.2f,1237/2,0,883/2);
		//setupMatricesOrtho(gl,sun.getX(),sun.getY(),sun.getZ(),1237/2,0,883/2);
		//setupMatrices(gl,0f,8960f,-1000f,0,0,0);
		//setupMatricesCamera(gl);
		camera.set(gl);
		gl.glCullFace(GL.GL_BACK);

		gl.glPushMatrix();
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3f(1f, 0f, 0f);
		gl.glVertex3f(0, 20, -100000);
		gl.glVertex3f(0, 20, 100000);
		gl.glEnd();
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(-100, 0, 1900);
		gl.glVertex3f(-100, 0, 2100);
		gl.glVertex3f(100, 0, 2100);
		gl.glVertex3f(100, 0, 1900);
		gl.glEnd();
		gl.glBegin(GL2.GL_LINES);
		gl.glColor3f(0f, 1f, 0f);
		gl.glVertex3f(-10000, 20, 0);
		gl.glVertex3f(10000, 20, 0);
		gl.glEnd();
		gl.glBegin(GL2.GL_QUADS);
		gl.glVertex3f(1400, 0, -100);
		gl.glVertex3f(1600, 0, -100);
		gl.glVertex3f(1600, 0, 100);
		gl.glVertex3f(1400, 0, 100);
		gl.glEnd();
		gl.glScalef(100f, 100f, 100f);
		gl.glPopMatrix();
		
		if(wireframe)
			gl.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_LINE );
		else
			gl.glPolygonMode( GL2.GL_FRONT_AND_BACK, GL2.GL_FILL );
		sun.draw(gl);
		dunedinMap.draw(gl);
		drawData(gl);
		
	}
	
	@Override
	public void dispose(GLAutoDrawable drawable) {
		dunedinMap.deleteVertexBuffer(drawable.getGL().getGL2());
	}

	public void generateShadowFBO(GL2 gl){
		  int shadowMapWidth = (int)(RENDER_WIDTH * SHADOW_MAP_RATIO);
		  int shadowMapHeight = (int)(RENDER_HEIGHT*SHADOW_MAP_RATIO);
		  
		  // Try to use a texture depth component
		  gl.glGenTextures(1, depthTextureID,0);
		  gl.glBindTexture(GL2.GL_TEXTURE_2D, depthTextureID[0]);
		  
		  // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF
		  gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST);
		  gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_NEAREST);

		  // Remove artifact on the edges of the shadowmap
		  gl.glTexParameterf( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP );
		  gl.glTexParameterf( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP );
		  
		// No need to force GL_DEPTH_COMPONENT24, drivers usually give you the max precision if available
		  gl.glTexImage2D( GL2.GL_TEXTURE_2D, 0, GL2.GL_DEPTH_COMPONENT, shadowMapWidth, shadowMapHeight, 0, GL2.GL_DEPTH_COMPONENT, GL2.GL_UNSIGNED_BYTE, null);
		  gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);

		  // create a framebuffer object
		  gl.glGenFramebuffers(1, frameBufferID,0);
		  gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, frameBufferID[0]);

		  // Instruct openGL that we won't bind a color texture with the currently binded FBO
		  gl.glDrawBuffer(GL2.GL_NONE);
		  gl.glReadBuffer(GL2.GL_NONE);
		  
		// attach the texture to FBO depth attachment point
		  gl.glFramebufferTexture2D(GL2.GL_FRAMEBUFFER, GL2.GL_DEPTH_ATTACHMENT,GL2.GL_TEXTURE_2D, depthTextureID[0], 0);

		  // check FBO status
		  int FBOStatus = gl.glCheckFramebufferStatus(GL2.GL_FRAMEBUFFER);
		  if(FBOStatus != GL2.GL_FRAMEBUFFER_COMPLETE)
			  System.err.println("Warning: GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
		  // switch back to window-system-provided framebuffer
		  gl.glBindFramebuffer(GL2.GL_FRAMEBUFFER, 0);
		  
	}
	
	void setTextureMatrix(GL2 gl){
		double [] modelView = new double[16];
		double [] projection = new double[16];
		
		double [] bias = {	
			0.5, 0.0, 0.0, 0.0, 
			0.0, 0.5, 0.0, 0.0,
			0.0, 0.0, 0.5, 0.0,
		    0.5, 0.5, 0.5, 1.0};
		
		// Grab modelview and transformation matrices
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelView,0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection,0);
		
		
		gl.glMatrixMode(GL2.GL_TEXTURE);
		gl.glActiveTexture(GL2.GL_TEXTURE7);
		
		gl.glLoadIdentity();	
		gl.glLoadMatrixd(bias,0);
		
		// concatenating all matrices into one.
		gl.glMultMatrixd (projection,0);
		gl.glMultMatrixd (modelView,0);
		
		// Go back to normal matrix mode
		gl.glMatrixMode(GL2.GL_MODELVIEW);
	}	
	
	public void initShaders(GL2 gl){
		String vertexShader = ShaderUtils.loadShaderSourceFileAsString("shaders/vertex_shader.glsl");
		String fragmentShader = ShaderUtils.loadShaderSourceFileAsString("shaders/fragment_shader.glsl");
		int v = ShaderUtils.generateVertexShader(gl, vertexShader);
		int f = ShaderUtils.generateFragmentShader(gl, fragmentShader);
		
		int shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, v);
		gl.glAttachShader(shaderProgram, f);
		gl.glLinkProgram(shaderProgram);
		
		shadowMapUniform = gl.glGetUniformLocationARB(shaderProgram,"ShadowMap");
		
		gl.glValidateProgram(shaderProgram);
		shaderId = shaderProgram;
	}
	
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		generateShadowFBO(gl);
		initShaders(gl);
		
		gl.glShadeModel(GL2.GL_SMOOTH);             // Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);   // Black Background
		gl.glClearDepth(1.0f);                     // Depth Buffer Setup
		gl.glEnable(GL.GL_DEPTH_TEST);             // Enables Depth Testing
		gl.glDepthFunc(GL.GL_LEQUAL);              // The Type Of Depth Testing To Do
		gl.glEnable(GL.GL_CULL_FACE);
		
		gl.glColorMaterial( GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE ) ;
		gl.glEnable ( GL2.GL_COLOR_MATERIAL ) ;
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		dunedinMap.init(gl);
		sun.init(gl);
		//dunedinMap.rayLighting(sun);
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
public Camera getCamera(){
	return camera;
}
}
