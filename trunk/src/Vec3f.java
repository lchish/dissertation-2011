public class Vec3f{
	public float x,y,z;
	public Vec3f(float[] a){
		x=a[0];
		y=a[1];
		z=a[2];
	}
	public Vec3f(float x,float y,float z){
		this.x=x;
		this.y=y;
		this.z=z;
	}
	public Vec3f(){x=y=z=0f;};
	public float [] toFloatArray(){
		float [] ret = new float[3];
		ret[0]=x;ret[1]=y;ret[2]=z;
		return ret;
	}
	public String toString(){
		return "x: " + x +" y: " + y +" z: " + z;
	}
}