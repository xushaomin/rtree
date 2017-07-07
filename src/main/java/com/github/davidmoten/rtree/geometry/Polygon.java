package com.github.davidmoten.rtree.geometry;

import java.util.ArrayList;
import java.util.List;

import com.github.davidmoten.guavamini.Objects;
import com.github.davidmoten.guavamini.Optional;
import com.github.davidmoten.rtree.internal.Util;
import com.github.davidmoten.rtree.internal.util.ObjectsHelper;

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
		boolean isInside = false;
		double ESP = 1.E-009D;
		int count = 0;

		double linePoint2x = 180.0D;

		double linePoint1x = px;
		double linePoint1y = py;
		double linePoint2y = py;

		for (int i = 0; i < polygonXA.size() - 1; i++) {
			double cx1 = polygonXA.get(i);
			double cy1 = polygonYA.get(i);
			double cx2 = polygonXA.get(i + 1);
			double cy2 = polygonYA.get(i + 1);
			if (isPointOnLine(px, py, cx1, cy1, cx2, cy2)) {
				return true;
			}
			if (Math.abs(cy2 - cy1) < ESP) {
				continue;
			}
			if (isPointOnLine(cx1, cy1, linePoint1x, linePoint1y, linePoint2x, linePoint2y)) {
				if (cy1 > cy2)
					count++;
			} else if (isPointOnLine(cx2, cy2, linePoint1x, linePoint1y, linePoint2x, linePoint2y)) {
				if (cy2 > cy1)
					count++;
			} else if (isIntersect(cx1, cy1, cx2, cy2, linePoint1x, linePoint1y, linePoint2x, linePoint2y)) {
				count++;
			}
		}
		if (count % 2 == 1) {
			isInside = true;
		}
		return isInside;
	}
    
    
	private static double Multiply(double px0, double py0, double px1, double py1, double px2, double py2) {
		return (px1 - px0) * (py2 - py0) - (px2 - px0) * (py1 - py0);
	}

	private boolean isPointOnLine(double px0, double py0, double px1, double py1, double px2, double py2) {
		boolean flag = false;
		double ESP = 1.E-009D;
		if ((Math.abs(Multiply(px0, py0, px1, py1, px2, py2)) < ESP) && ((px0 - px1) * (px0 - px2) <= 0.0D)
				&& ((py0 - py1) * (py0 - py2) <= 0.0D)) {
			flag = true;
		}
		return flag;
	}

	private boolean isIntersect(double px1, double py1, double px2, double py2, double px3, double py3, double px4, double py4) {
		boolean flag = false;
		double d = (px2 - px1) * (py4 - py3) - (py2 - py1) * (px4 - px3);
		if (d != 0.0D) {
			double r = ((py1 - py3) * (px4 - px3) - (px1 - px3) * (py4 - py3)) / d;
			double s = ((py1 - py3) * (px2 - px1) - (px1 - px3) * (py2 - py1)) / d;
			if ((r >= 0.0D) && (r <= 1.0D) && (s >= 0.0D) && (s <= 1.0D)) {
				flag = true;
			}
		}
		return flag;
	}

}
