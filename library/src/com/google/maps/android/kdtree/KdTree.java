package com.google.maps.android.kdtree;

import com.google.maps.android.geometry.Bounds;
import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class KdTree<T extends KdTree.Item> {
    public interface Item {
        public Point getPoint();
    }

    /**
     * For use in getBounds.
     * Sigma is used to ensure search is inclusive of upper bounds (eg if a point is on exactly the
     * upper bound, it should be returned)
     */
    static double sigma = 0.0000001;

    /**
     * The bounds of this quad.
     */
    private final Bounds mBounds;

    /**
     * The depth of this quad in the tree.
     */
    private final int mDepth;

    /**
     * Maximum number of elements to store in a quad before splitting.
     */
    private final static int MAX_ELEMENTS = 50;

    /**
     * The elements inside this node, sorted according to their x values.
     */
    private ArrayList<T> mXItems;

    /**
     * The elements inside this node, sorted according to their y values.
     */
    private ArrayList<T> mYItems;

    /**
     * Maximum depth.
     */
    private final static int MAX_DEPTH = 40;

    /**
     * Child quads.
     */
    private KdTree<T>[] mChildren = null;

    public KdTree(ArrayList<T> items) {
        //sort things;
        mXItems = new ArrayList<T>(items);
        Collections.sort(mXItems, new ItemXComparator());
        mYItems = new ArrayList<T>(items);
        Collections.sort(mYItems, new ItemYComparator());
        mDepth = 0;
        if (items == null) {
            mBounds = null;
        } else {
            mBounds = getBounds(items);
            if (mXItems.size() > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
                split();
            }
        }
    }

    private KdTree(ArrayList<T> xitems, ArrayList<T> yitems, int depth, Bounds bounds) {
        mXItems = xitems;
        mYItems = yitems;
        mDepth = depth;
        mBounds = bounds;
        if (mXItems.size() > MAX_ELEMENTS && mDepth < MAX_DEPTH) {
            split();
        }
    }

    private void split() {
        Bounds lowBounds, highBounds;
        if (mDepth % 2 == 0) {
            double boundary = mXItems.get(mXItems.size() / 2).getPoint().x;
            lowBounds = new Bounds(mBounds.minX, boundary + sigma, mBounds.minY, mBounds.maxY);
            highBounds = new Bounds(mBounds.minX, boundary, mBounds.minY, mBounds.maxY);
        } else {
            double boundary = mYItems.get(mYItems.size() / 2).getPoint().y;
            lowBounds = new Bounds(mBounds.minX, mBounds.maxX, mBounds.minY, boundary + sigma);
            highBounds = new Bounds(mBounds.minX, mBounds.maxX, mBounds.minY, boundary);
        }
        mChildren = new KdTree[]{
                new KdTree(x1arraylist, y1arraylist, mDepth + 1, lowBounds),
                new KdTree(x2arraylist, y2arraylist, mDepth + 1, highBounds)
        };
        mXItems = null;
        mYItems = null;
    }

    /**
     * Search for all items within a given bounds.
     */
    public Collection<T> search(Bounds searchBounds) {
        final List<T> results = new ArrayList<T>();
        if (mBounds != null) {
            search(searchBounds, results);
        }
        return results;
    }

    private void search(Bounds searchBounds, Collection<T> results) {
        if (!mBounds.intersects(searchBounds)) {
            return;
        }

        if (this.mChildren != null) {
            for (KdTree<T> quad : mChildren) {
                quad.search(searchBounds, results);
            }
        } else if (mXItems != null) {
            if (searchBounds.contains(mBounds)) {
                results.addAll(mXItems);
            } else {
                for (T item : mXItems) {
                    if (searchBounds.contains(item.getPoint())) {
                        results.add(item);
                    }
                }
            }
        }
    }

    /**
     * Helper function for quadtree creation
     *
     * @param points Collection of WeightedLatLng to calculate bounds for
     * @return Bounds that enclose the listed WeightedLatLng points
     */
    private Bounds getBounds(Collection<T> points) {

        // Use an iterator, need to access any one point of the collection for starting bounds
        Iterator<T> iter = points.iterator();

        T first = iter.next();

        double minX = first.getPoint().x;
        double maxX = first.getPoint().x + sigma;
        double minY = first.getPoint().y;
        double maxY = first.getPoint().y + sigma;

        while (iter.hasNext()) {
            T l = iter.next();
            double x = l.getPoint().x;
            double y = l.getPoint().y;
            // Extend bounds if necessary
            if (x < minX) minX = x;
            if (x + sigma > maxX) maxX = x + sigma;
            if (y < minY) minY = y;
            if (y + sigma > maxY) maxY = y + sigma;
        }

        return new Bounds(minX, maxX, minY, maxY);
    }

    private class ItemXComparator implements Comparator<Item> {

        @Override
        public int compare(Item a, Item b) {
            if (a.getPoint().x < b.getPoint().x) {
                return -1;
            } else if (a.getPoint().x == b.getPoint().x) {
                 if (a.getPoint().y < b.getPoint().y) {
                    return -1;
                 } else if (a.getPoint().y == b.getPoint().y) {
                     return 0;
                 } else {
                     return 1;
                 }
            } else {
                return 1;
            }
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }

    private class ItemYComparator implements Comparator<Item> {

        @Override
        public int compare(Item a, Item b) {
            if (a.getPoint().y < b.getPoint().y) {
                return -1;
            } else if (a.getPoint().y == b.getPoint().y) {
                if (a.getPoint().x < b.getPoint().x) {
                    return -1;
                } else if (a.getPoint().x == b.getPoint().x) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }
    }

}
