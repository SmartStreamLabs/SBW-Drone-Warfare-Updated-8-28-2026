#version 150
uniform sampler2D Sampler0;
uniform int UseTexture;
uniform float AlphaCutoff;
in vec2 texCoord;
in float vertexAlpha;
out vec4 fragColor;
void main(){float alpha=vertexAlpha;if(UseTexture!=0)alpha*=texture(Sampler0,texCoord).a;if(alpha<AlphaCutoff)discard;fragColor=vec4(1.0);}
