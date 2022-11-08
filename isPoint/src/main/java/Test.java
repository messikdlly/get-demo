import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        // 被检测的经纬度点
        String X="116.377872";
        String Y="39.911031";
        // 商业区域（百度多边形区域经纬度集合）
        String partitionLocation = "116.377679_39.911113,116.378052_39.911085,116.378047_39.910933,116.377679_39.910937";
        System.out.println(isInPolygon(X,Y,partitionLocation));
    }

    /**
     * 判断当前位置是否在多边形区域内
     * @param
     * @param partitionLocation 区域顶点
     * @return
     */
    public static boolean isInPolygon(String X,String Y,String partitionLocation){

        double p_x =Double.parseDouble(X);
        double p_y =Double.parseDouble(Y);
        Point2D.Double point = new Point2D.Double(p_x, p_y);

        List<Point2D.Double> pointList= new ArrayList<Point2D.Double>();
        String[] strList = partitionLocation.split(",");

        for (String str : strList){
            String[] points = str.split("_");
            double polygonPoint_x=Double.parseDouble(points[0]);
            double polygonPoint_y=Double.parseDouble(points[1]);
            Point2D.Double polygonPoint = new Point2D.Double(polygonPoint_x,polygonPoint_y);
            pointList.add(polygonPoint);
        }
        return IsPtInPoly(point,pointList);
    }
    /**
     * 判断点是否在多边形内，如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
     * @param point 检测点
     * @param pts   多边形的顶点
     * @return      点在多边形内返回true,否则返回false
     */
    public static boolean IsPtInPoly(Point2D.Double point, List<Point2D.Double> pts){

        int N = pts.size();
        boolean boundOrVertex = true; //如果点位于多边形的顶点或边上，也算做点在多边形内，直接返回true
        int intersectCount = 0;//cross points count of x
        double precision = 2e-10; //浮点类型计算时候与0比较时候的容差
        Point2D.Double p1, p2;//neighbour bound vertices
        Point2D.Double p = point; //当前点

        p1 = pts.get(0);//left vertex
        for(int i = 1; i <= N; ++i){//check all rays
            if(p.equals(p1)){
                return boundOrVertex;//p is an vertex
            }

            p2 = pts.get(i % N);
            if(p.x < Math.min(p1.x, p2.x) || p.x > Math.max(p1.x, p2.x)){
                p1 = p2;
                continue;
            }

            if(p.x > Math.min(p1.x, p2.x) && p.x < Math.max(p1.x, p2.x)){
                if(p.y <= Math.max(p1.y, p2.y)){
                    if(p1.x == p2.x && p.y >= Math.min(p1.y, p2.y)){
                        return boundOrVertex;
                    }

                    if(p1.y == p2.y){
                        if(p1.y == p.y){
                            return boundOrVertex;
                        }else{//before ray
                            ++intersectCount;
                        }
                    }else{
                        double xinters = (p.x - p1.x) * (p2.y - p1.y) / (p2.x - p1.x) + p1.y;
                        if(Math.abs(p.y - xinters) < precision){
                            return boundOrVertex;
                        }

                        if(p.y < xinters){
                            ++intersectCount;
                        }
                    }
                }
            }else{
                if(p.x == p2.x && p.y <= p2.y){
                    Point2D.Double p3 = pts.get((i+1) % N);
                    if(p.x >= Math.min(p1.x, p3.x) && p.x <= Math.max(p1.x, p3.x)){
                        ++intersectCount;
                    }else{
                        intersectCount += 2;
                    }
                }
            }
            p1 = p2;
        }

        if(intersectCount % 2 == 0){//偶数在多边形外
            return false;
        } else { //奇数在多边形内
            return true;
        }
    }

}
