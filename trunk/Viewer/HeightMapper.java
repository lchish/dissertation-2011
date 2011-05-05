import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
		Renderer r  = new Renderer(new File("dunedin.txt"));
		GLProfile glprofile = GLProfile.getDefault();
		GLCapabilities glcapabilities = new GLCapabilities( glprofile );
		GLCanvas glcanvas = new GLCanvas( glcapabilities );
		InputHandler i = new InputHandler();
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
        jframe.setSize( 1280, 800 );
        anim.start();
        jframe.setVisible( true );
	}

}
