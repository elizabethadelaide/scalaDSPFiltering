# scalaDSPFiltering
Learning Scala with some DSP

Image processing using convolution of basic kernels: box blurs, edge detection, sharpening, and laplace approximation. Uses Java AWT Buffered image.

Contains a kernel class which is used for the processing, usage:

```
val photo = ImageIO.read(new File(myPhotoPath.concat("funhat.jpg"))) //image to process as bufferedImage

val k =  new kernel() //init kernel

val out = k.edgedetection(photo) //run edgedetection on the input photo and output a new buffered image
```

Also contains an Image object for testing.

# Example run

Input image

![A regular fun crab hat](/images/funhat.jpg)

Box blur

```
BufferedImage outputPhoto = kernel.boxblur(BufferedImage inputPhoto, [Int sizeOfKernel])
```

![A blurry fun crab hat](/images/boxblur.jpg)

Edge detection

```
BufferedImage outputPhoto = kernel.edgedetection(BufferedImage inputPhoto)
```

![An outlined fun crab hat](/images/edgeDetection.jpg)

Sharpened

```
BufferedImage outputPhoto = kernel.sharpen(BufferedImage inputPhoto)
```

![A very sharp fun crab hat](/images/sharpen.jpg)

Discrete laplace approximation

```
BufferedImage outputPhoto = kernel.laplace(BufferedImage inputPhoto)
```

![A kinda faded fun crab hat](/images/laplace.jpg)

Emboss

```
BufferedImage outputPhoto = kernel.emboss(BufferedImage inputPhoto)
```

![An embossed crab hat](/images/emboss.jpg)

Roberts

```
BufferedImage outputPhoto = kernel.roberts(BufferedImage inputPhoto)
```

![A differed edge detected crab hat](/images/roberts.jpg)

Prewitt

```
BufferedImage outputPhoto = kernel.prewitt(BufferedImage inputPhoto)
```

![Another edge detected crab hat](/images/prewitt.jpg)

Custom Kernel - This example is a 5x5 Gaussian blur. Scaling is handled by the class.

```
BufferedImage outputPhoto = kernel.customKernel(BufferedImage inputPhoto, Array[Array[Double]])
```

![A blurry crab hat](/images/gaussian.jpg)
