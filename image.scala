import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

//kernels are the basic units of image analysis
class kernel(){
  var k: Array[Array[Double]] = Array.ofDim[Double](3,3)
  var kw = 3
  var kh = 3

  var scale = 1.0

  //box blur
  //1/9 * [1 1 1 \ 1 1 1 \ 1 1 1]
  def boxblur(size:Int, in: BufferedImage): BufferedImage ={
    //init array
    kw = size
    kh = size
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    val coeff = 1.0 / (kw*kw)

    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = coeff //basic box blur

    convolve(in)

  }

  //returns the input image
  def identity(in: BufferedImage): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    k(1)(1) = 1.0

    convolve(in)

  }

  def edgeDetection(in: BufferedImage): BufferedImage ={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    //set up edge detection kernel
    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = -1.0

    k(1)(1) = 8.0

    convolve(in)

  }

  def emboss(in: BufferedImage): BufferedImage ={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    k(0) = Array(-2.0, -1.0, 0.0)
    k(1) = Array(-1.0, 1.0, 1.0)
    k(2) = Array(0.0, 1.0, 2.0)

    convolve(in)
  }

  //discrete approximation of laplacian
  def laplace(in: BufferedImage): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    //could do it programmatically...
    k(0) = Array(0.0, 1.0, 0.0)
    k(1) = Array(1.0, 4.0, 1.0)
    k(2) = Array(0.0, 1.0, 0.0)

    convolve(in)
  }

  def sharpen(in: BufferedImage): BufferedImage={
    kw = 3
    kh = 3
    scale = 1.0
    k = Array.ofDim[Double](kw, kh)

    //could do it programmatically...
    k(0) = Array(0.0, -1.0, 0.0)
    k(1) = Array(-1.0, 5.0, -1.0)
    k(2) = Array(0.0, -1.0, 0.0)

    convolve(in)
  }

  def roberts(in: BufferedImage): BufferedImage= {
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

  def prewitt(in: BufferedImage): BufferedImage={
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

  //get the magnitude of two images
  //used for tensors like the roberts and prewitt filters
  def getMagnitude(A: BufferedImage, B: BufferedImage): BufferedImage={
    val w = A.getWidth()
    val h = A.getHeight()

    val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)

    //get magnitude of the two convolutions
    for (x <- 0 until w) {
      for (y <- 0 until h) {
        val Argb = A.getRGB(x, y)
        val Brgb = B.getRGB(x, y)
        val Blue = math.sqrt(sqr(Argb & 0xff) + sqr(Brgb & 0xff))
        val Green = math.sqrt(sqr((Argb & 0xff00) >> 8) + sqr((Brgb & 0xff00) >> 8))
        val Red = math.sqrt(sqr((Argb & 0xff0000) >> 16) + sqr((Brgb & 0xff0000) >> 16))
        out.setRGB(x, y, (mask(Red) << 16) + (mask(Green) << 8) + mask(Blue))
      }
    }

    out
  }

  //quick square function:
  def sqr(in:Int):Double={(in*in)}

  def convolve(in: BufferedImage): BufferedImage={
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
            val c = x + a - (kw - 1) / 2
            val d = y + b - (kh - 1) / 2
            //make sure everything is in bounds:
            if (!(c < 0 || c >= w || d < 0 || d >= h)) {
              val data = in.getRGB(c, d)
              val coefficient = k(a)(b)
              val B = data & 0xff //use bitwise operations based on buffereimage (ignore alpha)
              val G = (data & 0xff00) >> 8
              val R = (data & 0xff0000) >> 16
              Rsum += R * coefficient
              Gsum += G * coefficient
              Bsum += B * coefficient
            }
          }
        }
        Rsum = mask(Rsum)  << 16 //recombine RGB
        Gsum = mask(Gsum) << 8
        val sum = Rsum + Gsum + mask(Bsum) //when more complicated kernels are added, a scale factor needs to be added here
        out.setRGB(x, y, sum.toInt)
      }
    }

    out
  }

  def mask(valuein: Double):Int={
    mask(valuein, 255) //default value is 255
  }

  //make sure RGB values are in range
  def mask(valuein: Double, max: Int):Int={
    val myValue = (valuein / scale).toInt

    if (myValue > max)
      max
    else if (myValue < 0)
      0
    else
      myValue
  }

}

//object for
object image extends App{

  val myPhotoPath = "C:\\Users\\liz\\Documents\\Programs\\imageProcessing\\" //directory to process
  val photo = ImageIO.read(new File(myPhotoPath.concat("funhat.jpg"))) //image to process

  printf("Photo size is %d x %d\n", photo.getWidth, photo.getHeight)

  var ker = new kernel(); //init processing class

  //run through some processing and save results
  var out = ker.edgeDetection(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("edgeDetection.jpg")))

  out = ker.boxblur(9, photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("boxblur.jpg")))

  out = ker.sharpen(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("sharpen.jpg")))

  out = ker.laplace(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("laplace.jpg")))

  out = ker.emboss(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("emboss.jpg")))

  out = ker.roberts(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("roberts.jpg")))

  out = ker.prewitt(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("prewitt.jpg")))

  //quick util for checking directories
  def listdirectory(dir: String): List[File] ={
    val f = new File(dir)
    //get array of files in path:
    if (f.exists && f.isDirectory) {
      val paths: List[File] = f.listFiles().filter(_.isFile).toList
      //paths.foreach(println) //could print it out here
      paths //return list
    }
    else{
      println("Error: directory is empty")
      List[File]() //empty list
    }
  }
}

