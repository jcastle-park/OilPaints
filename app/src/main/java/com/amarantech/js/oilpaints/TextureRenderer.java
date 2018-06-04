/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amarantech.js.oilpaints;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextureRenderer {

    private int mEffectType;
    private int mProgram;
    private int mTexSamplerHandle;
    private int mTexCoordHandle;
    private int mPosCoordHandle;

    private FloatBuffer mTexVertices;
    private FloatBuffer mPosVertices;

    private int mViewWidth;
    private int mViewHeight;

    private int mTexWidth;
    private int mTexHeight;

    private int mResolutionX;
    private int mResolutionY;

    private static final String VERTEX_SHADER =
        "attribute vec4 a_position;\n" +
        "attribute vec2 a_texcoord;\n" +
        "varying vec2 v_texcoord;\n" +
        "void main() {\n" +
        "   gl_Position = a_position;\n" +
        "   v_texcoord = a_texcoord;\n" +
        "}\n";

    private static final String FRAGMENT_BASE_SHADER =
        "precision mediump float;\n" +
        "uniform sampler2D tex_sampler;\n" +
        "varying vec2 v_texcoord;\n" +
        "uniform float u_resolutionX;\n" +
        "uniform float u_resolutionY;\n" +
        "void main() {\n" +
        "   vec2 u_resolution = vec2(u_resolutionX, u_resolutionY);\n" +
        "   gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
        "}\n";

    private static final String FRAGMENT_SHADER =
        "precision mediump float;\n" +
        "varying vec2 v_texcoord;\n" +
        "uniform sampler2D tex_sampler;\n" +
        //"uniform vec2 u_resolution;\n" +
        "uniform float u_resolutionX;\n" +
        "uniform float u_resolutionY;\n" +
        //"uniform vec2 fgCenter;\n" +	// (0, 0) ~ (100, 100), (50, 50)
        //"uniform float imageSize;\n" +	// 40 ~ 100, 40

        "const float mixAmount = 1.0;\n" +
        "vec4 growablePoissonDiskBlur(vec2 paramUV)\n" +
        "{\n" +
        "   vec2 u_resolution = vec2(u_resolutionX, u_resolutionY);\n" +
        "	float DiskRadius = 5.0;\n" +
        "	vec4 cOut;\n" +
        "	vec2 poisson[12];\n" +
        "	poisson[0] = vec2(-0.326212, -0.40581);\n" +
        "	poisson[1] = vec2(-0.840144, -0.07358);\n" +
        "	poisson[2] = vec2(-0.695914, 0.457137);\n" +
        "	poisson[3] = vec2(-0.203345, 0.620716);\n" +
        "	poisson[4] = vec2(0.96234, -0.194983);\n" +
        "	poisson[5] = vec2(0.473434, -0.480026);\n" +
        "	poisson[6] = vec2(0.519456, 0.767022);\n" +
        "	poisson[7] = vec2(0.185461, -0.893124);\n" +
        "	poisson[8] = vec2(0.507431, 0.064425);\n" +
        "	poisson[9] = vec2(0.89642, 0.412458);\n" +
        "	poisson[10] = vec2(-0.32194, -0.932615);\n" +
        "	poisson[11] = vec2(-0.791559, -0.59771);\n" +

        "	cOut = texture2D(tex_sampler, paramUV);\n" +
        "	for (int tap = 0; tap < 12; tap++)\n" +
        "	{\n" +
        "		vec2 coord = paramUV.xy + (poisson[tap] / u_resolution * DiskRadius);\n" +
        "		cOut += texture2D(tex_sampler, coord);\n" +
        "	}\n" +
        //"	cOut.rgb = vec3(max(max(cOut.r, cOut.g), cOut.b));\n" +
        "	return(cOut / 13.0);\n" +
        "}\n" +

        "void main(void)\n" +
        "{\n" +
        "	vec2 coord = v_texcoord;\n" +
        "	vec4 c = growablePoissonDiskBlur(v_texcoord);\n" +
        //"	float delta = imageSize / 200.0;\n"
        "	float delta = 70.0 / 200.0;\n" +
        "	float ratio = 1.0 / (delta*2.0);\n" +
        //"	vec2 leftTop = vec2(fgCenter.x / 100.0 - delta, fgCenter.y / 100.0 - delta);\n" +
        //"	vec2 rightBottom = vec2(fgCenter.x / 100.0 + delta, fgCenter.y / 100.0 + delta);\n" +
        "	vec2 leftTop = vec2(50.0 / 100.0 - delta, 50.0 / 100.0 - delta);\n" +
        "	vec2 rightBottom = vec2(50.0 / 100.0 + delta, 50.0 / 100.0 + delta);\n" +
        "	if ((leftTop.x < coord.x) && (leftTop.y < coord.y) &&\n" +
        "		(coord.x < rightBottom.x) && (coord.y < rightBottom.y))\n" +
        "	{\n" +
        "		vec2 newCenter = vec2(leftTop.x, leftTop.y) * ratio;\n" +
        "		vec4 smallImg = texture2D(tex_sampler, coord*ratio - newCenter);\n" +
        "		c = mix(c, smallImg, mixAmount);\n" +
        "	}\n" +
        "	gl_FragColor = c;\n" +
        "}\n";

    private static final String FRAGMENT_OIL_SHADER =
        "precision mediump float;\n" +
        "varying vec2 v_texcoord;\n" +
        "uniform sampler2D tex_sampler;\n" +

        "uniform float u_resolutionX;\n" +
        "uniform float u_resolutionY;\n" +
        //"uniform int radius;\n" +
        "const int MAX_RADIUS = 10;\n" +
        "void main() {\n" +
        "   int radius = 10;\n" +
        "   vec2 u_resolution = vec2(u_resolutionX, u_resolutionY);\n" +
        "    vec2 uv = v_texcoord.st;\n" +
        "    float n = float((radius + 1) * (radius + 1));\n" +
        "    vec3 m[2];\n" +
        "    vec3 s[2];\n" +
        "    for (int k = 0; k < 2; ++k) {\n" +
        "        m[k] = vec3(0.0);\n" +
        "        s[k] = vec3(0.0);\n" +
        "    }\n" +
        "    for (int j = -MAX_RADIUS; j <= 0; ++j)  {\n" +
        "        int k = int(mod(float((-j+radius)), float(MAX_RADIUS)));\n" +
        "        for (int i = -MAX_RADIUS; i <= 0; ++i)  {\n" +
        "            int n = int(mod(float((-i + radius)), float(MAX_RADIUS)));\n" +
        "            vec3 c0 = texture2D(tex_sampler, uv + vec2(-n,-k) / u_resolution).rgb;\n" +
        "            m[0] += c0;\n" +
        "            s[0] += c0 * c0;\n" +
        "            vec3 c1 = texture2D(tex_sampler, uv + vec2(-n+radius, -k) / u_resolution).rgb;\n" +
        "            m[1] += c1;\n" +
        "            s[1] += c1 * c1;\n" +
        "            if ( n == 0 && radius != 10)\n" +
        "            {\n" +
        "                break;\n" +
        "            }\n" +
        "        }\n" +
        "        if ( k == 0 && radius != 10)\n" +
        "        {\n" +
        "            break;\n" +
        "        }\n" +
        "    }\n" +
        "    float min_sigma2 = 1e+2;\n" +
        "    for (int k = 0; k < 2; ++k) {\n" +
        "        m[k] /= n;\n" +
        "        s[k] = abs(s[k] / n - m[k] * m[k]);\n" +
        "        float sigma2 = s[k].r + s[k].g + s[k].b;\n" +
        "        if (sigma2 < min_sigma2) {\n" +
        "            min_sigma2 = sigma2;\n" +
        "            gl_FragColor = vec4(m[k], 1.0);\n" +
        "        }\n" +
        "    }\n" +
        "}\n";

    private static final float[] TEX_VERTICES = {
            0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
    };

    private static final float[] POS_VERTICES = {
            -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f
    };

    private static final int FLOAT_SIZE_BYTES = 4;

    public void init(int nEffect) {
        // Create program
        mEffectType = nEffect;
        GLES20.glDeleteProgram(mProgram);
        if (mEffectType == 0)
            mProgram = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_BASE_SHADER);
        else if (mEffectType == 1)
            mProgram = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_OIL_SHADER);
        else
            mProgram = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);

        // Bind attributes and uniforms
        mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram,
                "tex_sampler");
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texcoord");
        mPosCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_position");

        if (mEffectType != 0)
        {
            mResolutionX = GLES20.glGetUniformLocation(mProgram, "u_resolutionX");
            mResolutionY = GLES20.glGetUniformLocation(mProgram, "u_resolutionY");
        }

        // Setup coordinate buffers
        mTexVertices = ByteBuffer.allocateDirect(
                TEX_VERTICES.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTexVertices.put(TEX_VERTICES).position(0);
        mPosVertices = ByteBuffer.allocateDirect(
                POS_VERTICES.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPosVertices.put(POS_VERTICES).position(0);
    }

    public void tearDown() {
        GLES20.glDeleteProgram(mProgram);
    }

    public void updateTextureSize(int texWidth, int texHeight) {
        mTexWidth = texWidth;
        mTexHeight = texHeight;
        computeOutputVertices();
    }

    public void updateViewSize(int viewWidth, int viewHeight) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        computeOutputVertices();
    }

    public void renderTexture(int texId) {
        // Bind default FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // Use our shader program
        GLES20.glUseProgram(mProgram);
        GLToolbox.checkGlError("glUseProgram");

        // Set viewport
        GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
        GLToolbox.checkGlError("glViewport");

        // Disable blending
        GLES20.glDisable(GLES20.GL_BLEND);

        // Set the vertex attributes
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false,
                0, mTexVertices);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mPosCoordHandle, 2, GLES20.GL_FLOAT, false,
                0, mPosVertices);
        GLES20.glEnableVertexAttribArray(mPosCoordHandle);
        GLToolbox.checkGlError("vertex attribute setup");

        // Set the input texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLToolbox.checkGlError("glActiveTexture");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLToolbox.checkGlError("glBindTexture");
        GLES20.glUniform1i(mTexSamplerHandle, 0);

        // Set Parameter
        if (mEffectType != 0) {
            GLES20.glUniform1f(mResolutionX, (float) mViewWidth);
            GLES20.glUniform1f(mResolutionY, (float) mViewHeight);
        }


        // Draw
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    private void computeOutputVertices() {
        if (mPosVertices != null) {
            float imgAspectRatio = mTexWidth / (float)mTexHeight;
            float viewAspectRatio = mViewWidth / (float)mViewHeight;
            float relativeAspectRatio = viewAspectRatio / imgAspectRatio;
            float x0, y0, x1, y1;
            if (relativeAspectRatio > 1.0f) {
                x0 = -1.0f / relativeAspectRatio;
                y0 = -1.0f;
                x1 = 1.0f / relativeAspectRatio;
                y1 = 1.0f;
            } else {
                x0 = -1.0f;
                y0 = -relativeAspectRatio;
                x1 = 1.0f;
                y1 = relativeAspectRatio;
            }
            float[] coords = new float[] { x0, y0, x1, y0, x0, y1, x1, y1 };
            mPosVertices.put(coords).position(0);
        }
    }

}
