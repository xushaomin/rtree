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
    
    static Polygon create(List<String> polygonXA, List<String> polygonYA) {
    	List<Point> list = new ArrayList<Point>(polygonXA.size());
    	List<Double> polygonXAs = new ArrayList<>(polygonXA.size());
    	List<Double> polygonYAs = new ArrayList<>(polygonYA.size());
    	for (int i = 0; i < polygonXA.size(); i++) {
    		Double x = Double.parseDouble(polygonXA.get(i));
    		Double y = Double.parseDouble(polygonYA.get(i));
    		Point p = Point.create(x, y);
    		list.add(p);
    		polygonXAs.add(x);
    		polygonYAs.add(y);
		}
        return new Polygon(list, polygonXAs, polygonYAs);
    }
    
    public static Polygon create(String polygonXA, String polygonYA) {
    	String[] polygonXAs = polygonXA.split(",");
    	String[] polygonYAs = polygonXA.split(",");
    	List<Double> polygonXAss = new ArrayList<>(polygonXA.length());
    	List<Double> polygonYAss = new ArrayList<>(polygonYA.length());
    	List<Point> list = new ArrayList<Point>(polygonXAs.length);
    	for (int i = 0; i < polygonXAs.length; i++) {
    		Double x = Double.parseDouble(polygonXAs[i]);
    		Double y = Double.parseDouble(polygonYAs[i]);
    		Point p = Point.create(x, y);
    		list.add(p);
    		polygonXAss.add(x);
    		polygonYAss.add(y);
		}
    	return new Polygon(list, polygonXAss, polygonYAss);
    }

	@Override
	public double distance(Rectangle r) {
		return 0;
	}

	@Override
	public Rectangle mbr() {
		return mbr;
	}

	@Override
	public boolean intersects(Rectangle r) {
		
		/*double px1, double py1, 
		double px2, double py2, 
		double px3, double py3,
		double px4, double py4
		
		boolean flag = false;
		double d = (px2 - px1) * (py4 - py3) - (py2 - py1) * (px4 - px3);
		if (d != 0.0D) {
			double r = ((py1 - py3) * (px4 - px3) - (px1 - px3) * (py4 - py3)) / d;
			double s = ((py1 - py3) * (px2 - px1) - (px1 - px3) * (py2 - py1)) / d;
			if ((r >= 0.0D) && (r <= 1.0D) && (s >= 0.0D) && (s <= 1.0D)) {
				flag = true;
			}
		}*/
		
		System.out.println("----------------------------" + r);
		System.out.println("----------------------------" + r.x1());
		System.out.println("----------------------------" + r.y1());

		return this.contains(r.x1(), r.y1());

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

		/*if ((polygonXA == null) || (polygonYA == null)) {
			return false;
		}

		if (!(polygonXA.get(0).equals(polygonXA.get(polygonXA.size() - 1)))
				|| !(polygonYA.get(0).equals(polygonYA.get(polygonYA.size() - 1)))) {
			polygonXA.add(polygonXA.get(0));
			polygonYA.add(polygonYA.get(0));
		}*/

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
    
    
	private double Multiply(double px0, double py0, double px1, double py1, double px2, double py2) {
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
