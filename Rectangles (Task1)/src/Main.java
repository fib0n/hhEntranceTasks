//used java SDK 8

import java.awt.Point;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        Solver solver = new Solver("input.txt", "output.txt");
        solver.Do();
    }
}

class Solver {

    String inputFileName;
    String outputFileName;

    public Solver(String inputFileName, String outputFileName) {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
    }

    private Stream<Rectangle> getData() throws IOException {
        return Files.readAllLines(Paths.get(this.inputFileName), StandardCharsets.UTF_8)
                .stream()
                .map(line -> {
                    StringTokenizer st = new StringTokenizer(line);
                    return new Rectangle(nextPoint(st), nextPoint(st));
                });
    }

    public void Do() throws IOException {
        //http://en.wikipedia.org/wiki/Sweep_line_algorithm
        //https://www.topcoder.com/tc?module=Static&d1=tutorials&d2=lineSweep
        PriorityQueue<Event> queue = new PriorityQueue<>();
        SortedSet<Integer> ordinates = new TreeSet<>();

        getData().forEach(rectangle -> {
            queue.add(new Event(rectangle.leftBottom().x, true, rectangle));
            queue.add(new Event(rectangle.rightTop().x, false, rectangle));
            ordinates.add(rectangle.leftBottom().y);
            ordinates.add(rectangle.rightTop().y);
        });

        //сжатие координат прямоугольников для оптимального построения дерева
        //интервалов в след шаге
        HashMap<Integer, Integer> map = new HashMap();
        int[] primitives = new int[ordinates.size()];
        int i = 0;
        for (int e : ordinates) {
            map.put(e, i);
            primitives[i++] = e;
        }

        //создание модифицированного дерева интервалов для нахождения общей
        //длины пересечения сканирующей линии и прямоугольников за log(n)
        IntervalTree tree = new IntervalTree(primitives);
        long area = 0;
        Event event;
        int xPrev = 0;
        while ((event = queue.poll()) != null) {
            area += (event.key() - xPrev) * tree.getUnionLength();

            tree.add(map.get(event.rectangle().leftBottom().y),
                    map.get(event.rectangle().rightTop().y) - 1,
                    event.type() ? 1 : -1);

            xPrev = event.key();
        }
        writeAnswer(area);
    }

    private Point nextPoint(StringTokenizer st) {
        return new Point(nextInt(st), nextInt(st));
    }

    private int nextInt(StringTokenizer st) {
        return Integer.parseInt(st.nextToken());
    }

    private void writeAnswer(long area) throws IOException {
        List<String> data = Arrays.asList(Long.toString(area));
        Files.write(Paths.get(this.outputFileName), data, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    private class Event implements Comparable<Event> {

        private final int key;
        private final boolean type;
        private final Rectangle rectangle;

        public Event(int key, boolean type, Rectangle rectangle) {
            this.key = key;
            this.rectangle = rectangle;
            this.type = type;
        }

        public int key() {
            return this.key;
        }

        public boolean type() {
            return this.type;
        }

        public Rectangle rectangle() {
            return this.rectangle;
        }

        @Override
        public int compareTo(Event o) {
            return (int) Math.signum(this.key - o.key);
        }
    }

    private class Rectangle {

        private final Point leftBottom;
        private final Point rightTop;

        public Rectangle(Point leftBottom, Point rightTop) {
            this.leftBottom = leftBottom;
            this.rightTop = rightTop;
        }

        public Point leftBottom() {
            return leftBottom;
        }

        public Point rightTop() {
            return rightTop;
        }
    }

    private class IntervalTree {

        private final Node root;

        private class Node {

            private final int l;
            private final int r;
            private final int actualLength;
            private Node left;
            private Node right;
            private int covered;
            private int filledLength;

            private Node(int l, int r, int actualLength) {
                this.l = l;
                this.r = r;
                this.actualLength = actualLength;
            }
        }

        public IntervalTree(int[] a) {
            this.root = init(0, a.length - 2, a);
        }

        public int getUnionLength() {
            return this.root.filledLength;
        }

        public void add(int l, int r, int delta) {
            add(this.root, l, r, delta);
        }

        private Node init(int l, int r, int[] a) {
            int actualLength = a[r + 1] - a[l];
            if (l == r) {
                return new Node(l, r, actualLength);
            }
            Node node = new Node(l, r, actualLength);
            int middle = (l + r) / 2;
            node.left = init(l, middle, a);
            node.right = init(middle + 1, r, a);

            return node;
        }

        private void add(Node node, int l, int r, int delta) {
            if (l > r) {
                return;
            }
            if (node.l == l && node.r == r) {
                node.covered += delta;
            } else {
                add(node.left, l, Math.min(r, node.left.r), delta);
                add(node.right, Math.max(l, node.right.l), r, delta);
            }
            if (node.covered > 0) {
                node.filledLength = node.actualLength;
            } else {
                node.filledLength = node.left == null ? 0 : node.left.filledLength + node.right.filledLength;
            }
        }
    }
}
