-- Profile Types (PDF requirement: profile_id values 0, 1, 2)
INSERT INTO profile_types (id, name, description) VALUES 
(0, 'SuperAdmin', 'System administrator with full access'),
(1, 'Teacher', 'Teacher with limited access to assigned classrooms'),
(2, 'Student', 'Student with access to assigned courses')
ON CONFLICT (id) DO NOTHING;
