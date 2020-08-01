package edu.cg.models.Car;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import edu.cg.models.IRenderable;
import edu.cg.models.SkewedBox;

public class FrontBumber implements IRenderable {
	private SkewedBox bumperBox;
	private SkewedBox boxOfWings;

	public FrontBumber(){
		this.bumperBox = new SkewedBox(Specification.F_BUMPER_LENGTH, 
									   Specification.F_BUMPER_HEIGHT_1, 
									   Specification.F_BUMPER_HEIGHT_2, 
									   Specification.F_BUMPER_DEPTH, 
									   Specification.F_BUMPER_DEPTH);

		this.boxOfWings = new SkewedBox(Specification.F_BUMPER_LENGTH, 
										Specification.F_BUMPER_WINGS_HEIGHT_1, 
										Specification.F_BUMPER_WINGS_HEIGHT_2, 
										Specification.F_BUMPER_WINGS_DEPTH, 
										Specification.F_BUMPER_WINGS_DEPTH);
	}



	@Override
	public void render(GL2 gl) {
		// Remember the dimensions of the bumper, this is important when you
		// combine the bumper with the hood.
		GLU glu = new GLU();
		GLUquadric quadratic = glu.gluNewQuadric();
		gl.glPushMatrix();
		Materials.SetBlackMetalMaterial(gl);
		this.bumperBox.render(gl);
		gl.glTranslated(0.0, 0.0, Specification.F_BUMPER_DEPTH - Specification.F_BUMPER_WINGS_DEPTH);
		this.renderBumperWing(gl, glu, quadratic);
		gl.glTranslated(0.0, 0.0, -2*Specification.F_BUMPER_DEPTH + 2*Specification.F_BUMPER_WINGS_DEPTH);
		this.renderBumperWing(gl, glu, quadratic);
		gl.glPopMatrix();
		glu.gluDeleteQuadric(quadratic);
	}

	@Override
	public void init(GL2 gl) {
	}

	@Override
	public String toString() {
		return "FrontBumper";
	}

	private void renderBumperWing(GL2 gl, GLU glu, GLUquadric quadratic) {
		gl.glPushMatrix();
		Materials.SetBlackMetalMaterial(gl);
		this.boxOfWings.render(gl);
		Materials.SetRedMetalMaterial(gl);
		gl.glTranslated(0.0, 0.034, 0.0);
		glu.gluSphere(quadratic, 0.032, 25, 25);
		gl.glPopMatrix();
	}

	public void destroy(GL2 gl) {
    }

}
