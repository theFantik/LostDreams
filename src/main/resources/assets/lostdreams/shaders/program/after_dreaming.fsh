#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;


uniform float Saturation;  // > 1.0 = перенасыщение
uniform float Contrast;    // > 1.0 = больше контраста


vec3 rgb2hsv(vec3 c) {
    vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
    vec4 p = mix(vec4(c.bg, K.wz),
                 vec4(c.gb, K.xy),
                 step(c.b, c.g));
    vec4 q = mix(vec4(p.xyw, c.r),
                 vec4(c.r, p.yzx),
                 step(p.x, c.r));
    float d = q.x - min(q.w, q.y);
    float e = 1.0e-10;
    return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)),
                d / (q.x + e),
                q.x);
}

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    // Берём исходный цвет пикселя
    vec4 color = texture(DiffuseSampler, texCoord);

    // === SATURATION (насыщенность) ===
    vec3 hsv = rgb2hsv(color.rgb);
    // Выкручиваем насыщенность вверх
    hsv.y = clamp(hsv.y * Saturation, 0.0, 1.0);
    vec3 saturated = hsv2rgb(hsv);

    // === CONTRAST (контраст) ===
    // Формула: (color - 0.5) * contrast + 0.5
    vec3 contrasted = (saturated - vec3(0.5)) * Contrast + vec3(0.5);

    // === ЦВЕТОВОЙ СДВИГ (лёгкий фиолетовый оттенок) ===
    // Добавляем чуть фиолетового для атмосферы
    float dreamTint = (Saturation - 1.0) * 0.08;
    contrasted.r += dreamTint * 0.6;
    contrasted.b += dreamTint * 1.2;
    contrasted.g -= dreamTint * 0.2;

    // Финальный цвет с ограничением диапазона
    fragColor = vec4(clamp(contrasted, 0.0, 1.0), color.a);
}
