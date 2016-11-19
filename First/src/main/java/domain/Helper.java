package domain;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by victoraxelsson on 2016-11-19.
 */
public class Helper {

    private static Helper instance;
    Random rand;

    private Helper(){
        rand = new Random();
    }

    public static Helper getHelper(){
        if(instance == null){
            instance = new Helper();
        }

        return instance;
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
        String name = "Could not find file";
        try {
            String s = choose(new File("names.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }

    public Interest getRandomInterestEnum() {
        return Interest.getRandom();
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
