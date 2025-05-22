import javax.swing.*;
import java.awt.*;

public class Note {
    public long hitTime;
    public int x;
    public boolean leftHit = false;
    public boolean rightHit = false;
    public long leftError = Long.MAX_VALUE;
    public long rightError = Long.MAX_VALUE;

//default images, overriden in subclasses
    public Image image;
    public boolean big;


    public Note(long hitTime, int x, Image image, boolean big) {
        this.hitTime = hitTime;
        this.x = x;
        this.image = image;
        this.big = big;
    }

// simple getters
    public boolean isDon() { return false; }
    public boolean isKa () { return false; }

    // draws the image in certain place
    public void draw(Graphics g, JPanel panel) {
        int size;
        if (big) {
            size = (int) (100 * 1.25);
        } else {
            size = 100;
        }
        g.drawImage(image, x, 150, x + size, 150 + size,
                0, 0, image.getWidth(null), image.getHeight(null), panel);
    }

    public String getNoteKind() {
        return "base"; // probably never gonna be called unless im drawing an unknown name, but ill throw an error in that case
    }


    public boolean isBig() {
        return big;
    }
}
