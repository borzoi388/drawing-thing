import java.awt.*;
import java.util.Map;

public interface Action {
    Action performAction(Canvas canvas);
    String toString();
}


