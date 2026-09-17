package com.ontheway.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 배송완료 증빙 사진. 명세 3.1 의 "배송완료 확인요청" 시점에 1장만 등록된다.
 *
 * <p>컬럼 하나로 URL 만 들고 있지 않고 엔티티로 분리한 이유는 파일 메타데이터(원본명·저장명·
 * 크기·확장자)를 함께 보관하기 위해서다. {@code unique(order)} 가 1장을 강제한다 (B20).
 *
 * <p>이 행이 없으면 COMPLETION_REQUESTED 로 전이할 수 없다 — 검사는 서비스가 한다.
 *
 * <p><b>테이블명은 {@code file} 인데 클래스명은 {@code Image} 다.</b> 테이블·컬럼 이름은 팀이
 * 확정한 ERD 그대로 두고, 자바 쪽만 {@code java.io.File}·{@code MultipartFile} 과 섞이지 않도록
 * 바꿨다. 업로드 서비스에서 두 타입을 한 파일에 같이 쓰게 되기 때문이다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file",
        uniqueConstraints = @UniqueConstraint(name = "uk_file_order", columnNames = "order_id"))
public class Image extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_file_order"))
    private DeliveryOrder order;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    /** 사용자가 올린 원래 파일명. */
    @Column(nullable = false, length = 255)
    private String originalName;

    /** 스토리지에 저장된 이름. UUID 기반이라 짧다. */
    @Column(nullable = false, length = 100)
    private String storedName;

    /** 바이트 단위. */
    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 20)
    private String extension;

    @Builder
    public Image(DeliveryOrder order, String fileUrl, String originalName,
                 String storedName, Long fileSize, String extension) {
        this.order = order;
        this.fileUrl = fileUrl;
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileSize = fileSize;
        this.extension = extension;
    }
}
