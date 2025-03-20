package br.com.dio.service.report;

import br.com.dio.dto.CardBlockingReportDTO;
import br.com.dio.ui.util.TerminalColors;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do Strategy para relatórios de bloqueios de cards
 */
public class BlockingReportStrategy implements ReportStrategy<CardBlockingReportDTO> {

    @Override
    public List<CardBlockingReportDTO> generateReport(Long boardId, Connection connection) throws SQLException {
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

    @Override
    public void displayReport(List<CardBlockingReportDTO> reportData) {
        System.out.println("\n" + TerminalColors.BLUE_BOLD +
                "===== RELATÓRIO DE BLOQUEIOS DE CARDS =====" + TerminalColors.RESET);

        if (reportData.isEmpty()) {
            System.out.println(TerminalColors.YELLOW +
                    "Não há registros de bloqueios neste board." + TerminalColors.RESET);
        } else {
            System.out.printf("%-5s | %-20s | %-20s | %-20s | %-15s | %-30s\n",
                    "Card", "Título", "Bloqueado em", "Desbloqueado em", "Duração", "Motivo");
            System.out.println("----------------------------------------------------------------------------------------------------------");

            Long lastCardId = null;
            for (var blocking : reportData) {
                // Adiciona linha em branco entre cards diferentes
                if (lastCardId != null && !lastCardId.equals(blocking.id())) {
                    System.out.println();
                }

                String status = blocking.isStillBlocked() ?
                        TerminalColors.RED + "Ainda Bloqueado" + TerminalColors.RESET :
                        blocking.getFormattedUnblockedAt();

                System.out.printf("%-5d | %-20s | %-20s | %-20s | %-15s | %-30s\n",
                        blocking.id(),
                        truncateString(blocking.title(), 18),
                        blocking.getFormattedBlockedAt(),
                        status,
                        blocking.getFormattedDuration(),
                        truncateString(blocking.blockReason(), 28));

                lastCardId = blocking.id();
            }

            // Adiciona informações estatísticas
            int totalBlocks = reportData.size();
            long openBlocks = reportData.stream().filter(CardBlockingReportDTO::isStillBlocked).count();

            System.out.println("\n" + TerminalColors.CYAN_BOLD + "Estatísticas:" + TerminalColors.RESET);
            System.out.println("Total de bloqueios: " + totalBlocks);
            System.out.println("Bloqueios ainda abertos: " + openBlocks);
            System.out.println("Bloqueios resolvidos: " + (totalBlocks - openBlocks));
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