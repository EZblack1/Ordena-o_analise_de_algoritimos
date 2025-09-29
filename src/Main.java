import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import ordenacao.BubbleSort;
import ordenacao.HeapSort;
import ordenacao.InsertionSort;
import ordenacao.MergeSort;
import ordenacao.QuickSort;
import ordenacao.SelectionSort;

public class Main {

    private static final int[] TAMANHOS = { 100000, 160000, 220000, 280000, 340000, 400000, 460000, 520000, 580000, 640000, 700000 };
    private static final int REPETICOES = 3;
    private static final String BASE_PATH = "src/dados/";
    private static final String[] ARQUIVOS = { "USA-road-d.NY.gr", "USA-road-d.BAY.gr", "USA-road-d.COL.gr" };

    private enum TipoDataset {
        CRESCENTE_REP("Crescente c/ repetição"),
        DECRESCENTE_REP("Decrescente c/ repetição"),
        ALEATORIO_REP("Aleatório c/ repetição"),
        CRESCENTE_SEM("Crescente s/ repetição"),
        DECRESCENTE_SEM("Decrescente s/ repetição"),
        ALEATORIO_SEM("Aleatório s/ repetição");

        final String descricao;
        TipoDataset(String d) { this.descricao = d; }
    }

    private enum Algoritmo {
        BUBBLE, INSERTION, SELECTION, MERGE, QUICK, HEAP
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("=== Avaliação de Produto Vitor ===");
        System.out.println("1 - Execução simples (um algoritmo / um arquivo)");
        System.out.println("2 - Executar benchmark completo e gerar CSVs");
        System.out.println("3 - Retomar benchmark (reconstrói médias e continua)");
        System.out.print("Escolha: ");
        String opcao = sc.nextLine().trim();
        
        try {
            switch (opcao) {
                case "1" -> execucaoSimples(sc);
                case "2" -> executarBenchmarkCompleto();
                case "3" -> retomarBenchmark();
                default -> System.out.println("Opção inválida.");
            }
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        } finally {
            sc.close();
        }
    }

    private static void execucaoSimples(Scanner sc) throws IOException {
        System.out.print("Quantidade de números a ordenar: ");
        int qtd = Integer.parseInt(sc.nextLine().trim());
        System.out.print("Nome do arquivo de entrada (ex: USA-road-d.NY.gr): ");
        String arq = sc.nextLine().trim();
        System.out.print("Nome do arquivo de saída (ex: saida.txt): ");
        String saida = sc.nextLine().trim();
        System.out.println("Algoritmo (BUBBLE, INSERTION, SELECTION, MERGE, QUICK, HEAP): ");
        Algoritmo alg = Algoritmo.valueOf(sc.nextLine().trim().toUpperCase());

        int[] dados = carregarPesos(BASE_PATH + arq, qtd);
        long inicio = System.nanoTime();
        ordenar(alg, dados);
        long fim = System.nanoTime();
        gravarArray(saida, dados);
        System.out.printf(Locale.US, "Tempo: %.3f ms%n", (fim - inicio) / 1_000_000.0);
        System.out.println("Resultado gravado em " + saida);
    }

    private static void executarBenchmarkCompleto() throws IOException {
        System.out.println("Iniciando benchmark...");
        String detalhado = "resultados_detalhados.csv";
        String medias = "resultados_medias.csv";
        long inicioGeral = System.nanoTime();
        long totalEtapas = (long) ARQUIVOS.length * TAMANHOS.length * TipoDataset.values().length * Algoritmo.values().length * REPETICOES;
        long etapasCompletas = 0L;
        
        try (BufferedWriter bwDet = new BufferedWriter(new FileWriter(detalhado));
             BufferedWriter bwMed = new BufferedWriter(new FileWriter(medias))) {
            
            bwDet.write("arquivo;tipoEntrada;tamanho;algoritmo;execucao;tempoMs\n");
            StringBuilder headerMed = new StringBuilder("arquivo;tipoEntrada;tamanho;algoritmo");
            for (int r = 1; r <= REPETICOES; r++) headerMed.append(";tempo").append(r).append("Ms");
            headerMed.append(";mediaMs\n");
            bwMed.write(headerMed.toString());
            
            int maxSize = Arrays.stream(TAMANHOS).max().orElse(0);
            Map<String, int[]> cachePesos = new HashMap<>();
            for (String arq : ARQUIVOS) {
                cachePesos.put(arq, carregarPesos(BASE_PATH + arq, maxSize));
            }
            
            Random rnd = new Random(42);
            int flushDet = 0, flushMed = 0;
            
            for (String arq : ARQUIVOS) {
                int[] base = cachePesos.get(arq);
                for (int tam : TAMANHOS) {
                    if (tam > base.length) continue;
                    int[] sub = Arrays.copyOf(base, tam);
                    Map<TipoDataset, int[]> variantes = gerarVariantes(sub, rnd);
                    
                    for (TipoDataset tipo : TipoDataset.values()) {
                        int[] dataset = variantes.get(tipo);
                        for (Algoritmo alg : Algoritmo.values()) {
                            double soma = 0;
                            StringBuilder linhaMed = new StringBuilder();
                            linhaMed.append(arq).append(';').append(tipo.descricao).append(';').append(tam).append(';').append(alg.name());
                            
                            for (int exec = 1; exec <= REPETICOES; exec++) {
                                int[] copia = Arrays.copyOf(dataset, dataset.length);
                                long ini = System.nanoTime();
                                ordenar(alg, copia);
                                long fim = System.nanoTime();
                                double ms = (fim - ini) / 1_000_000.0;
                                soma += ms;
                                
                                bwDet.write(arq + ";" + tipo.descricao + ";" + tam + ";" + alg.name() + ";" + exec + ";" + String.format(Locale.US, "%.3f", ms) + "\n");
                                linhaMed.append(';').append(String.format(Locale.US, "%.3f", ms));
                                etapasCompletas++;
                                atualizarProgresso(etapasCompletas, totalEtapas, inicioGeral);
                                
                                if (++flushDet % 25 == 0) bwDet.flush();
                            }
                            
                            double media = soma / REPETICOES;
                            linhaMed.append(';').append(String.format(Locale.US, "%.3f", media)).append('\n');
                            bwMed.write(linhaMed.toString());
                            if (++flushMed % 10 == 0) bwMed.flush();
                        }
                    }
                }
            }
        }
        
        gerarTabelasPorTipo();
        System.out.println("\nBenchmark completo! Arquivos gerados:");
        System.out.println("- " + detalhado);
        System.out.println("- " + medias);
        System.out.println("- Tabelas individuais por tipo");
    }

    private static void retomarBenchmark() throws IOException {
        String detalhado = "resultados_detalhados.csv";
        if (!Files.exists(Paths.get(detalhado))) {
            System.out.println("Arquivo " + detalhado + " não encontrado. Execute o benchmark completo primeiro.");
            return;
        }
        
        Map<String, LinkedHashMap<String, Double>> resultados = carregarResultadosDetalhados(detalhado);
        String medias = "resultados_medias.csv";
        
        try (BufferedWriter bwMed = new BufferedWriter(new FileWriter(medias))) {
            StringBuilder headerMed = new StringBuilder("arquivo;tipoEntrada;tamanho;algoritmo");
            for (int r = 1; r <= REPETICOES; r++) headerMed.append(";tempo").append(r).append("Ms");
            headerMed.append(";mediaMs\n");
            bwMed.write(headerMed.toString());
            
            for (Map.Entry<String, LinkedHashMap<String, Double>> entry : resultados.entrySet()) {
                String chave = entry.getKey();
                LinkedHashMap<String, Double> tempos = entry.getValue();
                
                if (tempos.size() == REPETICOES) {
                    StringBuilder linha = new StringBuilder(chave);
                    double soma = 0;
                    for (double tempo : tempos.values()) {
                        linha.append(';').append(String.format(Locale.US, "%.3f", tempo));
                        soma += tempo;
                    }
                    double media = soma / REPETICOES;
                    linha.append(';').append(String.format(Locale.US, "%.3f", media)).append('\n');
                    bwMed.write(linha.toString());
                }
            }
        }
        
        gerarTabelasPorTipo();
        System.out.println("Benchmark retomado! Arquivos atualizados:");
        System.out.println("- " + medias);
        System.out.println("- Tabelas individuais por tipo");
    }

    private static Map<String, LinkedHashMap<String, Double>> carregarResultadosDetalhados(String arquivo) throws IOException {
        Map<String, LinkedHashMap<String, Double>> resultados = new LinkedHashMap<>();
        List<String> linhas = Files.readAllLines(Paths.get(arquivo));
        
        for (int i = 1; i < linhas.size(); i++) {
            String[] partes = linhas.get(i).split(";");
            if (partes.length >= 6) {
                String chave = partes[0] + ";" + partes[1] + ";" + partes[2] + ";" + partes[3];
                int execucao = Integer.parseInt(partes[4]);
                double tempo = Double.parseDouble(partes[5]);
                
                resultados.computeIfAbsent(chave, k -> new LinkedHashMap<>()).put("exec" + execucao, tempo);
            }
        }
        return resultados;
    }

    private static void gerarTabelasPorTipo() throws IOException {
        String medias = "resultados_medias.csv";
        if (!Files.exists(Paths.get(medias))) return;
        
        List<String> linhas = Files.readAllLines(Paths.get(medias));
        if (linhas.size() <= 1) return;
        
        String header = linhas.get(0);
        Map<String, List<String>> tabelasPorTipo = new HashMap<>();
        
        for (int i = 1; i < linhas.size(); i++) {
            String linha = linhas.get(i);
            String[] partes = linha.split(";");
            if (partes.length >= 4) {
                String arquivo = partes[0];
                String tipo = partes[1];
                String chave = arquivo + "_" + tipo.replace(" ", "_").replace("/", "");
                tabelasPorTipo.computeIfAbsent(chave, k -> new ArrayList<>()).add(linha);
            }
        }
        
        for (Map.Entry<String, List<String>> entry : tabelasPorTipo.entrySet()) {
            String nomeTabela = "tabela_" + entry.getKey() + ".csv";
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(nomeTabela))) {
                bw.write(header + "\n");
                for (String linha : entry.getValue()) {
                    bw.write(linha + "\n");
                }
            }
        }
    }

    private static void atualizarProgresso(long completas, long total, long inicio) {
        if (completas % 50 == 0 || completas == total) {
            double progresso = (double) completas / total * 100;
            long tempoDecorrido = (System.nanoTime() - inicio) / 1_000_000_000L;
            long tempoEstimado = completas > 0 ? (tempoDecorrido * total / completas) : 0;
            long tempoRestante = tempoEstimado - tempoDecorrido;
            
            System.out.printf("Progresso: %.1f%% (%d/%d) - Tempo: %ds - Restante: ~%ds%n", 
                progresso, completas, total, tempoDecorrido, Math.max(0, tempoRestante));
        }
    }

    private static int[] carregarPesos(String caminho, int limite) throws IOException {
        List<Integer> lista = new ArrayList<>(limite);
        try (Scanner sc = new Scanner(new File(caminho))) {
            while (sc.hasNextLine() && lista.size() < limite) {
                String linha = sc.nextLine().trim();
                if (linha.isEmpty()) continue;
                
                if (linha.startsWith("a ")) {
                    String[] parts = linha.split(" ");
                    if (parts.length >= 4) {
                        try {
                            int dist = Integer.parseInt(parts[3]);
                            lista.add(dist);
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        
        int[] arr = new int[lista.size()];
        for (int i = 0; i < lista.size(); i++) {
            arr[i] = lista.get(i);
        }
        return arr;
    }

    private static void gravarArray(String arquivoSaida, int[] arr) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(arquivoSaida))) {
            for (int v : arr) {
                bw.write(Integer.toString(v));
                bw.newLine();
            }
        }
    }

    private static Map<TipoDataset, int[]> gerarVariantes(int[] base, Random rnd) {
        Map<TipoDataset, int[]> map = new java.util.EnumMap<>(TipoDataset.class);

        int[] ascRep = Arrays.copyOf(base, base.length);
        Arrays.sort(ascRep);
        map.put(TipoDataset.CRESCENTE_REP, ascRep);

        int[] descRep = Arrays.copyOf(ascRep, ascRep.length);
        inverter(descRep);
        map.put(TipoDataset.DECRESCENTE_REP, descRep);

        int[] randRep = Arrays.copyOf(ascRep, ascRep.length);
        embaralhar(randRep, rnd);
        map.put(TipoDataset.ALEATORIO_REP, randRep);

        int[] ascSem = gerarSemRepeticao(ascRep);
        map.put(TipoDataset.CRESCENTE_SEM, ascSem);

        int[] descSem = Arrays.copyOf(ascSem, ascSem.length);
        inverter(descSem);
        map.put(TipoDataset.DECRESCENTE_SEM, descSem);

        int[] randSem = Arrays.copyOf(ascSem, ascSem.length);
        embaralhar(randSem, rnd);
        map.put(TipoDataset.ALEATORIO_SEM, randSem);
        
        return map;
    }

    private static int[] gerarSemRepeticao(int[] arr) {
        LinkedHashSet<Integer> set = new LinkedHashSet<>();
        for (int v : arr) {
            set.add(v);
        }
        return set.stream().mapToInt(Integer::intValue).toArray();
    }

    private static void inverter(int[] arr) {
        for (int i = 0; i < arr.length / 2; i++) {
            int temp = arr[i];
            arr[i] = arr[arr.length - 1 - i];
            arr[arr.length - 1 - i] = temp;
        }
    }

    private static void embaralhar(int[] arr, Random rnd) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    private static void ordenar(Algoritmo alg, int[] arr) {
        switch (alg) {
            case BUBBLE -> BubbleSort.ordenar(arr);
            case INSERTION -> InsertionSort.ordenar(arr);
            case SELECTION -> SelectionSort.ordenar(arr);
            case MERGE -> MergeSort.ordenar(arr);
            case QUICK -> QuickSort.ordenar(arr);
            case HEAP -> HeapSort.ordenar(arr);
        }
    }
}