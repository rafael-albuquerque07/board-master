package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.ReportService;
import br.com.dio.ui.util.TerminalColors;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class BlockingReportMenu {
    private final BoardEntity entity;
    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public BlockingReportMenu(BoardEntity entity) {
        this.entity = entity;
    }

    public void execute() throws SQLException {
        try (var connection = getConnection()) {
            var reportService = new ReportService(connection);
            var report = reportService.getBlockingReport(entity.getId());

            System.out.println("\n" + TerminalColors.BLUE_BOLD +
                    "===== RELATÓRIO DE BLOQUEIOS DE CARDS =====" + TerminalColors.RESET);
            System.out.println(TerminalColors.CYAN + "Board: " + entity.getName() +
                    " (ID: " + entity.getId() + ")" + TerminalColors.RESET + "\n");

            if (report.isEmpty()) {
                System.out.println(TerminalColors.YELLOW +
                        "Não há registros de bloqueios neste board." + TerminalColors.RESET);
            } else {
                System.out.printf("%-5s | %-20s | %-20s | %-20s | %-15s | %-30s\n",
                        "Card", "Título", "Bloqueado em", "Desbloqueado em", "Duração", "Motivo");
                System.out.println("----------------------------------------------------------------------------------------------------------");

                Long lastCardId = null;
                for (var blocking : report) {
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