package edu.cg.models.Car;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.*;
import edu.cg.algebra.Point;
import edu.cg.models.BoundingSphere;
import edu.cg.models.IIntersectable;
import edu.cg.models.IRenderable;

/**
 * A F1 Racing Car.
 *
 */
public class F1Car implements IRenderable, IIntersectable {
	// Remember to include a ReadMe file specifying what you implemented.
	Center carCenter = new Center();
	Back carBack = new Back();
	Front carFront = new Front();

	@Override
	public void render(GL2 gl) {
		carCenter.render(gl);
		gl.glPushMatrix();
		gl.glTranslated(-Specification.B_LENGTH / 2.0 - Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		carBack.render(gl);
		gl.glPopMatrix();
		gl.glPushMatrix();
		gl.glTranslated(Specification.F_LENGTH / 2.0 + Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		carFront.render(gl);
		gl.glPopMatrix();

	}

	@Override
	public String toString() {
		return "F1Car";
	}

	@Override
	public void init(GL2 gl) {

	}

	@Override
	public List<BoundingSphere> getBoundingSpheres() {
		// s1 -> s2 -> s3 -> s4
		// where:
		// s1 - sphere bounding the whole car
		// s2 - sphere bounding the car front
		// s3 - sphere bounding the car center
		// s4 - sphere bounding the car back
		//
		// * NOTE:
		// All spheres should be adapted so that they are place relative to
		// the car model coordinate system.
		LinkedList<BoundingSphere> res = new LinkedList<BoundingSphere>();

		// Obviosly the car length is longer then its depth, so we will use only the legnth as the radius.
		double carLength = Specification.C_LENGTH + Specification.F_LENGTH + Specification.B_LENGTH + Specification.F_BUMPER_LENGTH; 
		double carHight = Math.max(Math.max(Specification.C_HIEGHT, Specification.B_HEIGHT), Specification.F_HEIGHT);
		double carDepth = Math.max(Math.max(Specification.C_DEPTH, Specification.B_DEPTH), Specification.F_DEPTH);
		double diagonal = Math.pow(carLength, 2.0) + Math.pow(carHight, 2.0) + Math.pow(carDepth, 2.0);
		diagonal = Math.sqrt(diagonal);
		res.add(new BoundingSphere(diagonal/2, new Point(0.0, carHight/2, 0.0)));

		List<BoundingSphere> spheresFromFront = this.carFront.getBoundingSpheres();
		for (BoundingSphere sphere : spheresFromFront) {
			sphere.setSphereColore3d(1.0, 0.0, 0.0);
			sphere.translateCenter(Specification.F_LENGTH / 2.0 + Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		}
		res.addAll(spheresFromFront);

		List<BoundingSphere> spheresFromCenter = this.carCenter.getBoundingSpheres();
		for (BoundingSphere sphere : spheresFromCenter) {
			sphere.setSphereColore3d(0.0, 1.0, 0.0);
		}
		res.addAll(spheresFromCenter);

		List<BoundingSphere> spheresFromBack = this.carBack.getBoundingSpheres();
		for (BoundingSphere sphere : spheresFromBack) {
			sphere.setSphereColore3d(0.0, 0.0, 1.0);
			sphere.translateCenter(-Specification.B_LENGTH / 2.0 - Specification.C_BASE_LENGTH / 2.0, 0.0, 0.0);
		}
		res.addAll(spheresFromBack);

		return res;
	}

	public void destroy(GL2 gl) {
    }
}
