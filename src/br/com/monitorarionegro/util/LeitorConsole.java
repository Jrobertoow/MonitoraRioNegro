package br.com.monitorarionegro.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class LeitorConsole {
    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Scanner scanner;

    public LeitorConsole() {
        this.scanner = new Scanner(System.in);
    }

    public String lerTextoObrigatorio(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String valor = scanner.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("O campo é obrigatório.");
        }
    }

    public String lerTextoOpcional(String mensagem) {
        System.out.print(mensagem);
        return scanner.nextLine().trim();
    }

    public int lerInt(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String valor = scanner.nextLine().trim();
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    public int lerIntMinimo(String mensagem, int minimo) {
        while (true) {
            int valor = lerInt(mensagem);
            if (valor >= minimo) {
                return valor;
            }
            System.out.println("O valor mínimo permitido é " + minimo + ".");
        }
    }

    public double lerDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            String valor = scanner.nextLine().trim().replace(",", ".");
            try {
                return Double.parseDouble(valor);
            } catch (NumberFormatException erro) {
                System.out.println("Digite um número válido, como 28,20.");
            }
        }
    }

    public LocalDate lerData(String mensagem) {
        while (true) {
            System.out.print(mensagem + " (dd/MM/aaaa): ");
            String valor = scanner.nextLine().trim();
            try {
                return LocalDate.parse(valor, FORMATO_DATA);
            } catch (DateTimeParseException erro) {
                System.out.println("Data inválida. Exemplo: 23/07/2026.");
            }
        }
    }

    public boolean lerSimNao(String mensagem) {
        while (true) {
            System.out.print(mensagem + " (S/N): ");
            String valor = scanner.nextLine().trim();
            if (valor.equalsIgnoreCase("S")) {
                return true;
            }
            if (valor.equalsIgnoreCase("N")) {
                return false;
            }
            System.out.println("Digite S para sim ou N para não.");
        }
    }

    public void pausar() {
        System.out.print("\nPressione ENTER para continuar...");
        scanner.nextLine();
    }

    public void fechar() {
        scanner.close();
    }
}
