import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MovieTicketBooking {

    static class Movie implements Serializable {
        String id, title, description;
        List<Show> shows = new ArrayList<>();
        Movie(String id, String title, String desc) { this.id=id; this.title=title; this.description=desc; }
    }

    static class Show implements Serializable {
        String id;
        LocalDateTime dateTime;
        int rows, cols;
        boolean[][] occupied;
        Show(String id, LocalDateTime dt, int rows, int cols) {
            this.id=id; this.dateTime=dt; this.rows=rows; this.cols=cols;
            occupied = new boolean[rows][cols];
        }
    }

    static class Booking implements Serializable {
        String id, movieId, showId, customerName;
        List<int[]> seats;
        LocalDateTime bookedAt;
        Booking(String id, String movieId, String showId, String customerName, List<int[]> seats) {
            this.id=id; this.movieId=movieId; this.showId=showId; this.customerName=customerName;
            this.seats = seats;
            bookedAt = LocalDateTime.now();
        }
    }

    static class DataStore implements Serializable {
        List<Movie> movies = new ArrayList<>();
        List<Booking> bookings = new ArrayList<>();
        Map<String, String> users = new HashMap<>();
    }

    static final String STORE_FILE = "store.ser";
    static DataStore store;

    static Scanner sc = new Scanner(System.in);
    static String currentUser;
    static boolean isAdmin;

    public static void main(String[] args) {
        loadStore();
        login();
        mainMenu();
    }

    static void loadStore() {
        try {
            File f = new File(STORE_FILE);
            if (!f.exists()) {
                store = new DataStore();
                store.users.put("admin", "admin123");
                Movie m1 = new Movie("M1","The Adventures of Java","Epic coding adventure");
                m1.shows.add(new Show("S1", LocalDateTime.now().plusDays(1).withHour(11).withMinute(0),6,8));
                m1.shows.add(new Show("S2", LocalDateTime.now().plusDays(1).withHour(15).withMinute(30),6,8));
                store.movies.add(m1);
                saveStore();
            } else {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STORE_FILE))) {
                    store = (DataStore) ois.readObject();
                }
            }
        } catch (Exception e) { e.printStackTrace(); store = new DataStore(); store.users.put("admin","admin123"); }
    }

    static void saveStore() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORE_FILE))) {
            oos.writeObject(store);
        } catch (Exception e) { e.printStackTrace(); }
    }

    static void login() {
        while (true) {
            System.out.println("==== Login ====");
            System.out.print("Username: "); String user = sc.nextLine().trim();
            System.out.print("Password: "); String pass = sc.nextLine().trim();
            if (store.users.containsKey(user) && store.users.get(user).equals(pass)) {
                currentUser = user; isAdmin = "admin".equals(user);
                System.out.println("Welcome " + currentUser + (isAdmin ? " (Admin)" : ""));
                break;
            } else {
                System.out.println("Invalid credentials, try again.");
            }
        }
    }

    static void mainMenu() {
        while (true) {
            System.out.println("\n==== Main Menu ====");
            System.out.println("1. List Movies");
            System.out.println("2. List Shows");
            System.out.println("3. Book Seats");
            System.out.println("4. Cancel Booking");
            if (isAdmin) {
                System.out.println("5. Add Movie");
                System.out.println("6. Edit Movie");
                System.out.println("7. Delete Movie");
            }
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            String choice = sc.nextLine();
            switch (choice) {
                case "1": listMovies(); break;
                case "2": listShows(); break;
                case "3": bookSeats(); break;
                case "4": cancelBooking(); break;
                case "5": if(isAdmin) addMovie(); break;
                case "6": if(isAdmin) editMovie(); break;
                case "7": if(isAdmin) deleteMovie(); break;
                case "0": saveStore(); System.exit(0); break;
                default: System.out.println("Invalid choice."); break;
            }
        }
    }

    static void listMovies() {
        System.out.println("\n---- Movies ----");
        for (Movie m: store.movies) System.out.println(m.id + ": " + m.title + " - " + m.description);
    }

    static void listShows() {
        listMovies();
        System.out.print("Enter movie ID to list shows: ");
        String mid = sc.nextLine().trim();
        Movie m = findMovie(mid);
        if (m==null) { System.out.println("Movie not found."); return; }
        System.out.println("\nShows for " + m.title);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Show s: m.shows) System.out.println(s.id + " - " + s.dateTime.format(fmt) + " ("+s.rows+"x"+s.cols+")");
    }

    static void bookSeats() {
        listMovies();
        System.out.print("Movie ID: "); String mid = sc.nextLine().trim();
        Movie m = findMovie(mid); if (m==null) { System.out.println("Invalid movie."); return; }
        listShowsForMovie(m);
        System.out.print("Show ID: "); String sid = sc.nextLine().trim();
        Show s = findShow(m, sid); if (s==null) { System.out.println("Invalid show."); return; }

        printSeats(s);
        System.out.print("Customer name: "); String cust = sc.nextLine().trim();
        System.out.print("Enter seats (e.g., A1 A2 B3): ");
        String[] seatInputs = sc.nextLine().trim().split(" ");
        List<int[]> chosen = new ArrayList<>();
        for (String si: seatInputs) {
            if (si.length()<2) continue;
            int row = si.charAt(0)-'A';
            int col;
            try { col = Integer.parseInt(si.substring(1))-1; } catch(Exception e){ continue; }
            if (row<0 || row>=s.rows || col<0 || col>=s.cols || s.occupied[row][col]) {
                System.out.println("Seat " + si + " is invalid or occupied.");
            } else {
                chosen.add(new int[]{row,col});
            }
        }
        if (chosen.isEmpty()) { System.out.println("No valid seats chosen."); return; }
        for (int[] p: chosen) s.occupied[p[0]][p[1]] = true;
        String bid = "BK"+(store.bookings.size()+1);
        store.bookings.add(new Booking(bid, m.id, s.id, cust, chosen));
        saveStore();
        System.out.println("Booked " + chosen.size() + " seat(s). Booking ID: " + bid);
    }

    static void cancelBooking() {
        if (!isAdmin) { System.out.println("Admin only."); return; }
        System.out.print("Enter booking ID to cancel: "); String bid = sc.nextLine().trim();
        Booking target=null;
        for (Booking b: store.bookings) if (b.id.equals(bid)) { target=b; break; }
        if (target==null) { System.out.println("Booking not found."); return; }
        Movie m = findMovie(target.movieId);
        Show s = findShow(m, target.showId);
        if (s!=null) for(int[] p: target.seats) s.occupied[p[0]][p[1]]=false;
        store.bookings.remove(target);
        saveStore();
        System.out.println("Booking canceled.");
    }

    static void addMovie() {
        System.out.print("Title: "); String title=sc.nextLine().trim();
        System.out.print("Description: "); String desc=sc.nextLine().trim();
        Movie m = new Movie("M"+System.currentTimeMillis(), title, desc);
        store.movies.add(m);
        saveStore();
        System.out.println("Movie added.");
    }

    static void editMovie() {
        listMovies();
        System.out.print("Enter movie ID to edit: "); String mid=sc.nextLine().trim();
        Movie m = findMovie(mid); if (m==null){System.out.println("Movie not found."); return;}
        System.out.print("New title ("+m.title+"): "); String title=sc.nextLine().trim();
        System.out.print("New description ("+m.description+"): "); String desc=sc.nextLine().trim();
        if (!title.isEmpty()) m.title=title;
        if (!desc.isEmpty()) m.description=desc;
        saveStore();
        System.out.println("Movie updated.");
    }

    static void deleteMovie() {
        listMovies();
        System.out.print("Enter movie ID to delete: "); String mid=sc.nextLine().trim();
        Movie m = findMovie(mid); if (m==null){System.out.println("Movie not found."); return;}
        store.bookings.removeIf(b -> b.movieId.equals(m.id));
        store.movies.remove(m);
        saveStore();
        System.out.println("Movie deleted.");
    }

    static Movie findMovie(String id) {
        for (Movie m: store.movies) if (m.id.equals(id)) return m;
        return null;
    }

    static Show findShow(Movie m, String sid) {
        for (Show s: m.shows) if (s.id.equals(sid)) return s;
        return null;
    }

    static void listShowsForMovie(Movie m) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (Show s: m.shows) System.out.println(s.id+" - "+s.dateTime.format(fmt)+" ("+s.rows+"x"+s.cols+")");
    }

    static void printSeats(Show s) {
        System.out.println("Seat Map (X = occupied):");
        for(int r=0;r<s.rows;r++){
            for(int c=0;c<s.cols;c++){
                System.out.print(s.occupied[r][c] ? "X " : (char)('A'+r)+(c+1)+" ");
            }
            System.out.println();
        }
    }
}
