package com.ToxicBakery.apps.mandelbrot;

import java.text.DecimalFormat;

import rajawali.Camera2D;
import rajawali.RajawaliActivity;
import rajawali.materials.Material;
import rajawali.materials.shaders.FragmentShader;
import rajawali.materials.shaders.VertexShader;
import rajawali.primitives.Plane;
import rajawali.renderer.RajawaliRenderer;
import rajawali.util.OnFPSUpdateListener;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class MainActivity extends RajawaliActivity {

	private ScaleGestureDetector mScaleDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final MandelRenderer renderer = new MandelRenderer(this);
		renderer.setSurfaceView(mSurfaceView);
		setRenderer(renderer);

		mScaleDetector = new ScaleGestureDetector(this, renderer.scaleListener);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mScaleDetector.onTouchEvent(event);

		// Assume all touch events are consumed
		return true;
	}

	public class MandelRenderer extends RajawaliRenderer implements
			OnFPSUpdateListener {

		public final ScaleListener scaleListener;

		private final DecimalFormat mDFormat;

		private Plane mPlane;

		public MandelRenderer(Context context) {
			super(context);
			mDFormat = new DecimalFormat("#.##");

			scaleListener = new ScaleListener();
			setFrameRate(60);
			setFPSUpdateListener(this);
		}

		@Override
		protected void initScene() {
			getCurrentScene().switchCamera(new Camera2D());

			final Plane plane = new Plane(1, 1, 1, 1, 1);
			plane.setMaterial(new MandelMaterial());
			plane.setDoubleSided(true);
			addChild(plane);
		}

		public class ScaleListener extends
				ScaleGestureDetector.SimpleOnScaleGestureListener {

			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				float scale = (float) (mPlane.getScaleX() * detector
						.getScaleFactor());
				scale = Math.max(0.1f, Math.min(scale, 5.0f));
				mPlane.setScale(scale);

				return true;
			}
		}

		@Override
		public void onFPSUpdate(double fps) {
			System.out.println("~fps: " + mDFormat.format(fps));
		}

	}

	/**
	 * Simple Mandlebrot shader translated from wikipedia.
	 * 
	 * @see <a
	 *      href="http://en.wikipedia.org/wiki/Mandelbrot_set#Escape_time_algorithm">Mandelbrot</a>
	 * 
	 */
	public class MandelMaterial extends Material {
		// @formatter:off
		protected static final String mVShader = 
			"uniform mat4 uMVPMatrix;\n" + 
			"uniform int width;\n" + 
			"uniform int height;\n" +

			"attribute vec4 aPosition;\n" + 
			"attribute vec2 aTextureCoord;\n" + 
			"attribute vec4 aColor;\n" +

			"varying vec2 vTextureCoord;\n" + 
			"varying vec4 vColor;\n" +

			"void main() {\n" + 
			"	gl_Position = uMVPMatrix * aPosition;\n" + 
			"	vTextureCoord = aTextureCoord;\n" + 
			"	vColor = vec4(1, 1, 1, 1);\n" + 
			"}\n";

		protected static final String mFShader = 
			"precision mediump float;\n" +
			"varying vec2 vTextureCoord;\n" + 
			"uniform sampler2D uDiffuseTexture;\n" + 
			"varying vec4 vColor;\n" +

			"float xtemp;\n" + 
			"float x0;\n" + 
			"float y0;\n" + 
			"float iteration;\n" + 
			"float x;\n" + 
			"float y;\n" + 
			"vec4 tCol;\n" +

			"void main() {\n" + 
			"	tCol = vec4(1, 1, 1, 1);\n" +
			"	iteration = 0.0;\n" +
			"	x = 0.0;\n" +
			"	y = 0.0;\n" +
			"	x0 = (vTextureCoord.x / 1.0 * 3.5) - 2.5;\n" + 
			"	y0 = (vTextureCoord.y / 1.0 * 2.0) - 1.0;\n" + 
			"	while(iteration < 15.0 && x*x + y*y < 2.0*2.0) {\n" + 
			"		xtemp = x*x - y*y + x0;\n" + 
			"		y = 2.0*x*y + y0;\n" + 
			"		x = xtemp;\n" + 
			"		iteration++;\n" + 
			"	}\n" + 
			"	tCol.a = 1.0;\n" + 
			"	tCol.r = 1.0 - iteration;\n" + 
			"	tCol.g = 1.0 - (iteration / 20.0);\n" + 
			"	tCol.b = 1.0 - (iteration / 200.0);\n" + 
			"	gl_FragColor = tCol;\n" + 
			"}\n";
		// @formatter:on

		public MandelMaterial() {
			super(new VertexShader(mVShader), new FragmentShader(mFShader));
		}

	}
}
