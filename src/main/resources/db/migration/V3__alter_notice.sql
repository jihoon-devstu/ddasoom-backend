-- =============================================
-- notice.pin_order 칼럼 추가 및 인덱스 생성
--   INT NULL (NULL = 미고정, 값이 낮을수록 상단 노출)
--   공지사항 상단 고정(📌) 및 최신순 정렬 기능 고도화
--
-- ※ title의 이모지 파싱 방식 대신 별도 정렬 칼럼을 추가하여 DB 성능 최적화.
-- ※ (pin_order, created_at) 복합 인덱스를 생성하여 
--    상단 고정 및 페이징 조회 속도(인덱스 스캔) 보장.
-- =============================================

ALTER TABLE `notice`
    ADD COLUMN `pin_order` INT NULL COMMENT '상단 고정 순서 (NULL=미고정, 낮을수록 상단)'
    AFTER `is_visible`;

CREATE INDEX `idx_notice_pin_created` ON `notice` (`pin_order`, `created_at`);