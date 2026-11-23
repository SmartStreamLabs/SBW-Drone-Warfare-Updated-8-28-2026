#version 150

#moj_import <light.glsl>
#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform sampler2D Sampler1;
uniform sampler2D Sampler2;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

uniform vec3 Light0_Direction;
uniform vec3 Light1_Direction;

out float vertexDistance;
out vec4 vertexColor;
out vec4 lightMapColor;
out vec4 overlayColor;
out vec2 texCoord0;

uniform sampler2D Sampler3;
uniform vec2 AdvLight;
uniform vec2 AdvOverlay;

void main() {
    int bone=int(Color.r*255.0+0.5)+256*int(Color.g*255.0+0.5);
    int flags=int(Color.b*255.0+0.5);
    vec3 normalFlips=vec3(float(flags & 1),float((flags >> 1) & 1),float((flags >> 2) & 1));
    mat4 transform=mat4(texelFetch(Sampler3,ivec2(0,bone),0),texelFetch(Sampler3,ivec2(1,bone),0),texelFetch(Sampler3,ivec2(2,bone),0),texelFetch(Sampler3,ivec2(3,bone),0));
    mat3 normalTransform=mat3(texelFetch(Sampler3,ivec2(4,bone),0).xyz,texelFetch(Sampler3,ivec2(5,bone),0).xyz,texelFetch(Sampler3,ivec2(6,bone),0).xyz);
    vec4 drawPosition=transform*vec4(Position,1.0);
    vec3 drawNormal=normalTransform*Normal;
    drawNormal=mix(drawNormal,abs(drawNormal),normalFlips);
    drawNormal=trunc(clamp(drawNormal,-1.0,1.0)*127.0)/127.0;

    gl_Position = ProjMat * ModelViewMat * drawPosition;

    vertexDistance = fog_distance(drawPosition.xyz, FogShape);
    vertexColor = minecraft_mix_light(Light0_Direction, Light1_Direction, drawNormal, vec4(1.0));
    lightMapColor = texelFetch(Sampler2, ivec2(AdvLight) / 16, 0);
    overlayColor = texelFetch(Sampler1, ivec2(AdvOverlay), 0);
    texCoord0 = UV0;
}
