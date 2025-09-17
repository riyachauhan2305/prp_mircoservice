package com.prp.ocrservice.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class SuryaOcrService {

    public String extractText(MultipartFile file) throws IOException {
        // Save PDF to temp file
        File tempPdf = File.createTempFile("bank-statement", ".pdf");
        file.transferTo(tempPdf);

        StringBuilder fullOcrText = new StringBuilder();

        try (PDDocument document = PDDocument.load(tempPdf)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // Loop through each page
            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300);

                // TODO: Replace this with actual local Surya OCR SDK call
                String ocrText = mockOcr(image);
                fullOcrText.append(ocrText).append("\n");
            }
        }

        tempPdf.delete();
        return fullOcrText.toString();
    }

    // Mock OCR for testing
    private String mockOcr(BufferedImage image) {
        return "01/09/2025 ATM Withdrawal 5000.00 15000.50\n"
             + "03/09/2025 Salary Credit 25000.00 40000.50\n"
             + "05/09/2025 Grocery Shopping 2000.00 38000.50";
    }
}
