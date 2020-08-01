package edu.cg;

import java.awt.Component;
import java.util.List;

import javax.swing.JOptionPane;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import edu.cg.algebra.Vec;
import edu.cg.models.BoundingSphere;
import edu.cg.models.Track;
import edu.cg.models.TrackSegment;
import edu.cg.models.Car.F1Car;
import edu.cg.models.Car.Specification;

/**
 * An OpenGL 3D Game.
 *
 */
public class NeedForSpeed implements GLEventListener {
	private GameState gameState = null; // Tracks the car movement and orientation
	private F1Car car = null; // The F1 car we want to render
	private Vec carCameraTranslation = null; // The accumulated translation that should be applied on the car, camera
												// and light sources
	private Track gameTrack = null; // The game track we want to render
	private FPSAnimator ani; // This object is responsible to redraw the model with a constant FPS
	private Component glPanel; // The canvas we draw on.
	private boolean isModelInitialized = false; // Whether model.init() was called.
	private boolean isDayMode = true; // Indicates whether the lighting mode is day/night.
	private boolean isBirdseyeView = false; // Indicates whether the camera is looking from above on the scene or
											// looking
	// towards the car direction.
	// - Car initial position (should be fixed).
	// - Camera initial position (should be fixed)
	// - Different camera settings
	// - Light colors
	// Or in short anything reusable - this make it easier for your to keep track of your implementation.
	private float[] sunColor;
	private float[] shadeColor;
	private Vec cameraPosition;
	private Vec carPosition;
	private Vec cameraUpVec;
	
	public NeedForSpeed(Component glPanel) {
		this.glPanel = glPanel;
		gameState = new GameState();
		gameTrack = new Track();
		carCameraTranslation = new Vec(0.0);
		car = new F1Car();

		// Constants and extra
		sunColor = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
		shadeColor = new float[]{0.1f, 0.1f, 0.1f, 1.0f};
		cameraPosition = new Vec(0, 2, 2);
		carPosition = new Vec(0, 1.7, -4);
		cameraUpVec = new Vec(0, 0.7, -0.3);
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		if (!isModelInitialized) {
			initModel(gl);
		}
		if (isDayMode) {
			gl.glClearColor(0.44f, 0.77f, 0.91f, 1.0f);
		} else {
			gl.glClearColor(0.0f, 0.0f, 0.3f, 1.0f);
		}
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();

		// Step (1) Update the accumulated translation that needs to be
		// applied on the car, camera and light sources.
		updateCarCameraTranslation(gl);
		// Step (2) Position the camera and setup its orientation
		setupCamera(gl);
		// Step (3) setup the lights.
		setupLights(gl);
		// Step (4) render the car.
		renderCar(gl);
		// Step (5) render the track.
		renderTrack(gl);
		// Step (6) check collision. Note this has nothing to do with OpenGL.
		if (checkCollision()) {
			JOptionPane.showMessageDialog(this.glPanel, "Game is Over");
			this.gameState.resetGameState();
			this.carCameraTranslation = new Vec(0.0);
		}

	}

	/**
	 * @return Checks if the car intersects the one of the boxes on the track.
	 */
	private boolean checkCollision() {
		List<BoundingSphere> carSpheres = car.getBoundingSpheres();
		List<BoundingSphere> trackBoundingSpheres = gameTrack.getBoundingSpheres();

		// Loop over the boxes and check if their sphere intersect with the car's spheres.
		for (BoundingSphere boxSphere : trackBoundingSpheres) {
			for (BoundingSphere carSphere : carSpheres) {
				if (boxSphere.checkIntersection(carSphere)){
					return true;
				}
			}
		}
		return false;
	}

	private void updateCarCameraTranslation(GL2 gl) {
		// Update the car and camera translation values (not the ModelView-Matrix).
		// - Always keep track of the car offset relative to the starting
		// point.
		// - Change the track segments here.
		Vec ret = gameState.getNextTranslation();
		carCameraTranslation = carCameraTranslation.add(ret);
		double dx = Math.max(carCameraTranslation.x, -TrackSegment.ASPHALT_TEXTURE_DEPTH / 2.0 - 2);
		carCameraTranslation.x = (float) Math.min(dx, TrackSegment.ASPHALT_TEXTURE_DEPTH / 2.0 + 2);
		if (Math.abs(carCameraTranslation.z) >= TrackSegment.TRACK_LENGTH + 10.0) {
			carCameraTranslation.z = -(float) (Math.abs(carCameraTranslation.z) % TrackSegment.TRACK_LENGTH);
			gameTrack.changeTrack(gl);
		}
	}

	private void setupCamera(GL2 gl) {
		GLU glu = new GLU();
		if (isBirdseyeView) {
			cameraPosition = new Vec(0, 70, 0);
			carPosition = new Vec(0, 1.7, -4);
			cameraUpVec = new Vec(0, 0.7, -0.3);
			Vec eyeVec = cameraPosition.add(carCameraTranslation);
			Vec centerVec = carPosition.add(carCameraTranslation);

			glu.gluLookAt(eyeVec.x, eyeVec.y, eyeVec.z,
				      centerVec.x, centerVec.y, centerVec.z,
					  cameraUpVec.x, cameraUpVec.y, cameraUpVec.z
					  );
		} else {
			cameraPosition = new Vec(0, 2, 2);
			carPosition = new Vec(0, 1.7, -4);
			cameraUpVec = new Vec(0, 0.7, -0.3);
			Vec eyeVec = cameraPosition.add(carCameraTranslation);
			Vec centerVec = carPosition.add(carCameraTranslation);

			glu.gluLookAt(eyeVec.x, eyeVec.y, eyeVec.z,
				      centerVec.x, centerVec.y, centerVec.z,
					  cameraUpVec.x, cameraUpVec.y, cameraUpVec.z
					  );
		}

	}

	private void setupLights(GL2 gl) {
		
		if (isDayMode) {
			// Switch off night lights.
			gl.glDisable(GL2.GL_LIGHT1);
			gl.glDisable(GL2.GL_LIGHT2);
			gl.glDisable(GL2.GL_LIGHT3);

			Vec dir = new Vec(0, 1, 1).normalize();
			float[] pos = new float[]{dir.x, dir.y, dir.z, 0.0f};
	
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, sunColor, 0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, sunColor, 0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, pos, 0);
			gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, shadeColor, 0);
			gl.glEnable(GL2.GL_LIGHT0);

		} else {
			// * Remember: switch-off any light sources that are used in day mode
			// * Remember: spotlight sources also move with the camera.
			// * You may simulate moon-light using ambient light.
			float[] spotColor = new float[]{0.85f, 0.85f, 0.85f, 1.0f};
			float[] spotDirection = new float[]{0.0f, -1.0f, 0.0f};
			// Switch off daylight.
			gl.glDisable(GL2.GL_LIGHT0);
			// Setup moon light using ambiant only.
			gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, shadeColor, 0);
			float[] pos1 = new float[]{this.carCameraTranslation.x, 10.0f + this.carCameraTranslation.y, this.carCameraTranslation.z, 1.0f};
			this.setupSpotlight(gl, GL2.GL_LIGHT2, spotColor, spotDirection, pos1);
			float[] pos2 = new float[]{this.carCameraTranslation.x, -10.0f + this.carCameraTranslation.y, -15.0f + this.carCameraTranslation.z, 1.0f};
			this.setupSpotlight(gl, GL2.GL_LIGHT3, spotColor, spotDirection, pos2);
		}

	}

	private void renderTrack(GL2 gl) {
		// * Note: the track is not translated. It should be fixed.
		gl.glPushMatrix();
		gameTrack.render(gl);
		gl.glPopMatrix();
	}

	private void renderCar(GL2 gl) {
		double carRotation = this.gameState.getCarRotation();
		double carZPosition = -(Math.abs(carPosition.z) + Math.abs(cameraPosition.z));

		gl.glPushMatrix();
		gl.glTranslated((double)this.carCameraTranslation.x, 
						Specification.TIRE_RADIUS*2 + (double)this.carCameraTranslation.y, 
						carZPosition + (double)this.carCameraTranslation.z);
		
		gl.glRotated(- carRotation, 0, 1, 0);
		gl.glRotated(90, 0, 0.1, 0);
		gl.glScaled(4, 4, 4);
		this.car.render(gl);
		gl.glPopMatrix();
	}

	public GameState getGameState() {
		return gameState;
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// Initialize display callback timer
		ani = new FPSAnimator(30, true);
		ani.add(drawable);
		glPanel.repaint();

		initModel(gl);
		ani.start();
	}

	public void initModel(GL2 gl) {
		gl.glCullFace(GL2.GL_BACK);
		gl.glEnable(GL2.GL_CULL_FACE);

		gl.glEnable(GL2.GL_NORMALIZE);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_SMOOTH);

		car.init(gl);
		gameTrack.init(gl);
		isModelInitialized = true;
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLU();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(57, width / height, cameraPosition.z, 500);
	}

	/**
	 * Start redrawing the scene with 30 FPS
	 */
	public void startAnimation() {
		if (!ani.isAnimating())
			ani.start();
	}

	/**
	 * Stop redrawing the scene with 30 FPS
	 */
	public void stopAnimation() {
		if (ani.isAnimating())
			ani.stop();
	}

	public void toggleNightMode() {
		isDayMode = !isDayMode;
	}

	public void changeViewMode() {
		isBirdseyeView = !isBirdseyeView;
	}


	private void setupSpotlight(GL2 gl, int light, float[] color, float[] direction, float[] pos) {
		gl.glLightfv(light, GL2.GL_POSITION, pos, 0);
		gl.glLightf(light, GL2.GL_SPOT_CUTOFF, 75.0f);
		gl.glLightfv(light, GL2.GL_SPOT_DIRECTION, direction, 0);
		gl.glLightfv(light, GL2.GL_SPECULAR, color, 0);
		gl.glLightfv(light, GL2.GL_DIFFUSE, color, 0);
		gl.glEnable(light);	
	}
}
