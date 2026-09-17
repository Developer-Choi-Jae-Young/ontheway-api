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
 * 신고 접수. 명세 4.3.
 *
 * <p>신고 대상은 구체 FK 가 아니라 <b>다형성 참조</b>({@code entityType} + {@code entityId})로
 * 가리킨다. 게시글 신고는 게시글에, 사용자 신고는 사용자에 연결되며 그것이 데이터 항목의
 * "신고 대상 또는 관련 배송 거래"가 뜻하는 필드 하나다 (ERD_REVIEW 1-2).
 *
 * <p>그래서 DB FK 제약을 걸 수 없고 {@code @ManyToOne} 매핑도 못 한다 — 유효성은 서비스가
 * 검증한다. Product/Delivery 가 소프트 삭제라 대상 행이 사라지지 않아 실질 위험은 낮다.
 *
 * <p>다이어그램의 {@code Key4}/{@code Key5} 는 ERDCloud 작도용 부산물이라 여기 만들지 않는다.
 *
 * <p>처리 상태 컬럼도 없다 — MVP 에 신고를 처리하는 단계 자체가 없어 값이 하나로 고정된다(B8).
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report",
        // 대상별 신고 누적 집계
        indexes = @Index(name = "idx_report_entity", columnList = "entity_type, entity_id"))
public class Report extends BaseCreatedEntity {

    /** 한 신고가 고를 수 있는 유형의 최대 개수. 명세 4.3. */
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

    /** 연관관계 매핑이 아니라 그냥 값이다. FK 제약이 없다. */
    @Column(nullable = false)
    private Long entityId;

    /** 4.3 의 자유 입력 텍스트. 유형별이 아니라 신고별 값이다. */
    @Column(length = 1000)
    private String content;

    /**
     * 선택한 신고 유형. 한 신고당 1~3행이 되며 {@code unique(report_id, category)} 가
     * 같은 유형 중복 선택을 막는다.
     *
     * <p>"최대 3개"는 DB 제약으로 표현할 수 없다 — 행 개수 상한을 거는 방법이 없다. 명세 4.3 은
     * "4개 이상 선택 시 최대 3개까지만 선택 가능함을 안내"라는 <b>화면 동작</b>으로 규정하므로
     * 안내는 서비스가 하고, <b>불변식 자체는 생성자가 지킨다</b> (ERD_REVIEW 1-10).
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
     * 읽기 전용으로 돌려준다. 그냥 노출하면 밖에서 {@code getCategories().add(...)} 로
     * 생성자의 상한 검사를 우회할 수 있다.
     */
    public Set<ReportCategory> getCategories() {
        return Collections.unmodifiableSet(categories);
    }
}
