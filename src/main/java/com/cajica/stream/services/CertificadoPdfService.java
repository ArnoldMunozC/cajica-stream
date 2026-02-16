package com.cajica.stream.services;

import com.cajica.stream.entities.Certificado;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CertificadoPdfService {

  @Value("${app.base-url:http://localhost:8081}")
  private String baseUrl;

  @Value("${app.certificado.template:/static/images/diploma.png}")
  private String certificadoTemplatePath;

  private static final DeviceRgb COLOR_AZUL_OSCURO = new DeviceRgb(0, 51, 102);
  private static final DeviceRgb COLOR_DORADO = new DeviceRgb(184, 157, 102);

  public byte[] generarCertificadoPdf(Certificado certificado) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    PdfWriter writer = new PdfWriter(baos);
    PdfDocument pdf = new PdfDocument(writer);
    ImageData templateImage = cargarTemplateCertificado();
    pdf.addEventHandler(PdfDocumentEvent.START_PAGE, new CertificadoTemplateHandler(templateImage));
    Document document = new Document(pdf, PageSize.A4.rotate());
    document.setMargins(30, 50, 90, 50);

    try {
      PdfFont fontRegular = PdfFontFactory.createFont();
      PdfFont fontBold = PdfFontFactory.createFont();

      Rectangle page = pdf.getDefaultPageSize();
      float pageWidth = page.getWidth();

      // Nombre (zona central, sobre la línea)
      String nombreCompleto = obtenerNombreParaCertificado(certificado);
      document.add(
          new Paragraph(nombreCompleto)
              .setFont(fontRegular)
              .setFontSize(32)
              .setFontColor(COLOR_AZUL_OSCURO)
              .setItalic()
              .setTextAlignment(TextAlignment.CENTER)
              .setFixedPosition(1, 80, 330, pageWidth - 160));

      // Identificación
      String identificacion = certificado.getUsuario().getNumeroIdentificacion();
      if (identificacion == null || identificacion.isEmpty()) {
        identificacion = "N/A";
      }
      document.add(
          new Paragraph("Identificado (a) con C.C. No. " + identificacion)
              .setFont(fontRegular)
              .setFontSize(10)
              .setFontColor(COLOR_AZUL_OSCURO)
              .setTextAlignment(TextAlignment.CENTER)
              .setFixedPosition(1, 80, 298, pageWidth - 160));

      // Curso (entre comillas)
      document.add(
          new Paragraph("\"" + certificado.getCurso().getNombre() + "\"")
              .setFont(fontBold)
              .setFontSize(13)
              .setFontColor(COLOR_AZUL_OSCURO)
              .setTextAlignment(TextAlignment.CENTER)
              .setFixedPosition(1, 80, 258, pageWidth - 160));

      // Fecha del evento (línea "el día ...")
      DateTimeFormatter formatterEvento =
          DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
      String fechaEvento = certificado.getFechaEmision().format(formatterEvento);
      document.add(
          new Paragraph("el día " + fechaEvento)
              .setFont(fontRegular)
              .setFontSize(10)
              .setFontColor(COLOR_AZUL_OSCURO)
              .setTextAlignment(TextAlignment.CENTER)
              .setFixedPosition(1, 80, 225, pageWidth - 160));

      // QR Code y código de verificación (zona inferior)
      agregarCodigoVerificacion(document, pdf, certificado, fontRegular);

    } catch (Exception e) {
      throw new IOException("Error generando certificado PDF", e);
    }

    document.close();
    return baos.toByteArray();
  }

  private ImageData cargarTemplateCertificado() throws IOException {
    String path = Objects.toString(certificadoTemplatePath, "").trim();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    ClassPathResource resource = new ClassPathResource(path);
    try (InputStream is = resource.getInputStream()) {
      return ImageDataFactory.create(is.readAllBytes());
    }
  }

  private static class CertificadoTemplateHandler implements IEventHandler {
    private final ImageData template;

    private CertificadoTemplateHandler(ImageData template) {
      this.template = template;
    }

    @Override
    public void handleEvent(Event event) {
      PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
      PdfDocument pdfDoc = docEvent.getDocument();
      Rectangle pageSize = docEvent.getPage().getPageSize();

      PdfCanvas pdfCanvas = new PdfCanvas(docEvent.getPage().newContentStreamBefore(), docEvent.getPage().getResources(), pdfDoc);
      Canvas canvas = new Canvas(pdfCanvas, pageSize);
      Image bg = new Image(template);
      bg.scaleToFit(pageSize.getWidth(), pageSize.getHeight());
      bg.setFixedPosition(pageSize.getLeft(), pageSize.getBottom());
      canvas.add(bg);
      canvas.close();
    }
  }

  private void agregarHeader(Document document, PdfFont fontBold) {
    Table headerTable = new Table(UnitValue.createPercentArray(new float[] {1, 2, 1}));
    headerTable.setWidth(UnitValue.createPercentValue(100));

    // Celda izquierda vacía (para el diseño)
    Cell leftCell = new Cell().setBorder(Border.NO_BORDER);
    headerTable.addCell(leftCell);

    // Celda central con texto institucional
    Cell centerCell =
        new Cell()
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER)
            .setVerticalAlignment(VerticalAlignment.MIDDLE);

    centerCell.add(
        new Paragraph("ALCALDÍA DE CAJICÁ")
            .setFont(fontBold)
            .setFontSize(14)
            .setFontColor(COLOR_AZUL_OSCURO)
            .setMargin(0));

    centerCell.add(
        new Paragraph("SECRETARÍA DE SALUD")
            .setFont(fontBold)
            .setFontSize(18)
            .setFontColor(COLOR_AZUL_OSCURO)
            .setMargin(0));

    headerTable.addCell(centerCell);

    // Celda derecha con año
    Cell rightCell =
        new Cell()
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(TextAlignment.CENTER)
            .setVerticalAlignment(VerticalAlignment.TOP)
            .setPadding(0);

    int year = java.time.LocalDate.now().getYear();
    String yearStr = String.valueOf(year);
    rightCell.add(
        new Paragraph(yearStr.substring(0, 2))
            .setFont(fontBold)
            .setFontSize(20)
            .setFontColor(COLOR_AZUL_OSCURO)
            .setMargin(0));
    rightCell.add(
        new Paragraph(yearStr.substring(2))
            .setFont(fontBold)
            .setFontSize(20)
            .setFontColor(COLOR_AZUL_OSCURO)
            .setMargin(0));

    headerTable.addCell(rightCell);

    document.add(headerTable);
  }

  private void agregarFirma(Document document, PdfFont fontBold, PdfFont fontRegular) {
    // Línea de firma
    document.add(
        new Paragraph("_".repeat(40))
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(5));

    document.add(
        new Paragraph("LEIDY SUAREZ FERNANDEZ")
            .setFont(fontBold)
            .setFontSize(11)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(2));

    document.add(
        new Paragraph("DIRECTORA DE ASEGURAMIENTO DESARROLLO Y SERVICIOS DE SALUD")
            .setFont(fontRegular)
            .setFontSize(9)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(10));
  }

  private void agregarCodigoVerificacion(
      Document document, PdfDocument pdf, Certificado certificado, PdfFont fontRegular) {
    try {
      // Generar QR Code
      String urlVerificacion = normalizarBaseUrl(baseUrl) + "/certificados/verificar/" + certificado.getCodigoVerificacion();
      byte[] qrCodeImage = generarQRCode(urlVerificacion, 60, 60);

      Table qrTable = new Table(UnitValue.createPercentArray(new float[] {1, 2}));
      qrTable.setWidth(220);

      // Celda con QR
      Cell qrCell = new Cell().setBorder(Border.NO_BORDER);
      ImageData imageData = ImageDataFactory.create(qrCodeImage);
      Image qrImage = new Image(imageData);
      qrImage.setWidth(55);
      qrImage.setHeight(55);
      qrCell.add(qrImage);
      qrTable.addCell(qrCell);

      // Celda con código
      Cell codeCell =
          new Cell()
              .setBorder(Border.NO_BORDER)
              .setVerticalAlignment(VerticalAlignment.MIDDLE)
              .setPaddingLeft(10);
      codeCell.add(
          new Paragraph("Código de verificación:")
              .setFont(fontRegular)
              .setFontSize(8)
              .setFontColor(ColorConstants.GRAY));
      codeCell.add(
          new Paragraph(certificado.getCodigoVerificacion())
              .setFont(fontRegular)
              .setFontSize(10)
              .setFontColor(COLOR_AZUL_OSCURO)
              .setBold());
      qrTable.addCell(codeCell);

      float x = 90;
      float y = 12;
      qrTable.setFixedPosition(1, x, y, 220);
      document.add(qrTable);

    } catch (Exception e) {
      // Si falla el QR, solo mostrar el código
      document.add(
          new Paragraph("Código de verificación: " + certificado.getCodigoVerificacion())
              .setFont(fontRegular)
              .setFontSize(10)
              .setTextAlignment(TextAlignment.CENTER)
              .setFontColor(COLOR_AZUL_OSCURO));
    }
  }

  private byte[] generarQRCode(String text, int width, int height)
      throws WriterException, IOException {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

    ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
    return pngOutputStream.toByteArray();
  }

  private String obtenerNombreParaCertificado(Certificado certificado) {
    String nombreCompleto = certificado.getUsuario().getNombreCompleto();
    if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
      return nombreCompleto.trim();
    }
    return Objects.toString(certificado.getUsuario().getUsername(), "").trim();
  }

  private String normalizarBaseUrl(String baseUrl) {
    String url = Objects.toString(baseUrl, "").trim();
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      url = "http://" + url;
    }
    if (url.startsWith("https://localhost") || url.startsWith("https://127.0.0.1")) {
      url = "http://" + url.substring("https://".length());
    }
    return url;
  }
}
