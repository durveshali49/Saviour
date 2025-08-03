import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.*;

// --- User Class ---
class User {
    private String id;
    private String name;
    private String email;
    private String bloodType;
    private String location;
    private long mobile;
    private String role;
    private LocalDateTime lastDonatedDateTime;
    private String gender;

    public User(String id, String name, String email, String bloodType, String location, long mobile, String role, String gender) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.bloodType = bloodType;
        this.location = location;
        this.mobile = mobile;
        this.role = role;
        this.lastDonatedDateTime = null;
        this.gender = gender;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getBloodType() { return bloodType; }
    public String getLocation() { return location; }
    public long getMobile() { return mobile; }
    public String getRole() { return role; }
    public LocalDateTime getLastDonatedDateTime() { return lastDonatedDateTime; }
    public String getGender() { return gender; }

    public void setLastDonatedDateTime(LocalDateTime dateTime) {
        this.lastDonatedDateTime = dateTime;
    }
}

// --- BloodRequest Class ---
class BloodRequest {
    private String id;
    private String userId;
    private String bloodType;
    private String hospitalArea;
    private int unitsNeeded;
    private String seriousness;
    private String status;
    private LocalDateTime createdAt;

    public BloodRequest(String id, String userId, String bloodType, String hospitalArea, int unitsNeeded, String seriousness, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.bloodType = bloodType;
        this.hospitalArea = hospitalArea;
        this.unitsNeeded = unitsNeeded;
        this.seriousness = seriousness;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getBloodType() { return bloodType; }
    public String getHospitalArea() { return hospitalArea; }
    public int getUnitsNeeded() { return unitsNeeded; }
    public String getSeriousness() { return seriousness; }
    public String getStatus() { return status; }

    public void setUnitsNeeded(int unitsNeeded) { this.unitsNeeded = unitsNeeded; }
    public void setStatus(String status) { this.status = status; }
}

// --- Donation Class ---
class Donation {
    private String id;
    private String donorId;
    private String requestId;
    private LocalDateTime donationDateTime;

    public Donation(String id, String donorId, String requestId, LocalDateTime donationDateTime) {
        this.id = id;
        this.donorId = donorId;
        this.requestId = requestId;
        this.donationDateTime = donationDateTime;
    }

    public String getDonorId() { return donorId; }
    public String getRequestId() { return requestId; }
    public LocalDateTime getDonationDateTime() { return donationDateTime; }
}

// --- Main Web Application Class ---
public class BloodDonationWebApp {
    private List<User> users = new ArrayList<>();
    private List<BloodRequest> requests = new ArrayList<>();
    private List<Donation> donations = new ArrayList<>();
    private int userNumericIdCounter = 1;
    private int requestNumericIdCounter = 1;
    private int donationNumericIdCounter = 1;
    private Random rand = new Random();
    private HttpServer server;

    // Validation Methods
    private boolean isValidEmail(String email) {
        String regex = "^[a-z][a-z0-9]*@gmail\\.com$";
        return email != null && email.matches(regex);
    }

    private boolean isValidBloodType(String bloodType) {
        String[] validTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        if (bloodType == null) return false;
        for (String type : validTypes) {
            if (type.equalsIgnoreCase(bloodType)) return true;
        }
        return false;
    }

    private boolean isValidMobile(long mobile) {
        String mobileStr = String.valueOf(mobile);
        return mobileStr.length() == 10 && (mobileStr.charAt(0) >= '6' && mobileStr.charAt(0) <= '9');
    }

    private boolean isValidSeriousness(String seriousness) {
        String[] validLevels = {"LOW", "MODERATE", "HIGH"};
        if (seriousness == null) return false;
        for (String level : validLevels) {
            if (level.equalsIgnoreCase(seriousness)) return true;
        }
        return false;
    }

    private boolean isValidGender(String gender) {
        return gender != null && (gender.equalsIgnoreCase("MALE") || gender.equalsIgnoreCase("FEMALE") || gender.equalsIgnoreCase("OTHER"));
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z\\s]+$") && !name.trim().isEmpty();
    }

    private boolean isEmailTaken(String email) {
        return users.stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    private boolean isMobileTaken(long mobile) {
        return users.stream().anyMatch(u -> u.getMobile() == mobile);
    }

    private boolean isEligibleForDonation(String userId) {
        User donor = users.stream().filter(u -> u.getId().equals(userId) && u.getRole().equals("DONOR")).findFirst().orElse(null);
        if (donor == null) return false;
        if (donor.getLastDonatedDateTime() == null) return true;

        int donationCooldownDays = "MALE".equalsIgnoreCase(donor.getGender()) ? 120 : 180;
        long daysSinceLastDonation = ChronoUnit.DAYS.between(donor.getLastDonatedDateTime(), LocalDateTime.now());
        return daysSinceLastDonation >= donationCooldownDays;
    }

    private boolean canDonateTo(String donorBloodType, String receiverBloodType) {
        switch (donorBloodType.toUpperCase()) {
            case "O-": return true;
            case "O+": return receiverBloodType.endsWith("+") || receiverBloodType.equalsIgnoreCase("O+");
            case "A-": return receiverBloodType.startsWith("A") || receiverBloodType.startsWith("AB");
            case "A+": return (receiverBloodType.startsWith("A") || receiverBloodType.startsWith("AB")) && receiverBloodType.endsWith("+");
            case "B-": return receiverBloodType.startsWith("B") || receiverBloodType.startsWith("AB");
            case "B+": return (receiverBloodType.startsWith("B") || receiverBloodType.startsWith("AB")) && receiverBloodType.endsWith("+");
            case "AB-": return receiverBloodType.startsWith("AB");
            case "AB+": return receiverBloodType.equalsIgnoreCase("AB+");
            default: return false;
        }
    }

    // Web Server Setup
    public void startWebServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // Serve HTML files
            server.createContext("/", new StaticFileHandler());
            
            // API endpoints
            server.createContext("/api/register-donor", new RegisterDonorHandler());
            server.createContext("/api/register-receiver", new RegisterReceiverHandler());
            server.createContext("/api/post-request", new PostRequestHandler());
            server.createContext("/api/get-requests", new GetRequestsHandler());
            server.createContext("/api/get-donors", new GetDonorsHandler());
            server.createContext("/api/record-donation", new RecordDonationHandler());
            server.createContext("/api/donor-details", new DonorDetailsHandler());
            server.createContext("/api/my-requests", new MyRequestsHandler());
            
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            
            System.out.println("🌐 Blood Donation Web App started at http://localhost:8080");
            System.out.println("📱 Open your browser and go to http://localhost:8080 to use the app!");
            System.out.println("Press Ctrl+C to stop the server");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start web server: " + e.getMessage());
        }
    }

    // Static file handler to serve HTML, CSS, JS files
    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            // Try to read file from current directory
            File file = new File("." + path);
            if (file.exists() && file.isFile()) {
                String contentType = getContentType(path);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                
                try (FileInputStream fis = new FileInputStream(file);
                     OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                // File not found
                String response = "<!DOCTYPE html><html><head><title>File Not Found</title></head><body><h1>404 - File Not Found</h1><p>The file " + path + " was not found.</p></body></html>";
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(404, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            return "text/plain";
        }
    }

    // API Handlers
    class RegisterDonorHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    String[] params = requestBody.split("&");
                    
                    String name = "", email = "", bloodType = "", location = "", gender = "";
                    long mobile = 0;
                    LocalDateTime lastDonated = null;
                    
                    for (String param : params) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2) {
                            String key = URLDecoder.decode(kv[0], "UTF-8");
                            String value = URLDecoder.decode(kv[1], "UTF-8");
                            
                            switch (key) {
                                case "name": name = value; break;
                                case "email": email = value; break;
                                case "mobile": 
                                    try { mobile = Long.parseLong(value); } catch (NumberFormatException e) { mobile = 0; }
                                    break;
                                case "bloodType": bloodType = value.toUpperCase(); break;
                                case "location": location = value; break;
                                case "gender": gender = value.toUpperCase(); break;
                                case "lastDonated": 
                                    if (!value.isEmpty() && !value.equals("NEVER")) {
                                        try {
                                            lastDonated = LocalDateTime.parse(value + " 00:00:00", 
                                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                                        } catch (Exception e) { /* ignore */ }
                                    }
                                    break;
                            }
                        }
                    }
                    
                    // Validate inputs
                    if (!isValidName(name) || !isValidEmail(email) || !isValidMobile(mobile) || 
                        !isValidBloodType(bloodType) || location.isEmpty() || !isValidGender(gender)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid input data. Please check all fields.\"}");
                        return;
                    }
                    
                    if (isEmailTaken(email)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Email already registered.\"}");
                        return;
                    }
                    
                    if (isMobileTaken(mobile)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Mobile number already registered.\"}");
                        return;
                    }
                    
                    String donorId = String.valueOf(userNumericIdCounter++);
                    User newDonor = new User(donorId, name, email, bloodType, location, mobile, "DONOR", gender);
                    newDonor.setLastDonatedDateTime(lastDonated);
                    users.add(newDonor);
                    
                    String response = "{\"success\": true, \"message\": \"Donor registered successfully!\", \"userId\": \"" + donorId + "\"}";
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Registration failed: " + e.getMessage() + "\"}");
                }
            }
        }
    }
    
    class RegisterReceiverHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    String[] params = requestBody.split("&");
                    
                    String name = "", email = "", location = "", gender = "";
                    long mobile = 0;
                    
                    for (String param : params) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2) {
                            String key = URLDecoder.decode(kv[0], "UTF-8");
                            String value = URLDecoder.decode(kv[1], "UTF-8");
                            
                            switch (key) {
                                case "name": name = value; break;
                                case "email": email = value; break;
                                case "mobile": 
                                    try { mobile = Long.parseLong(value); } catch (NumberFormatException e) { mobile = 0; }
                                    break;
                                case "location": location = value; break;
                                case "gender": gender = value.toUpperCase(); break;
                            }
                        }
                    }
                    
                    if (!isValidName(name) || !isValidEmail(email) || !isValidMobile(mobile) || 
                        location.isEmpty() || !isValidGender(gender)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid input data\"}");
                        return;
                    }
                    
                    if (isEmailTaken(email)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Email already registered\"}");
                        return;
                    }
                    
                    if (isMobileTaken(mobile)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Mobile number already registered\"}");
                        return;
                    }
                    
                    String receiverId = "REC-" + userNumericIdCounter++;
                    users.add(new User(receiverId, name, email, null, location, mobile, "RECEIVER", gender));
                    
                    String response = "{\"success\": true, \"message\": \"Receiver registered successfully!\", \"userId\": \"" + receiverId + "\"}";
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Registration failed: " + e.getMessage() + "\"}");
                }
            }
        }
    }
    
    class PostRequestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    String[] params = requestBody.split("&");
                    
                    String userId = "", bloodType = "", hospitalArea = "", seriousness = "";
                    int unitsNeeded = 0;
                    
                    for (String param : params) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2) {
                            String key = URLDecoder.decode(kv[0], "UTF-8");
                            String value = URLDecoder.decode(kv[1], "UTF-8");
                            
                            switch (key) {
                                case "userId": userId = value; break;
                                case "bloodType": bloodType = value.toUpperCase(); break;
                                case "hospitalArea": hospitalArea = value; break;
                                case "unitsNeeded": 
                                    try { unitsNeeded = Integer.parseInt(value); } catch (NumberFormatException e) { unitsNeeded = 0; }
                                    break;
                                case "seriousness": seriousness = value.toUpperCase(); break;
                            }
                        }
                    }
                    
                    final String finalUserId = userId;
                    User user = users.stream().filter(u -> u.getId().equals(finalUserId)).findFirst().orElse(null);
                    if (user == null || user.getRole().equals("DONOR")) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid user or only receivers can post requests\"}");
                        return;
                    }
                    
                    if (!isValidBloodType(bloodType) || hospitalArea.isEmpty() || 
                        unitsNeeded < 1 || unitsNeeded > 10 || !isValidSeriousness(seriousness)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Invalid request data\"}");
                        return;
                    }
                    
                    LocalDateTime createdAt = LocalDateTime.now();
                    String requestId = "REQ-" + requestNumericIdCounter++;
                    requests.add(new BloodRequest(requestId, userId, bloodType, hospitalArea, unitsNeeded, seriousness, "OPEN", createdAt));
                    
                    String response = "{\"success\": true, \"message\": \"Blood request posted successfully!\", \"requestId\": \"" + requestId + "\"}";
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Failed to post request: " + e.getMessage() + "\"}");
                }
            }
        }
    }
    
    class GetRequestsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    StringBuilder json = new StringBuilder("[");
                    boolean first = true;
                    
                    for (BloodRequest request : requests) {
                        if (request.getStatus().equals("OPEN")) {
                            if (!first) json.append(",");
                            json.append("{")
                                .append("\"id\":\"").append(request.getId()).append("\",")
                                .append("\"bloodType\":\"").append(request.getBloodType()).append("\",")
                                .append("\"hospitalArea\":\"").append(request.getHospitalArea()).append("\",")
                                .append("\"unitsNeeded\":").append(request.getUnitsNeeded()).append(",")
                                .append("\"seriousness\":\"").append(request.getSeriousness()).append("\",")
                                .append("\"status\":\"").append(request.getStatus()).append("\"")
                                .append("}");
                            first = false;
                        }
                    }
                    json.append("]");
                    
                    sendResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"error\": \"Failed to get requests\"}");
                }
            }
        }
    }
    
    class RecordDonationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    String[] params = requestBody.split("&");
                    
                    String donorIdParam = "";
                    String requestIdParam = "";
                    
                    for (String param : params) {
                        String[] kv = param.split("=", 2);
                        if (kv.length == 2) {
                            String key = URLDecoder.decode(kv[0], "UTF-8");
                            String value = URLDecoder.decode(kv[1], "UTF-8");
                            
                            switch (key) {
                                case "donorId": donorIdParam = value; break;
                                case "requestId": requestIdParam = value; break;
                            }
                        }
                    }
                    
                    final String donorId = donorIdParam;
                    final String requestId = requestIdParam;
                    
                    User donor = users.stream().filter(u -> u.getId().equals(donorId) && u.getRole().equals("DONOR")).findFirst().orElse(null);
                    if (donor == null) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Donor not found\"}");
                        return;
                    }
                    
                    if (!isEligibleForDonation(donorId)) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Donor not eligible for donation yet\"}");
                        return;
                    }
                    
                    BloodRequest request = requests.stream().filter(r -> r.getId().equals(requestId) && r.getStatus().equals("OPEN")).findFirst().orElse(null);
                    if (request == null) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Request not found or already fulfilled\"}");
                        return;
                    }
                    
                    if (!canDonateTo(donor.getBloodType(), request.getBloodType())) {
                        sendResponse(exchange, 400, "{\"success\": false, \"message\": \"Blood type not compatible\"}");
                        return;
                    }
                    
                    LocalDateTime donationDateTime = LocalDateTime.now();
                    String donationId = "DON-" + donationNumericIdCounter++;
                    donations.add(new Donation(donationId, donorId, requestId, donationDateTime));
                    donor.setLastDonatedDateTime(donationDateTime);
                    
                    request.setUnitsNeeded(request.getUnitsNeeded() - 1);
                    if (request.getUnitsNeeded() <= 0) {
                        request.setStatus("FULFILLED");
                    }
                    
                    String response = "{\"success\": true, \"message\": \"Donation recorded successfully! Thank you for saving lives!\"}";
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Failed to record donation: " + e.getMessage() + "\"}");
                }
            }
        }
    }
    
    class DonorDetailsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String userIdParam = "";
                    if (query != null) {
                        String[] params = query.split("&");
                        for (String param : params) {
                            String[] kv = param.split("=", 2);
                            if (kv.length == 2 && kv[0].equals("userId")) {
                                userIdParam = URLDecoder.decode(kv[1], "UTF-8");
                            }
                        }
                    }
                    
                    final String userId = userIdParam;
                    User user = users.stream().filter(u -> u.getId().equals(userId) && u.getRole().equals("DONOR")).findFirst().orElse(null);
                    if (user == null) {
                        sendResponse(exchange, 404, "{\"success\": false, \"message\": \"Donor not found\"}");
                        return;
                    }
                    
                    boolean eligible = isEligibleForDonation(userId);
                    String lastDonated = user.getLastDonatedDateTime() != null ? 
                        user.getLastDonatedDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "Never";
                    
                    String response = "{" +
                        "\"success\": true," +
                        "\"name\": \"" + user.getName() + "\"," +
                        "\"email\": \"" + user.getEmail() + "\"," +
                        "\"bloodType\": \"" + user.getBloodType() + "\"," +
                        "\"location\": \"" + user.getLocation() + "\"," +
                        "\"mobile\": " + user.getMobile() + "," +
                        "\"gender\": \"" + user.getGender() + "\"," +
                        "\"lastDonated\": \"" + lastDonated + "\"," +
                        "\"eligible\": " + eligible +
                        "}";
                    
                    sendResponse(exchange, 200, response);
                    
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"success\": false, \"message\": \"Failed to get donor details\"}");
                }
            }
        }
    }
    
    class MyRequestsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String userIdParam = "";
                    if (query != null) {
                        String[] params = query.split("&");
                        for (String param : params) {
                            String[] kv = param.split("=", 2);
                            if (kv.length == 2 && kv[0].equals("userId")) {
                                userIdParam = URLDecoder.decode(kv[1], "UTF-8");
                            }
                        }
                    }
                    
                    final String userId = userIdParam;
                    StringBuilder json = new StringBuilder("[");
                    boolean first = true;
                    
                    for (BloodRequest request : requests) {
                        if (request.getUserId().equals(userId)) {
                            if (!first) json.append(",");
                            json.append("{")
                                .append("\"id\":\"").append(request.getId()).append("\",")
                                .append("\"bloodType\":\"").append(request.getBloodType()).append("\",")
                                .append("\"hospitalArea\":\"").append(request.getHospitalArea()).append("\",")
                                .append("\"unitsNeeded\":").append(request.getUnitsNeeded()).append(",")
                                .append("\"seriousness\":\"").append(request.getSeriousness()).append("\",")
                                .append("\"status\":\"").append(request.getStatus()).append("\"")
                                .append("}");
                            first = false;
                        }
                    }
                    json.append("]");
                    
                    sendResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"error\": \"Failed to get requests\"}");
                }
            }
        }
    }
    
    class GetDonorsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCORSHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    StringBuilder json = new StringBuilder("[");
                    boolean first = true;
                    
                    for (User user : users) {
                        if (user.getRole().equals("DONOR") && isEligibleForDonation(user.getId())) {
                            if (!first) json.append(",");
                            json.append("{")
                                .append("\"id\":\"").append(user.getId()).append("\",")
                                .append("\"name\":\"").append(user.getName()).append("\",")
                                .append("\"bloodType\":\"").append(user.getBloodType()).append("\",")
                                .append("\"location\":\"").append(user.getLocation()).append("\"")
                                .append("}");
                            first = false;
                        }
                    }
                    json.append("]");
                    
                    sendResponse(exchange, 200, json.toString());
                } catch (Exception e) {
                    sendResponse(exchange, 500, "{\"error\": \"Failed to get donors\"}");
                }
            }
        }
    }
    
    private void setCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        byte[] responseBytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    public static void main(String[] args) {
        BloodDonationWebApp app = new BloodDonationWebApp();
        app.startWebServer();
        
        // Keep the application running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
        }
    }
}
