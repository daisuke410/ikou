package com.example.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 進捗情報を通知するためのメッセージDTO。
 *
 * @author Spring Batch Data Migration Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressMessage {
    private Long executionId;
    private String stepName;
    private String status;
    private Long readCount;
    private Long writeCount;
    private Long skipCount;
    private Double progressPercentage;
    private Double readSpeed;
    private Double writeSpeed;
    private Long elapsedSeconds;
    private LocalDateTime timestamp;
    private String message;
}
