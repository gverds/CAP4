--SET SCHEMA CAP;

-- ====================================
-- 角色文件權限設定檔
-- ====================================
--DROP TABLE DEF_RDA;
create table DEF_RDA (
   DOCID                VARCHAR(15)            not null,
   ROLCODE              VARCHAR(10)            not null,
   SYSTYP               CHAR(1)                not null,
   PGMAUTH              DECIMAL(1,0),
   UPDATER              VARCHAR(10),
   UPDTIME              TIMESTAMP              default CURRENT_TIMESTAMP,
   DISCRIMINATOR		VARCHAR(31),
   constraint P_DEF_RDA primary key (DOCID,ROLCODE,PGMAUTH,SYSTYP)
);

COMMENT ON TABLE DEF_RDA IS '角色文件權限設定檔';

COMMENT ON COLUMN DEF_RDA.DOCID IS '主文件編號';
COMMENT ON COLUMN DEF_RDA.ROLCODE IS '角色代號';
COMMENT ON COLUMN DEF_RDA.PGMAUTH IS '功能隸屬系統別';
COMMENT ON COLUMN DEF_RDA.SYSTYP IS '權限選項';
COMMENT ON COLUMN DEF_RDA.UPDATER IS '資料修改人';
COMMENT ON COLUMN DEF_RDA.UPDTIME IS '資料修改時間';

