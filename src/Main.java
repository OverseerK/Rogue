import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class Main {

    static Random rand = new Random();

    static boolean game = false;
    static int mapY = 20;
    static int mapX = 80;
    static char[][] base = new char[mapY][mapX];
    static Entity[][] entity = new Entity[mapY][mapX];
    static ArrayList<ArrayList<ArrayList<Item>>> item = new ArrayList<>();

    static int dl;
    static int xl;
    static int hp;
    static int maxhp;
    static int pw;
    static int maxpw;
    static int time;

    static String role;
    static String name = null;
    static Entity pl;
    static ArrayList<Item> inv = new ArrayList<>();

    static StringBuilder log = new StringBuilder();

    private static void refLog() {
        int size = 0;
        for (int i = 0; i < log.length(); i++) {
            if (log.charAt(i) == '\n') {
                size++;
            }
        }

        if (size > 3) {
            int index = log.indexOf("\n") + 1;
            log.delete(0, index);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        for (int i = 0; i < mapY; i++) {
            ArrayList<ArrayList<Item>> row = new ArrayList<>();
            for (int j = 0; j < mapX; j++) {
                row.add(new ArrayList<>());
            }
            item.add(row);
        }
        BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
        while (name == null) {
            System.out.println("Your name?");
            name = bf.readLine();
        }
        while (!game) {
            System.out.println("""
                    Your role?
                    a) Monk
                    b) Ranger
                    c) Valkryie
                    d) Wizard""");
            preset(bf.readLine());
        }
        mapGen();
        while (game) {
            display();
            refLog();
            System.out.print(log);

            //debug
//            StringBuilder s = new StringBuilder();
//            for (Entity e : entity[2]) {
//                if (e == null) {
//                    s.append(0).append(" ");
//                } else {
//                    s.append(e.toString()).append(" ");
//                }
//            }
//            System.out.println(s);

            String cmd = bf.readLine();
            switch (cmd) {
                case "Q" -> System.exit(0);
                case "w" -> move(pl, 0, -1);
                case "a" -> move(pl, -1, 0);
                case "s" -> move(pl, 0, 1);
                case "d" -> move(pl, 1, 0);
                case "<" -> ascend();
                case "," -> pickup();
            }
        }

    }

    private static void updateStack(int x, int y, ArrayList<Item> items) {
        ArrayList<ArrayList<Item>> row = item.get(y);
        row.set(x, items);
        item.set(y, row);
    }

    private static void pickup() {
        int plx = pl.getX();
        int ply = pl.getY();
        ArrayList<Item> items = item.get(ply).get(plx);
        if (items.size() > 0) {
            Item i = items.get(0);
            inv.add(i);
            items.remove(0);
            updateStack(plx, ply, items);
            log.append("You picked up ").append(itemName(i)).append(".").append("\n");
        } else {
            log.append("There is nothing here.").append("\n");
        }
    }

    private static void ascend() {
        int plx = pl.getX();
        int ply = pl.getY();
        if (base[ply][plx] == '<') {
            System.out.println("You escaped from the dungeon.");
            System.exit(0);
        } else {
            log.append("You can't go up here.").append("\n");
        }
    }

    private static String itemName(Item i) {
        return i.getValue() + " " + i.getName() + (i.getValue() > 1 ? "s" : "");
    }

    private static void move(Entity en, int dx, int dy) {
        int x = en.getX();
        int y = en.getY();
        if (base[y + dy][x + dx] != '#' && entity[y + dy][x + dx] == null) {
            entity[y][x] = null;
            x += dx;
            y += dy;
            en.setX(x);
            en.setY(y);
            char floor = base[y][x];
            entity[y][x] = en;
            ArrayList<Item> items = item.get(y).get(x);

            //log output
            if (en == pl) {
                if (floor != '.' && floor != '\0') {
                    log.append("You are standing in ").append(floorDes(floor)).append(".\n");
                }
                if (items.size() > 1) {
                    log.append("You see here several items.").append("\n");
                } else if (items.size() > 0) {
                    Item i = items.get(0);
                    log.append("You see here ").append(itemName(i)).append(".").append("\n");
                }
            }
        }
    }

    private static void mapGen() {
        for (int y = 0; y < mapY; y++) {
            for (int x = 0; x < mapX; x++) {
                boolean xborder = (y == 1 || y == mapY - 2) && (x != 0 && x != mapX - 1);
                boolean yborder = (x == 1 || x == mapX - 2) && (y != 0 && y != mapY - 1);
                boolean floor = y > 1 && y < mapY - 2 && x > 1 && x < mapX - 2;

                //dungeon
                boolean up = x == 2 && y == 2;
                boolean down = x == mapX - 3 && y == mapY - 3;
                if (xborder || yborder) {
                    base[y][x] = '#';
                } else if (up) {
                    base[y][x] = '<';
                } else if (down) {
                    base[y][x] = '>';
                } else if (floor) {
                    base[y][x] = '.';
                }

                //item
                if (floor && rand.nextInt(100) == 0) {
                    ArrayList<Item> items = new ArrayList<>();
                    items.add(new Item('%', "food ration", 1));
                    item.get(y).set(x, items);
                }

                //entity
            }
        }
        pl = new Entity('@', name, 2, 2, hp);
        entity[2][2] = pl;
    }

    private static void display() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        StringBuilder display = new StringBuilder();
        display.append(name).append(" the ").append(role).append(" XL: ").append(xl).append(" HP: ").append(hp).append("/").append(maxhp).append(" Pw: ").append(pw).append("/").append(maxpw).append("\n");
        for (int y = 0; y < mapY; y++) {
            for (int x = 0; x < mapX; x++) {
                char floor = base[y][x];
                ArrayList<Item> it = item.get(y).get(x);
                Entity en = entity[y][x];
                String s = " ";
                if (floor != '\0') {
                    s = Character.toString(floor);
                }
                if (it.size() > 0) {
                    s = Character.toString(it.get(0).getType());
                }
                if (en != null) {
                    s = Character.toString(en.getType());
                }
                display.append(s);
            }
            display.append("\n");
        }
        System.out.print(display);
    }

    private static void preset(String s) {
        switch (s) {
            case "a" -> {
                role = "Monk";
                maxhp = 20;
                maxpw = 10;
            }
            case "b" -> {
                role = "Ranger";
                maxhp = 15;
                maxpw = 5;
            }
            case "c" -> {
                role = "Valkriye";
                maxhp = 20;
                maxpw = 5;
            }
            case "d" -> {
                role = "Wizard";
                maxhp = 15;
                maxpw = 15;
            }
            default -> {
                return;
            }
        }
        xl = 1;
        hp = maxhp;
        pw = maxpw;
        game = true;
    }

    static String floorDes(char c) {
        return switch (c) {
            case '<' -> "staircase leading upward";
            case '>' -> "staircase leading downward";
            default -> "nothing";
        };
    }

}

class Item {
    private final char type;
    private String name;
    private int value;

    public Item(char type, String name, int value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    public char getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(int value) {
        this.value = value;
    }
}

class Entity {

    private final char type;
    private final String name;
    private int x;
    private int y;
    private int health;

    public Entity(char type, String name, int x, int y, int health) {
        this.type = type;
        this.name = name;
        this.x = x;
        this.y = y;
        this.health = health;
    }

    public char getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public int getHealth() {
        return this.health;
    }

    public String getAttack() {
        return "1d4hit";
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String toString() {
        return type + " (" + name + ")" + "(" + getX() + ", " + getY() + ")";
    }

}