import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.List;

public class JoinerTest {

    public static void main(String[] args) {

        List<String> list = Arrays.asList("A", "B", "C", "D");

        System.out.println(Joiner.on(", ").join(list));

    }

}
