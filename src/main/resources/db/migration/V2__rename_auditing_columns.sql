-- HR Core 통합: AuditingFields 컬럼명 변경
-- Core의 AuditingFields는 create_at, modify_at 컬럼명을 사용합니다.
-- 기존 create_date, modify_date 컬럼을 rename 합니다.

-- company
ALTER TABLE company RENAME COLUMN create_date TO create_at;
ALTER TABLE company RENAME COLUMN modify_date TO modify_at;

-- users
ALTER TABLE users RENAME COLUMN create_date TO create_at;
ALTER TABLE users RENAME COLUMN modify_date TO modify_at;

-- department
ALTER TABLE department RENAME COLUMN create_date TO create_at;
ALTER TABLE department RENAME COLUMN modify_date TO modify_at;

-- user_department
ALTER TABLE user_department RENAME COLUMN create_date TO create_at;
ALTER TABLE user_department RENAME COLUMN modify_date TO modify_at;

-- roles
ALTER TABLE roles RENAME COLUMN create_date TO create_at;
ALTER TABLE roles RENAME COLUMN modify_date TO modify_at;

-- permissions
ALTER TABLE permissions RENAME COLUMN create_date TO create_at;
ALTER TABLE permissions RENAME COLUMN modify_date TO modify_at;

-- role_permissions
ALTER TABLE role_permissions RENAME COLUMN create_date TO create_at;
ALTER TABLE role_permissions RENAME COLUMN modify_date TO modify_at;

-- user_roles
ALTER TABLE user_roles RENAME COLUMN create_date TO create_at;
ALTER TABLE user_roles RENAME COLUMN modify_date TO modify_at;

-- notice
ALTER TABLE notice RENAME COLUMN create_date TO create_at;
ALTER TABLE notice RENAME COLUMN modify_date TO modify_at;

-- dues
ALTER TABLE dues RENAME COLUMN create_date TO create_at;
ALTER TABLE dues RENAME COLUMN modify_date TO modify_at;

-- holiday
ALTER TABLE holiday RENAME COLUMN create_date TO create_at;
ALTER TABLE holiday RENAME COLUMN modify_date TO modify_at;

-- schedule
ALTER TABLE schedule RENAME COLUMN create_date TO create_at;
ALTER TABLE schedule RENAME COLUMN modify_date TO modify_at;

-- vacation_policy
ALTER TABLE vacation_policy RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_policy RENAME COLUMN modify_date TO modify_at;

-- vacation_plan
ALTER TABLE vacation_plan RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_plan RENAME COLUMN modify_date TO modify_at;

-- vacation_plan_policy
ALTER TABLE vacation_plan_policy RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_plan_policy RENAME COLUMN modify_date TO modify_at;

-- user_vacation_plan
ALTER TABLE user_vacation_plan RENAME COLUMN create_date TO create_at;
ALTER TABLE user_vacation_plan RENAME COLUMN modify_date TO modify_at;

-- vacation_grant
ALTER TABLE vacation_grant RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_grant RENAME COLUMN modify_date TO modify_at;

-- vacation_grant_schedule
ALTER TABLE vacation_grant_schedule RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_grant_schedule RENAME COLUMN modify_date TO modify_at;

-- vacation_usage
ALTER TABLE vacation_usage RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_usage RENAME COLUMN modify_date TO modify_at;

-- vacation_usage_deduction
ALTER TABLE vacation_usage_deduction RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_usage_deduction RENAME COLUMN modify_date TO modify_at;

-- vacation_approval
ALTER TABLE vacation_approval RENAME COLUMN create_date TO create_at;
ALTER TABLE vacation_approval RENAME COLUMN modify_date TO modify_at;

-- work_code
ALTER TABLE work_code RENAME COLUMN create_date TO create_at;
ALTER TABLE work_code RENAME COLUMN modify_date TO modify_at;

-- work_history
ALTER TABLE work_history RENAME COLUMN create_date TO create_at;
ALTER TABLE work_history RENAME COLUMN modify_date TO modify_at;

-- work_system_logs
ALTER TABLE work_system_logs RENAME COLUMN create_date TO create_at;
ALTER TABLE work_system_logs RENAME COLUMN modify_date TO modify_at;
