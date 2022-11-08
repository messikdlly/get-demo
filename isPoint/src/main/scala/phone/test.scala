package phone

import org.apache.spark.sql.SparkSession
import org.apache.spark.{SparkConf, SparkContext}

import java.awt.geom.Point2D
import java.util
import scala.collection.convert.ImplicitConversions.`buffer AsJavaList`
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.{break, breakable}

object test {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("testRdd")
    val sc = new SparkContext(conf)
    //val spark = SparkSession.builder().config(conf).getOrCreate()
    val data = sc.textFile("D://项目//phone//行政区//shp2.txt").map(i=>i.split(" ")).collect()
    val data_OD = sc.textFile("C:\\Users\\pc\\Desktop\\草稿.txt").map(i=>i.split(" ")).collect()
    //val data_OD = sc.textFile("C://Users//pc//Desktop//草稿.txt").map(i=>i.split(" ")).collect()C:\Users\pc\Desktop\草稿.txt

    val list = new ListBuffer[String]()
    for(j <- 0 to data.length-1){
      for(i <- 0 to data_OD.length-1){
        if(isInPolygon(data_OD(i)(0), data_OD(i)(1), data(j)(1)) == true){
          list.add(i+":"+data(j)(0)+" 0")
          println(i+":"+data_OD(i)(0)+" "+data_OD(i)(1)+data(j)(0)+" 0")
        }
        if(isInPolygon(data_OD(i)(2), data_OD(i)(3), data(j)(1)) == true){
          list.add(i+":"+data(j)(0)+" 1")
          println(i+":"+data_OD(i)(2)+" "+data_OD(i)(3)+data(j)(0)+" 1")
        }
      }
    }
    val value = sc.makeRDD(list,1).map(i=>i.split(":"))
    val value0 = value.map(i=>i(0).toInt)
    val value1 = value.map(i=>i(1))
    val value_zip = value0.zip(value1)
    val value2 = value_zip.reduceByKey((x: String, y: String) => {
      if(x.split(" ")(1).toInt==0){
        x.split(" ")(0) + "--" + y.split(" ")(0)
      }else{
        y.split(" ")(0) + "--" + x.split(" ")(0)
      }
    }).sortBy(i=>i._1).collect().foreach(println)

    //value2.saveAsTextFile("D://项目//phone//result街道.txt")

  }


  /**
   * 判断当前位置是否在多边形区域内
   *
   * @param
   * @param partitionLocation 区域顶点
   * @return
   */
  def isInPolygon(X: String, Y: String, partitionLocation: String): Boolean = {
    val p_x = X.toDouble
    val p_y = Y.toDouble
    val point = new Point2D.Double(p_x, p_y)
    val pointList =  new util.ArrayList[Point2D.Double]
    val strList = partitionLocation.split(",")
    for (str <- strList) {
      val points = str.split("_")
      val polygonPoint_x = points(0).toDouble
      val polygonPoint_y = points(1).toDouble
      val polygonPoint = new Point2D.Double(polygonPoint_x, polygonPoint_y)
      pointList.add(polygonPoint)
    }
    IsPtInPoly(point, pointList)
  }

  /**
   * 判断点是否在多边形内，如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
   *
   * @param point 检测点
   * @param pts   多边形的顶点
   * @return 点在多边形内返回true,否则返回false
   */
  def IsPtInPoly(point: Point2D.Double, pts: util.ArrayList[Point2D.Double]): Boolean = {
    val N = pts.size()
    val boundOrVertex = true //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
    var intersectCount = 0 //cross points count of x//x的交叉点计数
    val precision = 2e-10 //浮点类型计算时候与0比较时候的容差
    var p1 = new Point2D.Double
    var p2 = new Point2D.Double
    var p = new Point2D.Double
    p = point //当前点

    p1 = pts.get(0)//left vertex

    for (i <- 1 to N) {
      if(p.equals(p1)){
        return boundOrVertex;//p is an vertex
      }
      p2 = pts.get(i % N)

      breakable(
        if (p.x < Math.min(p1.x, p2.x) || p.x > Math.max(p1.x, p2.x)) {
          p1 = p2
          break
        })

      if (p.x > Math.min(p1.x, p2.x) && p.x < Math.max(p1.x, p2.x)) if (p.y <= Math.max(p1.y, p2.y)) {
        if (p.y <= Math.max(p1.y, p2.y)) {
          if (p1.x == p2.x && p.y >= Math.min(p1.y, p2.y)) return boundOrVertex

          if (p1.y == p2.y) if (p1.y == p.y) return boundOrVertex
          else { //before ray
            intersectCount += 1
          }
          else {
            val xinters = (p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y
            if (Math.abs(p.y - xinters) < precision) return boundOrVertex
            if (p.y < xinters) intersectCount += 1
          }

        }

      }else{
        if (p.x == p2.x && p.y <= p2.y) {
          val p3 = pts.get((i + 1) % N)
          if (p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(p1.x, p3.x)) intersectCount += 1
          else intersectCount += 2
        }
      }
      p1 = p2
    }

    if (intersectCount % 2 == 0) { //偶数在多边形外
      return false
    }
    else { //奇数在多边形内
      return true
    }


  }

}
