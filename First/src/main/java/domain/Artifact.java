package domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 11/10/2016.
 */
public class Artifact {

    private static List<Artifact> collection = new ArrayList<>();

    private Interest type;
    private String name;
    private String author;
    private String date;

    public Artifact(Interest type, String name, String author, String date) {
        this.type = type;
        this.name = name;
        this.author = author;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

    public static List<Artifact> getCollection() {
        if (collection.isEmpty()) {
            Artifact painting1 = new Artifact(Interest.Paintings, "Mono Lisp",
                    "Leonardo Da No", "17 Oct 1519");

            Artifact painting2 = new Artifact(Interest.Paintings, "Screamer",
                    "Monk", "27 Feb 1893");

            Artifact cabbage = new Artifact(Interest.Cabbage, "The Great Cabbage",
                    "Not the Brightest Server", "2 Nov 2016");

            Artifact chocolate = new Artifact(Interest.Chocolate, "Choco Oompa Loompa",
                    "Charlie", "Unknown");

            Artifact flowers = new Artifact(Interest.Flowers, "Tulips",
                    "Two Gogh", "Unknown");

            collection.add(painting1);
            collection.add(painting2);
            collection.add(cabbage);
            collection.add(chocolate);
            collection.add(flowers);
        }
        return collection;
    }

    public Interest getType() {
        return type;
    }

    public void setType(Interest type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
