package br.com.dio.dto;

import java.time.Duration;
import java.util.List;

/**
 * DTO que representa uma timeline detalhada de um card
 */
public record CardDetailedTimelineDTO(
        Long cardId,
        String cardTitle,
        CardCompletionTimeDTO completionTime,
        List<ColumnTimeDTO> columnTimes,
        List<CardBlockingReportDTO> blockingHistory
) {
    /**
     * Verifica se o card foi concluído
     * @return true se o card já tiver sido concluído
     */
    public boolean isCompleted() {
        return completionTime != null;
    }

    /**
     * Retorna o número total de bloqueios do card
     * @return quantidade de bloqueios
     */
    public int getTotalBlockCount() {
        return blockingHistory.size();
    }

    /**
     * Calcula o tempo total de bloqueio do card
     * @return Duration com o tempo total de bloqueio
     */
    public Duration getTotalBlockingTime() {
        return blockingHistory.stream()
                .map(CardBlockingReportDTO::blockedTime)
                .reduce(Duration.ZERO, Duration::plus);
    }
}