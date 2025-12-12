import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class Railway {

    static class TrainClass {
        String name;
        int rows, cols;
        double baseFare;
        Map<Integer, String> berthMap = new HashMap<>();
        TrainClass(String name, int rows, int cols, double baseFare) {
            this.name = name; this.rows = rows; this.cols = cols; this.baseFare = baseFare;
        }
        int capacity() { return rows * cols; }
    }

    static class Train {
        int no;
        String name;
        Map<String, boolean[]> seatsByClass = new HashMap<>();
        Map<String, TrainClass> classes = new LinkedHashMap<>();
        Train(int no, String name, List<TrainClass> clsList) {
            this.no = no; this.name = name;
            for (TrainClass tc : clsList) {
                classes.put(tc.name, tc);
                seatsByClass.put(tc.name, new boolean[tc.capacity()]);
            }
        }
    }

    static class Passenger {
        String name; int age; String gender;
        Passenger(String n, int a, String g) { name=n; age=a; gender=g; }
        @Override public String toString() { return name+"("+age+")"; }
    }

    static class Booking {
        static int nextPNR = 7000;
        int pnr;
        Train train;
        String trainClass;
        int[] seats;
        List<Passenger> passengers;
        double totalFare;
        Booking(Train t, String tc, int[] seats, List<Passenger> ps, double fare){
            this.pnr = nextPNR++; this.train=t; this.trainClass=tc; this.seats=seats; this.passengers=ps; this.totalFare=fare;
        }
    }

    private static final Scanner sc = new Scanner(System.in);
    private static final List<Train> trains = new ArrayList<>();
    private static final List<Booking> bookings = new ArrayList<>();

    private static final Map<String, Double> BERTH_PRICE_MOD = Map.of("Lower",0.0,"Upper",0.10,"Side",-0.20);
    private static final Map<String, Double> CLASS_MULT = Map.of("AC",1.0,"Sleeper",0.7,"General",0.35);

    public static void main(String[] args) {
        initData();
        while (true) {
            System.out.println("\n--- Railway Terminal Reservation ---");
            System.out.println("1. Book Ticket\n2. View Bookings\n3. Search PNR\n4. Cancel Booking\n5. Admin: Free All Seats\n6. Exit");
            System.out.print("Choice: ");
            String choice = sc.nextLine().trim();
            switch(choice){
                case "1": bookTicket(); break;
                case "2": viewBookings(); break;
                case "3": searchPNR(); break;
                case "4": cancelBooking(); break;
                case "5": freeAllSeats(); break;
                case "6": System.exit(0);
                default: System.out.println("Invalid choice."); break;
            }
        }
    }

    private static void initData() {
        TrainClass ac = new TrainClass("AC", 4,4,1200);
        TrainClass sl = new TrainClass("Sleeper", 6,4,700);
        TrainClass gn = new TrainClass("General", 8,6,350);
        fillBerths(ac); fillBerths(sl); fillBerths(gn);
        List<TrainClass> cls = Arrays.asList(ac, sl, gn);
        trains.add(new Train(101,"InterCity Express", cls));
        trains.add(new Train(102,"Coastal Mail", cls));
        trains.add(new Train(303,"Mountain Special", cls));
    }

    private static void fillBerths(TrainClass tc) {
        for(int r=0;r<tc.rows;r++)
            for(int c=0;c<tc.cols;c++){
                int idx = r*tc.cols+c;
                String berth = (tc.cols>=4 && c==tc.cols-1) ? "Side" : (c%2==0 ? "Lower":"Upper");
                tc.berthMap.put(idx, berth);
            }
    }

    private static void bookTicket() {
        System.out.println("\nAvailable trains:");
        for(int i=0;i<trains.size();i++){
            Train t = trains.get(i);
            System.out.println((i+1)+". "+t.no+" - "+t.name);
        }
        System.out.print("Select train number: "); int tIdx = Integer.parseInt(sc.nextLine())-1;
        if(tIdx<0||tIdx>=trains.size()){ System.out.println("Invalid train."); return; }
        Train selectedTrain = trains.get(tIdx);

        System.out.println("Available classes:");
        List<String> clsList = new ArrayList<>(selectedTrain.classes.keySet());
        for(int i=0;i<clsList.size();i++) System.out.println((i+1)+". "+clsList.get(i));
        System.out.print("Select class: "); int cIdx = Integer.parseInt(sc.nextLine())-1;
        if(cIdx<0||cIdx>=clsList.size()){ System.out.println("Invalid class."); return; }
        String selectedClass = clsList.get(cIdx);
        TrainClass tc = selectedTrain.classes.get(selectedClass);
        boolean[] seatsTaken = selectedTrain.seatsByClass.get(selectedClass);

        System.out.println("Seats available (L=Lower,U=Upper,S=Side):");
        for(int i=0;i<tc.capacity();i++){
            if(seatsTaken[i]) System.out.print("XX "); 
            else System.out.print((i+1)+"("+tc.berthMap.get(i).charAt(0)+") ");
            if((i+1)%tc.cols==0) System.out.println();
        }

        System.out.print("How many passengers? (Max 6): "); int pCount = Integer.parseInt(sc.nextLine());
        if(pCount<1||pCount>6){ System.out.println("Invalid number."); return; }
        List<Passenger> passengers = new ArrayList<>();
        for(int i=0;i<pCount;i++){
            System.out.print("Passenger "+(i+1)+" name: "); String n = sc.nextLine();
            System.out.print("Age: "); int a = Integer.parseInt(sc.nextLine());
            System.out.print("Gender (M/F/Other): "); String g = sc.nextLine();
            passengers.add(new Passenger(n,a,g));
        }

        System.out.println("Select seats (space separated indices): ");
        String[] seatStrs = sc.nextLine().split("\\s+");
        if(seatStrs.length!=pCount){ System.out.println("Seat count must match passengers."); return; }
        int[] seats = new int[pCount];
        double totalFare = 0.0;
        for(int i=0;i<pCount;i++){
            int s = Integer.parseInt(seatStrs[i])-1;
            if(s<0||s>=tc.capacity()||seatsTaken[s]){ System.out.println("Invalid or taken seat: "+(s+1)); return; }
            seats[i] = s;
            String berth = tc.berthMap.get(s);
            double berthMod = BERTH_PRICE_MOD.getOrDefault(berth,0.0);
            double clsMult = CLASS_MULT.getOrDefault(tc.name,1.0);
            totalFare += tc.baseFare*clsMult*(1+berthMod);
        }

        System.out.println("Confirm booking? Total fare: ₹"+new DecimalFormat("#.##").format(totalFare)+" (Y/N): ");
        String confirm = sc.nextLine();
        if(!confirm.equalsIgnoreCase("Y")) return;

        // mark seats
        for(int s: seats) seatsTaken[s] = true;
        Booking b = new Booking(selectedTrain, selectedClass, seats, passengers, totalFare);
        bookings.add(b);

        // write ticket
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter("ticket_"+b.pnr+".txt"));
            writer.write("PNR: "+b.pnr+"\nTrain: "+selectedTrain.no+" "+selectedTrain.name+"\nClass: "+selectedClass+"\nSeats: ");
            for(int s: seats) writer.write((s+1)+"("+tc.berthMap.get(s)+") ");
            writer.write("\nPassengers: ");
            for(Passenger p:passengers) writer.write(p+"; ");
            writer.write("\nFare: ₹"+new DecimalFormat("#.##").format(totalFare));
            writer.close();
        } catch(Exception ex){ System.out.println("Error writing ticket: "+ex.getMessage()); }

        System.out.println("Booking confirmed! PNR: "+b.pnr);
    }

    private static void viewBookings() {
        if(bookings.isEmpty()){ System.out.println("No bookings."); return; }
        for(Booking b:bookings){
            System.out.println("PNR: "+b.pnr+", Train: "+b.train.no+" "+b.train.name+", Class: "+b.trainClass+", Seats: "+Arrays.toString(b.seats)+", Fare: ₹"+b.totalFare);
        }
    }

    private static void searchPNR() {
        System.out.print("Enter PNR: ");
        int pnr = Integer.parseInt(sc.nextLine());
        for(Booking b:bookings){
            if(b.pnr==pnr){
                System.out.println("PNR: "+b.pnr+"\nTrain: "+b.train.no+" "+b.train.name+"\nClass: "+b.trainClass+"\nSeats: "+Arrays.toString(b.seats)+"\nPassengers: "+b.passengers+"\nFare: ₹"+b.totalFare);
                return;
            }
        }
        System.out.println("PNR not found.");
    }

    private static void cancelBooking() {
        System.out.print("Enter PNR to cancel: ");
        int pnr = Integer.parseInt(sc.nextLine());
        Booking toRemove = null;
        for(Booking b:bookings) if(b.pnr==pnr) { toRemove=b; break; }
        if(toRemove==null){ System.out.println("PNR not found."); return; }
        boolean[] arr = toRemove.train.seatsByClass.get(toRemove.trainClass);
        for(int s: toRemove.seats) arr[s]=false;
        bookings.remove(toRemove);
        System.out.println("Cancelled PNR "+pnr);
    }

    private static void freeAllSeats() {
        System.out.print("Admin password: ");
        String pwd = sc.nextLine();
        if(!pwd.equals("admin123")){ System.out.println("Wrong password."); return; }
        for(Train t: trains) for(String cls: t.classes.keySet()) Arrays.fill(t.seatsByClass.get(cls), false);
        bookings.clear();
        System.out.println("All bookings cleared.");
    }
}
