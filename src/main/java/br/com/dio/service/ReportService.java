package br.com.dio.service;

import br.com.dio.dto.CardBlockingReportDTO;
import br.com.dio.dto.CardCompletionTimeDTO;
import br.com.dio.dto.CardDetailedTimelineDTO;
import br.com.dio.dto.CardHistoryDTO;
import br.com.dio.dto.ColumnTimeDTO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ReportService {

    private final Connection connection;

    /**
     * Gera um relatório com o tempo que cada card demorou para ser concluído
     *
     * @param boardId ID do board para gerar o relatório
     * @return Lista de DTOs com informações de tempo de conclusão
     * @throws SQLException se houver erro no acesso ao banco
     */
    public List<CardCompletionTimeDTO> getCompletionTimeReport(Long boardId) throws SQLException {
        List<CardCompletionTimeDTO> report = new ArrayList<>();

        // Usar a primeira entrada do histórico como data de criação
        String sql = """
            SELECT c.id, c.title, 
                   (SELECT MIN(ch.moved_at) 
                    FROM CARD_HISTORY ch 
                    WHERE ch.card_id = c.id AND ch.from_column_id IS NULL) AS start_time,
                   (SELECT MIN(ch.moved_at) 
                    FROM CARD_HISTORY ch 
                    INNER JOIN BOARDS_COLUMNS bc ON ch.to_column_id = bc.id 
                    WHERE ch.card_id = c.id AND bc.kind = 'FINAL') AS end_time
            FROM CARDS c
            INNER JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id
            WHERE bc.board_id = ?
            HAVING start_time IS NOT NULL AND end_time IS NOT NULL
            ORDER BY TIMESTAMPDIFF(MINUTE, start_time, end_time) DESC;
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                LocalDateTime startTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = resultSet.getTimestamp("end_time").toLocalDateTime();
                Duration duration = Duration.between(startTime, endTime);

                CardCompletionTimeDTO dto = new CardCompletionTimeDTO(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        startTime,
                        endTime,
                        duration
                );
                report.add(dto);
            }
        }

        return report;
    }

    /**
     * Gera um relatório com os bloqueios de cada card
     *
     * @param boardId ID do board para gerar o relatório
     * @return Lista de DTOs com informações de bloqueios
     * @throws SQLException se houver erro no acesso ao banco
     */
    public List<CardBlockingReportDTO> getBlockingReport(Long boardId) throws SQLException {
        List<CardBlockingReportDTO> report = new ArrayList<>();

        String sql = """
            SELECT c.id, c.title, 
                   b.blocked_at, b.block_reason,
                   b.unblocked_at, b.unblock_reason
            FROM CARDS c
            INNER JOIN BLOCKS b ON c.id = b.card_id
            INNER JOIN BOARDS_COLUMNS bc ON c.board_column_id = bc.id
            WHERE bc.board_id = ?
            ORDER BY c.id, b.blocked_at;
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                LocalDateTime blockedAt = resultSet.getTimestamp("blocked_at").toLocalDateTime();
                LocalDateTime unblockedAt = null;

                if (resultSet.getTimestamp("unblocked_at") != null) {
                    unblockedAt = resultSet.getTimestamp("unblocked_at").toLocalDateTime();
                }

                Duration blockedTime = unblockedAt != null ?
                        Duration.between(blockedAt, unblockedAt) :
                        Duration.between(blockedAt, LocalDateTime.now());

                CardBlockingReportDTO dto = new CardBlockingReportDTO(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        blockedAt,
                        resultSet.getString("block_reason"),
                        unblockedAt,
                        resultSet.getString("unblock_reason"),
                        blockedTime
                );
                report.add(dto);
            }
        }

        return report;
    }

    /**
     * Obtém o histórico de movimentação de um card entre colunas
     *
     * @param cardId ID do card para buscar o histórico
     * @return Lista de movimentações do card
     * @throws SQLException se houver erro no acesso ao banco
     */
    public List<CardHistoryDTO> getCardMovementHistory(Long cardId) throws SQLException {
        List<CardHistoryDTO> history = new ArrayList<>();

        String sql = """
            SELECT ch.id, ch.card_id, c.title AS card_title,
                   ch.from_column_id, bc_from.name AS from_column_name,
                   ch.to_column_id, bc_to.name AS to_column_name,
                   ch.moved_at
            FROM CARD_HISTORY ch
            INNER JOIN CARDS c ON ch.card_id = c.id
            LEFT JOIN BOARDS_COLUMNS bc_from ON ch.from_column_id = bc_from.id
            INNER JOIN BOARDS_COLUMNS bc_to ON ch.to_column_id = bc_to.id
            WHERE ch.card_id = ?
            ORDER BY ch.moved_at;
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CardHistoryDTO dto = new CardHistoryDTO(
                        resultSet.getLong("id"),
                        resultSet.getLong("card_id"),
                        resultSet.getString("card_title"),
                        resultSet.getObject("from_column_id") != null ? resultSet.getLong("from_column_id") : null,
                        resultSet.getString("from_column_name"),
                        resultSet.getLong("to_column_id"),
                        resultSet.getString("to_column_name"),
                        resultSet.getTimestamp("moved_at").toLocalDateTime()
                );
                history.add(dto);
            }
        }

        return history;
    }

    /**
     * Calcula o tempo que um card passou em cada coluna
     *
     * @param cardId ID do card para análise
     * @return Lista de DTOs com tempo em cada coluna
     * @throws SQLException se houver erro no acesso ao banco
     */
    public List<ColumnTimeDTO> getTimePerColumn(Long cardId) throws SQLException {
        // Primeiro, obtenha o histórico completo de movimentação do card
        List<CardHistoryDTO> history = getCardMovementHistory(cardId);

        if (history.isEmpty()) {
            return List.of();
        }

        // Mapa para armazenar o tempo por coluna
        Map<Long, ColumnTimeDTO> timePerColumn = new HashMap<>();

        // Informações sobre a coluna atual
        String sql = "SELECT bc.id, bc.name FROM BOARDS_COLUMNS bc INNER JOIN CARDS c ON bc.id = c.board_column_id WHERE c.id = ?";

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Long currentColumnId = resultSet.getLong("id");
                String currentColumnName = resultSet.getString("name");

                // Processa o histórico para calcular o tempo em cada coluna
                LocalDateTime lastMovementTime = null;
                Long lastColumnId = null;

                for (int i = 0; i < history.size(); i++) {
                    CardHistoryDTO movement = history.get(i);

                    if (i > 0) {
                        // Calcula o tempo na coluna anterior
                        Duration timeSpent = Duration.between(lastMovementTime, movement.movedAt());

                        if (timePerColumn.containsKey(lastColumnId)) {
                            ColumnTimeDTO existing = timePerColumn.get(lastColumnId);
                            Duration newTotal = existing.timeSpent().plus(timeSpent);
                            timePerColumn.put(lastColumnId, new ColumnTimeDTO(
                                    existing.columnId(),
                                    existing.columnName(),
                                    newTotal
                            ));
                        } else {
                            // Busque o nome da coluna
                            String columnName = "";
                            for (CardHistoryDTO m : history) {
                                if (m.toColumnId().equals(lastColumnId)) {
                                    columnName = m.toColumnName();
                                    break;
                                }
                            }

                            timePerColumn.put(lastColumnId, new ColumnTimeDTO(
                                    lastColumnId,
                                    columnName,
                                    timeSpent
                            ));
                        }
                    }

                    lastMovementTime = movement.movedAt();
                    lastColumnId = movement.toColumnId();
                }

                // Adiciona o tempo na coluna atual
                if (lastMovementTime != null) {
                    Duration timeInCurrentColumn = Duration.between(lastMovementTime, LocalDateTime.now());

                    if (lastColumnId.equals(currentColumnId)) {
                        // Ainda está na última coluna do histórico
                        if (timePerColumn.containsKey(currentColumnId)) {
                            ColumnTimeDTO existing = timePerColumn.get(currentColumnId);
                            Duration newTotal = existing.timeSpent().plus(timeInCurrentColumn);
                            timePerColumn.put(currentColumnId, new ColumnTimeDTO(
                                    existing.columnId(),
                                    existing.columnName(),
                                    newTotal
                            ));
                        } else {
                            timePerColumn.put(currentColumnId, new ColumnTimeDTO(
                                    currentColumnId,
                                    currentColumnName,
                                    timeInCurrentColumn
                            ));
                        }
                    } else {
                        // Mudou para uma nova coluna que ainda não está no histórico
                        timePerColumn.put(currentColumnId, new ColumnTimeDTO(
                                currentColumnId,
                                currentColumnName,
                                timeInCurrentColumn
                        ));
                    }
                }
            }
        }

        // Converte o mapa para lista e ordena por ID da coluna (geralmente reflete a ordem no board)
        return timePerColumn.values().stream()
                .sorted((c1, c2) -> c1.columnId().compareTo(c2.columnId()))
                .collect(Collectors.toList());
    }

    /**
     * Gera um relatório detalhado sobre um card específico
     *
     * @param cardId ID do card para análise
     * @return DTO com timeline detalhada do card
     * @throws SQLException se houver erro no acesso ao banco
     */
    public CardDetailedTimelineDTO getCardDetailedTimeline(Long cardId) throws SQLException {
        String cardInfoSql = "SELECT c.id, c.title FROM CARDS c WHERE c.id = ?";

        try (var statement = connection.prepareStatement(cardInfoSql)) {
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String title = resultSet.getString("title");

                // Obtém o histórico de bloqueios
                List<CardBlockingReportDTO> blockingHistory = getCardBlockings(cardId);

                // Obtém o tempo por coluna
                List<ColumnTimeDTO> columnTimes = getTimePerColumn(cardId);

                // Verifica se o card já foi concluído
                CardCompletionTimeDTO completionTime = getCardCompletionTime(cardId);

                return new CardDetailedTimelineDTO(
                        id,
                        title,
                        completionTime,
                        columnTimes,
                        blockingHistory
                );
            }
        }

        return null;
    }

    /**
     * Obtém o tempo de conclusão de um card específico, se já foi concluído
     *
     * @param cardId ID do card
     * @return DTO com informações de conclusão ou null se não concluído
     * @throws SQLException se houver erro no acesso ao banco
     */
    private CardCompletionTimeDTO getCardCompletionTime(Long cardId) throws SQLException {
        String sql = """
            SELECT c.title, 
                   (SELECT MIN(ch.moved_at) 
                    FROM CARD_HISTORY ch 
                    WHERE ch.card_id = c.id AND ch.from_column_id IS NULL) AS start_time,
                   (SELECT MIN(ch.moved_at) 
                    FROM CARD_HISTORY ch 
                    INNER JOIN BOARDS_COLUMNS bc ON ch.to_column_id = bc.id 
                    WHERE ch.card_id = c.id AND bc.kind = 'FINAL') AS end_time
            FROM CARDS c
            WHERE c.id = ?
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();

            if (resultSet.next() && resultSet.getTimestamp("end_time") != null) {
                LocalDateTime startTime = resultSet.getTimestamp("start_time").toLocalDateTime();
                LocalDateTime endTime = resultSet.getTimestamp("end_time").toLocalDateTime();
                Duration duration = Duration.between(startTime, endTime);

                return new CardCompletionTimeDTO(
                        cardId,
                        resultSet.getString("title"),
                        startTime,
                        endTime,
                        duration
                );
            }
        }

        return null;
    }

    /**
     * Obtém o histórico de bloqueios de um card específico
     *
     * @param cardId ID do card
     * @return Lista de bloqueios do card
     * @throws SQLException se houver erro no acesso ao banco
     */
    private List<CardBlockingReportDTO> getCardBlockings(Long cardId) throws SQLException {
        List<CardBlockingReportDTO> blockings = new ArrayList<>();

        String sql = """
            SELECT c.title, b.blocked_at, b.block_reason, b.unblocked_at, b.unblock_reason
            FROM BLOCKS b
            INNER JOIN CARDS c ON b.card_id = c.id
            WHERE c.id = ?
            ORDER BY b.blocked_at;
        """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cardId);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                LocalDateTime blockedAt = resultSet.getTimestamp("blocked_at").toLocalDateTime();
                LocalDateTime unblockedAt = null;

                if (resultSet.getTimestamp("unblocked_at") != null) {
                    unblockedAt = resultSet.getTimestamp("unblocked_at").toLocalDateTime();
                }

                Duration blockedTime = unblockedAt != null ?
                        Duration.between(blockedAt, unblockedAt) :
                        Duration.between(blockedAt, LocalDateTime.now());

                CardBlockingReportDTO dto = new CardBlockingReportDTO(
                        cardId,
                        resultSet.getString("title"),
                        blockedAt,
                        resultSet.getString("block_reason"),
                        unblockedAt,
                        resultSet.getString("unblock_reason"),
                        blockedTime
                );
                blockings.add(dto);
            }
        }

        return blockings;
    }
}