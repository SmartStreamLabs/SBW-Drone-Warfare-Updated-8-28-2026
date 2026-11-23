#version 150
#moj_import <fog.glsl>
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;

// Alpha-correct bilinear sampling without changing the shared atlas filtering.
// Transparent guard pixels around each original frame prevent neighbour bleed.
vec4 fetch(ivec2 p) { return texelFetch(Sampler0, clamp(p, ivec2(0), textureSize(Sampler0,0)-ivec2(1)), 0); }
vec4 premultiply(vec4 c) { return vec4(c.rgb * c.a, c.a); }
void main() {
    vec2 pixel = texCoord0 * vec2(textureSize(Sampler0, 0)) - 0.5;
    ivec2 base = ivec2(floor(pixel));
    vec2 f = fract(pixel);
    vec4 a = premultiply(fetch(base));
    vec4 b = premultiply(fetch(base + ivec2(1,0)));
    vec4 c = premultiply(fetch(base + ivec2(0,1)));
    vec4 d = premultiply(fetch(base + ivec2(1,1)));
    vec4 sampleColor = mix(mix(a,b,f.x), mix(c,d,f.x), f.y);
    if (sampleColor.a > 0.0) sampleColor.rgb /= sampleColor.a;
    vec4 color = sampleColor * vertexColor * ColorModulator;
    // Native particles discard below 10% alpha; that cuts holes in fading smoke.
    if (color.a < 0.001) discard;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
