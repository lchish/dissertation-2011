import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener,MouseListener,MouseMotionListener{
	private int mouseX=0,mouseY=0;
	private Renderer renderer;
	private Time time;
	private Camera camera;
	public InputHandler(Renderer r,Time t,Camera c){
		renderer =r;
		time = t;
		camera = c;
	}
	@Override
	public void keyPressed(KeyEvent key) {
		float xrotrad, yrotrad;
		switch(key.getKeyCode()){
		case KeyEvent.VK_F:
			renderer.wireframe = ! renderer.wireframe;
			break;
		case KeyEvent.VK_W:
		    yrotrad = (float)Math.toRadians(camera.yRot);
		    xrotrad = (float)Math.toRadians(camera.xRot);
		    camera.xPos += (float)(Math.sin(yrotrad)) ;
		    camera.zPos -= (float)(Math.cos(yrotrad)) ;
		    camera.yPos -= (float)(Math.sin(xrotrad)) ;
        	break;
		case KeyEvent.VK_S:
		    yrotrad = (float)Math.toRadians(camera.yRot);
		    xrotrad = (float)Math.toRadians(camera.xRot);
		    camera.xPos -= (float)(Math.sin(yrotrad)) ;
		    camera.zPos += (float)(Math.cos(yrotrad)) ;
		    camera.yPos += (float)(Math.sin(xrotrad)) ;
        	break;
		case KeyEvent.VK_A:
			yrotrad = (float)Math.toRadians(camera.yRot);
			camera.xPos -= (float)(Math.cos(yrotrad))*0.2 ;
			camera.zPos -= (float)(Math.sin(yrotrad))*0.2 ;
        	break;
		case KeyEvent.VK_D:
			yrotrad = (float)Math.toRadians(camera.yRot);
			camera.xPos += (float)(Math.cos(yrotrad))*0.2 ;
			camera.zPos += (float)(Math.sin(yrotrad))*0.2 ;
        	break;
		case KeyEvent.VK_R:
			camera.reset();
        	break;
		case KeyEvent.VK_P:
			camera.printCoordinates();
        	break;
		case KeyEvent.VK_EQUALS:
			time.setTimeSpeed(time.getTimeSpeed()+1);
			break;
		case KeyEvent.VK_MINUS:
			time.setTimeSpeed(time.getTimeSpeed()-1);
			break;
		case KeyEvent.VK_0:
			time.setTimeSpeed(0);
			break;
		case KeyEvent.VK_H: //go to signal hill!
			camera.setPosition(965.81104f,42.77104f,269.20132f,-1.0f,236.0f);
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent key) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent m) {
		mouseX = m.getX();
		mouseY = m.getY();
	}

	@Override
	public void mouseReleased(MouseEvent m) {
		int diffX = m.getX() - mouseX;
		int diffY = m.getY() - mouseY;
		camera.xRot += (float)diffY;
		camera.yRot += (float)diffX;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent m) {
		float diffX = (m.getX() - mouseX) *1.1f;
		float diffY = (m.getY() - mouseY) * .1f;
		System.out.println(diffX);
		mouseX = m.getX();
		mouseY = m.getY();
		camera.xRot += (float)diffY;
		camera.yRot += (float)diffX;
		
	}

}
