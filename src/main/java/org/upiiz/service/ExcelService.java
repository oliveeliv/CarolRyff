package org.upiiz.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.upiiz.entities.Participante;
import org.upiiz.entities.Respuesta;
import org.upiiz.models.Resultado;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ParticipanteService participanteService;

    private static final byte[] COLOR_NAVY_DARK  = {(byte)17,  (byte)27,  (byte)51};
    private static final byte[] COLOR_NAVY       = {(byte)26,  (byte)39,  (byte)68};
    private static final byte[] COLOR_GOLD       = {(byte)201, (byte)168, (byte)76};
    private static final byte[] COLOR_CREAM      = {(byte)245, (byte)240, (byte)232};
    private static final byte[] COLOR_CREAM_ALT  = {(byte)250, (byte)248, (byte)244};
    private static final byte[] COLOR_CREAM_DARK = {(byte)236, (byte)230, (byte)216};
    private static final byte[] COLOR_WINE       = {(byte)107, (byte)31,  (byte)42};

    private static final Set<Integer> INVERSAS = Set.of(
            2, 4, 5, 9, 13, 15, 20, 22, 25, 26, 27, 29, 30, 33, 34, 36
    );

    private static final Map<Integer, String> PREGUNTAS = new LinkedHashMap<>();
    static {
        PREGUNTAS.put(1,  "Cuando repaso la historia de mi vida, estoy contento de cómo han resultado las cosas.");
        PREGUNTAS.put(2,  "A menudo me siento solo porque tengo pocos amigos íntimos con quien compartir mis preocupaciones.");
        PREGUNTAS.put(3,  "No tengo miedo de expresar mis opiniones, incluso cuando son opuestas a las de la mayoría.");
        PREGUNTAS.put(4,  "Me preocupa cómo otra gente evalúa las elecciones que he hecho en mi vida.");
        PREGUNTAS.put(5,  "Me resulta difícil dirigir mi vida hacia un camino que me satisfaga.");
        PREGUNTAS.put(6,  "Disfruto haciendo planes para el futuro y trabajar para hacerlos realidad.");
        PREGUNTAS.put(7,  "En general, me siento seguro y positivo conmigo mismo.");
        PREGUNTAS.put(8,  "No tengo muchas personas que quieran escucharme cuando necesito hablar.");
        PREGUNTAS.put(9,  "Tiendo a preocuparme sobre lo que otra gente piensa de mí.");
        PREGUNTAS.put(10, "Me juzgo por lo que yo creo que es importante, no por los valores que otros piensan.");
        PREGUNTAS.put(11, "He sido capaz de construir un hogar y un modo de vida a mi gusto.");
        PREGUNTAS.put(12, "Soy una persona activa al realizar los proyectos que propuse para mí mismo.");
        PREGUNTAS.put(13, "Si tuviera la oportunidad, hay muchas cosas de mí mismo que cambiaría.");
        PREGUNTAS.put(14, "Siento que mis amistades me aportan muchas cosas.");
        PREGUNTAS.put(15, "Tiendo a estar influenciado por la gente con fuertes convicciones.");
        PREGUNTAS.put(16, "En general, siento que soy responsable de la situación en la que vivo.");
        PREGUNTAS.put(17, "Me siento bien cuando pienso en lo que he hecho en el pasado y lo que espero hacer en el futuro.");
        PREGUNTAS.put(18, "Mis objetivos en la vida han sido más una fuente de satisfacción que de frustración.");
        PREGUNTAS.put(19, "Me gusta la mayor parte de los aspectos de mi personalidad.");
        PREGUNTAS.put(20, "Me parece que la mayor parte de las personas tienen más amigos que yo.");
        PREGUNTAS.put(21, "Tengo confianza en mis opiniones incluso si son contrarias al consenso general.");
        PREGUNTAS.put(22, "Las demandas de la vida diaria a menudo me deprimen.");
        PREGUNTAS.put(23, "Tengo clara la dirección y el objetivo de mi vida.");
        PREGUNTAS.put(24, "En general, con el tiempo siento que sigo aprendiendo más sobre mí mismo.");
        PREGUNTAS.put(25, "En muchos aspectos, me siento decepcionado de mis logros en la vida.");
        PREGUNTAS.put(26, "No he experimentado muchas relaciones cercanas y de confianza.");
        PREGUNTAS.put(27, "Es difícil para mí expresar mis propias opiniones en asuntos polémicos.");
        PREGUNTAS.put(28, "Soy bastante bueno manejando muchas de mis responsabilidades en la vida diaria.");
        PREGUNTAS.put(29, "No tengo claro qué es lo que intento conseguir en la vida.");
        PREGUNTAS.put(30, "Hace mucho tiempo que dejé de intentar hacer grandes mejoras en mi vida.");
        PREGUNTAS.put(31, "En su mayor parte, me siento orgulloso de quien soy y la vida que llevo.");
        PREGUNTAS.put(32, "Sé que puedo confiar en mis amigos, y ellos saben que pueden confiar en mí.");
        PREGUNTAS.put(33, "A menudo cambio mis decisiones si mis amigos o familia están en desacuerdo.");
        PREGUNTAS.put(34, "No quiero intentar nuevas formas de hacer las cosas; mi vida está bien como está.");
        PREGUNTAS.put(35, "Pienso que es importante tener nuevas experiencias que desafíen lo que uno piensa.");
        PREGUNTAS.put(36, "Cuando pienso en ello, realmente con los años no he mejorado mucho como persona.");
        PREGUNTAS.put(37, "Tengo la sensación de que con el tiempo me he desarrollado mucho como persona.");
        PREGUNTAS.put(38, "Para mí, la vida ha sido un proceso continuo de estudio, cambio y crecimiento.");
        PREGUNTAS.put(39, "Si me sintiera infeliz con mi situación de vida daría los pasos más eficaces para cambiarla.");
    }

    public byte[] generarExcel(List<Resultado> resultados) throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            DataFormat format = workbook.createDataFormat();
            Estilos e = new Estilos(workbook, format);

            List<Participante> participantes;
            try {
                participantes = participanteService.listarTodos();
            } catch (Exception ex) {
                participantes = Collections.emptyList();
            }

            crearHojaResumen(workbook, e, resultados);
            crearHojaRespuestas(workbook, e, participantes);
            crearHojaPromediosGrupales(workbook, e, participantes);

            workbook.write(out);
            workbook.dispose();
            return out.toByteArray();
        }
    }

    private void crearHojaResumen(SXSSFWorkbook wb, Estilos e, List<Resultado> resultados) {
        Sheet sheet = wb.createSheet("1. Resumen Promedios");

        Row filaTitulo = sheet.createRow(0);
        filaTitulo.setHeightInPoints(32);
        Cell titulo = filaTitulo.createCell(0);
        titulo.setCellValue("ESCALA DE BIENESTAR PSICOLÓGICO - CAROL RYFF · Resumen de Promedios");
        titulo.setCellStyle(e.cabecera);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

        Row filaEnc = sheet.createRow(1);
        filaEnc.setHeightInPoints(42);
        String[] cols = {"Nombre Completo","Sexo","Edad","Año","Grupo",
                "Autoaceptación","Relaciones\nPositivas","Autonomía",
                "Dominio del\nEntorno","Propósito en\nla Vida",
                "Crecimiento\nPersonal","Bienestar\nGlobal","Nivel"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = filaEnc.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(e.encabezado);
        }

        int nf = 2;
        if (resultados != null) {
            for (Resultado r : resultados) {
                Row row = sheet.createRow(nf);
                row.setHeightInPoints(20);
                boolean alt = nf % 2 == 0;
                CellStyle txt = alt ? e.datoAlt : e.dato;
                CellStyle num = alt ? e.numAlt  : e.num;

                crearCeldaTxt(row, 0, r.getNombreCompleto(), txt);
                crearCeldaTxt(row, 1, r.getSexo(), txt);
                crearCeldaNum(row, 2, r.getEdad() != null ? r.getEdad() : 0, num);
                crearCeldaTxt(row, 3, r.getAnioEscolar(), txt);
                crearCeldaTxt(row, 4, r.getGrupo(), txt);
                crearCeldaNum(row, 5,  safe(r.getAutoaceptacion()), num);
                crearCeldaNum(row, 6,  safe(r.getRelacionesPositivas()), num);
                crearCeldaNum(row, 7,  safe(r.getAutonomia()), num);
                crearCeldaNum(row, 8,  safe(r.getDominioEntorno()), num);
                crearCeldaNum(row, 9,  safe(r.getPropositoVida()), num);
                crearCeldaNum(row, 10, safe(r.getCrecimientoPersonal()), num);
                crearCeldaNum(row, 11, safe(r.getBienestarGlobal()), e.global);
                crearCeldaTxt(row, 12, r.getNivelBienestarGlobal(), e.nivel);
                nf++;
            }
        }

        if (resultados != null && !resultados.isEmpty()) {
            Row fp = sheet.createRow(nf);
            fp.setHeightInPoints(22);
            Cell lbl = fp.createCell(0);
            lbl.setCellValue("PROMEDIO GENERAL");
            lbl.setCellStyle(e.promLabel);
            sheet.addMergedRegion(new CellRangeAddress(nf, nf, 0, 4));
            for (int i = 1; i <= 4; i++) fp.createCell(i).setCellStyle(e.promLabel);
            String[] letras = {"F","G","H","I","J","K","L"};
            for (int i = 0; i < letras.length; i++) {
                Cell cp = fp.createCell(5 + i);
                cp.setCellFormula("AVERAGE(" + letras[i] + "3:" + letras[i] + nf + ")");
                cp.setCellStyle(e.promNum);
            }
        }

        sheet.setColumnWidth(0, 9000); sheet.setColumnWidth(1, 3200);
        sheet.setColumnWidth(2, 2000); sheet.setColumnWidth(3, 2500);
        sheet.setColumnWidth(4, 2500);
        for (int i = 5; i <= 11; i++) sheet.setColumnWidth(i, 3800);
        sheet.setColumnWidth(12, 3000);
        sheet.createFreezePane(0, 2);
    }

    private void crearHojaRespuestas(SXSSFWorkbook wb, Estilos e, List<Participante> participantes) {
        Sheet sheet = wb.createSheet("2. Respuestas por Pregunta");

        Row filaTitulo = sheet.createRow(0);
        filaTitulo.setHeightInPoints(32);
        Cell titulo = filaTitulo.createCell(0);
        titulo.setCellValue("RESPUESTAS INDIVIDUALES POR PREGUNTA (valor procesado si es inversa)");
        titulo.setCellStyle(e.cabecera);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 40));

        Row filaEnc = sheet.createRow(1);
        filaEnc.setHeightInPoints(50);
        Cell cNombre = filaEnc.createCell(0);
        cNombre.setCellValue("Nombre Completo");
        cNombre.setCellStyle(e.encabezado);
        Cell cGrupo = filaEnc.createCell(1);
        cGrupo.setCellValue("Grupo");
        cGrupo.setCellStyle(e.encabezado);

        for (int i = 1; i <= 39; i++) {
            Cell c = filaEnc.createCell(i + 1);
            c.setCellValue("i" + i + (INVERSAS.contains(i) ? "\n(inv)" : ""));
            c.setCellStyle(INVERSAS.contains(i) ? e.encabezadoInverso : e.encabezado);
        }

        Row filaPregTexto = sheet.createRow(2);
        filaPregTexto.setHeightInPoints(30);
        Cell lblTxt = filaPregTexto.createCell(0);
        lblTxt.setCellValue("Texto de pregunta →");
        lblTxt.setCellStyle(e.promLabel);
        filaPregTexto.createCell(1).setCellStyle(e.promLabel);

        for (int i = 1; i <= 39; i++) {
            Cell c = filaPregTexto.createCell(i + 1);
            c.setCellValue(PREGUNTAS.getOrDefault(i, "P" + i));
            c.setCellStyle(e.estTexto);
        }

        int nf = 3;
        if (participantes != null) {
            for (Participante p : participantes) {
                Row row = sheet.createRow(nf);
                row.setHeightInPoints(20);
                boolean alt = nf % 2 == 0;
                CellStyle txt = alt ? e.datoAlt : e.dato;

                crearCeldaTxt(row, 0, p.getNombreCompleto(), txt);
                crearCeldaTxt(row, 1, p.getGrupo(), txt);

                List<Respuesta> respuestas = p.getRespuestas() != null ? p.getRespuestas() : Collections.emptyList();
                Map<Integer, Respuesta> mapaResp = new HashMap<>();
                for (Respuesta resp : respuestas) {
                    if (resp != null && resp.getNumeroPregunta() != null) {
                        mapaResp.putIfAbsent(resp.getNumeroPregunta(), resp);
                    }
                }

                for (int i = 1; i <= 39; i++) {
                    Cell c = row.createCell(i + 1);
                    Respuesta resp = mapaResp.get(i);
                    if (resp != null && resp.getValorProcesado() != null) {
                        c.setCellValue(resp.getValorProcesado());
                        c.setCellStyle(INVERSAS.contains(i)
                                ? (alt ? e.numAltInverso : e.numInverso)
                                : (alt ? e.numAlt : e.num));
                    } else {
                        c.setCellValue("-");
                        c.setCellStyle(txt);
                    }
                }
                nf++;
            }
        }

        sheet.setColumnWidth(0, 8000);
        sheet.setColumnWidth(1, 2500);
        for (int i = 2; i <= 40; i++) sheet.setColumnWidth(i, 1800);
        sheet.createFreezePane(2, 3);
    }

    private void crearHojaPromediosGrupales(SXSSFWorkbook wb, Estilos e, List<Participante> participantes) {
        Sheet sheet = wb.createSheet("3. Promedios por Grupo");

        Row filaTitulo = sheet.createRow(0);
        filaTitulo.setHeightInPoints(32);
        Cell titulo = filaTitulo.createCell(0);
        titulo.setCellValue("PROMEDIOS GRUPALES POR DIMENSIÓN - ESCALA RYFF");
        titulo.setCellStyle(e.cabecera);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

        Row filaEnc = sheet.createRow(1);
        filaEnc.setHeightInPoints(42);
        String[] cols = {"Grupo","N° Alumnos","Autoaceptación",
                "Relaciones\nPositivas","Autonomía","Dominio del\nEntorno",
                "Propósito en\nla Vida","Crecimiento\nPersonal","Bienestar\nGlobal"};
        for (int i = 0; i < cols.length; i++) {
            Cell c = filaEnc.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(e.encabezado);
        }

        Map<String, List<Participante>> porGrupo = (participantes != null ? participantes : Collections.<Participante>emptyList()).stream()
                .filter(p -> p != null && p.getGrupo() != null)
                .collect(Collectors.groupingBy(
                        Participante::getGrupo,
                        TreeMap::new,
                        Collectors.toList()));

        int nf = 2;
        for (Map.Entry<String, List<Participante>> entry : porGrupo.entrySet()) {
            String grupo = entry.getKey();
            List<Participante> lista = entry.getValue();
            Row row = sheet.createRow(nf);
            row.setHeightInPoints(22);
            boolean alt = nf % 2 == 0;
            CellStyle txt = alt ? e.datoAlt : e.dato;
            CellStyle num = alt ? e.numAlt  : e.num;

            crearCeldaTxt(row, 0, grupo, txt);
            crearCeldaNum(row, 1, lista.size(), num);
            crearCeldaNum(row, 2, avgDim(lista, "autoaceptacion"), num);
            crearCeldaNum(row, 3, avgDim(lista, "relacionesPositivas"), num);
            crearCeldaNum(row, 4, avgDim(lista, "autonomia"), num);
            crearCeldaNum(row, 5, avgDim(lista, "dominioEntorno"), num);
            crearCeldaNum(row, 6, avgDim(lista, "propositoVida"), num);
            crearCeldaNum(row, 7, avgDim(lista, "crecimientoPersonal"), num);
            crearCeldaNum(row, 8, avgDim(lista, "global"), e.global);
            nf++;
        }

        if (!porGrupo.isEmpty()) {
            Row rowTotal = sheet.createRow(nf);
            rowTotal.setHeightInPoints(24);
            Cell lbl = rowTotal.createCell(0);
            lbl.setCellValue("TOTAL GENERAL");
            lbl.setCellStyle(e.promLabel);
            rowTotal.createCell(1).setCellStyle(e.promLabel);
            String[] letras = {"C","D","E","F","G","H","I"};
            for (int i = 0; i < letras.length; i++) {
                Cell cp = rowTotal.createCell(2 + i);
                cp.setCellFormula("AVERAGE(" + letras[i] + "3:" + letras[i] + nf + ")");
                cp.setCellStyle(e.promNum);
            }
        }

        sheet.setColumnWidth(0, 3000); sheet.setColumnWidth(1, 3000);
        for (int i = 2; i <= 8; i++) sheet.setColumnWidth(i, 4000);
        sheet.createFreezePane(0, 2);
    }

    private double avgDim(List<Participante> lista, String dim) {
        if (lista == null || lista.isEmpty()) return 0.0;
        return lista.stream().mapToDouble(p -> switch (dim) {
            case "autoaceptacion"      -> safe(p.getPromedioAutoaceptacion());
            case "relacionesPositivas" -> safe(p.getPromedioRelacionesPositivas());
            case "autonomia"           -> safe(p.getPromedioAutonomia());
            case "dominioEntorno"      -> safe(p.getPromedioDominioEntorno());
            case "propositoVida"       -> safe(p.getPromedioPropositoVida());
            case "crecimientoPersonal" -> safe(p.getPromedioCrecimientoPersonal());
            case "global"              -> safe(p.getPromedioBienestarGlobal());
            default -> 0.0;
        }).average().orElse(0.0);
    }

    private double safe(Double v) { return v != null ? v : 0.0; }

    private void crearCeldaTxt(Row row, int col, String val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val != null ? val : "");
        c.setCellStyle(style);
    }

    private void crearCeldaNum(Row row, int col, double val, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(style);
    }

    private static class Estilos {
        CellStyle cabecera, encabezado, encabezadoInverso;
        CellStyle dato, datoAlt, num, numAlt;
        CellStyle numInverso, numAltInverso;
        CellStyle global, nivel;
        CellStyle promLabel, promNum;
        CellStyle estTexto;

        Estilos(SXSSFWorkbook wb, DataFormat fmt) {
            // Obtenemos el XSSFWorkbook subyacente para gestionar fuentes y colores de forma segura
            XSSFWorkbook xssfWb = wb.getXSSFWorkbook();

            cabecera = wb.createCellStyle();
            cabecera.setFillForegroundColor(color(COLOR_NAVY_DARK));
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecera.setAlignment(HorizontalAlignment.CENTER);
            cabecera.setVerticalAlignment(VerticalAlignment.CENTER);
            cabecera.setFont(fuente(xssfWb, COLOR_GOLD, true, 13));

            encabezado = wb.createCellStyle();
            encabezado.setFillForegroundColor(color(COLOR_NAVY));
            encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            encabezado.setAlignment(HorizontalAlignment.CENTER);
            encabezado.setVerticalAlignment(VerticalAlignment.CENTER);
            encabezado.setWrapText(true);
            encabezado.setBorderBottom(BorderStyle.MEDIUM);
            encabezado.setFont(fuente(xssfWb, COLOR_CREAM, true, 10));

            encabezadoInverso = wb.createCellStyle();
            encabezadoInverso.cloneStyleFrom(encabezado);
            encabezadoInverso.setFillForegroundColor(color(COLOR_WINE));

            dato = wb.createCellStyle();
            dato.setAlignment(HorizontalAlignment.CENTER);
            dato.setVerticalAlignment(VerticalAlignment.CENTER);
            dato.setBorderBottom(BorderStyle.THIN);
            dato.setBorderLeft(BorderStyle.THIN);
            dato.setBorderRight(BorderStyle.THIN);
            dato.setFont(fuente(xssfWb, null, false, 10));

            datoAlt = wb.createCellStyle();
            datoAlt.cloneStyleFrom(dato);
            datoAlt.setFillForegroundColor(color(COLOR_CREAM_ALT));
            datoAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            num = wb.createCellStyle();
            num.cloneStyleFrom(dato);
            num.setDataFormat(fmt.getFormat("0.00"));

            numAlt = wb.createCellStyle();
            numAlt.cloneStyleFrom(datoAlt);
            numAlt.setDataFormat(fmt.getFormat("0.00"));

            numInverso = wb.createCellStyle();
            numInverso.cloneStyleFrom(num);
            numInverso.setFillForegroundColor(
                    color(new byte[]{(byte)250,(byte)235,(byte)237}));
            numInverso.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            numInverso.setDataFormat(fmt.getFormat("0"));

            numAltInverso = wb.createCellStyle();
            numAltInverso.cloneStyleFrom(numInverso);
            numAltInverso.setFillForegroundColor(
                    color(new byte[]{(byte)245,(byte)225,(byte)228}));

            global = wb.createCellStyle();
            global.cloneStyleFrom(num);
            global.setFillForegroundColor(color(COLOR_CREAM_DARK));
            global.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            global.setFont(fuente(xssfWb, COLOR_NAVY_DARK, true, 11));
            global.setDataFormat(fmt.getFormat("0.00"));

            nivel = wb.createCellStyle();
            nivel.cloneStyleFrom(dato);
            nivel.setFont(fuente(xssfWb, null, true, 10));

            promLabel = wb.createCellStyle();
            promLabel.setFillForegroundColor(color(COLOR_NAVY));
            promLabel.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            promLabel.setAlignment(HorizontalAlignment.RIGHT);
            promLabel.setVerticalAlignment(VerticalAlignment.CENTER);
            promLabel.setFont(fuente(xssfWb, COLOR_GOLD, true, 10));

            promNum = wb.createCellStyle();
            promNum.cloneStyleFrom(promLabel);
            promNum.setAlignment(HorizontalAlignment.CENTER);
            promNum.setDataFormat(fmt.getFormat("0.00"));

            estTexto = wb.createCellStyle();
            estTexto.cloneStyleFrom(dato);
            estTexto.setWrapText(true);
            estTexto.setAlignment(HorizontalAlignment.LEFT);
        }

        private static XSSFColor color(byte[] rgb) {
            return new XSSFColor(rgb, null);
        }

        private static XSSFFont fuente(XSSFWorkbook xssfWb, byte[] colorRgb,
                                       boolean bold, int size) {
            XSSFFont f = xssfWb.createFont();
            f.setBold(bold);
            f.setFontHeightInPoints((short) size);
            if (colorRgb != null) f.setColor(new XSSFColor(colorRgb, null));
            return f;
        }
    }
}