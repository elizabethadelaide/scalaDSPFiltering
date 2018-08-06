import java.awt.image.BufferedImage
import java.io.{BufferedWriter, File, FileWriter}

import javax.imageio.ImageIO

//test app for debugging corner detection
//as well as to set up for debugging and testing performance of future algorithms
object tests extends App{

  val fD = new featureDetection()
  val u = new utils()

  val myPhotoPath = "C:\\Users\\liz\\Documents\\Programs\\imageProcessing\\" //directory to process
  val photo = ImageIO.read(new File(myPhotoPath.concat("flowers.jpg"))) //image to process
  val myTestPath = myPhotoPath.concat("testing\\")

  checkMatrixMultiply()
  checkDeterminant()
  checkTrace()
  checkCrop()
  checkPartials()
  checkM()
  //checkGaussian()
  //timeGaussian(1000000)

  //time gaussian operation
  //return average time
  def timeGaussian(n:Int):Long={
    fD.NGauss = 100
    val t0 = System.nanoTime()

    //do n gaussian loops
    for (i <- 0 until n){
      val n = fD.getGaussian(i % 100)
    }

    val t1 = System.nanoTime()

    val average = (t1 - t0) / n

    printf("Total time of a Gaussian operation over %d loops is %d milliseconds\n", n, (t1-t0)/1000000)
    printf("Average time of a Gaussian operation over %d loops is %d nanoseconds\n", n, average)

    average
  }

  //outputs a CSV file of M matrix
  def checkM():Int={

    //preprocess with gaussian
    var gray = fD.k.color2gray(photo) //smooth and gray scale
    gray = fD.crop(gray) //image needs to be square

    var t0 = System.currentTimeMillis()
    var M = fD.getM(gray)
    var t1 = System.currentTimeMillis()

    var w = M.getZSize()
    var h = M.getWSize()

    printf("An M matrix with flat window of %d by %d was processed in %d millseconds\n", w, h, t1-t0)

    /*fD.setWindowFunction("Gaussian")

    t0 = System.currentTimeMillis()
    M = fD.getM(gray)
    t1 = System.currentTimeMillis()

    w = M(0)(0).length
    h = M(0)(0)(0).length

    printf("An M matrix with gaussian window of %d by %d was processed in %d millseconds\n", w, h, t1-t0)*/

    //check if M actually did anything:
    val max = M.getMax
    if (max == 0){
      println("The M matrix had no maxes, likely not working")
    }


    val bw = new BufferedWriter(new FileWriter(new File(myTestPath.concat("M.csv"))))

    val IX2 = Array.ofDim[Double](w, h)
    val IXIY = Array.ofDim[Double](w, h)
    val IYIX = Array.ofDim[Double](w, h)
    val IY2 = Array.ofDim[Double](w, h)

    for (x <- 0 until w){
      for (y <- 0 until h){
        val m = M.getMatrix(x, y)
        val A = m(0)(0)
        val B = m(0)(1)
        val C = m(1)(0)
        val D = m(1)(1)
        IX2(x)(y) = A
        IXIY(x)(y) = B
        IYIX(x)(y) = C
        IY2(x)(y) = D
        bw.write(f"$x , $y , $A, $B, $C, $D\n")
      }
    }
    bw.close()

    var out = u.colorMap(IX2)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("Ix2.jpg")))
    out = u.colorMap(IXIY)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("IxIy.jpg")))
    out = u.colorMap(IYIX)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("IyIx.jpg")))
    out = u.colorMap(IY2)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("Iy2.jpg")))

    println("M written to to CSV, with images in testing folder")

    1
  }

  def checkGaussian():Int={
    val n = 34
    val N = 100
    fD.setGaussSigma(0.2) //set different sigma
    fD.NGauss = N.toDouble //set window size
    val ans = 0.29357 //expected answer
    val myAns = fD.getGaussian(n)

    val error = Math.abs(ans - myAns)

    if (error < 0.00001){
      printf("Gaussian is working with error %f\n", error)
      1
    }
    else{
      printf("Gaussian is not working with error %f\n", error)
      -1
    }
  }

  def checkPartials():Int={
    val gray = fD.k.color2gray(photo)
    val Ix = fD.getIx(gray)
    val Iy = fD.getIy(gray)
    val w = gray.getWidth
    val h = gray.getHeight

    val xOut = u.colorMap(Ix)
    val yOut = u.colorMap(Iy)

    ImageIO.write(xOut, "jpg", new File(myTestPath.concat("Ix.jpg")))
    ImageIO.write(yOut, "jpg", new File(myTestPath.concat("Iy.jpg")))
    println("Partials written to testing folder")
    1
  }

  def checkCrop():Int={
    val out = fD.crop(photo)
    ImageIO.write(out, "jpg", new File(myTestPath.concat("cropped.jpg")))
    if (out.getHeight == out.getWidth){
      println("Crop set the height and width equal to each other, check testing folder")
      1
    }
    else{
      println("Crop is definitely not working")
      -1
    }
  }

  def checkTrace():Int={
    val A = new fD.Tensor(2, 2, 1, 1)
    val Ans = 9

    var pushReturn = A.push(0, 0, 3)
    pushReturn = A.push(0, 0, 8)
    pushReturn = A.push(0, 0, 4)
    pushReturn = A.push(0, 0, 6)

    val myAns = A.getTrace(0, 0)

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
    val A = new fD.Tensor(2, 2, 1, 1)
    val Ans = -14.0

    //push one 2 x 2 array
    var pushReturn = A.push(0, 0, 3)
    pushReturn = A.push(0, 0, 8)
    pushReturn = A.push(0, 0, 4)
    pushReturn = A.push(0, 0, 6)

    /*printf("Push returns %d, expected is 4\n", pushReturn)
    pushReturn = A.push(0, 0, 3) //attempt to push another value
    printf("Push returns %d, expected is 0\n", pushReturn)*/


    val myAns = A.getDet(0, 0)

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
