import java.util.Optional;

public class Test {

    public static void main(String[] args) {
        Optional<String> opt = Optional.empty();

        if (opt.isEmpty()){
            System.out.println("No value");
        }
    }

}
