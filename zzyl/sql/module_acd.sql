-- A/C/D 模块数据表（在 zzyl 库执行）

CREATE TABLE IF NOT EXISTS t_reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) COMMENT '参观预约/探访预约',
    visitor_name VARCHAR(50),
    visitor_phone VARCHAR(20),
    elder_name VARCHAR(50),
    appoint_time DATETIME,
    status VARCHAR(20) DEFAULT '待上门',
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_visit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type VARCHAR(20) COMMENT '参观来访/探访来访',
    visitor_name VARCHAR(50),
    visitor_phone VARCHAR(20),
    elder_name VARCHAR(50),
    visit_time DATETIME,
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    signed TINYINT DEFAULT 0,
    order_count INT DEFAULT 0,
    elder_names VARCHAR(200),
    first_login_time DATETIME
);

CREATE TABLE IF NOT EXISTS t_contract (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    contract_no VARCHAR(50),
    contract_name VARCHAR(100),
    elder_name VARCHAR(50),
    elder_idcard VARCHAR(20),
    checkin_no VARCHAR(50),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20),
    sign_date DATE,
    third_party_name VARCHAR(50),
    third_party_phone VARCHAR(20),
    contract_file VARCHAR(200),
    terminate_user VARCHAR(50),
    terminate_date DATE,
    terminate_file VARCHAR(200),
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_room_type (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50),
    image VARCHAR(200),
    price DECIMAL(10,2),
    intro VARCHAR(500),
    status TINYINT DEFAULT 1,
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_floor (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20),
    sort_num INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS t_room (
    id INT PRIMARY KEY AUTO_INCREMENT,
    floor_id INT,
    room_no VARCHAR(20),
    room_type_id INT,
    room_type_name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS t_bed (
    id INT PRIMARY KEY AUTO_INCREMENT,
    room_id INT,
    bed_no VARCHAR(20),
    elder_name VARCHAR(50),
    status VARCHAR(20) DEFAULT '空闲'
);

CREATE TABLE IF NOT EXISTS t_leave (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doc_no VARCHAR(50),
    elder_name VARCHAR(50),
    elder_idcard VARCHAR(20),
    elder_phone VARCHAR(20),
    nursing_level VARCHAR(50),
    bed_info VARCHAR(100),
    caregivers VARCHAR(200),
    start_time DATETIME,
    expect_return_time DATETIME,
    actual_return_time DATETIME,
    status VARCHAR(20) DEFAULT '请假中',
    escort VARCHAR(50),
    leave_days INT,
    reason VARCHAR(500),
    applicant VARCHAR(50),
    apply_time DATETIME,
    return_remark VARCHAR(500),
    cancel_user VARCHAR(50),
    cancel_time DATETIME,
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_checkout (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    doc_no VARCHAR(50),
    elder_name VARCHAR(50),
    elder_idcard VARCHAR(20),
    elder_phone VARCHAR(20),
    fee_start DATE,
    fee_end DATE,
    nursing_level VARCHAR(50),
    bed_info VARCHAR(100),
    contract_name VARCHAR(100),
    contract_file VARCHAR(200),
    consultant VARCHAR(50),
    caregivers VARCHAR(200),
    checkout_date DATE,
    reason VARCHAR(100),
    remark VARCHAR(500),
    applicant VARCHAR(50),
    apply_time DATETIME,
    step INT DEFAULT 1,
    step_status VARCHAR(20) DEFAULT '进行中',
    terminate_date DATE,
    terminate_file VARCHAR(200),
    refund_amount DECIMAL(10,2),
    flow_status VARCHAR(20),
    approval_result VARCHAR(20),
    approval_comment VARCHAR(500),
    creator VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
