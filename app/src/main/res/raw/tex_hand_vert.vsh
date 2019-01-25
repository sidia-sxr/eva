#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

layout ( location = 0 )in vec3 a_position;
layout ( location = 1 )in vec2 a_texcoord;

@MATRIX_UNIFORMS

layout ( location = 0 ) out vec2 diffuse_coord;

void main()
{
    diffuse_coord = a_texcoord;

    gl_Position = u_mvp * vec4(a_position, 1);
}