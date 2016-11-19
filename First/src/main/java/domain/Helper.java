package domain;

import java.io.*;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class Helper {
    private List<String> names = new ArrayList<>();

    private static Helper instance;
    private SecureRandom random;

    private Helper(){
        random = new SecureRandom();
    }

    public static Helper getHelper(){
        if(instance == null){
            instance = new Helper();
            try {
                instance.readNames();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return instance;
    }

    private void readNames() throws IOException {
        String line;
        ClassLoader classLoader = getClass().getClassLoader();
        File fi = new File(classLoader.getResource("names.txt").getFile());
        try (
                InputStream fis = new FileInputStream(fi);
                InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
        ) {
            while ((line = br.readLine()) != null) {
                names.add(line);
            }
        }
    }

    /*
    public String getInterestType() {
        return Interest.Cabbage;
    }
    */

    private  String choose(File f) throws FileNotFoundException
    {
        String result = null;
        Random rand = new Random();
        int n = 0;
        for(Scanner sc = new Scanner(f); sc.hasNext(); ) {
            ++n;
            String line = sc.nextLine();
            if(rand.nextInt(n) == 0)
                result = line;
        }

        return result;
    }

    public String getRandomName() {
        if (names == null || names.size() == 0) return "Victor";
        return names.get(random.nextInt(names.size())).trim();
    }

    public String getRandomInterest() {
        String name = "Could not find file";
        try {
            String s = choose(new File("interests.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }
}
