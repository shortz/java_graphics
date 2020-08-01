package edu.cg.scene.camera;

// import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;
import java.lang.Math;

public class PinholeCamera {

	private Point P0;
	private Vec towardsVec;
	private Vec upVec;
	private Vec rightVec;
	private double distToPlain;
	private int Rx;
	private int Ry;
	private double viewAngle;
	private double viewPlainWidth;


	/**	
	 * Initializes a pinhole camera model with default resolution 200X200 (RxXRy)
	 * and View Angle 90.
	 * 
	 * @param cameraPosition  - The position of the camera.
	 * @param towardsVec      - The towards vector of the camera (not necessarily
	 *                        normalized).
	 * @param upVec           - The up vector of the camera.
	 * @param distanceToPlain - The distance of the camera (position) to the center
	 *                        point of the image-plain.
	 * 
	 */
	public PinholeCamera(Point cameraPosition, Vec towardsVec, Vec upVec, double distanceToPlain) {
		
		this.P0 = cameraPosition;
		this.towardsVec = towardsVec.normalize();
		this.upVec = upVec.normalize();
		this.distToPlain = distanceToPlain;
		// Calculate the right vector using the toward and the up.
		this.rightVec = this.towardsVec.cross(this.upVec).normalize();

		// Default values for resolutions.
		this.Ry = 200;
		this.Rx = 200;
		this.viewAngle = 90;
		this.convertAngleToPlainWidth(this.viewAngle);
	}

	/**
	 * Initializes the resolution and width of the image.
	 * 
	 * @param height    - the number of pixels in the y direction.
	 * @param width     - the number of pixels in the x direction.
	 * @param viewAngle - the view Angle.
	 */
	public void initResolution(int height, int width, double viewAngle) {
		this.Ry = height;
		this.Rx = width;
		this.viewAngle = viewAngle;
		this.convertAngleToPlainWidth(viewAngle);
	}

	/**
	 * Transforms from pixel coordinates to the center point of the corresponding
	 * pixel in model coordinates.
	 * 
	 * @param x - the pixel index in the x direction.
	 * @param y - the pixel index in the y direction.
	 * @return the middle point of the pixel (x,y) in the model coordinates.
	 */
	public Point transform(int x, int y) {
		Point Pc = this.P0.add(this.towardsVec.mult(this.distToPlain));
		Vec upOrtogonalVec = this.rightVec.cross(this.towardsVec).normalize();
		double ratio = this.viewPlainWidth / this.Rx;

		Vec tempX = rightVec.mult(ratio * (x - Math.floor(Rx / 2)));
        Vec tempY = upOrtogonalVec.mult(ratio * (y - Math.floor(Ry / 2))).neg();

		return Pc.add(tempX).add(tempY);
	}

	/**
	 * Returns the camera position
	 * 
	 * @return a new point representing the camera position.
	 */
	public Point getCameraPosition() {
		return new Point(P0.x, P0.y, P0.z);
	}

	// Helper function for converting the angle to the plain width.
	private void convertAngleToPlainWidth(double angle) {
		// Conver the angle to plain width
		double halfAngle = angle/2;
        double angleRadians = Math.toRadians(halfAngle); 
		this.viewPlainWidth = 2 * this.distToPlain * (Math.tan(angleRadians));
	}
}
