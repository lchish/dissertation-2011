//Taken from http://fabiensanglard.net/shadowmapping/index.php

varying vec4 ShadowCoord;
varying vec3 normal,lightDir,halfVector;
varying vec4 diffuse,ambient;

void main(){
	normal = normalize(gl_NormalMatrix * gl_Normal);
	lightDir = normalize(vec3(gl_LightSource[0].position));
	halfVector = normalize(gl_LightSource[0].halfVector.xyz);
	
	diffuse = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse;
	ambient = gl_FrontMaterial.ambient *gl_LightSource[0].ambient;
	ambient += gl_LightModel.ambient *gl_FrontMaterial.ambient;
	ShadowCoord= gl_TextureMatrix[7] * gl_Vertex;
  	//gl_Position = ftransform();
	gl_Position = gl_ModelViewProjectionMatrix  * gl_Vertex;
	gl_FrontColor = gl_Color;
	//gl_FrontColor = (NdotL * diffuse + globalAmbient + ambient) *gl_Color;
}