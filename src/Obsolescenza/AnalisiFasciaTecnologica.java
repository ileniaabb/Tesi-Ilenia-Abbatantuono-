package Obsolescenza;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;

import Obsolescenza.PipelinePugliaHighTech.Fascia;

public class AnalisiFasciaTecnologica implements StrategiaAnalisi {

    public enum Fascia {
        BASSA,
        MEDIA,
        ALTA,
        NON_DEFINITA
    }

    private final Map<String, Fascia> mappaFasce;

    public AnalisiFasciaTecnologica() {
        this.mappaFasce = inizializzaMappaturaFasce();
    }

    @Override
    public String getNomeStrategia() {
        return "Analisi per Fascia Tecnologica";
    }

    private Map<String, Fascia> inizializzaMappaturaFasce() {
        Map<String, Fascia> mappa = new HashMap<>();

        // TOMOGRAFI COMPUTERIZZATI (TAC - Z110306)
        mappa.put("Z11030601", Fascia.BASSA); // <= 2 Strati
        mappa.put("Z11030602", Fascia.MEDIA); // > 2 e < 16 Strati
        mappa.put("Z11030603", Fascia.MEDIA); // >= 16 e < 64 Strati
        mappa.put("Z11030604", Fascia.ALTA);  // >= 64 Strati
        mappa.put("Z11030605", Fascia.ALTA);  // >= 64 e < 128 Strati
        mappa.put("Z11030606", Fascia.ALTA);  // >= 128 e < 256 Strati
        mappa.put("Z11030607", Fascia.ALTA);  // >= 256 Strati

        // RISONANZE MAGNETICHE (RM - Z110501)
        mappa.put("Z11050101", Fascia.BASSA); // Settoriali / Estremità
        mappa.put("Z11050102", Fascia.BASSA); // Magnete aperto <= 0.5T
        mappa.put("Z11050103", Fascia.MEDIA); // Magnete aperto > 0.5T
        mappa.put("Z11050104", Fascia.BASSA); // Magnete chiuso <= 0.5T o <= 2T
        mappa.put("Z11050105", Fascia.ALTA);  // Magnete chiuso 1.5T - 3T / 2T - 4T
        mappa.put("Z11050106", Fascia.ALTA);  // > 3T o > 4T

     // ACCELERATORI LINEARI (Z110101)
        mappa.put("Z11010101", Fascia.BASSA); // Energia singola (tecnologia superata/limitata per oncologia complessa)
        mappa.put("Z11010102", Fascia.MEDIA); // Energia media e multipla
        mappa.put("Z11010103", Fascia.ALTA);  // Energia alta e multipla (massima precisione radioterapica)
        mappa.put("Z11010104", Fascia.ALTA);  // Intraoperatori (IORT, altissima specializzazione)
        
     // MEDICINA NUCLEARE E PET (Z1102)
        mappa.put("Z11020101", Fascia.BASSA); // Gamma camere mobili
        mappa.put("Z11020102", Fascia.BASSA); // Gamma camere fisse singola testata - senza Total Body
        mappa.put("Z11020103", Fascia.MEDIA); // Gamma camere fisse singola testata - con Total Body
        mappa.put("Z11020104", Fascia.MEDIA); // Gamma camere fisse testata multipla - senza Total Body
        mappa.put("Z11020105", Fascia.ALTA);  // Gamma camere fisse testata multipla - con Total Body
        mappa.put("Z11020201", Fascia.ALTA);  // Sistemi TC / Gamma Camera
        mappa.put("Z11020301", Fascia.ALTA);  // Sistemi TC / PET (Massima specializzazione metabolica)
        return mappa;
    }
    @Override
    public void eseguiAnalisiEGrafico(Map<String, Map<String, Integer>> macchinariPerRegione) {
        System.out.println("ESECUZIONE: " + getNomeStrategia());
        System.out.println("Indicatore: % Apparecchiature a Fascia Tecnologica Bassa su totale apparecchiature classificate\n");
        
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // 1. Creiamo una lista per memorizzare i risultati prima di metterli nel dataset
        java.util.List<Map.Entry<String, Double>> datiDaOrdinare = new java.util.ArrayList<>();

        for (Map.Entry<String, Map<String, Integer>> entryRegione : macchinariPerRegione.entrySet()) {
            String regione = entryRegione.getKey();
            Map<String, Integer> cndMap = entryRegione.getValue();
            int qtaBassa = 0;
            int qtaMedia = 0;
            int qtaAlta = 0;
            for (Map.Entry<String, Integer> entryCnd : cndMap.entrySet()) {
                String codiceCnd = entryCnd.getKey();
                int qta = entryCnd.getValue();
                Fascia f = mappaFasce.getOrDefault(codiceCnd, Fascia.NON_DEFINITA);
                switch (f) {
                    case BASSA:
                        qtaBassa += qta;
                        break;
                    case MEDIA:
                        qtaMedia += qta;
                        break;
                    case ALTA:
                        qtaAlta += qta;
                        break;
                    default:
                        break;
                }
            }
            int totaleMappato = qtaBassa + qtaMedia + qtaAlta;
            if (totaleMappato > 0) {
                double percentualeBassa = ((double) qtaBassa / totaleMappato) * 100.0;
                System.out.printf("Regione: %-22s | Mappate: %4d | Bassa: %4d | Media: %4d | Alta: %4d | %% Fascia Bassa: %5.1f%%\n",
                        regione, totaleMappato, qtaBassa, qtaMedia, qtaAlta, percentualeBassa);
                
                // Invece di popolare il dataset, salviamo la coppia (Regione, Percentuale) nella lista
                datiDaOrdinare.add(new java.util.AbstractMap.SimpleEntry<>(regione, percentualeBassa));
            }
        }
        
        // 2. Ordiniamo la lista in modo crescente in base al valore (percentualeBassa)
        datiDaOrdinare.sort(Map.Entry.comparingByValue());

        // 3. Popoliamo il dataset iterando sulla lista ordinata
        for (Map.Entry<String, Double> entry : datiDaOrdinare) {
            dataset.addValue(entry.getValue(), "Quota Fascia Bassa (%)", entry.getKey());
        }
        
        JFreeChart barChart = ChartFactory.createBarChart(
                "Quota Apparecchiature a Fascia Tecnologica Bassa per Regione",
                "Regione",
                "Percentuale su totale macchinari classificati (%)",
                dataset,
                PlotOrientation.HORIZONTAL,
                true, true, false
        );
        CategoryPlot plotC = barChart.getCategoryPlot();
        
        StackedBarRenderer renderer1 = new StackedBarRenderer();
       
        renderer1.setShadowVisible(false);
        renderer1.setBarPainter(new StandardBarPainter()); 
        plotC.setRenderer(renderer1);
        plotC.setBackgroundPaint(new java.awt.Color(190,225,245));
        
        try {
            File outputFile = new File("grafico_quota_fascia_bassa.png");
            ChartUtils.saveChartAsPNG(outputFile, barChart, 1000, 700);
            System.out.println("Grafico generato con successo: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio del grafico fascia tecnologica: " + e.getMessage());
        }
    }
}