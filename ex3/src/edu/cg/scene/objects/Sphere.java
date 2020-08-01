package edu.cg.scene.objects;

import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Ops;
import edu.cg.algebra.Vec;

public class Sphere extends Shape {
	private Point center;
	private double radius;
	
	public Sphere(Point center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Sphere() {
		this(new Point(0, -0.5, -6), 0.5);
	}
	
	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Sphere:" + endl + 
				"Center: " + center + endl +
				"Radius: " + radius + endl;
	}
	
	public Sphere initCenter(Point center) {
		this.center = center;
		return this;
	}
	
	public Sphere initRadius(double radius) {
		this.radius = radius;
		return this;
	}
	
	@Override
	public Hit intersect(Ray ray) {
		// 2 * (V dot (P0-C))
		double b = ray.direction().mult(2.0).dot(ray.source().sub(this.center));

		// Calc discriminant.
		double discriminant = Math.sqrt(b * b - 4.0 * (this.plug(ray.source())));
		if (Double.isNaN(discriminant)) {
			return null;
		}

		double t1 = (- b - discriminant) / 2.0;
		double t2 = (- b + discriminant) / 2.0;
		if (t2 < Ops.epsilon) {
			return null;
		}
		double minT = t1;
		Vec normal = this.normal(ray.add(t1));

		if (minT > Ops.infinity) {
			return null;
		}
		return new Hit(minT, normal);
	}

	public double plug(Point p) {
		return p.distSqr(this.center) - this.radius * this.radius;
	}
	private Vec normal(Point p) {
		return p.sub(this.center).normalize();
	}
}
