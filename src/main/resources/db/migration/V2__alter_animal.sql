-- =============================================
-- animal.age / animal.weight 타입 변경
--   SMALLINT UNSIGNED / DECIMAL(5,2) -> VARCHAR(20)
--   공공API 원본 문자열을 가공 없이 그대로 저장하는 방식으로 전환
--
-- ※ weight에 걸려있던 chk_animal_weight (CHECK weight > 0)는
--   숫자 비교 제약이라 VARCHAR 전환과 함께 제거함.
-- =============================================

ALTER TABLE `animal`
    DROP CHECK `chk_animal_weight`,
    MODIFY COLUMN `age`    VARCHAR(20) NOT NULL COMMENT '출생 연도 원본 문자열 (공공API 그대로)',
    MODIFY COLUMN `weight` VARCHAR(20) NOT NULL COMMENT '몸무게 원본 문자열 (공공API 그대로, kg)';
