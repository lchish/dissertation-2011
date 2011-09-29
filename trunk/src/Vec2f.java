
public class Vec2f {
	public float x,y;
	public Vec2f(float[] a){
		x=a[0];
		y=a[1];
	}
	public Vec2f(float x,float y){
		this.x=x;
		this.y=y;
	}
	public Vec2f(){x=y=0f;};
	public float [] toFloatArray(){
		float [] ret = new float[2];
		ret[0]=x;ret[1]=y;
		return ret;
	}
	public String toString(){
		return "x: " + x +" y: " + y +" z: ";
	}
}

