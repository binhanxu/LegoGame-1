/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia.samples.SampleApplication.utils;

import com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets.Const;

import java.nio.Buffer;


public class CubeObject extends MeshObject
{
    // Data for drawing the 3D plane as overlay
    private int cubeVertexLen;

    private static final double cubeTexcoords[] = { 
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1,
                                                
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1,
                                                
            0, 0, 1, 0, 1, 1, 0, 1,
                                                
            1, 0, 0, 0, 0, 1, 1, 1 };
    
    
    private static final double cubeNormals[]   = { 
            0, 0, 1,  0, 0, 1,  0, 0, 1,  0, 0, 1,
            
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,
            
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,
            
            1, 0, 0,  1, 0, 0,  1, 0, 0,  1, 0, 0,
            
            0, 1, 0,  0, 1, 0,  0, 1, 0,  0, 1, 0,
            
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,
            };
    
    private static final short  cubeIndices[]   = { 
            0, 1, 2, 0, 2, 3, // front
            4, 6, 5, 4, 7, 6, // back
            8, 9, 10, 8, 10, 11, // left
            12, 14, 13, 12, 15, 14, // right
            16, 17, 18, 16, 18, 19, // top
            20, 22, 21, 20, 23, 22  // bottom
                                                };
    
    private Buffer mVertBuff;
    private Buffer mTexCoordBuff;
    private Buffer mNormBuff;
    private Buffer mIndBuff;


    private int cubeVertLen;



    public CubeObject()
    {

        double[] cubeVertices = {
                -1.00f, -1.00f, 1.00f, // front
                1.00f, -1.00f, 1.00f,
                1.00f, 1.00f, 1.00f,
                -1.00f, 1.00f, 1.00f,

                -1.00f, -1.00f, -1.00f, // back
                1.00f, -1.00f, -1.00f,
                1.00f, 1.00f, -1.00f,
                -1.00f, 1.00f, -1.00f,

                -1.00f, -1.00f, -1.00f, // left
                -1.00f, -1.00f, 1.00f,
                -1.00f, 1.00f, 1.00f,
                -1.00f, 1.00f, -1.00f,

                1.00f, -1.00f, -1.00f, // right
                1.00f, -1.00f, 1.00f,
                1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, -1.00f,

                -1.00f, 1.00f, 1.00f, // top
                1.00f, 1.00f, 1.00f,
                1.00f, 1.00f, -1.00f,
                -1.00f, 1.00f, -1.00f,

                -1.00f, -1.00f, 1.00f, // bottom
                1.00f, -1.00f, 1.00f,
                1.00f, -1.00f, -1.00f,
                -1.00f, -1.00f, -1.00f };

//        for(int i = 0; i < cubeVertices.length ; i++)
//        {
//            if (i%3 == 0)
//                cubeVertices[i]*=length/2;
//            if (i%3 == 1)
//                cubeVertices[i]*=width/2;
//            if (i%3 == 2)
//                cubeVertices[i]*=height/2;
//        }
        for(int i = 0; i < cubeVertices.length ; i++)
        {
            cubeVertices[i]*= Const.cubeSize/2;
       }

        cubeVertexLen = cubeVertices.length;
        mVertBuff = fillBuffer(cubeVertices);
        mTexCoordBuff = fillBuffer(cubeTexcoords);
        mNormBuff = fillBuffer(cubeNormals);
        mIndBuff = fillBuffer(cubeIndices);
    }
    
    
    @Override
    public Buffer getBuffer(BUFFER_TYPE bufferType)
    {
        Buffer result = null;
        switch (bufferType)
        {
            case BUFFER_TYPE_VERTEX:
                result = mVertBuff;
                break;
            case BUFFER_TYPE_TEXTURE_COORD:
                result = mTexCoordBuff;
                break;
            case BUFFER_TYPE_INDICES:
                result = mIndBuff;
                break;
            case BUFFER_TYPE_NORMALS:
                result = mNormBuff;
            default:
                break;
        }
        return result;
    }
    
    
    @Override
    public int getNumObjectVertex()
    {
        return cubeVertLen / 3;
    }
    
    
    @Override
    public int getNumObjectIndex()
    {
        return cubeIndices.length;
    }
}
