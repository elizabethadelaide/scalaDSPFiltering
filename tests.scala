import java.awt.image.BufferedImage
import java.io.{BufferedWriter, File, FileWriter}

import javax.imageio.ImageIO

//test app for debugging corner detection
//as well as to set up for debugging and testing performance of future algorithms
object tests extends App{

  val fD = new featureDetection()

  val myPhotoPath = "C:\\Users\\liz\\Documents\\Programs\\imageProcessing\\" //directory to process
  val photo = ImageIO.read(new File(myPhotoPath.concat("flowers.jpg"))) //image to process
  val myTestPath = myPhotoPath.concat("testing\\")

  checkMatrixMultiply()
  checkDeterminant()
  checkTrace()
  checkCrop()
  checkPartials()
  checkM()

  //outputs a CSV file of M matrix
  def checkM():Int={
    //preprocess with gaussian
    var gray = fD.k.color2gray(photo) //smooth and gray scale
    gray = fD.crop(gray) //image needs to be square

    val M = fD.getM(gray)

    val w = M(0)(0).length
    val h = M(0)(0)(0).length

    val bw = new BufferedWriter(new FileWriter(new File(myTestPath.concat("M.csv"))))

    val IX2 = Array.ofDim[Double](w, h)
    val IXIY = Array.ofDim[Double](w, h)
    val IYIX = Array.ofDim[Double](w, h)
    val IY2 = Array.ofDim[Double](w, h)

    for (x <- 0 until w){
      for (y <- 0 until h){
        val A = M(0)(0)(x)(y)
        val B = M(0)(1)(x)(y)
        val C = M(1)(0)(x)(y)
        val D = M(1)(1)(x)(y)
        IX2(x)(y) = A
        IXIY(x)(y) = B
        IYIX(x)(y) = C
        IY2(x)(y) = D
        bw.write(f"$x , $y , $A, $B, $C, $D\n")
      }
    }
    bw.close()

    var out = fD.colorMap(IX2)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("Ix2.jpg")))
    out = fD.colorMap(IXIY)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("IxIy.jpg")))
    out = fD.colorMap(IYIX)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("IyIx.jpg")))
    out = fD.colorMap(IY2)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("Iy2.jpg")))


    1
  }

  def checkPartials():Int={
    val gray = fD.k.color2gray(photo)
    val Ix = fD.getIx(gray)
    val Iy = fD.getIy(gray)
    val w = gray.getWidth
    val h = gray.getHeight

    val xOut = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)
    val yOut = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY)

    val xOutRaster = xOut.getRaster
    val yOutRaster = yOut.getRaster

    for (x <- 0 until w) {
      for (y <- 0 until h) {
        xOutRaster.setSample(x, y, 0, fD.k.mask(Ix(x)(y)))
        yOutRaster.setSample(x, y, 0, fD.k.mask(Iy(x)(y)))
      }
    }
    ImageIO.write(xOut, "jpg", new File(myTestPath.concat("Ix.jpg")))
    ImageIO.write(yOut, "jpg", new File(myTestPath.concat("Iy.jpg")))

    1
  }

  def checkCrop():Int={
    val out = fD.crop(photo)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("cropped.jpg")))
    1
  }

  def checkTrace():Int={
    val A = Array.ofDim[Double](2, 2)
    val Ans = 9

    A(0) = Array(3, 8)
    A(1) = Array(4, 6)

    val myAns = fD.getTrace(A)

    if (myAns == Ans){
      println("Trace is working")
      1
    }
    else{
      println("Trace is not working")
      -1
    }
  }

  def checkDeterminant():Int={
    val A = Array.ofDim[Double](2, 2)
    val Ans = -14.0

    A(0) = Array(3, 8)
    A(1) = Array(4, 6)

    val myAns = fD.getDet(A)

    if (myAns == Ans){
      println("Determinant is working")
      1
    }
    else{
      println("Determinant is not working")
      -1
    }
  }

  def checkMatrixMultiply():Int={
    val Ans = Array.ofDim[Double](3,3)
    Ans(0) = Array(50, 36, 76)
    Ans(1) = Array(34, 36, 66)
    Ans(2) = Array(101, 80, 112)

    val A = Array.ofDim[Double](3, 4)
    A(0) = Array(5, 2, 1, 0)
    A(1) = Array(1, 4, 3, 2)
    A(2) = Array(3, 5, 7, 8)

    val B = Array.ofDim[Double](4, 3)
    B(0) = Array(9, 5, 10)
    B(1) = Array(1, 4, 12)
    B(2) = Array(3, 3, 2)
    B(3) = Array(6, 3, 1)

    val myAns = fD.matrixMultiply(A, B)

    var isWorking = true

    for (x <- 0 until myAns.length){
      print("[ ")
      for (y <- 0 until myAns(0).length){
        if (myAns(x)(y) != Ans(x)(y)){
          printf("Matrix multiplication is not working, failed at %d, %d", x, y)
          isWorking = false
        }
        print(myAns(x)(y))
        print(" , ")
      }
      print(" ]\n")
    }

    if (isWorking) {
      println("Matrix multiplication is working")
      1
    }
    else {-1
    }

  }
}
