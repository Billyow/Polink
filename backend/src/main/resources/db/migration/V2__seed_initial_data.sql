INSERT INTO roles (id, name) VALUES
    ('10000000-0000-0000-0000-000000000001', 'ROLE_STUDENT'),
    ('10000000-0000-0000-0000-000000000002', 'ROLE_TUTOR'),
    ('10000000-0000-0000-0000-000000000003', 'ROLE_SUPERVISOR'),
    ('10000000-0000-0000-0000-000000000004', 'ROLE_ADMIN');

INSERT INTO subject_branches (id, name, description, active) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Ciencias Básicas', 'Asignaturas fundamentales de matemáticas, física y estadística.', TRUE),
    ('20000000-0000-0000-0000-000000000002', 'Ingeniería y Programación', 'Asignaturas de desarrollo de software, estructuras de datos y arquitectura.', TRUE),
    ('20000000-0000-0000-0000-000000000003', 'Obligatorias por Ley', 'Asignaturas institucionales y constitucionales exigidas por normativa.', TRUE),
    ('20000000-0000-0000-0000-000000000004', 'Ciencias Económicas y Administrativas', 'Asignaturas de economía, finanzas y gestión de proyectos.', TRUE);

INSERT INTO subjects (id, branch_id, code, name, active) VALUES
    ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'CB-101', 'Cálculo Diferencial', TRUE),
    ('30000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'CB-102', 'Cálculo Integral', TRUE),
    ('30000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', 'CB-103', 'Álgebra Lineal', TRUE),
    ('30000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'CB-104', 'Física Mecánica', TRUE),
    ('30000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', 'IP-201', 'Algoritmos y Programación', TRUE),
    ('30000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000002', 'IP-202', 'Estructuras de Datos', TRUE),
    ('30000000-0000-0000-0000-000000000007', '20000000-0000-0000-0000-000000000002', 'IP-203', 'Bases de Datos Relacionales', TRUE),
    ('30000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', 'IP-204', 'Ingeniería de Software', TRUE),
    ('30000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003', 'OL-301', 'Constitución Política y Civismo', TRUE),
    ('30000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000003', 'OL-302', 'Ética Profesional y Responsabilidad Social', TRUE),
    ('30000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000004', 'CE-401', 'Ingeniería Económica', TRUE),
    ('30000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000004', 'CE-402', 'Contabilidad y Costos', TRUE);
