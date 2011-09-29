// Borrowed from http://fabiensanglard.net/shadowmapping/index.php

uniform sampler2D ShadowMap;

varying vec4 ShadowCoord;

varying vec3 normal,lightDir,halfVector;
varying vec4 diffuse,ambient;

void main(){
	//diffuse/specular/ambient code
	vec3 n,halfV;
	float NdotL,NdotHV;
	vec4 color = ambient;
	n = normalize(normal);
	NdotL = max(dot(n,lightDir),0.0);
	if(NdotL > 0.0){
		color += diffuse * NdotL;
		halfV = normalize(halfVector);
		NdotHV = max(dot(n,halfV),0.0);
		color += gl_FrontMaterial.specular * gl_LightSource[0].specular * pow(NdotHV, gl_FrontMaterial.shininess);
	}

	//shadowing code
	vec4 shadowCoordinateWdivide = ShadowCoord / ShadowCoord.w ;
	
	// Used to lower moiré pattern and self-shadowing
	shadowCoordinateWdivide.z += 0.0005;
	
	float distanceFromLight = texture2D(ShadowMap,shadowCoordinateWdivide.st).z;
	
	
 	float shadow = 1.0;
 	if (ShadowCoord.w > 0.0)
 		shadow = distanceFromLight < shadowCoordinateWdivide.z ? 0.5 : 1.0 ;
  	
	
  	gl_FragColor =	 shadow * color * gl_Color;
  
}
