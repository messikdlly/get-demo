package AdministrativeRegion

import org.apache.spark.{SparkConf, SparkContext}

object Region_result {
  def main(args: Array[String]): Unit = {

      val conf = new SparkConf().setMaster("local[*]").setAppName("jiedao_result")
      val sc = new SparkContext(conf)
      val data_Region = sc.textFile("D://项目//phone//Result_file//行政区//file//shp1.txt")
      val data_OD = sc.textFile("D://项目//phone//经纬度文件//test_OD.txt").map(i => i.split(" ")).collect()

      val data_Region1 = data_Region.map(line => line.replaceAll("\\[", "").replaceAll("\\]", "")).map(_.split(";")).map(i=>i)

      data_Region1.collect().foreach(println)



    }


    def isPointInPoly(point: Point, poly: Array[Point]) = {

      var intersectionp = 0
      for (i <- 0 until poly.length) {

        val point1: Point = poly(i)
        val point2: Point = poly((i + 1) % poly.length)

        if (point.y >= Math.min(point1.y, point2.y) && point.y < Math.max(point1.y, point2.y))
          if (((point.y - point1.y) * (point2.x - point1.x) / (point2.y - point1.y) + point1.x) < point.x)
            intersectionp = intersectionp + 1
      }
      intersectionp % 2
    }


}