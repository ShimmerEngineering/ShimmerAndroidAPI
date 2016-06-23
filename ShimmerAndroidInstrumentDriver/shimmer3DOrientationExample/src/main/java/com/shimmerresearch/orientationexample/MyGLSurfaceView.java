package com.shimmerresearch.orientationexample;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

/**
 * This is a port of the {@link http://nehe.gamedev.net} OpenGL 
 * tutorials to the Android 1.5 OpenGL ES platform. Thanks to 
 * NeHe and all contributors for their great tutorials and great 
 * documentation. This source should be used together with the
 * textual explanations made at {@link http://nehe.gamedev.net}.
 * The code is based on the original Visual C++ code with all
 * comments made. It has been altered and extended to meet the
 * Android requirements. The Java code has according comments.
 * 
 * If you use this code or find it helpful, please visit and send
 * a shout to the author under {@link http://www.insanitydesign.com/}
 * 
 * @DISCLAIMER
 * This source and the whole package comes without warranty. It may or may
 * not harm your computer or cell phone. Please use with care. Any damage
 * cannot be related back to the author. The source has been tested on a
 * virtual environment and scanned for viruses and has passed all tests.
 * 
 * 
 * This is an interpretation of "Lesson 04: Rotation"
 * for the Google Android platform.
 * 
 * @author Savas Ziplies (nea/INsanityDesign)
 */
public class MyGLSurfaceView implements Renderer {
	
	private Cube cube;
	
	private float rtri; 	
	/** Angle For The Cube */
	private float rquad; 
	private float ax=0;
	private float ay=0;
	private float az=0;
	Context context;
	/**
	 * Instance the Triangle and Square objects
	 */
	public MyGLSurfaceView(Context context) {
		this.context = context;
		cube = new Cube(context);
	}

	/**
	 * The Surface is created/init()
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
		//square.loadGLTexture(gl, this.context);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glClearColor(0,0,0,0);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glDisable(GL10.GL_DITHER);
	    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
	    gl.glEnable(GL10.GL_CULL_FACE);
		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
		gl.glEnable(GL10.GL_ALPHA);
	}

	/**
	 * Here we do our drawing
	 */
	public void onDrawFrame(GL10 gl) {
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glLoadIdentity();					//Reset The Current Modelview Matrix
		
		//Drawing
		gl.glTranslatef(0.0f, 0.0f, -10.0f);	//Move down 1.0 Unit And Into The Screen 6.0
		gl.glRotatef(rquad, ax, ay, az);	//Rotate The Square On The X axis ( NEW )
		cube.draw(gl);						//Draw the square
		
		}

	/**
	/**
	 * If the surface changes, reset the view
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
	}

	public void setAngleAxis(float angle, float x, float y, float z){
		rquad = angle;
		ax=x;
		ay=y;
		az=z;
	}
	
}
