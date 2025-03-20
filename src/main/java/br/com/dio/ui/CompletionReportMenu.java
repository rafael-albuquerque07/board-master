package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.ReportService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class CompletionReportMenu {
    private final BoardEntity entity;
    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public CompletionReportMenu(BoardEntity entity) {
        this.entity = entity;
    }

    public void execute() throws SQLException {
        try (var connection = getConnection()) {
            var reportService = new ReportService(connection);
            var report = reportService.getCompletionTimeReport(entity.getId());

            System.out.println("\n" + TerminalColors.BLUE_BOLD +
                    "===== RELATÓRIO DE TEMPO DE CONCLUSÃO =====" + TerminalColors.RESET);
            System.out.println(TerminalColors.CYAN + "Board: " + entity.getName() +
                    " (ID: " + entity.getId() + ")" + TerminalColors.RESET + "\n");

            if (report.isEmpty()) {
                System.out.println(TerminalColors.YELLOW +
                        "Não há cards concluídos neste board." + TerminalColors.RESET);
            } else {
                System.out.printf("%-5s | %-30s | %-20s | %-20s | %-25s\n",
                        "ID", "Título", "Início", "Conclusão", "Tempo Total");
                System.out.println("----------------------------------------------------------------------------------");

                for (var cardReport : report) {
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