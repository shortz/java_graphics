package edu.cg;

import java.awt.image.BufferedImage;

public class SeamsCarver extends ImageProcessor {

	// MARK: An inner interface for functional programming.
	@FunctionalInterface
	interface ResizeOperation {
		BufferedImage resize();
	}

	// MARK: Fields
	private int numOfSeams;
	private ResizeOperation resizeOp;

    private boolean[][] imageMask; // The original image mask
    private boolean[][] postCarveMask; // The original image mask, carved

    private int[][] originalIndexMatrix; // The original X mapping matrix
    private int[][] greyImage; // Pixel grey scale values per given RGB weights
    private boolean[][] seamedPixels; // Remembered seamed pixels

    private int currWidth; // The current width of the working bounds

	public SeamsCarver(Logger logger, BufferedImage workingImage, int outWidth, RGBWeights rgbWeights,
			boolean[][] imageMask) {
		super((s) -> logger.log("Seam carving: " + s), workingImage, rgbWeights, outWidth, workingImage.getHeight());

		numOfSeams = Math.abs(outWidth - inWidth);
		this.imageMask = imageMask;
		if (inWidth < 2 | inHeight < 2)
			throw new RuntimeException("Can not apply seam carving: workingImage is too small");

		if (numOfSeams > inWidth / 2)
			throw new RuntimeException("Can not apply seam carving: too many seams...");

		// Setting resizeOp by with the appropriate method reference
		if (outWidth > inWidth)
			resizeOp = this::increaseImageWidth;
		else if (outWidth < inWidth)
			resizeOp = this::reduceImageWidth;
		else
			resizeOp = this::duplicateWorkingImage;

		// calculations.
		this.currWidth = inWidth;
		// Declare our matrices
        this.greyImage = new int[inHeight][inWidth];
        this.seamedPixels = new boolean[inHeight][inWidth];
        this.originalIndexMatrix = new int[inHeight][inWidth];
		
		// This method is done here since the class cannot change the new output image size anyhow.
		this.logger.log("Find K optimal seams.");
		this.findKOptimalSeams();
		this.logger.log("Found K optimal seams!");
	}

	

	public BufferedImage resize() {
		return resizeOp.resize();
	}

	private BufferedImage reduceImageWidth() {
		this.logger.log("Start image reducing.");
	
		postCarveMask = new boolean[outHeight][outWidth];
		BufferedImage ans = this.newEmptyOutputSizedImage();

        for (int i = 0; i < inHeight; i++) {
            for (int j = 0; j < currWidth; j++) {
                int orgIndex = originalIndexMatrix[i][j];
                ans.setRGB(j, i, workingImage.getRGB(orgIndex, i));
                postCarveMask[i][j] = imageMask[i][orgIndex];
			}
		}
		this.logger.log("Image reducing done.");
        return ans;
	}

	private BufferedImage increaseImageWidth() {
		this.logger.log("Start image increase.");

		postCarveMask = new boolean[outHeight][outWidth];
		BufferedImage ans = newEmptyOutputSizedImage();

		for (int i = 0; i < outHeight; i++) {
			int outputIndex = 0;
            for (int originalIndex = 0; originalIndex < inWidth; originalIndex++) {
				// Copy the seam pixel twice.
				if (seamedPixels[i][originalIndex]) {
					ans.setRGB(outputIndex, i, workingImage.getRGB(originalIndex, i));
					postCarveMask[i][outputIndex] = imageMask[i][originalIndex];
					outputIndex++;
				}
				ans.setRGB(outputIndex, i, workingImage.getRGB(originalIndex, i));
				postCarveMask[i][outputIndex] = imageMask[i][originalIndex];
				outputIndex++;
			}
		}
		this.logger.log("Image incrising done.");
		return ans;
	}

	public BufferedImage showSeams(int seamColorRGB) {
        BufferedImage ans = duplicateWorkingImage();
		for (int i=0; i < inHeight; i++){
			for (int j=0; j < inWidth; j++){
				if (seamedPixels[i][j]) {
					ans.setRGB(j, i, seamColorRGB);
				}
			}
		}
        return ans;
	}

	public boolean[][] getMaskAfterSeamCarving() {
		// This method should return the mask of the resize image after seam carving.
		// Meaning, after applying Seam Carving on the input image,
		// getMaskAfterSeamCarving() will return a mask, with the same dimensions as the
		// resized image, where the mask values match the original mask values for the
		// corresponding pixels.
		// HINT: Once you remove (replicate) the chosen seams from the input image, you
		// need to also remove (replicate) the matching entries from the mask as well.

		// If we didn't create a post carve mask, meaning we had no seams, return the original map
		if (postCarveMask == null){
			return imageMask;
		} else {
			return postCarveMask;
		}
	}

	/**
     * Find K optimal seams
     */
    private void findKOptimalSeams() {
		int[] seam;
        // Convert image to gray scale and set the original indices matrix.
        for (int i = 0; i < inHeight; i++) {
            for (int j = 0; j < inWidth; j++) {
                originalIndexMatrix[i][j] = j;
                greyImage[i][j] = convertRGBlToGray(workingImage, j, i);
            }
		}
		// Find k optimal seams
		for (int i = 0; i < numOfSeams; i++) {
			seam = findNextOptimalSeam();
			updateSeamsPixels(seam);
			updateOriganlIndexMatrix(seam);
        }
	}
	
	/**
     * Update the original image scale seams pixels -
	 * if pixel (i,j) is part of a seam --> set true in the seam pixel matrix.
     */
	private void updateSeamsPixels(int[] seamPixelsLocation){
		// Go over each row and update the found seam column.
		for (int row = 0; row < seamPixelsLocation.length; row++){
			int seamColumn = seamPixelsLocation[row];
			int orgIndex = originalIndexMatrix[row][seamColumn];
			seamedPixels[row][orgIndex] = true;
		}
	}

	/**
     * Update the original indeces matrix.
     */
	private void updateOriganlIndexMatrix(int[] seamPixelsLocation){
		// Since we only reduce the width, we can loop only on the j values.
		for (int row = 0; row < seamPixelsLocation.length; row++){
			int seamColumn = seamPixelsLocation[row];
			// Update the row of the original matrix.
			for (int j=0; j < inWidth-1; j++){
				if (j < seamColumn){
					originalIndexMatrix[row][j] = originalIndexMatrix[row][j];
				} else {
					originalIndexMatrix[row][j] = originalIndexMatrix[row][j+1];
				}
			}
		}
	}

	/**
     * Finds an optimal seam in the current image.
     * We use currentWidth param and the update indeices matrix to keep track 
	 * on the "current image".
     */
	private int[] findNextOptimalSeam() {
		// The cost matrix has two values - 0 is the actual cost, 1 is the path it came from.
		long[][][] costs = new long[inHeight][currWidth][2];
		int[] seam = new int[inHeight]; // Keep the seam path (column number).
		long leftCost = Long.MAX_VALUE;
		long rightCost = Long.MAX_VALUE;
		long midCost = Long.MAX_VALUE;
		// We need to make sure the value will never overflow.
		long maxVal = Long.MAX_VALUE / (long)inHeight - 1;

		// Calculate the costs matrix via dynamic programming.
		// Set first row.
		for (int j=0; j < currWidth; j++){
			costs[0][j][0] = calcPixelEnergy(0, j);
			costs[0][j][1] = -1; // -1 meaning it is the last row in the seam path.
		}
		// All other rows of the cost matrix.
		for (int i = 1; i < inHeight; i++) {
			for (int j = 0; j < currWidth; j++){
				// Update cell (i,j) - calculate left, right and middle forward costs and check minimum.
				// If we reach the edges of the picture, put the maximum value in the correct edge.
				if (j == 0) {
					leftCost = Long.MAX_VALUE;
				} else {
					leftCost = costs[i-1][j-1][0] + calcCostL(i,j);
				}

				if (j == currWidth-1) {
					rightCost = Long.MAX_VALUE;
				} else {
					rightCost = costs[i-1][j+1][0] + calcCostR(i,j);
				}

				midCost = costs[i-1][j][0] + calcCostUp(i,j);
				// Set the path for pixel (i,j)
				if ((midCost <= leftCost) && (midCost <= rightCost)){
					costs[i][j][1] = 1; 
				} else if ((rightCost <= leftCost)){
					costs[i][j][1] = 2; 
				} else {
					costs[i][j][1] = 0; 
				}
				long cheapest = Math.min(midCost, Math.min(leftCost, rightCost));
				long costVal = calcPixelEnergy(i, j) + cheapest;
				// Make sure we will never overflow.
				if (maxVal <= costVal){
					costs[i][j][0] = maxVal;
				} else {
					costs[i][j][0] = costVal;
				}
			}
		}

		// Find the cheapest seam traversal using the path remarks in the cost matrix.
		// Find the first one.
		long minValue = costs[inHeight-1][0][0];
		seam[inHeight-1] = 0;
		for(int j=1; j < currWidth; j++){
			if(costs[inHeight-1][j][0] < minValue){
				minValue = costs[inHeight-1][j][0];
				seam[inHeight-1] = j;
			}
		}
		// Find the rest of the path using the marks in the cost martix. 0 = left, 1 = middle, 2 = right.
		for (int i = inHeight-2; i >= 0; i--) {
			int j = seam[i + 1];
			if (costs[i+1][j][1] == 0) {
				seam[i] = j-1;
			} else if (costs[i+1][j][1] == 1){
				seam[i] = j;
			} else {
				seam[i] = j+1;
			}
		}
        // Update the current width
		currWidth = currWidth - 1;;
		return seam;
	}

	private long calcPixelEnergy(int i, int j){

		int height = greyImage.length;
		int xDeriv;
		int yDeriv;
		long maskVal;
		// The actual "column" of the picture.
		int originalIndex = originalIndexMatrix[i][j];

		// Backward derivitive.	
		if (j  == this.currWidth - 1) {
			int orignalIndexDown = originalIndexMatrix[i][j-1];
			xDeriv = Math.abs(greyImage[i][originalIndex] - greyImage[i][orignalIndexDown]);
		}
		// Forward derivitive. 
		else {
			int orignalIndexUp = originalIndexMatrix[i][j+1];
			xDeriv = Math.abs(greyImage[i][originalIndex] - greyImage[i][orignalIndexUp]);
		}
		// Backward derivitive.
		if (i == height - 1) {
			yDeriv = Math.abs(greyImage[i][originalIndex] - greyImage[i-1][originalIndex]);
		}
		// Forward derivitive.  
		else {
			yDeriv = Math.abs(greyImage[i][originalIndex] - greyImage[i+1][originalIndex]);
		}
		// Mask value.
		if (this.imageMask[i][originalIndex]) {
			maskVal = Long.MIN_VALUE / (long)inHeight + 1; // Make sure we never overflow.
		} else {
			maskVal = 0;
		}

		return (long)xDeriv + (long)yDeriv + maskVal;
	}

	 /**
     * The forward looking cost up.
     */
    private int calcCostUp(int i, int j) {
		// First and last columns don't count.
		if ((j == 0) || (j  == this.currWidth - 1)) return 0;
		
		int orignalXUp = originalIndexMatrix[i][j+1];
		int orignalXDown = originalIndexMatrix[i][j-1];
        // int orgX = originalImageX[x][y];
        return Math.abs(pixelsDiff(i, orignalXUp, i, orignalXDown));
    }

    /**
     * The forward looking cost to the left.
     */
    private int calcCostL(int i, int j) {
		int upcost = calcCostUp(i, j);
		// First row doesn't count.
		if ((i == 0) || (j == 0))  return 0;

		int orignalX = originalIndexMatrix[i][j];
		int orignalXDown = originalIndexMatrix[i][j-1];

		return (Math.abs(pixelsDiff(i-1, orignalX, i, orignalXDown)) + upcost);
    }

    /**
     * The forward looking cost to the right.
     */
    private int calcCostR(int i, int j) {
        int upcost = calcCostUp(i, j);
		// First row doesn't count.
		if ((i == 0) || (j  == this.currWidth - 1)) return 0;

		int orignalX = originalIndexMatrix[i][j];
		int orignalXUp = originalIndexMatrix[i][j+1];

		return (Math.abs(pixelsDiff(i-1, orignalX, i, orignalXUp)) + upcost);
    }

	private int pixelsDiff(int x1, int y1, int x2, int y2) {
        return greyImage[x1][y1] - greyImage[x2][y2];
	}

}
