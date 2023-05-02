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
    static ArrayList<Entity> entities = new ArrayList<>();
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
    static boolean showInv = false;

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
            if (showInv) {
                inventory();
                bf.readLine();
            } else {
                display();
                String[] cmd = bf.readLine().split(" ");
                switch (cmd[0]) {
                    case "." -> turn();
                    case "h" -> move(pl, -1, 0);
                    case "l" -> move(pl, 1, 0);
                    case "j" -> move(pl, 0, -1);
                    case "k" -> move(pl, 0, 1);
                    case "y" -> move(pl, -1, -1);
                    case "u" -> move(pl, 1, -1);
                    case "b" -> move(pl, -1, 1);
                    case "n" -> move(pl, 1, 1);
                    case "<" -> ascend();
                    case "," -> pickup();
                    case "i" -> showInv = true;
                    case "Q" -> System.exit(0);
                }
            }
        }

    }

    private static void updateStack(int x, int y, ArrayList<Item> items) {
        ArrayList<ArrayList<Item>> row = item.get(y);
        row.set(x, items);
        item.set(y, row);
    }

    private static void pickup() throws IOException, InterruptedException {
        int plx = pl.getX();
        int ply = pl.getY();
        ArrayList<Item> items = item.get(ply).get(plx);
        if (items.size() > 0) {
            Item i = items.get(0);
            inv.add(i);
            items.remove(0);
            updateStack(plx, ply, items);
            log.append("You picked up ").append(itemName(i)).append(".").append("\n");
            turn();
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

    private static void move(Entity en, int dx, int dy) throws IOException, InterruptedException {
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
                turn();
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

    private static void turn() throws IOException, InterruptedException {
        for (Entity e : entities) {
            if (e != pl) {
                if (adjacent(e, pl)) {
                    attack(e, pl);
                } else {
                    switch (rand.nextInt(8)) {
                        case 0 -> move(e, -1, 0);
                        case 1 -> move(e, 1, 0);
                        case 2 -> move(e, 0, -1);
                        case 3 -> move(e, 0, 1);
                        case 4 -> move(e, -1, -1);
                        case 5 -> move(e, 1, -1);
                        case 6 -> move(e, -1, 1);
                        case 7 -> move(e, 1, 1);
                    }
                }
            }
        }
        time++;
    }

    private static void mapGen() {
        for (int y = 0; y < mapY; y++) {
            for (int x = 0; x < mapX; x++) {

                //old mapgen
                boolean xborder = (y == 1 || y == mapY - 2) && (x != 0 && x != mapX - 1);
                boolean yborder = (x == 1 || x == mapX - 2) && (y != 0 && y != mapY - 1);
                boolean floor = y > 1 && y < mapY - 2 && x > 1 && x < mapX - 2;
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
                if (base[y][x] == '.' && rand.nextInt(80) == 0) {
                    ArrayList<Item> items = new ArrayList<>();
                    items.add(switch (rand.nextInt(6)) {
                        case 0 -> new Item('%', "food ration", 1);
                        case 1 -> new Item('$', "gold piece", 25);
                        case 2 -> new Item(')', "dagger", 1);
                        case 3 -> new Item('[', "leather armor", 1);
                        case 4 -> new Item('?', "scroll of upgrade", 1);
                        case 5 -> new Item('!', "potion of healing", 1);
                        default -> null;
                    });
                    item.get(y).set(x, items);
                }

                //entity
                if (base[y][x] == '.' && rand.nextInt(100) == 0) {
                    Entity e = switch (rand.nextInt(5)) {
                        case 0 -> new Entity('B', "bat", x, y, getHP("bat"));
                        case 1 -> new Entity('r', "sewer rat", x, y, getHP("sewer rat"));
                        case 2 -> new Entity('k', "kobold", x, y, getHP("kobold"));
                        case 3 -> new Entity('o', "goblin", x, y, getHP("goblin"));
                        case 4 -> new Entity('&', "Demogorgon", x, y, getHP("Demogorgon"));
                        default -> null;
                    };
                    entities.add(e);
                    entity[y][x] = e;
                }

            }
        }
        pl = new Entity('@', name, 2, 2, hp);
        entity[2][2] = pl;
    }

    public static int getHP(String name) {
        return switch (name) {
            case "bat" -> 2;
            case "sewer rat" -> 4;
            case "kobold" -> 5;
            case "goblin" -> 8;
            case "Demogorgon" -> 456;
            default -> 1;
        };
    }

    public static int getAttack(String s) {
        return switch (s) {
            case "bat", "goblin", "kobold" -> 4;
            case "sewer rat" -> 3;
            case "Demogorgon" -> 48;
            default -> 1;
        };
    }

    public static boolean adjacent(Entity a, Entity b) {
        return Math.abs(a.getX() - b.getX()) <= 1 && Math.abs(a.getY() - b.getY()) <= 1;
    }

    public static void attack(Entity damager, Entity damagee) throws IOException, InterruptedException {
        int dam = damager.getAttack();
        int health = damagee.getHealth();
        if (damagee == pl) {
            log.append("The ").append(damager.getName()).append(" hits!\n");
            hp -= dam;
        } else if (damager == pl) {
            log.append("You hit the ").append(damagee.getName()).append("\n");
        }
        if (dam >= health) {
            death(damagee);
        } else {
            damagee.setHealth(damagee.getHealth() - dam);
        }
    }

    public static void death(Entity e) throws IOException, InterruptedException {
        if (e == pl) {
            log.append("You were slain...");
            refLog();
            display();
            System.exit(0);
        } else {
            entities.remove(e);
            entity[e.getY()][e.getX()] = null;
            log.append(e.getName()).append(" died!\n");
        }
    }

    private static void display() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        StringBuilder display = new StringBuilder();
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
        refLog();
        System.out.print(log);
        display.append(name).append(" the ").append(role).append(" t:").append(time).append("\nDlvl:").append(dl).append(" Lv:").append(xl).append(" HP:").append(hp).append("/").append(maxhp).append(" Pw:").append(pw).append("/").append(maxpw).append("\n\n");
        System.out.print(display);
    }

    private static void inventory() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        StringBuilder display = new StringBuilder();
        char c = 97;
        display.append("Your inventory:\n");
        if (inv.size() == 0) {
            display.append("It's empty.\n");
        } else {
            for (Item i : inv) {
                display.append(c).append(") ").append(itemName(i)).append("\n");
                c++;
            }
        }
        display.append("\nType any command...\n");
        System.out.print(display);
        showInv = false;
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

    public int getAttack() {
        return Main.getAttack(getName());
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