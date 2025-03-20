package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.service.CardQueryService;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class CardViewMenu {
    private final Scanner scanner;

    public CardViewMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void execute() throws SQLException {
        System.out.println("\n" + TerminalColors.BLUE_BOLD + "===== VISUALIZAÇÃO DE CARD =====" + TerminalColors.RESET);
        System.out.println("Informe o id do card que deseja visualizar:");
        var selectedCardId = scanner.nextLong();
        try(var connection = getConnection()){
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                System.out.println(TerminalColors.GREEN_BOLD +
                                        "Card " + c.id() + " - " + c.title() +
                                        TerminalColors.RESET);
                                System.out.println("Descrição: " + c.description());

                                if (c.blocked()) {
                                    System.out.println(TerminalColors.RED +
                                            "Status: BLOQUEADO" +
                                            TerminalColors.RESET);
                                    System.out.println("Motivo: " + c.blockReason());
                                } else {
                                    System.out.println(TerminalColors.GREEN +
                                            "Status: DESBLOQUEADO" +
                                            TerminalColors.RESET);
                                }

                                System.out.println("Histórico de bloqueios: " + c.blocksAmount() + " vez(es)");
                                System.out.println("Coluna atual: " + c.columnName() + " (ID: " + c.columnId() + ")");
                            },
                            () -> System.out.println(TerminalColors.error(
                                    "Não existe um card com o id " + selectedCardId))
                    );
        }
    }
}