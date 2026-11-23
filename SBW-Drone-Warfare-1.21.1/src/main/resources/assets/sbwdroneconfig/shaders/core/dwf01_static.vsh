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

uniform mat4 DwfPose;
uniform mat3 DwfNormal;
uniform float DwfAssemblyStage;
uniform float DwfEmissive;
uniform vec2 AdvLight;
uniform vec2 AdvOverlay;

void main() {
    vec4 drawPosition=DwfPose*vec4(Position,1.0);
    vec3 drawNormal=DwfNormal*Normal;
    drawNormal=mix(drawNormal,abs(drawNormal),Color.gba);
    drawNormal=trunc(clamp(drawNormal,-1.0,1.0)*127.0)/127.0;

    gl_Position = Color.r*255.0>DwfAssemblyStage+.5 ? vec4(2.0,2.0,2.0,1.0) : ProjMat * ModelViewMat * drawPosition;

    vertexDistance = fog_distance(drawPosition.xyz, FogShape);
    vertexColor = mix(minecraft_mix_light(Light0_Direction, Light1_Direction, drawNormal, vec4(1.0)),vec4(1.0),DwfEmissive);
    lightMapColor = mix(texelFetch(Sampler2, ivec2(AdvLight) / 16, 0),vec4(1.0),DwfEmissive);
    overlayColor = mix(texelFetch(Sampler1, ivec2(AdvOverlay), 0),vec4(0.0,0.0,0.0,1.0),DwfEmissive);
    texCoord0 = UV0;
}
