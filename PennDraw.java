/*************************************************************************
 *  Compilation:  javac PennDraw.java
 *  Execution:    java PennDraw
 *
 *  Standard drawing library. This class provides a basic capability for
 *  creating drawings with your programs. It uses a simple graphics model that
 *  allows you to create drawings consisting of points, lines, and curves
 *  in a window on your computer and to save the drawings to a file.
 *
 *  Todo
 *  ----
 *    -  Add support for gradient fill, stipple, etc.
 *    -  On some systems, drawing a line (or other shape) that extends way
 *       beyond canvas (e.g., to infinity) dimensions does not get drawn.
 *
 *  Remarks
 *  -------
 *    -  don't use AffineTransform for rescaling since it
 *       messes up images, strings, and penRadius
 *    -  careful using setFont in inner loop within an animation -
 *       it can cause flicker
 *
 *************************************************************************/

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 *  <i>Standard draw</i>. This class provides a basic capability for
 *  creating drawings with your programs. It uses a simple graphics model that
 *  allows you to create drawings consisting of points, lines, and curves
 *  in a window on your computer and to save the drawings to a file.
 *  <p>
 *  For additional documentation, see <a href="http://introcs.cs.princeton.edu/15inout">Section 1.5</a> of
 *  <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by Robert Sedgewick and Kevin Wayne.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 *  @author Benedict Brown
 */
public final class PennDraw implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    // pre-defined colors
    public static final Color BLACK      = Color.BLACK;
    public static final Color BLUE       = Color.BLUE;
    public static final Color CYAN       = Color.CYAN;
    public static final Color DARK_GRAY  = Color.DARK_GRAY;
    public static final Color GRAY       = Color.GRAY;
    public static final Color GREEN      = Color.GREEN;
    public static final Color LIGHT_GRAY = Color.LIGHT_GRAY;
    public static final Color MAGENTA    = Color.MAGENTA;
    public static final Color ORANGE     = Color.ORANGE;
    public static final Color PINK       = Color.PINK;
    public static final Color RED        = Color.RED;
    public static final Color WHITE      = Color.WHITE;
    public static final Color YELLOW     = Color.YELLOW;

    /**
     * Shade of blue used in Introduction to Programming in Java.
     * It is Pantone 300U. The RGB values are approximately (9, 90, 166).
     */
    public static final Color BOOK_BLUE       = new Color(  9,  90, 166);
    public static final Color BOOK_LIGHT_BLUE = new Color(103, 198, 243);

    /**
     * Shade of red used in Algorithms 4th edition.
     * It is Pantone 1805U. The RGB values are approximately (150, 35, 31).
     */
    public static final Color BOOK_RED = new Color(150, 35, 31);

    // default colors
    private static final Color DEFAULT_PEN_COLOR   = BLACK;
    private static final Color DEFAULT_CLEAR_COLOR = WHITE;

    // current pen color
    private static Color penColor;

    // default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
    private static final int DEFAULT_SIZE = 512;
    private static int width  = DEFAULT_SIZE;
    private static int height = DEFAULT_SIZE;

    // default pen radius
    private static final double DEFAULT_PEN_RADIUS = 0.002;

    // current pen radius
    private static double penRadius;

    // show we draw immediately or wait until next show?
    private static boolean defer = false;

    // time in milliseconds (from currentTimeMillis()) when we can draw again
    // used to control the frame rate
    private static long nextDraw = -1;

    // frame rate for animation mode in ms (60000 / fps)
    // 0 to draw as soon as advance() is called
    // -1 to disable animation mode
    private static int animationSpeed = -1;
    
    
    // boundary of drawing canvas, 5% border, scale factor to convert back to window coordinates
    private static final double BORDER = 0.05;
    private static final double DEFAULT_XMIN = 0.0;
    private static final double DEFAULT_XMAX = 1.0;
    private static final double DEFAULT_YMIN = 0.0;
    private static final double DEFAULT_YMAX = 1.0;
    private static double xmin, ymin, xmax, ymax;
    private static double xscale, yscale;

    // for synchronization
    private static Object mouseLock = new Object();
    private static Object keyLock = new Object();

    // default font
    private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);

    // current font
    private static Font font;

    // double buffered graphics
    private static BufferedImage offscreenImage, onscreenImage;
    private static Graphics2D offscreen, onscreen;

    // singleton for callbacks: avoids generation of extra .class files
    private static PennDraw std = new PennDraw();

    // the frame for drawing to the screen
    private static JFrame frame;

    // mouse state
    private static boolean mousePressed = false;
    private static double mouseX = 0;
    private static double mouseY = 0;

    // queue of typed key characters
    private static LinkedList<Character> keysTyped = new LinkedList<Character>();

    // set of key codes currently pressed down
    private static TreeSet<Integer> keysDown = new TreeSet<Integer>();
  

    // singleton pattern: client can't instantiate
    private PennDraw() { }


    // static initializer
    static { init(); }

    /**
     * Set the window size to the default size 512-by-512 pixels.
     * This method must be called before any other commands.
     */
    public static void setCanvasSize() {
        setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
    }

    /**
     * Set the window size to w-by-h pixels.
     * This method resets the x- and y-scales, fonts, colors, pen radius, etc., and clears the screen
     *
     * @param w the width as a number of pixels
     * @param h the height as a number of pixels
     * @throws a IllegalArgumentException if the width or height is 0 or negative
     */
    public static void setCanvasSize(int w, int h) {
        if (w < 1 || h < 1) throw new IllegalArgumentException("width and height must be positive");
        width = w;
        height = h;

        init();
    }

    // init
    private static void init() {
        // init() should only be called once at class initialization, and when the canvas is resized

        if (frame != null) frame.setVisible(false);
        frame = new JFrame();
        offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        onscreenImage  = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        offscreen = offscreenImage.createGraphics();
        onscreen  = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();

        // add antialiasing
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                                                  RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);

        // frame stuff
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);

        draw.addMouseListener(std);
        draw.addMouseMotionListener(std);

        frame.setContentPane(draw);
        frame.addKeyListener(std);    // JLabel cannot get keyboard focus
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Standard Draw");
        frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    // create the menu bar (changed to private)
    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem menuItem1 = new JMenuItem(" Save...   ");
        menuItem1.addActionListener(std);
        menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItem1);
        return menuBar;
    }


   /*************************************************************************
    *  User and screen coordinate systems
    *************************************************************************/

    /**
     * Set the x-scale to be the default (between 0.0 and 1.0).
     */
    public static void setXscale() { setXscale(DEFAULT_XMIN, DEFAULT_XMAX); }

    /**
     * Set the y-scale to be the default (between 0.0 and 1.0).
     */
    public static void setYscale() { setYscale(DEFAULT_YMIN, DEFAULT_YMAX); }

    /**
     * Set the x-scale (a 10% border is added to the values)
     * @param min the minimum value of the x-scale
     * @param max the maximum value of the x-scale
     */
    public static void setXscale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
            setTransform();
        }
    }

    /**
     * Set the y-scale (a 10% border is added to the values).
     * @param min the minimum value of the y-scale
     * @param max the maximum value of the y-scale
     */
    public static void setYscale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
            setTransform();
        }
    }

    /**
     * Set the x-scale and y-scale (a 10% border is added to the values)
     * @param min the minimum value of the x- and y-scales
     * @param max the maximum value of the x- and y-scales
     */
    public static void setScale(double min, double max) {
        double size = max - min;
        synchronized (mouseLock) {
            xmin = min - BORDER * size;
            xmax = max + BORDER * size;
            ymin = min - BORDER * size;
            ymax = max + BORDER * size;
            setTransform();
        }
    }

    // helper function that sets the canvas transform based on xmin, xmax, etc.
    private static void setTransform() {
        xscale = width  / (xmax - xmin);
        yscale = height / (ymax - ymin);
    }

    // helper functions that scale from user coordinates to screen coordinates and back
    private static double  scaleX(double x) { return xscale * (x - xmin); }
    private static double  scaleY(double y) { return yscale * (ymax - y); }
    private static double factorX(double w) { return w * width  / Math.abs(xmax - xmin);  }
    private static double factorY(double h) { return h * height / Math.abs(ymax - ymin);  }
    private static double   userX(double x) { return xmin + x / xscale; }
    private static double   userY(double y) { return ymax - y / yscale; }

    /**
     * Clear the screen to the default color (white).
     */
    public static void clear() { clear(DEFAULT_CLEAR_COLOR); }

    /**
     * Clear the screen to the given color.
     * @param color the Color to make the background
     */
    public static void clear(Color color) {
        offscreen.setColor(color);
        filledRectangle(0.5 * (xmax + xmin), 0.5 * (ymax + ymin),
                        0.5 * (xmax - xmin), 0.5 * (ymax - ymin));
        offscreen.setColor(penColor);
        draw();
    }

    /**
     * Clear the screen to the given color.
     * @param red the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue the amount of blue (between 0 and 255)
     * @throws IllegalArgumentException if the amount of red, green, or blue are outside prescribed range
     */
    public static void clear(int red, int green, int blue) {
        if (red   < 0 || red   >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue  < 0 || blue  >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        clear(new Color(red, green, blue));
    }

    /**
     * Clear the screen to the given color.
     * @param red the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue the amount of blue (between 0 and 255)
     * @param alpha the amount of alpha (between 0 and 255)
     * @throws IllegalArgumentException if the amount of red, green, or blue are outside prescribed range
     */
    public static void clear(int red, int green, int blue, int alpha) {
        if (red   < 0 || red   >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue  < 0 || blue  >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        if (alpha < 0 || alpha >= 256) throw new IllegalArgumentException("amount of alpha must be between 0 and 255");
        clear(new Color(red, green, blue, alpha));
    }

    /**
     * Get the current pen radius.
     */
    public static double getPenRadius() { return penRadius; }

    /**
     * Set the pen size to the default (.002).
     */
    public static void setPenRadius() { setPenRadius(DEFAULT_PEN_RADIUS); }

    /**
     * Set the radius of the pen to the given size.
     * @param r the radius of the pen
     * @throws IllegalArgumentException if r is negative
     */
    public static void setPenRadius(double r) {
        if (r < 0) throw new IllegalArgumentException("pen radius must be nonnegative");
        penRadius = r;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);
        BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
        // BasicStroke stroke = new BasicStroke(scaledPenRadius);
        offscreen.setStroke(stroke);
    }

    /**
     * Set the width of the pen to the given size based on the window width
     * @param w the width of the pen in pixels
     * @throws IllegalArgumentException if width is negative
     */
    public static void setPenWidthInPixels(double w) {
        if (w < 0) throw new IllegalArgumentException("pen radius must be nonnegative");

        setPenRadius(w / (2 * width));
    }

    /**
     * Set the width of the pen to the given size based on the window width and screen resolution
     * @param w the width of the pen in points (72 points/pinch)
     * @throws IllegalArgumentException if width is negative
     */
    public static void setPenWidthInPoints(double w) {
        if (w < 0) throw new IllegalArgumentException("pen radius must be nonnegative");

        int dpi = frame.getToolkit().getScreenResolution();
        setPenRadius(dpi * w / (144 * width));
    }

    /**
     * Get the current pen color.
     */
    public static Color getPenColor() { return penColor; }

    /**
     * Set the pen color to the default color (black).
     */
    public static void setPenColor() { setPenColor(DEFAULT_PEN_COLOR); }

    /**
     * Set the pen color to the given color. The available pen colors are
     * BLACK, BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA,
     * ORANGE, PINK, RED, WHITE, and YELLOW.
     * @param color the Color to make the pen
     */
    public static void setPenColor(Color color) {
        penColor = color;
        offscreen.setColor(penColor);
    }

    /**
     * Set the pen color to the given RGB color.
     * @param red the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue the amount of blue (between 0 and 255)
     * @throws IllegalArgumentException if the amount of red, green, or blue are outside prescribed range
     */
    public static void setPenColor(int red, int green, int blue) {
        if (red   < 0 || red   >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue  < 0 || blue  >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        setPenColor(new Color(red, green, blue));
    }

    /**
     * Set the pen color to the given RGBA color.
     * @param red the amount of red (between 0 and 255)
     * @param green the amount of green (between 0 and 255)
     * @param blue the amount of blue (between 0 and 255)
     * @param alpha the amount of alpha (between 0 and 255)
     * @throws IllegalArgumentException if the amount of red, green, blue, or alpha are outside prescribed range
     */
    public static void setPenColor(int red, int green, int blue, int alpha) {
        if (red   < 0 || red   >= 256) throw new IllegalArgumentException("amount of red must be between 0 and 255");
        if (green < 0 || green >= 256) throw new IllegalArgumentException("amount of green must be between 0 and 255");
        if (blue  < 0 || blue  >= 256) throw new IllegalArgumentException("amount of blue must be between 0 and 255");
        if (alpha < 0 || alpha >= 256) throw new IllegalArgumentException("amount of alpha must be between 0 and 255");
        setPenColor(new Color(red, green, blue, alpha));
    }

    /**
     * Get the current font.
     */
    public static Font getFont() { return font; }

    /**
     * List all available fonts
     * Code from http://alvinalexander.com/blog/post/jfc-swing/swing-faq-list-fonts-current-platform
     */
    public static void listFonts() {
        String fonts[] = 
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        for (String s : fonts)
            System.out.println(s);
    }

    /**
     * Set the font to the default font (sans serif, 16 point).
     */
    public static void setFont() { setFont(DEFAULT_FONT); }

    /**
     * Set the font to the given value.
     * @param f the font to make text
     */
    public static void setFont(Font f) {
        font = f;
        offscreen.setFont(f);
    }

    /**
     * Set the font to the given font, with the same style and point size as the current font.
     * @param fontName the font to make text
     */
    public static void setFont(String fontName) {
        setFont(new Font(fontName, font.getStyle(), font.getSize()));
    }

    /**
     * Set the font to the given font and size, with the same style as the current font.
     * @param fontName the font to make text
     * @param pointSize the font to make text
     */
    public static void setFont(String fontName, double pointSize) {
        setFont(fontName);        
        setFont(font.deriveFont((float) pointSize));
    }

    /**
     * Set the font to the specified point size
     * @param pointSize the desired font size
     */
    public static void setFontSize(double pointSize) {
        setFont(font.deriveFont((float) pointSize));
    }

    /**
     * Set the font to the specified height in pixels.  The conversion
     * is approximate due to limitations in Java.
     * @param pixelHeight the desired font size in pixels
     */
    public static void setFontSizeInPixels(double pixelHeight) {
        int dpi = frame.getToolkit().getScreenResolution();
        double pointSize = pixelHeight * dpi / 72;
        System.out.println(dpi);
        System.out.println(pointSize);
        setFont(font.deriveFont((float) pointSize));
    }

    /**
     * Set the font to a plain style (no bold or italic)
     */
    public static void setFontPlain() {
        setFont(font.deriveFont(Font.PLAIN));
    }

    /**
     * Set the font to a bold style (no italic)
     */
    public static void setFontBold() {
        setFont(font.deriveFont(Font.BOLD));
    }

    /**
     * Set the font to an italics style (no bold)
     */
    public static void setFontItalic() {
        setFont(font.deriveFont(Font.ITALIC));
    }

    /**
     * Set the font to a bold italic style
     */
    public static void setFontBoldItalic() {
        setFont(font.deriveFont(Font.BOLD | Font.ITALIC));
    }


   /*************************************************************************
    *  Drawing geometric shapes.
    *************************************************************************/

    /**
     * Draw a line from (x0, y0) to (x1, y1).
     * @param x0 the x-coordinate of the starting point
     * @param y0 the y-coordinate of the starting point
     * @param x1 the x-coordinate of the destination point
     * @param y1 the y-coordinate of the destination point
     */
    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    }

    /**
     * Draw one pixel at (x, y).
     * @param x the x-coordinate of the pixel
     * @param y the y-coordinate of the pixel
     */
    private static void pixel(double x, double y) {
        offscreen.fillRect((int) Math.round(scaleX(x)), (int) Math.round(scaleY(y)), 1, 1);
    }

    /**
     * Draw a point at (x, y).
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     */
    public static void point(double x, double y) {
        double r = penRadius;
        float scaledPenRadius = (float) (r * DEFAULT_SIZE);

        // double ws = factorX(2*r);
        // double hs = factorY(2*r);
        // if (ws <= 1 && hs <= 1) pixel(x, y);
        if (scaledPenRadius <= 1) pixel(x, y);
        else offscreen.fill(new Ellipse2D.Double(scaleX(x) - scaledPenRadius/2,
                                                 scaleY(y) - scaledPenRadius/2,
                                                 scaledPenRadius, scaledPenRadius));
        draw();
    }

    /**
     * Draw a circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    public static void circle(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("circle radius must be nonnegative");
        ellipse(x, y, r, r);
    }

    /**
     * Draw a filled circle of radius r, centered on (x, y).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    public static void filledCircle(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("circle radius must be nonnegative");
        filledEllipse(x, y, r, r);
    }

    /**
     * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws IllegalArgumentException if either of the axes are negative
     */
    public static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        ellipse(x, y, semiMajorAxis, semiMinorAxis, false);
    }

    /**
     * Draw an ellipse with given semimajor and semiminor axes, centered on (x, y).
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @param degrees counter-clockwise rotation by angle degrees around the center
     * @throws IllegalArgumentException if either of the axes are negative
     */
    public static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis, double degrees) {
        AffineTransform t = (AffineTransform) offscreen.getTransform();
        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        ellipse(x, y, semiMajorAxis, semiMinorAxis);
        offscreen.setTransform(t);
    }

    /**
     * Draw a filled ellipse with given semimajor and semiminor axes, centered on (x, y).
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @throws IllegalArgumentException if either of the axes are negative
     */
    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        ellipse(x, y, semiMajorAxis, semiMinorAxis, true);
    }


    /**
     * Draw a filled ellipse with given semimajor and semiminor axes, centered on (x, y),
     * @param x the x-coordinate of the center of the ellipse
     * @param y the y-coordinate of the center of the ellipse
     * @param semiMajorAxis is the semimajor axis of the ellipse
     * @param semiMinorAxis is the semiminor axis of the ellipse
     * @param degrees counter-clockwise rotation by angle degrees around the center
     * @throws IllegalArgumentException if either of the axes are negative
     */
    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis, double degrees) {
        AffineTransform t = (AffineTransform) offscreen.getTransform().clone();
        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        filledEllipse(x, y, semiMajorAxis, semiMinorAxis);
        offscreen.setTransform(t);
    }

    // helper function for drawing ellipses and filled ellipses
    private static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis, boolean filled) {
        if (semiMajorAxis < 0) throw new IllegalArgumentException("ellipse semimajor axis must be nonnegative");
        if (semiMinorAxis < 0) throw new IllegalArgumentException("ellipse semiminor axis must be nonnegative");
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * semiMajorAxis);
        double hs = factorY(2 * semiMinorAxis);
        if (ws <= 1 && hs <= 1) pixel(x, y);
        else if (filled)        offscreen.fill(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        else                    offscreen.draw(new Ellipse2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }

    /**
     * Draw an arc of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *        you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    public static void arc(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, Arc2D.OPEN, false);
    }

    /**
     * Draw a closed arc of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *        you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    public static void closedArc(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, Arc2D.CHORD, false);
    }

    /**
     * Draw a pie wedge of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *        you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    public static void pie(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, Arc2D.PIE, false);
    }

    /**
     * Draw a filled pie wedge of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *        you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    public static void filledPie(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, Arc2D.PIE, true);
    }

    /**
     * Draw a filled arc of radius r, centered on (x, y), from angle1 to angle2 (in degrees).
     * @param x the x-coordinate of the center of the circle
     * @param y the y-coordinate of the center of the circle
     * @param r the radius of the circle
     * @param angle1 the starting angle. 0 would mean an arc beginning at 3 o'clock.
     * @param angle2 the angle at the end of the arc. For example, if
     *        you want a 90 degree arc, then angle2 should be angle1 + 90.
     * @throws IllegalArgumentException if the radius of the circle is negative
     */
    public static void filledArc(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, Arc2D.CHORD, true);
    }

    // common code for all arc functions
    private static void arc(double x, double y, double r, double angle1, double angle2, int pathType, boolean fill) {
        if (r < 0) throw new IllegalArgumentException("arc radius must be nonnegative");
        while (angle2 < angle1) angle2 += 360;
        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2*r);
        double hs = factorY(2*r);
        if (ws <= 1 && hs <= 1) {
            pixel(x, y);
        } else {
            if (fill) offscreen.fill(new Arc2D.Double(xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, pathType));
            else      offscreen.draw(new Arc2D.Double(xs - ws/2, ys - hs/2, ws, hs, angle1, angle2 - angle1, pathType));
        }

        draw();
    }


    /**
     * Draw a square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws IllegalArgumentException if r is negative
     */
    public static void square(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("square side length must be nonnegative");
        rectangle(x, y, r, r);
    }

    /**
     * Draw a square of side length 2r, centered on (x, y) and rotated counter-clockwise.
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @param degrees counter-clockwise rotation by angle degrees around the center
     * @throws IllegalArgumentException if r is negative
     */
    public static void square(double x, double y, double r, double degrees) {
        if (r < 0) throw new IllegalArgumentException("square side length must be nonnegative");
        rectangle(x, y, r, r, degrees);
    }

    /**
     * Draw a filled square of side length 2r, centered on (x, y).
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @throws IllegalArgumentException if r is negative
     */
    public static void filledSquare(double x, double y, double r) {
        if (r < 0) throw new IllegalArgumentException("square side length must be nonnegative");
        filledRectangle(x, y, r, r);
    }

    /**
     * Draw a filled square of side length 2r, centered on (x, y) and rotated counter-clockwise.
     * @param x the x-coordinate of the center of the square
     * @param y the y-coordinate of the center of the square
     * @param r radius is half the length of any side of the square
     * @param degrees counter-clockwise rotation by angle degrees around the center
     * @throws IllegalArgumentException if r is negative
     */
    public static void filledSquare(double x, double y, double r, double degrees) {
        if (r < 0) throw new IllegalArgumentException("square side length must be nonnegative");
        filledRectangle(x, y, r, r, degrees);
    }

    /**
     * Draw a rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws IllegalArgumentException if halfWidth or halfHeight is negative
     */
    public static void rectangle(double x, double y, double halfWidth, double halfHeight) {
        rectangle(x, y, halfWidth, halfHeight, false);
    }

    /**
     * Draw a rectangle of given half width and half height, centered on (x, y) and rotated counter-clockwise.
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @param degrees counter-clockwise rotation by angle degrees around the center
     * @throws IllegalArgumentException if halfWidth or halfHeight is negative
     */
    public static void rectangle(double x, double y, double halfWidth, double halfHeight, double degrees) {
        AffineTransform t = (AffineTransform) offscreen.getTransform().clone();

        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        rectangle(x, y, halfWidth, halfHeight);
        offscreen.setTransform(t);
    }

    /**
     * Draw a filled rectangle of given half width and half height, centered on (x, y).
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @throws IllegalArgumentException if halfWidth or halfHeight is negative
     */
    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
        rectangle(x, y, halfWidth, halfHeight, true);
    }


    /**
     * Draw a rectangle of given half width and half height, centered on (x, y) and rotated counter-clockwise.
     * @param x the x-coordinate of the center of the rectangle
     * @param y the y-coordinate of the center of the rectangle
     * @param halfWidth is half the width of the rectangle
     * @param halfHeight is half the height of the rectangle
     * @param degrees counter-clockwise rotation by angle degrees around the center
     * @throws IllegalArgumentException if halfWidth or halfHeight is negative
     */
    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight, double degrees) {
        AffineTransform t = (AffineTransform) offscreen.getTransform();

        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        filledRectangle(x, y, halfWidth, halfHeight);
        offscreen.setTransform(t);
    }

    private static void rectangle(double x, double y, double halfWidth, double halfHeight, boolean filled) {
        if (halfWidth  < 0) throw new IllegalArgumentException("half width must be nonnegative");
        if (halfHeight < 0) throw new IllegalArgumentException("half height must be nonnegative");

        double xs = scaleX(x);
        double ys = scaleY(y);
        double ws = factorX(2 * halfWidth);
        double hs = factorY(2 * halfHeight);

        if (ws <= 1 && hs <= 1) pixel(x, y);
        else if (filled)        offscreen.fill(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        else                    offscreen.draw(new Rectangle2D.Double(xs - ws / 2, ys - hs / 2, ws, hs));
        draw();
    }

    /**
     * Draw a polyline with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    public static void polyline(double[] x, double[] y) {
        polygon(x, y, false, false);
    }

    /**
     * Draw a polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    public static void polygon(double[] x, double[] y) {
        polygon(x, y, true, false);
    }

    /**
     * Draw a filled polygon with the given (x[i], y[i]) coordinates.
     * @param x an array of all the x-coordindates of the polygon
     * @param y an array of all the y-coordindates of the polygon
     */
    public static void filledPolygon(double[] x, double[] y) {
        polygon(x, y, true, true);
    }

    private static void polygon(double[] x, double[] y, boolean close, boolean fill) {
        int N = x.length;

        if (y.length != N)
            throw new IllegalArgumentException("x[] and y[] must have the same number of elements.  " +
                                               "x[] has " + x.length + " elements, but y[] has " +
                                               y.length + " elements.");

        if ((close || fill) && N < 3)
            throw new IllegalArgumentException("You must specify at least three for a polygon.  " +
                                               "You have only provided " + N + " points.");
            
        Path2D.Double path = new Path2D.Double();
        path.moveTo(scaleX(x[0]), scaleY(y[0]));
        for (int i = 0; i < N; i++)
            path.lineTo(scaleX(x[i]), scaleY(y[i]));
        if (close || fill) path.closePath();
        if (fill) offscreen.fill(path);
        else      offscreen.draw(path);
        draw();
    }
    
    /**
     * Draw a polyline with the given coordinates.
     * @param coords an array of all the coordindates of the polygon (x1, y1, x2, y2, ...)
     * @throws IllegalArgumentException if the number of arguments is not even
     */
    public static void polyline(double ... coords) {
        polygon(false, false, coords);
    }

    /**
     * Draw a polygon with the given coordinates.
     * @param coords an array of all the coordindates of the polygon (x1, y1, x2, y2, ...)
     * @throws IllegalArgumentException if the number of arguments is not even and at least 6
     */
    public static void polygon(double ... coords) {
        polygon(true, false, coords);
    }

    /**
     * Draw a filled polygon with the given coordinates.
     * @param coords an array of all the coordindates of the polygon (x1, y1, x2, y2, ...)
     * @throws IllegalArgumentException if the number of arguments is not even and at least 6
     */
    public static void filledPolygon(double ... coords) {
        polygon(true, true, coords);
    }

    private static void polygon(boolean close, boolean fill, double ... coords) {
        int N = coords.length;

        if ((N % 2) != 0)
            throw new IllegalArgumentException("You must specify an even number of coordinates.  " +
                                               "You actually specified " + N + " coordinates.");

        // only need at least three vertices for closed/filled polygons
        if (close || fill)
            if (N < 6)
                throw new IllegalArgumentException("You must specify at least six coordinates (three points).  " +
                                                   "You only specified " + N + " coordinates.");

        Path2D.Double path = new Path2D.Double();
        path.moveTo(scaleX(coords[0]), scaleY(coords[1]));
        for (int i = 0; i < N; i += 2)
            path.lineTo(scaleX(coords[i]), scaleY(coords[i + 1]));

        if (close || fill) path.closePath();

        if (fill) offscreen.fill(path);
        else      offscreen.draw(path);

        draw();
    }

   /*************************************************************************
    *  Drawing images.
    *************************************************************************/

    // get an image from the given filename
    private static Image getImage(String filename) {

        // to read from file
        ImageIcon icon = new ImageIcon(filename);

        // try to read from URL
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            try {
                URL url = new URL(filename);
                icon = new ImageIcon(url);
            } catch (Exception e) { /* not a url */ }
        }

        // in case file is inside a .jar
        if ((icon == null) || (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
            URL url = PennDraw.class.getResource(filename);
            if (url == null) throw new IllegalArgumentException("image " + filename + " not found");
            icon = new ImageIcon(url);
        }

        return icon.getImage();
    }

    /**
     * Draw picture (gif, jpg, or png) centered on (x, y).
     * @param x the center x-coordinate of the image
     * @param y the center y-coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @throws IllegalArgumentException if the image is corrupt
     */
    public static void picture(double x, double y, String s) {
        picture(x, y, s, 0, 0, 0);
    }

    /**
     * Draw picture (gif, jpg, or png) centered on (x, y), rotated given number of degrees
     * @param x the center x-coordinate of the image
     * @param y the center y-coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @param degrees is the number of degrees to rotate counterclockwise
     * @throws IllegalArgumentException if the image is corrupt
     */
    public static void picture(double x, double y, String s, double degrees) {
        picture(x, y, s, 0, 0, degrees);
    }

    /**
     * Draw picture (gif, jpg, or png) centered on (x, y), rescaled to w-by-h.
     * @param x the center x coordinate of the image
     * @param y the center y coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @param w the width of the image
     * @param h the height of the image
     * @throws IllegalArgumentException if the width height are negative
     * @throws IllegalArgumentException if the image is corrupt
     */
    public static void picture(double x, double y, String s, double w, double h) {
        picture(x, y, s, w, h, 0);
    }


    /**
     * Draw picture (gif, jpg, or png) centered on (x, y), rotated
     * given number of degrees, rescaled to w-by-h.  If w and h are
     * both 0, use the original image dimensions.  If one is 0,
     * scale the image to match the other dimension.
     * @param x the center x-coordinate of the image
     * @param y the center y-coordinate of the image
     * @param s the name of the image/picture, e.g., "ball.gif"
     * @param w the width of the image (or zero to use the image width)
     * @param h the height of the image (or zero to use the image height)
     * @param degrees is the number of degrees to rotate counterclockwise
     * @throws IllegalArgumentException if the image is corrupt
     */
    public static void picture(double x, double y, String s, double w, double h, double degrees) {
        Image image = getImage(s);
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        if (iw <= 0 || ih <= 0) throw new IllegalArgumentException("image " + s + " is corrupt");

        AffineTransform t = (AffineTransform) offscreen.getTransform();

        double xs = xscale * (x - xmin);
        double ys = height - yscale * (y - ymin);

        if (degrees != 0) offscreen.rotate(-Math.toRadians(degrees), xs, ys);

        if (w == 0 && h == 0) // unscaled
            offscreen.drawImage(image, (int) Math.round(xs - 0.5 * iw), (int) Math.round(ys - 0.5 * ih), null);
        else {
            if (w == 0) w = (iw * h) / ih; // scale based on single dimension if only one is provided
            if (h == 0) h = (ih * w) / iw;

            offscreen.drawImage(image, (int) Math.round(xs - 0.5 * w), (int) Math.round(ys - 0.5 * h),
                                (int) Math.round(w), (int) Math.round(h), null);
        }

        if (degrees != 0) offscreen.setTransform(t);
        draw();
    }


   /*************************************************************************
    *  Drawing text.
    *************************************************************************/

    /**
     * Write the given text string in the current font, centered on (x, y).
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     */
    public static void text(double x, double y, String s) {
        text(x, y, s, 0);
    }

    /**
     * Write the given text string in the current font, centered on (x, y) and
     * rotated by the specified number of degrees  
     * @param x the center x-coordinate of the text
     * @param y the center y-coordinate of the text
     * @param s the text
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    public static void text(double x, double y, String s, double degrees) {
        text(x, y, s, degrees, -0.5);
    }

    /**
     * Write the given text string in the current font, left-aligned at (x, y).
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    public static void textLeft(double x, double y, String s) {
        textLeft(x, y, s, 0);
    }

    /**
     * Write the given text string in the current font, left-aligned at (x, y) and
     * rotated by the specified number of degrees.
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    public static void textLeft(double x, double y, String s, double degrees) {
        text(x, y, s, degrees, 0);
    }

    /**
     * Write the given text string in the current font, right-aligned at (x, y).
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     */
    public static void textRight(double x, double y, String s) {
        textRight(x, y, s, 0);
    }

    /**
     * Write the given text string in the current font, right-aligned at (x, y) and
     * rotated by the specified number of degrees.
     * @param x the x-coordinate of the text
     * @param y the y-coordinate of the text
     * @param s the text
     * @param degrees is the number of degrees to rotate counterclockwise
     */
    public static void textRight(double x, double y, String s, double degrees) {
        text(x, y, s, degrees, -1);
    }


    // dw controls the shift so this function can draw left, center, or right aligned text
    private static void text(double x, double y, String s, double degrees, double dw) {
        AffineTransform t = (AffineTransform) offscreen.getTransform();

        FontMetrics metrics = offscreen.getFontMetrics();
        int w = metrics.stringWidth(s);
        int h = metrics.getDescent();
        
        double xs = scaleX(x);
        double ys = scaleY(y);

        if (degrees != 0) offscreen.rotate(-Math.toRadians(degrees), xs, ys);

        offscreen.drawString(s, (float) (xs + dw * w), (float) (ys + h));

        if (degrees != 0) offscreen.setTransform(t);

        draw();
    }

    /**
     * Display on screen, pause for t milliseconds, and turn on
     * <em>animation mode</em>: subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will not be displayed on screen until the next call to <tt>show()</tt>.
     * This is useful for producing animations (clear the screen, draw a bunch of shapes,
     * display on screen for a fixed amount of time, and repeat). It also speeds up
     * drawing a huge number of shapes (call <tt>show(0)</tt> to defer drawing
     * on screen, draw the shapes, and call <tt>show(0)</tt> to display them all
     * on screen at once).
     * @param t number of milliseconds
     */
    public static void show(int t) {
        // sleep until the next time we're allowed to draw
        long millis = System.currentTimeMillis();
        if (millis < nextDraw) {
            try { Thread.sleep(nextDraw - millis); }
            catch (InterruptedException e) { System.out.println("Error sleeping"); }
            millis = nextDraw;
        }

        defer = false;
        draw();
        defer = true;

        nextDraw = millis + t;
    }

    /**
     * Display on-screen and turn off animation mode:
     * subsequent calls to
     * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt>
     * will be displayed on screen when called. This is the default.
     */
    public static void show() {
        defer = false;
        draw();
    }

    // draw onscreen if defer is false
    private static void draw() {
        if (defer) return;
        onscreen.drawImage(offscreenImage, 0, 0, null);
        frame.repaint();
    }

    /**
     * Draw everything immediately (equivalent to calling PennDraw.show() with no arguments)
     */
    public static void disableAnimation() {
        animationSpeed = -1; // disable animation mode
        show();
    }

    /**
     * Set animation mode with specified frame rate
     * Equivalent to calling PennDraw.show(0), with
     * subsequent calls to PennDraw.advance() being
     * equivalent to calling PennDraw.show(10000.0 / frameRate).
     *
     * Use PennDraw.enableAnimation(0) to have PennDraw.advance()
     * draw as fast as possible (just like PennDraw.show(0)).
     * @param frameRate animation speed in frames per second
     * @throws IllegalArgumentException if frameRate is negative
     */
    public static void enableAnimation(double frameRate) {
        if (frameRate < 0) throw new IllegalArgumentException("frameRate must be >= 0");
        animationSpeed = frameRate == 0 ? 0 : (int) Math.round(1000.0 / frameRate); // save frame rate in ms
        show(0);                                                               // and switch to animation mode
    }

    /**
     * Indicate that all drawing commands for the next frame are complete
     * so it can be drawn
     *
     * @throws RuntimeException is animation mode has not been enabled
     */
    public static void advance() {
        if (animationSpeed < 0)
            throw new RuntimeException("You must call PennDraw.enableAnimation() to activate animation mode before calling PennDraw.advance()");

        show(animationSpeed);
    }
    
   /*************************************************************************
    *  Save drawing to a file.
    *************************************************************************/

    /**
     * Save onscreen image to file - suffix must be png, jpg, or gif.
     * @param filename the name of the file with one of the required suffixes
     */
    public static void save(String filename) {
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);

        // png files
        if (suffix.toLowerCase().equals("png")) {
            try { ImageIO.write(onscreenImage, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }

        // need to change from ARGB to RGB for jpeg
        // reference: http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
        else if (suffix.toLowerCase().equals("jpg")) {
            WritableRaster raster = onscreenImage.getRaster();
            WritableRaster newRaster;
            newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[] {0, 1, 2});
            DirectColorModel cm = (DirectColorModel) onscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
                                                          cm.getRedMask(),
                                                          cm.getGreenMask(),
                                                          cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false,  null);
            try { ImageIO.write(rgbBuffer, suffix, file); }
            catch (IOException e) { e.printStackTrace(); }
        }

        else {
            System.out.println("Invalid image file type: " + suffix);
        }
    }


    /**
     * This method cannot be called directly.
     */
    public void actionPerformed(ActionEvent e) {
        FileDialog chooser = new FileDialog(PennDraw.frame, "Use a .png or .jpg extension", FileDialog.SAVE);
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            PennDraw.save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }


   /*************************************************************************
    *  Mouse interactions.
    *************************************************************************/

    /**
     * Is the mouse being pressed?
     * @return true or false
     */
    public static boolean mousePressed() {
        synchronized (mouseLock) {
            return mousePressed;
        }
    }

    /**
     * What is the x-coordinate of the mouse?
     * @return the value of the x-coordinate of the mouse
     */
    public static double mouseX() {
        synchronized (mouseLock) {
            return mouseX;
        }
    }

    /**
     * What is the y-coordinate of the mouse?
     * @return the value of the y-coordinate of the mouse
     */
    public static double mouseY() {
        synchronized (mouseLock) {
            return mouseY;
        }
    }


    /**
     * This method cannot be called directly.
     */
    public void mouseClicked(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    public void mouseEntered(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    public void mouseExited(MouseEvent e) { }

    /**
     * This method cannot be called directly.
     */
    public void mousePressed(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = userX(e.getX());
            mouseY = userY(e.getY());
            mousePressed = true;
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void mouseReleased(MouseEvent e) {
        synchronized (mouseLock) {
            mousePressed = false;
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void mouseDragged(MouseEvent e)  {
        synchronized (mouseLock) {
            mouseX = userX(e.getX());
            mouseY = userY(e.getY());
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void mouseMoved(MouseEvent e) {
        synchronized (mouseLock) {
            mouseX = userX(e.getX());
            mouseY = userY(e.getY());
        }
    }


   /*************************************************************************
    *  Keyboard interactions.
    *************************************************************************/

    /**
     * Has the user typed a key?
     * @return true if the user has typed a key, false otherwise
     */
    public static boolean hasNextKeyTyped() {
        synchronized (keyLock) {
            return !keysTyped.isEmpty();
        }
    }

    /**
     * What is the next key that was typed by the user? This method returns
     * a Unicode character corresponding to the key typed (such as 'a' or 'A').
     * It cannot identify action keys (such as F1
     * and arrow keys) or modifier keys (such as control).
     * @return the next Unicode key typed
     */
    public static char nextKeyTyped() {
        synchronized (keyLock) {
            return keysTyped.removeLast();
        }
    }

    /**
     * Is the keycode currently being pressed? This method takes as an argument
     * the keycode (corresponding to a physical key). It can handle action keys
     * (such as F1 and arrow keys) and modifier keys (such as shift and control).
     * See <a href = "http://download.oracle.com/javase/6/docs/api/java/awt/event/KeyEvent.html">KeyEvent.java</a>
     * for a description of key codes.
     * @return true if keycode is currently being pressed, false otherwise
     */
    public static boolean isKeyPressed(int keycode) {
        synchronized (keyLock) {
            return keysDown.contains(keycode);
        }
    }


    /**
     * This method cannot be called directly.
     */
    public void keyTyped(KeyEvent e) {
        synchronized (keyLock) {
            keysTyped.addFirst(e.getKeyChar());
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void keyPressed(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.add(e.getKeyCode());
        }
    }

    /**
     * This method cannot be called directly.
     */
    public void keyReleased(KeyEvent e) {
        synchronized (keyLock) {
            keysDown.remove(e.getKeyCode());
        }
    }

    /**
     * Test client.
     */
    public static void main(String[] args) {
        PennDraw.square(.2, .8, .1);
        PennDraw.setPenWidthInPoints(12);
        PennDraw.rectangle(.2, .8, .1, .2, 10);
        PennDraw.setPenRadius();
        PennDraw.filledRectangle(.8, .8, .2, .1, 10);
        PennDraw.circle(.8, .2, .2);
        PennDraw.filledEllipse(.8, .2, .2, .1, 10);

        PennDraw.setPenColor(PennDraw.BOOK_RED);
        PennDraw.setPenRadius(.02);
        PennDraw.arc(.8, .2, .1, 200, 45);

        // draw a blue diamond
        PennDraw.setPenRadius();
        PennDraw.setPenColor(PennDraw.BOOK_BLUE);
        double[] x = { .1, .2, .3, .2 };
        double[] y = { .2, .3, .2, .1 };
        PennDraw.polyline(x, y);
        PennDraw.filledPolygon(.1, .2, .2, .3, .3, .2);

        // text
        PennDraw.setFontSize(12);
        PennDraw.setPenColor(PennDraw.BLACK);
        PennDraw.text(0.2, 0.5, "black text");
        PennDraw.text(0.2, 0.5, "black text", 30);
        PennDraw.setFont("Serif");
        PennDraw.setPenColor(PennDraw.WHITE);
        PennDraw.text(0.8, 0.8, "white serif text");
    }

}
