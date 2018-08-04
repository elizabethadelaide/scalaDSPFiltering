import java.awt.image.{BufferedImage, Raster}

class featureDetection(){
  val k = new kernel()

  //process a buffered image to get corners
  //K is empircally found, and is generally between 0.4 and 0.6 depending on application
  //finds:
  //R = det(M) - K*trace(M)^2
  //derivation can be found here: https://docs.opencv.org/2.4/doc/tutorials/features2d/trackingmotion/harris_detector/harris_detector.html
  def harrisStephens(in:BufferedImage, K:Double=0.14, RThreshold:Double=120.0):Array[corner]={

    var gray = k.color2gray(in) //apply gray scale
    gray = crop(gray) //image needs to be square for matrix operations, cropping is simplest and probably worst solution

    val M = getM(gray)

    val w = gray.getWidth
    val h = gray.getHeight

    val matrix = Array.ofDim[Double](2, 2)

    //check for consistent sizing:
    //printf("Out size is %d x %d, Matrix size is %d x %d\n", w, h, M(0)(0).length, M(0)(0)(0).length)

    //empty arrayBuffer
    val out = scala.collection.mutable.ArrayBuffer.empty[corner]


    //find R = det(M) - K*trace(M)^2 for each point in the image
    for (x <- 0 until w) {
      for (y <- 0 until h) {
        matrix(0) = Array(M(0)(0)(x)(y), M(0)(1)(x)(y))
        matrix(1) = Array(M(1)(0)(x)(y), M(1)(1)(x)(y))
        val determinant = getDet(matrix)
        val trace = getTrace(matrix)

        val R = determinant - K * sqr(trace)

        if (R > RThreshold) {
          //ignore edge cases
          if (!(x == 0 || x == w - 1 || y  == 0 || y == h - 1)){
            //printf("Corner detected at: %d, %d with R = %f\n", x, y, R)
            out += new corner(x, y, R)
          }
        }
      }
    }
    printf("%d corners detected\n", out.length)
    out.toArray
  }

  //class contains information about detected corners
  class corner(x:Int, y:Int, R:Double){
    def getX():Int={x}
    def getY():Int={y}
    def getR():Double={R}
  }

  //creates an RGB image with red highlighted corners
  def displayCorners(corners:Array[corner], gray:BufferedImage): BufferedImage={
    val w = gray.getWidth
    val h = gray.getHeight

    val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

    val grayRaster = gray.getData

    //copy data, could be faster doing the opposite of the gray filter in the color2gray kernel
    for (x <- 0 until w){
      for (y <- 0 until h){
        val grayData = grayRaster.getSample(x, y, 0)
        val G = grayData
        val B = grayData
        val R = grayData
        out.setRGB(x, y, (R << 16) + (G << 8) + B)
      }
    }
    for (i <- 0 until corners.length){
      val x = corners(i).getX()
      val y = corners(i).getY()
      //for (j <- x-5 until x+5){
        //for (k <- y-20 until y+20){
          //if (j > 0 && j < w && k > 0 && k < h){
            out.setRGB(x, y, 255 << 16) //set to red
          //}
        //}
      //}
    }
    out
  }

  //determinant of 2x2 matrx
  def getDet(m:Array[Array[Double]]):Double={
    m(0)(0)*m(1)(1) - m(0)(1)*m(1)(0)
  }

  //trace of 2x2 matrix
  def getTrace(m:Array[Array[Double]]):Double={
    m(0)(0) + m(1)(1)
  }

  //M is a tensor which describes the directional changes of the image
  //Will clean up this object type...
  def getM(in:BufferedImage):Array[Array[Array[Array[Double]]]]={
    //get partial spatial derivatives
    val Ix = getIx(in)
    val Iy = getIy(in)

    //get window function
    val w = in.getWidth
    val h = in.getHeight
    val win = getW(w, h)

    //could rewrite this as some type of object? not the prettiest
    val M = Array.ofDim[Double](2, 2, w, h)

    //M =
    //[ Ix^2 Ix*Iy ]
    //[ Iy*Ix Iy^2 ]
    //remember that with matrix multiplication, order matters
    M(0)(0) = matrixMultiply(Ix, Ix)
    M(0)(1) = matrixMultiply(Ix, Iy)
    M(1)(0) = matrixMultiply(Iy, Ix)
    M(1)(1) = matrixMultiply(Iy, Iy)

    //TODO: add window function and sum here:

    M
  }

  //matrix multiplication utility
  def matrixMultiply(A:Array[Array[Double]], B:Array[Array[Double]]):Array[Array[Double]]={
    //get bounds

    val Aw = A.length
    val Ah = A(0).length
    val Bw = B.length
    val Bh = B(0).length

    if (Ah != Bw){
      println("Error multiplying matrices, invalid match")
      Array.ofDim[Double](0,0)
    }
    else {
      val out = Array.ofDim[Double](Aw, Bh)

      //for each entry in the matrix
      for (i <- 0 until Aw) {
        for (j <- 0 until Bh) {
          //do a sum:
          for (k <- 0 until Ah) {
            val Avalue = A(i)(k)
            val Bvalue = B(k)(j)
            out(i)(j) += Avalue * Bvalue
          }
        }
      }
      out
    }

  }

  //do a separable sobel operator to get Ix
  def getIx(in:BufferedImage):Array[Array[Double]]={

    val kx = Array.ofDim[Double](3, 3)

    //Array:
    kx(0) = Array(1.0, 0.0, -1.0)
    kx(1) = Array(2.0, 0.0, -2.0)
    kx(2) = Array(1.0, 0.0, -1.0)

    convolve(in, kx)
  }

  //do a separable operator to get Iy
  def getIy(in:BufferedImage):Array[Array[Double]]={
    val ky = Array.ofDim[Double](3, 3)

    //Array:
    ky(0) = Array(1.0, 2.0, 1.0)
    ky(1) = Array(0.0, 0.0, 0.0)
    ky(2) = Array(-1.0, -2.0, -1.0)

    convolve(in, ky)
  }

  def getW(w:Int, h:Int):Array[Array[Double]]={
    val window = Array.ofDim[Double](w, h)
    //constant window:
    //will add gaussian and box later
    for (x <- 0 until w){
      for (y <- 0 until h){
        window(x)(y) = 1.0
      }
    }
    window
  }

  //quick square function:
  def sqr(in:Double):Double={in*in}

  //image must be square for matrix multiplication:
  //option 1: is to crop
  //option 2 is to expand with some filler
  //other option is upsample gradients??
  def crop(in:BufferedImage):BufferedImage= {
    val w = in.getWidth
    val h = in.getHeight

    var a = h
    if (w < h) {
      a = w
    }
    val out = new BufferedImage(a, a, BufferedImage.TYPE_BYTE_GRAY)
    val outRaster = out.getRaster //writable raster
    val inRaster = in.getData //readable raster
    for (x <- 0 until a){
      for (y <- 0 until a){
        outRaster.setSample(x, y, 0, inRaster.getSample(x,y,0))
      }
    }
    out
  }

  //for more advanced filters, convolution should be done with doubles
  //this prevents overflow errors and rounding errors
  //I don't necessarily like having so many copies of the convolution alogrithm however...
  def convolve(in: BufferedImage, kernel: Array[Array[Double]], kw:Int=3, kh:Int=3): Array[Array[Double]]={
    //used for image detection

    val w = in.getWidth
    val h = in.getHeight

    //output array
    val out = Array.ofDim[Double](w, h)

    val rasterIn = in.getData() //use readable raster to get data at each point of image
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
              val data = (rasterIn.getSample(c, d, 0) & 0xff) / 255.0 //normalize to 1
              val coefficient = kernel(a)(b)
              accum += coefficient*data
            }
            /*
            else{
              //could do out of bound stuff here or wrapping...
            }
             */
          }
        }
        //set output:
        out(x)(y) = accum
      }
    }
    //return convolved image
    out

  }

  //scale a 2D array to RGB space
  //for visualizing 2D functions
  //3 byte space = 0 - 765
  def colorMap(in: Array[Array[Double]]):BufferedImage={

    val min = in.flatten.min
    val max = in.flatten.max

    val w = in.length
    val h = in(0).length

    val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

    for (x <- 0 until w){
      for (y <- 0 until h){
        out.setRGB(x, y, (765.0*(in(x)(y)-min)/(max-min)).toInt)
      }
    }
    out
  }


}
