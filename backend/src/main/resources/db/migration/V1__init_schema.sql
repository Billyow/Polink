CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(128) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE subject_branches (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    branch_id UUID NOT NULL REFERENCES subject_branches(id),
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE tutor_profiles (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    gpa_reported NUMERIC(3,2) NOT NULL,
    grade_certificate_url VARCHAR(500) NOT NULL,
    approval_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    rating_avg NUMERIC(3,2) NOT NULL DEFAULT 0.00,
    rating_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_tutor_gpa CHECK (gpa_reported >= 4.00 AND gpa_reported <= 5.00),
    CONSTRAINT chk_tutor_approval_status CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE TABLE tutor_initial_subjects (
    tutor_profile_id UUID NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    PRIMARY KEY (tutor_profile_id, subject_id)
);

CREATE TABLE tutoring_offers (
    id UUID PRIMARY KEY,
    tutor_profile_id UUID NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id),
    topics TEXT NOT NULL,
    hourly_rate NUMERIC(12,2) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    max_students_per_session INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(40) NOT NULL DEFAULT 'PENDING_APPROVAL',
    supervisor_feedback TEXT,
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_offer_hourly_rate CHECK (hourly_rate >= 0),
    CONSTRAINT chk_offer_max_students CHECK (max_students_per_session >= 1),
    CONSTRAINT chk_offer_status CHECK (status IN ('PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'DEACTIVATED_BY_SUPERVISOR', 'PAUSED_BY_TUTOR'))
);

CREATE TABLE offer_discount_tiers (
    id UUID PRIMARY KEY,
    offer_id UUID NOT NULL REFERENCES tutoring_offers(id) ON DELETE CASCADE,
    min_students INTEGER NOT NULL,
    discount_percentage NUMERIC(5,2) NOT NULL,
    CONSTRAINT uq_offer_min_students UNIQUE (offer_id, min_students),
    CONSTRAINT chk_tier_min_students CHECK (min_students >= 2),
    CONSTRAINT chk_tier_discount_pct CHECK (discount_percentage > 0 AND discount_percentage <= 90)
);

CREATE TABLE offer_moderation_reports (
    id UUID PRIMARY KEY,
    offer_id UUID NOT NULL REFERENCES tutoring_offers(id) ON DELETE CASCADE,
    supervisor_user_id UUID NOT NULL REFERENCES users(id),
    action_type VARCHAR(40) NOT NULL,
    report_reason TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE availability_slots (
    id UUID PRIMARY KEY,
    tutor_profile_id UUID NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_tutor_slot_datetime UNIQUE (tutor_profile_id, slot_date, start_time),
    CONSTRAINT chk_slot_status CHECK (status IN ('AVAILABLE', 'BOOKED', 'CANCELLED'))
);

CREATE TABLE study_groups (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    creator_student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invite_code VARCHAR(32) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE study_group_members (
    group_id UUID NOT NULL REFERENCES study_groups(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL DEFAULT 'JOINED',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (group_id, student_id),
    CONSTRAINT chk_group_member_status CHECK (status IN ('JOINED', 'INVITED'))
);

CREATE TABLE bookings (
    id UUID PRIMARY KEY,
    offer_id UUID NOT NULL REFERENCES tutoring_offers(id),
    requester_student_id UUID NOT NULL REFERENCES users(id),
    study_group_id UUID REFERENCES study_groups(id),
    student_count INTEGER NOT NULL DEFAULT 1,
    hours_count INTEGER NOT NULL DEFAULT 1,
    base_hourly_rate NUMERIC(12,2) NOT NULL,
    discount_applied_pct NUMERIC(5,2) NOT NULL DEFAULT 0.00,
    total_price NUMERIC(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_or_cancel_reason TEXT,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_booking_student_count CHECK (student_count >= 1),
    CONSTRAINT chk_booking_hours_count CHECK (hours_count >= 1),
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED'))
);

CREATE TABLE booking_slots (
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    slot_id UUID NOT NULL REFERENCES availability_slots(id) ON DELETE CASCADE,
    PRIMARY KEY (booking_id, slot_id)
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES users(id),
    tutor_profile_id UUID NOT NULL REFERENCES tutor_profiles(id) ON DELETE CASCADE,
    score SMALLINT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_booking_student_review UNIQUE (booking_id, student_id),
    CONSTRAINT chk_review_score CHECK (score BETWEEN 1 AND 5)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    reference_id UUID,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    actor_user_id UUID NOT NULL REFERENCES users(id),
    actor_role VARCHAR(50) NOT NULL,
    action_type VARCHAR(80) NOT NULL,
    target_entity VARCHAR(80) NOT NULL,
    target_id UUID,
    reason_or_details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subjects_branch_id ON subjects(branch_id);
CREATE INDEX idx_tutor_profiles_status ON tutor_profiles(approval_status);
CREATE INDEX idx_tutoring_offers_status ON tutoring_offers(status);
CREATE INDEX idx_tutoring_offers_subject_id ON tutoring_offers(subject_id);
CREATE INDEX idx_availability_slots_tutor_date ON availability_slots(tutor_profile_id, slot_date, status);
CREATE INDEX idx_bookings_offer_status ON bookings(offer_id, status);
CREATE INDEX idx_bookings_requester ON bookings(requester_student_id);
CREATE INDEX idx_notifications_recipient_unread ON notifications(recipient_user_id, is_read, created_at DESC);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
