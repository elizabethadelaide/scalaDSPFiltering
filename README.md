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

![An embossed Botticelli painting(/images/emboss.jpg)

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

Custom Kernel - This example is a 5x5 Gaussian blur. Scaling is handled by the class.

```
BufferedImage outputPhoto = kernel.customKernel(BufferedImage inputPhoto, Array[Array[Double]], [String "grayscale"|"color"])
```

![A blurry Botticelli painting](/images/gaussian.jpg)

Color - Each RGB channel is filtered independenetly. This example is a laplace transformation

![A kinda faded Botticelli painting](/images/color.jpg)
