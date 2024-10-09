
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<byte[]> bytes = new ArrayList<>();
        while(true){
            bytes.add(new byte[10240]);
        }
    }
}
