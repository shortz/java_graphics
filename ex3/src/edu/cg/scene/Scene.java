package edu.cg.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import edu.cg.Logger;
// import edu.cg.UnimplementedMethodException;
import edu.cg.algebra.Point;
import edu.cg.algebra.Ray;
import edu.cg.algebra.Vec;
import edu.cg.algebra.Hit;
import edu.cg.algebra.Ops;
import edu.cg.scene.camera.PinholeCamera;
import edu.cg.scene.lightSources.Light;
import edu.cg.scene.objects.Surface;

public class Scene {
	private String name = "scene";
	private int maxRecursionLevel = 1;
	private int antiAliasingFactor = 1; // gets the values of 1, 2 and 3
	private boolean renderRefarctions = false;
	private boolean renderReflections = false;

	private PinholeCamera camera;
	private Vec ambient = new Vec(1, 1, 1); // white
	private Vec backgroundColor = new Vec(0, 0.5, 1); // blue sky
	private List<Light> lightSources = new LinkedList<>();
	private List<Surface> surfaces = new LinkedList<>();

	// MARK: initializers
	public Scene initCamera(Point eyePoistion, Vec towardsVec, Vec upVec, double distanceToPlain) {
		this.camera = new PinholeCamera(eyePoistion, towardsVec, upVec, distanceToPlain);
		return this;
	}

	public Scene initAmbient(Vec ambient) {
		this.ambient = ambient;
		return this;
	}

	public Scene initBackgroundColor(Vec backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public Scene addLightSource(Light lightSource) {
		lightSources.add(lightSource);
		return this;
	}

	public Scene addSurface(Surface surface) {
		surfaces.add(surface);
		return this;
	}

	public Scene initMaxRecursionLevel(int maxRecursionLevel) {
		this.maxRecursionLevel = maxRecursionLevel;
		return this;
	}

	public Scene initAntiAliasingFactor(int antiAliasingFactor) {
		this.antiAliasingFactor = antiAliasingFactor;
		return this;
	}

	public Scene initName(String name) {
		this.name = name;
		return this;
	}

	public Scene initRenderRefarctions(boolean renderRefarctions) {
		this.renderRefarctions = renderRefarctions;
		return this;
	}

	public Scene initRenderReflections(boolean renderReflections) {
		this.renderReflections = renderReflections;
		return this;
	}

	// MARK: getters
	public String getName() {
		return name;
	}

	public int getFactor() {
		return antiAliasingFactor;
	}

	public int getMaxRecursionLevel() {
		return maxRecursionLevel;
	}

	public boolean getRenderRefarctions() {
		return renderRefarctions;
	}

	public boolean getRenderReflections() {
		return renderReflections;
	}

	@Override
	public String toString() {
		String endl = System.lineSeparator();
		return "Camera: " + camera + endl + "Ambient: " + ambient + endl + "Background Color: " + backgroundColor + endl
				+ "Max recursion level: " + maxRecursionLevel + endl + "Anti aliasing factor: " + antiAliasingFactor
				+ endl + "Light sources:" + endl + lightSources + endl + "Surfaces:" + endl + surfaces;
	}

	private transient ExecutorService executor = null;
	private transient Logger logger = null;

	private void initSomeFields(int imgWidth, int imgHeight, Logger logger) {
		this.logger = logger;
		// TODO: initialize your additional field here.
	}

	public BufferedImage render(int imgWidth, int imgHeight, double viewAngle, Logger logger)
			throws InterruptedException, ExecutionException, IllegalArgumentException {

		initSomeFields(imgWidth, imgHeight, logger);

		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		camera.initResolution(imgHeight, imgWidth, viewAngle);
		int nThreads = Runtime.getRuntime().availableProcessors();
		nThreads = nThreads < 2 ? 2 : nThreads;
		this.logger.log("Intitialize executor. Using " + nThreads + " threads to render " + name);
		executor = Executors.newFixedThreadPool(nThreads);

		@SuppressWarnings("unchecked")
		Future<Color>[][] futures = (Future<Color>[][]) (new Future[imgHeight][imgWidth]);

		this.logger.log("Starting to shoot " + (imgHeight * imgWidth * antiAliasingFactor * antiAliasingFactor)
				+ " rays over " + name);

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x)
				futures[y][x] = calcColor(x, y);

		this.logger.log("Done shooting rays.");
		this.logger.log("Wating for results...");

		for (int y = 0; y < imgHeight; ++y)
			for (int x = 0; x < imgWidth; ++x) {
				Color color = futures[y][x].get();
				img.setRGB(x, y, color.getRGB());
			}

		executor.shutdown();

		this.logger.log("Ray tracing of " + name + " has been completed.");

		executor = null;
		this.logger = null;

		return img;
	}

	private Future<Color> calcColor(int x, int y) {
		return executor.submit(() -> {
			// TODO: You need to re-implement this method if you want to handle
			// super-sampling. You're also free to change the given implementation if you
			// want.
			Point centerPoint = camera.transform(x, y);
			Ray ray = new Ray(camera.getCameraPosition(), centerPoint);
			Vec color = calcColor(ray, 0);
			return color.toColor();
		});
	}

	private Vec calcColor(Ray ray, int recusionLevel) {
		// Recusion stop condition - return a vector of zeros if we reached the max recursion level.
		if(this.maxRecursionLevel <= recusionLevel ) {
			return new Vec();
		}

		// Find the colsest intersection, if there is no hit, return background color.
		Hit hit = this.findClosestIntersection(ray);
		if(hit == null) {
			return this.backgroundColor;
		}
		// Get surface paramerts.
		Surface surface = hit.getSurface();
		Point hitPoint = ray.getHittingPoint(hit);
		Vec colorResult = this.ambient.mult(surface.Ka());

		for(Light light : this.lightSources) {
			Ray rayToLight = light.rayToLight(hitPoint);
			if (!this.isLightOccluded(light, rayToLight)){
				Vec tempColor = this.calcDiffusePart(hit, rayToLight);
				tempColor = tempColor.add(this.calcSpecularPart(hit, rayToLight, ray));
				colorResult = colorResult.add(tempColor.mult(light.intensity(hitPoint, rayToLight)));
			}
		}

		if(this.renderReflections) {
			Ray refRay = new Ray(hitPoint, Ops.reflect(ray.direction(), hit.getNormalToSurface()));
			Vec refVec = surface.Ks().mult(surface.reflectionIntensity());
			colorResult = colorResult.add(refVec.mult(calcColor(refRay, ++recusionLevel)));
		}

		return colorResult;
	}

	// Helper function to find the colsest intersection.
	private Hit findClosestIntersection(Ray ray) {
		Hit min = null;
		for(Surface surface : this.surfaces) {
			Hit hit = surface.intersect(ray);
			if (hit != null) {
				if(min == null || hit.compareTo(min) <= 0) {
					min = hit;
				}
			}
		}
		return min;
	}

	// Helper function to find if a light source is occluded from a point.
	private boolean isLightOccluded(Light light, Ray rayToLight) {
		for (Surface surface : surfaces) {
			if (light.isOccludedBy(surface, rayToLight)) {
				return true;
			}
		}
		return false;
	}

	// Helper function to calc the diffuse part of the image.
	private Vec calcDiffusePart(Hit hit, Ray rayToLight) {
		Vec Kd = hit.getSurface().Kd();
		Vec normal = hit.getNormalToSurface();
		return Kd.mult(Math.max(normal.dot(rayToLight.direction()), 0.0));
	}

	// Helper function to calc the specular part of the image.
	private Vec calcSpecularPart(Hit hit, Ray rayToLight, Ray rayFromOrigin) {
		double shininess = (double) hit.getSurface().shininess();
		Vec Ks = hit.getSurface().Ks();

		Vec L_hat = Ops.reflect(rayToLight.direction().neg(), hit.getNormalToSurface());
		double dotProduct = L_hat.dot(rayFromOrigin.direction().neg());
	
		if (dotProduct < 0) {
			return new Vec();
		}
		else{
			return Ks.mult(Math.pow(dotProduct, shininess));
		}
	}		
}
