#version 150
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform float Intensity;
uniform float Lost;
uniform float Frozen;
uniform float Clock;
uniform float Seed;
uniform vec2 Resolution;
in vec2 texCoord;
out vec4 fragColor;
float noise(vec2 p){uvec2 v=uvec2(ivec2(floor(p*16.0)));uint h=v.x*1664525u+v.y*1013904223u;h^=h>>16u;h*=2246822519u;h^=h>>13u;return float(h&0x00ffffffu)/16777216.0;}
vec2 bounded(vec2 uv){vec2 edge=vec2(.5)/Resolution;return clamp(uv,edge,vec2(1)-edge);}
void main(){
    float t=floor(Clock*20.0),i=clamp(Intensity,0.0,1.0);
    vec2 pixel=floor(texCoord*Resolution);
    float grain=noise(pixel+vec2(t*37.0+Seed,t*19.0));
    if(Lost>.5){float band=noise(vec2(floor(pixel.y/3.0),t+Seed));fragColor=vec4(vec3(.018+grain*.105+band*.025),1);return;}
    float stripe=floor(texCoord.y*71.0+noise(vec2(t,Seed))*5.0);
    float event=noise(vec2(stripe+Seed,t));
    float gate=step(1.0-(.018+.19*i)*i,event);
    float shift=(noise(vec2(stripe*3.1,t+Seed))-.5)*(.004+.072*i*i)*gate;
    float roll=fract(texCoord.y+Clock*(.19+noise(vec2(floor(Clock),Seed))*.22));
    float rollBand=(1.0-smoothstep(0.0,.018,abs(roll-.46)));
    shift+=rollBand*.014*i*sin(t+Seed);
    vec2 uv=bounded(texCoord+vec2(shift,0));
    float channel=(.35+2.2*i*i)*i*i/Resolution.x;
    vec3 image;
    if(Frozen>.5)image=vec3(texture(Sampler1,bounded(uv+vec2(channel,0))).r,texture(Sampler1,uv).g,texture(Sampler1,bounded(uv-vec2(channel,0))).b);
    else image=vec3(texture(Sampler0,bounded(uv+vec2(channel,0))).r,texture(Sampler0,uv).g,texture(Sampler0,bounded(uv-vec2(channel,0))).b);
    float luma=dot(image,vec3(.2126,.7152,.0722));image=mix(image,vec3(luma),i*i*.38);image=(image-.5)*(1.0-i*i*.21)+.5;
    float fineLine=noise(vec2(floor(pixel.y/2.0),t+Seed))-.5;
    image+=fineLine*.13*i+gate*(event>.97?-.20:.10)*i-rollBand*.20*i;
    image=mix(image,vec3(grain),(.035*i+.34*i*i)*(.65+.35*gate));
    float dip=step(.95,noise(vec2(t,Seed+41.0)))*.12*i*i;
    image*=1.0-.055*i-dip;
    fragColor=vec4(clamp(image,0.0,1.0),1);
}
