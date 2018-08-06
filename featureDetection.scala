import java.awt.image.{BufferedImage, Raster}

class featureDetection(inK:Double=0.14, inRThreshold:Double=120.0, inSigma:Double = 0.3){
  val k = new kernel()

  var K = inK
  var RThreshold = inRThreshold
  var sigma = inSigma //adjustable gauss value
  var NGauss = 0.0 //adjustable gauss size

  var windowFunction = "Constant"

  //process a buffered image to get corners
  //K is empircally found, and is generally between 0.4 and 0.6 depending on application
  //finds:
  //R = det(M) - K*trace(M)^2
  //derivation can be found here: https://docs.opencv.org/2.4/doc/tutorials/features2d/trackingmotion/harris_detector/harris_detector.html
  def harrisStephens(in:BufferedImage):Array[corner]={

    var gray = k.color2gray(in) //apply gray scale
    gray = crop(gray) //image needs to be square for matrix operations, cropping is simplest and probably worst solution

    val w = gray.getWidth
    val h = gray.getHeight

    val M = getM(gray)

    val matrix = Array.ofDim[Double](2, 2)

    //check for consistent sizing:
    //printf("Out size is %d x %d, Matrix size is %d x %d\n", w, h, M(0)(0).length, M(0)(0)(0).length)

    //empty arrayBuffer
    val out = scala.collection.mutable.ArrayBuffer.empty[corner]


    //find R = det(M) - K*trace(M)^2 for each point in the image
    for (x <- 0 until w) {
      for (y <- 0 until h) {
        val determinant = M.getDet(x, y)
        val trace = M.getTrace(x, y)

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

  //Get R array for some scaling purposes
  //Not extremely efficient
  def getRArray(thisArray:Array[corner]):Array[Double]={
    val R = Array.ofDim[Double](thisArray.length)
    for (i <- 0 until thisArray.length){
      R(i) = thisArray(i).getR()
    }
    R
  }



  //creates an RGB image with red highlighted corners
  def displayCorners(corners:Array[corner], in:BufferedImage): BufferedImage={
    val gray = k.color2gray(in) //make sure it's gray scale

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

    //get R array to get min and max or R
    val rArray = getRArray(corners)
    val min = rArray.min
    val max = rArray.max

    for (i <- 0 until corners.length){
      val x = corners(i).getX()
      val y = corners(i).getY()

      //set color to represent intensity of cornerness
      val Rvalue = 10*(255.0*(corners(i).getR() - min) / (max - min)).toInt

      out.setRGB(x, y, (k.mask(Rvalue) << 16) + k.mask(255-Rvalue)) //set to red

    }
    out
  }

  //M is a tensor which describes the directional changes of the image
  //Will clean up this object type...
  def getM(in:BufferedImage):Tensor={
    //get partial spatial derivatives
    val Ix = getIx(in)
    val Iy = getIy(in)

    //get window function
    val w = in.getWidth
    val h = in.getHeight

    NGauss = w.toDouble //set Guass window size

    //could rewrite this as some type of object? not the prettiest
    val M = new Tensor(2, 2, w, h)

    //M =
    //[ Ix^2 Ix*Iy ]
    //[ Iy*Ix Iy^2 ]
    //remember that with matrix multiplication, order matters
    val Ix2 = matrixMultiply(Ix, Ix)
    val IxIy = matrixMultiply(Ix, Iy)
    val IyIx = matrixMultiply(Iy, Ix)
    val Iy2 = matrixMultiply(Iy, Iy)

    if (windowFunction == "Constant"){
      M.push(Ix2)
      M.push(IxIy)
      M.push(IyIx)
      M.push(Iy2)
    }

    //M = sum_x,y (w(x,y) *
    //[ Ix^2 Ix*Iy ]
    //[ Iy*Ix Iy^2 ]
    //TODO: Check math for Guassian windows
    //Is is supposed to simply center issues in center of image?
    //If the window did do an operation centered on each pixel, that wouldn't be separable?
    //That would be a convolution wouldn't it? hmm...
    /*if (windowFunction == "Gaussian") {
      var W = 0.0
      for (x <- 0 until w) {
        for (y <- 0 until h) {
          //2D gaussian is a separable function
          W = getGaussian(x) * getGaussian(y)
          for ()
          M(0)(0)(x)(y) += W*Ix2
          M(0)(1)(x)(y) += W*IxIy
          M(1)(0)(x)(y) += W*IyIx
          M(1)(1)(x)(y) += W*Iy2
        }
      }
    }*/

    M
  }

  //Tensor class
  //Starting with a simple 2x2 Matrix
  //TODO: This can be moved to its own class and generalized for more tensors
  class Tensor(XSize:Int, YSize:Int, ZSize:Int, WSize:Int){

    val M = Array.ofDim[Double](XSize, YSize, ZSize, WSize)
    var counter = 0

    //push an array
    def push(newValue: Array[Array[Double]]):Int={
      //check dimensions
      if (newValue.length == ZSize && newValue(0).length == WSize){
        if (counter < XSize*YSize){
          val x = counter % XSize
          val y = (counter - x) / XSize
          M(x)(y) = newValue
          counter += 1
          counter
        }
        else {
          0 //tensor is full
        }
      }
      else{
        -1 //invalid size array
      }
    }

    //push a value to a coordinate
    def push(a:Int, b:Int, value:Double):Int={
      if (a < ZSize && b < WSize){
        if (counter < XSize*YSize){
          val x = counter % XSize
          val y = (counter - x) / XSize
          M(x)(y)(a)(b) = value
          counter += 1
          counter
        }
        else{
          0 //tensor full
        }
      }
      else{
        -1
      }
    }

    //determinant of 2x2 matrix
    //TODO: expand to general case
    def getDet(x:Int, y:Int):Double={
      M(0)(0)(x)(y)*M(1)(1)(x)(y) - M(0)(1)(x)(y)*M(1)(0)(x)(y)
    }
    //trace of 2x2 matrix
    //TODO: expand to general case
    def getTrace(x:Int, y:Int):Double={
      M(0)(0)(x)(y) + M(1)(1)(x)(y)
    }

    /***********Some utils*********************/
    def getXSize():Int={XSize}
    def getYSize():Int={YSize}
    def getZSize():Int={ZSize}
    def getWSize():Int={WSize}

    def getMatrix(z:Int, w:Int): Array[Array[Double]] ={
      val out = Array.ofDim[Double](WSize, ZSize)
      if (z < ZSize && w < WSize){
        for (a <- 0 until XSize)
          for (b <- 0 until YSize)
            out(w)(z) = M(a)(b)(z)(w)
      }
      out
    }

    //helpful for quickly checking it the tensor is empty
    def getMax():Double={M.flatten.flatten.flatten.max}
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
            out(i)(j) += A(i)(k) * B(k)(j)
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
  def convolve(in: BufferedImage, kernel: Array[Array[Double]]): Array[Array[Double]]={
    //used for image detection

    val w = in.getWidth
    val h = in.getHeight

    val kw = kernel.length
    val kh = kernel(0).length

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
              val data = rasterIn.getSample(c, d, 0) / 255.0 //normalize to 1
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

  //get one dimensional gaussian window
  //(2D Gaussian is a separable function)
  def getGaussian(nInt: Int): Double={
    val n = nInt.toDouble

    //w(n) =
    //split up gaussian for readability
    val num = n - (NGauss-1.0)/2.0
    val den = (sigma*(NGauss-1.0))/2.0

    scala.math.exp(-0.5*sqr(num/den))
  }

  /**********Class setters*******************/
  //change window function
  def setWindowFunction(newWindowFunction:String):String={
    if (newWindowFunction == "Constant"){
      windowFunction = "Constant"
    }
    else if (newWindowFunction == "Gaussian"){
      windowFunction = "Gaussian"
    }
    else{
      printf("Invalid window function: %s", newWindowFunction)
    }
    windowFunction
  }

  //for setting sigma
  def setGaussSigma(newSigma:Double): Double ={
    sigma = newSigma
    sigma
  }

  //set K value for harris stephens
  def setK(newK:Double):Double={
    K = newK
    K
  }

  //set R threshold for harris stephens
  def setRThreshold(newR:Double):Double={
    RThreshold = newR
    RThreshold
  }
}
