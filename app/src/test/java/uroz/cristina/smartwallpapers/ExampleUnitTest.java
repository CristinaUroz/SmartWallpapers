package uroz.cristina.smartwallpapers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void myMainActivityTest(){
      MainActivity mainActivity = new MainActivity();

      LinkedList<Integer> l = new LinkedList<>(Arrays.asList(3, 5, 4));

      LinkedList<Integer> result = new LinkedList<>();

      for (Integer i : l){
        System.out.println(i);
      }

    }
}