import java.awt.*;

public class RedNote extends Note {
    public RedNote(long hitTime, boolean big, int x, Image image) {
        super(hitTime, x, image, big);
    }

    @Override
    public String getNoteKind() {
        if (big) {
            return "bigDon";
        } else {
            return "smallDon";
        }
    }

    @Override public boolean isDon() { return true; }


}
