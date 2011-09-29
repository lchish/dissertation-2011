uniform sampler2D AggregatorMap;

void main(){
	vec4 texColour = texture2D(AggregatorMap,vec2(gl_TexCoord[0]));
  	gl_FragColor =	 texColour  ;//gl_Color*ambient;
}
