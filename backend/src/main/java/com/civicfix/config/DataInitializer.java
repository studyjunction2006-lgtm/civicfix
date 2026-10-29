package com.civicfix.config;

import com.civicfix.category.IssueCategory;
import com.civicfix.category.CategoryRepository;
import com.civicfix.department.Department;
import com.civicfix.department.DepartmentRepository;
import com.civicfix.issue.*;
import com.civicfix.user.Role;
import com.civicfix.user.User;
import com.civicfix.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final CategoryRepository categoryRepository;
    private final IssueRepository issueRepository;
    private final IssueStatusHistoryRepository statusHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           CategoryRepository categoryRepository,
                           IssueRepository issueRepository,
                           IssueStatusHistoryRepository statusHistoryRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.categoryRepository = categoryRepository;
        this.issueRepository = issueRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // If users already exist, update passwords to ensure password123 is valid
        if (userRepository.count() > 0) {
            userRepository.findAll().forEach(u -> {
                if (u.getPasswordHash() == null || !u.getPasswordHash().startsWith("$2a$")) {
                    u.setPasswordHash(passwordEncoder.encode("password123"));
                    userRepository.save(u);
                }
            });
            log.info("Verified existing user credentials in database.");
            return;
        }

        log.info("Database empty. Initializing departments, categories, demo users, and sample issues...");

        // 1. Departments
        Department estate = departmentRepository.save(new Department("Estate & Infrastructure Maintenance", "ESTATE", "Classroom, desks, civil works", "estate@civicfix.org", "+91 172 5001001"));
        Department electrical = departmentRepository.save(new Department("Electrical & Power Systems", "ELECTRICAL", "Wiring, lighting, HVAC", "electrical@civicfix.org", "+91 172 5001002"));
        Department sanitation = departmentRepository.save(new Department("Water, Sanitation & Hygiene", "SANITATION", "Plumbing, drinking water, waste", "sanitation@civicfix.org", "+91 172 5001003"));
        Department it = departmentRepository.save(new Department("IT & Campus Network Facilities", "IT_FACILITIES", "Wi-Fi, smart classroom electronics", "it@civicfix.org", "+91 172 5001004"));
        Department security = departmentRepository.save(new Department("Campus Security & Traffic Operations", "SECURITY", "Parking, barriers, CCTV", "security@civicfix.org", "+91 172 5001005"));

        // 2. Categories
        IssueCategory catFurniture = categoryRepository.save(new IssueCategory("Broken Furniture & Infrastructure", "FURNITURE", "Broken desks, doors, windows", estate, 48));
        IssueCategory catElectrical = categoryRepository.save(new IssueCategory("Electrical Faults & Power Outage", "ELECTRICAL_FAULT", "Sparking switches, power cuts", electrical, 12));
        IssueCategory catWater = categoryRepository.save(new IssueCategory("Water Leakage & Plumbing Issues", "WATER_LEAK", "Burst pipes, tap leaks, washroom issues", sanitation, 24));
        IssueCategory catGarbage = categoryRepository.save(new IssueCategory("Garbage & Cleanliness Hazards", "CLEANLINESS", "Overflowing bins, unhygienic waste", sanitation, 12));
        IssueCategory catWifi = categoryRepository.save(new IssueCategory("Wi-Fi & Network Connectivity", "WIFI_CONNECTIVITY", "Campus Wi-Fi AP dead zones", it, 12));
        IssueCategory catParking = categoryRepository.save(new IssueCategory("Parking & Facility Access", "PARKING_ACCESS", "Vehicle obstruction, broken barriers", security, 24));
        IssueCategory catLighting = categoryRepository.save(new IssueCategory("Corridor & Streetlight Failure", "STREETLIGHT", "Dark pathways, broken lamps", electrical, 24));

        // 3. Demo Users (Password: password123)
        String passHash = passwordEncoder.encode("password123");
        User admin = userRepository.save(new User("admin@civicfix.org", passHash, "Dr. Arvind Mehra (Dean)", "+91 9876543210", Role.ADMIN, null));
        User authority = userRepository.save(new User("authority@civicfix.org", passHash, "Er. Shalini Verma", "+91 9876543211", Role.AUTHORITY, estate));
        User workerEstate = userRepository.save(new User("worker@civicfix.org", passHash, "Ramesh Kumar (Estate Tech)", "+91 9876543212", Role.WORKER, estate));
        User workerElec = userRepository.save(new User("rajesh.worker@civicfix.org", passHash, "Rajesh Sharma (Electrician)", "+91 9876543213", Role.WORKER, electrical));
        User citizen1 = userRepository.save(new User("citizen@civicfix.org", passHash, "Pulkit Sharma (Student)", "+91 9876543214", Role.CITIZEN, null));
        User citizen2 = userRepository.save(new User("priya.citizen@civicfix.org", passHash, "Priya Patel (Researcher)", "+91 9876543215", Role.CITIZEN, null));

        // 4. Sample Issues for Campus Map
        createSampleIssue("CF-2026-0001", "Severe Water Pipe Leakage near Academic Block B Exit",
                "Water is rapidly gushing out from the secondary ceiling pipe near the ground floor south exit. Slipping hazard for students.",
                catWater, sanitation, citizen1, null, IssueStatus.REPORT_CREATED, IssueSeverity.HIGH, IssuePriority.HIGH, 78.5,
                new BigDecimal("30.7699450"), new BigDecimal("76.5759120"), "Academic Block B, Ground Floor South Exit", 4, 0);

        createSampleIssue("CF-2026-0002", "Broken Auditorium Projector Mount & Desks in Hall 402",
                "Two rows of bench desks have broken metallic joints and the projector ceiling bracket is loose vibrating dangerously.",
                catFurniture, estate, citizen1, workerEstate, IssueStatus.ASSIGNED, IssueSeverity.MEDIUM, IssuePriority.MEDIUM, 52.0,
                new BigDecimal("30.7712000"), new BigDecimal("76.5768000"), "Central Academic Complex, Lecture Hall 402", 2, 0);

        createSampleIssue("CF-2026-0003", "Central Library 2nd Floor High-Speed Wi-Fi AP Offline",
                "The Cisco Access Point AP-LIB-203 has power blinking red and cannot route any 5GHz traffic. Over 80 students unable to study.",
                catWifi, it, citizen2, null, IssueStatus.VERIFIED, IssueSeverity.HIGH, IssuePriority.HIGH, 72.0,
                new BigDecimal("30.7688000"), new BigDecimal("76.5745000"), "Central Library, Reference Section 2nd Floor", 8, 0);

        createSampleIssue("CF-2026-0004", "Main Dining Hall Garbage Collection Bin Overflow",
                "Waste disposal drums outside North Canteen are overflowing attracting pests. Urgent sanitation needed before lunch service.",
                catGarbage, sanitation, citizen2, null, IssueStatus.IN_PROGRESS, IssueSeverity.CRITICAL, IssuePriority.CRITICAL, 94.0,
                new BigDecimal("30.7725000"), new BigDecimal("76.5781000"), "Student Dining Hall 1, North Food Court", 12, 0);

        createSampleIssue("CF-2026-0005", "Main Gate Corridor Floodlights Not Operational",
                "Three halogen lighting poles on the pathway towards Hostel Block 4 are completely dark after 7:30 PM.",
                catLighting, electrical, citizen1, workerElec, IssueStatus.RESOLVED, IssueSeverity.MEDIUM, IssuePriority.MEDIUM, 45.0,
                new BigDecimal("30.7705000"), new BigDecimal("76.5732000"), "Campus Outer Ring Road, Near Gate 2", 3, 0);

        log.info("Initialized 5 departments, 7 categories, 6 users, and 5 sample issues with map coordinates.");
    }

    private void createSampleIssue(String tracking, String title, String desc, IssueCategory cat, Department dept,
                                   User reporter, User worker, IssueStatus status, IssueSeverity sev, IssuePriority prio,
                                   double score, BigDecimal lat, BigDecimal lng, String address, int confirms, int reopens) {
        Issue issue = new Issue();
        issue.setTrackingNumber(tracking);
        issue.setTitle(title);
        issue.setDescription(desc);
        issue.setCategory(cat);
        issue.setDepartment(dept);
        issue.setReporter(reporter);
        issue.setAssignedWorker(worker);
        issue.setStatus(status);
        issue.setSeverity(sev);
        issue.setCalculatedPriority(prio);
        issue.setPriorityScore(score);
        issue.setLatitude(lat);
        issue.setLongitude(lng);
        issue.setAddressText(address);
        issue.setConfirmationCount(confirms);
        issue.setReopenCount(reopens);
        issue.setDueDate(Instant.now().plus(cat.getSlaHours(), ChronoUnit.HOURS));
        if (status == IssueStatus.RESOLVED || status == IssueStatus.CLOSED) {
            issue.setResolvedAt(Instant.now().minus(2, ChronoUnit.HOURS));
        }
        Issue saved = issueRepository.save(issue);

        IssueStatusHistory history = new IssueStatusHistory(saved, reporter, null, status, "Initial issue recorded in CivicFix");
        statusHistoryRepository.save(history);
    }
}
