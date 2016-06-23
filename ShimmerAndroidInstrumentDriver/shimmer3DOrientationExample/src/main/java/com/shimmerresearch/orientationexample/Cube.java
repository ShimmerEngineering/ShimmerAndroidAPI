package com.shimmerresearch.orientationexample;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

/**
 * This class is an object representation of 
 * a Cube containing the vertex information,
 * color information, the vertex indices
 * and drawing functionality, which is called 
 * by the renderer.
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class Cube {
		
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the color values */
	private FloatBuffer colorBuffer;
	/** The buffer holding the indices */
	private ByteBuffer  indexBuffer;
	
	/** 
	 * The initial vertex definition
	 * 
	 * It defines the eight vertices a cube has
	 * based on the OpenGL coordinate system
	 */
	private float vertices[] = {  // Vertices of the 6 faces
		      // FRONT
		      -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
		       1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
		      -1.0f,  1.0f,  1.0f,  // 2. left-top-front
		       1.0f,  1.0f,  1.0f,  // 3. right-top-front
		      // BACK
		       1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
		      -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
		       1.0f,  1.0f, -1.0f,  // 7. right-top-back
		      -1.0f,  1.0f, -1.0f,  // 5. left-top-back
		      // LEFT
		      -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
		      -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front 
		      -1.0f,  1.0f, -1.0f,  // 5. left-top-back
		      -1.0f,  1.0f,  1.0f,  // 2. left-top-front
		      // RIGHT
		       1.0f, -1.0f,  1.0f,  // 1. right-bottom-front
		       1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
		       1.0f,  1.0f,  1.0f,  // 3. right-top-front
		       1.0f,  1.0f, -1.0f,  // 7. right-top-back
		      // TOP
		      -1.0f,  1.0f,  1.0f,  // 2. left-top-front
		       1.0f,  1.0f,  1.0f,  // 3. right-top-front
		      -1.0f,  1.0f, -1.0f,  // 5. left-top-back
		       1.0f,  1.0f, -1.0f,  // 7. right-top-back
		      // BOTTOM
		      -1.0f, -1.0f, -1.0f,  // 4. left-bottom-back
		       1.0f, -1.0f, -1.0f,  // 6. right-bottom-back
		      -1.0f, -1.0f,  1.0f,  // 0. left-bottom-front
		       1.0f, -1.0f,  1.0f   // 1. right-bottom-front
		       };
		       
    
    /** The initial color definition */	
	private float colors[][] = {  // Colors of the 6 faces
		      {1.0f, 0.5f, 0.0f, 1.0f},  // 0. orange
		      {1.0f, 0.0f, 1.0f, 1.0f},  // 1. violet
		      {0.0f, 1.0f, 0.0f, 1.0f},  // 2. green
		      {0.0f, 0.0f, 1.0f, 1.0f},  // 3. blue
		      {1.0f, 0.0f, 0.0f, 1.0f},  // 4. red
		      {1.0f, 1.0f, 0.0f, 1.0f}   // 5. yellow
		      };
   
    /** 
     * The initial indices definition
     * 
     * The indices define our triangles.
     * Always two define one of the six faces
     * a cube has.
     */	
	private byte indices[] = {
    					/*
    					 * Example: 
    					 * Face made of the vertices lower back left (lbl),
    					 * lfl, lfr, lbl, lfr, lbr
    					 */
			            0, 4, 5,    0, 5, 1,
			            //and so on...
			            1, 5, 6,    1, 6, 2,
			            2, 6, 7,    2, 7, 3,
			            3, 7, 4,    3, 4, 0,
			            4, 7, 6,    4, 6, 5,
			            3, 0, 1,    3, 1, 2
    										};
		
	/**
	 * The Cube constructor.
	 * 
	 * Initiate the buffers.
	 */
	public Cube(Context context) {
	      // Setup vertex-array buffer. Vertices in float. An float has 4 bytes
	      ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
	      vbb.order(ByteOrder.nativeOrder()); // Use native byte order
	      vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
	      vertexBuffer.put(vertices);         // Copy data into buffer
	      vertexBuffer.position(0);           // Rewind
	      }

	/**
	 * The object own drawing function.
	 * Called from the renderer to redraw this instance
	 * with possible changes in values.
	 * 
	 * @param gl - The GL Context
	 */
	public void draw(GL10 gl) {
	      gl.glFrontFace(GL10.GL_CCW);    // Front face in counter-clockwise orientation
	      gl.glEnable(GL10.GL_CULL_FACE); // Enable cull face
	      gl.glCullFace(GL10.GL_BACK);    // Cull the back face (don't display)
	  
	      gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	      gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

	      // Render all the faces
	      for (int face = 0; face < 6; face++) {
	         // Set the color for each of the faces
	         gl.glColor4f(colors[face][0], colors[face][1], colors[face][2], colors[face][3]);
	         // Draw the primitive from the vertex-array directly
	         gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, face*4, 4);
	      }
	      gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	      gl.glDisable(GL10.GL_CULL_FACE);
	      }
}
