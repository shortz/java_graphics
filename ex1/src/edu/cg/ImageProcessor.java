package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor extends FunctioalForEachLoops {
	// MARK: fields
	public final Logger logger;
	public final BufferedImage workingImage;
	public final RGBWeights rgbWeights;
	public final int inWidth;
	public final int inHeight;
	public final int workingImageType;
	public final int outWidth;
	public final int outHeight;

	// MARK: constructors
	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights, int outWidth,
			int outHeight) {
		super(); // initializing for each loops...

		this.logger = logger;
		this.workingImage = workingImage;
		this.rgbWeights = rgbWeights;
		inWidth = workingImage.getWidth();
		inHeight = workingImage.getHeight();
		workingImageType = workingImage.getType();
		this.outWidth = outWidth;
		this.outHeight = outHeight;
		setForEachInputParameters();
	}

	public ImageProcessor(Logger logger, BufferedImage workingImage, RGBWeights rgbWeights) {
		this(logger, workingImage, rgbWeights, workingImage.getWidth(), workingImage.getHeight());
	}

	// Changes the picture's hue - example
	public BufferedImage changeHue() {
		logger.log("Prepareing for hue changing...");

		int r = rgbWeights.redWeight;
		int g = rgbWeights.greenWeight;
		int b = rgbWeights.blueWeight;
		int max = rgbWeights.maxWeight;

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			Color c = new Color(workingImage.getRGB(x, y));
			int red = r * c.getRed() / max;
			int green = g * c.getGreen() / max;
			int blue = b * c.getBlue() / max;
			Color color = new Color(red, green, blue);
			ans.setRGB(x, y, color.getRGB());
		});

		logger.log("Changing hue done!");

		return ans;
	}

	// Sets the ForEach parameters with the input dimensions
	public final void setForEachInputParameters() {
		setForEachParameters(inWidth, inHeight);
	}

	// Sets the ForEach parameters with the output dimensions
	public final void setForEachOutputParameters() {
		setForEachParameters(outWidth, outHeight);
	}

	// A helper method that creates an empty image with the specified input dimensions.
	public final BufferedImage newEmptyInputSizedImage() {
		return newEmptyImage(inWidth, inHeight);
	}

	// A helper method that creates an empty image with the specified output dimensions.
	public final BufferedImage newEmptyOutputSizedImage() {
		return newEmptyImage(outWidth, outHeight);
	}

	// A helper method that creates an empty image with the specified dimensions.
	public final BufferedImage newEmptyImage(int width, int height) {
		return new BufferedImage(width, height, workingImageType);
	}

	// A helper method that deep copies the current working image.
	public final BufferedImage duplicateWorkingImage() {
		BufferedImage output = newEmptyInputSizedImage();

		forEach((y, x) -> output.setRGB(x, y, workingImage.getRGB(x, y)));

		return output;
	}
	
	public BufferedImage greyscale() {
		logger.log("Prepareing for grey scale.");

		BufferedImage ans = newEmptyInputSizedImage();

		forEach((y, x) -> {
			int grayLevel = convertRGBlToGray(workingImage, x, y);

			Color greyColor = new Color(grayLevel, grayLevel, grayLevel);
			ans.setRGB(x, y, greyColor.getRGB());
		});

		logger.log("Grey scaling done!");

		return ans;
	}

	public final int convertRGBlToGray(BufferedImage img, int width, int hight) {
		// Acquire the current color
		Color c = new Color(img.getRGB(width, hight));

		// Calculate the weighted sum
		double pixelSum = c.getRed() * rgbWeights.redWeight
				+ c.getGreen() * rgbWeights.greenWeight
				+ c.getBlue() * rgbWeights.blueWeight;

		// Average through division
		int greyLevel = (int)(pixelSum / rgbWeights.weightsAmount);

		return greyLevel;
	}

	public BufferedImage nearestNeighbor() {
		logger.log("Preparing nearest neighbor resizing...");

		BufferedImage ans = newEmptyOutputSizedImage();

		double ratioX = inWidth / (double)outWidth;
		double ratioY = inHeight / (double)outHeight;

		this.setForEachOutputParameters();

		forEach((y, x) -> {
			int nearestX = (int)(x * ratioX);
			int nearestY = (int)(y * ratioY);
			ans.setRGB(x, y, workingImage.getRGB(nearestX, nearestY));
		});

		logger.log("Nearest-neighbor resizing done!");

		return ans;
	}
}
