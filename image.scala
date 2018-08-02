import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage


//object runs through example processing
object image extends App{

  val myPhotoPath = "C:\\Users\\liz\\Documents\\Programs\\imageProcessing\\" //directory to process
  val photo = ImageIO.read(new File(myPhotoPath.concat("flowers.jpg"))) //image to process

  printf("Photo size is %d x %d\n", photo.getWidth, photo.getHeight)

  var ker = new kernel(); //init processing class

  //run through some processing and save results
  var out = ker.edgeDetection(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("edgeDetection.jpg")))

  out = ker.boxblur(photo, 9)
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

  out = ker.sobel(photo)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("sobel.jpg")))

  out = ker.laplace(photo, "color")
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("color.jpg")))


  //custom kernel example:
  //(Gaussian 5x5 blur
  var myArray = Array.ofDim[Double](5, 5)

  myArray(0) = Array(1.0, 4.0, 6.0, 4.0, 1.0)
  myArray(1) = Array(4.0, 16.0, 24.0, 16.0, 4.0)
  myArray(2) = Array(6.0, 24.0, 36.0, 24.0, 6.0)
  myArray(3) = myArray(1)
  myArray(4) = myArray(0)

  out = ker.customKernel(photo, myArray)
  ImageIO.write(out, "jpg", new File(myPhotoPath.concat("gaussian.jpg")))

  //ImageIO.write(filterStack(out), "jpg", new File(myPhotoPath.concat("stack.jpg")))

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

