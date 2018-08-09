import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage


//object runs through example processing
object image extends App{

  val myPhotoPath = "C:\\Users\\liz\\Documents\\Programs\\imageProcessing\\" //directory to process
  val photo = ImageIO.read(new File(myPhotoPath.concat("window.jpg"))) //image to process

  printf("Photo size is %d x %d\n", photo.getWidth, photo.getHeight)

  val fD = new featureDetection()

  runKernel()

  runFeatureDetection()

  def runFeatureDetection():Int={
    val fD = new featureDetection() //init feature detection class

    val featurePath = myPhotoPath.concat("featureDetection\\")

    //try gaussian window function
    //fD.setWindowFunction("Gaussian")

    val corners = fD.harrisStephens(photo)
    //ImageIO.write(out, "jpg", new File(featurePath.concat("harrisStephens.jpg")))

    val out = fD.displayCorners(corners, photo)
    ImageIO.write(out, "png", new File(featurePath.concat("harrisStephens.png")))

    1
  }

  def runKernel():Int= {
    val ker = new kernel() //init processing class

    val kernelPath = myPhotoPath.concat("kernelConvolution\\")

    //run through some processing and save results
    var out = ker.edgeDetection(photo)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("edgeDetection.jpg")))

    out = ker.boxblur(photo, 9)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("boxblur.jpg")))

    out = ker.sharpen(photo)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("sharpen.jpg")))

    out = ker.laplace(photo)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("laplace.jpg")))

    out = ker.emboss(photo)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("emboss.jpg")))

    out = ker.roberts(photo)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("roberts.jpg")))

    out = ker.prewitt(photo)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("prewitt.jpg")))

    out = ker.sobel(photo)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("sobel.jpg")))

    out = ker.laplace(photo, "color")
    ImageIO.write(out, "jpg", new File(kernelPath.concat("color.jpg")))


    //custom kernel example:
    //(Gaussian 5x5 blur
    val myArray = Array.ofDim[Double](5, 5)

    myArray(0) = Array(1.0, 4.0, 6.0, 4.0, 1.0)
    myArray(1) = Array(4.0, 16.0, 24.0, 16.0, 4.0)
    myArray(2) = Array(6.0, 24.0, 36.0, 24.0, 6.0)
    myArray(3) = myArray(1)
    myArray(4) = myArray(0)

    out = ker.customKernel(photo, myArray)
    ImageIO.write(out, "jpg", new File(kernelPath.concat("gaussian.jpg")))

    1
  }

  //example of stacked filters for more interesting effects
  def filterStack(in: BufferedImage): BufferedImage={
    val ker = new kernel()
    var out = ker.prewitt(in)
    out = ker.sharpen(out)
    out = ker.emboss(out)
    ker.boxblur(out)
  }

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

