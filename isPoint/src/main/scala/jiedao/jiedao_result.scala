package jiedao

import org.apache.spark.{SparkConf, SparkContext}

object jiedao_result {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("jiedao_result")
    val sc = new SparkContext(conf)

    val data = sc.textFile("D://项目//phone//行政区//shp2.txt").map(i=>i.split(" ")).collect()
    val data_OD = sc.textFile("D://项目//phone//经纬度文件//test_OD.txt").map(i=>i.split(" ")).collect()
  }









  def isPointInPoly(point : Point,poly : Array[Point]) = {

    var intersectionp = 0
    for (i <- 0 until poly.length){

      val point1: Point = poly(i)
      val point2: Point = poly((i + 1) % poly.length)

      if (point.y >= Math.min(point1.y, point2.y) && point.y < Math.max(point1.y, point2.y))
        if (((point.y - point1.y) * (point2.x - point1.x) / (point2.y - point1.y) + point1.x) < point.x)
          intersectionp = intersectionp + 1
    }
    intersectionp % 2
  }

}
