package com.assistant.core.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 增强文档解析服务
 * 支持多种文档格式的解析
 */
@Service
public class EnhancedDocumentParserService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedDocumentParserService.class);
    
    private final Tika tika = new Tika();
    
    /**
     * 解析文档内容
     */
    public String parseDocument(Path filePath) {
        try {
            String fileExtension = getFileExtension(filePath.toString());
            String mimeType = tika.detect(filePath.toFile());
            
            logger.debug("解析文档: {} (类型: {})", filePath, mimeType);
            
            switch (fileExtension.toLowerCase()) {
                case "pdf":
                    return parsePDF(filePath);
                case "docx":
                    return parseWordDocument(filePath);
                case "xlsx":
                    return parseExcelDocument(filePath);
                case "pptx":
                    return parsePowerPointDocument(filePath);
                case "txt":
                case "md":
                case "java":
                case "js":
                case "html":
                case "css":
                case "xml":
                case "json":
                    return parseTextFile(filePath);
                default:
                    return parseWithTika(filePath);
            }
        } catch (Exception e) {
            logger.error("解析文档失败: {}", filePath, e);
            return "";
        }
    }
    
    /**
     * 解析PDF文档
     */
    private String parsePDF(Path filePath) throws IOException {
        try {
            // 直接使用Tika解析PDF
            return parseWithTika(filePath);
        } catch (Exception e) {
            logger.warn("PDF解析失败: {}", filePath, e);
            return "";
        }
    }
    
    /**
     * 解析Word文档
     */
    private String parseWordDocument(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument document = new XWPFDocument(fis)) {
            
            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                content.append(paragraph.getText()).append("\n");
            }
            return content.toString();
        }
    }
    
    /**
     * 解析Excel文档
     */
    private String parseExcelDocument(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            
            StringBuilder content = new StringBuilder();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                XSSFSheet sheet = workbook.getSheetAt(i);
                content.append("Sheet: ").append(sheet.getSheetName()).append("\n");
                
                for (org.apache.poi.ss.usermodel.Row row : sheet) {
                    StringBuilder rowContent = new StringBuilder();
                    for (org.apache.poi.ss.usermodel.Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
                                rowContent.append(cell.getStringCellValue()).append("\t");
                                break;
                            case NUMERIC:
                                rowContent.append(cell.getNumericCellValue()).append("\t");
                                break;
                            case BOOLEAN:
                                rowContent.append(cell.getBooleanCellValue()).append("\t");
                                break;
                            default:
                                rowContent.append(" ").append("\t");
                        }
                    }
                    content.append(rowContent.toString().trim()).append("\n");
                }
            }
            return content.toString();
        } catch (Exception e) {
            logger.warn("Excel解析失败，使用Tika解析: {}", filePath, e);
            try {
                return parseWithTika(filePath);
            } catch (Exception ex) {
                logger.warn("Tika解析也失败: {}", filePath, ex);
                return "";
            }
        }
    }
    
    /**
     * 解析PowerPoint文档
     */
    private String parsePowerPointDocument(Path filePath) throws IOException {
        try {
            // 使用Tika解析PPTX
            return parseWithTika(filePath);
        } catch (Exception e) {
            logger.warn("PowerPoint解析失败: {}", filePath, e);
            return "";
        }
    }
    
    /**
     * 解析文本文件
     */
    private String parseTextFile(Path filePath) throws IOException {
        return Files.readString(filePath);
    }
    
    /**
     * 使用Tika解析文档
     */
    private String parseWithTika(Path filePath) throws IOException, TikaException {
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return tika.parseToString(inputStream);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    /**
     * 检查文件是否可解析
     */
    public boolean isParseable(Path filePath) {
        try {
            String fileExtension = getFileExtension(filePath.toString());
            String mimeType = tika.detect(filePath.toFile());
            
            // 检查文件大小
            long fileSize = Files.size(filePath);
            if (fileSize > 50 * 1024 * 1024) { // 50MB限制
                return false;
            }
            
            // 检查文件类型
            return isSupportedFileType(fileExtension, mimeType);
        } catch (Exception e) {
            logger.warn("检查文件可解析性失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 检查是否为支持的文件类型
     */
    private boolean isSupportedFileType(String extension, String mimeType) {
        Set<String> supportedExtensions = Set.of(
            "txt", "md", "java", "js", "html", "css", "xml", "json",
            "pdf", "docx", "xlsx", "pptx", "doc", "xls", "ppt"
        );
        
        return supportedExtensions.contains(extension.toLowerCase()) ||
               mimeType.startsWith("text/") ||
               mimeType.contains("pdf") ||
               mimeType.contains("word") ||
               mimeType.contains("excel") ||
               mimeType.contains("powerpoint");
    }
    
    /**
     * 提取文档元数据
     */
    public Map<String, Object> extractMetadata(Path filePath) {
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            // 基本文件信息
            metadata.put("fileName", filePath.getFileName().toString());
            metadata.put("fileSize", Files.size(filePath));
            metadata.put("lastModified", Files.getLastModifiedTime(filePath));
            metadata.put("mimeType", tika.detect(filePath.toFile()));
            
            // 使用Tika提取更多元数据
            try (InputStream inputStream = Files.newInputStream(filePath)) {
                org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
                tika.parse(inputStream, tikaMetadata);
                
                for (String name : tikaMetadata.names()) {
                    metadata.put(name, tikaMetadata.get(name));
                }
            }
        } catch (Exception e) {
            logger.warn("提取文档元数据失败: {}", filePath, e);
        }
        
        return metadata;
    }
    
    /**
     * 分块解析大文档
     */
    public List<String> parseDocumentInChunks(Path filePath, int chunkSize) {
        String content = parseDocument(filePath);
        if (content.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> chunks = new ArrayList<>();
        String[] sentences = content.split("[.!?]\\s+");
        
        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > chunkSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
            }
            currentChunk.append(sentence).append(". ");
        }
        
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        
        return chunks;
    }
}
