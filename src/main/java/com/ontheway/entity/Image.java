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
 * 배송완료 증빙 사진. 확인요청할 때 한 장 올라가고 {@code unique(order)} 가 한 장을 강제한다.
 * 이 행이 없으면 확인요청 상태로 넘어갈 수 없다(검사는 서비스가 한다).
 *
 * URL 한 칸이 아니라 엔티티로 뺀 건 원본명, 저장명, 크기, 확장자를 같이 들고 있기 위해서다.
 *
 * 주의: 테이블명은 {@code file} 인데 클래스명은 {@code Image} 다. 테이블과 컬럼 이름은 팀이
 * 정한 그대로 두고, 자바 쪽만 {@code java.io.File}, {@code MultipartFile} 과 헷갈리지 않게 바꿨다.
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "file",
        uniqueConstraints = @UniqueConstraint(name = "uk_file_order", columnNames = "order_id"))
public class Image extends BaseCreatedEntity {

    /** 컬럼 크기. 업로드(OrderService)가 원본 파일명을 이 길이로 자른다. */
    public static final int ORIGINAL_NAME_MAX_LENGTH = 255;

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
    @Column(nullable = false, length = ORIGINAL_NAME_MAX_LENGTH)
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
