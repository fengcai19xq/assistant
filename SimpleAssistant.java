import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 简化版文件AI助手 - 兼容Java 8
 */
public class SimpleAssistant {
    
    private static final String DB_URL = "jdbc:sqlite:" + System.getProperty("user.home") + "/.file-assistant/data/assistant.db";
    private static final String DATA_DIR = System.getProperty("user.home") + "/.file-assistant/data";
    
    public static void main(String[] args) {
        System.out.println("🤖 文件AI助手启动中...");
        
        try {
            // 加载SQLite驱动
            Class.forName("org.sqlite.JDBC");
            
            // 创建必要目录
            createDirectories();
            
            // 初始化数据库
            initDatabase();
            
            // 启动简单Web服务器
            startWebServer();
            
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
     * 初始化数据库
     */
    private static void initDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            // 创建表
            String createFoldersTable = 
                "CREATE TABLE IF NOT EXISTS watch_folders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "path TEXT UNIQUE NOT NULL, " +
                "recursive BOOLEAN DEFAULT 1, " +
                "enabled BOOLEAN DEFAULT 1, " +
                "created_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
            
            String createFilesTable = 
                "CREATE TABLE IF NOT EXISTS file_index (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "file_path TEXT UNIQUE NOT NULL, " +
                "file_name TEXT NOT NULL, " +
                "file_size INTEGER, " +
                "file_type TEXT, " +
                "last_modified DATETIME, " +
                "indexed_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "content TEXT" +
                ")";
            
            String createSearchHistoryTable = 
                "CREATE TABLE IF NOT EXISTS search_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "query_text TEXT NOT NULL, " +
                "result_count INTEGER, " +
                "search_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(createFoldersTable);
                stmt.execute(createFilesTable);
                stmt.execute(createSearchHistoryTable);
            }
            
            System.out.println("✅ 数据库初始化完成");
        }
    }
    
    /**
     * 启动简单Web服务器
     */
    private static void startWebServer() {
        System.out.println("🌐 启动Web服务器...");
        System.out.println("📱 访问地址: http://localhost:8080");
        System.out.println("📁 前端界面: 打开 frontend/index.html");
        
        // 这里可以集成简单的HTTP服务器
        // 为了演示，我们创建一个简单的文件索引功能
        demonstrateFeatures();
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
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "INSERT OR IGNORE INTO watch_folders (path, recursive) VALUES (?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, path);
                pstmt.setBoolean(2, recursive);
                pstmt.executeUpdate();
            }
            System.out.println("✅ 添加监控文件夹: " + path);
        } catch (SQLException e) {
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
            
            final int[] count = {0};
            try (Connection conn = DriverManager.getConnection(DB_URL)) {
                String sql = "INSERT OR REPLACE INTO file_index (file_path, file_name, file_size, file_type, last_modified, content) VALUES (?, ?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    Files.walk(path)
                        .filter(Files::isRegularFile)
                        .filter(p -> isTextFile(p.toString()))
                        .limit(10) // 限制数量用于演示
                        .forEach(filePath -> {
                            try {
                                String content = extractTextContent(filePath);
                                if (content != null && !content.trim().isEmpty()) {
                                    pstmt.setString(1, filePath.toString());
                                    pstmt.setString(2, filePath.getFileName().toString());
                                    pstmt.setLong(3, Files.size(filePath));
                                    pstmt.setString(4, getFileType(filePath.toString()));
                                    pstmt.setTimestamp(5, new java.sql.Timestamp(Files.getLastModifiedTime(filePath).toMillis()));
                                    pstmt.setString(6, content.substring(0, Math.min(content.length(), 1000))); // 限制长度
                                    pstmt.executeUpdate();
                                    count[0]++;
                                }
                            } catch (Exception e) {
                                // 忽略错误，继续处理其他文件
                            }
                        });
                }
            }
            System.out.println("✅ 索引完成，处理了 " + count[0] + " 个文件");
        } catch (Exception e) {
            System.err.println("❌ 索引失败: " + e.getMessage());
        }
    }
    
    /**
     * 搜索文件
     */
    private static void searchFiles(String query) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            String sql = "SELECT * FROM file_index WHERE content LIKE ? OR file_name LIKE ? LIMIT 5";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                String searchPattern = "%" + query + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    System.out.println("🔍 搜索结果:");
                    int count = 0;
                    while (rs.next()) {
                        count++;
                        System.out.println("  " + count + ". " + rs.getString("file_name"));
                        System.out.println("     路径: " + rs.getString("file_path"));
                        System.out.println("     类型: " + rs.getString("file_type"));
                        System.out.println();
                    }
                    if (count == 0) {
                        System.out.println("  未找到匹配的文件");
                    }
                }
            }
        } catch (SQLException e) {
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
