package com.example.cs4076_project2;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

public class ServerGUI {
    private static final int PORT = 1234;
    private static ServerSocket servSock;
    private static String[] monday = new String[9];
    private static String[] tuesday = new String[9];
    private static String[] wednesday = new String[9];
    private static String[] thursday = new String[9];
    private static String[] friday = new String[9];
    private static ArrayList<String> moduleCount = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Opening port ...\n");
        try {
            servSock = new ServerSocket(1234);
            System.out.println("Server started, waiting for connections...");
            while (true) {
                Socket clientSocket = servSock.accept();
                System.out.println("Client connected");
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException ioe) {
            System.out.println("Could not open port " + PORT);
            System.exit(1);
        }
    }

    static class ClientHandler extends Thread {
        private final Socket link;

        public ClientHandler(Socket clientSocket) {
            this.link = clientSocket;
        }

        @Override
        public void run() {
            handleClient(link);
        }

        private void handleClient(Socket clientSocket) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter printer = new PrintWriter(clientSocket.getOutputStream(), true);

                while (true) {
                    String message = in.readLine();
                    if (message == null) {
                        break;
                    }

                    System.out.println("Received message: " + message);
                    String[] components = message.split("/");

                    if (components.length == 0) {
                        continue;
                    }

                    String command = components[0].toUpperCase();
                    boolean success;

                    switch (command) {
                        case "A":
                            String day = components[1];
                            String time = components[2];
                            String room = components[3];
                            String module = components[4];
                            success = addLecture(day, time, room, module);
                            printer.println(success ? "Lecture Added Successfully!" : "Failed to Add Lecture!");
                            break;

                        case "D":
                            day = components[1];
                            time = components[2];
                            success = deleteLecture(day, time);
                            printer.println(success ? "Lecture Deleted Successfully!" : "Failed to Delete Lecture!");
                            break;

                        case "V":
                            viewWeekSchedule(printer);
                            break;

                        case "R":
                            day = components[1];
                            RequestGUI(printer, day);
                            break;

                        default:
                            printer.println("Unknown command: " + command);
                            break;
                    }
                }

            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    public synchronized static boolean addLecture(String day, String time, String room, String module) {
        int timeSlot = Integer.parseInt(time);
        boolean added = false;
        boolean result = checkModule(module);
        if (!result) {
            System.out.println("Module " + module + " is not available");
            return false;
        }
        String entry = module + ", " + room;

        switch (day.toLowerCase()) {
            case "monday":
                if (timeSlot >= 0 && timeSlot < monday.length && monday[timeSlot] != null) {
                    System.out.println("Time slot already taken for Monday.");
                } else {
                    monday[timeSlot] = entry;
                    added = true;
                }
                break;
            case "tuesday":
                if (timeSlot >= 0 && timeSlot < tuesday.length && tuesday[timeSlot] != null) {
                    System.out.println("Time slot already taken for Tuesday.");
                } else {
                    tuesday[timeSlot] = entry;
                    added = true;
                }
                break;
            case "wednesday":
                if (timeSlot >= 0 && timeSlot < wednesday.length && wednesday[timeSlot] != null) {
                    System.out.println("Time slot already taken for Wednesday.");
                } else {
                    wednesday[timeSlot] = entry;
                    added = true;
                }
                break;
            case "thursday":
                if (timeSlot >= 0 && timeSlot < thursday.length && thursday[timeSlot] != null) {
                    System.out.println("Time slot already taken for Thursday.");
                } else {
                    thursday[timeSlot] = entry;
                    added = true;
                }
                break;
            case "friday":
                if (timeSlot >= 0 && timeSlot < friday.length && friday[timeSlot] != null) {
                    System.out.println("Time slot already taken for Friday.");
                } else {
                    friday[timeSlot] = entry;
                    added = true;
                }
                break;
            default:
                System.out.println("Invalid day entered.");
        }
        return added;
    }

    public synchronized static void viewWeekSchedule(PrintWriter printer) {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        String[][] weekSchedule = {monday, tuesday, wednesday, thursday, friday};

        for (int i = 0; i < days.length; i++) {
            for (int j = 0; j < 9; j++) {
                if (weekSchedule[i][j] != null) {
                    String[] parts = weekSchedule[i][j].split(", ");
                    printer.println(days[i] + "/" + j + "/" + parts[0] + "/" + parts[1]);
                }
            }
        }
        printer.println("END");
    }

    private synchronized static boolean deleteLecture(String day, String time) {
        int timeSlot = Integer.parseInt(time);
        boolean deleted = false;
        switch (day.toLowerCase()) {
            case "monday":
                if (monday[timeSlot] != null) {
                    monday[timeSlot] = null;
                    deleted = true;
                }
                break;
            case "tuesday":
                if (tuesday[timeSlot] != null) {
                    tuesday[timeSlot] = null;
                    deleted = true;
                }
                break;
            case "wednesday":
                if (wednesday[timeSlot] != null) {
                    wednesday[timeSlot] = null;
                    deleted = true;
                }
                break;
            case "thursday":
                if (thursday[timeSlot] != null) {
                    thursday[timeSlot] = null;
                    deleted = true;
                }
                break;
            case "friday":
                if (friday[timeSlot] != null) {
                    friday[timeSlot] = null;
                    deleted = true;
                }
                break;
            default:
                System.out.println("Invalid day entered.");
        }
        return deleted;
    }

    public synchronized static boolean checkModule(String module) {
        if (!moduleCount.contains(module) && moduleCount.size() < 5) {
            moduleCount.add(module);
            return true;
        } else if (moduleCount.contains(module)) {
            return true;
        }
        return false;
    }



    public static synchronized void RequestGUI(PrintWriter printer, String day) {
        String[] daySchedule = null;

        switch (day.toLowerCase()) {
            case "monday":
                daySchedule = monday;
                break;
            case "tuesday":
                daySchedule = tuesday;
                break;
            case "wednesday":
                daySchedule = wednesday;
                break;
            case "thursday":
                daySchedule = thursday;
                break;
            case "friday":
                daySchedule = friday;
                break;
            default:
                printer.println("Invalid day entered.");
                return;
        }

        ForkJoinPool pool = new ForkJoinPool();
        ShiftTask task = new ShiftTask(daySchedule);
        pool.submit(task);
        pool.shutdown();

        try {
            task.get();
            StringBuilder newSchedule = new StringBuilder();
            for (int i = 0; i < 9; i++) {
                if (daySchedule[i] != null) {
                    newSchedule.append("Slot ").append(i + 1).append(": ").append(daySchedule[i]).append("\n");
                }
            }
            printer.println("Updated schedule for " + day + ":\n" + newSchedule);
        } catch (Exception e) {
            printer.println("Error processing the schedule shift: " + e.getMessage());
        }
    }

    private static class ShiftTask extends RecursiveTask<Void> {
        private final String[] daySchedule;

        public ShiftTask(String[] daySchedule) {
            this.daySchedule = daySchedule;
        }

        @Override
        protected Void compute() {
            boolean[] isSlotOccupied = new boolean[9];
            for (int i = 0; i < 4; i++) {
                if (daySchedule[i] != null) {
                    isSlotOccupied[i] = true;
                }
            }

            for (int i = 4; i < 9; i++) {
                if (daySchedule[i] != null) {
                    boolean shifted = false;
                    for (int j = 0; j < 4; j++) {
                        if (!isSlotOccupied[j]) {
                            daySchedule[j] = daySchedule[i];
                            daySchedule[i] = null;
                            isSlotOccupied[j] = true;
                            shifted = true;
                            break;
                        }
                    }
                }
            }

            return null;
        }
    }
}
