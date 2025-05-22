import java.awt.*;

public class BlueNote extends Note {
    public BlueNote(long hitTime, boolean big, int x, Image image) {
        super(hitTime, x, image, big);
    }
        public String getNoteKind() {
            if (big) {
                return "bigKa";
            } else {
                return "smallKa";
            }
        }

    public boolean isKa() { return true; }
    }
