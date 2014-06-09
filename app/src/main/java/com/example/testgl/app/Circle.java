package com.example.testgl.app;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Barbados on 27/05/2014.
 */
public class Circle {
    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    double mTheta;
    double mTangFactor;
    double mRadialFactor;
    private float mCenterX;
    private float mCenterY;
    private int mProgram, mPositionHandle, mColorHandle, mMVPMatrixHandle ;
    private FloatBuffer mVertexBuffer;

    float mColor[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    private int mVertexCount = 36;

    private float mVertices[] = new float[mVertexCount * 3];

    public Circle(float r) {
        init(r);
    }

    public void setCoordinates(float cX, float cY) {
        for (int i = 0; i < mVertexCount; i++)
        {
            mVertices[i * 3] += cX;
            mVertices[i * 3 + 1] += cY;
        }

        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(mVertices.length * 4);
        vertexByteBuffer.order(ByteOrder.nativeOrder());
        mVertexBuffer = vertexByteBuffer.asFloatBuffer();
        mVertexBuffer.put(mVertices);
        mVertexBuffer.position(0);
        int vertexShader = GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);
    }

    private void init(float r) {
        mTheta = 2 * Math.PI / mVertexCount;
        mTangFactor = Math.tan(mTheta);
        mRadialFactor = Math.cos(mTheta);

        float x = r;
        float y = 0;
        for (int i = 0; i < mVertexCount; i++)
        {
            float tx = -y;
            float ty = x;
            x += tx * mTangFactor;
            y += ty * mTangFactor;
            x *= mRadialFactor;
            y *= mRadialFactor;
            mVertices[i * 3] = x;
            mVertices[i * 3 + 1] = y;
            mVertices[i * 3 + 2] = 0;
        }
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLRenderer.checkGlError("mPositionHandle");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, mVertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, mVertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}