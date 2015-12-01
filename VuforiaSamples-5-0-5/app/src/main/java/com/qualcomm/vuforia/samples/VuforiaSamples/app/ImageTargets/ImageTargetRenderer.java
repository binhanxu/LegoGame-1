/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Teapot;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;

// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer {
    private static final String LOGTAG = "ImageTargetRenderer";

    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;

    private Vector<Texture> mTextures;

    private int shaderProgramID;

    private int vertexHandle;

    private int normalHandle;

    private int textureCoordHandle;

    private int mvpMatrixHandle;

    private int texSampler2DHandle;

    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;

    private Renderer mRenderer;

    boolean mIsActive = false;

    private static final float OBJECT_SCALE_FLOAT = 1.0f;

    private static final float CUBE_SIDE = 50.0f;

    float currentZ = 0.0f;

    public ImageTargetRenderer(ImageTargets activity,
                               SampleApplicationSession session) {
        mActivity = activity;
        vuforiaAppSession = session;

//        selfRotateMatrixVector.setSize(4);
//        for(int i=0;i<=3;i++)
//        {
//            //initialize teapot positions
//            X[i] = -120.0f+(80.0f*i);
//            Y[i] = 86.5f;
//            Z[i] = 1.0f;
//
//            //initialize self-rotation matrix
//            float[] Identity=new float[16];
//            Matrix.setIdentityM(Identity,0);
//            selfRotateMatrixVector.add(i, Identity);
//
//            //initialize stick or not
//            isSticked[i]=false;
////            interval[i]=50;
//
//            //initialize locks
//            lock[i]=false;
//        }

    }

    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }

    // Function for initializing the renderer.
    private void initRendering() {

//        mCube = new CubeObject(10.00f,10.0f,10.0f);

        mRenderer = Renderer.getInstance();

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        for (Texture t : mTextures) {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

        try {
            mBuildingsModel = new SampleApplication3DModel();
            mBuildingsModel.loadModel(mActivity.getResources().getAssets(),
                    "ImageTargets/Buildings.txt");
        } catch (IOException e) {
            Log.e(LOGTAG, "Unable to load buildings");
        }

        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

    }


    boolean hasSticked = false;
    int thresholdDistance = 100;
    int thresholdInterval = 50;

    int count = 30;
    int up =0;

    private void renderFrame() {
        count--;
        if (count == 0)
        { count =30;
            up =(up+1)%10;}
           List<Object3D> objectList = new ArrayList<Object3D>();

            List<float[]> CenterList1 = new ArrayList<float[]>();
            CenterList1.add(new float[]{0, 0, 0});
            CenterList1.add(new float[]{0, 0, 1});
            objectList.add(new Object3D(0, 0, 0 + up*20, 1, CenterList1));

            List<float[]> CenterList2 = new ArrayList<float[]>();
            CenterList2.add(new float[]{0, 0, 0});
            CenterList2.add(new float[]{0, 0, 1});
            CenterList2.add(new float[]{0, 0, 2});
            CenterList2.add(new float[]{0, 0, 3});
            CenterList2.add(new float[]{0, 0, 4});
            objectList.add(new Object3D(80, 80, 0 + up*20, 0, CenterList2));

            List<float[]> CenterList3 = new ArrayList<float[]>();
            CenterList3.add(new float[]{0, 0, 0});
            CenterList3.add(new float[]{0, 0, 1});
            CenterList3.add(new float[]{1, 0, 0});
            objectList.add(new Object3D(-80, 80, 0 + up*20, 0, CenterList3));


            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera


        HashMap<String, Matrix44F> modelViewMap = new HashMap<String, Matrix44F>();
        getAllModelViewMap(state, modelViewMap);

        //Render teapot on chips
        if (modelViewMap.containsKey("chips")) {
            Matrix44F modelViewMatrix_Vuforia = modelViewMap.get("chips");
            int textureIndex = 1;
            float[] modelViewProjection = new float[16];
            float[] Projectionmatrix = vuforiaAppSession.getProjectionMatrix().getData();
            Matrix.multiplyMM(modelViewProjection, 0, Projectionmatrix, 0, modelViewMatrix_Vuforia.getData(), 0);
            renderTeapot(modelViewProjection, textureIndex);
        }

        if (modelViewMap.containsKey("stones")) {
            Matrix44F modelViewMatrix_Vuforia = modelViewMap.get("stones");
            int textureIndex = 0;


            for (Object3D obj : objectList) {
                for (float[] offset : obj.CenterList) {
                    //X,Y,Z in camera coordinate
                    float X_camera, Y_camera, Z_camera;
                    double distance;

                    //Declare projection and modelview matrix
                    float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
                    float[] projectionMatrix = vuforiaAppSession.getProjectionMatrix().getData();
                    CameraCalibration camCal = CameraDevice.getInstance()
                            .getCameraCalibration();
                    //Get intrinsic parameters
                    Vec2F f = camCal.getFocalLength();
                    Vec2F center = camCal.getPrincipalPoint();
                    Vec2F size = camCal.getSize();
                    float fx = f.getData()[0];
                    float fy = f.getData()[1];
                    float cx = center.getData()[0];
                    float cy = center.getData()[1];
                    float width = size.getData()[0];
                    float height = size.getData()[1];

                    if (!mActivity.isExtendedTrackingActive()) {
                        Matrix.translateM(modelViewMatrix, 0, obj.Center_X + offset[0]*20, obj.Center_Y + offset[1]*20,
                                obj.Center_Z + offset[2]*20);
                        Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                                OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);

                    } else {
                        Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                        Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                                kBuildingScale, kBuildingScale);
                    }
                    float[] modelViewProjectionMatrix = new float[16];
                    //float[] tempMatrix=new float[16];
                    //Matrix.multiplyMM(tempMatrix, 0, modelViewMatrix, 0, obj.selfRotationMatrix, 0);
                    //Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0);
                    Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

                    // activate the shader program and bind the vertex/normal/tex coords
                    GLES20.glUseProgram(shaderProgramID);

                    if (!mActivity.isExtendedTrackingActive()) {
                        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                                false, 0, obj.cube.getVertices());
                        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                                false, 0, obj.cube.getNormals());
                        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                                GLES20.GL_FLOAT, false, 0, obj.cube.getTexCoords());

                        GLES20.glEnableVertexAttribArray(vertexHandle);
                        GLES20.glEnableVertexAttribArray(normalHandle);
                        GLES20.glEnableVertexAttribArray(textureCoordHandle);

                        // activate texture 0, bind it, and pass to shader
                        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                                mTextures.get(obj.textureId).mTextureID[0]);
                        GLES20.glUniform1i(texSampler2DHandle, 0);

                        // pass the model view matrix to the shader
                        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                                modelViewProjectionMatrix, 0);

                        // finally draw the teapot
                        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                                obj.cube.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                                obj.cube.getIndices());

                        // disable the enabled arrays
                        GLES20.glDisableVertexAttribArray(vertexHandle);
                        GLES20.glDisableVertexAttribArray(normalHandle);
                        GLES20.glDisableVertexAttribArray(textureCoordHandle);
                    } else {
                        GLES20.glDisable(GLES20.GL_CULL_FACE);
                        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                                false, 0, mBuildingsModel.getVertices());
                        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                                false, 0, mBuildingsModel.getNormals());
                        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                                GLES20.GL_FLOAT, false, 0, mBuildingsModel.getTexCoords());

                        GLES20.glEnableVertexAttribArray(vertexHandle);
                        GLES20.glEnableVertexAttribArray(normalHandle);
                        GLES20.glEnableVertexAttribArray(textureCoordHandle);

                        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                                mTextures.get(3).mTextureID[0]);
                        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                                modelViewProjectionMatrix, 0);
                        GLES20.glUniform1i(texSampler2DHandle, 0);
                        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                                mBuildingsModel.getNumObjectVertex());

                        SampleUtils.checkGLError("Renderer DrawBuildings");
                    }

                    SampleUtils.checkGLError("Render Frame");
                }
            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        mRenderer.end();


}
    //
    private void getNewSelfRotationMatrix(float[] modelViewMatrix, Object3D obj) {
        float[] temp=new float[16];
        float[] result_temp=new float[16];
        Matrix.setIdentityM(temp,0);

        temp[0]=modelViewMatrix[0];
        temp[1]=modelViewMatrix[1];
        temp[2]=modelViewMatrix[2];
        temp[4]=modelViewMatrix[4];
        temp[5]=modelViewMatrix[5];
        temp[6]=modelViewMatrix[6];
        temp[8]=modelViewMatrix[8];
        temp[9]=modelViewMatrix[9];
        temp[10]=modelViewMatrix[10];

        Matrix.multiplyMM(result_temp, 0, temp, 0, obj.selfRotationMatrix, 0);
        obj.selfRotationMatrix = result_temp;
    }

    private void getAllModelViewMap(State state, HashMap<String, Matrix44F> modelViewMap) {
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            modelViewMap.put(trackable.getName(),modelViewMatrix_Vuforia);
        }
    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;

    }

//    private boolean isNotIntersected(int i,float X_temp,float Y_temp)
//    {
//        for(int m=0;m< objectList.size();m++)
//        {
//            if(m==i) continue;
//            else if(objectList.get(m).isSticked == objectList.get(i).isSticked) continue;
//            else {
//                if (isInBox(m, X_temp - CUBE_SIDE / 2, Y_temp - CUBE_SIDE / 2)
//                        || isInBox(m, X_temp - CUBE_SIDE / 2, Y_temp + CUBE_SIDE / 2)
//                        || isInBox(m, X_temp + CUBE_SIDE / 2, Y_temp - CUBE_SIDE / 2)
//                        || isInBox(m, X_temp + CUBE_SIDE / 2, Y_temp + CUBE_SIDE / 2))
//                    return false;
//            }
//        }
//
//        return true;
//    }

//    private boolean isInBox(int i,float curX,float curY)
//    {
//        if(curX>objectList.get(i).X -CUBE_SIDE/2 && curX<objectList.get(i).X+CUBE_SIDE/2
//                && curY>objectList.get(i).Y-CUBE_SIDE/2 && curY<objectList.get(i).Y+CUBE_SIDE/2)
//            return true;
//        else return false;
//    }

    private double[] Mat2Quat(float[][] R) {

        double[] Quat = new double[4];
        double[] trace = new double[4];

        trace[0] = 1.0f + R[0][0] + R[1][1] + R[2][2];
        trace[1] = 1.0f + R[0][0] - R[1][1] - R[2][2];
        trace[2] = 1.0f - R[0][0] + R[1][1] - R[2][2];
        trace[3] = 1.0f - R[0][0] - R[1][1] + R[2][2];

        int j = 0;
        for (int i = 1; i < 4; i++) {
            if (trace[i] > trace[j]) j = i;
        }

        if (j == 0) {
            Quat[0] = trace[0];
            Quat[1] = R[1][2] - R[2][1];
            Quat[2] = R[2][0] - R[0][2];
            Quat[3] = R[0][1] - R[1][0];
        } else if (j == 1) {
            Quat[0] = R[1][2] - R[2][1];
            Quat[1] = trace[1];
            Quat[2] = R[0][1] + R[1][0];
            Quat[3] = R[2][0] + R[0][2];
        } else if (j == 2) {
            Quat[0] = R[2][0] - R[0][2];
            Quat[1] = R[0][1] + R[1][0];
            Quat[2] = trace[2];
            Quat[3] = R[1][2] + R[2][1];
        } else //j==3
        {
            Quat[0] = R[0][1] - R[1][0];
            Quat[1] = R[2][0] + R[0][2];
            Quat[2] = R[1][2] + R[1][0];
            Quat[3] = trace[3];
        }

        double sum = Math.sqrt(0.25 / trace[j]);
        Quat[0] *= sum;
        Quat[1] *= sum;
        Quat[2] *= sum;
        Quat[3] *= sum;

        return Quat;
    }



    void renderTeapot(float[] modelViewProjection, int textureIndex)
    {
        Teapot mTeapot = new Teapot();
        // activate the shader program and bind the vertex/normal/tex coords
        GLES20.glUseProgram(shaderProgramID);

        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        // activate texture 0, bind it, and pass to shader
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(textureIndex).mTextureID[0]);
        GLES20.glUniform1i(texSampler2DHandle, 0);

        // pass the model view matrix to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);

        // finally draw the teapot
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                mTeapot.getIndices());

        // disable the enabled arrays
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
//        Matrix.invertM()
    }
}