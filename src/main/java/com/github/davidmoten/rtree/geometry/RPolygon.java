package com.github.davidmoten.rtree.geometry;

import java.util.ArrayList;
import java.util.List;

public class RPolygon implements HasGeometry, Geometry {

    private List<Point> points;  //多边形的顶点

    public RPolygon(List<Point> points) {
        this.points = points;
    }
    
	public RPolygon(List<Double> polygonXA, List<Double> polygonYA) {
		points = new ArrayList<Point>(polygonXA.size());
		for (int i = 0; i < polygonXA.size(); i++) {
			points.add(Point.create(polygonXA.get(i), polygonYA.get(i)));
		}
	}
	
	static RPolygon create(List<Double> polygonXA, List<Double> polygonYA) {
		if (null == polygonXA || null == polygonYA) {
			return null;
		}
		if (polygonXA.size() != polygonYA.size()) {
			return null;
		}
		return new RPolygon(polygonXA, polygonYA);
	}

    List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    @Override
    public Rectangle geometry() {
        if (points != null && points.size() > 2) {
            double minX = points.get(0).x();
            double minY = points.get(0).y();
            double maxX = points.get(0).x();
            double maxY = points.get(0).y();
            for (int i = 1; i < points.size(); i++) {
                minX = Math.min(minX, points.get(i).x());
                minY = Math.min(minY, points.get(i).y());
                maxX = Math.max(maxX, points.get(i).x());
                maxY = Math.max(maxY, points.get(i).y());
            }
            return RectangleImpl.create(minX, minY, maxX, maxY);
        }
        return null;
    }

    private boolean polygonsIntersect(RPolygon p) {
        // 如果一个范围包含另一个范围，则返回true;
        int size = p.points.size() - 1;
        double x = p.points.get(size).x();
        double y = p.points.get(size).y();
        if (searchPoint(Point.create(x, y)))
            return true;
        x = this.points.get(size).x();
        y = this.points.get(size).y();
        if (p.searchPoint(Point.create(x, y)))
            return true;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < this.points.size() - 1; j++) {
                if (Line.linesIntersect(p.points.get(i).x(), p.points.get(i)
                                .y(), p.points.get(i + 1).x(), p.points
                                .get(i + 1).y(), this.points.get(j).x(),
                        this.points.get(j).y(), this.points.get(j + 1)
                                .x(), this.points.get(j + 1).y()))
                    return true;
            }
        }
        return false;
    }

    @Override
    public double distance(Rectangle r) {
        return r.distance(this.geometry());
    }

    @Override
    public Rectangle mbr() {
        return this.geometry();
    }

    @Override
    public boolean intersects(Rectangle r) {
        return this.polygonsIntersect(new RectangleImpl(r.x1(), r.x2(), r.y1(), r.y2()).createRPolygon());
    }

    @Override
    public boolean searchPoint(Point point) {
        double x = point.x();
        double y = point.y();
        int nPoints = points.size();
        int hits = 0;

        double lastX = points.get(nPoints - 1).x();
        double lastY = points.get(nPoints - 1).y();
        double curX, curY;

        // Walk the edges of the polygon
        for (int i = 0; i < nPoints; lastX = curX, lastY = curY, i++) {
            curX = points.get(i).x();
            curY = points.get(i).y();

            if (curY == lastY) {
                continue;
            }

            double leftX;
            if (curX < lastX) {
                if (x >= lastX) {
                    continue;
                }
                leftX = curX;
            } else {
                if (x >= curX) {
                    continue;
                }
                leftX = lastX;
            }

            double test1, test2;
            if (curY < lastY) {
                if (y < curY || y >= lastY) {
                    continue;
                }
                if (x < leftX) {
                    hits++;
                    continue;
                }
                test1 = x - curX;
                test2 = y - curY;
            } else {
                if (y < lastY || y >= curY) {
                    continue;
                }
                if (x < leftX) {
                    hits++;
                    continue;
                }
                test1 = x - lastX;
                test2 = y - lastY;
            }

            if (test1 < (test2 / (lastY - curY) * (lastX - curX))) {
                hits++;
            }
        }
        return (hits & 1) != 0;
    }

}
