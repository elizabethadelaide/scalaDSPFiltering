import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

//kernels are the basic units of image analysis
class kernel(){
  var k: Array[Array[Double]] = Array.ofDim[Double](3,3)
  var kw = 3
  var kh = 3

  //box blur
  //1/9 * [1 1 1 \ 1 1 1 \ 1 1 1]
  def boxblur(size:Int, in: BufferedImage): BufferedImage ={
    //init array
    kw = size
    kh = size
    k = Array.ofDim[Double](kw, kh)

    val coeff = 1.0 / (kw*kw)

    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = coeff //basic box blur

    val out = this.convolve(in)

    out
  }

  //returns the input image
  def identity(in: BufferedImage): BufferedImage={
    kw = 3
    kh = 3
    k = Array.ofDim[Double](kw, kh)

    k(1)(1) = 1.0

    val out = this.convolve(in)

    out
  }

  def edgeDetection(in: BufferedImage): BufferedImage ={
    kw = 3
    kh = 3
    k = Array.ofDim[Double](kw, kh)

    //set up edge detection kernel
    for (i <- 0 until kw)
      for (l <- 0 until kh)
        k(i)(l) = -1.0

    k(1)(1) = 8.0

    val out = this.convolve(in)

    out
  }

  //discrete approximation of laplacian
  def laplace(in: BufferedImage): BufferedImage={
    kw = 3
    kh = 3
    k = Array.ofDim[Double](kw, kh)

    //could do it programmatically...
    k(0) = Array(0.0, 1.0, 0.0)
    k(1) = Array(1.0, 4.0, 1.0)
    k(2) = Array(0.0, 1.0, 0.0)

    val out = this.convolve(in)

    out
  }

  def sharpen(in: BufferedImage): BufferedImage={
    kw = 3
    kh = 3
    k = Array.ofDim[Double](kw, kh)

    //could do it programmatically...
    k(0) = Array(0.0, -1.0, 0.0)
    k(1) = Array(-1.0, 5.0, -1.0)
    k(2) = Array(0.0, -1.0, 0.0)

    val out = this.convolve(in)

    out
  }

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
        Rsum = clipmask(Rsum.toInt)  << 16 //recombine RGB
        Gsum = clipmask(Gsum.toInt) << 8
        val sum = Rsum + Gsum + clipmask(Bsum.toInt) //when more complicated kernels are added, a scale factor needs to be added here
        out.setRGB(x, y, sum.toInt)
      }
    }

    out
  }


  def clipmask(valuein: Int):Int={
    clipmask(valuein, 255) //default value is 255
  }

  //make sure RGB values are in range
  def clipmask(valuein: Int, max: Int):Int={
    if (valuein > max)
      max
    else if (valuein < 0)
      0
    else
      valuein
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

