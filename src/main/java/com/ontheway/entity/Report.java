package com.ontheway.entity;

import com.ontheway.enums.ReportCategory;
import com.ontheway.enums.ReportEntityType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 신고 접수.
 *
 * 신고 대상을 구체 FK 로 잡지 않고 {@code entityType} + {@code entityId} 두 값으로 가리킨다.
 * 게시글 신고와 사용자 신고가 같은 두 컬럼을 쓴다.
 *
 * 주의: 그래서 DB FK 제약이 없고 {@code @ManyToOne} 매핑도 안 된다. 대상이 실제로 있는지는
 * 서비스가 확인해야 한다.
 *
 * 처리 상태 컬럼은 없다. 신고를 처리하는 단계가 아직 없다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report",
        // 대상별로 신고를 모아 세는 용도
        indexes = @Index(name = "idx_report_entity", columnList = "entity_type, entity_id"))
public class Report extends BaseCreatedEntity {

    /** 한 신고가 고를 수 있는 유형의 최대 개수. */
    public static final int MAX_CATEGORIES = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_report_reporter"))
    private User reporter;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private ReportEntityType entityType;

    /** 연관관계가 아니라 그냥 값이다. FK 제약이 없다. */
    @Column(nullable = false)
    private Long entityId;

    /** 자유 입력 텍스트. 유형별이 아니라 신고별로 하나다. */
    @Column(length = 1000)
    private String content;

    /**
     * 선택한 신고 유형. 한 신고당 1~3행이 되고 {@code unique(report_id, category)} 가 같은
     * 유형을 두 번 고르는 걸 막는다.
     *
     * 주의: {@link #MAX_CATEGORIES} 는 DB 로 막을 수 없다. 행 개수 상한을 거는 방법이 없어서
     * 생성자가 대신 검사한다.
     */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "report_category",
            joinColumns = @JoinColumn(name = "report_id", nullable = false,
                    foreignKey = @ForeignKey(name = "FK_report_category_report")),
            uniqueConstraints = @UniqueConstraint(name = "uk_report_category",
                    columnNames = {"report_id", "category"}))
    @Column(name = "category", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private Set<ReportCategory> categories = EnumSet.noneOf(ReportCategory.class);

    @Builder
    public Report(User reporter, ReportEntityType entityType, Long entityId,
                  String content, Set<ReportCategory> categories) {
        this.reporter = reporter;
        this.entityType = entityType;
        this.entityId = entityId;
        this.content = content;
        this.categories = (categories == null || categories.isEmpty())
                ? EnumSet.noneOf(ReportCategory.class)
                : EnumSet.copyOf(categories);

        if (this.categories.size() > MAX_CATEGORIES) {
            throw new IllegalArgumentException("신고 유형은 최대 %d개까지 선택할 수 있다: %d개"
                    .formatted(MAX_CATEGORIES, this.categories.size()));
        }
    }

    /**
     * 읽기 전용으로 돌려준다. 그냥 내주면 밖에서 {@code getCategories().add(...)} 로
     * 생성자의 상한 검사를 지나칠 수 있다.
     */
    public Set<ReportCategory> getCategories() {
        return Collections.unmodifiableSet(categories);
    }
}
