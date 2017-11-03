package com.github.davidmoten.rtree.util;

import java.util.List;

public class PolygonUtils {

	public static boolean isPointInPolygon(double px, double py, List<Double> polygonXA, List<Double> polygonYA) {
		
		boolean isInside = false;
		double ESP = 1.E-009D;
		int count = 0;

		double linePoint2x = 180.0D;

		double linePoint1x = px;
		double linePoint1y = py;
		double linePoint2y = py;

		if ((polygonXA == null) || (polygonYA == null)) {
			return false;
		}

		if (!(polygonXA.get(0).equals(polygonXA.get(polygonXA.size() - 1)))
				|| !(polygonYA.get(0).equals(polygonYA.get(polygonYA.size() - 1)))) {
			polygonXA.add(polygonXA.get(0));
			polygonYA.add(polygonYA.get(0));
		}

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

	private static double multiply(double px0, double py0, double px1, double py1, double px2, double py2) {
		return (px1 - px0) * (py2 - py0) - (px2 - px0) * (py1 - py0);
	}

	private static boolean isPointOnLine(double px0, double py0, double px1, double py1, double px2, double py2) {
		boolean flag = false;
		double ESP = 1.E-009D;
		if ((Math.abs(multiply(px0, py0, px1, py1, px2, py2)) < ESP) && ((px0 - px1) * (px0 - px2) <= 0.0D)
				&& ((py0 - py1) * (py0 - py2) <= 0.0D)) {
			flag = true;
		}
		return flag;
	}

	private static boolean isIntersect(double px1, double py1, double px2, double py2, double px3, double py3, double px4, double py4) {
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