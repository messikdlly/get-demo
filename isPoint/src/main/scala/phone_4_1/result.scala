package phone_4_1

import org.apache.spark.{SparkConf, SparkContext}

import java.awt.geom.Point2D
import java.util
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.Breaks.{break, breakable}

object result {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("testRdd")
    val sc = new SparkContext(conf)
    val dataset = sc.textFile("D://项目//phone//长_短驻人口4_1//test.txt")//原始的test数据
    val dataset_filter = dataset.map(i=>i.filterNot(c => c == '(' || c==')' ))//把括号和分号去掉
//    dataset_filter.collect().foreach(println)
    val dataset_split = dataset_filter.map(i=>i.split(",")).map(i=>i(1)).map(i => i.split(";")).collect()//.map(i=>i.replaceAll(":",";"))
//    dataset_split.collect().foreach(println)
    val res = new ArrayBuffer[String]
    for (i <- 0 to dataset_split.length-1) {
      if (dataset_split(i).length != 1) {
        for (j <- 0 to dataset_split(i).length - 1) {
          res.append(dataset_split(i)(j)+":"+i)
        }
      }
    }
    val datapre = sc.makeRDD(res)
    val data = datapre.map(i => i.split(":")).map(i => i(0) + "," + i(1) + "," + i(3)).map(i=>i.split(",")).collect()//要重新写入数据map两次？？
//    data.collect().foreach(println)
    val data_shp = sc.textFile("D://项目//phone//行政区//shp2.txt").map(i=>i.split(" ")).collect()
    val list = new ListBuffer[String]()
    for(i <- 0 to data.length-1){
      for(j <- 0 to data_shp.length-1){
          if(isInPolygon(data(i)(0),data(i)(1),data_shp(j)(1))==true){
            list.append(data_shp(j)(0)+","+data(i)(2)+","+1.toString)
            println(data_shp(j)(0),data(i)(2),1.toString)
          }
        }
      }
//    println(list)
    val datalist = sc.makeRDD(list)
    datalist.cache()
//    datalist.collect().foreach(println)
    val datalist1 = datalist.map(i => i.split(",")).map(i=>i(0)+","+i(1))
    val datalist2 = datalist.map(i => i.split(",")).map(i=>i(2))
    val datalist_zip = datalist1.zip(datalist2)
//    datalist_zip.collect().foreach(println)
    val datas = datalist_zip.reduceByKey((x: String, y: String)=>{
      (x.toInt + y.toInt).toString
    })
    val datass = datas.map(i => (i._1.split(","), i._2, 1)).map(i=>(i._1(0)+","+i._2,i._3))
//    datass.collect().foreach(println)
    val result1 = datass.reduceByKey((x: Int, y: Int)=>{x+y})
//    result1.collect().foreach(println)
    val arr = Array.ofDim[Int](39,32)
    for(i <- 0 to arr.length-1) {
        arr(i)(0) = data_shp(i)(0).filter(_.isDigit).toInt
    }
    val result2 = result1.map(i=>(i._1.split(","),i._2)).map(i=>(i._1(0),i._1(1),i._2)).collect()

//    result2.collect().foreach(println)
    for(i <- 0 to result2.length-1) {
      val x1 = result2(i)._1.filter(_.isDigit).toInt
      val x2 = result2(i)._2.toInt
      val x3 = result2(i)._3
      arr(x1)(x2) = x3
    }
    for(i <- 0 to arr.length-1) {
      for(j <- 0 to arr(i).length-1){
        print(arr(i)(j)+" ")
      }
      println()
    }
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
