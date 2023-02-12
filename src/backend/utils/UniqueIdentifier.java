package backend.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniqueIdentifier {

    private static List<Integer> ids = new ArrayList<Integer>();
    private static int RANGE = 10000;

    private static int idx = 0;

    static {
        for (int i =0; i < RANGE; i++) {
            ids.add(i);
        }
        Collections.shuffle(ids);
    }

    public static int getIdentifier() {
        if(idx > ids.size()-1)
            idx = 0;

        return ids.get(idx++);
    }

}
