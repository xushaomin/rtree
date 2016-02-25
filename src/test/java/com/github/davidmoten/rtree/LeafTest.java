package com.github.davidmoten.rtree;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Rectangle;

public class LeafTest {

    private static Context<Object, Rectangle> context = new Context<Object, Rectangle>(2, 4,
            new SelectorMinimalAreaIncrease(), new SplitterQuadratic(),
            FactoryDefault.<Object, Rectangle> instance());

    @Test(expected = IllegalArgumentException.class)
    public void testCannotHaveZeroChildren() {
        new LeafDefault<Object, Rectangle>(new ArrayList<Entry<Object, Rectangle>>(), context);
    }

    @Test
    public void testMbr() {
        Rectangle r1 = Geometries.rectangle(0, 1, 3, 5);
        Rectangle r2 = Geometries.rectangle(1, 2, 4, 6);
        @SuppressWarnings("unchecked")
        Rectangle r = new LeafDefault<Object, Rectangle>(
                Arrays.asList(EntryDefault.entry(new Object(), r1), EntryDefault.entry(new Object(), r2)),
                context).geometry().mbr();
        assertEquals(r1.add(r2), r);
    }
}
