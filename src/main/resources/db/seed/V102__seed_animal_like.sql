-- =============================================
-- [개발용 더미] animal_like (local/test 전용)
-- =============================================

-- [좋아요-1] 동물1(뭉치)에 회원 user01(박하늘) 좋아요
INSERT INTO `animal_like` (`animal_id`, `member_id`, `created_at`, `updated_at`)
VALUES (1, 6, '2026-06-03 10:00:00.000000', '2026-06-03 10:00:00.000000');

-- [좋아요-2] 동물1(뭉치)에 회원 user02(최바다) 좋아요
INSERT INTO `animal_like` (`animal_id`, `member_id`, `created_at`, `updated_at`)
VALUES (1, 7, '2026-06-04 11:00:00.000000', '2026-06-04 11:00:00.000000');

-- [좋아요-3] 동물1(뭉치)에 회원 user04(한별님) 좋아요 후 취소 (※ deleted_at 컬럼이 없으므로 데이터 삽입 제외)

-- [좋아요-4] 동물2(코리안숏헤어)에 회원 user02(최바다) 좋아요 눌렀다가 취소 (※ deleted_at 컬럼이 없으므로 데이터 삽입 제외)

-- [좋아요-5] 동물3(초코)에 회원 user01(박하늘) 좋아요
INSERT INTO `animal_like` (`animal_id`, `member_id`, `created_at`, `updated_at`)
VALUES (3, 6, '2026-04-21 09:00:00.000000', '2026-04-21 09:00:00.000000');

-- [좋아요-6] 동물7(대박이)에 회원 user01(박하늘) 좋아요
INSERT INTO `animal_like` (`animal_id`, `member_id`, `created_at`, `updated_at`)
VALUES (7, 6, '2026-02-16 08:00:00.000000', '2026-02-16 08:00:00.000000');

-- [좋아요-7] 동물7(대박이)에 회원 user02(최바다) 좋아요
INSERT INTO `animal_like` (`animal_id`, `member_id`, `created_at`, `updated_at`)
VALUES (7, 7, '2026-02-17 08:00:00.000000', '2026-02-17 08:00:00.000000');

-- [좋아요-8] 동물7(대박이)에 회원 user03(정온유) 좋아요
INSERT INTO `animal_like` (`animal_id`, `member_id`, `created_at`, `updated_at`)
VALUES (7, 8, '2026-02-18 08:00:00.000000', '2026-02-18 08:00:00.000000');

-- [좋아요-9] 동물9(싱크로)에 회원 user02(최바다) 좋아요
INSERT INTO `animal_like` (`animal_id`, `member_id`, `created_at`, `updated_at`)
VALUES (9, 7, '2026-01-22 09:00:00.000000', '2026-01-22 09:00:00.000000');