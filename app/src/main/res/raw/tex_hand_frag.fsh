precision highp float;
uniform sampler2D u_texture;

#define PI 3.14159265359
#define TPI 6.28318530718
#define HPI 1.57079632679

@MATERIAL_UNIFORMS

layout ( location = 0 ) in vec2 diffuse_coord;

out vec4 outColor;

void main()
{

   vec2 uv = diffuse_coord.xy;

   //set position (x, y) in vec2(x, y)
   vec2 p = uv - vec2(.5, .5);

   float time =  u_time - 0.8;

   float angle = 0.5 * PI;
   mat2 rot = mat2(cos(angle),sin(angle),-sin(angle),cos(angle));

   p = rot * p;

   vec3 col = vec3(0.);
   float L = length(p);
   float f = 0.;

   //External radius size
   f = smoothstep(L-.005, L, .5);

   float t = mod(time,TPI) - PI;
   float t1 = -PI ;
   float t2 = sin(t) *  (PI - .25) ;

   float a = atan(p.y,p.x)  ;
   f = f * step(a,t2) * (1.-step(a,t1)) ;

   vec4 color = texture(u_texture, diffuse_coord);

   // Color RGBA
   outColor = vec4(color.r * u_color.r * u_opacity * f, color.g * u_color.g * u_opacity * f, color.b * u_color.b * u_opacity * f, color.a * u_opacity * f);

}
