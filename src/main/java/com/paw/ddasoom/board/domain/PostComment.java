package com.paw.ddasoom.board.domain;

import com.paw.ddasoom.common.util.BaseTimeEntity;
import com.paw.ddasoom.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /** 작성자 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 부모 댓글 (자기참조) — NULL = 일반 댓글, 값 있음 = 대댓글 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /** NULL = 활성 (soft delete) */
    @Column(name = "deleted_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime deletedAt;

    @Builder
    private PostComment(Post post, Member member, PostComment parentComment, String content) {
        this.post = post;
        this.member = member;
        this.parentComment = parentComment;
        this.content = content;
    }

    // ===== 리치 도메인 메서드 =====

    public void updateContent(String content) {
        this.content = content;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isReply() {
        return this.parentComment != null;
    }
}