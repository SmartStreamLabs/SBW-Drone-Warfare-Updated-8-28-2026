#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform sampler2D Sampler3;
in vec2 texCoord;
out vec4 fragColor;
void main(){
 vec4 scene=texture(Sampler0,texCoord);
 float luminance=dot(scene.rgb,vec3(0.2126,0.7152,0.0722));
 float gray=clamp(0.025+0.48*pow(max(luminance,0.0),0.65),0.025,0.55);
 float entityDepth=texture(Sampler3,texCoord).r;
 float worldDepth=texture(Sampler2,texCoord).r;
 bool visible=texture(Sampler1,texCoord).r>0.5&&entityDepth<1.0&&entityDepth<=worldDepth+0.0000003;
 fragColor=vec4(vec3(visible?0.98:gray),scene.a);
}
