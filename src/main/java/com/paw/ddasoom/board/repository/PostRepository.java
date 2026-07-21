package com.paw.ddasoom.board.repository;

import com.paw.ddasoom.board.domain.BoardType;
import com.paw.ddasoom.board.domain.Post;
import com.paw.ddasoom.board.dto.projection.MyPostListProjection;
import com.paw.ddasoom.board.dto.projection.PostListProjection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 게시글 목록 조회 — 카드 UI 전용 projection.
     * content 전체 대신 SUBSTRING으로 미리보기만 가져오고(무거운 컬럼 회피),
     * member는 JOIN으로 닉네임만 평면화(N+1 방지).
     */
    @Query("""
    SELECT NEW com.paw.ddasoom.board.dto.projection.PostListProjection(
        p.id, p.category, p.title, SUBSTRING(p.content, 1, 200),
        p.viewCount, p.commentCount, p.createdAt,
        m.id, m.nickname)
    FROM Post p
    JOIN p.member m
    WHERE p.boardType = :boardType
      AND (:category IS NULL OR p.category = :category)
      AND (:keyword IS NULL OR p.title LIKE CONCAT('%', :keyword, '%'))
      AND p.deletedAt IS NULL
    ORDER BY p.createdAt DESC
    """)
    Page<PostListProjection> findPostList(
            @Param("boardType") BoardType boardType,
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable);

    /**
     * 마이페이지 "내가 쓴 글" 목록 — findPostList와 동일한 projection 전략(무거운 content 회피).
     * 여러 보드의 글이 섞여 나오므로 boardType을 함께 조회(프론트 상세 경로 조립용),
     * member 조인은 불필요(본인 글만 조회).
     * boardType NULL = 전체 보드 조회 (탭 내 필터의 "전체"와 대응).
     */
    @Query("""
    SELECT NEW com.paw.ddasoom.board.dto.projection.MyPostListProjection(
        p.id, p.boardType, p.category, p.title, SUBSTRING(p.content, 1, 200),
        p.viewCount, p.commentCount, p.createdAt)
    FROM Post p
    WHERE p.member.id = :memberId
      AND (:boardType IS NULL OR p.boardType = :boardType)
      AND p.deletedAt IS NULL
    ORDER BY p.createdAt DESC
    """)
    Page<MyPostListProjection> findMyPosts(
            @Param("memberId") Long memberId,
            @Param("boardType") BoardType boardType,
            Pageable pageable);

    /**
     * 게시글 상세 조회 — member fetch join.
     * 단건 조회라 컬렉션 fetch join 페이징 이슈 없음.
     */
    @Query("""
        SELECT p FROM Post p
        JOIN FETCH p.member
        WHERE p.id = :postId
          AND p.deletedAt IS NULL
        """)
    Optional<Post> findDetailById(@Param("postId") Long postId);
}