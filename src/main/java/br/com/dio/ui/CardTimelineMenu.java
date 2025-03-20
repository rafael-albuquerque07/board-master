package br.com.dio.ui;

import br.com.dio.ui.util.TerminalColors;
import br.com.dio.dto.CardBlockingReportDTO;
import br.com.dio.dto.ColumnTimeDTO;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.ReportService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class CardTimelineMenu {
    private final BoardEntity entity;
    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public CardTimelineMenu(BoardEntity entity) {
        this.entity = entity;
    }

    public void execute() throws SQLException {
        System.out.println("\nInforme o ID do card para visualizar a timeline detalhada: ");
        Long cardId = scanner.nextLong();

        try (var connection = getConnection()) {
            var reportService = new ReportService(connection);
            var timeline = reportService.getCardDetailedTimeline(cardId);

            if (timeline == null) {
                System.out.println(TerminalColors.RED +
                        "Card não encontrado ou não pertence a este board." + TerminalColors.RESET);
                return;
            }

            System.out.println("\n" + TerminalColors.BLUE_BOLD +
                    "===== TIMELINE DETALHADA DO CARD =====" + TerminalColors.RESET);
            System.out.println(TerminalColors.CYAN + "Card: " + timeline.cardTitle() +
                    " (ID: " + timeline.cardId() + ")" + TerminalColors.RESET);

            // Status do card
            if (timeline.isCompleted()) {
                System.out.println("\n" + TerminalColors.GREEN + "Status: CONCLUÍDO" + TerminalColors.RESET);
                System.out.println("Data de início: " + timeline.completionTime().getFormattedStartTime());
                System.out.println("Data de conclusão: " + timeline.completionTime().getFormattedEndTime());
                System.out.println("Tempo total: " + timeline.completionTime().getFormattedDuration());
            } else {
                System.out.println("\n" + TerminalColors.YELLOW + "Status: EM ANDAMENTO" + TerminalColors.RESET);
            }

            // Tempo por coluna
            System.out.println("\n" + TerminalColors.PURPLE_BOLD + "TEMPO POR COLUNA:" + TerminalColors.RESET);
            if (timeline.columnTimes().isEmpty()) {
                System.out.println("Não há registros de movimentação entre colunas.");
            } else {
                System.out.printf("%-25s | %-20s\n", "Coluna", "Tempo Gasto");
                System.out.println("---------------------------------------");

                for (ColumnTimeDTO columnTime : timeline.columnTimes()) {
                    System.out.printf("%-25s | %-20s\n",
                            columnTime.columnName(),
                            columnTime.getFormattedDuration());
                }
            }

            // Histórico de bloqueios
            System.out.println("\n" + TerminalColors.PURPLE_BOLD + "HISTÓRICO DE BLOQUEIOS:" + TerminalColors.RESET);
            if (timeline.blockingHistory().isEmpty()) {
                System.out.println("O card nunca foi bloqueado.");
            } else {
                System.out.printf("%-20s | %-20s | %-15s | %-30s | %-30s\n",
                        "Bloqueado em", "Desbloqueado em", "Duração", "Motivo Bloqueio", "Motivo Desbloqueio");
                System.out.println("-----------------------------------------------------------------------------------------------------------------");

                for (CardBlockingReportDTO blocking : timeline.blockingHistory()) {
                    String status = blocking.isStillBlocked() ?
                            TerminalColors.RED + "Ainda Bloqueado" + TerminalColors.RESET :
                            blocking.getFormattedUnblockedAt();

                    System.out.printf("%-20s | %-20s | %-15s | %-30s | %-30s\n",
                            blocking.getFormattedBlockedAt(),
                            status,
                            blocking.getFormattedDuration(),
                            truncateString(blocking.blockReason(), 28),
                            truncateString(blocking.unblockReason(), 28));
                }

                // Total de bloqueios
                System.out.println("\nTotal de vezes que o card foi bloqueado: " + timeline.blockingHistory().size());
            }

            System.out.println("\n" + TerminalColors.YELLOW +
                    "Pressione ENTER para voltar ao menu..." + TerminalColors.RESET);
            try {
                System.in.read();
            } catch (IOException e) {
                // Ignorar exceção
            }
        }
    }

    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength - 3) + "..." : str;
    }
}