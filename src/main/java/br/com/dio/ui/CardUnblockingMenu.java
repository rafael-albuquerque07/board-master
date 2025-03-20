package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;


import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.CardService;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class CardUnblockingMenu {
    private final BoardEntity entity;
    private final Scanner scanner;

    public CardUnblockingMenu(BoardEntity entity, Scanner scanner) {
        this.entity = entity;
        this.scanner = scanner;
    }

    public void execute() throws SQLException {
        System.out.println("\n" + TerminalColors.BLUE_BOLD + "===== DESBLOQUEAR CARD =====" + TerminalColors.RESET);
        System.out.println("Informe o id do card que será desbloqueado:");
        var cardId = scanner.nextLong();
        System.out.println("Informe o motivo do desbloqueio:");
        var reason = scanner.next();
        try(var connection = getConnection()){
            new CardService(connection).unblock(cardId, reason);
            System.out.println(TerminalColors.success("Card desbloqueado com sucesso!"));
        } catch (RuntimeException ex){
            System.out.println(TerminalColors.error(ex.getMessage()));
        }
    }
}