package br.com.dio.service.report;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.dto.CardCompletionTimeDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do Strategy para relatórios de tempo de conclusão de cards
 */
public class CompletionTimeReportStrategy implements ReportStrategy<CardCompletionTimeDTO> {

    @Override
    public List<CardCompletionTimeDTO> generateReport(Long boardId, Connection connection) throws SQLException {
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

    @Override
    public void displayReport(List<CardCompletionTimeDTO> reportData) {
        System.out.println("\n" + TerminalColors.BLUE_BOLD +
                "===== RELATÓRIO DE TEMPO DE CONCLUSÃO =====" + TerminalColors.RESET);

        if (reportData.isEmpty()) {
            System.out.println(TerminalColors.YELLOW +
                    "Não há cards concluídos neste board." + TerminalColors.RESET);
        } else {
            System.out.printf("%-5s | %-30s | %-20s | %-20s | %-25s\n",
                    "ID", "Título", "Início", "Conclusão", "Tempo Total");
            System.out.println("----------------------------------------------------------------------------------");

            for (var cardReport : reportData) {
                System.out.printf("%-5d | %-30s | %-20s | %-20s | %-25s\n",
                        cardReport.id(),
                        truncateString(cardReport.title(), 28),
                        cardReport.getFormattedStartTime(),
                        cardReport.getFormattedEndTime(),
                        cardReport.getFormattedDuration());
            }
        }

        System.out.println("\n" + TerminalColors.YELLOW +
                "Pressione ENTER para voltar ao menu..." + TerminalColors.RESET);
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignorar exceção
        }
    }

    /**
     * Trunca uma string para um tamanho máximo, adicionando "..." no final se necessário
     *
     * @param str String a ser truncada
     * @param maxLength Tamanho máximo desejado
     * @return String truncada
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}