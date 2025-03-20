package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardColumnQueryService;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class ColumnViewMenu {
    private final BoardEntity entity;
    private final Scanner scanner;

    public ColumnViewMenu(BoardEntity entity, Scanner scanner) {
        this.entity = entity;
        this.scanner = scanner;
    }

    public void execute() throws SQLException {
        System.out.println("\n" + TerminalColors.BLUE_BOLD + "===== VISUALIZAÇÃO DE COLUNA =====" + TerminalColors.RESET);
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumnId = -1L;
        while (!columnsIds.contains(selectedColumnId)){
            System.out.println("Escolha uma coluna pelo ID:");
            entity.getBoardColumns().forEach(c ->
                    System.out.printf("  %s - %s [%s]\n", c.getId(), c.getName(), c.getKind()));
            selectedColumnId = scanner.nextLong();

            if (!columnsIds.contains(selectedColumnId)) {
                System.out.println(TerminalColors.error("ID de coluna inválido!"));
            }
        }
        try(var connection = getConnection()){
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(co -> {
                System.out.println(TerminalColors.CYAN + "\nColuna: " + co.getName() +
                        " (Tipo: " + co.getKind() + ")" + TerminalColors.RESET);

                if (co.getCards().isEmpty()) {
                    System.out.println(TerminalColors.YELLOW + "\nNão há cards nesta coluna." +
                            TerminalColors.RESET);
                } else {
                    System.out.println("\nCards:");
                    co.getCards().forEach(ca -> {
                        System.out.println(TerminalColors.GREEN_BOLD +
                                "\n  Card " + ca.getId() + " - " + ca.getTitle() +
                                TerminalColors.RESET);
                        System.out.println("  Descrição: " + ca.getDescription());
                    });
                }
            });
        }
    }
}