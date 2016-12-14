public class KdTree 
{
    private Node root;
    private int count;
    private static class Node 
    {
        private Point2D point;
        private Node left, right;
        private RectHV rect;
        
        public Node(Point2D point, RectHV rect)
        {
            this.point = point; 
            this.rect = rect;
        }
    }
    
    // construct an empty set of points 
    public KdTree()         
    {
        root = null;
        count = 0;
    }
    
    // is the set empty?
    public boolean isEmpty()  
    {
        return (root == null);
    }
    
    // number of points in the set
    public int size()     
    {
        return count;
    }
    
    // add the point to the set (if it is not already in the set)
    public void insert(Point2D p)    
    {
        if (p == null) throw new NullPointerException();
        root = insert(root, p, true, null);
    }
    
    private Node insert(Node h, Point2D p, boolean LeftRight, Node parent)
    {
        if ((h != null) && (h.point.equals(p))) return h;
        if (h == null) 
        {
            ++count;
            if (parent == null)
                return new Node(p, new RectHV(0.0, 0.0, 1.0, 1.0));
            if (!LeftRight)     // this means if the parent is a LeftRight  
            {
                if (p.x() < parent.point.x())
                    return new Node(p, new RectHV(parent.rect.xmin(), parent.rect.ymin(), parent.point.x(), parent.rect.ymax()));
                else
                    return new Node(p, new RectHV(parent.point.x(), parent.rect.ymin(), parent.rect.xmax(), parent.rect.ymax()));
            }
            else
            {
                if (p.y() < parent.point.y())
                    return new Node(p, new RectHV(parent.rect.xmin(), parent.rect.ymin(), parent.rect.xmax(), parent.point.y()));
                else
                    return new Node(p, new RectHV(parent.rect.xmin(), parent.point.y(), parent.rect.xmax(), parent.rect.ymax()));
            }
        }
        if (LeftRight)
        {
            if (p.x() < h.point.x()) h.left = insert(h.left, p, !LeftRight, h); 
            else h.right = insert(h.right, p, !LeftRight, h);
            
        }
        else
        {
            if (p.y() < h.point.y()) h.left = insert(h.left, p, !LeftRight, h); 
            else h.right = insert(h.right, p, !LeftRight, h);
        }
        return h;
    }
    
    // does the set contain point p?
    public boolean contains(Point2D p)  
    {
        if (p == null) throw new NullPointerException();
        return contains(root, p, true);
    }
    
    private boolean contains(Node h, Point2D p, boolean LeftRight)
    {
        if (h == null) return false;
        if (p.equals(h.point)) return true;
        if (LeftRight)
        {
            if (p.x() < h.point.x()) return contains(h.left, p, !LeftRight); 
            else return contains(h.right, p, !LeftRight);
        }
        else
        {
            if (p.y() < h.point.y()) return contains(h.left, p, !LeftRight); 
            else return contains(h.right, p, !LeftRight);
        }
    }
    
    // draw all points to standard draw
    public void draw()      
    {
        draw(root, true);
    }
    
    private void draw(Node h, boolean LeftRight)
    {
        if (h == null) return;
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(.01);
        h.point.draw();
        if (LeftRight)
        {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.setPenRadius();
            StdDraw.line(h.point.x(), h.rect.ymin(), h.point.x(), h.rect.ymax());
        }
        else
        {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.setPenRadius();
            StdDraw.line(h.rect.xmin(), h.point.y(), h.rect.xmax(), h.point.y());
        }
        draw(h.left, !LeftRight);
        draw(h.right, !LeftRight);
    }
    
    // all points that are inside the rectangle 
    public Iterable<Point2D> range(RectHV rect) 
    {
        if (rect == null) throw new NullPointerException();
        Queue<Point2D> q = new Queue<Point2D>();
        rangeSearch(root, rect, q);
        return q;
    }
    
    private void rangeSearch(Node h, RectHV rect, Queue<Point2D> q)
    {
        if (h == null) return;
        if (rect.contains(h.point))
            q.enqueue(h.point);
        if ((h.left != null) && rect.intersects(h.left.rect))
            rangeSearch(h.left, rect, q);
        if ((h.right != null) && rect.intersects(h.right.rect))
            rangeSearch(h.right, rect, q);
    }
    
    // a nearest neighbor in the set to point p; null if the set is empty
    public Point2D nearest(Point2D p)   
    {
        if (p == null) throw new NullPointerException();
        if (root == null) return null;
        Point2D thePoint = root.point;
        thePoint = nearestNeighborSearch(root, thePoint, p);
        return thePoint;
    }
    
    private Point2D nearestNeighborSearch(Node h, Point2D thePoint, Point2D p)
    {
        if (h.point.distanceSquaredTo(p) < thePoint.distanceSquaredTo(p))
            thePoint = h.point;
        // if there are 2 possible subtrees to go down
        if ((h.left != null) && (h.right != null) && (h.left.rect.distanceSquaredTo(p) <= thePoint.distanceSquaredTo(p)) && (h.right.rect.distanceSquaredTo(p) <= thePoint.distanceSquaredTo(p)))
        {
            // choose the subtree that is on the same side of the spliting line as the query point
            if (h.left.rect.distanceSquaredTo(p) == 0.0)
            {
                thePoint = nearestNeighborSearch(h.left, thePoint, p);
                thePoint = nearestNeighborSearch(h.right, thePoint, p);
            }
            else 
            {
                thePoint = nearestNeighborSearch(h.right, thePoint, p);
                thePoint = nearestNeighborSearch(h.left, thePoint, p);
            }
        }
        else
        {
            if ((h.left != null) && (thePoint.distanceSquaredTo(p) >= h.left.rect.distanceSquaredTo(p)))
                thePoint = nearestNeighborSearch(h.left, thePoint, p);
            if ((h.right != null) && (thePoint.distanceSquaredTo(p) >= h.right.rect.distanceSquaredTo(p)))
                thePoint = nearestNeighborSearch(h.right, thePoint, p);
        }
        return thePoint;
    }
    
    // unit testing of the methods (optional)
    public static void main(String[] args)   
    {
        String filename = args[0];
        In in = new In(filename);

        StdDraw.show(0);

        // initialize the two data structures with point from standard input
        PointSET brute = new PointSET();
        KdTree kdtree = new KdTree();
        while (!in.isEmpty()) {
            double x = in.readDouble();
            double y = in.readDouble();
            Point2D p = new Point2D(x, y);
            kdtree.insert(p);
            brute.insert(p);
        }

        while (true) {

            // the location (x, y) of the mouse
            double x = StdDraw.mouseX();
            double y = StdDraw.mouseY();
            Point2D query = new Point2D(x, y);

            // draw all of the points
            StdDraw.clear();
            StdDraw.setPenColor(StdDraw.BLACK);
            StdDraw.setPenRadius(.01);
            brute.draw();

            // draw in red the nearest neighbor (using brute-force algorithm)
            StdDraw.setPenRadius(.03);
            StdDraw.setPenColor(StdDraw.RED);
            brute.nearest(query).draw();
            StdDraw.setPenRadius(.02);

            // draw in blue the nearest neighbor (using kd-tree algorithm)
            StdDraw.setPenColor(StdDraw.BLUE);
            kdtree.nearest(query).draw();
            StdDraw.show(0);
            StdDraw.show(40);
        }
        
       
        
    }
}