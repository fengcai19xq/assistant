import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 简化版文件助手 - 无外部依赖
 */
public class SimpleFileAssistant {
    
    private static final String DATA_DIR = System.getProperty("user.home") + "/.file-assistant/data";
    private static final String INDEX_FILE = DATA_DIR + "/file-index.txt";
    
    public static void main(String[] args) {
        System.out.println("🤖 文件AI助手启动中...");
        
        try {
            // 创建必要目录
            createDirectories();
            
            // 演示功能
            demonstrateFeatures();
            
        } catch (Exception e) {
            System.err.println("启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 创建必要目录
     */
    private static void createDirectories() throws IOException {
        Path dataDir = Paths.get(DATA_DIR);
        Files.createDirectories(dataDir);
        System.out.println("✅ 创建目录: " + dataDir);
    }
    
    /**
     * 演示功能
     */
    private static void demonstrateFeatures() {
        System.out.println("\n🔍 演示功能:");
        
        // 1. 添加监控文件夹
        addWatchFolder("/Users/qianxu/Documents", true);
        
        // 2. 索引文件
        indexFiles("/Users/qianxu/Documents");
        
        // 3. 搜索文件
        searchFiles("java");
        
        System.out.println("\n✅ 演示完成！");
        System.out.println("💡 提示: 打开 frontend/index.html 使用完整界面");
    }
    
    /**
     * 添加监控文件夹
     */
    private static void addWatchFolder(String path, boolean recursive) {
        try {
            Path folderPath = Paths.get(path);
            if (Files.exists(folderPath) && Files.isDirectory(folderPath)) {
                System.out.println("✅ 添加监控文件夹: " + path + " (递归: " + recursive + ")");
            } else {
                System.out.println("❌ 文件夹不存在: " + path);
            }
        } catch (Exception e) {
            System.err.println("❌ 添加文件夹失败: " + e.getMessage());
        }
    }
    
    /**
     * 索引文件
     */
    private static void indexFiles(String folderPath) {
        try {
            Path path = Paths.get(folderPath);
            if (!Files.exists(path)) {
                System.out.println("❌ 文件夹不存在: " + folderPath);
                return;
            }
            
            List<String> indexedFiles = new ArrayList<>();
            Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(p -> isTextFile(p.toString()))
                .limit(10) // 限制数量用于演示
                .forEach(filePath -> {
                    try {
                        String content = extractTextContent(filePath);
                        if (content != null && !content.trim().isEmpty()) {
                            String fileInfo = String.format("%s|%s|%d|%s|%s",
                                filePath.toString(),
                                filePath.getFileName().toString(),
                                Files.size(filePath),
                                getFileType(filePath.toString()),
                                content.substring(0, Math.min(content.length(), 200)) // 限制长度
                            );
                            indexedFiles.add(fileInfo);
                        }
                    } catch (Exception e) {
                        // 忽略错误，继续处理其他文件
                    }
                });
            
            // 保存索引到文件
            saveIndexToFile(indexedFiles);
            
            System.out.println("✅ 索引完成，处理了 " + indexedFiles.size() + " 个文件");
            
        } catch (Exception e) {
            System.err.println("❌ 索引失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存索引到文件
     */
    private static void saveIndexToFile(List<String> indexedFiles) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(INDEX_FILE))) {
            for (String fileInfo : indexedFiles) {
                writer.println(fileInfo);
            }
            System.out.println("✅ 索引已保存到: " + INDEX_FILE);
        } catch (IOException e) {
            System.err.println("❌ 保存索引失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索文件
     */
    private static void searchFiles(String query) {
        try {
            List<String> results = new ArrayList<>();
            
            if (Files.exists(Paths.get(INDEX_FILE))) {
                Files.lines(Paths.get(INDEX_FILE))
                    .filter(line -> line.toLowerCase().contains(query.toLowerCase()))
                    .limit(5)
                    .forEach(results::add);
            }
            
            System.out.println("🔍 搜索结果 (关键词: " + query + "):");
            if (results.isEmpty()) {
                System.out.println("  未找到匹配的文件");
            } else {
                for (int i = 0; i < results.size(); i++) {
                    String[] parts = results.get(i).split("\\|");
                    if (parts.length >= 2) {
                        System.out.println("  " + (i + 1) + ". " + parts[1]); // 文件名
                        System.out.println("     路径: " + parts[0]); // 文件路径
                        if (parts.length >= 4) {
                            System.out.println("     类型: " + parts[3]); // 文件类型
                        }
                        System.out.println();
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ 搜索失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查是否为文本文件
     */
    private static boolean isTextFile(String filePath) {
        String lowerPath = filePath.toLowerCase();
        return lowerPath.endsWith(".txt") || 
               lowerPath.endsWith(".md") || 
               lowerPath.endsWith(".java") || 
               lowerPath.endsWith(".js") || 
               lowerPath.endsWith(".html") || 
               lowerPath.endsWith(".css") || 
               lowerPath.endsWith(".xml") || 
               lowerPath.endsWith(".json");
    }
    
    /**
     * 提取文本内容
     */
    private static String extractTextContent(Path filePath) {
        try {
            return new String(Files.readAllBytes(filePath), "UTF-8");
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取文件类型
     */
    private static String getFileType(String filePath) {
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".java")) return "code";
        if (lowerPath.endsWith(".js")) return "code";
        if (lowerPath.endsWith(".html")) return "web";
        if (lowerPath.endsWith(".css")) return "web";
        if (lowerPath.endsWith(".md")) return "text";
        if (lowerPath.endsWith(".txt")) return "text";
        return "other";
    }
}
