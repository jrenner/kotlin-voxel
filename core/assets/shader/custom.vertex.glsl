//our attributes
attribute vec3 a_position;
attribute vec3 a_normal;

struct PointLight
{
   vec3 pos;
   vec3 color;
   float intensity;
};
attribute vec2 a_textureCoords;

varying vec2 v_diffuseUV;

uniform mat4 u_projTrans;
uniform vec4 u_ambientLight;
uniform vec3 u_cameraPos;

#define NUM_POINTLIGHTS 11
uniform PointLight u_pointLights[NUM_POINTLIGHTS];

varying vec4 v_lighting;
varying float v_distFromCamera;

vec4 getPointLightColor(const PointLight ptLight) {
    vec3 pos_to_light = a_position - ptLight.pos;
    float dist = length(pos_to_light);
    pos_to_light = normalize(pos_to_light);

    float diffuse = max(0.0, dot(a_normal, -pos_to_light));

    float amb = 0.2;
    float constAtt = 0.8;
    float linearAtt = 0.04 * dist;
    float expAtt = 0.04 * dist * dist;

    float attTotal = constAtt + linearAtt + (expAtt) / ptLight.intensity;

    vec4 result = vec4(ptLight.color, 1.0) * (amb + diffuse) / attTotal;
    result.a = 1.0;
    return result;
}

vec4 applyPointLight(const PointLight ptLight, vec4 color) {
    vec4 lightCol = getPointLightColor(ptLight);
    return color + lightCol;
}

void main() {


    v_lighting = vec4(u_ambientLight.rgb, 1.0);

    for(int i = 0; i < NUM_POINTLIGHTS; i++) {
        PointLight pLight = u_pointLights[i];
        v_lighting = applyPointLight(pLight, v_lighting);
    }

    gl_Position = u_projTrans * vec4(a_position.xyz, 1.0);
    v_diffuseUV = a_textureCoords;
    v_distFromCamera = abs(length(u_cameraPos - a_position.xyz));
}
