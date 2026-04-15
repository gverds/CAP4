
--=====================================
--SCH_SCHEDULES 排程設定檔
--=====================================
--DROP TABLE AUDIT_LOG
CREATE TABLE AUDIT_LOG (
    oid            VARCHAR(32) NOT NULL,
    user_id        VARCHAR(20),
    ip_address     VARCHAR(50),
    function_id    VARCHAR(20),
    action_type    VARCHAR(20),
    execute_date   TIMESTAMP,
    remark         VARCHAR(50),
    CONSTRAINT P_AUDIT_LOG PRIMARY KEY (oid)
);

COMMENT ON TABLE AUDIT_LOG IS '使用軌跡表格';

COMMENT ON COLUMN AUDIT_LOG.user_id IS '使用者SSOID';
COMMENT ON COLUMN AUDIT_LOG.ip_address IS 'IP 位址';
COMMENT ON COLUMN AUDIT_LOG.function_id IS '作業代號';
COMMENT ON COLUMN AUDIT_LOG.action_type IS '新增/修改/刪除/查詢/匯出/匯入';
COMMENT ON COLUMN AUDIT_LOG.execute_date IS '執行時間';
COMMENT ON COLUMN AUDIT_LOG.remark IS '備註/Key值';
