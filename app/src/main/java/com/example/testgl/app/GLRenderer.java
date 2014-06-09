package com.example.testgl.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Barbados on 04/03/14.
 */
public class GLRenderer implements GLSurfaceView.Renderer {
    private static Context mContext;
    private Triangle mTriangle;
    private Square mSquare;
    private Circle mCircle;
    private Circle mCircleInner;
    private final float[] mViewMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];
    private float mCurrentPositionY = 0;
    private float mCurrentPositionX = 0;
    private float mShift = 0.005f;
    private boolean mIsMoving = false;
    private Direction mDirection;
    private float mAngle = 0;

    public GLRenderer(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mTriangle = new Triangle();
        mSquare = new Square();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        if (mCircle != null) {
            mCircle.draw(mMVPMatrix);
        }

        if (mCircleInner != null) {
            mCircleInner.draw(mMVPMatrix);
        }

        float[] tempMatrix = new float[16];
        Matrix.setIdentityM(mTranslationMatrix, 0);

        float angleShift = (float) (mShift / Math.sqrt(mAngle * mAngle + 1));
        if (mIsMoving) {
            switch (mDirection) {
                case UP:
                    mCurrentPositionY += mShift;
                    break;
                case UP_RIGHT:
                    mCurrentPositionX += angleShift;
                    mCurrentPositionY += angleShift * mAngle;
                    break;
                case RIGHT:
                    mCurrentPositionX += mShift;
                    break;
                case DOWN_RIGHT:
                    mCurrentPositionX += angleShift;
                    mCurrentPositionY -= angleShift * mAngle;
                    break;
                case DOWN:
                    mCurrentPositionY -= mShift;
                    break;
                case DOWN_LEFT:
                    mCurrentPositionX -= angleShift;
                    mCurrentPositionY -= angleShift * mAngle;
                    break;
                case LEFT:
                    mCurrentPositionX -= mShift;
                    break;
                case UP_LEFT:
                    mCurrentPositionX -= angleShift;
                    mCurrentPositionY += angleShift * mAngle;
                    break;
            }
        }
        Matrix.translateM(mTranslationMatrix, 0, mCurrentPositionX, mCurrentPositionY, 0);
        Matrix.multiplyMM(tempMatrix, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

        mTriangle.draw(tempMatrix);
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void setDirection(Direction direction) {
        mDirection = direction;
    }

    public void startTriangleMove() {
        mIsMoving = true;
    }

    public void stopTriangleMove() {
        mIsMoving = false;
    }

    public void createJoystick(float r) {
        mCircle = new Circle(r);
    }

    public void createInnerJoystick(float r) {
        mCircleInner = new Circle(r);
    }

    public void setJoystickCoordinates(float x, float y) {
        mCircle.setCoordinates(x, y);
    }

    public void setJoystickInnerCoordinates(float x, float y) {
        mCircleInner.setCoordinates(x, y);
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static int loadTexture(final int resourceId)
    {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        int textureId = textureHandle[0];

        if (textureId != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            final Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resourceId, options);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
        else
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureId;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("Error", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
