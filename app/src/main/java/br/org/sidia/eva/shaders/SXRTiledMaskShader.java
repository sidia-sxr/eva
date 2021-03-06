/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package br.org.sidia.eva.shaders;

import android.content.Context;

import com.samsungxr.SXRContext;
import com.samsungxr.SXRShaderData;
import com.samsungxr.SXRShaderTemplate;
import br.org.sidia.eva.R;
import com.samsungxr.utility.TextFile;

/**
 * Shader which samples from an external texture.
 * This shader does not use light sources.
 * @<code>
 *    a_position    position vertex attribute
 *    a_texcoord    texture coordinate vertex attribute
 *    u_color       color to modulate texture
 *    u_opacity     opacity
 *    u_texture     external texture
 * </code>
 */
public class SXRTiledMaskShader extends SXRShaderTemplate
{
    public SXRTiledMaskShader(SXRContext sxrContext)
    {
        super("float3 u_color float u_opacity float2 u_scale",
              "sampler2D u_texture",
              "float3 a_position", GLSLESVersion.VULKAN);
        Context context = sxrContext.getContext();
        setSegment("FragmentTemplate", TextFile.readTextFile(context, R.raw.tex_tiled_frag));
        setSegment("VertexTemplate", TextFile.readTextFile(context, R.raw.tex_tiled_vert));
    }

    protected void setMaterialDefaults(SXRShaderData material)
    {
        material.setVec3("u_color", 1, 1, 1);
        material.setFloat("u_opacity", 0.5f);
        material.setVec2("u_scale", 0.01f, 0.01f);
    }
}
