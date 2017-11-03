package com.github.davidmoten.rtree.geometry;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.guavamini.Objects;
import com.github.davidmoten.guavamini.Optional;
import com.github.davidmoten.rtree.internal.Util;
import com.github.davidmoten.rtree.internal.util.ObjectsHelper;
import com.github.davidmoten.rtree.util.PolygonUtils;

public class Polygon implements Geometry {

    private final List<Double> polygonXA;
    private final List<Double> polygonYA;
    
    private final Rectangle mbr;

    public Polygon(List<Point> list, List<Double> polygonXA, List<Double> polygonYA) {
        this.polygonXA = polygonXA;
        this.polygonYA = polygonYA;
        this.mbr = Util.mbr(list);
    }
    
    /*static Polygon create(List<Point> list) {
        return new Polygon(list);
    }*/
    
    /*static Polygon create(List<Double> polygonXA, List<Double> polygonYA) {
    	List<Point> list = new ArrayList<Point>(polygonXA.size());
    	for (int i = 0; i < polygonXA.size(); i++) {
    		Point p = Point.create(polygonXA.get(i), polygonYA.get(i));
    		list.add(p);
		}
        return new Polygon(list);
    }*/
    
    static Polygon create(List<Double> polygonXA, List<Double> polygonYA) {
    	List<Point> list = new ArrayList<Point>(polygonXA.size());
    	for (int i = 0; i < polygonXA.size(); i++) {
    		Double x = polygonXA.get(i);
    		Double y = polygonYA.get(i);
    		Point p = Point.create(x, y);
    		list.add(p);
		}
        return new Polygon(list, polygonXA, polygonYA);
    }
    
    private static boolean intersects(float x1, float y1, float x2, float y2, float a1, float b1, float a2, float b2) {
        return x1 <= a2 && a1 <= x2 && y1 <= b2 && b1 <= y2;
    }
    
    private static float max(float a, float b) {
        if (a < b)
            return b;
        else
            return a;
    }
    
    public static double distance(float x1, float y1, float x2, float y2, float a1, float b1, float a2, float b2) {
        if (intersects(x1, y1, x2, y2, a1, b1, a2, b2)) {
            return 0;
        }
        boolean xyMostLeft = x1 < a1;
        float mostLeftX1 = xyMostLeft ? x1 : a1;
        float mostRightX1 = xyMostLeft ? a1 : x1;
        float mostLeftX2 = xyMostLeft ? x2 : a2;
        double xDifference = max(0, mostLeftX1 == mostRightX1 ? 0 : mostRightX1 - mostLeftX2);

        boolean xyMostDown = y1 < b1;
        float mostDownY1 = xyMostDown ? y1 : b1;
        float mostUpY1 = xyMostDown ? b1 : y1;
        float mostDownY2 = xyMostDown ? y2 : b2;

        double yDifference = max(0, mostDownY1 == mostUpY1 ? 0 : mostUpY1 - mostDownY2);

        return Math.sqrt(xDifference * xDifference + yDifference * yDifference);
    }
    
	@Override
	public double distance(Rectangle r) {
		float x1 = this.mbr.x1();
		float y1 = this.mbr.y1(); 
		float x2 = this.mbr.x2();
		float y2 = this.mbr.y2();		
		return distance(x1, y1, x2, y2, r.x1(), r.y1(), r.x2(), r.y2());
	}

	@Override
	public Rectangle mbr() {
		return mbr;
	}

	@Override
	public boolean intersects(Rectangle r) {		
		float x1 = this.mbr.x1();
		float y1 = this.mbr.y1(); 
		float x2 = this.mbr.x2();
		float y2 = this.mbr.y2();
		return intersects(x1, y1, x2, y2, r.x1(), r.y1(), r.x2(), r.y2());
	}
	
	@Override
    public int hashCode() {
        return Objects.hashCode(polygonXA) + Objects.hashCode(polygonYA);
    }

	@Override
    public boolean equals(Object obj) {
        Optional<Polygon> other = ObjectsHelper.asClass(obj, Polygon.class);
        if (other.isPresent()) {
            return Objects.equal(polygonXA, other.get().polygonXA);
        } else
            return false;
    }

    public boolean intersects(Point point) {
        return this.contains(point.x(), point.y());
    }
	
	public boolean contains(double px, double py) {
		return PolygonUtils.isPointInPolygon(px, py, polygonXA, polygonYA);
	}
	
}
