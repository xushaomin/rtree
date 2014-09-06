package com.github.davidmoten.rtree;

import static com.github.davidmoten.rtree.Comparators.overlapListPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.davidmoten.rtree.geometry.HasGeometry;
import com.github.davidmoten.rtree.geometry.ListPair;
import com.google.common.base.Preconditions;

public class SplitterTopological implements Splitter {

    private final Comparator<ListPair<?>> comparator;

    @SuppressWarnings("unchecked")
    public SplitterTopological(ListPairMetric metric) {
        this.comparator = Comparators.compose(overlapListPair(metric), Comparators.areaPair);
    }

    @Override
    public <T extends HasGeometry> ListPair<T> split(List<T> items, int minSize) {
        Preconditions.checkArgument(!items.isEmpty());
        // sort nodes into increasing x, calculate min overlap where both groups
        // have more than minChildren

        List<List<T>> lists = new ArrayList<List<T>>();
        lists.add(sort(items, INCREASING_X_LOWER));
        lists.add(sort(items, INCREASING_X_UPPER));
        lists.add(sort(items, INCREASING_Y_LOWER));
        lists.add(sort(items, INCREASING_Y_UPPER));

        List<ListPair<T>> pairs = getPairs(minSize, lists);
        return Collections.min(pairs, comparator);
    }

    private static <T extends HasGeometry> List<ListPair<T>> getPairs(int minSize,
            List<List<T>> lists) {
        List<ListPair<T>> pairs = new ArrayList<ListPair<T>>();

        for (List<T> list : lists) {
            // try all splits of list where the groups have at least minChildren
            for (int i = minSize; i < list.size() - minSize; i++) {
                List<T> list1 = list.subList(0, i);
                List<T> list2 = list.subList(i, list.size());
                ListPair<T> pair = new ListPair<T>(list1, list2);
                pairs.add(pair);
            }
        }
        return pairs;
    }

    private static <T extends HasGeometry> List<T> sort(List<T> items,
            Comparator<HasGeometry> comparator) {
        ArrayList<T> list = new ArrayList<T>(items);
        Collections.sort(list, comparator);
        return list;
    }

    private static Comparator<HasGeometry> INCREASING_X_LOWER = new Comparator<HasGeometry>() {

        @Override
        public int compare(HasGeometry n1, HasGeometry n2) {
            return ((Float) n1.geometry().mbr().x1()).compareTo(n2.geometry().mbr().x1());
        }
    };

    private static Comparator<HasGeometry> INCREASING_X_UPPER = new Comparator<HasGeometry>() {

        @Override
        public int compare(HasGeometry n1, HasGeometry n2) {
            return ((Float) n1.geometry().mbr().x2()).compareTo(n2.geometry().mbr().x2());
        }
    };

    private static Comparator<HasGeometry> INCREASING_Y_LOWER = new Comparator<HasGeometry>() {

        @Override
        public int compare(HasGeometry n1, HasGeometry n2) {
            return ((Float) n1.geometry().mbr().y1()).compareTo(n2.geometry().mbr().y1());
        }
    };

    private static Comparator<HasGeometry> INCREASING_Y_UPPER = new Comparator<HasGeometry>() {

        @Override
        public int compare(HasGeometry n1, HasGeometry n2) {
            return ((Float) n1.geometry().mbr().y2()).compareTo(n2.geometry().mbr().y2());
        }
    };

}