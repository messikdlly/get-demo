package jiedao

case class Point(var x : Double, var y : Double) {

  def getPoint = Point.apply(x,y)

}