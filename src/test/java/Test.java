import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Set;

public class Test {

    public static void main(String[] args) {
        Set<String> a = Sets.newHashSet("a", "b", "c");
        Set<String> b = Sets.newHashSet("c", "d", "e");

        Set<String> c = Sets.newHashSet();
        Iterables.addAll(c, a);
        Iterables.addAll(c, b);

        c.forEach(System.out::print);

    }

}
