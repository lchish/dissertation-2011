import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.gl2.GLUgl2;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.awt.Overlay;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;


public class Renderer implements GLEventListener{
	private GLUgl2 glu = new GLUgl2();
	private DunedinMap dunedinMap;
	private Sun sun;
	private Camera camera;
	private int [] frameBufferID = new int[1];
	private int [] depthTextureID = new int[1];
	private int [] aggregatorTextureID = new int[1];
	private int shadowMapUniform,aggregatorShadowMapUniform;
	private int shaderId,shaderIdAggregator;
	public static float scale = 0.1f;
	public boolean wireframe = false,useShadowMapping = true;
	public static final int RENDER_WIDTH = 1024,RENDER_HEIGHT = 768,SHADOW_MAP_RATIO = 8;
	public Overlay overlay;
	private Time time;
	//private Texture aggregatorTexture;
	private Aggregator aggregator;
	private boolean useAggregator = false,drawAggregatorShadows=false;
	ByteBuffer textureBuffer;
	
	
	public Renderer(String heightData,String terrainData,String suburbData, Time time){
		dunedinMap = new DunedinMap(heightData,terrainData,suburbData);
		sun = new Sun(time);
		camera = new Camera();
		this.time = time;
		aggregator = new Aggregator(this.time);
		textureBuffer = Buffers.newDirectByteBuffer((dunedinMap.MapHeight-1)*(dunedinMap.MapWidth-1)*3);
	}
	public void update(){
		time.update(sun);
		sun.updateNew(time);
		if(useAggregator)
			aggregator.run();
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
		gl.glColor3f(1.0f,1.0f,1.0f);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0,RENDER_WIDTH,0,RENDER_HEIGHT,1,20);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glActiveTexture(GL2.GL_TEXTURE0);
		gl.glTranslated(0,0,-1);
		TextRenderer t =new TextRenderer(new Font("Ariel",Font.BOLD,9));
		t.draw(time.toString(),RENDER_WIDTH-300 , RENDER_HEIGHT-50);
		double [] tmp = sun.getAzimuthElevation();
		t.draw("Azimuth: " + tmp[0]*(180/Math.PI) + " Elevation: " + tmp[1]*(180/Math.PI), RENDER_WIDTH-300 , RENDER_HEIGHT-60);
		t.flush();
	}
	@Override
	public void display(GLAutoDrawable drawable) {
		update();

		GL2 gl = drawable.getGL().getGL2();

		if(useShadowMapping){
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
		}else{ //use the aggregator to create shadows
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			gl.glUseProgramObjectARB(shaderIdAggregator);
			gl.glUniform1i(aggregatorShadowMapUniform,1);
			gl.glActiveTexture(GL2.GL_TEXTURE1);
			textureFromAggregator(gl);
			gl.glBindTexture(GL2.GL_TEXTURE_2D,aggregatorTextureID[0]);
		}
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
		sun.lighting(gl);
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
		String vertexShaderAggregator = ShaderUtils.loadShaderSourceFileAsString("shaders/vertex_shader_aggregator.glsl");
		String fragmentShaderAggregator = ShaderUtils.loadShaderSourceFileAsString("shaders/fragment_shader_aggregator.glsl");
		
		int v = ShaderUtils.generateVertexShader(gl, vertexShader);
		int f = ShaderUtils.generateFragmentShader(gl, fragmentShader);
		int v2 = ShaderUtils.generateVertexShader(gl, vertexShaderAggregator);
		int f2 = ShaderUtils.generateFragmentShader(gl, fragmentShaderAggregator);
		
		int shaderProgram = gl.glCreateProgram();
		gl.glAttachShader(shaderProgram, v);
		gl.glAttachShader(shaderProgram, f);
		gl.glLinkProgram(shaderProgram);
		
		shadowMapUniform = gl.glGetUniformLocationARB(shaderProgram,"ShadowMap");

		gl.glValidateProgram(shaderProgram);
		shaderId = shaderProgram;
		
		int shaderProgramAggregator = gl.glCreateProgram();
		gl.glAttachShader(shaderProgramAggregator, v2);
		gl.glAttachShader(shaderProgramAggregator, f2);
		gl.glLinkProgram(shaderProgramAggregator);
		aggregatorShadowMapUniform = gl.glGetUniformLocation(shaderProgramAggregator, "AggregatorMap");
		gl.glValidateProgram(shaderProgramAggregator);
		shaderIdAggregator = shaderProgramAggregator;
	}

	/*private void textureFromAggregatorOld(GL2 gl){
		int width = dunedinMap.MapWidth, height =dunedinMap.MapHeight;
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, aggregatorTextureID[0]);
		
		int numTexels = width * height;
		IntBuffer textureBuffer = Buffers.newDirectIntBuffer(numTexels*3);
		for(int x = 0;x<dunedinMap.MapWidth;x++){
			for(int y=0;y<dunedinMap.MapHeight;y++){
				//System.out.println(aggregator.stepData[x][y]?0:255);
				textureBuffer.put(aggregator.stepData[x][y]?0:1);
				textureBuffer.put(aggregator.stepData[x][y]?0:1);
				textureBuffer.put(aggregator.stepData[x][y]?0:1);
			}
		}
		textureBuffer.rewind();
		gl.glTexImage2D(GL2.GL_TEXTURE_2D,0,GL2.GL_RGB,width,height,0,GL2.GL_RGB,GL2.GL_INT,textureBuffer);
		//glu.gluBuild2DMipmaps( GL2.GL_TEXTURE_2D, 3, width, height,GL2.GL_RGB, GL2.GL_INT, textureBuffer);
	}*/
	private void textureFromAggregator(GL2 gl){
		int width = dunedinMap.MapWidth-1, height =dunedinMap.MapHeight-1;//have to reduce it by 1 to make the numbers even
		
		gl.glBindTexture(GL2.GL_TEXTURE_2D, aggregatorTextureID[0]);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
		
		//int size = width*height*3;
		//textureBuffer = Buffers.newDirectByteBuffer(size);
		if(drawAggregatorShadows || aggregator.steps == 0){
			for(int y=0;y<height;y++){
				for(int x = 0;x<width;x++){
					for(int z = 0 ;z<3;z++){
						textureBuffer.put((byte) (aggregator.stepData[x][y]? 0:0xFF));
					}
				}
			}
		}else{
			for(int y=0;y<height;y++){
				for(int x = 0;x<width;x++){
						for(int z = 0 ;z<3;z++){
							textureBuffer.put((byte) (aggregator.totalData[x][y]));
						}
					}
				}
		}
		textureBuffer.rewind();
		gl.glTexImage2D(GL2.GL_TEXTURE_2D,0,3,width,height,0,GL2.GL_BGR,GL2.GL_UNSIGNED_BYTE,textureBuffer);
		textureBuffer.clear();
	}
	
	/*public void createAggregatorTexture(GL2 gl){
		//loadTextureTGA("aggregator.tga", false, gl);
		try {
			aggregatorTexture = TextureIO.newTexture(new File("aggregator.tga"),false);
		} catch (GLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		//createAggregatorTexture(gl);
		generateShadowFBO(gl);
		initShaders(gl);
		gl.glGenTextures(1, aggregatorTextureID, 0);
		
		gl.glShadeModel(GL2.GL_SMOOTH);             // Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1f);   // Black Background
		gl.glClearDepth(1.0f);                     // Depth Buffer Setup
		gl.glEnable(GL.GL_DEPTH_TEST);             // Enables Depth Testing
		gl.glDepthFunc(GL.GL_LEQUAL);              // The Type Of Depth Testing To Do
		gl.glEnable(GL.GL_CULL_FACE);
		gl.glEnable(GL.GL_TEXTURE_2D);
		gl.glColorMaterial( GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE ) ;
		gl.glEnable ( GL2.GL_COLOR_MATERIAL ) ;
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		dunedinMap.init(gl);
		sun.init(gl);
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
