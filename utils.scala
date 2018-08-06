import java.awt.image.BufferedImage

class utils {

  //util for displaying 2D function
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
        //set color to represent intensity of cornerness
        val Rvalue = (255.0*(in(x)(y) - min) / (max - min)).toInt
        out.setRGB(x, y, (mask(Rvalue) << 16) + mask(255-Rvalue)) //set to red
      }
    }
    out
  }

  //grayscale to colored scale image
  def colorMap(in: BufferedImage):BufferedImage={
    if (in.getColorModel.getNumColorComponents == 1) {
      val w = in.getWidth
      val h = in.getHeight

      val out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
      val inRaster = in.getData //readable Raster

      for (x <- 0 until w) {
        for (y <- 0 until h) {
          //set color to represent intensity of cornerness
          val value = inRaster.getSample(x, y, 0)
          out.setRGB(x, y, mask(value) << 16 + mask(255-value)) //set to red
        }
      }
      out
    }
    else{
      println("Attempted to color map non grayscale image!")
      in
    }
  }

  def mask(valuein:Int, max:Int=255):Int={
    if (valuein > max)
      max
    else if (valuein < 0)
      0
    else
      valuein
  }

}
