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

![A blurry fun crab hat](/images/boxblur.jpg)

Edge detection

![An outlined fun crab hat](/images/edgeDetection.jpg)

Sharpened

![A very sharp fun crab hat](/images/sharpen.jpg)

Discrete laplace approximation

![A kinda faded fun crab hat](/images/laplace.jpg)

Emboss

![An embossed crab hat](/images/emboss.jpg)

Roberts

![A differed edge detected crab hat](/images/roberts.jpg)

Prewitt

![Another edge detected crab hat](/images/prewitt.jpg)
