#version 150

uniform float GameTime;

in vec2 texCoord;

out vec4 fragColor;


vec3 p(float t)
{
    vec3 a = vec3(0.4, 0.4, 0.4);
    vec3 b = vec3(0.4, 0.4, 0.4);
    vec3 c = vec3(1.0, 1.0, 1.0);
    vec3 d = vec3(0.263, 0.417, 0.557);

    return a + b * cos(
        6.28318 * (c * t + d)
    );
}


void main()
{
    /*
     * Shadertoy:
     *
     * fragCoord / iResolution
     *
     * Здесь вместо экранных координат
     * используем UV самой поверхности портала.
     */

    vec2 uv = texCoord * 2.0 - 1.0;


    float d = length(uv);

    float dt = d;


    /*
     * Minecraft GameTime:
     *
     * GameTime обычно нормализован относительно
     * длины игрового дня.
     *
     * Увеличиваем скорость для заметной анимации.
     */

    float time = GameTime * 1200.0;


    vec3 col = p(
        length(uv) + time
    );


    d = sin(
        d * 2.6 + time
    ) / 2.0;


    dt = floor(d - 2.2);

    dt = dt * tan(
        floor((d + dt) / 2.2)
    );

    d = d * dt;


    d += uv.y;

    d = abs(d);


    /*
     * Защита от деления на 0.
     */

    d = 0.2 / max(d, 0.0001);


    col *= d;


    fragColor = vec4(
        col,
        1.0
    );
}