package com.example.testgl.app;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Barbados on 04/03/14.
 */
public class GlSurfaceView extends GLSurfaceView {

    private float mJoystickRadius;
    private float mInnerJoystickRadius;
    private float mJoystickCenterX;
    private float mJoystickCenterY;
    private float mPreviousX;
    private float mPreviousY;
    private final GLRenderer mRenderer;
    private boolean mGoTop = false;
    private boolean mGoRight = false;
    private Direction mMoveDirection;
    private float halfWidth;
    private float halfHeight;

    public GlSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        mRenderer = new GLRenderer(context);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float x = event.getX();
        final float y = event.getY();
        final int actionId = event.getAction();
        halfWidth = getWidth() / 2.0f;
        halfHeight = getHeight() / 2.0f;
        mJoystickRadius = getWidth() / 10.0f;
        mInnerJoystickRadius = mJoystickRadius / 3.0f;

        queueEvent(new Runnable() {
            public void run() {
                switch (actionId) {
                    case MotionEvent.ACTION_DOWN:
                        mJoystickCenterX = x;
                        mJoystickCenterY = y;
                        renderJoystick(x, y);
                        renderInnerJoystick(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!isInJoystickCircle(x, y)) {
                            moveJoystick(x, y);
                        }
                        renderInnerJoystick(x, y);
                        float angle = calculateAngle(x, y);
                        mRenderer.setAngle(angle);
                        mRenderer.setDirection(mMoveDirection);
                        mRenderer.startTriangleMove();
                        break;
                    case MotionEvent.ACTION_UP:
                        mRenderer.stopTriangleMove();
                        break;
                }

                mPreviousX = x;
                mPreviousY = y;
            }
        });

        return true;
    }

    private void renderJoystick(float x, float y) {
        mRenderer.createJoystick(mJoystickRadius / halfWidth);
        mRenderer.setJoystickCoordinates(getOpenGlX(x), getOpenGlY(y));
    }

    private void renderInnerJoystick(float x, float y) {
        mRenderer.createInnerJoystick(mInnerJoystickRadius / halfWidth);
        mRenderer.setJoystickInnerCoordinates(getOpenGlX(x), getOpenGlY(y));
    }

    private float getOpenGlX(float x) {
        return (x - halfWidth) / halfHeight;
    }

    private float getOpenGlY(float y) {
        return (halfHeight - y) / halfHeight;
    }

    private boolean isInJoystickCircle(float x, float y) {
        return mJoystickRadius * mJoystickRadius
                >= (x - mJoystickCenterX) * (x - mJoystickCenterX) + (y - mJoystickCenterY) * (y - mJoystickCenterY);
    }

    private void moveJoystick(float x, float y) {
        float dy = mPreviousY - mJoystickCenterY;
        float dx = mPreviousX - mJoystickCenterX;
        mJoystickCenterY = y - dy;
        mJoystickCenterX = x - dx;
        renderJoystick(mJoystickCenterX, mJoystickCenterY);
    }

    private float calculateAngle(float x, float y) {
        float angle = 0;
        if (x > mJoystickCenterX && y > mJoystickCenterY) {
            angle = (x - mJoystickCenterX) / (y - mJoystickCenterY);
            mGoTop = false;
            mGoRight = true;
            mMoveDirection = Direction.DOWN_RIGHT;
        } else if (x > mJoystickCenterX && y < mJoystickCenterY) {
            angle = (mJoystickCenterY - y) / (x - mJoystickCenterX);
            mGoTop = true;
            mGoRight = true;
            mMoveDirection = Direction.UP_RIGHT;
        } else if (x < mJoystickCenterX && y < mJoystickCenterY) {
            angle = (mJoystickCenterY - y) / (mJoystickCenterX - x);
            mGoTop = true;
            mGoRight = false;
            mMoveDirection = Direction.UP_LEFT;
        } else if (x < mJoystickCenterX && y > mJoystickCenterY) {
            angle = (y - mJoystickCenterY) / (mJoystickCenterX - x);
            mGoTop = false;
            mGoRight = false;
            mMoveDirection = Direction.DOWN_LEFT;
        } else if (x == mJoystickCenterX && y < mJoystickCenterY) {
            mMoveDirection = Direction.UP;
        } else if (x == mJoystickCenterX && y > mJoystickCenterY) {
            mMoveDirection = Direction.DOWN;
        } else if (x > mJoystickCenterX && y == mJoystickCenterY) {
            mMoveDirection = Direction.RIGHT;
        } else if (x < mJoystickCenterX && y == mJoystickCenterY) {
            mMoveDirection = Direction.LEFT;
        }

        return angle;
    }
}
