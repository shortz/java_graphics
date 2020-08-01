package edu.cg.models.Car;

import java.util.LinkedList;
import java.util.List;

import com.jogamp.opengl.GL2;
import edu.cg.models.BoundingSphere;
import edu.cg.models.IIntersectable;
import edu.cg.models.IRenderable;
import edu.cg.algebra.Point;

public class Front implements IRenderable, IIntersectable {
	private FrontHood hood = new FrontHood();
	private PairOfWheels wheels = new PairOfWheels();
	private FrontBumber bumpber = new FrontBumber();

	@Override
	public void render(GL2 gl) {
		gl.glPushMatrix();
		// Render hood - Use Red Material.
		gl.glTranslated(-Specification.F_LENGTH / 2.0 + Specification.F_HOOD_LENGTH / 2.0, 0.0, 0.0);
		hood.render(gl);
		// Render the wheels.
		gl.glTranslated(Specification.F_HOOD_LENGTH / 2.0 - 1.25 * Specification.TIRE_RADIUS,
				0.5 * Specification.TIRE_RADIUS, 0.0);
		wheels.render(gl);
		gl.glTranslated(Specification.F_LENGTH / 2.0 - Specification.F_BUMPER_LENGTH + 0.01, 
						-0.5 * Specification.TIRE_RADIUS, 0.0);
		bumpber.render(gl);
		gl.glPopMatrix();
	}

	@Override
	public void init(GL2 gl) {
	}

	@Override
	public List<BoundingSphere> getBoundingSpheres() {
		// s1
		// where:
		// s1 - sphere bounding the car front
		LinkedList<BoundingSphere> res = new LinkedList<BoundingSphere>();
		double diagonal = Math.pow(Specification.F_DEPTH, 2.0) + Math.pow(Specification.F_LENGTH, 2.0) + Math.pow(Specification.F_HEIGHT, 2.0);
		diagonal = Math.sqrt(diagonal);
		res.add(new BoundingSphere(diagonal/2, new Point(0.0, Specification.F_HEIGHT/2, 0.0)));
		return res;
	}

	@Override
	public String toString() {
		return "CarFront";
	}

	public void destroy(GL2 gl) {
    }
}
