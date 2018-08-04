# Image processing

## Overview

Image processing using convolution of basic kernels: box blurs, edge detection, sharpening, and laplace approximation. Uses Java AWT Buffered image.

Kernel class which is used for the processing, usage:

```
val photo = ImageIO.read(new File(myPhotoPath.concat("myImage.jpg"))) //image to process as BufferedImage

val k =  new kernel() //init kernel

val out = k.edgedetection(photo) //run edgedetection on the input photo and output a new buffered image
```

Corner detection uses Harris Stephens algorihm, usage:

```
val photo = ImageIO.read(new File(myPhotoPath.concat("myImage.jpg"))) //image to process as BufferedImage

val fD = new featureDetection() //init feature detection

val corners = fD.harrisStephens(photo) //get array of object containing x, y and R of corners

val out = fD.displayCorners(corners, photo) //get BufferedImage with corner pixels in red
```


Also contains an Image object which shows example usage, and a testing class for debugging feature detection class.

## Kernel Convolution

Input image

![A Botticelli painting](/images/flowers.jpg)

Box blur

```
BufferedImage outputPhoto = kernel.boxblur(BufferedImage inputPhoto, [Int sizeOfKernel], [String "grayscale"|"color"])
```

![A blurry Botticelli painting](/images/boxblur.jpg)

Edge detection

```
BufferedImage outputPhoto = kernel.edgedetection(BufferedImage inputPhoto, [String "grayscale"|"color"])
```

![An outlined Botticelli painting](/images/edgeDetection.jpg)

Sharpened

```
BufferedImage outputPhoto = kernel.sharpen(BufferedImage inputPhoto, [String "grayscale"|"color"])
```

![A very sharp Botticelli painting](/images/sharpen.jpg)

Discrete laplace approximation

```
BufferedImage outputPhoto = kernel.laplace(BufferedImage inputPhoto, [String "grayscale"|"color"])
```

![A kinda faded Botticelli painting](/images/laplace.jpg)

Emboss

```
BufferedImage outputPhoto = kernel.emboss(BufferedImage inputPhoto, [String "grayscale"|"color"])
```

![An embossed Botticelli painting](/images/emboss.jpg)

Roberts

```
BufferedImage outputPhoto = kernel.roberts(BufferedImage inputPhoto, [String "grayscale"|"color"])
```

![A differed edge detected Botticelli painting](/images/roberts.jpg)

Prewitt

```
BufferedImage outputPhoto = kernel.prewitt(BufferedImage inputPhoto, [String "grayscale"|"color"])
```

![Another edge detected Botticelli painting](/images/prewitt.jpg)

Sobel

```
BufferedImage outputPhoto = kernel.sobel(BufferedImage inputPhoto, [String "grayscale"|"color"])
```

![Another edge detected Botticelli painting](/images/sobel.jpg)



Custom Kernel - This example is a 5x5 Gaussian blur. Scaling is handled by the class.

```
BufferedImage outputPhoto = kernel.customKernel(BufferedImage inputPhoto, Array[Array[Double]], [String "grayscale"|"color"])
```

![A blurry Botticelli painting](/images/gaussian.jpg)

Color - Each RGB channel is filtered independently. This example is a laplace transformation

![A kinda faded Botticelli painting](/images/color.jpg)

## Corner Detection

Feature detection algorithm calculates the tensor, M, of an image. 

![M = sum of the window function times the matrix of partial derivaties](/math/M.jpg)

Currently the window function, w(x, y), is a constant (1), however a Gaussian and Box filter will be implemented.

Partial spatial derivatives, I_x and I_y, are calculated using Sobel operators. For I_x:

![I_x = Sobel operator convolved onto the input imaged](/math/I_x.jpg)

The likelihood of a corner is determined by R, given by the formula:

![R = determinant of M - kappa * square of the trace of M](/math/R.jpg)

The determinant is:

![Det of M = M_11 times M_22 - M_12 times M_21 = lambda_1 times Lambda_2](/math/det.jpg)

Where lambda are the eigenvalues of M.

The trace is:

![Trace of M = M_11 - M_22 = lambda_1 plus lambda_2](/math/trace.jpg)

The outcome:

![A window with corners highlighted in red](/images/harrisStephens.jpg)

