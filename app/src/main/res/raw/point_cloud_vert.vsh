#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision mediump float;
layout ( location = 0 ) in vec3 a_position;
@MATRIX_UNIFORMS

uniform float u_point_size;

void main()
{
	gl_Position = u_mvp * vec4(a_position, 1);
    gl_PointSize = u_point_size;
}