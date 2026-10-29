-- ========================================================
-- CivicFix - Seed Data Migration V2
-- ========================================================

-- Insert Departments
INSERT INTO departments (id, name, code, description, contact_email, contact_phone) VALUES
(1, 'Estate & Infrastructure Maintenance', 'ESTATE', 'Handles furniture, civil works, roads, buildings and general facility repairs.', 'estate@civicfix.org', '+91 172 5001001'),
(2, 'Electrical & Power Systems', 'ELECTRICAL', 'Responsible for electrical installations, wiring, generators, HVAC and indoor/outdoor lighting.', 'electrical@civicfix.org', '+91 172 5001002'),
(3, 'Water, Sanitation & Hygiene', 'SANITATION', 'Oversees plumbing, drinking water coolers, sewage pipelines and campus waste sanitation.', 'sanitation@civicfix.org', '+91 172 5001003'),
(4, 'IT & Campus Network Facilities', 'IT_FACILITIES', 'Manages campus Wi-Fi access points, optical fiber connectivity and smart classroom electronics.', 'it@civicfix.org', '+91 172 5001004'),
(5, 'Campus Security & Traffic Operations', 'SECURITY', 'Regulates parking lots, barrier gates, campus surveillance and security fixtures.', 'security@civicfix.org', '+91 172 5001005')
ON CONFLICT (id) DO NOTHING;

SELECT setval('departments_id_seq', (SELECT MAX(id) FROM departments));

-- Insert Issue Categories
INSERT INTO issue_categories (id, name, code, description, department_id, sla_hours) VALUES
(1, 'Broken Furniture & Infrastructure', 'FURNITURE', 'Broken desks, cracked doors, window glass or damaged classroom benches.', 1, 48),
(2, 'Electrical Faults & Power Outage', 'ELECTRICAL_FAULT', 'Sparking switches, power cuts, broken sockets or equipment trip.', 2, 12),
(3, 'Water Leakage & Plumbing Issues', 'WATER_LEAK', 'Burst water pipes, leaking taps, washroom flush failure or water pump issues.', 3, 24),
(4, 'Garbage & Cleanliness Hazards', 'CLEANLINESS', 'Overflowing trash bins, litter accumulation or unhygienic corridor waste.', 3, 12),
(5, 'Wi-Fi & Network Connectivity', 'WIFI_CONNECTIVITY', 'Hostel or academic block dead zones, AP authentication failures or packet loss.', 4, 12),
(6, 'Parking & Facility Access', 'PARKING_ACCESS', 'Unauthorized vehicle blocking, damaged boom barriers or broken walkways.', 5, 24),
(7, 'Corridor & Streetlight Failure', 'STREETLIGHT', 'Dark campus pathways, failed sodium lights or broken stairwell bulbs.', 2, 24)
ON CONFLICT (id) DO NOTHING;

SELECT setval('issue_categories_id_seq', (SELECT MAX(id) FROM issue_categories));

-- Password for all demo accounts is: password123
-- BCrypt encoded hash: $2a$10$wT5i2d97yvGzG1U3L1u7teU5n77C7Yp1l5uV2N7Hqj75a/sN4q5eq
INSERT INTO users (id, email, password_hash, full_name, phone, role, department_id, is_active) VALUES
(1, 'admin@civicfix.org', '$2a$10$wT5i2d97yvGzG1U3L1u7teU5n77C7Yp1l5uV2N7Hqj75a/sN4q5eq', 'Dr. Arvind Mehra (Dean)', '+91 9876543210', 'ADMIN', NULL, TRUE),
(2, 'authority@civicfix.org', '$2a$10$wT5i2d97yvGzG1U3L1u7teU5n77C7Yp1l5uV2N7Hqj75a/sN4q5eq', 'Er. Shalini Verma', '+91 9876543211', 'AUTHORITY', 1, TRUE),
(3, 'worker@civicfix.org', '$2a$10$wT5i2d97yvGzG1U3L1u7teU5n77C7Yp1l5uV2N7Hqj75a/sN4q5eq', 'Ramesh Kumar (Estate Tech)', '+91 9876543212', 'WORKER', 1, TRUE),
(4, 'rajesh.worker@civicfix.org', '$2a$10$wT5i2d97yvGzG1U3L1u7teU5n77C7Yp1l5uV2N7Hqj75a/sN4q5eq', 'Rajesh Sharma (Electrician)', '+91 9876543213', 'WORKER', 2, TRUE),
(5, 'citizen@civicfix.org', '$2a$10$wT5i2d97yvGzG1U3L1u7teU5n77C7Yp1l5uV2N7Hqj75a/sN4q5eq', 'Pulkit Sharma (Student)', '+91 9876543214', 'CITIZEN', NULL, TRUE),
(6, 'priya.citizen@civicfix.org', '$2a$10$wT5i2d97yvGzG1U3L1u7teU5n77C7Yp1l5uV2N7Hqj75a/sN4q5eq', 'Priya Patel (Researcher)', '+91 9876543215', 'CITIZEN', NULL, TRUE)
ON CONFLICT (id) DO NOTHING;

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- Seed Initial Issues
INSERT INTO issues (id, tracking_number, title, description, category_id, department_id, reporter_id, assigned_worker_id, status, severity, calculated_priority, priority_score, latitude, longitude, address_text, confirmation_count, reopen_count, created_at, updated_at) VALUES
(1, 'CF-2026-0001', 'Severe Water Pipe Leakage near Academic Block B Exit', 'Water is rapidly gushing out from the secondary ceiling pipe near the ground floor south exit. Slipping hazard for students.', 3, 3, 5, NULL, 'REPORT_CREATED', 'HIGH', 'HIGH', 78.5, 30.7699450, 76.5759120, 'Academic Block B, Ground Floor South Exit', 4, 0, CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP),
(2, 'CF-2026-0002', 'Broken Auditorium Projector Mount & Desks in Hall 402', 'Two rows of bench desks have broken metallic joints and the projector ceiling bracket is loose vibrating dangerously.', 1, 1, 5, 3, 'ASSIGNED', 'MEDIUM', 'MEDIUM', 52.0, 30.7712000, 76.5768000, 'Central Academic Complex, Lecture Hall 402', 2, 0, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
(3, 'CF-2026-0003', 'Central Library 2nd Floor High-Speed Wi-Fi AP Offline', 'The Cisco Access Point AP-LIB-203 has power blinking red and cannot route any 5GHz traffic. Over 80 students unable to study.', 5, 4, 6, NULL, 'VERIFIED', 'HIGH', 'HIGH', 72.0, 30.7688000, 76.5745000, 'Central Library, Reference Section 2nd Floor', 8, 0, CURRENT_TIMESTAMP - INTERVAL '8 hours', CURRENT_TIMESTAMP),
(4, 'CF-2026-0004', 'Main Dining Hall Garbage Collection Bin Overflow', 'Waste disposal drums outside North Canteen are overflowing attracting pests. Urgent sanitation needed before lunch service.', 4, 3, 6, NULL, 'IN_PROGRESS', 'CRITICAL', 'CRITICAL', 94.0, 30.7725000, 76.5781000, 'Student Dining Hall 1, North Food Court', 12, 0, CURRENT_TIMESTAMP - INTERVAL '6 hours', CURRENT_TIMESTAMP),
(5, 'CF-2026-0005', 'Main Gate Corridor Floodlights Not Operational', 'Three halogen lighting poles on the pathway towards Hostel Block 4 are completely dark after 7:30 PM.', 7, 2, 5, 4, 'RESOLVED', 'MEDIUM', 'MEDIUM', 45.0, 30.7705000, 76.5732000, 'Campus Outer Ring Road, Near Gate 2', 3, 0, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('issues_id_seq', (SELECT MAX(id) FROM issues));

-- Status History Records for Auditing
INSERT INTO issue_status_history (issue_id, actor_id, previous_status, new_status, reason_or_notes, created_at) VALUES
(1, 5, NULL, 'REPORT_CREATED', 'Citizen reported water leakage with photographic proof.', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
(2, 5, NULL, 'REPORT_CREATED', 'Citizen reported auditorium damage.', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(2, 2, 'REPORT_CREATED', 'UNDER_REVIEW', 'Authority reviewed physical damage report.', CURRENT_TIMESTAMP - INTERVAL '20 hours'),
(2, 2, 'UNDER_REVIEW', 'VERIFIED', 'Damage verified as legitimate safety hazard.', CURRENT_TIMESTAMP - INTERVAL '18 hours'),
(2, 2, 'VERIFIED', 'ASSIGNED', 'Assigned to Ramesh Kumar from Estate maintenance.', CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(5, 5, NULL, 'REPORT_CREATED', 'Citizen reported pathway darkness.', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(5, 2, 'REPORT_CREATED', 'ASSIGNED', 'Assigned to Electrical Dept.', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(5, 4, 'ASSIGNED', 'IN_PROGRESS', 'Replaced capacitor and cabling.', CURRENT_TIMESTAMP - INTERVAL '8 hours'),
(5, 4, 'IN_PROGRESS', 'RESOLVED', 'All 3 floodlights tested operational and glowing at 100%.', CURRENT_TIMESTAMP - INTERVAL '1 hour');

-- Community Confirmations
INSERT INTO community_confirmations (issue_id, citizen_id, note, created_at) VALUES
(1, 6, 'Saw this flooding while going to physics class. High slipping danger.', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(3, 5, 'Can confirm, entire hall has lost wireless connection.', CURRENT_TIMESTAMP - INTERVAL '7 hours')
ON CONFLICT (issue_id, citizen_id) DO NOTHING;

-- Worker Assignments
INSERT INTO worker_assignments (issue_id, worker_id, assigned_by_id, status, assigned_at) VALUES
(2, 3, 2, 'PENDING', CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(5, 4, 2, 'COMPLETED', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Notifications
INSERT INTO notifications (recipient_id, title, message, notification_type, issue_id, is_read, created_at) VALUES
(3, 'New Task Assigned', 'You have been assigned issue CF-2026-0002: Broken Auditorium Projector Mount & Desks in Hall 402', 'TASK_ASSIGNED', 2, FALSE, CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(5, 'Issue Status Update', 'Your report CF-2026-0002 has been verified and assigned to the Estate Department.', 'STATUS_CHANGE', 2, FALSE, CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(5, 'Issue Resolved', 'Your reported issue CF-2026-0005 has been marked as resolved by Rajesh Sharma. Please verify the resolution.', 'STATUS_CHANGE', 5, FALSE, CURRENT_TIMESTAMP - INTERVAL '1 hour');
