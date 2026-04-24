import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.print.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LottoGen Pro - Versão Final Premium
 * Desenvolvido por Mr. Magic Programmin 2025
 * Recursos: Splash c/ Progresso, PDF/Print, Binário Auto-Path e Stats.
 */
public class LottoGen extends JFrame {

    private JTextField txtNumerosBase, txtUsuario;
    private JTextArea txtAreaResultado, txtAreaStats;
    private JComboBox<String> comboLoteria;
    private List<Integer> historicoGlobal = new ArrayList<>();
    private boolean arquivoSalvo = true;

    public LottoGen() {
        configurarUI();
    }

    private void configurarUI() {
        setTitle("LottoGen Pro - Sistema de Combinações Avançado");
        setSize(1000, 750);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) { confirmarSaida(); }
        });

        // Painel Superior
        JPanel pnlConfig = new JPanel(new GridLayout(3, 2, 10, 10));
        pnlConfig.setBorder(BorderFactory.createTitledBorder("Configurações do Usuário"));
        pnlConfig.add(new JLabel(" Seu Nome:"));
        txtUsuario = new JTextField();
        pnlConfig.add(txtUsuario);
        pnlConfig.add(new JLabel(" Números Base (Ex: 3, 5, 7):"));
        txtNumerosBase = new JTextField("3, 5, 7");
        pnlConfig.add(txtNumerosBase);
        pnlConfig.add(new JLabel(" Escolha a Loteria:"));
        comboLoteria = new JComboBox<>(new String[]{"Mega-Sena", "Quina", "LotoFácil", "Sena"});
        pnlConfig.add(comboLoteria);
        add(pnlConfig, BorderLayout.NORTH);

        // Painel Central
        JPanel pnlCentro = new JPanel(new BorderLayout(5, 5));
        JPanel pnlBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnGerarUnico = new JButton("Gerar Selecionado");
        JButton btnGerarTodos = new JButton("Gerar Todas Loterias");
        JButton btnLimpar = new JButton("Limpar");
        btnGerarTodos.setBackground(new Color(39, 174, 96));
        btnGerarTodos.setForeground(Color.WHITE);
        pnlBotoes.add(btnGerarUnico);
        pnlBotoes.add(btnGerarTodos);
        pnlBotoes.add(btnLimpar);

        txtAreaResultado = new JTextArea();
        txtAreaResultado.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtAreaResultado.setBorder(BorderFactory.createTitledBorder("Resultados dos Jogos"));
        pnlCentro.add(pnlBotoes, BorderLayout.NORTH);
        pnlCentro.add(new JScrollPane(txtAreaResultado), BorderLayout.CENTER);
        add(pnlCentro, BorderLayout.CENTER);

        // Painel Lateral (Estatísticas)
        txtAreaStats = new JTextArea(10, 22);
        txtAreaStats.setEditable(false);
        txtAreaStats.setBackground(new Color(245, 245, 245));
        txtAreaStats.setBorder(BorderFactory.createTitledBorder("Frequência"));
        add(new JScrollPane(txtAreaStats), BorderLayout.EAST);

        // Painel Inferior: Rodapé
        JPanel pnlRodape = new JPanel(new BorderLayout());
        pnlRodape.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel lblCopyright = new JLabel("© Mr. Magic Programmin 2025");
        lblCopyright.setFont(new Font("SansSerif", Font.ITALIC, 11));

        JPanel pnlAcoesRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnImprimir = new JButton("Imprimir / Salvar PDF");
        JButton btnAbrirBin = new JButton("Abrir .bin");
        JButton btnSair = new JButton("Sair");
        btnSair.setBackground(new Color(192, 57, 43));
        btnSair.setForeground(Color.WHITE);
        btnImprimir.setBackground(new Color(52, 152, 219));
        btnImprimir.setForeground(Color.WHITE);
        
        pnlAcoesRodape.add(btnImprimir);
        pnlAcoesRodape.add(btnAbrirBin);
        pnlAcoesRodape.add(btnSair);

        pnlRodape.add(lblCopyright, BorderLayout.WEST);
        pnlRodape.add(pnlAcoesRodape, BorderLayout.EAST);
        add(pnlRodape, BorderLayout.SOUTH);

        // Listeners
        btnGerarUnico.addActionListener(e -> processarJogos(false));
        btnGerarTodos.addActionListener(e -> processarJogos(true));
        btnLimpar.addActionListener(e -> { txtAreaResultado.setText(""); historicoGlobal.clear(); atualizarStats(); arquivoSalvo = true; });
        btnAbrirBin.addActionListener(e -> lerArquivoBinario());
        btnSair.addActionListener(e -> confirmarSaida());
        btnImprimir.addActionListener(e -> imprimirDocumento());
    }

    private void imprimirDocumento() {
        if (txtAreaResultado.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Gere um jogo antes de imprimir!");
            return;
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            int y = 20;
            for (String line : txtAreaResultado.getText().split("\n")) {
                g2d.drawString(line, 50, y);
                y += 15;
            }
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); } catch (PrinterException ex) { ex.printStackTrace(); }
        }
    }

    private void lerArquivoBinario() {
        // Define a pasta onde o programa está rodando como diretório inicial
        JFileChooser chooser = new JFileChooser(new File(".")); 
        chooser.setFileFilter(new FileNameExtensionFilter("Arquivos BIN (.bin)", "bin"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile()))) {
                txtAreaResultado.setText("--- CONTEÚDO BINÁRIO RECUPERADO ---\n" + in.readObject());
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Erro na leitura."); }
        }
    }

    private void processarJogos(boolean todos) {
        try {
            String nome = txtUsuario.getText().trim();
            if (nome.isEmpty()) throw new Exception("Informe seu nome.");
            List<Integer> base = Arrays.stream(txtNumerosBase.getText().split(",")).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList());
            StringBuilder sb = new StringBuilder("=== RELATÓRIO MR. MAGIC ===\n");
            sb.append("Proprietário: ").append(nome).append("\n\n");
            if (todos) {
                adicionarJogo(sb, "Mega-Sena", gerarLogica(base, 6, 60));
                adicionarJogo(sb, "Quina", gerarLogica(base, 5, 80));
                adicionarJogo(sb, "LotoFácil", gerarLogica(base, 15, 25));
                adicionarJogo(sb, "Sena", gerarLogica(base, 6, 50));
            } else {
                int idx = comboLoteria.getSelectedIndex();
                int q = (idx == 0) ? 6 : (idx == 1) ? 5 : (idx == 2) ? 15 : 6;
                int l = (idx == 0) ? 60 : (idx == 1) ? 80 : (idx == 2) ? 25 : 50;
                adicionarJogo(sb, comboLoteria.getSelectedItem().toString(), gerarLogica(base, q, l));
            }
            txtAreaResultado.setText(sb.toString());
            salvarBinario(nome, sb.toString());
            atualizarStats();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage()); }
    }

    private void adicionarJogo(StringBuilder sb, String nome, List<Integer> jogo) {
        sb.append(nome).append(": ").append(jogo).append("\n");
        historicoGlobal.addAll(jogo);
        arquivoSalvo = false;
    }

    private List<Integer> gerarLogica(List<Integer> base, int qtd, int lim) {
        Set<Integer> res = new TreeSet<>();
        for (int i : base) {
            for (int j : base) {
                res.add(Math.min(lim, Math.max(1, i + j)));
                res.add(Math.min(lim, Math.max(1, Math.abs(i - j))));
                res.add(Math.min(lim, Math.max(1, i * j)));
            }
        }
        int filler = 1; while (res.size() < qtd) res.add(filler++);
        List<Integer> list = new ArrayList<>(res); Collections.shuffle(list);
        list = list.subList(0, qtd); Collections.sort(list);
        return list;
    }

    private void atualizarStats() {
        if (historicoGlobal.isEmpty()) { txtAreaStats.setText(""); return; }
        Map<Integer, Long> freq = historicoGlobal.stream().collect(Collectors.groupingBy(n -> n, Collectors.counting()));
        StringBuilder sb = new StringBuilder("FREQUÊNCIA:\n\n");
        freq.entrySet().stream().sorted(Map.Entry.<Integer, Long>comparingByValue().reversed()).forEach(e -> sb.append("Nº ").append(e.getKey()).append(": ").append(e.getValue()).append("x\n"));
        txtAreaStats.setText(sb.toString());
    }

    private void salvarBinario(String usuario, String conteudo) {
        String dataStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        String arqNome = usuario.replaceAll("\\s+", "_") + "_" + dataStr + ".bin";
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(arqNome))) {
            out.writeObject(conteudo); arquivoSalvo = true;
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void confirmarSaida() {
        if (!arquivoSalvo) {
            if (JOptionPane.showConfirmDialog(this, "Dados não salvos detectados. Sair mesmo assim?", "Aviso", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        int sw = 500, sh = 262;
        JWindow splash = new JWindow();
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(Color.WHITE);
        File imgFile = new File("logo.png");
        if (imgFile.exists()) {
            pnl.add(new JLabel(new ImageIcon(new ImageIcon("logo.png").getImage().getScaledInstance(sw, sh, Image.SCALE_SMOOTH))), BorderLayout.CENTER);
        } else {
            pnl.add(new JLabel("SISTEMA LOTERIA PRO", SwingConstants.CENTER), BorderLayout.CENTER);
        }
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        JPanel pnlBot = new JPanel(new BorderLayout());
        pnlBot.add(progress, BorderLayout.CENTER);
        pnlBot.add(new JLabel("© Mr. Magic Programmin 2025  ", SwingConstants.RIGHT), BorderLayout.SOUTH);
        pnl.add(pnlBot, BorderLayout.SOUTH);
        splash.setContentPane(pnl);
        splash.setSize(sw, sh + 45);
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);
        for (int i = 0; i <= 100; i++) {
            final int p = i; SwingUtilities.invokeLater(() -> progress.setValue(p));
            try { Thread.sleep(25); } catch (Exception e) {}
        }
        splash.dispose();
        SwingUtilities.invokeLater(() -> new LottoGen().setVisible(true));
    }
}
