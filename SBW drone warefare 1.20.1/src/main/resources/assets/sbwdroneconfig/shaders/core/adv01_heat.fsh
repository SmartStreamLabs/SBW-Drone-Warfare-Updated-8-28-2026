#version 150
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float HeatCutoff;
in vec2 texCoord0;
out vec4 fragColor;
void main(){float alpha=texture(Sampler0,texCoord0).a*ColorModulator.a;if(alpha<HeatCutoff)discard;fragColor=vec4(1.0,1.0,1.0,alpha);}
