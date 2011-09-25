import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.jogamp.opengl.util.Animator;


public class HeightMapper {

	static {
		// setting this true causes window events not to get sent on Linux if you run from inside Eclipse
		GLProfile.initSingleton( false );
	}
	public static void main(String [] args){
		if(args.length == 1){
			//use the text file for input reading?
		}
		Time time = new Time();
		Renderer r  = new Renderer("dunedin.txt","terrain.tga","suburbs.tga",time);
		GLProfile glprofile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities( glprofile );
		GLCanvas glcanvas = new GLCanvas( glcapabilities );
		InputHandler i = new InputHandler(r,time,r.getCamera());
		glcanvas.addGLEventListener(r);
		glcanvas.addKeyListener(i);
		glcanvas.addMouseListener(i);
		final Animator anim = new Animator(glcanvas);
		final JFrame jframe = new JFrame( "Dunedin!" ); 
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) {
                jframe.dispose();
                System.exit( 0 );
            }
        });
        
        jframe.getContentPane().add( glcanvas, BorderLayout.CENTER );
        jframe.setSize( 1024, 768 );
        anim.start();
        jframe.setVisible( true );
	}

}
