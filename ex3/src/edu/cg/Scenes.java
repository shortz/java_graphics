package edu.cg;

import edu.cg.algebra.Point;
import edu.cg.algebra.Vec;
import edu.cg.scene.Scene;
import edu.cg.scene.lightSources.CutoffSpotlight;
import edu.cg.scene.lightSources.DirectionalLight;
import edu.cg.scene.lightSources.PointLight;
// import edu.cg.scene.objects.Dome;
import edu.cg.scene.objects.Material;
import edu.cg.scene.objects.Plain;
import edu.cg.scene.objects.Shape;
import edu.cg.scene.objects.Sphere;
import edu.cg.scene.objects.Surface;

public class Scenes {
	public static Scene scene1() {
		Shape sphereShape1 = new Sphere(new Point(0.5, 0.5, 0.5), 0.5);
		Material sphereMat1 = Material.getGlassMaterial(true);
		Surface sphereSurface1 = new Surface(sphereShape1, sphereMat1);

		Shape sphereShape2 = new Sphere(new Point(-0.5, 0.5, 0.5), 0.5);
		Material sphereMat2 = Material.getGlassMaterial(true);
		Surface sphereSurface2 = new Surface(sphereShape2, sphereMat2);

		Shape sphereShape3 = new Sphere(new Point(0.5, -0.5, 0.5), 0.5);
		Material sphereMat3 = Material.getGlassMaterial(true);
		Surface sphereSurface3 = new Surface(sphereShape3, sphereMat3);

		Shape sphereShape4 = new Sphere(new Point(-0.5, -0.5, 0.5), 0.5);
		Material sphereMat4 = Material.getGlassMaterial(true);
		Surface sphereSurface4 = new Surface(sphereShape4, sphereMat4);

		DirectionalLight directionalLight = new DirectionalLight(new Vec(0.0, 0.0, -1.0),new Vec(0.7));

		return new Scene().initAmbient(new Vec(1.0))
				.initCamera(new Point(0.0, 0.0, 2.0), new Vec(0.0, 0.0 , -1.0),
						new Vec(1.0, 1.0, 0.0), 1.0)
				.addLightSource(directionalLight).addSurface(sphereSurface1).addSurface(sphereSurface2)
				.addSurface(sphereSurface3).addSurface(sphereSurface4).initName("scene1").initAntiAliasingFactor(1)
				.initRenderRefarctions(true).initRenderReflections(true).initMaxRecursionLevel(6);
	}

	public static Scene scene2() {
		// Define basic properties of the scene
		Scene finalScene = new Scene()
				.initCamera(/* Camera Position = */new Point(0.0, 2.0, 6.0), 
						/* Towards Vector = */ new Vec(0.0, -0.1 ,-1.0),
						/* Up vector = */new Vec(0.0, 1.0, 0.0), 
						/*Distance to plain =*/ 2.0)
				.initName("scene2").initAntiAliasingFactor(1)
				.initAmbient(new Vec(0.4)).initBackgroundColor(new Vec(0.2))
				.initRenderRefarctions(true).initRenderReflections(true).initMaxRecursionLevel(6);
        // Add Surfaces to the scene.
		// (1) A plain that represents the ground floor.
		Shape plainShape = new Plain(new Vec(0.0,1.0,0.0), new Point(0.0, -1.0, 0.0));
		Material plainMat = Material.getMetalMaterial();
		Surface plainSurface = new Surface(plainShape, plainMat);
		finalScene.addSurface(plainSurface);
		
		// (2) We will also add spheres to form a triangle shape (similar to a pool game). 
		for (int depth = 0; depth < 4; depth++) {
			for(int width=-1*depth; width<=depth; width++) {
				Shape sphereShape = new Sphere(new Point((double)width, 0.0, -1.0*(double)depth), 0.5);
				Material sphereMat = new Material().initKa(new Vec(0.0)).initKd(new Vec(0.0)).initKs(new Vec(0.9))
				.initShininess(2).initIsTransparent(false).initRefractionIntensity(0.0)
				.initReflectionIntensity(2.0);
				Surface sphereSurface = new Surface(sphereShape, sphereMat);
				finalScene.addSurface(sphereSurface);
			}	
		}
		
		// Add light sources:
		CutoffSpotlight cutoffSpotlight = new CutoffSpotlight(new Vec(0.0, -1.0, 0.0), 45.0);
		cutoffSpotlight.initPosition(new Point(4.0, 4.0, -3.0));
		cutoffSpotlight.initIntensity(new Vec(1.0,0.6,0.6));
		finalScene.addLightSource(cutoffSpotlight);
		cutoffSpotlight = new CutoffSpotlight(new Vec(0.0, -1.0, 0.0), 30.0);
		cutoffSpotlight.initPosition(new Point(-4.0, 4.0, -3.0));
		cutoffSpotlight.initIntensity(new Vec(0.6,1.0,0.6));
		finalScene.addLightSource(cutoffSpotlight);
		cutoffSpotlight = new CutoffSpotlight(new Vec(0.0, -1.0, 0.0), 30.0);
		cutoffSpotlight.initPosition(new Point(0.0, 4.0, 0.0));
		cutoffSpotlight.initIntensity(new Vec(0.6,0.6,1.0));
		finalScene.addLightSource(cutoffSpotlight);
		
		return finalScene;
	}
	
	public static Scene scene3() {
		// Define basic properties of the scene
		Scene finalScene = new Scene()
				.initCamera(/* Camera Position = */new Point(0.0, 2.0, 6.0), 
						/* Towards Vector = */ new Vec(0.0, -0.1 ,-1.0),
						/* Up vector = */new Vec(0.0, 1.0, 0.0), 
						/*Distance to plain =*/ 2.0)
				.initName("scene3").initAntiAliasingFactor(1)
				.initAmbient(new Vec(0.4)).initBackgroundColor(new Vec(0.2))
				.initRenderRefarctions(true).initRenderReflections(true).initMaxRecursionLevel(6);
        // Add Surfaces to the scene.
		// (1) A plain that represents the ground floor.
		Shape plainShape = new Plain(new Vec(0.0,1.0,0.0), new Point(0.0, -1.0, 0.0));
		Material plainMat = new Material().initKa(new Vec(0.0)).initKd(new Vec(0.0)).initKs(new Vec(0.9))
		.initShininess(10).initIsTransparent(false).initRefractionIntensity(0.0)
		.initReflectionIntensity(2.0);
		Surface plainSurface = new Surface(plainShape, plainMat);
		finalScene.addSurface(plainSurface);

		// (2) We will also add spheres to form a triangle shape (similar to a pool game). 
		for (int depth = 0; depth < 4; depth++) {
			for(int width=-1*depth; width<=depth; width++) {
				Shape sphereShape = new Sphere(new Point((double)width, 0.0, -1.0*(double)depth), 0.5);
				Material sphereMat = Material.getRandomMaterial();
				Surface sphereSurface = new Surface(sphereShape, sphereMat);
				finalScene.addSurface(sphereSurface);
			}	
		}
		// Add light sources:
		CutoffSpotlight cutoffSpotlight = new CutoffSpotlight(new Vec(0.0, -1.0, 0.0), 45.0);
		cutoffSpotlight.initPosition(new Point(4.0, 4.0, -3.0));
		cutoffSpotlight.initIntensity(new Vec(1.0,0.6,0.6));
		finalScene.addLightSource(cutoffSpotlight);
		cutoffSpotlight = new CutoffSpotlight(new Vec(0.0, -1.0, 0.0), 30.0);
		cutoffSpotlight.initPosition(new Point(-4.0, 4.0, -3.0));
		cutoffSpotlight.initIntensity(new Vec(0.6,1.0,0.6));
		finalScene.addLightSource(cutoffSpotlight);
		cutoffSpotlight = new CutoffSpotlight(new Vec(0.0, -1.0, 0.0), 30.0);
		cutoffSpotlight.initPosition(new Point(0.0, 4.0, 0.0));
		cutoffSpotlight.initIntensity(new Vec(0.6,0.6,1.0));
		finalScene.addLightSource(cutoffSpotlight);
		
		return finalScene;
	}
	
	public static Scene scene4() {
		// Define basic properties of the scene
		Scene finalScene = new Scene().initAmbient(new Vec(1.0))
				.initCamera(/* Camera Position = */new Point(0.0, 2.0, 6.0), 
						/* Towards Vector = */ new Vec(0.0, -0.1 ,-1.0),
						/* Up vector = */new Vec(0.0, 1.0, 0.0), 
						/*Distance to plain =*/ 2.0)
				.initName("scene4").initAntiAliasingFactor(1)
				.initAmbient(new Vec(0.4))
				.initRenderRefarctions(true).initRenderReflections(true).initMaxRecursionLevel(6);
		
		Shape plainShape = new Plain(new Vec(0.0,1.0,0.0), new Point(0.0, -1.0, 0.0));
		Material plainMat = Material.getMetalMaterial();
		plainMat.reflectionIntensity = 1.0;
		Surface plainSurface = new Surface(plainShape, plainMat);
		finalScene.addSurface(plainSurface);

		Shape sphereShape1 = new Sphere(new Point(0.0), 1.0);
		Material sphereMat1 = new Material().initKa(new Vec(0.8, 0.05, 0.05)).initKd(new Vec(0.0)).initKs(new Vec(0.9))
				.initShininess(10).initIsTransparent(false).initRefractionIntensity(0.0);
		Surface boxSurface1 = new Surface(sphereShape1, sphereMat1);
		finalScene.addSurface(boxSurface1);

		// Add lighting condition:
		CutoffSpotlight cutoffSpotlight = new CutoffSpotlight(new Vec(0.5,-0.5,0.5), 75.0);
		cutoffSpotlight.initPosition(new Point(0.0, 6.0, -10.0));
		cutoffSpotlight.initIntensity(new Vec(1.0));
		finalScene.addLightSource(cutoffSpotlight);

		return finalScene;
	}

	public static Scene scene5() {
		int pyramidHeight = 4, boxHeight=1,boxWidth=1,boxDepth=1;
		Scene pyramidScence = new Scene();
		pyramidScence.initName("Scene5");
		pyramidScence.initAmbient(new Vec(0.33));
		pyramidScence.initRenderRefarctions(true).initRenderReflections(true).initMaxRecursionLevel(6);
		// Init camera position and setup
		Point cameraPosition = new Point(10, 10, 4);
		Vec towardsVec = new Vec(-1.0, -1.0, 0.0);
		Vec upVec = new Vec(0.0, 0.0, 1.0);
		double distanceFromPlain = 0.25*cameraPosition.dist(new Point(0.0,0.0,0.0));
		pyramidScence.initCamera(cameraPosition,towardsVec, upVec, distanceFromPlain);
		// Add some light sources to the Scene
		DirectionalLight dirLight = new DirectionalLight(new Vec(-0.5, -0.5, -1.0), new Vec(0.5));
		PointLight pointLight1 = new PointLight().initPosition(new Point(12.0,4.0,6.0)).initIntensity(new Vec(1.0));
		PointLight pointLight2 = new PointLight().initPosition(new Point(4.0,12.0,6.0)).initIntensity(new Vec(1.0));
		PointLight pointLight3 = new PointLight().initPosition(new Point(-12.0,-20.0,6.0)).initIntensity(new Vec(1.0));
		pyramidScence.addLightSource(dirLight);
		pyramidScence.addLightSource(pointLight1);
		pyramidScence.addLightSource(pointLight2);
		pyramidScence.addLightSource(pointLight3);
		for (int currentHeight = 0; currentHeight < pyramidHeight;currentHeight++) {
			int numOfBoxes = (int)Math.pow(2,pyramidHeight-1) - 2*currentHeight;
			int offsetX = currentHeight*boxWidth;
			int offsetY = currentHeight*boxDepth;
			for (int i = 0; i <numOfBoxes;i++) {
				for (int j = 0; j<numOfBoxes;j++) {
					Shape sphereShape = new Sphere(new Point(offsetX+i*boxWidth, offsetY+j*boxDepth, boxHeight*currentHeight), 0.5);
					Material sphereMat = Material.getRandomMaterial();
					Surface sphereSurface = new Surface(sphereShape, sphereMat);
					pyramidScence.addSurface(sphereSurface);
				}
			}

		}
		Shape plainShape = new Plain(new Vec(0.0,1.0,0.0), new Point(0.0, -1.0, 0.0));
		Material plainMat = Material.getMetalMaterial();
		Surface plainSurface = new Surface(plainShape, plainMat);
		plainMat.reflectionIntensity = 1.0;
		pyramidScence.addSurface(plainSurface);
		return pyramidScence;
	}

}
