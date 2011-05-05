import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class InputHandler implements KeyListener,MouseListener{
	private int mouseX;
	private int mouseY;
	@Override
	public void keyPressed(KeyEvent key) {
		float xrotrad, yrotrad;
		switch(key.getKeyCode()){
		case 61:
			System.out.println("Scale up");
			Renderer.scale *=1.1;
			break;
		case 45:
			System.out.println("Scale down");
			Renderer.scale /=1.1;
			break;
		case KeyEvent.VK_F:
			Renderer.wireframe = ! Renderer.wireframe;
			break;
		case KeyEvent.VK_W:
		    yrotrad = (Camera.yRot / 180 * 3.141592654f);
		    xrotrad = (Camera.xRot / 180 * 3.141592654f);
		    Camera.xPos += (float)(Math.sin(yrotrad)) ;
		    Camera.zPos -= (float)(Math.cos(yrotrad)) ;
		    Camera.yPos -= (float)(Math.sin(xrotrad)) ;
        	break;
		case KeyEvent.VK_S:
		    yrotrad = (Camera.yRot / 180 * 3.141592654f);
		    xrotrad = (Camera.xRot / 180 * 3.141592654f);
		    Camera.xPos -= (float)(Math.sin(yrotrad)) ;
		    Camera.zPos += (float)(Math.cos(yrotrad)) ;
		    Camera.yPos += (float)(Math.sin(xrotrad)) ;
        	break;
		case KeyEvent.VK_A:
			yrotrad = (Camera.yRot / 180 * 3.141592654f);
			Camera.xPos -= (float)(Math.cos(yrotrad))*0.2 ;
			Camera.zPos -= (float)(Math.sin(yrotrad))*0.2 ;
        	break;
		case KeyEvent.VK_D:
			yrotrad = (Camera.yRot / 180 * 3.141592654f);
			Camera.xPos += (float)(Math.cos(yrotrad))*0.2 ;
			Camera.zPos += (float)(Math.sin(yrotrad))*0.2 ;
        	break;
		case KeyEvent.VK_R:
			Camera.reset();
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
		Camera.xRot += (float)diffY;
		Camera.yRot += (float)diffX;
	}

}
