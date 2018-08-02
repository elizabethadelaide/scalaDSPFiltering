import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.io.IOException


//kernels are the basic units of image analysis
class kernel(){
  var k: Array[Array[Double]] = Array.ofDim[Double](3,3)
  var kw = 3
  var kh = 3

  var scale = 1.0

  //default
  //1/9 * [1 1 1 \ 1 1 1 \ 1 1 1]
  def boxblur(in: BufferedImage, size:Int = 3, colorMode:String="grayscale"): BufferedImage ={
    //init array
    kw = size
    kh = size
    scale = 1.0 //handles size with coeff
    k = Array.ofDim[Double](kw, kh)

    val coeff = 1.0 / (kw*kw)

    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = coeff //basic box blur

    convolve(in, colorMode)

  }

  //returns the input image
  def identity(in: BufferedImage, colorMode:String="grayscale"): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    k(1)(1) = 1.0

    convolve(in, colorMode)

  }

  def edgeDetection(in: BufferedImage, colorMode:String="grayscale"): BufferedImage ={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    //set up edge detection kernel
    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = -1.0

    k(1)(1) = 8.0

    convolve(in, colorMode)

  }

  def emboss(in: BufferedImage, colorMode:String="grayscale"): BufferedImage ={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    k(0) = Array(-2.0, -1.0, 0.0)
    k(1) = Array(-1.0, 1.0, 1.0)
    k(2) = Array(0.0, 1.0, 2.0)

    convolve(in, colorMode)
  }

  //discrete approximation of laplacian
  def laplace(in: BufferedImage, colorMode:String="grayscale"): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    //could do it programmatically...
    k(0) = Array(0.0, 1.0, 0.0)
    k(1) = Array(1.0, 4.0, 1.0)
    k(2) = Array(0.0, 1.0, 0.0)

    convolve(in, colorMode)
  }

  def sharpen(in: BufferedImage, colorMode:String="grayscale"): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    //could do it programmatically...
    k(0) = Array(0.0, -1.0, 0.0)
    k(1) = Array(-1.0, 5.0, -1.0)
    k(2) = Array(0.0, -1.0, 0.0)

    convolve(in, colorMode)
  }

  def roberts(in: BufferedImage, colorMode:String="grayscale"): BufferedImage= {
    kw = 2
    kh = 2
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    k(0) = Array(1.0, 0.0)
    k(1) = Array(0.0, -1.0)

    val A = convolve(in)

    k(0) = Array(0.0, 1.0)
    k(1) = Array(-1.0, 0.0)

    val B = convolve(in)

    getMagnitude(A, B)

  }

  def prewitt(in: BufferedImage, colorMode:String="grayscale"): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = 1 - l //1, 0, -1 in columns

    val A = convolve(in)

    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = 1 - i //1, 0, -1 in rows

    val B = convolve(in)

    getMagnitude(A, B)

  }

  def sobel(in: BufferedImage, colorMode:String="grayscale"): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    //Array 1:
    k(0) = Array(1.0, 0.0, -1.0)
    k(1) = Array(2.0, 0.0, -2.0)
    k(2) = Array(1.0, 0.0, -1.0)

    val A = convolve(in, colorMode)

    //Array 2:
    k(0) = Array(1.0, 2.0, 1.0)
    k(1) = Array(0.0, 0.0, 0.0)
    k(2) = Array(-1.0, -2.0, -1.0)

    val B = convolve(in, colorMode)

    getMagnitude(A, B)
  }

  def getMagnitude(A: BufferedImage, B: BufferedImage, colorMode: String="grayscale"): BufferedImage={
    if (colorMode == "grayscale"){
      var grayA:BufferedImage = A
      var grayB:BufferedImage = B
      //if it's a colored image (assume it's RGB if it has 3 components
      if (A.getColorModel().getNumColorComponents() == 3){
        grayA = color2gray(A)
      }
      if (B.getColorModel().getNumColorComponents() == 3){
        grayB = color2gray(B)
      }
      getMagnitudeGray(grayA, grayB)
    }
    else if(colorMode == "color"){
      getMagnitudeColor(A, B)
    }
    else{ //invalid color mode
      println("Error: Colormode can only be 'grayscale' or 'color'")
      A //don't get the magnitude, throw out A
    }
  }

  def getMagnitudeGray(A:BufferedImage, B: BufferedImage): BufferedImage={
    val w = A.getWidth()
    val h = A.getHeight()

    val out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)
    val rasterOut = out.getRaster() //writable raster out
    val rasterA = A.getData() //readable rasters In
    val rasterB = B.getData()

    //get magnitude of the two convolutions
    for (x <- 0 until w) {
      for (y <- 0 until h) {
        val grayA = rasterA.getSample(x, y, 0) & 0xff //get gray byte
        val grayB = rasterB.getSample(x, y, 0) & 0xff //get gray byte

        //this could be made faster by approximation: simply getting the sums
        //get magnitue
        rasterOut.setSample(x, y, 0, Math.sqrt(sqr(grayA) + sqr(grayB)))
      }
    }
    out
  }

  //get the magnitude of two images
  //used for tensors like the roberts and prewitt filters
  def getMagnitudeColor(A: BufferedImage, B: BufferedImage): BufferedImage={
    val w = A.getWidth()
    val h = A.getHeight()

    val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

    //get magnitude of the two convolutions
    for (x <- 0 until w) {
      for (y <- 0 until h) {
        val Argb = A.getRGB(x, y)
        val Brgb = B.getRGB(x, y)

        //this could be made faster by approximation: simply getting the sums
        val Blue = math.sqrt(sqr(Argb & 0xff) + sqr(Brgb & 0xff))
        val Green = math.sqrt(sqr((Argb & 0xff00) >> 8) + sqr((Brgb & 0xff00) >> 8))
        val Red = math.sqrt(sqr((Argb & 0xff0000) >> 16) + sqr((Brgb & 0xff0000) >> 16))
        out.setRGB(x, y, (mask(Red) << 16) + (mask(Green) << 8) + mask(Blue))
      }
    }

    out
  }

  def customKernel(in:BufferedImage, kern: Array[Array[Double]], colorMode:String="grayscale"): BufferedImage={
    kw = kern.length
    kh = kern(0).length
    if (kw != kh)
      println("Warning: non-square convolution matrix") //won't break anything, but is generally not the shape of kernels

    k = kern
    scale = 0
    for (x <- 0 until kw)
      for (y <- 0 until kh)
        scale += kern(x)(y) //set scale (sum of all kernel values)

    convolve(in, colorMode)

  }

  //quick square function:
  def sqr(in:Int):Double={in*in}



  def convolve(in: BufferedImage, colorMode: String = "grayscale"): BufferedImage={
    if (colorMode == "grayscale"){
      var gray:BufferedImage = in
      //if it's a colored image (assume it's RGB if it has 3 components
      if (in.getColorModel().getNumColorComponents() == 3){
        gray = color2gray(in)
      }
      convolveGray(gray)
    }
    else if(colorMode == "color"){
      convolveColor(in)
    }
    else{ //invalid color mode
      println("Error: Colormode can only be 'grayscale' or 'color'")
      in //don't filter anything
    }
  }

  def color2gray(in: BufferedImage): BufferedImage={
    try {
      val w = in.getWidth
      val h = in.getHeight

      val out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)

      //color convert op seems to be fastest option
      val op = new ColorConvertOp(in.getColorModel().getColorSpace(), out.getColorModel().getColorSpace(), null)
      op.filter(in, out) //filter input image to output colorspace
      out
    }
    catch{
      case e: //handle errors
        IOException => println("Error converting RGB to gray")
        null
    }
  }

  def convolveGray(in: BufferedImage): BufferedImage={
    //used for imaage detection

    //keeps output image format as RGB
    //this is less efficient, but allows filters to be stacked easier
    val w = in.getWidth
    val h = in.getHeight

    //output image
    val out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)
    val rasterOut = out.getRaster()
    val raster = in.getData() //use raster to get data at each point of image
    val pixel:Object = null // pixel initialized on first invocation of getDataElements
    //convolution algorithm
    for (x <- 0 until w) {
      for (y <- 0 until h) {
        var accum = 0.0 //grayscale sum
        for (a <- 0 until kw) {
          for (b <- 0 until kh) {
            val c = x + a - (kw - 1) / 2 //corresponding image coordinates
            val d = y + b - (kh - 1) / 2
            //make sure everything is in bounds:
            if (!(c < 0 || c >= w || d < 0 || d >= h)) {
              val data = raster.getSample(c, d, 0) & 0xff
              val coefficient = k(a)(b)
              accum += coefficient*data.toDouble
            }
            /*
            else{
              //could do out of bound stuff here or wrapping...
            }
             */
          }
        }
        //set output image pixel:
        rasterOut.setSample(x, y, 0, mask(accum))
      }
    }

    //return convolved image
    out

  }

  //do convolution of image and kernel
  //for larger kernels, can do an FIT
  def convolveColor(in: BufferedImage): BufferedImage={
    val w = in.getWidth //get img properties
    val h = in.getHeight

    //print array:
    //println(k.deep.mkString("\n"))

    //output image
    val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)


    //convolution algorithm
    for (x <- 0 until w) {
      for (y <- 0 until h) {
        var Rsum = 0.0 //separate RGB
        var Gsum = 0.0
        var Bsum = 0.0
        for (a <- 0 until kw) {
          for (b <- 0 until kh) {
            val c = x + a - (kw - 1) / 2 //corresponding image coordinates
            val d = y + b - (kh - 1) / 2
            //make sure everything is in bounds:
            if (!(c < 0 || c >= w || d < 0 || d >= h)) {
              val data = in.getRGB(c, d) //get image data
              val coefficient = k(a)(b) //get kernel data
              val B = data & 0xff //use bitwise operations based on buffered image (ignore alpha)
              val G = (data & 0xff00) >> 8
              val R = (data & 0xff0000) >> 16
              Rsum += R * coefficient //combine kernel and image data
              Gsum += G * coefficient
              Bsum += B * coefficient
            }
            /*
            else{
              //could do out of bound stuff here or wrapping...
            }
             */
          }
        }
        //recombine RGB, mask handles scaling and type:
        val sum =  (mask(Rsum)  << 16) + (mask(Gsum) << 8) + mask(Bsum)
        //set output image pixel:
        out.setRGB(x, y, sum)
      }
    }

    //return convolved image
    out
  }

  //make sure RGB values are in range
  def mask(valuein: Double, max: Int = 255):Int={
    val myValue = (valuein / scale).toInt //handle scaling and typing

    if (myValue > max)
      max
    else if (myValue < 0)
      0
    else
      myValue
  }

}