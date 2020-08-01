package edu.cg.models;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import edu.cg.algebra.Point;

public class BoundingSphere implements IRenderable {
	private double radius = 0.0;
	private Point center;
	private double color[];

	public BoundingSphere(double radius, Point center) {
		color = new double[3];
		this.setRadius(radius);
		this.setCenter(new Point(center.x, center.y, center.z));
	}

	public void setSphereColore3d(double r, double g, double b) {
		this.color[0] = r;
		this.color[1] = g;
		this.color[2] = b;
	}

	/**
	 * Given a sphere s - check if this sphere and the given sphere intersect.
	 * 
	 * @return true if the spheres intersects, and false otherwise
	 */
	public boolean checkIntersection(BoundingSphere s) {
		return this.center.dist(s.center) <= this.radius + s.radius;
	}

	public void translateCenter(double dx, double dy, double dz) {
		this.center = this.center.add(new Point(dx, dy, dz));
	}

	@Override
	public void render(GL2 gl) {
		// Set color.
		gl.glColor3d(this.color[0], this.color[1], this.color[2]);
		gl.glPushMatrix();
		// Render the sphere.
		GLU glu = new GLU();
		GLUquadric quadratic = glu.gluNewQuadric();
		gl.glTranslated((double)this.center.x, (double)this.center.y, (double)this.center.z);
		glu.gluSphere(quadratic, this.radius, 10, 10);
		glu.gluDeleteQuadric(quadratic);
		gl.glPopMatrix();
	}

	@Override
	public void init(GL2 gl) {
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public Point getCenter() {
		return center;
	}

	public void setCenter(Point center) {
		this.center = center;
	}

	public void destroy(GL2 gl) {
    }

}
