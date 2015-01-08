#ifdef GL_ES
precision mediump float;
#endif

//input from vertex shader
varying vec4 v_lighting;

uniform sampler2D u_diffuseTexture;
uniform float u_maxViewDist;
uniform vec4 u_fogColor;

varying vec2 v_diffuseUV;
varying float v_distFromCamera;


void main() {
    float distRatio = clamp(v_distFromCamera / u_maxViewDist, 0.0, 1.0);
    float fogIntensity = u_fogColor.a * (distRatio * distRatio);
    vec3 fogBaseColor = u_fogColor.rgb;
    vec4 litDiffuse = v_lighting * texture2D(u_diffuseTexture, v_diffuseUV.st);
    gl_FragColor.rgb = (fogBaseColor * fogIntensity) + (litDiffuse.rgb * (1.0 - fogIntensity));
    gl_FragColor.a = litDiffuse.a;
}